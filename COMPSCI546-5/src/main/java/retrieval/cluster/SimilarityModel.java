package retrieval.cluster;

import index.DocumentVector;

public interface SimilarityModel {

    public Double calculateSimilarity(DocumentVector dv1, DocumentVector dv2);
}
