package nyc.c4q.translator.presenter;

;
import android.util.Log;

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

import nyc.c4q.translator.contract.Contract;
import nyc.c4q.translator.model.SystemTranslationModel;


/**
 * Created by jervon.arnoldd on 6/2/18.
 */

public class Presenter implements Contract.Presenter {
    private Contract.View viewImpl;
    private TextToSpeech textToSpeechService;
    private LanguageTranslator languageTranslatorService;
    private SpeechToText speechToTextService;
    private SystemTranslationModel systemTran = SystemTranslationModel.getInstance();

    public Presenter(Contract.View viewImpl, TextToSpeech textToSpeechService,
                     LanguageTranslator languageTranslatorService, SpeechToText speechToTextService) {
        this.viewImpl = viewImpl;
        this.textToSpeechService = textToSpeechService;
        this.languageTranslatorService = languageTranslatorService;
        this.speechToTextService = speechToTextService;
    }

    private RecognizeOptions getRecognizeOptions() {
        Log.e("Presenter",getBroadBand(systemTran.getSource()));
        return new RecognizeOptions.Builder()
                .contentType(ContentType.OPUS.toString())
                .model(getBroadBand(systemTran.getSource()))
                .interimResults(true)
                .inactivityTimeout(4000)
                .build();
    }

    private void getVoice(String str) {
        textToSpeechService.synthesize(str, voices(systemTran.getChosenVoice())).enqueue(new ServiceCallback<InputStream>() {
            @Override
            public void onResponse(InputStream response) {
                viewImpl.playStream(response);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Error TTS", e.getMessage());
            }
        });
    }

    public void translateString(String str) {
        TranslateOptions translateOptions = new TranslateOptions.Builder()
                .addText(str)
                .source(systemTran.getSource())
                .target(systemTran.getTarget())
                .build();

        languageTranslatorService.translate(translateOptions).enqueue(new ServiceCallback<TranslationResult>() {
            @Override
            public void onResponse(TranslationResult response) {
                String tran = response.getTranslations().get(0).getTranslation();

               if (systemTran.isGetVoice()) {
                   getVoice(tran);
               }
                Log.e("THis is a Tran", tran);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Error Translate", e.getMessage());
            }
        });
    }

    private void recordMessage(MicrophoneInputStream capture) {
            final BaseRecognizeCallback callback = new BaseRecognizeCallback() {
                @Override
                public void onTranscription(SpeechResults speechResults) {
                    if (speechResults.getResults() != null && !speechResults.getResults().isEmpty()) {
                        String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
                        Log.e("Text", text);
                        viewImpl.showText(text);
                    }
                }
                @Override
                public void onDisconnected() {
                    Log.e("Disconnected","Disconnected");
                }
            };
            new Thread(() -> {
                try {
                    speechToTextService.recognizeUsingWebSocket(capture, getRecognizeOptions(), callback);
                } catch (Exception e) {
                    Log.e("Exception", e.getMessage());
                }
            }).start();
    }


    @Override
    public void start() {

    }

    @Override
    public void translate(String str) {
          translateString(str);
    }

    @Override
    public void recordAudio(MicrophoneInputStream capture) {
        recordMessage(capture);
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


    private String getBroadBand(String source){
        HashMap<String, String> broadBandHashMap = new HashMap<>();
        broadBandHashMap.put("ar","ar-AR_BroadbandModel");
        broadBandHashMap.put("en","en-GB_BroadbandModel");
        broadBandHashMap.put("en","en-US_BroadbandModel");
        broadBandHashMap.put("es","es-ES_BroadbandModel");
        broadBandHashMap.put("fr","fr-FR_BroadbandModel");
        broadBandHashMap.put("ja","ja-JP_BroadbandModel");
        broadBandHashMap.put("ko","ko-KR_BroadbandModel");
        broadBandHashMap.put("pt","pt-BR_BroadbandModel");
        broadBandHashMap.put("zh","zh-CN_BroadbandModel");
        return broadBandHashMap.get(source);
    }







}