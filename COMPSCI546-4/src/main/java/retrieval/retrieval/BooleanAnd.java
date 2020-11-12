package retrieval.retrieval;

import index.index.Index;
import retrieval.retrieval.models.RetrievalModel;
import retrieval.retrieval.proximityNodes.windowNodes.UnorderedWindow;

import java.util.List;

public class BooleanAnd extends UnorderedWindow {
    public BooleanAnd(Index index, RetrievalModel model, List<ProximityNode> children, int windowSize) {
        super(index, model, children, 0);
    }
}
