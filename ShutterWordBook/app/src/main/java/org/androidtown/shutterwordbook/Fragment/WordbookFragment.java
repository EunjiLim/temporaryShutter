package org.androidtown.shutterwordbook.Fragment;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.androidtown.shutterwordbook.Helper.WordbooksOpenHelper;
import org.androidtown.shutterwordbook.Activity.StartActivity;
import org.androidtown.shutterwordbook.Helper.DictionaryOpenHelper;
import org.androidtown.shutterwordbook.R;
import org.androidtown.shutterwordbook.Helper.WordbooksOpenHelper;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class WordbookFragment extends Fragment {


    //
    //  DB 관련
    private SQLiteDatabase db;
    WordbooksOpenHelper dbHelper;

    private static String DB_NAME = "Wordbooks.sqlite";
    private int listCount = 0;
    private String[] nameList = null;       // 단어장 이름을 담는 배열
    //

    private ListView listWordbook;  // 단어장 리스트
    private ArrayAdapter<String> adapter;
    private ArrayList wordbooks;

    /* Start of onCreate View*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_wordbook, container, false);
        listWordbook = (ListView) rootView.findViewById(R.id.listView_wordbooks);

       boolean isOpen = openDatabase();
        if(isOpen){
            initList();
        }
      return rootView;
    }
//


    /* 데이터베이스 열기 */
    public boolean openDatabase(){
        System.out.println("opening database"+WordbooksOpenHelper.DATABASE_NAME);
        dbHelper = new WordbooksOpenHelper(getActivity());
    //    db = dbHelper.getWritableDatabase();
        return true;
    }

    //

    public void initList(){
        try {
            db = dbHelper.getReadableDatabase();

            Cursor cursor;
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

            if (cursor.moveToFirst()) {
                while ( !cursor.isAfterLast()) {
                    System.out.println("DB : "+cursor.getString(0));
                  //  Toast.makeText(Dbinput.this, "Table Name=> "+cursor.getString(0), Toast.LENGTH_LONG).show();
                    cursor.moveToNext();
                }
            }
            //       String sql = "SELECT name  from Wordbook";



/*
            cursor = db.rawQuery(sql, null);
            while(cursor.moveToNext())
            {
                String name = cursor.getString(0);
                System.out.println("cursor : "+cursor.getString(1));
                wordbooks = new ArrayList<String >();
                wordbooks.add(name);
                listCount++;
            }
            System.out.println(wordbooks);
            adapter =   new ArrayAdapter<String>(getActivity().getBaseContext(), android.R.layout.simple_list_item_1, wordbooks);
            listWordbook.setAdapter(adapter);
*/

        } catch (Exception e) {
            System.out.println("에러 "+e.toString());
            Log.d("StartActivityyyy", "error in init : " + e.toString());
        }
    }

}
