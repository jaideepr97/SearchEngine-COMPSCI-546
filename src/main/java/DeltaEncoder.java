import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class DeltaEncoder {

    public Map<String, Integer[]> encode(Map<String, Integer[]> invertedIndex) {
        for (Map.Entry<String, Integer[]> entry : invertedIndex.entrySet()) {
            int previousDocIdCopy = -1, currentDocIdCopy = -1;
            Integer[] postingList = entry.getValue();
            for (int i=0; i<postingList.length; i++) {
                int docId = postingList[i];
                int positionArraySize = postingList[i+1];

                if(i > 0) {
                    currentDocIdCopy = docId;
                    postingList[i] = docId - previousDocIdCopy;
                    previousDocIdCopy = currentDocIdCopy;
                } else {
                    previousDocIdCopy = docId;
                }
                int startPosition = i+2;
                int endPosition = startPosition + positionArraySize -1;
                while(endPosition > startPosition) {
                    postingList[endPosition] -= postingList[endPosition - 1];
                    endPosition--;
                }

                i = startPosition + positionArraySize -1;

            }
            invertedIndex.replace(entry.getKey(), postingList);
        }
        return invertedIndex;
    }

//    public Map<String, Integer[]> decode(Map<String, Integer[]> encodedInvertedIndex) {
//        HashMap<String, Integer[]> decodedQueryIndex = new HashMap<>();
//        for(Map.Entry<String, Integer[]> entry: encodedInvertedIndex.entrySet()) {
////            Integer[] decodedPostingList = new Integer[entry.getValue().length];
//            Integer[] encodedPostingList = entry.getValue();
//            int previousDocId = -1;
//            for (int i=0; i<encodedPostingList.length; i++) {
//                int docId = encodedPostingList[i];
//                int positionArraySize = encodedPostingList[i+1];
//
//                if(previousDocId != -1) {
//                    encodedPostingList[i] = docId + previousDocId;
//                } else {
//                    encodedPostingList[i] = docId;
//                }
//                previousDocId = encodedPostingList[i];
//
//                int startPosition = i+3, endPosition = startPosition + positionArraySize -2;
//                while(startPosition <= endPosition) {
//                    encodedPostingList[startPosition] += encodedPostingList[startPosition-1];
//                    startPosition++;
//                }
//
//                i = endPosition;
//            }
//            decodedQueryIndex.put(entry.getKey(), encodedPostingList);
//        }
//        return decodedQueryIndex;
//    }

    public int[] decode(int[] deltaEncodedPostingList) {
        int previousDocId = -1;
        for (int i=0; i<deltaEncodedPostingList.length; i++) {
            int docId = deltaEncodedPostingList[i];
            int positionArraySize = deltaEncodedPostingList[i+1];

            if(previousDocId != -1) {
                deltaEncodedPostingList[i] = docId + previousDocId;
            } else {
                deltaEncodedPostingList[i] = docId;
            }
            previousDocId = deltaEncodedPostingList[i];

            int startPosition = i+3, endPosition = startPosition + positionArraySize -2;
            while(startPosition <= endPosition) {
                deltaEncodedPostingList[startPosition] += deltaEncodedPostingList[startPosition-1];
                startPosition++;
            }

            i = endPosition;
        }
        return deltaEncodedPostingList;
    }

    public static void main(String[] args) {
        DeltaEncoder de = new DeltaEncoder();
        int[] test = new int[]{1,2,1,6,1,3,6,11,180,1,1,1};
        int[] result = de.decode(test);
        System.out.println("done");
    }
}
