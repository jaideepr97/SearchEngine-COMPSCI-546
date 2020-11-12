package retrieval.retrieval.beliefNodes;

import retrieval.retrieval.BeliefNode;
import retrieval.retrieval.QueryNode;

import java.util.List;

public class Max extends BeliefNode {

    public Max(List<QueryNode> children) {
        super(children);
    }
    @Override
    public Double score(Integer docId) {
        Double score = (-1.0) * Double.MAX_VALUE;
        for (QueryNode child : children) {
            score = Math.max(score, child.score(docId));
        }
        return score;
    }
}
