package it.unipi.dii.aide.mircv.index.binary;

import it.unipi.dii.aide.mircv.index.posting.InvertedIndex;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
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
     * Writes a short value to the provided FileChannel using a MappedByteBuffer.
     *
     * @param  fc     the FileChannel to write to
     * @param  value  the short value to be written
     */
        public static void writeShortToBuffer(FileChannel fc, short value) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), 2);
        mbb.putShort(value);
    }

    /**
     * Reads a short value from the given FileChannel at the specified offset.
     *
     * @param  fc     the FileChannel to read from
     * @param  offset the offset in the FileChannel where to start reading
     * @return        the short value read from the FileChannel
     */
    public static short readShortFromBuffer(FileChannel fc, Long offset) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, offset, 2);
        return mbb.getShort();
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
     * Reads a long from the given FileChannel at the specified offset.
     *
     * @param  fc     the FileChannel to read from
     * @param  offset the offset at which to start reading
     * @return        the long read from the FileChannel
     */
    public static long readLongFromBuffer(FileChannel fc, Long offset) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, offset, 8);
        return mbb.getLong();
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
     * A description of the entire Java function.
     *
     * @param  fc       description of parameter
     * @param  offsetStart       description of parameter
     * @param  offsetEnd       description of parameter
     * @return         	description of return value
     */
    public static byte[] readArrayByteFromBuffer(FileChannel fc, Long offsetStart, Long offsetEnd) throws IOException {
        byte[] bytesToRead = new byte[(int) (offsetEnd - offsetStart)];
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, offsetStart, offsetEnd);
        mbb.get(bytesToRead);
        return bytesToRead;
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
     * This function reads a list of Short values from a given FileChannel within the specified offsets.
     *
     * @param  fc         the FileChannel to read from
     * @param  offsetStart the starting offset within the FileChannel
     * @param  offsetEnd   the ending offset within the FileChannel
     * @return            a List of Short values read from the FileChannel
     */
    public static List<Short> readShortListFromBuffer(FileChannel fc, Long offsetStart, Long offsetEnd) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, offsetStart, offsetEnd);
        List<Short> freqs = new ArrayList<>();
        for (long i = offsetStart; i < offsetEnd; i += 2) {
            freqs.add(mbb.getShort());
        }
        return freqs;
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

    /**
     * Reads a list of integers from a specified range in a file channel.
     *
     * @param  fc         the FileChannel to read from
     * @param  offsetStart the starting offset in the file
     * @param  offsetEnd   the ending offset in the file
     * @return            a List of integers read from the specified range
     */
    public static List<Integer> readIntListFromBuffer(FileChannel fc, Long offsetStart, Long offsetEnd) throws IOException {
        List<Integer> listIntToRead = new ArrayList<>();
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, offsetStart, offsetEnd);
        for (long i = offsetStart; i < offsetEnd; i += 4) {
            listIntToRead.add(mbb.getInt());
        }
        return listIntToRead;
    }

    public static List<Integer> readIntListFromBuffer(FileChannel fc) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
        List<Integer> listInt = new ArrayList<>();
        int valueInt;
        while ((valueInt = mbb.getInt()) != -1) {
            listInt.add(valueInt);
        }
        return listInt;
    }

    /**
     * Writes a list of long values to the specified file channel.
     *
     * @param  fc      the file channel to write the values to
     * @param  values  the list of long values to write
     * @throws IOException if an I/O error occurs
     */
    public static void writeLongListToBuffer(FileChannel fc, List<Long> values) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), values.size() * 8L);
        values.forEach(mbb::putLong);
    }

    public static void writeStringToBuffer(FileChannel fc, String term) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), term.length() * 2L);
        term.chars().forEach(character->mbb.putChar((char) character));
    }

    public static String readStringFromBuffer(FileChannel fc, Integer lenTerm, Long offset) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, offset, offset + lenTerm * 2L);
        StringBuilder term = new StringBuilder();
        for(int i=0;i<term.length();i++){
           term.append(mbb.getChar());
        }
        return term.toString();
    }



}
