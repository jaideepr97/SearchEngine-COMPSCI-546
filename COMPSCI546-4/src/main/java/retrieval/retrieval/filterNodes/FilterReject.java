package retrieval.retrieval.filterNodes;

import retrieval.retrieval.FilterOperator;
import retrieval.retrieval.ProximityNode;
import retrieval.retrieval.QueryNode;

public class FilterReject extends FilterOperator {
    public FilterReject(ProximityNode proximityNode, QueryNode queryNode) {
        super(proximityNode, queryNode);
    }

    @Override
    public Integer nextCandidate() {
        return query.nextCandidate();
    }

    @Override
    public Double score(Integer docId) {
        if(!docId.equals(filter.nextCandidate())) {
            return query.score(docId);
        } else {
            return null;
        }
    }
}
