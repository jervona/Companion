package nyc.c4q.translator;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.widget.EditText;

import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.http.ServiceCallback;
import com.ibm.watson.developer_cloud.language_translator.v2.LanguageTranslator;
import com.ibm.watson.developer_cloud.language_translator.v2.model.TranslateOptions;
import com.ibm.watson.developer_cloud.language_translator.v2.model.Translation;
import com.ibm.watson.developer_cloud.language_translator.v2.model.TranslationResult;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;

import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.text)
    EditText editText;

    private boolean permissionToRecordAccepted = false;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String TAG = "MainActivity";
    private static final int RECORD_REQUEST_CODE = 101;


    StreamPlayer streamPlayer;
    TextToSpeech service;
    LanguageTranslator languageTranslator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        service = new TextToSpeech();
        String user = getResources().getString(R.string.text_to_speech_username);
        String password = getResources().getString(R.string.text_to_speech_password);
        service.setUsernameAndPassword(user, password);


        languageTranslator = new LanguageTranslator();
        String transUser = getResources().getString(R.string.language_translator_username);
        String transPassword = getResources().getString(R.string.language_translator_password);
        languageTranslator.setUsernameAndPassword(transUser, transPassword);

    }


    public void getVoice(String str) {
        streamPlayer = new StreamPlayer();
        service.synthesize(str, Voice.ES_LAURA).enqueue(new ServiceCallback<InputStream>() {
            @Override
            public void onResponse(InputStream response) {
                streamPlayer.playStream(response);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Error TTS", e.getMessage());
            }
        });
    }

    @OnClick(R.id.button)
    public void translate() {
        TranslateOptions translateOptions = new TranslateOptions.Builder()
                .addText(editText.getText().toString())
                .modelId("en-es")
                .build();


       languageTranslator.translate(translateOptions).enqueue(new ServiceCallback<TranslationResult>() {
           @Override
           public void onResponse(TranslationResult response) {
               String tran =response.getTranslations().get(0).getTranslation();
               getVoice(tran);
               Log.e("", tran);
           }

           @Override
           public void onFailure(Exception e) {
               Log.e("Error Translate", e.getMessage());
           }
       });

    }


}
