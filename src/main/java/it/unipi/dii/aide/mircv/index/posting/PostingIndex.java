package it.unipi.dii.aide.mircv.index.posting;

import it.unipi.dii.aide.mircv.index.merge.Lexicon;
import it.unipi.dii.aide.mircv.index.merge.SkippingBlock;

import java.io.IOException;
import java.util.*;

/**
 * Represents an index of postings for a specific term in the context of information retrieval.
 */
public class PostingIndex {
    private String term;  // Term associated with the postings
    private final ArrayList<Posting> postings = new ArrayList<>();  // List of postings for the term
    private Posting currentPosting;  // Currently active posting
    private Iterator<Posting> postingIterator;  // Iterator for postings
    private float upperBound;
    private float idf;
    private SkippingBlock skippingBlockCurrent;  // Currently active skipping block
    private Iterator<SkippingBlock> skippingBlockIterator;  // Iterator for skipping blocks

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
     * @return the list of document IDs
     */
    public List<Integer> getDocIds() {
        return new ArrayList<>(postings.stream().map(Posting::getDocId).toList());
    }

    public List<Integer> getFrequencies() {
        return new ArrayList<>(postings.stream().map(Posting::getFrequency).toList());
    }

    /**
     * Initializes a new instance of the PostingIndex class.
     */
    public PostingIndex() {
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
    public Posting getCurrentPosting() {
        return currentPosting;
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
     * @param documentId the id of the document to add
     */
    public void addPosting(int documentId) {
        Posting existingPosting = postings.stream()
                .filter(p -> p.getDocId() == documentId)
                .findFirst()
                .orElse(null);

        if (existingPosting != null) {
            existingPosting.incrementFrequency();
        } else {
            postings.add(new Posting(documentId, 1, 1));
        }
    }


    /**
     * This method is used to merge the posting list of the intermediate index, in order to create the final posting list
     *
     * @param intermediatePostingList is the posting list of the intermediate index for a specific term
     */
    public void appendList(PostingIndex intermediatePostingList) {
        //here we have to add the posting keeping the sorting in base of the docId
        this.postings.addAll(intermediatePostingList.postings);
        this.postings.sort(Comparator.comparing(Posting::getDocId));
    }


    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object
     */
    public String toString() {
        return term + " " + getDocIds().toString() + " " + getFrequencies().toString() + " " + getIdf();
    }


    /**
     * Opens the posting list by reading associated skipping blocks from the lexicon.
     */
    public void openList() throws IOException {
        // List of skipping blocks for efficient iteration
        ArrayList<SkippingBlock> blocks = Lexicon.getInstance().get(term).readBlocks();

        if (blocks == null) {
            return;
        }
        skippingBlockIterator = blocks.iterator();
        postingIterator = postings.iterator();
    }

    /**
     * Moves to the next posting in the list.
     */
    public void next(Boolean compression) {
        if (postingIterator.hasNext()) {
            currentPosting = postingIterator.next();
        } else if (skippingBlockIterator.hasNext()) {
            skippingBlockCurrent = skippingBlockIterator.next();
            postings.clear();
            postings.addAll(skippingBlockCurrent.getSkippingBlockPostings(compression));
            postingIterator = postings.iterator();
            currentPosting = postingIterator.next();
        } else {
            currentPosting = null;
        }
    }

    /**
     * Moves to the next posting with a document ID greater than or equal to the specified value.
     *
     * @param docId The document ID to compare.
     * @return The next posting with a document ID greater than or equal to doc_id, or null if not found.
     */
    public Posting nextGEQ(int docId, boolean compression) {
        boolean skip = false;
        while(skippingBlockCurrent == null || skippingBlockCurrent.getDocIdMax() < docId) {
            if (skippingBlockIterator.hasNext()) {
                skippingBlockCurrent = skippingBlockIterator.next();
                skip = true;
            } else {
                currentPosting = null;
                return null;
            }
        }
        if (skip) {
            postings.clear();
            postings.addAll(skippingBlockCurrent.getSkippingBlockPostings(compression));
            postingIterator = postings.iterator();
        }
        while (currentPosting.getDocId() < docId){
            if (postingIterator.hasNext()) {
                currentPosting = postingIterator.next();
            }
        }
        if (currentPosting.getDocId() >= docId) {
            return currentPosting;
        }
        else {
            currentPosting = null;
            return null;
        }
    }
}
