package retrieval.retrieval.beliefNodes;

import retrieval.retrieval.BeliefNode;
import retrieval.retrieval.QueryNode;

import java.util.List;

public class And extends BeliefNode {

    public And (List<QueryNode> children) {
        super(children);
    }

    @Override
    public Double score(Integer docId) {
        return children.stream().mapToDouble(child -> child.score(docId)).sum();
    }
}
