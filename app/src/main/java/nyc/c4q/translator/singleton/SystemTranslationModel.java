package nyc.c4q.translator.singleton;


/**
 * Created by jervon.arnoldd on 6/2/18.
 */


public class SystemTranslationModel {

    private String target;
    private String source;
    private String chosenVoice;
    private  boolean getVoice;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getChosenVoice() {
        return chosenVoice;
    }

    public void setChosenVoice(String chosenVoice) {
        this.chosenVoice = chosenVoice;
    }

    public boolean isGetVoice() {
        return getVoice;
    }

    public void setGetVoice(boolean getVoice) {
        this.getVoice = getVoice;
    }
}
