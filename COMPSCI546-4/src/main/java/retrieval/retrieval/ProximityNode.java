package retrieval.retrieval;

import index.index.Index;
import index.index.PostingList;
import retrieval.retrieval.models.RetrievalModel;

public abstract class ProximityNode implements QueryNode{
    public int collectionTermFrequency = 0;
    public int currentDocIterator = 0;
    public PostingList postingList = null;
    public Index index;
    public RetrievalModel model;
    public ProximityNode(Index index, RetrievalModel model){
        this.index = index;
        this.model = model;

    }

    public abstract void generatePostings();

    public boolean hasMore() {
        return currentDocIterator < postingList.documentCount();
    }

    public Integer nextCandidate() {
        if (hasMore()) {
            return postingList.get(currentDocIterator).getDocId();
        }
        return null;
    }

    public void skipTo(int docId) {
        while (hasMore() && postingList.get(currentDocIterator).getDocId() < docId) {
            currentDocIterator++;
            postingList.skipTo(docId);
        }

    }
    public Double score(Integer docId) {
        int tf = 0;
        if (hasMore() && postingList.get(currentDocIterator).getDocId().equals(docId)) {
            tf = postingList.get(currentDocIterator).getTermFreq();
        }

        return model.scoreOccurrence(tf, collectionTermFrequency, index.getDocLength(docId));
    }

    public PostingList getPostingList() {
        return postingList;
    }
}
