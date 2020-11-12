package retrieval.retrieval.proximityNodes.windowNodes;

import index.index.Index;
import index.index.Posting;
import retrieval.retrieval.ProximityNode;
import retrieval.retrieval.models.RetrievalModel;
import retrieval.retrieval.proximityNodes.Window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrderedWindow extends Window {

    public OrderedWindow(Index index, RetrievalModel model, List<ProximityNode> children, int windowSize) {
        super(index, model, children, windowSize);
        generatePostings();
    }

    @Override
    public Posting calculateWindows(List<Posting> commonPostings) {
        boolean foundWindowMatch = false;
        Posting computedPosting = null;
        if (commonPostings.size() == 1) {
            return commonPostings.get(0);
        }
        List<Integer> computedWindowPositions = new ArrayList<>();
        List<Integer[]> positionArrays = new ArrayList<>();

        for(Posting p: commonPostings) {
            positionArrays.add(p.getPositionsArray());
        }

        int[] positionArrayPointers = new int[positionArrays.size()];
        Arrays.fill(positionArrayPointers, 0);

        int firstTermPosition = 0;
        int previousTermPosition = -1;

        for(int i=0; i<positionArrays.get(0).length; i++) {
            firstTermPosition = positionArrays.get(0)[i];
            previousTermPosition = firstTermPosition;

            for(int j=1; j<positionArrays.size(); j++) {
                foundWindowMatch = false;
                Integer[] currPositionArray = positionArrays.get(j);

                // keeping track of pointers in individual position arrays to avoid looping through the whole thing each time
                for(int k=positionArrayPointers[j]; k<currPositionArray.length; k++) {
                    if(previousTermPosition < currPositionArray[k] && (currPositionArray[k] - previousTermPosition) <= windowSize) {
                        foundWindowMatch = true;
                        positionArrayPointers[j] = k;
                        previousTermPosition = currPositionArray[k];
                        break;
                    }
                }
                if(!foundWindowMatch) {
                    break;
                }
            }
            if(foundWindowMatch) {
                computedWindowPositions.add(firstTermPosition);
                // skipping forward to a position after the latest found position to avoid double dipping
               for(int m=1; m<positionArrayPointers.length; m++) {
                   positionArrayPointers[m] += 1;
               }

            }
        }

        if(computedWindowPositions.size() > 0) {
            computedPosting = new Posting();
            computedPosting.setDocId(commonPostings.get(0).getDocId());
            computedPosting.setPositions(computedWindowPositions);
        }

        return computedPosting;
    }

}
