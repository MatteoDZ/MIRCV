package it.unipi.dii.aide.mircv.index.compression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * class used to implement the unary compressor used to compress the frequencies in the inverted index
 */
public class UnaryCompressor {


    /**
     * Method to compress an array of integers into an array of bytes using Unary compression algorithm
     * @param toBeCompressed: array of integers to be compressed
     * @return an array containing the compressed bytes
     */
    /*public static byte[] integerArrayCompression(List<Integer> toBeCompressed) {
        int totalBits = 0;

        // computing total number of bits to be written
        for (int k : toBeCompressed) {
            totalBits += k;
        }

        int totalBytes = (totalBits + 7) / 8; // computing total number of bytes needed

        byte[] compressedArray = new byte[totalBytes]; // initialization of array for the compressed bytes

        int byteIndex = 0;
        int bitIndex = 0;

        // compress each integer
        for (int value : toBeCompressed) {
            // check if integer is 0
            if (value <= 0) {
                System.out.println("skipped element <=0 in the list of integers to be compressed");
                continue;
            }

            // write as many 1s as the value of the integer to be compressed -1
            for (int j = 0; j < value - 1; j++) {
                // setting to 1 the j-th bit starting from left
                compressedArray[byteIndex] |= (byte) (1 << (7 - bitIndex)); //imposta a 1 il bit corrispondente a bitindex nel byte byteindex

                // update counters for next bit to write
                bitIndex++;

                // check if the current byte as been filled
                if (bitIndex == 8) {
                    // new byte must be written as next byte
                    byteIndex++;
                    bitIndex = 0;
                }
            }

            // skip a bit since we should encode a 0 as last bit of the Unary encoding of the integer to be compressed
            bitIndex++;

            // check if the current byte as been filled
            if (bitIndex == 8) {
                // new byte must be written as next byte
                byteIndex++;
                bitIndex = 0;
            }
        }
        //se l'ultimo byte non è interamente occupato da informazioni, vogliamo riempire lo spazio rimanenete di 1, cosi che
        //sia possibile leggerlo correttamente senza dover sapere quanti numeri sono contenuti
        //se bitIndex è diverso da 0 vuol dire che dobbiamo scrivere 1 nello stesso byte finchè non si arriva a 7
        if(bitIndex!=0){
            for(int i=bitIndex ; i<8 ; i++ ){
                compressedArray[byteIndex] |= (byte) (1 << (7 - i));
            }
        }


        return compressedArray;
    }*/
    /*public static byte[] integerArrayCompression(List<Integer> toBeCompressed) {
        int nBits = 0;

        // omputing total number of bits to be written
        for (int i = 0; i < toBeCompressed.size(); i++) {
            // each integer number will be compressed in a number of bits equal to its value
            nBits += toBeCompressed.get(i);
        }

        // computing total number of bytes needed as ceil of nBits/8
        int nBytes = (nBits / 8 + (((nBits % 8) != 0) ? 1 : 0));

        // initialization of array for the compressed bytes
        byte[] compressedArray = new byte[nBytes];

        int nextByteToWrite = 0;
        int nextBitToWrite = 0;

        // compress each integer
        for (int i = 0; i < toBeCompressed.size(); i++) {

            // check if integer is 0
            if (toBeCompressed.get(i) <= 0) {
                System.out.println("skipped element <=0 in the list of integers to be compressed");
                continue;
            }

            // write as many 1s as the value of the integer to be compressed -1
            for (int j = 0; j < toBeCompressed.get(i) - 1; j++) {
                // setting to 1 the j-th bit starting from left
                compressedArray[nextByteToWrite] |= (1 << 7 - nextBitToWrite);

                // update counters for next bit to write
                nextBitToWrite++;

                // check if the current byte has been filled
                if (nextBitToWrite == 8) {
                    // new byte must be written as next byte
                    nextByteToWrite++;
                    nextBitToWrite = 0;
                }
            }

            // skip a bit since we should encode a 0 (which is the default value) as the last bit
            // of the Unary encoding of the integer to be compressed
            nextBitToWrite++;

            // check if the current byte has been filled
            if (nextBitToWrite == 8) {
                // new byte must be written as next byte
                nextByteToWrite++;
                nextBitToWrite = 0;
            }
        }

        return compressedArray;
    }*/
    public static byte[] integerArrayCompression(int[] toBeCompressed){

        int nBits = 0;

        /* computing total number of bits to be written */
        for(int i=0; i<toBeCompressed.length; i++){
            // each integer number will be compressed in a number of bits equal to its value
            nBits+=toBeCompressed[i];
        }

        // computing total number of bytes needed as ceil of nBits/8
        int nBytes = (nBits/8 + (((nBits % 8) != 0) ? 1 : 0));

        //System.out.println("total bits needed: "+ nBits+"\ttotal bytes needed: "+nBytes);

        // initialization of array for the compressed bytes
        byte[] compressedArray = new byte[nBytes];

        int nextByteToWrite = 0;
        int nextBitToWrite = 0;

        // compress each integer
        for(int i=0; i<toBeCompressed.length; i++){

            // check if integer is 0
            if(toBeCompressed[i]<=0){
                System.out.println("skipped element <=0 in the list of integers to be compressed");
                continue;
            }

            // write as many 1s as the value of the integer to be compressed -1
            for(int j=0; j<toBeCompressed[i]-1; j++){
                // setting to 1 the j-th bit starting from left
                compressedArray[nextByteToWrite] = (byte) (compressedArray[nextByteToWrite] | (1 << 7-nextBitToWrite));

                // update counters for next bit to write
                nextBitToWrite++;

                // check if the current byte as been filled
                if(nextBitToWrite==8){
                    // new byte must be written as next byte
                    nextByteToWrite++;
                    nextBitToWrite = 0;
                }
            }

            // skip a bit since we should encode a 0 (which is the default value) as last bit
            // of the Unary encoding of the integer to be compressed
            nextBitToWrite++;

            // check if the current byte as been filled
            if(nextBitToWrite==8){
                // new byte must be written as next byte
                nextByteToWrite++;
                nextBitToWrite = 0;
            }
        }

        return compressedArray;
    }





    /**
     * Method to decompress an array of bytes int an array of totNums integers using Unary compression algorithm
     * @return an array containing the decompressed integers
     */
    public static List<Short> integerArrayDecompression(byte[] toBeDecompressed) {
        List<Short> decompressedArray=new ArrayList<>();
        int onesCounter = 0;

        for (byte b : toBeDecompressed) {
            for (int i = 7; i >= 0; i--) {
                // check if the i-th bit is set to 1 or 0
                if (((b >> i) & 1) == 0) {
                    // i-th bit is set to 0

                    // writing the decompressed number in the array of the results
                    decompressedArray.add((short)(onesCounter+1));

                    // resetting the counter of ones for next integer
                    onesCounter = 0;

                } else {
                    // i-th bit is set to 1

                    // increment the counter of ones
                    onesCounter++;
                }
            }
        }

        return decompressedArray;
    }

    /*public static List<Short> integerArrayDecompression(byte[] toBeDecompressed){
        List<Short> decompressedArray = new ArrayList<>();
        int size = decompressedArray.size();

        int toBeReadedByte = 0;
        int toBeReadedBit = 0;
        int nextInteger = 0;
        int onesCounter = 0;

        // process each bit
        for(int i=0; i<toBeDecompressed.length*8; i++){

            // create a byte b where only the bit (i%8)-th is set
            byte b = 0b00000000;
            b |= (1 << 7-(i%8));

            // check if in the byte to be read the bit (i%8)-th is set to 1 or 0
            if((toBeDecompressed[toBeReadedByte] & b)==0){
                // i-th bit is set to 0

                // writing the decompressed number in the array of the results
                decompressedArray.add((short) (onesCounter+1));

                // the decompression of a new integer ends with this bit
                nextInteger++;

                // resetting the counter of ones for next integer
                onesCounter = 0;

            } else{
                // i-th bit is set to 1

                // increment the counter of ones
                onesCounter++;

            }

            toBeReadedBit++;

            if(toBeReadedBit==8){
                toBeReadedByte++;
                toBeReadedBit=0;
            }

        }

        return decompressedArray;
    }*/

    /*public static byte[] integerArrayCompression(List<Integer> numbers) {
        List<Integer> integers = new ArrayList<>();
        for (int num : numbers) {
            for (int i = 0; i < num; i++) {
                integers.add(1);
            }
            integers.add(0);
        }

        BitSet bits = new BitSet(integers.size());
        for (int i = 0; i < integers.size(); i++) {
            if (integers.get(i) == 1) {
                bits.set(i);
            }
        }


        return bits.toByteArray();
    }

    public static List<Short> integerArrayDecompression(byte[] bytes) {
        BitSet bits = BitSet.valueOf(bytes);
        List<Short> numbers = new ArrayList<>();

        short currentNumber = 0;
        for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i+1)) {
            currentNumber++;
            if (!bits.get(i+1)) {  // next bit is 0
                numbers.add(currentNumber);
                currentNumber = 0;
            }
        }

        return numbers;
    }*/







}