package retrieval.retrieval.proximityNodes;

import index.index.Index;
import index.index.Posting;
import index.index.PostingList;
import retrieval.retrieval.ProximityNode;
import retrieval.retrieval.models.RetrievalModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public abstract class Window extends ProximityNode {

    public List<ProximityNode> children;
    public int windowSize;

    public Window(Index ind, RetrievalModel mod, List<ProximityNode> children, int windowSize) {
        super(ind, mod);
        this.children = children;
        this.windowSize = windowSize;
    }

    abstract public Posting calculateWindows(List<Posting> matchingPostings);

    public boolean allHaveMore(){
        for(int i=0; i<children.size(); i++) {
            if(!children.get(i).hasMore()) {
                return false;
            }
        }
        return true;
    }

    public Integer candidate(){
        List<Integer> nextCandidatesOfChildren = new ArrayList<>();
            for(ProximityNode child: children) {
                nextCandidatesOfChildren.add(child.nextCandidate());
            }
           return Collections.max(nextCandidatesOfChildren);
    }

    @Override
    public void generatePostings(){
        postingList = new PostingList();
        postingList.startIteration();
        ArrayList<Posting> matchingPostings = new ArrayList<>();
        while(allHaveMore()){
            Integer next = candidate();
            children.forEach(c -> c.skipTo(next));
            if(children.stream().allMatch(c -> next.equals(c.nextCandidate()))){
                for(ProximityNode child : children){
                    matchingPostings.add(child.postingList.getCurrentPosting());
                }
                Posting p = calculateWindows(matchingPostings);
                if(p != null){
                    postingList.add(p);
                    collectionTermFrequency += p.getTermFreq();
                }
            }
            matchingPostings.clear();
            children.forEach(c -> c.skipTo(next +1));
        }
        postingList.startIteration();
        currentDocIterator = 0;
    }
}