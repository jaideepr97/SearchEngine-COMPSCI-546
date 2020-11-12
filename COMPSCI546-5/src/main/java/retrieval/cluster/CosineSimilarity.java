package retrieval.cluster;

import index.DocumentVector;

import java.util.Map;

public class CosineSimilarity implements SimilarityModel{

    public Double calculateSimilarity(DocumentVector docVector1, DocumentVector docVector2) {
        Double similarity = 0.0, sumOfSquaresDv1 = 0.0, sumOfSquaresDv2 = 0.0, numerator = 0.0;
        for(Double value: docVector1.docVector.values()) {
            sumOfSquaresDv1 += value*value;
        }
        for(Double value: docVector2.docVector.values()) {
            sumOfSquaresDv2 += value*value;
        }

        for(Map.Entry<String, Double> entry: docVector1.docVector.entrySet()) {
            if(docVector2.docVector.containsKey(entry.getKey())) {
                numerator += (entry.getValue() * docVector2.docVector.get(entry.getKey()));
            }
        }

        similarity = (numerator)/(Math.sqrt(sumOfSquaresDv1 * sumOfSquaresDv2));
        return similarity;
    }
}
