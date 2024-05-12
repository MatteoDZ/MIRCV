package it.unipi.dii.aide.mircv.index.compression;

import java.util.*;
import java.nio.ByteBuffer;

import static java.lang.Math.log;

/**
 * class that implements the variable byte compressor used to compress docids in the inverted index file
 */
public class VariableByteCompressor {

    /**
     * Encodes a given number into a byte array.
     *
     * @param n the number to be encoded
     * @return the encoded byte array
     */
    public static byte[] encodeNumber(int n) {
        // If the number is 0, return a byte array with a single element 0
        if (n == 0) {
            return new byte[]{0};
        }

        // Calculate the length of the byte array needed to store the encoded number
        int i = (int) (log(n) / log(128)) + 1;

        // Create a byte array with the calculated length
        byte[] rv = new byte[i];

        // Index of the last element in the byte array
        int j = i - 1;

        // Encode the number by dividing it by 128 and storing the remainder in the byte array
        do {
            byte currentByte = (byte) (n % 128);
            n /= 128;
            if(j>0){
                // Add 128 to the last element of the byte array to indicate the end of the encoded number
                currentByte |= (byte) 128;
            }
            rv[j] = currentByte;
            j--;
        } while (j >= 0);



        // Return the encoded byte array
        return rv;
    }

    /**
     * Compresses a list of integers into a byte array.
     *
     * @param  toBeCompressed  the list of integers to be compressed
     * @return                 the compressed byte array
     */
    public static byte[] encode(List<Integer> toBeCompressed) {
        // Create a byte buffer with the appropriate capacity
        ByteBuffer buf = ByteBuffer.allocate(toBeCompressed.size() * (Integer.SIZE / Byte.SIZE));

        // Iterate over each integer in the list
        for (Integer number : toBeCompressed) {
            // Encode the number and put it into the byte buffer
            buf.put(encodeNumber(number));
        }

        // Flip the byte buffer to prepare for reading
        buf.flip();

        // Create a byte array with the same length as the byte buffer
        byte[] rv = new byte[buf.limit()];

        // Get the bytes from the byte buffer and store them in the byte array
        buf.get(rv);

        // Return the compressed byte array
        return rv;
    }

    public static List<Integer> decode(byte[] compressedData){
        List<Integer> decompressedArray = new ArrayList<>();
        int i = 0; //index of the current byte
        int number = 0; //number to decompress

        for (byte b: compressedData) {
            if (b >= 0) {
                if(i > 0){ //if the current byte is positive, and it is not the first byte, we have to add the number to the array, because it is the first byte of the next number
                    decompressedArray.add(number); //add the number to the array
                    number = 0; //reset the number
                }
                number = 128 * number + b; //multiply the number by 128 and add the current byte

            } else {
                number = 128 * number + (b + 128); //multiply the number by 128 and add the current byte, adding 128 because the current byte is negative
            }
            i++;
        }
        decompressedArray.add(number); //the last values is not been added in the cycle for, so we add it here

        return decompressedArray;
    }





}
