package org.androidtown.shutterwordbook.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import org.androidtown.shutterwordbook.Helper.DictionaryOpenHelper;
import org.androidtown.shutterwordbook.R;

import java.util.ArrayList;

public class StartActivity extends FragmentActivity {


    // List
    private static ArrayList<String> words;
    public static ArrayList<String> getWords() {
        return words;
    }

    private boolean checkInitList;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        words = new ArrayList<String>();

        Thread thread1 = new backgroundThread();
        thread1.start();
    }



    class backgroundThread extends Thread{
        public void run() {
            ////db가 없는 경우에만, 최초실행인 경우에만 ///

            SharedPreferences pref = getSharedPreferences("db", Context.MODE_PRIVATE);
            Boolean existDB = pref.getBoolean("copyDB", false); // copyDB를 한 적 있는지 확인

            if (existDB == false) {

                DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(StartActivity.this);
                dbHelper.copyDB();
                Log.d("StartActivityyyy", "copyDB Thread");

                // copyDB 설정 변경
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("copyDB", true);
                editor.commit();
            }
            // listView에 add
            initListView();
            if (checkInitList == true) {
                Intent in = new Intent(StartActivity.this, MainActivity.class);
                startActivity(in);
                finish();
            }
        }

    }

    /* Push dictionary's data into Listview */
    public void initListView() {
        try {
            SQLiteDatabase db;
            DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(StartActivity.this);
            db = dbHelper.getReadableDatabase();
            Cursor cursor;
            String sql = "SELECT word from Dictionary";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                String word = cursor.getString(0);
                words.add(word);
            }
            checkInitList = true;
        } catch (Exception e) {
            Log.d("StartActivityyyy", "error in init : " + e.toString());
        }
    }
}
