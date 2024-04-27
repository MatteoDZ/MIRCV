package it.unipi.dii.aide.mircv.index.binary;

import it.unipi.dii.aide.mircv.index.posting.InvertedIndex;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class BinaryFile {


    /**
     * Writes an inverted index block to a binary file.
     *
     * @param inv  the inverted index object containing the block to be written
     * @param path the file path where the block will be written
     * @throws RuntimeException if an error occurs while writing to the binary file
     */
    public static void writeBlock(InvertedIndex inv, String path){
        try (FileChannel fc = FileChannel.open(Paths.get(path), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(),inv.calculateDimensionByte());
            List<String> sorted_keys=inv.sort();
            for(String key: sorted_keys){
                PostingIndex tpl=inv.searchTerm(key);
                String term=tpl.getTerm();
                mbb.putInt(term.length());
                term.chars().forEach(character->mbb.putChar((char) character));
                tpl.getDocIds().forEach(mbb::putInt);
                mbb.putInt(-1);
                tpl.getFrequencies().forEach(mbb::putInt);
                mbb.putInt(-1);
            }
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing to the" + path + "  file.");
        }
    }

    /**
     * A description of the entire Java function.
     *
     * @param  fc       description of parameter
     * @param  value    description of parameter
     * @throws IOException description of exception
     */
    public static void writeIntToBuffer(FileChannel fc, int value) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), 4);
        mbb.putInt(value);
    }

    /**
     * Reads an integer from the given file channel at the specified offset.
     *
     * @param  fc     the file channel to read from
     * @param  offset the offset at which to start reading
     * @return        the integer read from the file channel
     */
    public static int readIntFromBuffer(FileChannel fc, Long offset) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, offset, 4);
        return mbb.getInt();
    }

    /**
     * Write a long value to the provided FileChannel at the current position.
     *
     * @param  fc    the FileChannel where the long value will be written
     * @param  value the long value to be written
     * @throws IOException if an I/O error occurs
     */
    public static void writeLongToBuffer(FileChannel fc, long value) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), 8);
        mbb.putLong(value);
    }

    /**
     * Writes a byte array to a file channel using a mapped byte buffer.
     *
     * @param  fc      the file channel to write the byte array to
     * @param  values  the byte array to write
     * @throws IOException if an I/O error occurs
     */
    public static void writeArrayByteToBuffer(FileChannel fc, byte[] values) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), values.length);
        mbb.put(values);
    }


    /**
     * Writes a list of integers to a file channel as a short list.
     *
     * @param  fc      the file channel to write to
     * @param  values  the list of integers to be written
     * @throws IOException if an I/O error occurs
     */
    public static void writeShortListToBuffer(FileChannel fc, List<Integer> values) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), values.size() * 2L);
        values.stream().map(b -> (short) b.intValue()).toList().forEach(mbb::putShort);
    }


    /**
     * Writes a list of integers to a file channel buffer.
     *
     * @param  fc      the file channel to write to
     * @param  values  the list of integers to write
     * @throws IOException if an I/O error occurs
     */
    public static void writeIntListToBuffer(FileChannel fc, List<Integer> values) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), values.size() * 4L);
        values.forEach(mbb::putInt);
    }




}
