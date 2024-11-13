package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.posting.Posting;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import it.unipi.dii.aide.mircv.index.utils.Statistics;
import org.javatuples.Pair;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Merge {
    private final HashMap<BlockReader, PostingIndex> readerLines = new HashMap<>();
    private final Integer blockSize;
    private final Statistics stats = new Statistics();

    public Merge(List<String> paths, Integer blockSize) throws IOException {
        for (String path : paths) {
            BlockReader reader = new BlockReader(path);
            String line = reader.readTerm();
            List<Integer> docIds = reader.readNumbers();
            List<Integer> freqs = reader.readNumbers();
            readerLines.put(reader, new PostingIndex(line, docIds, freqs));
        }
        this.blockSize = blockSize;
        stats.readSpimiFromDisk();
    }

    /**
     * Writes the merged data to the files
     *
     * @param compress A boolean indicating whether compression is used.
     * @throws IOException If there is an issue writing to the files.
     */
    public void write(Boolean compress) throws IOException {
        long lexSize = 0L;

        Lexicon lexicon = new Lexicon();

        // Open the file channel for the skipping block file
        FileChannel fcSkippingBlock = FileChannel.open(Paths.get(Configuration.SKIPPING_BLOCK_PATH),
                StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

        FrequencyFile frequencyWriter = new FrequencyFile();
        DocIdFile docIdWriter = new DocIdFile();

        ArrayList<Integer> docLens = new ArrayList<>();

        // Open the file channel for the document lengths file
        FileChannel fc = FileChannel.open(Path.of(Configuration.PATH_DOC_TERMS), StandardOpenOption.READ, StandardOpenOption.WRITE);
        for (int documentId = 0; documentId < stats.getNumDocs(); documentId++) {
            docLens.add(BinaryFile.readIntFromBuffer(fc, documentId*4L));
        }
        fc.close();

        // While there are still lines to read
        while (!readerLines.isEmpty()) {

            if (lexSize % 100000 == 0)
                System.out.println("Term number " + lexSize);

            lexSize++;

            // Find the minimum term
            String minTerm = findMinTerm(readerLines);

            // Create a new PostingIndex for the minimum term
            PostingIndex minPosting = new PostingIndex(minTerm);
            Iterator<Map.Entry<BlockReader, PostingIndex>> iterator = readerLines.entrySet().iterator();

            // Iterate through the readers
            while (iterator.hasNext()) {
                Map.Entry<BlockReader, PostingIndex> entry = iterator.next();
                PostingIndex postingList = entry.getValue();

                if (postingList.getTerm().equals(minTerm)) {
                    minPosting.appendList(postingList);

                    BlockReader reader = entry.getKey();
                    String line = reader.readTerm();

                    if (line != null) {
                        List<Integer> docIds = reader.readNumbers();
                        List<Integer> freqs = reader.readNumbers();
                        readerLines.put(reader, new PostingIndex(line, docIds, freqs));
                    } else {
                        iterator.remove();
                    }
                }
            }

            // Create structures for the new postings
            List<Integer> docIdsNew = minPosting.getDocIds();
            List<Integer> freqsNew = minPosting.getFrequencies();
            ArrayList<Integer> docLengthsNew = new ArrayList<>();

            int block_size;
            int num_blocks;

            // Calculate the block size and number of blocks
            if (minPosting.getPostings().size() <= this.blockSize) {
                block_size = minPosting.getPostings().size();
                num_blocks = 1;
            }
            else {
                block_size = (int) Math.ceil(Math.sqrt(minPosting.getPostings().size()));
                num_blocks = (int) Math.ceil((double) minPosting.getPostings().size()/block_size);
            }

            ArrayList<Integer> docIds;
            ArrayList<Integer> freqs;
            ArrayList<Integer> docLengths;

            for (Integer docId : docIdsNew) {
                docLengthsNew.add(docLens.get(docId));
            }

            // Write the lexicon entry for the term
            lexiconWrite(minPosting, fcSkippingBlock.size(), lexicon, num_blocks, docLens);

            // Write the blocks of postings to disk
            for (int currentBlock = 0; currentBlock < num_blocks; currentBlock++){
                docIds = new ArrayList<>();
                freqs = new ArrayList<>();
                docLengths = new ArrayList<>();

                for(int i = 0; i < block_size; i++) {
                    if(currentBlock * block_size + i < minPosting.getPostings().size()) {
                        docIds.add(docIdsNew.get(currentBlock * block_size + i));
                        freqs.add(freqsNew.get(currentBlock * block_size + i));
                        docLengths.add(docLengthsNew.get(currentBlock * block_size + i));
                    }
                }

                // Retrieve offset and size for docIds and frequencies
                Pair<Long, Integer> pair_docIds = docIdWriter.writeBlock(docIds, docLengths, compress);
                Pair<Long, Integer> pair_freqs = frequencyWriter.writeBlockP(freqs, compress);

                // Write the skipping block to disk
                SkippingBlock skippingBlock = new SkippingBlock();
                skippingBlock.setDocIdOffset(pair_docIds.getValue0());
                skippingBlock.setFreqOffset(pair_freqs.getValue0());
                skippingBlock.setDocIdMax(docIds.get(docIds.size() - 1));
                skippingBlock.setDocIdSize(compress ? pair_docIds.getValue1() : (docIds.size() + docLengths.size()));
                skippingBlock.setFreqSize(compress ? pair_freqs.getValue1() : freqs.size());
                skippingBlock.setNumPostingOfBlock(docIds.size());
                if(!skippingBlock.writeToDisk(fcSkippingBlock)) {
                    System.out.println("Problems with writing the block of postings to disk.");
                }

            }
        }
        fcSkippingBlock.close();

        // Compute statistics and write them to disk
        Statistics statistics = new Statistics();
        statistics.setTerms(lexSize);
        statistics.writeMergeToDisk();
    }



    /**
     * this function finds the min term between all the posting lists of the same line of the intermediateIndexes
     * @param map is the map of the posting lists, containing the reader of each intermediateIndex file associated to the last posting list read from that intermediateIndex
     * @return the min term found
     */
    protected static String findMinTerm (HashMap<BlockReader, PostingIndex> map){
        String minTerm = null;
        for (PostingIndex postingList : map.values()) {
            String term = postingList.getTerm();
            if (minTerm == null || term.compareTo(minTerm) < 0) {
                minTerm = term;
            }
        }
        return minTerm;
    }

    /**
     * Writes the lexicon entry for a given term.
     *
     * @param pi The PostingIndex containing the postings for the term.
     * @param offset The offset at which to write the lexicon entry.
     * @param lexicon The Lexicon object for writing the entry.
     * @param numBlock The number of blocks.
     * @throws IOException If an I/O error occurs.
     */
    protected void lexiconWrite(PostingIndex pi, Long offset, Lexicon lexicon, Integer numBlock, ArrayList<Integer> docLengths) throws IOException {
        float BM25UpperBound = 0.0f;
        float currentBM25;
        int  maxTf  = 0;
        int df = pi.getPostings().size();
        float idf = (float) Math.log10((double) stats.getNumDocs() /df);

        for (Posting posting : pi.getPostings()) {
            currentBM25 = calculateBM25(posting, docLengths.get(posting.getDocId()), idf);

            if (currentBM25 > BM25UpperBound){
                BM25UpperBound = currentBM25;
            }

            if (maxTf < posting.getFrequency()) {
                maxTf = posting.getFrequency();
            }
        }
        lexicon.write(pi.getTerm(), offset, df, (double) stats.getNumDocs(), maxTf, BM25UpperBound, numBlock);
    }

    /**
     * Calculates the BM25 score for a specific term in a document given the term frequency (tf) and the document ID.
     * Utilizes the BM25 configuration parameters defined in the Configuration class.
     *
     * @param posting The term frequency (tf) in the document.
     * @param idf The ID of the document.
     * @return The calculated BM25 score for the term in the specified document.
     * @throws RuntimeException If an error occurs while reading from the document terms file.
     */
    protected float calculateBM25(Posting posting, Integer docLen, float idf) {
        if (docLen == null || docLen == 0) {
            throw new RuntimeException("Document length is null or zero.");
        }
        float tf = (float) (1 + Math.log(posting.getFrequency()));
        return (float) (idf * (tf * (Configuration.BM25_K1 + 1))/(tf + Configuration.BM25_K1 * (1 - Configuration.BM25_B + Configuration.BM25_B * (docLen / stats.getAvgDocLen()))));
    }
}
