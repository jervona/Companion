package nyc.c4q.translator.dependancies;

import android.app.Application;


/**
 * Created by jervon.arnoldd on 6/24/18.
 */

public class BaseApp extends Application {
   MyComponent myComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        myComponent = DaggerMyComponent.builder()
                .contextProvider(new ContextProvider(getApplicationContext())).build();
    }

    public MyComponent getMyComponent() {
        return myComponent;
    }
}
