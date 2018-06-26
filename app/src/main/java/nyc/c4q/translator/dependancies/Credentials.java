package nyc.c4q.translator.dependancies;

import android.content.Context;

import com.ibm.watson.developer_cloud.language_translator.v2.LanguageTranslator;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import nyc.c4q.translator.R;

/**
 * Created by jervon.arnoldd on 6/24/18.
 */

@Module
public class Credentials {

    @Provides
    @Singleton
    TextToSpeech provideTextToSpeech(Context context) {
        TextToSpeech textToSpeechService = new TextToSpeech();
        String user = context.getResources().getString(R.string.text_to_speech_username);
        String password = context.getResources().getString(R.string.text_to_speech_password);
        textToSpeechService.setUsernameAndPassword(user, password);
        return textToSpeechService;
    }


    @Provides
    @Singleton
    LanguageTranslator providelanguageTranslatorService(Context context) {
        LanguageTranslator languageTranslatorService = new LanguageTranslator();
        String transUser = context.getResources().getString(R.string.language_translator_username);
        String transPassword = context.getResources().getString(R.string.language_translator_password);
        languageTranslatorService.setUsernameAndPassword(transUser, transPassword);
        return languageTranslatorService;
    }

    @Provides
    @Singleton
    SpeechToText provideSpeechToText(Context context) {
        SpeechToText speechToTextService = new SpeechToText();
        String speechToTextUser = context.getResources().getString(R.string.speech_to_text_username);
        String speechToTextPassword = context.getResources().getString(R.string.speech_to_text_password);
        speechToTextService.setUsernameAndPassword(speechToTextUser, speechToTextPassword);
        return speechToTextService;
    }

}
