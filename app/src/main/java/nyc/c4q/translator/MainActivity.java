package nyc.c4q.translator;



import android.support.constraint.Group;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxAdapterView;
import com.skyfishjy.library.RippleBackground;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import nyc.c4q.translator.chat_rv.ChatAdapter;
import nyc.c4q.translator.contract.Contract;
import nyc.c4q.translator.Pojo.Message;
import nyc.c4q.translator.dependancies.BaseApp;
import nyc.c4q.translator.presenter.Presenter;
import nyc.c4q.translator.singleton.SystemTranslationModel;

public class MainActivity extends AppCompatActivity implements Contract.View {

    @BindView(R.id.text) EditText editText;
    @BindView(R.id.voice_spinner) Spinner voice_spinner;
    @BindView(R.id.source) Spinner source_spinner;
    @BindView(R.id.target) Spinner target_spinner;
    @BindView(R.id.mic) FloatingActionButton primaryButton;
    @BindView(R.id.second_speaker_button) FloatingActionButton secondaryUser;
    @BindView(R.id.recyclerView) RecyclerView chatRecyclerView;
    @BindView(R.id.ripple_background) RippleBackground rippleBackground;
    @BindView(R.id.centerImage) FloatingActionButton imageView;
    @BindView(R.id.group) Group group1;
    @BindView(R.id.group2) Group group2;

    @Inject Presenter presenter;
    @Inject SystemTranslationModel systemTran;


    private static String TAG = "MainActivity";
    private MicrophoneHelper microphoneHelper;

    public StreamPlayer streamPlayer;
    ChatAdapter chatAdapter;

    String[] lan;
    String[] modelArray;
    String[] sourceIDMArray;

    RxPermissions rxPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        BaseApp application = (BaseApp) getApplicationContext();
        application.getMyComponent().inject(this);
        setupDataStructions();
        setupRecyclerView();
        setSpinners();
        microphoneHelper = new MicrophoneHelper(this);
        rxPermissions = new RxPermissions(this);
        streamPlayer = new StreamPlayer();
        presenter.setView(this);
        presenter.checkPermission(rxPermissions);
        presenter.checkInternetConnection();
    }

    @OnClick(R.id.translatedMessage)
    public void translatedMessage() {
        systemTran.setGetVoice(true);
        presenter.translateString(editText.getText().toString());
    }

    @OnClick(R.id.mic)
    public void primary() {
        presenter.primary();
        toggleVisibility();
    }

    @OnClick(R.id.second_speaker_button)
    public void secondary() {
        presenter.secondary();
        toggleVisibility();
    }

    @OnClick(R.id.back)
    public void toggleVisibility() {
        if (group2.getVisibility() == View.GONE) {
            group2.setVisibility(View.VISIBLE);
            group1.setVisibility(View.GONE);
        } else {
            presenter.addCurrentToChat();
            group1.setVisibility(View.VISIBLE);
            group2.setVisibility(View.GONE);
        }
    }

    public void recordToggle() {
        RxView.touches(imageView)
                .subscribe(motionEvent -> {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            rippleBackground.startRippleAnimation();
                            startRecording();
                            break;
                        case MotionEvent.ACTION_UP:
                            rippleBackground.stopRippleAnimation();
                            stopRecording();
                            break;
                    }
                });
    }

    public void startRecording() {
        MicrophoneInputStream capture = microphoneHelper.getInputStream(true);
        presenter.startRecording(capture);
    }

    public void stopRecording() {
        microphoneHelper.closeInputStream();
    }

    public void playVoice(InputStream stream) {
        streamPlayer.playStream(stream);
    }

    private void setupRecyclerView() {
        List<Message> chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatList);
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void setSpinners() {
        RxAdapterView.itemSelections(voice_spinner)
                .subscribe(integer -> systemTran.setChosenVoice(voice_spinner.getItemAtPosition(integer).toString()));

        RxAdapterView.itemSelections(source_spinner)
                .subscribe(position -> {
                    systemTran.setSource(sourceIDMArray[position]);
                    presenter.setSourceHolder(sourceIDMArray[position]);
                });

        RxAdapterView.itemSelections(target_spinner)
                .subscribe(position -> {
                    systemTran.setTarget(sourceIDMArray[position]);
                    presenter.setTargetHolder(sourceIDMArray[position]);
                });
    }

    private void setupDataStructions() {
        lan = getResources().getStringArray(R.array.language_ibm_array);
        sourceIDMArray = getResources().getStringArray(R.array.soruce_ibm_array);
        modelArray = getResources().getStringArray(R.array.Broadband_model_ibm_array);
    }

    @Override
    public void playStream(InputStream streamPlayer) {
        playVoice(streamPlayer);
    }

    @Override
    public void showErrorMessage() {

    }

    @Override
    public void showText(String text) {
        editText.setText(text);
    }

    @Override
    public void accessGranted() {
        recordToggle();
    }

    @Override
    public void groupToggle(boolean b) {
        group1.setClickable(b);
    }

    @Override
    public void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateChatList(List<Message> chatList) {
        chatAdapter.updateTicketListItems(chatList);
    }

    @Override
    public void setPresenter(Contract.Presenter p) {

    }
}