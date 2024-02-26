package it.unipi.dii.aide.mircv.index.posting;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class InvertedIndex {
    private final HashMap<String, Term_PostingList> Block;
    public HashMap<String, Term_PostingList> getInvertedIndexBlock() {
        return Block;
    }
    public InvertedIndex(){
        Block=new HashMap<>();
    }

    /**
     * Adds the given words to the block's posting list for the specified document ID.
     *
     * @param  words     the list of words to add
     * @param  doc_id    the ID of the document
     */
    public void add(List<String> words, int doc_id){
        for(String term : words) {
            Term_PostingList tpl = searchTerm(term);
            if (tpl == null) {
                Block.put(term, new Term_PostingList(term, doc_id, 1));
            } else {
                tpl.addPosting(doc_id);
            }
        }
    }

    /**
     * Sorts the list of strings and returns the sorted list.
     *
     * @return         the sorted list of strings
     */
    public List<String> sort(){
        return Block.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Cleans the block by clearing all its contents.
     */
    public void clean(){
        Block.clear();
    }

    /**
     * Searches for a given term in the block.
     *
     * @param  term  the term to search for
     * @return       the posting list associated with the term
     */
    public Term_PostingList searchTerm(String term){
        return Block.get(term);
    }

    /**
     * Checks if the given term is present in the Block.
     *
     * @param  term  the term to check
     * @return       true if the term is present, false otherwise
     */
    public boolean isPresent(String term){
        return Block.containsKey(term);
    }

    /**
     * Calculates the size of the inverted index block.
     *
     * @param bytesPerChar   The number of bytes per character.
     * @param bytesPerInt    The number of bytes per integer.
     * @param separator     The number of separators.
     * @return The size of the inverted index block in bytes.
     */
    public int calculateDimensionByte(int bytesPerChar, int bytesPerInt, int separator) {
        // Calculate the number of terms in the inverted index block
        int numberOfTerms = Block.size();

        // Calculate the total number of integers in the inverted index block
        int numberOfIntegers = Block.values().stream()
                .mapToInt(termPostingList -> termPostingList.getDocIds().size() * 2)
                .sum();

        // Calculate the total number of characters in the inverted index block
        int numberOfChars = Block.values().stream()
                .mapToInt(termPostingList -> termPostingList.getTerm().length())
                .sum();

        // Calculate the size of the inverted index block in bytes
        return numberOfIntegers * bytesPerInt + numberOfChars * bytesPerChar + separator * bytesPerInt * numberOfTerms + numberOfTerms * bytesPerInt;
    }

}
