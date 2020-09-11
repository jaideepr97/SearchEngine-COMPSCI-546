import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ComputeDice {

    static Map<String, TermLookupEntry> lookupTable;
    static String[] vocabulary;
    static Map<String, PostingList> invertedIndex;
    static String destination = "filesWrittenToDisk/";


    public static void main(String[] args) throws IOException {
        ComputeDice cd = new ComputeDice();
        ReadFromDisk rfd = new ReadFromDisk();

        lookupTable = rfd.readLookupFromDisk(false);
        vocabulary = new String[lookupTable.keySet().size()];
        lookupTable.keySet().toArray(vocabulary);
        invertedIndex = rfd.readUncompressedIndexFromDisk(vocabulary, lookupTable);

        cd.computeDiceQueryTerms(destination + args[0], args[1]);

    }

    public void computeDiceQueryTerms(String queryTermsFilename, String outputFileName) throws IOException {
        ComputeDice cd = new ComputeDice();
        BufferedWriter writer= new BufferedWriter(new FileWriter(destination + outputFileName));
        BufferedReader reader = new BufferedReader(new FileReader(queryTermsFilename));
        String query;
        while((query = reader.readLine()) != null) {
            String[] queryTerms = query.split("\\s+");
            List<String> mostRelevantMatches = new ArrayList<>();
            for(String queryTerm : queryTerms) {
                double bestScore = 0;
                String mostRelevantMatch = "";
                for(String vocabTerm: vocabulary) {
                    double computedDiceScore = cd.computeDiceScore(queryTerm, vocabTerm);
                    if( computedDiceScore > bestScore) {
                        bestScore = computedDiceScore;
                        mostRelevantMatch = vocabTerm;
                    }
                }
                mostRelevantMatches.add(mostRelevantMatch);
            }

            for(int i=0; i<queryTerms.length; i++) {
                writer.write(queryTerms[i] + " " + mostRelevantMatches.get(i) + " ");
            }
            writer.write("\n");
        }
        reader.close();
        writer.close();
    }

    public double computeDiceScore (String queryTerm, String vocabTerm) throws IOException {
        PostingList queryTermPostingList = invertedIndex.get(queryTerm);
        PostingList vocabTermPostingList = invertedIndex.get(vocabTerm);
        int nQueryTerm = lookupTable.get(queryTerm).getTermFrequency();
        int nVocabTerm = lookupTable.get(vocabTerm).getTermFrequency();
        double nTogether = 0.0;

        for(int i=0; i<queryTermPostingList.getPostingList().size(); i++) {
            Posting queryTermPosting = queryTermPostingList.getPostingList().get(i);
            Posting vocabTermPosting = null;
            for(int j=0; j<vocabTermPostingList.getPostingList().size(); j++) {
                if(vocabTermPostingList.getPostingList().get(j).getDocId() == queryTermPosting.getDocId()) {
                    vocabTermPosting = vocabTermPostingList.getPostingList().get(j);
                    break;
                }
            }
            if(vocabTermPosting != null && vocabTermPosting.getDocId() == queryTermPosting.getDocId()) {
                List<Integer> queryTermPostingPositions = queryTermPosting.getPositions();
                List<Integer> vocabTermPostingPositions = vocabTermPosting.getPositions();
                for(int qIdx = 0; qIdx < queryTermPostingPositions.size(); qIdx++ ) {
                    for(int vIdx = 0; vIdx < vocabTermPostingPositions.size(); vIdx++ ) {
                        if(vocabTermPostingPositions.get(vIdx) == (queryTermPostingPositions.get(qIdx) + 1)) {
                            nTogether++;
                        }
                    }
                }

            }
            queryTermPostingList.skipTo(queryTermPosting.getDocId() + 1);

        }

        //        while(queryTermPostingList.hasNext()) {
//            Posting queryTermPosting = queryTermPostingList.getCurrentPosting();
//            vocabTermPostingList.skipTo(queryTermPosting.getDocId());
//            Posting vocabTermPosting = vocabTermPostingList.getCurrentPosting();
//            if(vocabTermPosting != null && vocabTermPosting.getDocId() == queryTermPosting.getDocId()) {
//                List<Integer> queryTermPostingPositions = queryTermPosting.getPositions();
//                List<Integer> vocabTermPostingPositions = vocabTermPosting.getPositions();
//                for(int qIdx = 0; qIdx < queryTermPostingPositions.size(); qIdx++ ) {
//                    for(int vIdx = 0; vIdx < vocabTermPostingPositions.size(); vIdx++ ) {
//                        if(vocabTermPostingPositions.get(vIdx) == (queryTermPostingPositions.get(qIdx) + 1)) {
//                            nTogether++;
//                        }
//                    }
//                }
//
//            }
//            queryTermPostingList.skipTo(queryTermPosting.getDocId() + 1);
//        }

        return nTogether/(nQueryTerm + nVocabTerm);
    }
}
