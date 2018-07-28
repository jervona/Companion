package nyc.c4q.translator.dependancies;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import nyc.c4q.translator.singleton.SystemTranslationModel;

/**
 * Created by jervon.arnoldd on 7/27/18.
 */
@Module
public class RenameSoon {

    @Provides
    @Singleton
    SystemTranslationModel provideSystemTran() {
        return new SystemTranslationModel();
    }
}
