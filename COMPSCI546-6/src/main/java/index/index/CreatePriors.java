package index.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CreatePriors {

    public static void main(String[] args) throws IOException {
        CreatePriors cp = new CreatePriors();
        Index index = new InvertedIndex();
        index.load(false);

        List<Double> uniformPriors = new ArrayList<>();
        for(int docId = 1; docId <= index.getDocCount(); docId++) {
            uniformPriors.add(Math.log(1.0/ index.getDocCount()));
        }
        cp.savePrior("uniform.prior", uniformPriors);

        Random rand = new Random();
        List<Double> randomPriors = new ArrayList<>();
        for(int docId = 1; docId <= index.getDocCount(); docId++) {
            randomPriors.add(Math.log(rand.nextDouble()));
        }
        cp.savePrior("random.prior", randomPriors);


    }

    public void savePrior(String filename, List<Double> priorList) throws IOException {
        RandomAccessFile priorWriter = new RandomAccessFile(filename, "rw");
        for(int i=0; i<priorList.size(); i++) {
            priorWriter.writeDouble(priorList.get(i));
        }
        priorWriter.close();
    }
}
