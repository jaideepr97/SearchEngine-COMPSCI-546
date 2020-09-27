import java.io.IOException;
import java.util.Map;

public class BM25Model {

    public double getDocumentQueryTermScore(int r, int R, int n, int N, double k1, double k2, double K, int docTermFrequency, int queryTermFrequency) {
        double a = Math.log((r + 0.5) / (R - r + 0.5) / ((n - r + 0.5) / (N - n - R + r + 0.5)));
        double b = ((k1 + 1) * docTermFrequency)/(K  + docTermFrequency);
        double c = ((k2 + 1) * queryTermFrequency)/(k2  + queryTermFrequency);

        return a * b * c;
    }

    public double getK(double k1, double b, int docLength, double avgDocLength) throws IOException {
        BM25Model bm25 = new BM25Model();
        double K = k1 * ((1-b) + b * (docLength/ avgDocLength));
        return K;
    }
}
