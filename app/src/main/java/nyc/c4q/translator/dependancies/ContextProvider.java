package nyc.c4q.translator.dependancies;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

/**
 * Created by jervon.arnoldd on 6/24/18.
 */
@Module
public class ContextProvider {
    private Context context;

    ContextProvider(Context context) {
        this.context = context;
    }

    @Provides
    Context provideContext() {
        return context;
    }
}
