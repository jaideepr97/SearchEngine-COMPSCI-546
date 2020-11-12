package retrieval.retrieval.proximityNodes;

import index.index.Index;
import index.index.PostingList;
import retrieval.retrieval.ProximityNode;
import retrieval.retrieval.models.RetrievalModel;

public class Term extends ProximityNode {

    public String term;

    public Term(String term, Index index, RetrievalModel model) {
        super(index, model);
        this.term = term;
        generatePostings();
    }

    @Override
    public void generatePostings() {
        postingList = index.getPostings(this.term);
        collectionTermFrequency = index.getTermFreq(this.term);
    }

}
