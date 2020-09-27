public class TermLookupEntry {
    private long offset;
    private long bytesToRead;
    private int termFrequency;
    private int documentFrequency;

    public TermLookupEntry(long offset, long bytesToRead, int termFrequency, int documentFrequency) {
        this.offset = offset;
        this.bytesToRead = bytesToRead;
        this.termFrequency = termFrequency;
        this.documentFrequency = documentFrequency;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getBytesToRead() {
        return bytesToRead;
    }

    public void setBytesToRead(int bytesToRead) {
        this.bytesToRead = bytesToRead;
    }

    public int getTermFrequency() {
        return termFrequency;
    }

    public void setTermFrequency(int termFrequency) {
        this.termFrequency = termFrequency;
    }

    public int getDocumentFrequency() {
        return documentFrequency;
    }

    public void setDocumentFrequency(int documentFrequency) {
        this.documentFrequency = documentFrequency;
    }
}
