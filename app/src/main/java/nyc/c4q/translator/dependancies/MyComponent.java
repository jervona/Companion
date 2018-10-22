package nyc.c4q.translator.dependancies;

import javax.inject.Singleton;

import dagger.Component;
import nyc.c4q.translator.MainActivity;
import nyc.c4q.translator.dependancies.ContextProvider;
import nyc.c4q.translator.dependancies.Credentials;
import nyc.c4q.translator.singleton.SystemTranslationModel;

/**
 * Created by jervon.arnoldd on 6/24/18.
 */

@Singleton
@Component(modules = {Credentials.class,ContextProvider.class, RenameSoon.class,})
public interface MyComponent {
   void inject(MainActivity activity);
}
