package org.androidtown.shutterwordbook.Fragment;


import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.androidtown.shutterwordbook.R;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class WordmeanFragment extends Fragment implements View.OnClickListener, TextToSpeech.OnInitListener {

    private TextView textMean;  // 사전의미
    private TextView textWord;  // 사전에서 보여주는 단어

    // 발음
    private Button buttonSpeak;
    TextToSpeech tts;
    boolean ttsActive = false;

    String word;
    String mean;

    public WordmeanFragment() {
        // Required empty public constructor
    }
    public WordmeanFragment(String textWord, String textMean) {
        // Required empty public constructor
        this.word = textWord;
        this.mean = textMean;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_wordmean, container, false);

        // layout
        textWord = (TextView)rootView.findViewById(R.id.textView_word);
        textMean = (TextView)rootView.findViewById(R.id.textView_meaning);
        buttonSpeak = (Button) rootView.findViewById(R.id.button_speak_mean_fragment);

        // init
        textWord.setText(word);
        textMean.setText(mean);
        tts = new TextToSpeech(getActivity(), this);

        // listener
        buttonSpeak.setOnClickListener((View.OnClickListener) this);

        return rootView;
    }


    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.button_speak_mean_fragment :
                speak();
                break;
        }
    }

    // tts
    public void speak() {

        // 읽을 단어를 가져온다. 오른쪽 화면에서 가져옴.
        String toSpeak = textWord.getText().toString();
        Log.d("speak", toSpeak);

        // queue를 비우고 지정한 단어를 발음을 하게 한다
        tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }

    // tts listener
    @Override
    public void onInit(int status) {

        // tts가 가능한 경우
        if(status == TextToSpeech.SUCCESS) {

            String toSpeak = textWord.getText().toString();

            int result = tts.setLanguage(Locale.US); // 언어설정. 미국, 영국,호주 등 다양한 발음 제공 가능할 듯
            // setPitch() - 발음의 높낮이
            // setSpeechRate() - 발음 속도

            // 데이터가 없거나 지원하지 않는 경우
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {

                Toast.makeText(getActivity(), R.string.text_tts_error, Toast.LENGTH_SHORT).show();
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

}