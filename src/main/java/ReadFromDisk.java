import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;

public class ReadFromDisk {

    String compressedIndex = "invertedIndex-compressed", uncompressedIndex = "invertedIndex-uncompressed";
    String compressedLookup = "lookupTable-compressed", uncompressedLookup = "lookupTable-uncompressed";
    String destination = "filesWrittenToDisk/";
    long offset;
    long buffLength;

    public Map<String, PostingList> readUncompressedIndexFromDisk(String[] queryTerms, Map<String, TermLookupEntry> lookupTable) throws IOException {
        RandomAccessFile reader = new RandomAccessFile(destination + uncompressedIndex, "rw");
        Map<String, PostingList> queryInvertedIndex = new HashMap<>();
        for(String queryTerm: queryTerms) {
            TermLookupEntry tle = lookupTable.get(queryTerm);
            PostingList postingList = new PostingList();
            offset = tle.getOffset();
            buffLength = tle.getBytesToRead();
            byte[] buffer = new byte[(int) buffLength];
            reader.seek(offset);
            reader.read(buffer, 0, (int) buffLength);
            int off = 0;

            while (off < buffLength) {
                Posting posting = new Posting();
                int docId = fromByteArray(Arrays.copyOfRange(buffer, off, off + 4));
                posting.setDocId(docId);
                off += 4;
                int tf = fromByteArray(Arrays.copyOfRange(buffer, off, off + 4));
                off +=4;
                for (int i = 0; i < tf; i++) {
                    posting.updatePosting(fromByteArray(Arrays.copyOfRange(buffer, off, off + 4)));
                    off += 4;
                }
                postingList.addPosting(posting);
            }
            queryInvertedIndex.put(queryTerm, postingList);

        }
        reader.close();
        return queryInvertedIndex;
    }

    public Map<String, PostingList> readCompressedIndexFromDisk(String[] queryTerms, Map<String, TermLookupEntry> lookupTable) throws IOException {
        RandomAccessFile reader = new RandomAccessFile(destination + compressedIndex, "rw");
        Map<String, PostingList> queryInvertedIndex = new HashMap<>();
        VByteEncoder vByteEncoder = new VByteEncoder();
        DeltaEncoder deltaEncoder = new DeltaEncoder();
        for(String queryTerm: queryTerms) {
            TermLookupEntry tle = lookupTable.get(queryTerm);
            PostingList postingList = new PostingList();
            offset = tle.getOffset();
            buffLength = tle.getBytesToRead();
            byte[] buffer = new byte[(int) buffLength];
            reader.seek(offset);
            reader.read(buffer, 0, (int) buffLength);
            IntBuffer intBuffer = IntBuffer.allocate((int) buffLength);
            IntBuffer ib = vByteEncoder.decode(buffer, intBuffer);
            int[] tempDeltaEncodedPostingList = ib.array();
            int j = tempDeltaEncodedPostingList.length-1;
            while(tempDeltaEncodedPostingList[j] == 0) {
                --j;
            }
            int[] deltaEncodedPostingList = new int[j+1];
            for (int i=0; i<deltaEncodedPostingList.length; i++)
                deltaEncodedPostingList[i] = tempDeltaEncodedPostingList[i];
            int[] flattenedPostingList = deltaEncoder.decode(deltaEncodedPostingList);

            for(int i=0; i<flattenedPostingList.length; i++) {
                Posting posting = new Posting();
                int docId = flattenedPostingList[i];
                int positionArraySize = flattenedPostingList[i+1];
                int startPosition = i+2, endPosition = startPosition + positionArraySize -1;
                ArrayList<Integer> positions = new ArrayList<>();
                while(startPosition <= endPosition) {
                    positions.add(flattenedPostingList[startPosition]);
                    startPosition++;
                }
                i = endPosition;
                posting.setDocId(docId);
                posting.setPositions(positions);
                postingList.addPosting(posting);
            }

            queryInvertedIndex.put(queryTerm, postingList);
        }
        return queryInvertedIndex;
    }

        public Map<String, TermLookupEntry> readLookupFromDisk(boolean compressionRequired) throws IOException {
        String filename = compressionRequired? compressedLookup: uncompressedLookup;
        String line;
        Map<String, TermLookupEntry> lookupTable = new HashMap<>();
        FileReader fileReader = new FileReader(destination + filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        while((line = bufferedReader.readLine()) != null) {
            String[] data = line.split("\\s+");
            String term = data[0];
            long offset = Long.parseLong(data[1]);
            long numBytes = Long.parseLong(data[2]);
            int dtf = Integer.parseInt(data[3]);
            int ctf = Integer.parseInt(data[4]);
            TermLookupEntry tle = new TermLookupEntry(offset, numBytes, dtf, ctf);

            lookupTable.put(term, tle);
        }
        bufferedReader.close();
        return lookupTable;
    }

    public Map<Integer, Integer> readDocLengthsFromDisk() throws IOException {
        String filename = "docIdToDocLength";
        FileReader fileReader = new FileReader(destination + filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        Map<Integer, Integer> docIdToDocLength = new HashMap<>();
        String line;
        while((line = bufferedReader.readLine()) != null) {
            String[] data = line.split("\\s+");
            docIdToDocLength.put(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
        }

        return docIdToDocLength;
    }

    public Map<Integer, String> readDocIdMappingFromDisk(String filename) throws IOException {
        FileReader fileReader = new FileReader(destination + filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        Map<Integer, String> docIdMapping = new HashMap<>();
        String line;
        while((line = bufferedReader.readLine()) != null) {
            String[] data = line.split("\\s+");
            docIdMapping.put(Integer.parseInt(data[0]), data[1]);
        }

        return docIdMapping;
    }


    private int fromByteArray(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

}
