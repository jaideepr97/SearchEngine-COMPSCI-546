package retrieval.cluster;

import index.DocumentVector;
import retrieval.CreateClusters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cluster {

    private int clusterID;
    private List<DocumentVector> clusterDocumentVectors;
    private DocumentVector meanDocumentVector;
    private SimilarityModel similarityModel;

    public int getClusterID() {
        return clusterID;
    }

    public void setClusterID(int clusterID) {
        this.clusterID = clusterID;
    }

    public List<DocumentVector> getClusterDocumentVectors() {
        return clusterDocumentVectors;
    }

    public void setClusterDocumentVectors(List<DocumentVector> clusterDocumentVectors) {
        this.clusterDocumentVectors = clusterDocumentVectors;
    }

    public DocumentVector getMeanDocumentVector() {
        return meanDocumentVector;
    }

    public void setMeanDocumentVector(DocumentVector meanDocumentVector) {
        this.meanDocumentVector = meanDocumentVector;
    }

    public SimilarityModel getSimilarityModel() {
        return similarityModel;
    }

    public void setSimilarityModel(SimilarityModel similarityModel) {
        this.similarityModel = similarityModel;
    }


    public Cluster(int clusterID) {
        this.clusterID = clusterID;
        this.clusterDocumentVectors = new ArrayList<>();
        this.meanDocumentVector = new DocumentVector("", -1);
        this.similarityModel = new CosineSimilarity();
    }


    public void addDocument(DocumentVector candidate) {
        calculateCentroid(candidate);
        this.clusterDocumentVectors.add(candidate);
    }

    public Double calculateSimilarity(DocumentVector documentVector1, DocumentVector documentVector2) {
        return this.similarityModel.calculateSimilarity(documentVector1, documentVector2);
    }

    public Double calculateSimilarityToCentroid(DocumentVector candidate) {
        return this.similarityModel.calculateSimilarity(candidate, meanDocumentVector);
    }

    private void calculateCentroid(DocumentVector candidate) {
        if(clusterDocumentVectors.size() > 0) {

            for(Map.Entry<String, Double> entry: candidate.docVector.entrySet()) {
                meanDocumentVector.docVector.put(entry.getKey(), ( ((meanDocumentVector.docVector.getOrDefault(entry.getKey(), 0.0)* clusterDocumentVectors.size()) + entry.getValue()) / (clusterDocumentVectors.size() + 1)  ));
            }
        } else {
            meanDocumentVector = candidate;
        }
    }

    public Double score(DocumentVector candidate, CreateClusters.Linking linking) {
        switch(linking) {
            case Single: return scoreSingle(candidate);
            case Average: return scoreAverage(candidate);
            case Complete: return scoreComplete(candidate);
            case Mean: return scoreMean(candidate);
            default: return 0.0;
        }
    }

    public Double scoreComplete(DocumentVector candidate) {
        Double score = Double.MAX_VALUE;
        for(DocumentVector docVector: clusterDocumentVectors) {
            Double calcScore = calculateSimilarity(candidate, docVector);
            if(calcScore < score) {
                score = calcScore;
            }
        }
        return score;
    }

    public Double scoreSingle(DocumentVector candidate) {
        Double score = Double.MIN_VALUE;
        for(DocumentVector docVector: clusterDocumentVectors) {
            Double calcScore = calculateSimilarity(candidate, docVector);
            if(calcScore > score) {
                score = calcScore;
            }
        }
        return score;
    }

    public Double scoreAverage(DocumentVector candidate) {
        Double score = 0.0;
        for(DocumentVector docVector: clusterDocumentVectors) {
            Double calcScore = calculateSimilarity(candidate, docVector);
            if(calcScore > score) {
                score += calcScore;
            }
        }
        return score/clusterDocumentVectors.size();
    }

    public Double scoreMean(DocumentVector candidate) {
        return calculateSimilarityToCentroid(candidate);
    }



}
