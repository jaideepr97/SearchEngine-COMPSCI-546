import java.util.ArrayList;
public class Posting {
    private int docId;
    private ArrayList<Integer> positions;

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public ArrayList<Integer> getPositions() {
        return positions;
    }

    public void setPositions(ArrayList<Integer> positions) {
        this.positions = positions;
    }

    public int getDocumentTermFrequency() {
        return positions.size();
    }

    public Posting(){
        this.positions = new ArrayList<>();
    }

    public Posting(int docId, int position) {
        this.docId = docId;
        this.positions = new ArrayList<>();
        this.positions.add(position);
    }

    public void updatePosting(int position) {
        this.positions.add(position);
    }

    public ArrayList<Integer> getFormattedPosting() {
        ArrayList<Integer> formattedPosting = new ArrayList<>();
        formattedPosting.add(docId);
        formattedPosting.add(positions.size());
        formattedPosting.addAll(positions);
        return formattedPosting;

    }

    @Override
    public String toString() {
        return "Posting{" +
                "docId=" + docId +
                ", positions=" + positions.toString() +
                '}';
    }
}
