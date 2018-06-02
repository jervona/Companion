package nyc.c4q.translator;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.language_translator.v2.LanguageTranslator;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nyc.c4q.translator.chat_rv.ChatAdapter;
import nyc.c4q.translator.contract.Contract;
import nyc.c4q.translator.model.Message;
import nyc.c4q.translator.model.SystemTranslationModel;
import nyc.c4q.translator.presenter.Presenter;

public class MainActivity extends AppCompatActivity implements Contract.View {


    @BindView(R.id.text)
    EditText editText;
    @BindView(R.id.voice_spinner)
    Spinner voice_spinner;
    @BindView(R.id.source)
    Spinner source_spinner;
    @BindView(R.id.target)
    Spinner target_spinner;
    @BindView(R.id.mic)
    ImageButton imageButton;
    @BindView(R.id.second_speaker_button)
    ImageButton secondaryUser;


    @BindView(R.id.recyclerView)
    RecyclerView chatRecyclerView;


    private boolean permissionToRecordAccepted = false;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String TAG = "MainActivity";
    private static final int RECORD_REQUEST_CODE = 101;

    private boolean listening = false;
    private MicrophoneInputStream capture;
    private MicrophoneHelper microphoneHelper;

    StreamPlayer streamPlayer;
    TextToSpeech textToSpeechService;
    LanguageTranslator languageTranslatorService;
    SpeechToText speechToTextService;
    ChatAdapter chatAdapter;

    private List<Message> chatList;
    String[] lan;
    String[] modelArray;
    String[] sourceIDMArray;

    String sourceHolder;
    String targetHolder;


    SystemTranslationModel systemTran;
    Contract.Presenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupServiceCredentials();


        systemTran = SystemTranslationModel.getInstance();
        chatList = new ArrayList<>();
        lan = getResources().getStringArray(R.array.language_ibm_array);
        sourceIDMArray = getResources().getStringArray(R.array.soruce_ibm_array);
        modelArray = getResources().getStringArray(R.array.Broadband_model_ibm_array);
        microphoneHelper = new MicrophoneHelper(this);

        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied");
            makeRequest();
        }

        presenter = new Presenter(this, textToSpeechService, languageTranslatorService, speechToTextService);

        setupRecyclerView();
        setSpinners();
    }


    @OnClick(R.id.mic)
    public void primary() {
        recordMessage("1");
    }

    @OnClick(R.id.second_speaker_button)
    public void secondary() {
        recordMessage("0");
    }

    public void recordMessage(String user) {
        if (!listening) {
            switch (user) {
                case "0":
                    sourceHolder = systemTran.getSource();
                    targetHolder = systemTran.getTarget();
                    systemTran.setTarget(sourceHolder);
                    systemTran.setSource(targetHolder);
                    break;
            }
            capture = microphoneHelper.getInputStream(true);
            presenter.recordAudio(capture);
            listening = true;
            Toast.makeText(MainActivity.this, "Listening....Click to Stop", Toast.LENGTH_LONG).show();
        } else {
            microphoneHelper.closeInputStream();
            listening = false;
            Message input = new Message(user, editText.getText().toString());
            chatList.add(input);
            chatAdapter.notifyDataSetChanged();
            presenter.translate(editText.getText().toString());
            editText.setText("");
            systemTran.setTarget(targetHolder);
            systemTran.setSource(sourceHolder);
            Toast.makeText(MainActivity.this, "Stopped Listening....Click to Start", Toast.LENGTH_LONG).show();
        }
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(chatList);
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void setSpinners() {
        voice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                systemTran.setChosenVoice(item);
                Log.e("Choosing Voice", item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        source_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("source_spinner", sourceIDMArray[position]);
                systemTran.setSource(sourceIDMArray[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        target_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("target_spinner", sourceIDMArray[position]);
                systemTran.setTarget(sourceIDMArray[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupServiceCredentials() {
        textToSpeechService = new TextToSpeech();
        String user = getResources().getString(R.string.text_to_speech_username);
        String password = getResources().getString(R.string.text_to_speech_password);
        textToSpeechService.setUsernameAndPassword(user, password);

        languageTranslatorService = new LanguageTranslator();
        String transUser = getResources().getString(R.string.language_translator_username);
        String transPassword = getResources().getString(R.string.language_translator_password);
        languageTranslatorService.setUsernameAndPassword(transUser, transPassword);

        speechToTextService = new SpeechToText();
        String speechToTextUser = getResources().getString(R.string.speech_to_text_username);
        String speechToTextPassword = getResources().getString(R.string.speech_to_text_password);
        speechToTextService.setUsernameAndPassword(speechToTextUser, speechToTextPassword);
    }

    private boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        // Check for network connections
        if (isConnected) {
            return true;
        } else {
            Toast.makeText(this, " No Internet Connection available ", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO}, MicrophoneHelper.REQUEST_PERMISSION);
    }

    private void showTextOnUi(final String str) {
        runOnUiThread(() -> editText.setText(str));
    }


    @Override
    public void playStream(InputStream stream) {
        streamPlayer = new StreamPlayer();
        streamPlayer.playStream(stream);
    }

    @Override
    public void showErrorMessage() {

    }

    @Override
    public void showText(String text) {
        showTextOnUi(text);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            case RECORD_REQUEST_CODE: {
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission has been denied by user");
                } else {
                    Log.i(TAG, "Permission has been granted by user");
                }
                return;
            }
            case MicrophoneHelper.REQUEST_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission to record audio denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
