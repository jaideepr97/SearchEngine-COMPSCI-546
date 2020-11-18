package retrieval.retrieval.beliefNodes;

import retrieval.retrieval.BeliefNode;
import retrieval.retrieval.QueryNode;

import java.util.List;

public class Sum extends BeliefNode {
    public Sum(List<QueryNode> children) {
        super(children);
    }
    @Override
    public Double score(Integer docId) {
        Double score = 0.0;
        for (QueryNode child : children) {
            score += Math.exp(child.score(docId));
        }
        return Math.log(score/children.size());
    }
}
