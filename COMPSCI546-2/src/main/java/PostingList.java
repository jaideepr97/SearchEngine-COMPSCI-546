import java.util.ArrayList;
import java.util.List;

public class PostingList {

    private ArrayList<Posting> postingList;

    public int getPostingIndex() {
        return postingIndex;
    }

    public void setPostingIndex(int postingIndex) {
        this.postingIndex = postingIndex;
    }

    private int postingIndex;
    private int termFrequency;
    private int documentFrequency;

    public ArrayList<Posting> getPostingList() {
        return postingList;
    }

    public void setPostingList(ArrayList<Posting> postingList) {
        this.postingList = postingList;
    }

    public PostingList() {
        postingList = new ArrayList<>();
        postingIndex = 0;
    }

    public void startIteration(){
        postingIndex = 0;
    }
    public boolean hasNext(){
        if(postingIndex >= 0 && postingIndex < postingList.size())
            return true;
        return false;
    }

    public Posting getCurrentPosting(){
        Posting currentPosting = null;
        try{
            currentPosting = postingList.get(postingIndex);
        } catch (IndexOutOfBoundsException ex) {}
        return currentPosting;
    }

    public void skipTo(int docId) {
        while(postingIndex < postingList.size() && getCurrentPosting().getDocId() < docId) {
            postingIndex++;
        }
    }



    public void add(int docId, int position) {
        if(postingList.size() > 0 && postingList.get(postingList.size()-1) != null && postingList.get(postingList.size()-1).getDocId() == docId) {
            postingList.get(postingList.size()-1).updatePosting(position);
        } else {
            addPosting(new Posting(docId, position));
        }
    }

    public void addPosting(Posting posting) {
        postingList.add(posting);
    }

    public Integer[] getIntegerFormattedPostingList() {
        List<Integer> formattedList = new ArrayList<>();
        for(Posting p: postingList) {
            formattedList.addAll(p.getFormattedPosting());
        }
        Integer[] result = formattedList.toArray(new Integer[formattedList.size()]);
        return result;
    }

    public int getTermFrequency(){
        this.termFrequency = 0;
        for(Posting p: postingList) {
            this.termFrequency += p.getDocumentTermFrequency();
        }
        return this.termFrequency;
    }

    public int getDocumentFrequency(){
        this.documentFrequency = this.postingList.size();
        return this.documentFrequency;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Posting List = { ");
        for(Posting p: postingList) {
            sb.append(p.toString());
            sb.append(" ");
        }
        sb.append(" }");
        return sb.toString();
    }
}
