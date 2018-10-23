package nyc.c4q.translator.contract;

import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.tbruyelle.rxpermissions2.RxPermissions;


import java.io.InputStream;
import java.util.List;

import nyc.c4q.translator.BasePresenter;
import nyc.c4q.translator.BaseView;
import nyc.c4q.translator.Pojo.Message;


/**
 * Created by jervon.arnoldd on 6/1/18.
 */

public interface Contract {
    interface View extends BaseView<Presenter> {
        void playStream(InputStream streamPlayer);
        void showErrorMessage();
        void showText(String text);
        void accessGranted();
        void groupToggle(boolean b);
        void showToast(String message);
        void updateChatList(List<Message> chatList);
        void toggleVisibility();
    }

    interface Presenter  extends BasePresenter {
        void setView(Contract.View v);
        void start();
        void translateString(String str);
        void startRecording(MicrophoneInputStream capture);
        void getModels();
        void checkPermission(RxPermissions rxPermissions);
        void setSourceHolder(String source);
        void setTargetHolder(String target);
        void addCurrentToChat();
        void backButtonClicked();
        void primaryUser();
        void secondaryUser();

        void checkInternetConnection();
    }
}
