package retrieval.retrieval.models;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import index.index.Index;
import index.index.Posting;
import index.index.PostingList;

public class VectorSpace {
    private int numDocs;
    private Index index;
    public VectorSpace(Index ind) {
        index = ind;
        this.numDocs = index.getDocCount();
    }


    private double scoreOccurrence(int tf, int dtf, int qtf) {

        double score = 0;
        // don't take log of 0...
        double logTF = tf > 0 ? 1 + Math.log(tf) : 0;
        double logIDF = Math.log((double)numDocs/(double)dtf);
        double logQtf = 1 + Math.log(qtf);
        // wt q idf is the same
        score = logTF * logIDF * logQtf * logIDF;
        return score;
    }
    /**
     * @return a list of the top k documents in descending order with respect to scores.
     * key = sceneId, value = score
     * Does document at a time retrieval using the Vector space model.
     *
     */
    public List<Map.Entry<Integer, Double>> retrieveQuery(String query, int k) {
        PriorityQueue<Map.Entry<Integer, Double>> result =
                new PriorityQueue<>(Map.Entry.<Integer, Double>comparingByValue());
        String [] queryTerms = query.split("\\s+");
        Map <String, Integer> qTerms = new HashMap<String, Integer> ();
        Set<String> words = new HashSet<String>();
        // convert terms to set with frequency
        for (String qterm: queryTerms) {
            qTerms.put(qterm, qTerms.getOrDefault(qterm, 0) + 1);
            words.add(qterm);
        }
        List<String> terms = new ArrayList<String>(words);
        PostingList[] lists = new PostingList[terms.size()];
        for (int i = 0; i < terms.size(); i++) {
            lists[i] = index.getPostings(terms.get(i));
        }
        for (int doc = 1; doc <= index.getDocCount(); doc++) {
            Double curScore = 0.0;
            boolean scored = false;
            for (int i = 0; i < lists.length; i++) {
                PostingList p = lists[i];
                String term = terms.get(i);
                p.skipTo(doc);
                Posting post = p.getCurrentPosting();
                int tf = 0;
                if (post != null && post.getDocId() == doc) {
                    tf = post.getTermFreq();
                    scored = true;
                    int dtf = index.getDocFreq(term);
                    int qtf = qTerms.get(term);
                    curScore += scoreOccurrence(tf, dtf, qtf);
                }
            }
            if (scored) {
                // normalize by document length
                curScore = curScore/index.getDocLength(doc);
                result.add(new AbstractMap.SimpleEntry<Integer, Double>(doc, curScore));
                // trim the queue if necessary
                if (result.size() > k) {
                    result.poll();
                }
            }
        }
        // reverse the queue
        ArrayList<Map.Entry<Integer, Double>> scores = new ArrayList<Map.Entry<Integer, Double>>();
        scores.addAll(result);
        scores.sort(Map.Entry.<Integer, Double>comparingByValue(Comparator.reverseOrder()));
        return scores;
    }
}

