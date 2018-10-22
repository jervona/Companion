package nyc.c4q.translator.presenter;


import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.developer_cloud.http.ServiceCallback;
import com.ibm.watson.developer_cloud.language_translator.v2.LanguageTranslator;
import com.ibm.watson.developer_cloud.language_translator.v2.model.TranslateOptions;
import com.ibm.watson.developer_cloud.language_translator.v2.model.TranslationResult;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;
import com.tbruyelle.rxpermissions2.RxPermissions;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;


import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import nyc.c4q.translator.Pojo.Message;
import nyc.c4q.translator.R;
import nyc.c4q.translator.contract.Contract;
import nyc.c4q.translator.singleton.SystemTranslationModel;

/**
 * Created by jervon.arnoldd on 6/2/18.
 */
@Singleton
public class Presenter implements Contract.Presenter {
    private TextToSpeech textToSpeechService;
    private LanguageTranslator languageTranslatorService;
    private SpeechToText speechToTextService;
    private SystemTranslationModel systemTran;
    private RecognizeOptions recognizeOptions;
    private Contract.View vml;
    @Inject Context context;
    private String delegateUser;
    private String sourceHolder;
    private String targetHolder;
    private String tempOrginal;
    private String tempTranslation;
    private List<Message> chatList;


    @Inject
    Presenter(TextToSpeech textToSpeechService, LanguageTranslator languageTranslatorService,
              SpeechToText speechToTextService, SystemTranslationModel systemTran) {

        this.textToSpeechService = textToSpeechService;
        this.languageTranslatorService = languageTranslatorService;
        this.speechToTextService = speechToTextService;
        this.systemTran = systemTran;
        chatList= new ArrayList<>();
    }


    private RecognizeOptions getRecognizeOptions() {
        return new RecognizeOptions.Builder()
                .contentType(ContentType.OPUS.toString())
                .model(getBroadBand(systemTran.getSource()))
                .interimResults(true)
                .inactivityTimeout(4000)
                .build();
    }

    private Single<InputStream> voiceSingle(String str) {
        return Single.create(emitter ->
                textToSpeechService.synthesize(str, voices(systemTran.getChosenVoice()))
                        .enqueue(new ServiceCallback<InputStream>() {
                            @Override
                            public void onResponse(InputStream response) {
                                emitter.onSuccess(response);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                emitter.onError(e);
                            }
                        }));

    }

    private Single<String> textTranslationSingle(String str) {
        TranslateOptions translateOptions = new TranslateOptions.Builder()
                .addText(str)
                .source(systemTran.getSource())
                .target(systemTran.getTarget())
                .build();

        return Single.create(emitter -> languageTranslatorService.translate(translateOptions)
                .enqueue(new ServiceCallback<TranslationResult>() {
                    @Override
                    public void onResponse(TranslationResult response) {
                        emitter.onSuccess(response.getTranslations().get(0).getTranslation());
                        vml.showText(response.getTranslations().get(0).getTranslation());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        emitter.onError(e);
                    }
                }));
    }

    @NonNull
    private Observable<SpeechResults> speechToVoiceObservable(MicrophoneInputStream capture) {
        recognizeOptions = getRecognizeOptions();
        return Observable.create(emitter -> {
            BaseRecognizeCallback callback = new BaseRecognizeCallback() {
                @Override
                public void onTranscription(SpeechResults speechResults) {
                    emitter.onNext(speechResults);
                }

                @Override
                public void onDisconnected() {
                    Log.e("onDisconnected", "Disconnected");
                    emitter.onComplete();
                }

                @Override
                public void onError(Exception e) {
                    emitter.onError(e);
                }
            };
            speechToTextService.recognizeUsingWebSocket(capture, getRecognizeOptions(), callback);
        });
    }


    public void start(MicrophoneInputStream capture) {
        speechToVoiceObservable(capture)
                .subscribeOn(Schedulers.io())
                .map(speechResults -> speechResults.getResults().get(0).getAlternatives().get(0).getTranscript())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(string -> {
                            vml.showText(string);
                            tempOrginal = string;
                        }
                )
                .doOnError(throwable -> Log.e("Speech 2 txt Error", throwable.getMessage()))
                .takeLast(1)
                .map(this::textTranslationSingle)
                .doOnNext(stringSingle -> Log.e("Translation :", stringSingle.toString()))
                .doOnError(throwable -> Log.e("Translation Error", throwable.getMessage()))
                .flatMap(Single::toObservable)
                .doOnNext(translatedString -> tempTranslation=translatedString)
                .map(this::voiceSingle)
                .flatMap(Single::toObservable)
                .subscribe(
                        (InputStream inputStream) -> vml.playStream(inputStream),
                        throwable -> Log.e("Error", throwable.getMessage()));
    }

    private void translatingTextOnly(String s) {
        textTranslationSingle(s)
                .flatMap(string -> voiceSingle(string))
                .subscribe(
                        inputStream -> vml.playStream(inputStream),
                        throwable -> Log.e("Error", throwable.getMessage()));
    }

    private Voice voices(String voiceChoice) {
        HashMap<String, Voice> voiceHashMap = new HashMap<>();
        voiceHashMap.put("Birgit-German", Voice.DE_BIRGIT);
        voiceHashMap.put("Dieter-German", Voice.DE_DIETER);
        voiceHashMap.put("Allison-English", Voice.EN_ALLISON);
        voiceHashMap.put("Lisa-English", Voice.EN_LISA);
        voiceHashMap.put("Michael-English", Voice.EN_MICHAEL);
        voiceHashMap.put("Enrique-Spain", Voice.ES_ENRIQUE);
        voiceHashMap.put("Laura-Spain", Voice.ES_LAURA);
        voiceHashMap.put("Sofia-Spanish", Voice.ES_LAURA);
        voiceHashMap.put("Renee-French", Voice.FR_RENEE);
        voiceHashMap.put("Kate-UK", Voice.GB_KATE);
        voiceHashMap.put("Francesca-Italian", Voice.IT_FRANCESCA);
        voiceHashMap.put("Emi-Japan", Voice.JA_EMI);
        voiceHashMap.put("Sofia-Spanish(LA)", Voice.LA_SOFIA);
        voiceHashMap.put("Isabela-Spanish(LA)", Voice.PT_ISABELA);
        return voiceHashMap.get(voiceChoice);
    }

    private String getBroadBand(String source) {
        HashMap<String, String> broadBandHashMap = new HashMap<>();
        broadBandHashMap.put("ar", "ar-AR_BroadbandModel");
        broadBandHashMap.put("en", "en-GB_BroadbandModel");
        broadBandHashMap.put("en", "en-US_BroadbandModel");
        broadBandHashMap.put("es", "es-ES_BroadbandModel");
        broadBandHashMap.put("fr", "fr-FR_BroadbandModel");
        broadBandHashMap.put("ja", "ja-JP_BroadbandModel");
        broadBandHashMap.put("ko", "ko-KR_BroadbandModel");
        broadBandHashMap.put("pt", "pt-BR_BroadbandModel");
        broadBandHashMap.put("zh", "zh-CN_BroadbandModel");
        return broadBandHashMap.get(source);
    }

    public void checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        vml.groupToggle(activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    public void primary() {
        delegateUser = "1";
        systemTran.setTarget(targetHolder);
        systemTran.setSource(sourceHolder);
    }

    public void secondary() {
        delegateUser = "0";
        systemTran.setTarget(sourceHolder);
        systemTran.setSource(targetHolder);
    }

    @Override
    public void setView(Contract.View view) {
        vml = view;
    }

    @Override
    public void start() {

    }

    @Override
    public void translateString(String str) {
        translatingTextOnly(str);
    }


    @Override
    public void startRecording(MicrophoneInputStream capture) {
        start(capture);
    }

    @Override
    public void getModels() {

    }

    @Override
    public void checkPermission(RxPermissions rxPermissions) {
        rxPermissions.request(Manifest.permission.RECORD_AUDIO)
                .subscribe(granted -> {
                    if (granted) {
                        vml.accessGranted();
                    } else {
                        vml.showToast("Needs Permission to Record");
                    }
                }, throwable -> Log.e("Error Permissions", throwable.toString()), this::unsubscribe);
    }

    @Override
    public void setSourceHolder(String s) {
        sourceHolder = s;
    }

    @Override
    public void setTargetHolder(String s) {
        targetHolder = s;
    }

    @Override
    public void addCurrentToChat() {
        Message tempPojo= new Message(delegateUser,tempOrginal,tempTranslation);
        chatList.add(tempPojo);
        vml.updateChatList(chatList);
    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unsubscribe() {

    }
}