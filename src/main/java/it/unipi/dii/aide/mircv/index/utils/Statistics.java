package it.unipi.dii.aide.mircv.index.utils;

import it.unipi.dii.aide.mircv.index.binary.BinaryFile;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
public class Statistics {

    private int numDocs;
    private long totalLenDoc;
    private double avgDocLen;
    private long terms = 0; //written in merge
    protected long ENTRY_SIZE = 4 + 8 + 8;
    private  MappedByteBuffer mbb;
    private final FileChannel fc;


    public Statistics(String path) {
        try {
            fc = FileChannel.open(Paths.get(path),
                    StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (
                IOException e) {
            throw new RuntimeException("An error occurred while writing to the " + path + " file.");
        }
    }

    /**
     * Getter for the number of documents in the collection.
     *
     * @return The number of documents.
     */
    public  int getNumDocs() {
        return numDocs;
    }


    /**
     * Setter for the number of documents in the collection.
     *
     * @param numDoc The number of documents to set.
     */
    public  void setNumDocs(int numDoc) {
        numDocs = numDoc;
    }

    /**
     * Getter for the average document length.
     *
     * @return The average document length.
     */
    public  double getAvgDocLen() {
        return avgDocLen;
    }

    /**
     * Setter for the average document length.
     *
     * @param avgDocLen1 The average document length to set.
     */
    public  void setAvgDocLen(double avgDocLen1) {
        avgDocLen = avgDocLen1;
    }

    /**
     * Getter for the total number of terms in the collection.
     *
     * @return The total number of terms.
     */
    public  long getTerms() {
        return terms;
    }

    /**
     * Setter for the total number of terms in the collection.
     *
     * @param terms1 The total number of terms to set.
     */
    public  void setTerms(long terms1) {
        terms = terms1;
    }

    /**
     * Getter for the total document length in the collection.
     *
     * @return The total document length.
     */
    public  long getTotalLenDoc() {
        return totalLenDoc;
    }

    /**
     * Setter for the total document length in the collection.
     *
     * @param totalLenDoc The total document length to set.
     */
    public  void setTotalLenDoc(long totalLenDoc) {
        this.totalLenDoc = totalLenDoc;
    }

    /**
     * Writes collection statistics to disk.
     *
     */
    public void writeSpimiToDisk() throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_WRITE, 0, ENTRY_SIZE);
        mbb.putInt(this.numDocs);
        mbb.putDouble(this.avgDocLen);
        mbb.putLong(this.totalLenDoc);
    }

    /**
     * Writes collection statistics to disk.
     *
     */
    public void writeMergeToDisk() throws IOException {
        BinaryFile.writeLongToBuffer(fc, this.terms);
    }


    /**
     * Reads collection statistics from disk.
     *
     */
    public void readFromDisk() throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
        this.setNumDocs(mbb.getInt());
        this.setAvgDocLen(mbb.getDouble());
        this.setTotalLenDoc(mbb.getLong());
        this.setTerms(mbb.getLong());
    }

    public void readSPIMI() throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
        this.setNumDocs(mbb.getInt());
        this.setAvgDocLen(mbb.getDouble());
    }

    /**
     * A description of the entire Java function.
     *
     * @return         	description of return value
     */
    public String toString() {
        return "Number of documents: " + this.getNumDocs() + " " +
                "Average document length: " + this.getAvgDocLen() + " " +
                "Total number of terms: " + this.getTerms() + " " +
                "Total document length: " + this.getTotalLenDoc();
    }

}
