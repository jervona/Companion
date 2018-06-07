package nyc.c4q.translator.model;

/**
 * Created by jervon.arnoldd on 5/16/18.
 */

public class Message {
    private String id;
    private String message;
    private String translatedMessage;


    public Message() {
    }

    public Message(String id, String message,String translatedMessage) {
        this.id = id;
        this.message = message;
        this.translatedMessage = translatedMessage;
    }

    public String getTranslatedMessage() {
        return translatedMessage;
    }

    public void setTranslatedMessage(String translatedMessage) {
        this.translatedMessage = translatedMessage;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
