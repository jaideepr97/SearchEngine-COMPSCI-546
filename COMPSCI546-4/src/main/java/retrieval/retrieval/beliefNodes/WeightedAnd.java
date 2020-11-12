package retrieval.retrieval.beliefNodes;

import retrieval.retrieval.BeliefNode;
import retrieval.retrieval.QueryNode;

import java.util.List;

public class WeightedAnd extends BeliefNode {
    List<Double> weights;
    public WeightedAnd(List<QueryNode> children, List<Double> weights) {
        super(children);
        this.weights = weights;
    }
    @Override
    public Double score(Integer docId) {
        Double score = 0.0;
        for (int i=0; i<children.size(); i++) {
            score += weights.get(i) * children.get(i).score(docId);
        }
        return score;
    }
}
