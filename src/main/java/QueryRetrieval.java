import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class QueryRetrieval {

    String destination = "filesWrittenToDisk/";
    public static void main(String[] args) throws IOException {
        QueryRetrieval qr = new QueryRetrieval();
        String filename = args[0];
        boolean compressionRequired = (args[1].equalsIgnoreCase("compressed"))? true : false;
        qr.retrieveQuery(filename, compressionRequired);
    }

    public void retrieveQuery(String queryTermsFilename, boolean compressionRequired) throws IOException {
        ReadFromDisk rfd = new ReadFromDisk();
        Map<Integer, Integer> docIdToDocLengths = rfd.readDocLengthsFromDisk();
        int docCount = docIdToDocLengths.entrySet().size();

        Map<String, TermLookupEntry> lookupTable = rfd.readLookupFromDisk(compressionRequired);
        BufferedReader reader = new BufferedReader(new FileReader(destination + queryTermsFilename));

        String query;
        Instant start, end;
        start = Instant.now();
        while((query = reader.readLine()) != null) {
            String[] queryTerms = query.split("\\s+");
            PriorityQueue<HashMap.Entry<Integer, Double>> pq = new PriorityQueue<>(new ScoreComparator());
            Map<String, PostingList> postingListMap;
            if(compressionRequired) {
                postingListMap = rfd.readCompressedIndexFromDisk(queryTerms, lookupTable);
            } else {
                postingListMap = rfd.readUncompressedIndexFromDisk(queryTerms, lookupTable);
            }
            for(int doc = 1; doc < docCount; doc++) {
                double currentScore = 0.0;
                for(PostingList p: postingListMap.values()) {
                    p.skipTo(doc);
                    Posting currentPost = p.getCurrentPosting();
                    if(currentPost != null && currentPost.getDocId() == doc) {
                        currentScore += p.getTermFrequency();
                    }
                }

                pq.offer(new AbstractMap.SimpleEntry<Integer, Double>(doc, currentScore));
                if(pq.size() > 20) {
                    pq.poll();
                }
            }

        }
        end = Instant.now();
        System.out.println("\n Time taken for " + (compressionRequired? "compressed" : "uncompressed") + " retrieval is " +
                Duration.between(start, end).toMillis() + " ms");
    }
}

class ScoreComparator implements Comparator<Map.Entry<Integer, Double>> {

    @Override
    public int compare(Map.Entry<Integer, Double> a, Map.Entry<Integer, Double> b) {
        if (a.getValue() < b.getValue()) {
            return -1;
        } else if (a.getValue() > b.getValue()) {
            return 1;
        }
        return 0;
    }
}
