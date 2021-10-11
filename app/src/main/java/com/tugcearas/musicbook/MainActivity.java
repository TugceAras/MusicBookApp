package com.tugcearas.musicbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    ListView listView;
    ArrayList<String> musicNameArray;
    ArrayList<Integer> idArray;
    ArrayAdapter<String> adapter;
    SQLiteDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = this.openOrCreateDatabase("MusicBook",MODE_PRIVATE,null);
        database.execSQL("DELETE FROM musicbook WHERE musicName='lorke'");

        listView = findViewById(R.id.listView);
        musicNameArray = new ArrayList<>();
        idArray = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,musicNameArray);
        listView.setAdapter(adapter);



        // extracting data with listview
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(MainActivity.this,BookDetails.class);
                intent.putExtra("musicId",idArray.get(position));
                intent.putExtra("info","old");
                startActivity(intent);


            }
        });

        getData();
    }

    public void getData(){

        try {

           // database = this.openOrCreateDatabase("MusicBook",MODE_PRIVATE,null);
            Cursor cursor = database.rawQuery("SELECT * FROM musicbook", null);

            int musicNameIndex = cursor.getColumnIndex("musicName");
            int musicIdIndex = cursor.getColumnIndex("id");


            while (cursor.moveToNext()) {

                musicNameArray.add(cursor.getString(musicNameIndex));
                idArray.add(cursor.getInt(musicIdIndex));

            }


            adapter.notifyDataSetChanged();
            cursor.close();

        }

        catch (Exception e) {
            e.printStackTrace();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {  //We have specified which menu to show.

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_music,menu);


        return super.onCreateOptionsMenu(menu);
    }



    // Clicking somewhere in the menu is written what should be
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        if (item.getItemId() == R.id.add_music_item){

            Intent intent = new Intent(MainActivity.this,BookDetails.class);
            intent.putExtra("info","new");
            startActivity(intent);


        }


        return super.onOptionsItemSelected(item);
    }
}