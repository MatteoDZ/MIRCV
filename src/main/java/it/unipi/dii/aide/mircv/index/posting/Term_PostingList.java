package it.unipi.dii.aide.mircv.index.posting;

import java.util.ArrayList;
import java.util.List;

public class Term_PostingList{
    private final String term;
    private final List<Integer> doc_ids;
    private final List<Integer> frequencies;

    public Term_PostingList(String term){
        this.term=term;
        this.doc_ids=new ArrayList<>();
        this.frequencies=new ArrayList<>();
    }

    public Term_PostingList(String term, List<Integer> docIds, List<Integer> frequencies){
        this.term=term;
        this.doc_ids=new ArrayList<>(docIds);
        this.frequencies=new ArrayList<>(frequencies);
    }

    public Term_PostingList(String term, int doc_id, int freq){
        this.term=term;
        this.doc_ids=new ArrayList<>();
        this.frequencies=new ArrayList<>();
        this.doc_ids.add(doc_id);
        this.frequencies.add(freq);
    }

    public String getTerm() {
        return this.term;
    }

    public List<Integer> getDocIds() {
        return this.doc_ids;
    }

    public List<Integer> getFrequencies() {
        return this.frequencies;
    }

    /**
     * Add a posting to the data structure.
     *
     * @param  doc_id   the document ID to add
     * @param  freq     the frequency of the posting
     */
    public void addPosting(int doc_id,int freq){
        this.doc_ids.add(doc_id);
        this.frequencies.add(freq);
    }

    /**
     * Adds a posting to the collection.
     *
     * @param  doc_id  the document ID to add
     */
    public void addPosting(int doc_id){
        int lastIndex= doc_ids.size()-1;
        if(doc_ids.get(lastIndex)!=doc_id){ //add a doc_id and the corresponding freq is initialized at 1
            doc_ids.add(doc_id);
            frequencies.add(1);
        }else {
            frequencies.set(lastIndex,frequencies.get(lastIndex)+1); //add 1 to the last frequency value
        }
    }

    /**
     * Adds a posting to the list of postings.
     *
     * @param  doc_id  the list of document IDs to add
     * @param  freq    the list of frequencies to add
     */
    public void addPosting(List<Integer> doc_id, List<Integer> freq){
        this.doc_ids.addAll(doc_id);
        this.frequencies.addAll(freq);
    }

    /**
     * Retrieves the document ID and frequency at the specified index.
     *
     * @param  index  the index of the document ID and frequency to retrieve
     * @return        a list containing the document ID and frequency at the specified index
     */
    public List<Integer> getDocIdsFrequency(int index){
        return List.of(this.doc_ids.get(index), this.frequencies.get(index));
    }

    /**
     * Returns a string representation of the object.
     *
     * @return  a string representation of the object
     */
    public String toString() {return term + " " + doc_ids.toString() + " " + frequencies.toString();}


}
