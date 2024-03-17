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
    public static void writeBlock(InvertedIndex inv, String path){ //a serve a differenziarlo dal metodo precedente. Ã¨ una beta
        try (FileChannel fc = FileChannel.open(Paths.get(path), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(),inv.calculateDimensionByte()); // errore nella scelta della dimensione . non dovrebbe avere il +10000. con +10000 da ancora errore quindi il tasso di errore Ã¨ molto alto. indagare
            List<String> sorted_keys=inv.sort();
            for(String key: sorted_keys){
                PostingIndex tpl=inv.searchTerm(key);
                String termine=tpl.getTerm();
                List<Integer> doc_ids=tpl.getDocIds();
                List<Integer> freqs=tpl.getFrequencies();

                mbb.putInt(termine.length()); //scrivo in un primo int la lunghezza del termine
                for(int i=0;i<termine.length();i++){
                    mbb.putChar(termine.charAt(i));
                }

                for (Integer doc_id : doc_ids) {
                    mbb.putInt(doc_id);
                }
                mbb.putInt(-1); //separatore tra doc id e freq
                for (Integer freq : freqs) {
                    mbb.putInt(freq);
                }
                mbb.putInt(-1);

                /*writeIntToBuffer(fc, termine.length());
                writeStringToBuffer(fc, termine);
                writeIntListToBuffer(fc, tpl.getDocIds());
                writeIntToBuffer(fc, -1);
                writeIntListToBuffer(fc, tpl.getFrequencies());
                writeIntToBuffer(fc,-1);*/

            }

        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing to the binary file.");
        }
    }



    public static void writeShortToBuffer(FileChannel fc, short value) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), 2);
        mbb.putShort(value);
    }

    public static short readShortFromBuffer(FileChannel fc, Long offset) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, offset, 2);
        return mbb.getShort();
    }

    public static void writeIntToBuffer(FileChannel fc, int value) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), 4);
        mbb.putInt(value);
    }

    public static int readIntFromBuffer(FileChannel fc, Long offset) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, offset, 4);
        return mbb.getInt();
    }

    public static void writeLongToBuffer(FileChannel fc, long value) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), 8);
        mbb.putLong(value);
    }
    public static long readLongFromBuffer(FileChannel fc, Long offset) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, offset, 8);
        return mbb.getLong();
    }

    public static void writeArrayByteToBuffer(FileChannel fc, byte[] values) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), values.length);
        mbb.put(values);
    }

    public static byte[] readArrayByteFromBuffer(FileChannel fc, Long offsetStart, Long offsetEnd) throws IOException {
        byte[] bytesToRead = new byte[(int) (offsetEnd - offsetStart)];
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, offsetStart, offsetEnd);
        mbb.get(bytesToRead);
        return bytesToRead;
    }

    public static void writeShortListToBuffer(FileChannel fc, List<Integer> values) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), values.size() * 2L);
        for (int value : values) {
            mbb.putShort((short)value);
        }
    }

    public static List<Short> readShortListFromBuffer(FileChannel fc, Long offsetStart, Long offsetEnd) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, offsetStart, offsetEnd);
        List<Short> freqs = new ArrayList<>();
        for (long i = offsetStart; i < offsetEnd; i += 2) {
            freqs.add(mbb.getShort());
        }
        return freqs;
    }


    // Helper method: Write a list of integers to the buffer
    public static void writeIntListToBuffer(FileChannel fc, List<Integer> values) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), values.size() * 4L);
        for (int value : values) {
            mbb.putInt(value);
        }
    }

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

    // Helper method: Write a list of long values to the buffer
    public static void writeLongListToBuffer(FileChannel fc, List<Long> values) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), values.size() * 8L);
        for (Long value : values) {
            mbb.putLong(value);
        }
    }

    public static void writeStringToBuffer(FileChannel fc, String term) throws IOException {
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), term.length() * 2L);
        for(int i=0;i<term.length();i++){
            mbb.putChar(term.charAt(i));
        }
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
