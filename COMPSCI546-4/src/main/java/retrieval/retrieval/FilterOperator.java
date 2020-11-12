package retrieval.retrieval;

public abstract class FilterOperator implements QueryNode {

    public QueryNode query = null;
    public ProximityNode filter;

    public FilterOperator(ProximityNode proximityNode, QueryNode queryNode) {
        this.filter = proximityNode;
        this.query = queryNode;
    }
    @Override
    public Integer nextCandidate() {
        return null;
    }

    @Override
    public Double score(Integer docId) {
        return null;
    }

    @Override
    public boolean hasMore() {
        return query.hasMore();
    }

    @Override
    public void skipTo(int docId) {
        filter.skipTo(docId);
        query.skipTo(docId);
    }
}
