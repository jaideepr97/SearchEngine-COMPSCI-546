package index;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DocumentVector implements Serializable {

    public String sceneId;
    public int docId;
    public Map<String, Double> docVector;


    public DocumentVector (String sceneId, int docId) {
        this.docVector = new HashMap<>();
        this.sceneId = sceneId;
        this.docId = docId;
    }

    @Override
    public String toString() {
        return sceneId + " " + docVector;
    }
}
