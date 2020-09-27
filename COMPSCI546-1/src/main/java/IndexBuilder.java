import java.io.*;

import java.util.HashMap;
import java.util.Map;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class IndexBuilder {
    JSONParser jsonParser = new JSONParser();
    static Map<Integer, String> docIdToSceneId = new HashMap<>();
    static Map<Integer, String> docIdToPlayId = new HashMap<>();
    static Map<Integer, Integer> docIdToDocLength = new HashMap<>();
    static Map<String, PostingList> invertedIndex = new HashMap<>();
    static Map<String, TermLookupEntry> lookupTableUncompressed;
    static Map<String, TermLookupEntry> lookupTableCompressed;

    static Map<String, Integer> sceneToSceneLength = new HashMap<>();
    static Map<String, Integer> playToPlayLength = new HashMap<>();

    static String destination = "filesWrittenToDisk/";

    DeltaEncoder deltaEncoder = new DeltaEncoder();
    VByteEncoder vbyteEncoder = new VByteEncoder();
    WriteToDisk wtd = new WriteToDisk();

    public static void main(String args[]) throws IOException {
        File file = new File("./" + destination);
        boolean create = file.mkdir();
        IndexBuilder indexBuilder = new IndexBuilder();
        indexBuilder.buildIndex(args[0]);

    }
    public void buildIndex(String compression) throws IOException {
        IndexBuilder indexBuilder = new IndexBuilder();

        int docId = 0;
        boolean compressionRequired = false;

        if(compression.equalsIgnoreCase("compressed")) {
            compressionRequired = true;
        }

//        try (FileReader reader = new FileReader("./src/main/java/test-scene.json"))
        try (FileReader reader = new FileReader("./src/main/java/shakespeare-scenes.json"))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray corpus = (JSONArray) jsonObject.get("corpus");
            for(Object sceneObj: corpus){
                JSONObject scene = (JSONObject) sceneObj;
                docId += 1;
                docIdToSceneId.put(docId, scene.get("sceneId").toString());
                docIdToPlayId.put(docId, scene.get("playId").toString());
                String[] sceneWords = scene.get("text").toString().split("\\s+");
                docIdToDocLength.put(docId, sceneWords.length);
                sceneToSceneLength.put(scene.get("sceneId").toString(), sceneToSceneLength.getOrDefault(scene.get("sceneId").toString(), 0) + sceneWords.length);
                playToPlayLength.put(scene.get("playId").toString(), playToPlayLength.getOrDefault(scene.get("playId").toString() , 0) + docIdToDocLength.get(docId));

                for(int position=0; position<sceneWords.length; position++) {
                    if(!invertedIndex.containsKey(sceneWords[position])) {
                        PostingList pl = new PostingList();
                        pl.add(docId, position+1);
                        invertedIndex.put(sceneWords[position], pl);
                    } else {
                        invertedIndex.get(sceneWords[position]).add(docId, position+1);
                    }
                }
            }

            Map<String, Integer[]> processedInvertedIndex = indexBuilder.getProcessedInvertedIndex(invertedIndex);

            if(!compressionRequired) {
                lookupTableUncompressed = wtd.writeUncompressedIndexToDisk(processedInvertedIndex, invertedIndex);

            }
            else {
                Map<String, Integer[]> deltaEncodedInvertedIndex = deltaEncoder.encode(processedInvertedIndex);
                Map<String, byte[]> vByteEncodedInvertedIndex = vbyteEncoder.generateVByteEncodedIndex(deltaEncodedInvertedIndex);
                lookupTableCompressed = wtd.writeCompressedIndexToDisk(vByteEncodedInvertedIndex, invertedIndex);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        indexBuilder.writeMapsToDisk(compressionRequired);

        // For comparison of original and retrieved inverted indexes (after compression & decompression)
        if(compressionRequired) {
            ReadFromDisk rfd = new ReadFromDisk();
            Map<String, TermLookupEntry> lookupTable = rfd.readLookupFromDisk(true);
            String[] queryTerms = new String[lookupTable.keySet().size()];
            lookupTable.keySet().toArray(queryTerms);
            Map<String, PostingList> retrievedInvertedIndex = rfd.readCompressedIndexFromDisk(queryTerms, lookupTable);
            indexBuilder.printInvertedIndex(invertedIndex, destination + "original_inverted_index");
            indexBuilder.printInvertedIndex(retrievedInvertedIndex, destination + "retrieved_inverted_index");

        }
//        indexBuilder.getSceneStatistics();
//        indexBuilder.getPlayStatistics();
    }

    public Map<String, Integer[]> getProcessedInvertedIndex(Map<String, PostingList> invertedIndex) {
        HashMap<String, Integer[]> processedInvertedIndex = new HashMap<>();
        for (Map.Entry<String,PostingList> entry : invertedIndex.entrySet()) {
            Integer[] postingList = entry.getValue().getIntegerFormattedPostingList();
            processedInvertedIndex.put(entry.getKey(), postingList);
        }
        return processedInvertedIndex;
    }

    public void writeMapsToDisk(boolean compressionRequired){


        try {
            FileWriter myWriter = new FileWriter(destination + "docIdToPlayId");
            for(Map.Entry<Integer, String> entry: this.docIdToPlayId.entrySet()) {
                myWriter.write( entry.getKey() + "\t" + entry.getValue()+ "\n");
            }
            myWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileWriter myWriter = new FileWriter(destination + "docIdToSceneId");
            for(Map.Entry<Integer, String> entry: this.docIdToSceneId.entrySet()) {
                myWriter.write( entry.getKey() + "\t" + entry.getValue() + "\n");
            }
            myWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileWriter myWriter = new FileWriter(destination + "docIdToDocLength");
            for(Map.Entry<Integer, Integer> entry: this.docIdToDocLength.entrySet()) {
                myWriter.write( entry.getKey() + "\t" + entry.getValue()+ "\n");
            }
            myWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(compressionRequired) {
            wtd.writeLookupIndexToDisk(this.lookupTableCompressed, true);
        } else {
            wtd.writeLookupIndexToDisk(this.lookupTableUncompressed, false);
        }
    }

    public void printInvertedIndex(Map<String, PostingList> invertedIndex, String filename) {
        try {
            FileWriter myWriter = new FileWriter(filename);
            for(Map.Entry<String, PostingList> entry: invertedIndex.entrySet()) {
                myWriter.write( entry.getKey() + "\t" + entry.getValue().toString() + "\n");
            }
            myWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getSceneStatistics() {
        int longest = Integer.MIN_VALUE, shortest = Integer.MAX_VALUE, totalLength = 0;
        String longestScene = "", shortestScene = "";
        for(Map.Entry<String, Integer> entry: sceneToSceneLength.entrySet()) {
            if(entry.getValue() > longest) {
                longest = entry.getValue();
                longestScene = entry.getKey();
            }
            if(entry.getValue() < shortest) {
                shortest = entry.getValue();
                shortestScene = entry.getKey();
            }
            totalLength += entry.getValue();
        }

        System.out.println("\n Longest scene is: " + longestScene);
        System.out.println("\n Shortest scene is: " + shortestScene);
        System.out.println("\n Average length of a scene is: " + (int)(totalLength/sceneToSceneLength.entrySet().size()) + " words");
    }

    public void getPlayStatistics() {
        int longest = Integer.MIN_VALUE, shortest = Integer.MAX_VALUE, totalLength = 0;
        String longestPlay = "", shortestPlay = "";
        for(Map.Entry<String, Integer> entry: playToPlayLength.entrySet()) {
            if(entry.getValue() > longest) {
                longest = entry.getValue();
                longestPlay = entry.getKey();
            }
            if(entry.getValue() < shortest) {
                shortest = entry.getValue();
                shortestPlay = entry.getKey();
            }
            totalLength += entry.getValue();
        }

        System.out.println("\n Longest play is: " + longestPlay);
        System.out.println("\n Shortest play is: " + shortestPlay);
        System.out.println("\n Average length of a play is: " + (int)(totalLength/playToPlayLength.entrySet().size()) + " words");


    }
}
