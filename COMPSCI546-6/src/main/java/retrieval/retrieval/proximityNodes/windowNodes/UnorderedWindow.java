package retrieval.retrieval.proximityNodes.windowNodes;

import index.index.Index;
import index.index.Posting;
import retrieval.retrieval.ProximityNode;
import retrieval.retrieval.models.RetrievalModel;
import retrieval.retrieval.proximityNodes.Window;

import java.util.*;

public class UnorderedWindow extends Window {
    List<Integer> computedWindowPositions;
    List<Integer[]> positionArrays;
    int[] positionArrayPointers;

    public UnorderedWindow(Index index, RetrievalModel model, List<ProximityNode> children, int windowSize) {
        super(index, model, children, windowSize);
        generatePostings();
    }

    public Integer getWindowSize(Integer d){
        return windowSize == 0 ? index.getDocLength(d) : windowSize;
    }

    @Override
    public Posting calculateWindows(List<Posting> commonPostingsFromQuery) {
        Set<Posting> commonPostingsSet = new HashSet<>();
        commonPostingsSet.addAll(commonPostingsFromQuery);
        List<Posting> commonPostings = new ArrayList<>();
        commonPostings.addAll(commonPostingsSet);
        computedWindowPositions = new ArrayList<>();
        positionArrays = new ArrayList<>();
        Posting computedPosting = null;
        if (commonPostings.size() == 1) {
            return commonPostings.get(0);
        }

        for(Posting p: commonPostings) {
            positionArrays.add(p.getPositionsArray());
        }

        positionArrayPointers = new int[positionArrays.size()];
        Arrays.fill(positionArrayPointers, 0);

        while(!anyListFinished()) {
            int earliestPosition = Integer.MAX_VALUE;
            int earliestPositionArrayId = -1;
            int latestPosition = Integer.MIN_VALUE;
            int latestPositionArrayId = -1;

            for(int i=0; i<positionArrayPointers.length; i++) {
                if(positionArrays.get(i)[positionArrayPointers[i]] < earliestPosition) {
                    earliestPosition = positionArrays.get(i)[positionArrayPointers[i]];
                    earliestPositionArrayId = i;
                }
                if(positionArrays.get(i)[positionArrayPointers[i]] > latestPosition) {
                    latestPosition = positionArrays.get(i)[positionArrayPointers[i]];
                    latestPositionArrayId = i;
                }
            }

            if(latestPosition - earliestPosition < windowSize) {
                computedWindowPositions.add(earliestPosition);
                for(int i=0; i<positionArrayPointers.length; i++) {
                    positionArrayPointers[i] += 1;
                }
            }
            else {
                positionArrayPointers[earliestPositionArrayId] += 1;
            }

        }
        if(computedWindowPositions.size() > 0) {
            computedPosting = new Posting();
            computedPosting.setDocId(commonPostings.get(0).getDocId());
            computedPosting.setPositions(computedWindowPositions);
        }
        return computedPosting;
    }

    public boolean anyListFinished() {

        for(int i=0; i<positionArrayPointers.length; i++) {
            if(positionArrayPointers[i] >= positionArrays.get(i).length) {
                return true;
            }
        }
        return false;
    }
}
