package org.androidtown.shutterwordbook.Helper;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ehye on 2015-08-02.
 */
/* 새로운 헬퍼 클래스 정의*/
public class WordbooksOpenHelper extends SQLiteOpenHelper {


    private static final String PACKAGE_DIR = "/data/data/org.androidtown.shutterwordbook/databases";   // 로컬 db 저장
    public static String DATABASE_NAME = "Wordbooks.sqlite";    // 로컬 db명
    public static int DATABASE_VERSION = 1;




    /* 생성자에서 데이터베이스 이름과 버전을 이용해 상위 클래스의 생성자 호출*/
    public WordbooksOpenHelper (Context context){
        super(context, PACKAGE_DIR+"/"+DATABASE_NAME, null, 1);
        setDatabase(context);   //sdetDatabase에 context 부여
    }

    /* 데이터베이스 파일이 처음 만들어질 때 호출되는 메소드 정의 */
    public void onCreate(SQLiteDatabase db){

    }

    /* 데이터베이스가 오픈될 때 호출되는 메소드 정의 */
    public void onOpen(SQLiteDatabase db){

    };
    /* 데이터베이스의 버전이 바뀌었을 때 호출되는 메소드 정의의*/

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
         if(newVersion > 1){
             db.execSQL("DROP TABLE IF EXISTS Wordbooks");
              onCreate(db);
         }

    }

    public static void setDatabase(Context context){
        File folder = new File(PACKAGE_DIR);

        if(folder.exists()) {
        } else {
            folder.mkdirs();
        }
        AssetManager assetManager = context.getResources().getAssets(); // context가 없으면 assets폴더를 찾지 못한다.
        File outFile = new File(PACKAGE_DIR + "/" + DATABASE_NAME);
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        long fileSize = 0;
        try{
            inputStream = assetManager.open(DATABASE_NAME, AssetManager.ACCESS_BUFFER);
            fileSize = inputStream.available();

            if(outFile.length() < 0){
                byte[] tempData = new byte[(int) fileSize];
                inputStream.read(tempData);
                inputStream.close();
                fileOutputStream = new FileOutputStream(outFile);
                fileOutputStream.write(tempData);
                fileOutputStream.close();
            }else { }

        } catch(IOException e){}
          // 이곳에 public으로 쿼리 코드 생성?
    }
}
