package retrieval.retrieval;
import index.index.InvertedIndex;
import java.io.IOException;

public class PriorNode implements QueryNode {

    InvertedIndex index;
    public String priorFileName;

    public PriorNode(String fileName) {
        priorFileName = fileName;
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
            score = index.retrievePrior(priorFileName, docId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return score;
    }

    @Override
    public boolean hasMore() {
        return false;
    }

    @Override
    public void skipTo(int docId) {

    }
}
