package com.tugcearas.musicbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BookDetails extends AppCompatActivity {

    ImageView imageView;
    EditText singerName, musicName, musicType ;
    Button button;
    Bitmap bitmapImage;
    SQLiteDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);


        imageView = findViewById(R.id.selectImageView);
        singerName = findViewById(R.id.singerNameText);
        musicName = findViewById(R.id.musicNameText);
        musicType = findViewById(R.id.musicTypeText);
        button = findViewById(R.id.saveButton);
        database = this.openOrCreateDatabase("MusicBook",MODE_PRIVATE,null);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        // add new music
        if (info.matches("new")){

            singerName.setText("");
            musicName.setText("");
            musicType.setText("");
            button.setVisibility(View.VISIBLE);

            // Was in a byte array will be converted to a bitmap
            Bitmap selectedimage = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.selectimage);
            imageView.setImageBitmap(selectedimage);

        }

        else {

            int musicId = intent.getIntExtra("musicId",1);
            button.setVisibility(View.INVISIBLE);
            singerName.setEnabled(false);
            musicName.setEnabled(false);
            musicType.setEnabled(false);


            try {

                Cursor cursor = database.rawQuery("SELECT * FROM musicbook WHERE id =?", new String[] {String.valueOf(musicId)});

                int singerNameIndex = cursor.getColumnIndex("singerName");
                int musicNameIndex = cursor.getColumnIndex("musicName");
                int musicTypeIndex = cursor.getColumnIndex("musicType");
                int imageIndex = cursor.getColumnIndex("image");


                while (cursor.moveToNext()){

                    singerName.setText(cursor.getString(singerNameIndex));
                    musicName.setText(cursor.getString(musicNameIndex));
                    musicType.setText(cursor.getString(musicTypeIndex));

                    byte[] bytes = cursor.getBlob(imageIndex);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    imageView.setImageBitmap(bitmap);

                }


                cursor.close();

            }

            catch(Exception e) {


            }

        }



    }


    public void selectImage(View view){


        // If permission is not granted, permission is requested
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        }

        // if allowed
        else {

            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentToGallery,2);

        }



    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 1){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                Intent intentToGallery = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentToGallery,2);
            }


        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 2 && resultCode == RESULT_OK && data!=null){

            Uri imageUri = data.getData();


            try {
                if (Build.VERSION.SDK_INT >= 28) {

                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(),imageUri);
                    bitmapImage = ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(bitmapImage);

                }

                else
                {

                    bitmapImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    imageView.setImageBitmap(bitmapImage);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }



    public void save(View view){


        String sName = singerName.getText().toString();
        String mName = musicName.getText().toString();
        String mType = musicType.getText().toString();

        Bitmap smallImage = makeSmallerImage(bitmapImage,300);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try {


            database = this.openOrCreateDatabase("MusicBook", MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS musicbook (id INTEGER PRIMARY KEY, singerName VARCHAR, musicName VARCHAR, musicType VARCHAR, image BLOB)");


            String sqlString = "INSERT INTO musicbook (singerName,musicName,musicType,image) VALUES (?,?,?,?)";
            SQLiteStatement statement = database.compileStatement(sqlString);
            statement.bindString(1,sName);
            statement.bindString(2,mName);
            statement.bindString(3,mType);
            statement.bindBlob(4,byteArray);
            statement.execute();


        }


        catch (Exception e){

            e.printStackTrace();
        }


        // Used to turn off activities.
        Intent intent = new Intent(BookDetails.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);


    }


    public Bitmap makeSmallerImage(Bitmap bitmapImage,int maximumSize){

        int widht = bitmapImage.getWidth();
        int height = bitmapImage.getHeight();

        float ratio = (float)widht / (float)height;

        if (ratio > 1){

            widht = maximumSize;
            height = (int) (widht/ratio);
        }

        else {

            height = maximumSize;
            widht = (int)(height*ratio);
        }


        return Bitmap.createScaledBitmap(bitmapImage,widht,height,true);
    }


}