import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class QueryRetrieval {

    String destination = "filesWrittenToDisk/";
    static Map<Integer, Integer> docIdToDocLengths;
    static Map<Integer, String> docIdToSceneId;
    static int docCount;
    static boolean compressionRequired;
    static Map<String, TermLookupEntry> lookupTable;
    static  ReadFromDisk rfd;

    public static void main(String[] args) throws IOException {
        QueryRetrieval qr = new QueryRetrieval();
        String inputFilename = args[0];
        String outputFilename = args[1];
        String retrievalModel = args[2];

        rfd = new ReadFromDisk();
        docIdToDocLengths = rfd.readDocLengthsFromDisk();
        docIdToSceneId = rfd.readDocIdMappingFromDisk("docIdToSceneId");
        docCount = docIdToDocLengths.entrySet().size();
        compressionRequired = false;
        lookupTable = rfd.readLookupFromDisk(compressionRequired);

        switch(retrievalModel.toLowerCase()) {
            case "vs" : qr.retrieveQueryVectorSpace(inputFilename, outputFilename);
                        break;
            case "bm25" : qr.retrieveQueryBM25(inputFilename, outputFilename);
                        break;
            case "ql-jm": qr.retrieveQueryQL(inputFilename, outputFilename, "jm");
                        break;
            case "ql-dir": qr.retrieveQueryQL(inputFilename, outputFilename, "dir");
                        break;
        }
    }

    public void retrieveQueryVectorSpace(String queryTermsFilename, String outputFilename) throws IOException {
        VectorSpaceModel vsm = new VectorSpaceModel();
        BufferedReader reader = new BufferedReader(new FileReader(destination + queryTermsFilename));

        String query;
        FileWriter myWriter = new FileWriter(destination + outputFilename);
        while((query = reader.readLine()) != null) {
            String[] queryTerms = query.split("\\s+");
            PriorityQueue<HashMap.Entry<Integer, Double>> pq = new PriorityQueue<>(new ScoreComparator());
            Map<String, PostingList> postingListMap;
            Map<String, Integer> counts = new HashMap<>();
            for(String queryTerm: queryTerms) {
                counts.put(queryTerm, counts.getOrDefault(queryTerm, 0) + 1);
            }

            if(compressionRequired) {
                postingListMap = rfd.readCompressedIndexFromDisk(queryTerms, lookupTable);
            } else {
                postingListMap = rfd.readUncompressedIndexFromDisk(queryTerms, lookupTable);
            }

            for(int doc = 1; doc <= docCount; doc++) {
                double currentScore = 0.0;
                int nonzero = 0;
                for(Map.Entry<String, PostingList> entry: postingListMap.entrySet()) {
                    entry.getValue().skipTo(doc);
                    Posting currentPost = entry.getValue().getCurrentPosting();
                    if(currentPost != null && currentPost.getDocId() == doc) {
                        double docTermWeight = vsm.getDocumentTermWeight(entry.getKey(), docCount, currentPost, postingListMap);
                        double queryTermWeight = vsm.getQueryTermWeight(entry.getKey(), docCount, postingListMap, counts);
                        currentScore += docTermWeight * queryTermWeight;
                        nonzero++;
                    }
                }

                if(nonzero == 0)
                    continue;
                currentScore = currentScore/docIdToDocLengths.get(doc);
                pq.offer(new AbstractMap.SimpleEntry<Integer, Double>(doc, currentScore));
                if(pq.size() > docCount) {
                    pq.poll();
                }
            }

            int rank = 1;
            while(!pq.isEmpty()) {
                Map.Entry<Integer, Double> entry = pq.poll();
                myWriter.write(String.format(("%s \t skip \t %-30s \t %d \t %f \t jrao-vs-logtf-logidf \n"),
                                                queryTerms[0],
                                                docIdToSceneId.get(entry.getKey()),
                                                rank,
                                                entry.getValue()));
                rank++;
            }
            myWriter.write("===========================================================\n");
        }
        myWriter.close();
    }


    public void retrieveQueryBM25(String queryTermsFilename, String outputFilename) throws IOException {
        BM25Model bm25 = new BM25Model();
        QueryRetrieval qr = new QueryRetrieval();
        BufferedReader reader = new BufferedReader(new FileReader(destination + queryTermsFilename));
        double k1 = 1.5, b = 0.75, K = 0;
        int r = 0, R = 0, k2 = 500;
        double avgDocLength = qr.getAvgDocLength();

        String query;
        FileWriter myWriter = new FileWriter(destination + outputFilename);
        while((query = reader.readLine()) != null) {
            String[] queryTerms = query.split("\\s+");
            PriorityQueue<HashMap.Entry<Integer, Double>> pq = new PriorityQueue<>(new ScoreComparator());
            Map<String, PostingList> postingListMap;

            if(compressionRequired) {
                postingListMap = rfd.readCompressedIndexFromDisk(queryTerms, lookupTable);
            } else {
                postingListMap = rfd.readUncompressedIndexFromDisk(queryTerms, lookupTable);
            }

            Map<String, Integer> counts = new HashMap<>();
            for(String queryTerm: queryTerms) {
                counts.put(queryTerm, counts.getOrDefault(queryTerm, 0) + 1);
            }

            for(int doc = 1; doc <= docCount; doc++) {
                double currentScore = 0.0;
                int nonzero = 0;
                for(Map.Entry<String, PostingList> entry: postingListMap.entrySet()) {
                    entry.getValue().skipTo(doc);
                    Posting currentPost = entry.getValue().getCurrentPosting();

                    if(currentPost != null && currentPost.getDocId() == doc) {
                        K = bm25.getK(k1, b, docIdToDocLengths.get(doc), avgDocLength);
                        int docTermFrequency = currentPost.getDocumentTermFrequency();
                        int queryTermFrequency = counts.get(entry.getKey());
                        int documentFrequency = entry.getValue().getDocumentFrequency();
                        currentScore += bm25.getDocumentQueryTermScore(r, R, documentFrequency, docCount, k1, k2, K, docTermFrequency, queryTermFrequency);
                        nonzero++;
                    }
                }
                if(nonzero == 0)
                    continue;
                pq.offer(new AbstractMap.SimpleEntry<Integer, Double>(doc, currentScore));
                if(pq.size() > docCount) {
                    pq.poll();
                }
            }
            int rank = 1;
            while(!pq.isEmpty()) {
                Map.Entry<Integer, Double> entry = pq.poll();
                myWriter.write(String.format(("%s \t skip \t %-30s %d \t %f \t jrao-bm25-%f-%d-%f \n"),
                        queryTerms[0],
                        docIdToSceneId.get(entry.getKey()),
                        rank,
                        entry.getValue(),
                        k1,
                        k2,
                        b));
                rank++;
            }
            myWriter.write("===========================================================\n");
        }
        myWriter.close();
    }


    public void retrieveQueryQL(String queryTermsFilename, String outputFilename, String model) throws IOException {
        QueryLikelihoodModel qlm = new QueryLikelihoodModel();
        QueryRetrieval qr = new QueryRetrieval();
        BufferedReader reader = new BufferedReader(new FileReader(destination + queryTermsFilename));
        long collectionSize = qr.getCollectionSize();
        double lambda = 0.2;
        int mu = 1200;

        String query;
        FileWriter myWriter = new FileWriter(destination + outputFilename);
        while((query = reader.readLine()) != null) {
            String[] queryTerms = query.split("\\s+");
            PriorityQueue<HashMap.Entry<Integer, Double>> pq = new PriorityQueue<>(new ScoreComparator());
            Map<String, PostingList> postingListMap;

            if(compressionRequired) {
                postingListMap = rfd.readCompressedIndexFromDisk(queryTerms, lookupTable);
            } else {
                postingListMap = rfd.readUncompressedIndexFromDisk(queryTerms, lookupTable);
            }

            for(int doc = 1; doc <= docCount; doc++) {
                double currentScore = 0.0;
                int nonzero = 0;

                for(Map.Entry<String, PostingList> entry: postingListMap.entrySet()) {
                    entry.getValue().startIteration();
                    entry.getValue().skipTo(doc);
                    Posting currentPost = entry.getValue().getCurrentPosting();
                    int docTermFrequency = 0;
                    if(currentPost != null && currentPost.getDocId() == doc) {
                        docTermFrequency = currentPost.getDocumentTermFrequency();
                        nonzero++;
                    }
                    int termFrequency = entry.getValue().getTermFrequency();
                    int docLength = docIdToDocLengths.get(doc);
                    currentScore += qlm.getDocumentQueryTermScore(lambda, mu, docLength, docTermFrequency, termFrequency, collectionSize, model);
                }
                if(nonzero == 0)
                    continue;

                pq.offer(new AbstractMap.SimpleEntry<Integer, Double>(doc, currentScore));
                if(pq.size() > docCount) {
                    pq.poll();
                }
            }
            int rank = 1;
            while(!pq.isEmpty()) {
                Map.Entry<Integer, Double> entry = pq.poll();
                if(model.equalsIgnoreCase("jm")) {
                    myWriter.write(String.format(("%s \t skip \t %-30s %d \t %f \t jrao-ql-jm-%f \n"),
                            queryTerms[0],
                            docIdToSceneId.get(entry.getKey()),
                            rank,
                            entry.getValue(),
                            lambda));
                }
                else if(model.equalsIgnoreCase("dir")) {
                    myWriter.write(String.format(("%s \t skip \t %-30s %d \t %f \t jrao-ql-dir-%d \n"),
                            queryTerms[0],
                            docIdToSceneId.get(entry.getKey()),
                            rank,
                            entry.getValue(),
                            mu));
                }
                rank++;
            }
            myWriter.write("===========================================================\n");
        }
        myWriter.close();
    }

    public long getCollectionSize() throws IOException {
        int totalDocsLength = 0;

        for(Map.Entry<Integer, Integer> entry: docIdToDocLengths.entrySet()) {
            totalDocsLength += entry.getValue();
        }
        return totalDocsLength;
    }

    public double getAvgDocLength() throws IOException {
        int totalDocsLength = 0;

        for(Map.Entry<Integer, Integer> entry: docIdToDocLengths.entrySet()) {
            totalDocsLength += entry.getValue();
        }
        return (double) totalDocsLength/docIdToDocLengths.keySet().size();
    }
}

class ScoreComparator implements Comparator<Map.Entry<Integer, Double>> {

    @Override
    public int compare(Map.Entry<Integer, Double> a, Map.Entry<Integer, Double> b) {
        if (a.getValue() < b.getValue()) {
            return 1;
        } else if (a.getValue() > b.getValue()) {
            return -1;
        }
        return 0;
    }
}

