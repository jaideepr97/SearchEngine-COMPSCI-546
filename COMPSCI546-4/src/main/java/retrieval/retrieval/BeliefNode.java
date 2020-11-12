package retrieval.retrieval;

import java.util.List;

public abstract class BeliefNode implements QueryNode {
    public List<QueryNode> children;

    public BeliefNode(List<QueryNode> children) {
        this.children = children;
    }

    public Integer nextCandidate() {
        int min = Integer.MAX_VALUE;
        for(QueryNode child: children) {
            if(child.hasMore()) {
                min = Math.min(min, child.nextCandidate());
            }
        }
        return min != Integer.MAX_VALUE? min: null;
    }

    public boolean hasMore() {
        for(QueryNode child: children) {
            if(child.hasMore()) {
                return true;
            }
        }
        return false;
    }

    public void skipTo(int docId) {
        for (QueryNode child : children) {
            child.skipTo(docId);
        }
    }

}
