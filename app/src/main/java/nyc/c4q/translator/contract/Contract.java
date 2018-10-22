package nyc.c4q.translator.contract;

import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;


import java.io.InputStream;


/**
 * Created by jervon.arnoldd on 6/1/18.
 */

public interface Contract {
    interface View {
        void playStream(InputStream streamPlayer);
        void showErrorMessage();
        void showText(String text);
    }

    interface Presenter {
        void setView(Contract.View v);
        void start();
        void translate(String str);
    }
}
