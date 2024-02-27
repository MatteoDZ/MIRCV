package it.unipi.dii.aide.mircv.index.posting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents an index of postings for a specific term in the context of information retrieval.
 */
public class PostingIndex {
    private String term;  // Term associated with the postings
    private ArrayList<Posting> postings = new ArrayList<>();  // List of postings for the term
    private Posting postingActual;  // Currently active posting
    private Iterator<Posting> postingIterator;  // Iterator for postings
    private float upperBound;
    private float idf;

    public float getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(float upperBound) {
        this.upperBound = upperBound;
    }

    public float getIdf() {
        return idf;
    }

    public void setIdf(float idf) {
        this.idf = idf;
    }
    /**
     * Retrieves the list of document IDs.
     *
     * @return         	the list of document IDs
     */
    public List<Integer> getDocIds() {
        return new ArrayList<>(postings.stream().map(Posting::getDoc_id).toList());
    }

    public List<Integer> getFrequencies() {
        return new ArrayList<>(postings.stream().map(Posting::getFrequency).toList());
    }

    /**
     * Initializes a new instance of the PostingIndex class.
     */
    public PostingIndex() {}

    public PostingIndex(String term, List<Integer> docIds, List<Integer> frequencies) {
        this.term = term;
        docIds.forEach(docId -> postings.add(new Posting(docId, frequencies.get(docIds.indexOf(docId)))));
    }

    /**
     * Initializes a new instance of the PostingIndex class with the specified term.
     *
     * @param term The term associated with the posting index.
     */
    public PostingIndex(String term) {
        this.term = term;
    }

    /**
     * Gets the currently active posting.
     *
     * @return The currently active posting.
     */
    public Posting getPostingActual() {
        return postingActual;
    }

    /**
     * Gets the term associated with this posting index.
     *
     * @return The term.
     */
    public String getTerm() {
        return term;
    }

    /**
     * Gets the list of postings for the term.
     *
     * @return The list of postings.
     */
    public ArrayList<Posting> getPostings() {
        return postings;
    }

    /**
     * Sets the term for this posting index.
     *
     * @param term The term to be set.
     */
    public void setTerm(String term) {
        this.term = term;
    }

    /**
     * Adds a list of postings to the existing postings.
     *
     * @param postings2Add The list of postings to add.
     */
    public void addPostings(ArrayList<Posting> postings2Add) {
        postings.addAll(postings2Add);
    }

    public void addPosting(int documentId) {
        Posting existingPosting = postings.stream()
                .filter(p -> p.getDoc_id() == documentId)
                .findFirst()
                .orElse(null);

        if (existingPosting != null) {
            existingPosting.incrementFrequency();
        } else {
            postings.add(new Posting(documentId, 1));
        }
    }

}
