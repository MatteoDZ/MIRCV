package it.unipi.dii.aide.mircv.index.merge;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class LexiconData {
    private String term;
    private long offsetInvertedIndex= 0; //old offset_doc_id
    private int upperTF = 0;
    private int tf = 0; //not written in the file
    private float idf = 0;
    private float upperTFIDF = 0;
    private int df = 0;
    private float upperBM25 = 0;
    private long offset_frequency = 0;
    private long offset_skip_pointer = 0;
    private int numBlocks = 1;
    private static final long BYTES_ATTRIBUTES = 4 * 6 + 8 * 3; //4bytes * (n_int + n_float) + 8bytes * (n_long)
    protected static final long ENTRY_SIZE = BYTES_ATTRIBUTES + Lexicon.MAX_LEN_OF_TERM;

    public LexiconData(){

    }

    public String getTerm() {
        return term;
    }

    public long getOffsetInvertedIndex() {
        return offsetInvertedIndex;
    }

    public int getUpperTF() {
        return upperTF;
    }

    public int getTf() {
        return tf;
    }

    public float getIdf() {
        return idf;
    }

    public float getUpperTFIDF() {
        return upperTFIDF;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public void setOffsetInvertedIndex(long offsetInvertedIndex) {
        this.offsetInvertedIndex = offsetInvertedIndex;
    }

    public void setUpperTF(int upperTF) {
        this.upperTF = upperTF;
    }

    public void setTf(int tf) {
        this.tf = tf;
    }

    public void setIdf(float idf) {
        this.idf = idf;
    }

    public void setUpperTFIDF(float upperTFIDF) {
        this.upperTFIDF = upperTFIDF;
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
            df = (mappedByteBuffer.getInt());
            idf = (mappedByteBuffer.getFloat());
            upperTF = (mappedByteBuffer.getInt());
            upperTFIDF = (mappedByteBuffer.getFloat());
            upperBM25 = (mappedByteBuffer.getFloat());
            offsetInvertedIndex = (mappedByteBuffer.getLong());
            offset_frequency = (mappedByteBuffer.getLong());
            numBlocks = (mappedByteBuffer.getInt());
            offset_skip_pointer = (mappedByteBuffer.getLong());
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
            mappedByteBuffer.putInt(df);
            mappedByteBuffer.putFloat(idf);
            mappedByteBuffer.putInt(upperTF);
            mappedByteBuffer.putFloat(upperTFIDF);
            mappedByteBuffer.putFloat(upperBM25);
            mappedByteBuffer.putLong(offsetInvertedIndex);
            mappedByteBuffer.putLong(offset_frequency);
            mappedByteBuffer.putInt(numBlocks);
            mappedByteBuffer.putLong(offset_skip_pointer);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing a LexiconData in to the lexicon file");
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
                "Offset Doc ID: " + offsetInvertedIndex + " " +
                "Upper TF: " + upperTF + " " +
                "DF: " + df + " " +
                "IDF: " + idf + " " +
                "Upper TF-IDF: " + upperTFIDF + " " +
                "Upper BM25: " + upperBM25 + " " +
                "Offset Frequency: " + offset_frequency + " " +
                "Offset Skip Pointer: " + offset_skip_pointer + " " +
                "Num Blocks: " + numBlocks + " ";
    }


}
