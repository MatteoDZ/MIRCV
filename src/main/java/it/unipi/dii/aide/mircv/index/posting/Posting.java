package it.unipi.dii.aide.mircv.index.posting;

/**
 * Represents a posting in the context of information retrieval.
 */
public class Posting implements Comparable<Posting>{
    private int doc_id;  // Document ID
    private int frequency = 0;  // Frequency of the term in the document

    /**
     * Initializes a new instance of the Posting class with the specified document ID and frequency.
     *
     * @param doc_id    The document ID.
     * @param frequency The frequency of the term in the document.
     */
    public Posting(int doc_id, int frequency) {
        this.doc_id = doc_id;
        this.frequency = frequency;
    }

    /**
     * Gets the document ID associated with this posting.
     *
     * @return The document ID.
     */
    public int getDoc_id() {
        return doc_id;
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
     * Sets the document ID for this posting.
     *
     * @param doc_id The document ID to be set.
     */
    public void setDoc_id(int doc_id) {
        this.doc_id = doc_id;
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
        return "[doc_id-> " + doc_id + " freq-> " + frequency + "]";
    }

    @Override
    public int compareTo(Posting o) {

        return Integer.compare(this.doc_id, o.doc_id);

        /*
        if (this.doc_id > o.doc_id){
            return 1;
        }
        else if (this.doc_id < o.doc_id){
            return -1;
        }
        else return 0;
         */
    }
}
