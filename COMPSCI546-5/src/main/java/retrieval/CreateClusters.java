package retrieval;

import index.DocumentVector;
import index.Index;
import index.InvertedIndex;
import retrieval.cluster.Cluster;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CreateClusters {

    static InvertedIndex index;
    static List<Cluster> clusters;
    static double threshold;
    public enum Linking {Single, Complete, Average, Mean};
    public Linking linkage = Linking.Mean;

    public static void main(String[] args) throws IOException {
        index = new InvertedIndex();
        clusters = new ArrayList<>();
        threshold = 0.05;
        index.load(false);
        CreateClusters cc = new CreateClusters();
        cc.clusterDocuments();
    }


    public void clusterDocuments() throws IOException {

        for(threshold= 0.05; threshold<1; threshold += 0.05) {
            threshold *= 100;
            threshold = Math.round(threshold);
            threshold /= 100;
            String filename = "cluster-" + threshold + ".out";
            for (int docId = 1; docId <= index.getDocCount(); docId++) {
//                System.out.println("Currently clustering on document : " + docId + " Current threshold: " + threshold);
                DocumentVector currDocVector = getTfIdfWeightedDocVector(index.getDocumentVector(docId));
                if(clusters.size() == 0) {
                    Cluster cluster = new Cluster (1);
                    cluster.addDocument(currDocVector);
                    clusters.add(cluster);
                }
                else {
                    Double maxScore = Double.MIN_VALUE;
                    Cluster bestCluster = null;
                    for(Cluster c: clusters) {
                        Double clusterScore = c.score(currDocVector, linkage);
                        if(clusterScore > maxScore) {
                            maxScore = clusterScore;
                            bestCluster = c;
                        }
                    }

                    if(maxScore > threshold) {
                        bestCluster.addDocument(currDocVector);
                    } else {
                        Cluster newCluster = new Cluster(clusters.size() + 1);
                        newCluster.addDocument(currDocVector);
                        clusters.add(newCluster);
                    }
                }
            }
//                System.out.println("===================================");
//                System.out.println("Threshold: " + threshold + " No. of clusters: " + clusters.size() );
//                int[] bins = new int[8];
//                for(Cluster c:clusters)
//                {
//                    if(c.getClusterDocumentVectors().size() == 1) {
//                        bins[0]++;
//                    }
//                    else if(c.getClusterDocumentVectors().size() == 2) {
//                        bins[1]++;
//                    }
//                    else if(c.getClusterDocumentVectors().size() >= 3 &&  c.getClusterDocumentVectors().size() <= 5) {
//                        bins[2]++;
//                    }
//                    else if(c.getClusterDocumentVectors().size() >= 6 &&  c.getClusterDocumentVectors().size() <= 10) {
//                        bins[3]++;
//                    }
//                    else if(c.getClusterDocumentVectors().size() >= 11 && c.getClusterDocumentVectors().size() <= 50) {
//                        bins[4]++;
//                    }
//                    else if(c.getClusterDocumentVectors().size() >= 51 && c.getClusterDocumentVectors().size() <= 100) {
//                        bins[5]++;
//                    }
//                    else if(c.getClusterDocumentVectors().size() >= 101 && c.getClusterDocumentVectors().size() <= 250) {
//                        bins[6]++;
//                    }
//                    else if(c.getClusterDocumentVectors().size() >= 251 && c.getClusterDocumentVectors().size() <= 400) {
//                        bins[7]++;
//                    }
//                    else if(c.getClusterDocumentVectors().size() >= 401 && c.getClusterDocumentVectors().size() <= 750) {
//                        bins[8]++;
//                    }
//
//                }
//                for(int i: bins) {
//                    System.out.println(i);
//                }
            writeToFile(filename);
            clusters.clear();
        }
    }

    public void writeToFile(String filename) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true));
        for(Cluster c:clusters)
        {
            for(int i=0; i< c.getClusterDocumentVectors().size(); i++)
            {
                bw.append(c.getClusterID() + " " + c.getClusterDocumentVectors().get(i).sceneId);
                bw.append("\n");
            }
        }
        bw.close();
    }

    public DocumentVector getTfIdfWeightedDocVector (DocumentVector candidate) {
        DocumentVector tfIdfWeightedDocVector = new DocumentVector(candidate.sceneId, candidate.docId);
        for(Map.Entry<String, Double> entry: candidate.docVector.entrySet()) {
            Double idf = Math.log((index.getDocCount()+1)/(index.getDocFreq(entry.getKey())+0.5));
            Double tf = entry.getValue();
            tfIdfWeightedDocVector.docVector.put(entry.getKey(), (tf*idf));
        }
        return tfIdfWeightedDocVector;
    }



}
