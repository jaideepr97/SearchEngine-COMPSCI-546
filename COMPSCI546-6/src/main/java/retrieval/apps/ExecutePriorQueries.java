package retrieval.apps;

import index.index.Index;
import index.index.InvertedIndex;
import retrieval.retrieval.InferenceNetwork;
import retrieval.retrieval.PriorNode;
import retrieval.retrieval.ProximityNode;
import retrieval.retrieval.QueryNode;
import retrieval.retrieval.beliefNodes.And;
import retrieval.retrieval.beliefNodes.Max;
import retrieval.retrieval.beliefNodes.Or;
import retrieval.retrieval.beliefNodes.Sum;
import retrieval.retrieval.models.Dirichlet;
import retrieval.retrieval.models.RetrievalModel;
import retrieval.retrieval.proximityNodes.Term;
import retrieval.retrieval.proximityNodes.windowNodes.OrderedWindow;
import retrieval.retrieval.proximityNodes.windowNodes.UnorderedWindow;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExecutePriorQueries {
    public static Index index;
    public static InferenceNetwork inferenceNetwork;
    public static RetrievalModel model;
    public static String inputFilePath = "./src/main/java/retrieval/apps/Queries.txt";
    public static String destination = "";
    public static int queueSize = 10;
    public static String runId;
    public static int mu = 1500;

    public static void main(String[] args) throws IOException {
        index = new InvertedIndex();
        index.load(false);
        inferenceNetwork = new InferenceNetwork();
        model = new Dirichlet(index, mu);
        ExecutePriorQueries epq = new ExecutePriorQueries();
        epq.executeUniformPrior();
        epq.executeRandomPrior();
    }

    public void executeUniformPrior() throws IOException {
        ExecutePriorQueries epq = new ExecutePriorQueries();
        runId = "jrao-uniform-and-dir-" + mu;
        int queryIndex = 0;
        String outputFilePath = destination + "uniform.trecrun";
        BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
        FileWriter myWriter = new FileWriter(outputFilePath);
        String query;
        while((query = reader.readLine()) != null) {
            queryIndex++;
            List<QueryNode> children = epq.createTermNodes(query, index, model);
            children.add(new PriorNode(PriorNode.priorType.Uniform));
            QueryNode q = new And(children);
            List<Map.Entry<Integer, Double>> rankedList = inferenceNetwork.runQuery(q, queueSize);
            String qId = "Q" + queryIndex;
            epq.writeResultsToFile(rankedList, index, runId, qId, myWriter);
        }
        myWriter.close();
    }

    public void executeRandomPrior() throws IOException {
        ExecutePriorQueries epq = new ExecutePriorQueries();
        runId = "jrao-random-and-dir-" + mu;
        int queryIndex = 0;
        String outputFilePath = destination + "random.trecrun";
        BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
        FileWriter myWriter = new FileWriter(outputFilePath);
        String query;
        while((query = reader.readLine()) != null) {
            queryIndex++;
            List<QueryNode> children = epq.createTermNodes(query, index, model);
            children.add(new PriorNode(PriorNode.priorType.Random));
            QueryNode q = new And(children);
            List<Map.Entry<Integer, Double>> rankedList = inferenceNetwork.runQuery(q, queueSize);
            String qId = "Q" + queryIndex;
            epq.writeResultsToFile(rankedList, index, runId, qId, myWriter);
        }
        myWriter.close();

    }

    public List<QueryNode> createTermNodes(String query, Index index, RetrievalModel model) {
        String [] terms = query.split("\\s+");
        List<QueryNode> children = new ArrayList<QueryNode>();
        for (String term : terms) {
            ProximityNode node = new Term(term, index, model);
            children.add(node);
        }
        return children;
    }

    public void writeResultsToFile(List<Map.Entry<Integer, Double>> results, Index index,
                                   String runId, String qId,  FileWriter myWriter) throws IOException {
        int rank = 1;
        for (Map.Entry<Integer, Double> entry : results) {
            String sceneId = index.getDocName(entry.getKey());
            myWriter.write(String.format(("%s \t skip \t %-30s %d \t %f \t %s \n"),
                    qId,
                    sceneId,
                    rank,
                    entry.getValue(),
                    runId));
            rank++;
        }
    }
}
