import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SelectQueryTerms {

    String destination = "filesWrittenToDisk/";

    public static void main(String[] args) throws IOException {
        SelectQueryTerms sqt = new SelectQueryTerms();
        sqt.selectTerms(args[0], args[1]);
    }

    public void selectTerms(String queryTermCount, String filename) throws IOException {
        int qtc = Integer.parseInt(queryTermCount);
        BufferedWriter writer= new BufferedWriter(new FileWriter(destination + filename));

        ReadFromDisk rfd = new ReadFromDisk();
        Map<String, TermLookupEntry> lookupTable = rfd.readLookupFromDisk(false);
        Set<String> set=lookupTable.keySet();
        Object[] vocabulary = set.toArray();
        Arrays.toString(vocabulary);
        Random random=new Random();
        for(int i=1; i<=100; i++) {
            StringBuffer sb = new StringBuffer();
            for(int j=0; j<qtc ;j++) {
                int index=random.nextInt(vocabulary.length);
                writer.write((String) vocabulary[index]+" ");
            }
            if(i<100)
                writer.write("\n");

        }
        writer.close();
    }
}
