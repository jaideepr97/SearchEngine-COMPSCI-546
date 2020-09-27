
public class QueryLikelihoodModel {

    public double getDocumentQueryTermScore (double lambda, int mu, int docLength, int docTermFrequency, int termFrequency, long collectionSize, String model) {
        double alphaD = 0;
        if(model.equalsIgnoreCase("jm")) {
            alphaD = lambda;
        }
        else if(model.equalsIgnoreCase("dir")) {
            alphaD = (double) mu / (mu + docLength);
        }

        double a = (1 - alphaD) * docTermFrequency / docLength;
        double b = alphaD * termFrequency / collectionSize;
        return Math.log(a + b);
    }
}
