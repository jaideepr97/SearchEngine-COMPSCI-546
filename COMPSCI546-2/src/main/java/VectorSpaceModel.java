import java.util.Map;

public class VectorSpaceModel {

    public double getQueryTermWeight(String queryTerm, int docCount, Map<String, PostingList> postingListMap, Map<String, Integer> queryTermCounts) {
        double qtf = 1 + Math.log(queryTermCounts.get(queryTerm));
        double qidf = Math.log((double) docCount / postingListMap.get(queryTerm).getDocumentFrequency());
        return qtf * qidf;
    }

    public Double getDocumentTermWeight(String queryTerm, int docCount, Posting currentPosting, Map<String, PostingList> postingListMap) {
        double qtf = 1 + Math.log(currentPosting.getDocumentTermFrequency());
        double qidf = Math.log((double) docCount / postingListMap.get(queryTerm).getDocumentFrequency());
        return qtf * qidf;
    }
}


