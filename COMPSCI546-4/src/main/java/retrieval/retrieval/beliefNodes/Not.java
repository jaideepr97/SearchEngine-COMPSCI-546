package retrieval.retrieval.beliefNodes;

import retrieval.retrieval.BeliefNode;
import retrieval.retrieval.QueryNode;

import java.util.List;

public class Not extends BeliefNode {

    public Not (List<QueryNode> children) {
        super(children);
    }

    @Override
    public Double score(Integer docId) {
        return Math.log(1-Math.exp(children.get(0).score(docId)));
    }
}
