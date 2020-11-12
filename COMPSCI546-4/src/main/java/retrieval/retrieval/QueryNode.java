package retrieval.retrieval;

public interface QueryNode {

    public Integer nextCandidate();

    public Double score(Integer docId);

    public boolean hasMore();

    public void skipTo(int docId);
}
