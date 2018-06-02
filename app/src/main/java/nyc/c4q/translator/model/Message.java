package nyc.c4q.translator.model;

/**
 * Created by jervon.arnoldd on 5/16/18.
 */

public class Message {
    private String id;
    private String message;


    public Message() {
    }

    public Message(String id, String message) {
        this.id = id;
        this.message = message;


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
