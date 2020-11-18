package retrieval.retrieval;
import index.index.InvertedIndex;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PriorNode implements QueryNode {

    public enum priorType {Uniform, Random};
    public Map<priorType, String> priorTypeFileName;
    public priorType pType;
    InvertedIndex index;

    public PriorNode(priorType pType) {
        this.priorTypeFileName = new HashMap<>();
        priorTypeFileName.put(priorType.Uniform, "uniform.prior");
        priorTypeFileName.put(priorType.Random, "random.prior");
        this.pType = pType;
        index = new InvertedIndex();
    }

    @Override
    public Integer nextCandidate() {
        return null;
    }

    @Override
    public Double score(Integer docId) {
        Double score = null;
        try {
            score = index.retrievePrior(priorTypeFileName.get(this.pType), docId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Math.log(score);
    }

    @Override
    public boolean hasMore() {
        return false;
    }

    @Override
    public void skipTo(int docId) {

    }
}
