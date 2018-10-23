package nyc.c4q.translator.dependancies;

import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import nyc.c4q.translator.singleton.SystemTranslationModel;

/**
 * Created by jervon.arnoldd on 7/27/18.
 */
@Module
class RenameSoon {
    @Provides
    @Singleton
    SystemTranslationModel provideSystemTran() {
        return new SystemTranslationModel();
    }
}
