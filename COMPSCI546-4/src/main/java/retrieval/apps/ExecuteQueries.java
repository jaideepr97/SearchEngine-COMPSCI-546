package retrieval.apps;

import index.index.Index;
import index.index.InvertedIndex;
import retrieval.retrieval.InferenceNetwork;
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

public class ExecuteQueries {

    public static Index index;
    public static InferenceNetwork inferenceNetwork;
    public static RetrievalModel model;
    public static String inputFilePath = "./src/main/java/retrieval/apps/Queries.txt";
    public static String destination = "./src/main/java/retrieval/apps";
    public static int queueSize = 10;
    public static String runId;
    public static int mu = 1500;

    public static void main(String[] args) throws IOException {
        index = new InvertedIndex();
        index.load(false);
        inferenceNetwork = new InferenceNetwork();
        model = new Dirichlet(index, mu);
        ExecuteQueries eq = new ExecuteQueries();
        eq.executeAnd();
        eq.executeOr();
        eq.executeMax();
        eq.executeSum();
        eq.executeOrderedWindow();
        eq.executeUnorderedWindow();
    }

    public void executeAnd () throws IOException {
        ExecuteQueries eq = new ExecuteQueries();
        runId = "jrao-and-dir-" + mu;
        int queryIndex = 0;
        String outputFilePath = destination + "/and.trecrun";
        BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
        FileWriter myWriter = new FileWriter(outputFilePath);
        String query;
        while((query = reader.readLine()) != null) {
            queryIndex++;
            List<QueryNode> termNodes = eq.createTermNodes(query, index, model);
            QueryNode q = new And(termNodes);
            List<Map.Entry<Integer, Double>> rankedList = inferenceNetwork.runQuery(q, queueSize);
            String qId = "Q" + queryIndex;
            eq.writeResultsToFile(rankedList, index, runId, qId, myWriter);
        }
        myWriter.close();

    }

    public void executeOr () throws IOException {
        ExecuteQueries eq = new ExecuteQueries();
        runId = "jrao-or-dir-" + mu;
        int queryIndex = 0;
        String outputFilePath = destination + "/or.trecrun";
        BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
        FileWriter myWriter = new FileWriter(outputFilePath);
        String query;
        while((query = reader.readLine()) != null) {
            queryIndex++;
            List<QueryNode> termNodes = eq.createTermNodes(query, index, model);
            QueryNode q = new Or(termNodes);
            List<Map.Entry<Integer, Double>> rankedList = inferenceNetwork.runQuery(q, queueSize);
            String qId = "Q" + queryIndex;
            eq.writeResultsToFile(rankedList, index, runId, qId, myWriter);
        }
        myWriter.close();

    }

    public void executeMax () throws IOException {
        ExecuteQueries eq = new ExecuteQueries();
        runId = "jrao-max-dir-" + mu;
        int queryIndex = 0;
        String outputFilePath = destination + "/max.trecrun";
        BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
        FileWriter myWriter = new FileWriter(outputFilePath);
        String query;
        while((query = reader.readLine()) != null) {
            queryIndex++;
            List<QueryNode> termNodes = eq.createTermNodes(query, index, model);
            QueryNode q = new Max(termNodes);
            List<Map.Entry<Integer, Double>> rankedList = inferenceNetwork.runQuery(q, queueSize);
            String qId = "Q" + queryIndex;
            eq.writeResultsToFile(rankedList, index, runId, qId, myWriter);
        }
        myWriter.close();
    }

    public void executeSum () throws IOException {
        ExecuteQueries eq = new ExecuteQueries();
        runId = "jrao-sum-dir-" + mu;
        int queryIndex = 0;
        String outputFilePath = destination + "/sum.trecrun";
        BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
        FileWriter myWriter = new FileWriter(outputFilePath);
        String query;
        while((query = reader.readLine()) != null) {
            queryIndex++;
            List<QueryNode> termNodes = eq.createTermNodes(query, index, model);
            QueryNode q = new Sum(termNodes);
            List<Map.Entry<Integer, Double>> rankedList = inferenceNetwork.runQuery(q, queueSize);
            String qId = "Q" + queryIndex;
            eq.writeResultsToFile(rankedList, index, runId, qId, myWriter);
        }
        myWriter.close();
    }

    public void executeOrderedWindow () throws IOException {
        ExecuteQueries eq = new ExecuteQueries();
        runId = "jrao-ordered-window-1-dir-" + mu;
        int queryIndex = 0;
        String outputFilePath = destination + "/od1.trecrun";
        BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
        FileWriter myWriter = new FileWriter(outputFilePath);
        String query;
        while((query = reader.readLine()) != null) {
            queryIndex++;
            List<QueryNode> children = eq.createTermNodes(query, index, model);
            List<ProximityNode> termNodes = new ArrayList<>();
            for(QueryNode child: children)
                termNodes.add((ProximityNode)child);
            QueryNode q = new OrderedWindow(index, model, termNodes, 1);
            List<Map.Entry<Integer, Double>> rankedList = inferenceNetwork.runQuery(q, queueSize);
            String qId = "Q" + queryIndex;
            eq.writeResultsToFile(rankedList, index, runId, qId, myWriter);
        }
        myWriter.close();
    }

    public void executeUnorderedWindow () throws IOException {
        ExecuteQueries eq = new ExecuteQueries();
        runId = "jrao-unordered-window-3*ql-dir-" + mu;
        int queryIndex = 0;
        String outputFilePath = destination + "/uw.trecrun";
        BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
        FileWriter myWriter = new FileWriter(outputFilePath);
        String query;
        while((query = reader.readLine()) != null) {
            queryIndex++;
            int windowSize = 3 * query.split("\\s+").length;
            List<QueryNode> children = eq.createTermNodes(query, index, model);
            List<ProximityNode> termNodes = new ArrayList<>();
            for(QueryNode q: children)
                termNodes.add((ProximityNode)q);
            QueryNode q = new UnorderedWindow(index, model, termNodes, windowSize);
            List<Map.Entry<Integer, Double>> rankedList = inferenceNetwork.runQuery(q, queueSize);
            String qId = "Q" + queryIndex;
            eq.writeResultsToFile(rankedList, index, runId, qId, myWriter);
        }
        myWriter.close();

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

    public List<QueryNode> createTermNodes(String query, Index index, RetrievalModel model) {
        String [] terms = query.split("\\s+");
        List<QueryNode> children = new ArrayList<QueryNode>();
        for (String term : terms) {
            ProximityNode node = new Term(term, index, model);
            children.add(node);
        }
        return children;
    }
}
