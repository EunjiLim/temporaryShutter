package org.androidtown.shutterwordbook.Fragment;

import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.androidtown.shutterwordbook.Activity.MainActivity;
import org.androidtown.shutterwordbook.Activity.StartActivity;
import org.androidtown.shutterwordbook.Helper.DictionaryOpenHelper;
import org.androidtown.shutterwordbook.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

public class DictionaryFragment extends Fragment implements View.OnClickListener, TextToSpeech.OnInitListener {
    // DB

    private Button buttonSearch;
    private Button buttonCamera;
    private Button buttonSpeak;
    private EditText editWord;  // 입력한 단어
    private TextView textMean;  // 사전의미
    private TextView textWord;  // 사전에서 보여주는 단어

    // List
    private ArrayAdapter<String> adapter;
    private ListView listWord;  // 단어리스트

    // DB관련
    private SQLiteDatabase db;
    DictionaryOpenHelper mHelper;

    // 발음
    TextToSpeech tts;
    boolean ttsActive = false;

    // 단어 찾기
    String toFind="null";
    String result="null";

    // 단어 사전 확장
    FragmentTransaction fragementTransaction;

    // 단어장에 단어 추가
    boolean isWordbook;

    //글자 인식 확인 태그
    private static final String TAG = "SimpleAndroidOCR.java";

    //사진이 저장될 경로
    //외부 저장소의 최상위 경로를
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().getAbsolutePath().toString() + "/SimpleAndroidOCR/";

    // You should have the trained data file in assets folder
    // You can get them at:
    // http://code.google.com/p/tesseract-ocr/downloads/list
    public static final String lang = "eng";

    //URI
    protected Uri outputFileUri;

    //임시 파일 경로
    protected String _path;

    protected static final String PHOTO_TAKEN = "photo_taken";

    protected boolean _taken;

    public DictionaryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Log.v(TAG, "************************DATA_PATH : " + DATA_PATH);
        View rootView =  inflater.inflate(R.layout.fragment_dictionary, container, false);

        Log.v(TAG,  isExternalStorageWritable() );
        // 레이아웃 연결
        buttonCamera = (Button) rootView.findViewById(R.id.button_camera);
        buttonSearch = (Button) rootView.findViewById(R.id.button_search);
        buttonSpeak = (Button) rootView.findViewById(R.id.button_speak);
        editWord = (EditText) rootView.findViewById(R.id.editText_word);
        listWord = (ListView) rootView.findViewById(R.id.listView_words);
        textMean = (TextView) rootView.findViewById(R.id.textView_meaning);
        textWord = (TextView) rootView.findViewById(R.id.textView_word);

        // 초기화
        mHelper = new DictionaryOpenHelper(getActivity());
        tts = new TextToSpeech(getActivity(), this);
        buttonSearch.setEnabled(true);

        // adapter
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, StartActivity.getWords());

        // adapter연결
        listWord.setAdapter(adapter);

        // 리스너 등록
        buttonSearch.setOnClickListener((View.OnClickListener) this);
        buttonCamera.setOnClickListener((View.OnClickListener) this);
        buttonSpeak.setOnClickListener((View.OnClickListener) this);



        //사진 저장할 경로
        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

        for (String path : paths) {
            File dir = new File(path);
            Log.v(TAG, "path 는??? " + path );
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }
        }

        // lang.traineddata file with the app (in assets folder)
        // You can get them at:
        // http://code.google.com/p/tesseract-ocr/downloads/list
        // This area needs work and optimization
/*        File train = new File(DATA_PATH+ "tessdata/" + lang + ".traineddata");
        if(train.exists()){
            Log.v(TAG,"train.exists() true");
        } else
           Log.v(TAG,"train.exists() false");*/

        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {
                Log.v(TAG,"if문 안에 들어옴  !(new File(DATA_PATH, 어쩌고저쩌고).exists()");
                AssetManager assetManager = getActivity().getAssets();
                Log.v(TAG,"1");
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                Log.v(TAG,"2");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((len = gin.read(buf)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }

        //임시 파일 경로
        _path = DATA_PATH + "/ocr.jpg";

        // 리스트를 눌렀을 때
        listWord.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Log.d("MyDicFrag", "click");
                    toFind = parent.getItemAtPosition(position).toString();
                    textWord.setText(toFind);
                    search(toFind, false);

                } catch (Exception e){
                    Log.d("MyDicFrag", "click error " + e.toString());
                }
            }
        });

        /* End of onItemClick methd */

        /* Start of setOnItemLongClickListener
        *  해당 단어를 길게 누를 시 단어장에 단어 추가
        * */
        listWord.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                toFind = parent.getItemAtPosition(position).toString();
                textWord.setText(toFind);

                // PopupMenu 객체 생성
                final PopupMenu popupMenu = new PopupMenu(getActivity(), view);  // Activity에서는 getContext()나 this, Fragment에서는 getActivity
                // popupMenu에 들어갈 MenuItem 추가
                popupMenu.getMenuInflater().inflate(R.menu.menu_addword, popupMenu.getMenu());

                //PopupMenu의 MenuItem을 클릭하는 것을 감지하는 listener 설정
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // TODO Auto-generated method stub

                        if(item.getItemId() == R.id.addword){
                            // 단어장에 추가 하고자 하는 단어와 뜻 찾음.
                            search(toFind, false);
                            showWordbookList(toFind, result);     // 기존에 존재하는 단어장 리스트 출력
                        }
                        return false;
                    }
                    });
                        popupMenu.show();
                        return false;
            }
        });
        /* End of  setOnItemLongClickListener */


        /* 단어 창을 누르면 단어 뜻 확대 */
        textWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragementTransaction = getFragmentManager().beginTransaction();
                search(toFind, true);
                fragementTransaction.replace(R.id.first_page, new WordmeanFragment(toFind, result));
                System.out.println(result);
                fragementTransaction.addToBackStack(null);

                fragementTransaction.commit();

            }
        });



        return rootView;
    }
    /* End of onCreateView() */

    /* 기존의 단어장 리스트 불러오기*/
    public void showWordbookList(String word, String meaning){

        // word : 추가할 단어 meaning : 추가할 단어의 뜻
    }

    /* 새로운 단어장 생성하기 */
    public void createWordbook(){

    }

    /* 선택한 단어를 단어장에 추가하기*/
    public void addWord(){

    }

    /* Start of onClick */
    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.button_search :
                toFind = editWord.getText().toString();
                search(toFind, true);
                break;

            case R.id.button_camera :
                Toast.makeText(getActivity(), "camera_button", Toast.LENGTH_LONG).show();
                camera();
                break;

            case R.id.button_speak :
                speak();
                break;
        }
    }

    /* End of onClick() */

    // 검색버튼 눌렀을 때
    public void search(String toFind, Boolean move) {
        db = mHelper.getReadableDatabase();
        Cursor cursor;

        String sql = "SELECT * from Dictionary where word like \"" + toFind + "%\"";
        cursor = db.rawQuery(sql, null);

        if(cursor.getCount()==0) {
            Toast.makeText(getActivity(), "찾는 단어가 존재하지 않습니다", Toast.LENGTH_LONG).show();

        }
        else {
            cursor.moveToFirst();

            int _id = cursor.getInt(0);
            String word = cursor.getString(1);
            result = cursor.getString(2);

            if(move)
                listWord.setSelection(_id -1);
            textMean.setText(result);
            textWord.setText(toFind);
        }
        cursor.close();
        mHelper.close();
    }

    //camera 버튼 눌렀을 때
    public void camera(){

        Log.v(TAG,_path);
        File file = new File(_path);

        //Uri는 자원에 접근하기 위한 주소이다.
        outputFileUri = Uri.fromFile(file);

        OutputStream out = null;
        //카메라 액티비티를 실행시키는 소스
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        /**
         *        크롭하기 전 사진 rotate를 잡아준다.
         */
        BitmapFactory.Options options = new BitmapFactory.Options();

        //이미지를 bitmap형태로 불러들임
        Bitmap bitmap = BitmapFactory.decodeFile(_path,  options);

        /*ExifInterface란 디지털 사진의 이미지 정보
        이미지 기본값, 크기, 화소 및 카메라 정보, 조리개, 노출 정도 등*/
        try {
            ExifInterface exif = new ExifInterface(_path);
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            Log.v(TAG, "Orient: " + exifOrientation);

            int rotate = 0;

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }

            Log.v(TAG, "Rotation: " + rotate);
            //카메라를 돌린 채로 사진을 찍었을 때 돌려준다.
            if (rotate != 0) {

                // Getting width & height of the given image.
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();

                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);

                // Rotating Bitmap
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);

                out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Couldn't correct orientation: " + e.toString());
        }

        Log.v(TAG, "startCameraActivityEnd");
        //촬영한 결과의 반환 값을 받기 위해 startActivityForResult로 넘겨준다.
        //0은 어떤 액티비티에서 반환값이 왔는지를 식별하기 위한 식별값이다.
        startActivityForResult(intent, 0);
    }

    //crop 시작
    protected void startCrop() {

        Intent intent = new Intent("com.android.camera.action.CROP");
        //_path 파일에서 불러온 outputFileUri에 다시 덮어씌웠는데
        intent.setDataAndType(outputFileUri, "image/*");

        Log.v(TAG, "startCropActivity");

        //intent.putExtra("outputX", 90);
        //intent.putExtra("outputY", 45);
        //intent.putExtra("aspectX", 1);
        //intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        //intent.putExtra("return-data", true); //저장 버튼 클릭 시 Bundle을 통해 bitmap으로 데이터를 받아옴
        intent.putExtra("output", outputFileUri);

        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(TAG, "resultCode: " + resultCode);

        if (resultCode == -1) {
            if(requestCode == 0){
                startCrop();
            }
            else if(requestCode == 1){
                onPhotoTaken();
            }
        } else {  //사진 찍고 취소 버튼 눌렀을 때
            Log.v(TAG, "User cancelled");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(DictionaryFragment.PHOTO_TAKEN, _taken);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i(TAG, "onRestoreInstanceState()");
        if (savedInstanceState.getBoolean(DictionaryFragment.PHOTO_TAKEN)) {
            onPhotoTaken();
        }
    }

    //사진 가져오기
    protected void onPhotoTaken() {
        _taken = true;

        //읽어들이려는 이미지 정보를 알아내기 위한 객체
        BitmapFactory.Options options = new BitmapFactory.Options();
        //이미지의 해상도를 몇분의 1로 줄일 지를 나타낸다. (1/4)
        //가로 세로 1/4 크기로 줄여 읽어드림, 면적은 1/16
        options.inSampleSize = 4;

        Bitmap bitmap = BitmapFactory.decodeFile(_path,  options);

        // _image.setImageBitmap( bitmap );

        Log.v(TAG, "Before baseApi");

        //baseApi 경로에서 이미지 받아옴?
        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(DATA_PATH, lang);
        baseApi.setImage(bitmap);

        String recognizedText = baseApi.getUTF8Text();

        baseApi.end();

        // You now have the text in recognizedText var, you can do anything with it.
        // We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
        // so that garbage doesn't make it to the display.

        Log.v(TAG, "OCRED TEXT: " + recognizedText);

        if ( lang.equalsIgnoreCase("eng") ) {
            recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
        }

        recognizedText = recognizedText.trim();

        if ( recognizedText.length() != 0 ) {
            editWord.setText(editWord.getText().toString().length() == 0 ? recognizedText : editWord.getText() + " " + recognizedText);
            editWord.setSelection(editWord.getText().toString().length());
        }

        // Cycle done.
    }

    // tts
    public void speak() {

        // 읽을 단어를 가져온다. 오른쪽 화면에서 가져옴.
        String toSpeak = textWord.getText().toString();

        // queue를 비우고 지정한 단어를 발음을 하게 한다
        tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }

    // tts listener
    @Override
    public void onInit(int status) {

        // tts가 가능한 경우
        if(status == TextToSpeech.SUCCESS) {
            buttonSearch.setEnabled(true);

            String toSpeak = textWord.getText().toString();

            int result = tts.setLanguage(Locale.US); // 언어설정. 미국, 영국,호주 등 다양한 발음 제공 가능할 듯
            // setPitch() - 발음의 높낮이
            // setSpeechRate() - 발음 속도

            // 데이터가 없거나 지원하지 않는 경우
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {

                Toast.makeText(getActivity(), R.string.text_tts_error,Toast.LENGTH_SHORT).show();
            }
            else {
                buttonSpeak.setEnabled(true);
            }
        }
        else {
            Toast.makeText(getActivity(), R.string.text_tts_error,Toast.LENGTH_SHORT).show();
            buttonSpeak.setEnabled(false);
        }

    }

    @Override
    public void onDestroy() {
        // tts를 꼭 종료시켜야함!!!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    public String isExternalStorageWritable(){
        String state = Environment.getExternalStorageState();
        if( Environment.MEDIA_MOUNTED.equals( state)){
            return "true";

        }
        return "false";
    }



}