package it.unipi.dii.aide.mircv.index.posting;

/**
 * Represents a posting in the context of information retrieval.
 */
public class Posting implements Comparable<Posting>{
    private int docId;  // Document ID
    private int frequency = 0;  // Frequency of the term in the document

    private int docLen = 0;

    /**
     * Initializes a new instance of the Posting class with the specified document ID and frequency.
     *
     * @param doc_id    The document ID.
     * @param frequency The frequency of the term in the document.
     */
    public Posting(int doc_id, int frequency, int docLen) {
        this.docId = doc_id;
        this.frequency = frequency;
        this.docLen = docLen;
    }

    public Posting(int doc_id, int frequency) {
        this.docId = doc_id;
        this.frequency = frequency;
    }

    /**
     * Gets the document ID associated with this posting.
     *
     * @return The document ID.
     */
    public int getDocId() {
        return docId;
    }

    /**
     * Gets the frequency of the term in the document associated with this posting.
     *
     * @return The frequency.
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * Gets the lengths of the document associated with the docId
     *
     * @return The document length.
     */
    public int getDocLen() {
        return docLen;
    }

    /**
     * Sets the document ID for this posting.
     *
     * @param docId The document ID to be set.
     */
    public void setDocId(int docId) {
        this.docId = docId;
    }

    /**
     * Sets the frequency of the term in the document for this posting.
     *
     * @param frequency The frequency to be set.
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void incrementFrequency() {
        this.frequency++;
    }

    /**
     * Returns a string representation of this posting.
     *
     * @return A string representation containing document ID and frequency.
     */
    @Override
    public String toString() {
        return "[doc_id-> " + docId + " freq-> " + frequency + "]";
    }

    @Override
    public int compareTo(Posting o) {

        return Integer.compare(this.docId, o.docId);

        /*
        if (this.doc_id > o.doc_id){
            return 1;
        }
        else return 0;

         */
    }
}
