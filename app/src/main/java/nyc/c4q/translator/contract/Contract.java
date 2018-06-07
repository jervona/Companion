package nyc.c4q.translator.contract;

import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;

import java.io.InputStream;
import java.util.List;

import nyc.c4q.translator.model.Message;

/**
 * Created by jervon.arnoldd on 6/1/18.
 */

public interface Contract {
    interface View {
        void playStream(InputStream streamPlayer);
        void showErrorMessage();
        void showText(String text);
        void sendMessage(String message);
        void disconnected();
    }

    interface Presenter {
        void start();
        void translate(String str);
        void recordAudio(MicrophoneInputStream capture);
    }
}
