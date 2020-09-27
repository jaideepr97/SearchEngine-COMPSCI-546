import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WriteToDisk {
    HashMap<String, TermLookupEntry> lookupTableUncompressed = new HashMap<>();
    HashMap<String, TermLookupEntry> lookupTableCompressed = new HashMap<>();
    String destination = "filesWrittenToDisk/";
    long offset;
    String compressedIndex = "invertedIndex-compressed", uncompressedIndex = "invertedIndex-uncompressed";
    String compressedLookup = "lookupTable-compressed", uncompressedLookup = "lookupTable-uncompressed";
    public Map<String, TermLookupEntry> writeUncompressedIndexToDisk(Map<String, Integer[]> invertedIndex, Map<String, PostingList> originalIndex) throws IOException {
        offset = 0;
        RandomAccessFile invListWriter = new RandomAccessFile(destination + uncompressedIndex, "rw");
        for (Map.Entry<String,Integer[]> entry : invertedIndex.entrySet()) {
            Integer[] postingList = entry.getValue();
            int bytesToWrite = 0;
            for (int i=0; i<postingList.length; i++) {
                int docId = postingList[i];
                int positionArraySize = postingList[i+1];
                int length = positionArraySize + 2;
                ByteBuffer byteBuffer = ByteBuffer.allocate(length * 4);
                bytesToWrite += length * 4;

                byteBuffer.putInt(docId);
                byteBuffer.putInt(positionArraySize);
                i += 2;
                int startPosition = i;
                while(i < startPosition + positionArraySize) {
                    byteBuffer.putInt(postingList[i]);
                    i++;
                }
                i--;
                byte[] array = byteBuffer.array();
                invListWriter.write(array);

            }
            PostingList plForCurrentTerm = originalIndex.get(entry.getKey());
            TermLookupEntry tle = new TermLookupEntry(offset, bytesToWrite, plForCurrentTerm.getTermFrequency(), plForCurrentTerm.getDocumentFrequency());
            lookupTableUncompressed.put(entry.getKey(), tle);
            offset = invListWriter.getFilePointer();
        }
        invListWriter.close();
        return lookupTableUncompressed;
    }

    public Map<String, TermLookupEntry> writeCompressedIndexToDisk(Map<String, byte[]> invertedIndex, Map<String, PostingList> originalIndex) throws IOException {
        offset = 0;
        RandomAccessFile invListWriter = new RandomAccessFile(destination + compressedIndex, "rw");
        for(Map.Entry<String, byte[]> entry: invertedIndex.entrySet()) {
            PostingList plForCurrentTerm = originalIndex.get(entry.getKey());
            TermLookupEntry tle = new TermLookupEntry(offset, entry.getValue().length, plForCurrentTerm.getTermFrequency(), plForCurrentTerm.getDocumentFrequency());
            lookupTableCompressed.put(entry.getKey(), tle);
            invListWriter.write(entry.getValue());
            offset = invListWriter.getFilePointer();
        }
        invListWriter.close();
        return lookupTableCompressed;
    }

    public void writeLookupIndexToDisk(Map<String, TermLookupEntry> lookupTable, boolean compressionRequired) {
        String filename = compressionRequired? compressedLookup: uncompressedLookup;
        try {
            FileWriter myWriter = new FileWriter(destination + filename);
            for(Map.Entry<String, TermLookupEntry> entry: lookupTable.entrySet()) {
                myWriter.write( entry.getKey() + "\t"
                                    + entry.getValue().getOffset() + "\t"
                                    + entry.getValue().getBytesToRead() + "\t"
                                    + entry.getValue().getTermFrequency() + "\t"
                                    + entry.getValue().getDocumentFrequency()+ "\n");
            }
            myWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
