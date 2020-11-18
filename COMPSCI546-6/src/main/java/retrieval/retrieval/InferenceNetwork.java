package retrieval.retrieval;

import java.util.*;

public class InferenceNetwork {

    public List<Map.Entry<Integer, Double>> runQuery(QueryNode queryNode, int queueSize) {
        PriorityQueue<Map.Entry<Integer, Double>> pQueue =
                new PriorityQueue<>(Map.Entry.<Integer,Double>comparingByValue());

        while(queryNode.hasMore()) {
            Integer nextDocument = queryNode.nextCandidate();
            queryNode.skipTo(nextDocument);
            Double documentScore = queryNode.score(nextDocument);
            if(documentScore != null) {
                pQueue.add(new AbstractMap.SimpleEntry<Integer, Double>(nextDocument, documentScore));
                if (pQueue.size() > queueSize) {
                    pQueue.poll();
                }
            }
            queryNode.skipTo(nextDocument + 1);
        }

        List<Map.Entry<Integer, Double>> rankedList = new ArrayList<>();
        while(!pQueue.isEmpty()) {
            rankedList.add(pQueue.poll());
        }
        rankedList.sort(Map.Entry.<Integer, Double>comparingByValue(Comparator.reverseOrder()));
        return rankedList;
    }
}

