package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class LexiconData {
    private String term;
    private float idf = 0;
    private float upperTFIDF = 0;
    private float upperBM25 = 0;
    private int numBlocks = 1;
    private Long offset_skip_pointer = 0L;
    private static final long BYTES_ATTRIBUTES = 4 * 4 + 8; //4bytes * (n_int + n_float) + 8bytes * (n_long)
    protected static final long ENTRY_SIZE = BYTES_ATTRIBUTES + Lexicon.MAX_LEN_OF_TERM;

    private static FileChannel fileChannel = null;


    static{
        try {
            File file = new File(Configuration.SKIPPING_BLOCK_PATH);
            if(file.exists()) {
                fileChannel = FileChannel.open(Paths.get(Configuration.SKIPPING_BLOCK_PATH), StandardOpenOption.READ);
            }
        }catch (IOException e){
            System.out.println("problems with opening file with lexicon entry with block file");
        }
    }


    public LexiconData(){
    }

    public String getTerm() {
        return term;
    }

    public float getIdf() {
        return idf;
    }

    public void setUpperTFIDF(float upperTFIDF) {
        this.upperTFIDF = upperTFIDF;
    }

    public float getUpperTFIDF() {
        return upperTFIDF;
    }

    public void setTerm(String term) {
        this.term = term;
    }


    public void setIdf(float idf) {
        this.idf = idf;
    }

    public float getUpperBM25() {
        return upperBM25;
    }

    public void setUpperBM25(float upperBM25) {
        this.upperBM25 = upperBM25;
    }

    public long getOffset_skip_pointer() {
        return offset_skip_pointer;
    }

    public void setOffset_skip_pointer(long skipPointer) {
        this.offset_skip_pointer = skipPointer;
    }

    public int getNumBlocks() {
        return numBlocks;
    }

    public void setNumBlocks(int numBlocks) {
        this.numBlocks = numBlocks;
    }


    /**
     * Reads the LexiconEntry from the specified offset in the given FileChannel.
     *
     * @param offset      The offset in the FileChannel.
     * @param fileChannel The FileChannel from which to read.
     */
    public void readEntryFromDisk(long offset, FileChannel fileChannel) {
        try {
            MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, offset, ENTRY_SIZE);
            byte[] termBytes = new byte[Lexicon.MAX_LEN_OF_TERM];
            mappedByteBuffer.get(termBytes);
            term = Lexicon.removePadding(new String(termBytes, StandardCharsets.UTF_8));
            idf = (mappedByteBuffer.getFloat());
            upperTFIDF = (mappedByteBuffer.getFloat());
            upperBM25 = (mappedByteBuffer.getFloat());
            offset_skip_pointer = (mappedByteBuffer.getLong());
            numBlocks = (mappedByteBuffer.getInt());
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while reading a LexiconData from the lexicon file.");
        }

    }

    /**
     * Writes the LexiconEntry to the specified offset in the given FileChannel.
     *
     * @param fileChannel The FileChannel to which to write.
     */
    public void writeEntryToDisk(FileChannel fileChannel) {
        try {
            MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, fileChannel.size(), ENTRY_SIZE);
            mappedByteBuffer.put(Lexicon.padStringToLength(term).getBytes(StandardCharsets.UTF_8));
            mappedByteBuffer.putFloat(idf);
            mappedByteBuffer.putFloat(upperTFIDF);
            mappedByteBuffer.putFloat(upperBM25);
            mappedByteBuffer.putLong(offset_skip_pointer);
            mappedByteBuffer.putInt(numBlocks);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing a LexiconData in to the lexicon file");
        }
    }

    /**
     * Reads the skipping blocks associated with the LexiconEntry from the block file.
     *
     * @return The list of SkippingBlocks.
     */
    public ArrayList<SkippingBlock> readBlocks() {

        try {
            ArrayList<SkippingBlock> blocks = new ArrayList<>();
            MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, offset_skip_pointer, (long) numBlocks * SkippingBlock.size_of_element);
            if (mappedByteBuffer == null) {
                return null;
            }
            for (int i = 0; i < numBlocks; i++) {
                SkippingBlock skippingBlock = new SkippingBlock();
                skippingBlock.setDoc_id_offset(mappedByteBuffer.getLong());
                skippingBlock.setDoc_id_size(mappedByteBuffer.getInt());
                skippingBlock.setFreq_offset(mappedByteBuffer.getLong());
                skippingBlock.setFreq_size(mappedByteBuffer.getInt());
                skippingBlock.setDoc_id_max(mappedByteBuffer.getInt());
                skippingBlock.setNum_posting_of_block(mappedByteBuffer.getInt());
                blocks.add(skippingBlock);
            }
            return blocks;
        } catch (IOException e) {
            System.out.println("Problems with reading blocks in the lexicon entry");
            return null;
        }
    }



    /**
     * Returns a string representation of the LexiconEntry.
     *
     * @return The string representation.
     */
    @Override
    public String toString() {
        return "Term: " + term + " " +
                // "Upper TF: " + upperTF + " " +
                // "DF: " + df + " " +
                "IDF: " + idf + " " +
                "Upper TF-IDF: " + upperTFIDF + " " +
                "Upper BM25: " + upperBM25 + " " +
                "Offset Skip Pointer: " + offset_skip_pointer + " " +
                "Num Blocks: " + numBlocks + " ";
    }


}
