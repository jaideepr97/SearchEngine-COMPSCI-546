package retrieval.retrieval.beliefNodes;

import retrieval.retrieval.BeliefNode;
import retrieval.retrieval.QueryNode;

import java.util.List;

public class Or extends BeliefNode {

    public Or (List<QueryNode> children) {
        super(children);
    }

    @Override
    public Double score(Integer docId) {
        Double score = children.stream().mapToDouble(
                child -> Math.log(1 - Math.exp(child.score(docId)))).sum();
        return Math.log(1 - Math.exp(score));
    }
}