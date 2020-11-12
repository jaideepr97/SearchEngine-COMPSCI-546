package retrieval.retrieval.filterNodes;

import retrieval.retrieval.FilterOperator;
import retrieval.retrieval.ProximityNode;
import retrieval.retrieval.QueryNode;

public class FilterRequire extends FilterOperator {
    public FilterRequire(ProximityNode proximityNode, QueryNode queryNode) {
        super(proximityNode, queryNode);
    }

    @Override
    public Integer nextCandidate() {
        return Math.max(filter.nextCandidate(), query.nextCandidate());
    }

    @Override
    public Double score(Integer docId) {
        if(docId.equals(filter.nextCandidate())) {
            return query.score(docId);
        } else {
            return null;
        }
    }
}
