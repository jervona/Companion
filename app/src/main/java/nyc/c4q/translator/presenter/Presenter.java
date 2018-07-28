package nyc.c4q.translator.presenter;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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

import java.io.InputStream;
import java.util.HashMap;

import javax.inject.Inject;


import io.reactivex.Observable;
import io.reactivex.Single;
import nyc.c4q.translator.contract.Contract;
import nyc.c4q.translator.singleton.SystemTranslationModel;

/**
 * Created by jervon.arnoldd on 6/2/18.
 */

public class Presenter {
    private TextToSpeech textToSpeechService;
    private LanguageTranslator languageTranslatorService;
    private SpeechToText speechToTextService;
    private SystemTranslationModel systemTran;
    private RecognizeOptions recognizeOptions;
    private Context context;

    @Inject
    Presenter(TextToSpeech textToSpeechService, LanguageTranslator languageTranslatorService, SpeechToText speechToTextService,SystemTranslationModel systemTran,Context context) {
        this.textToSpeechService = textToSpeechService;
        this.languageTranslatorService = languageTranslatorService;
        this.speechToTextService = speechToTextService;
        this.systemTran=systemTran;
        this.context=context;
    }

    public boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private RecognizeOptions getRecognizeOptions() {
        return new RecognizeOptions.Builder()
                .contentType(ContentType.OPUS.toString())
                .model(getBroadBand(systemTran.getSource()))
                .interimResults(true)
                .inactivityTimeout(4000)
                .build();
    }

    public Single<InputStream> voiceSingle(String str) {
        return Single.create(emitter -> {
            textToSpeechService.synthesize(str, voices(systemTran.getChosenVoice())).enqueue(new ServiceCallback<InputStream>() {
                @Override
                public void onResponse(InputStream response) {
                    emitter.onSuccess(response);
                }
                @Override
                public void onFailure(Exception e) {
                   emitter.onError(e);
                }
            });
        });

    }

    public Single<String> textTranslationSingle(String str) {
        TranslateOptions translateOptions = new TranslateOptions.Builder()
                .addText(str)
                .source(systemTran.getSource())
                .target(systemTran.getTarget())
                .build();

        return Single.create(emitter -> {
            languageTranslatorService.translate(translateOptions).enqueue(new ServiceCallback<TranslationResult>() {
                @Override
                public void onResponse(TranslationResult response) {
                    emitter.onSuccess(response.getTranslations().get(0).getTranslation());
                }

                @Override
                public void onFailure(Exception e) {
                    emitter.onError(e);
                }
            });
        });
    }

    @NonNull
    public Observable<SpeechResults> speechToVoiceObservable(MicrophoneInputStream capture) {
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
}