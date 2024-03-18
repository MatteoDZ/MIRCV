package it.unipi.dii.aide.mircv.index.posting;

import java.util.*;

/**
 * Represents an index of postings for a specific term in the context of information retrieval.
 */
public class PostingIndex {
    private String term;  // Term associated with the postings
    private final ArrayList<Posting> postings = new ArrayList<>();  // List of postings for the term
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

    public PostingIndex(Posting p) {
        postings.add(p);
    }

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

    public PostingIndex(String term, Integer docId) {
        this.term = term;
        addPosting(docId);
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
     * A method that adds a posting to the list of postings.
     *
     * @param  documentId   the id of the document to add
     */
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


    public void addPosting(Posting posting) {
        Posting existingPosting = postings.stream()
                .filter(p -> p.getDoc_id() == posting.getDoc_id())
                .findFirst()
                .orElse(null);

        if (existingPosting != null) {
            existingPosting.setFrequency(existingPosting.getFrequency() + posting.getFrequency());
        } else {
            postings.add(new Posting(posting.getDoc_id(), posting.getFrequency()));
        }
    }

    /**
     * This method is used to merge the posting list of the intermediate index, in order to create the final posting list
     * @param intermediatePostingList is the posting list of the intermediate index for a specific term
     */
    public void appendList(PostingIndex intermediatePostingList) {
        //here we have to add the posting keeping the sorting in base of the docId
        this.postings.addAll(intermediatePostingList.postings);
        this.postings.sort(Comparator.comparing(Posting::getDoc_id));
    }


    /**
     * Returns a string representation of the object.
     *
     * @return  a string representation of the object
     */
    public String toString() {
        return term + " " + getDocIds().toString() + " " + getFrequencies().toString();}

}
