package retrieval.retrieval.beliefNodes;

import retrieval.retrieval.BeliefNode;
import retrieval.retrieval.QueryNode;

import java.util.List;

public class WeightedSum extends BeliefNode {
    List<Double> weights;
    public WeightedSum(List<QueryNode> children, List<Double> weights) {
        super(children);
        this.weights = weights;
    }
    @Override
    public Double score(Integer docId) {
        Double score = 0.0;
        Double weightSum = 0.0;
        for(int i=0; i<weights.size(); i++)
            weightSum += weights.get(i);

        for(int i=0; i<children.size(); i++) {
               score += weights.get(i) * Math.exp(children.get(i).score(docId));
               score /= weightSum;
        }
        return Math.log(score);
    }
}
