package it.unipi.dii.aide.mircv.index.compression;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * class used to implement the unary compressor used to compress the frequencies in the inverted index
 */
public class UnaryCompressor {

    public static byte[] integerArrayCompression(int[] toBeCompressed){

        int nBits = 0;

        /* computing total number of bits to be written */
        for (int k : toBeCompressed) {
            // each integer number will be compressed in a number of bits equal to its value
            nBits += k;
        }

        // computing total number of bytes needed as ceil of nBits/8
        int nBytes = (nBits/8 + (((nBits % 8) != 0) ? 1 : 0));

        //System.out.println("total bits needed: "+ nBits+"\ttotal bytes needed: "+nBytes);

        // initialization of array for the compressed bytes
        byte[] compressedArray = new byte[nBytes];

        int nextByteToWrite = 0;
        int nextBitToWrite = 0;

        // compress each integer
        for (int k : toBeCompressed) {

            // check if integer is 0
            if (k <= 0) {
                System.out.println("skipped element <=0 in the list of integers to be compressed");
                continue;
            }

            // write as many 1s as the value of the integer to be compressed -1
            for (int j = 0; j < k - 1; j++) {
                // setting to 1 the j-th bit starting from left
                compressedArray[nextByteToWrite] = (byte) (compressedArray[nextByteToWrite] | (1 << 7 - nextBitToWrite));

                // update counters for next bit to write
                nextBitToWrite++;

                // check if the current byte as been filled
                if (nextBitToWrite == 8) {
                    // new byte must be written as next byte
                    nextByteToWrite++;
                    nextBitToWrite = 0;
                }
            }

            // skip a bit since we should encode a 0 (which is the default value) as last bit
            // of the Unary encoding of the integer to be compressed
            nextBitToWrite++;

            // check if the current byte as been filled
            if (nextBitToWrite == 8) {
                // new byte must be written as next byte
                nextByteToWrite++;
                nextBitToWrite = 0;
            }
        }

        return compressedArray;
    }

    /**
     * Method to decompress an array of bytes int an array of totNums integers using Unary compression algorithm
     * @return a list containing the decompressed integers
     */
    public static List<Short> integerArrayDecompression(byte[] toBeDecompressed, Integer len) {
        List<Short> decompressedArray=new ArrayList<>();
        short onesCounter = 1;
        int lenCounter = 0;

        for (byte b : toBeDecompressed) {
            //System.out.println("UnaryDec " + String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
            for (int i = 0x80; i != 0; i >>= 1) {
                // System.out.println("UnaryDec AND " + String.format("%8s", Integer.toBinaryString(i & b & 0xFF)).replace(' ', '0'));
                // check if the i-th bit is set to 1 or 0
                if ((b & i) == 0) {
                    // System.out.println("UnaryDec IF " + onesCounter);
                    // i-th bit is set to 0
                    decompressedArray.add(onesCounter);
                    // resetting the counter of ones for next integer
                    onesCounter = 1;
                    lenCounter++;
                    // writing the decompressed number in the array of the results


                } else {
                    // i-th bit is set to 1
                    onesCounter++;
                    // increment the counter of ones

                }
            }
        }

        return decompressedArray.stream().limit(len).collect(Collectors.toList());
    }
}