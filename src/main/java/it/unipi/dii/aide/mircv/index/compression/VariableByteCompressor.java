package it.unipi.dii.aide.mircv.index.compression;

import java.util.*;
import java.nio.ByteBuffer;

import static java.lang.Math.log;

/**
 * class that implements the variable byte compressor used to compress docids in the inverted index file
 */
public class VariableByteCompressor {




    public static List<Integer> integerArrayDecompressionGiusta(byte[] toBeDecompressed){

        List<Integer> decompressedArray=new ArrayList<Integer>();
        // integer that I'm processing
        int decompressedNumber = 0;



        for(byte elem: toBeDecompressed){
            if((elem & 0xff) < 128) { //elem è un byte. elem può andare da 0 a 255. se elem è minore di 128 vuol dire che ha 0 davanti. IN QUALCHE MODO FA TRUE SE è MAGGIORE
                // not the termination byte, shift the actual number and insert the new byte
                //System.out.println("ciao" + (elem & 0xff));
                decompressedNumber = 128 * decompressedNumber + elem; //in automatico somma il valore letto in elem
            }else{
                // termination byte (INIZIA PER 0), remove the 1 at the MSB and then append the byte to the number
                decompressedNumber =  decompressedNumber+ ((elem - 128) & 0xff);
                decompressedArray.add(decompressedNumber);

                decompressedNumber=0;
                // save the number in the output array




                //reset the variable for the next number to decompress

            }
        }
        // il primo if riguarda se abbiamo compresso un numero con lunghezza maggiore di 1 byte la seconda parte cerca se l'ultimo numero è uguale a 0.
//        if (decompressedArray.length > 1 && decompressedArray[decompressedArray.length - 1] == 0) {
//            decompressedArray = IntStream.concat(IntStream.of(decompressedArray[decompressedArray.length - 1]),
//                    Arrays.stream(decompressedArray, 0, decompressedArray.length - 1)).toArray();
//        }

        return decompressedArray;
    }


    //facciamo come sulle slide.
    public List<Byte> compressor(List<Integer> daComprimere){
        List<Byte> compressi=new ArrayList<>();
        for(int numero: daComprimere){

        }

        return compressi;
    }

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

    /**
     * Decompresses an array of bytes into a list of integers.
     *
     * @param toBeDecompressed the array of bytes to be decompressed
     * @return the list of decompressed integers
     */
    public static List<Integer> decode(byte[] toBeDecompressed) {
        List<Integer> numbers = new ArrayList<>();
        int n = 0;

        List<Byte> lst = new ArrayList<>();
        for (byte b : toBeDecompressed) {
            lst.add(b);
        }

        Iterator<Byte> it = lst.iterator();

        // TODO: Pensare a tutte le casisitiche per la decompressione degli array di byte

        while(it.hasNext()) {
            Byte elem = it.next();
            System.out.println(elem);
        }


        // Iterate over each byte in the array
        for (byte b : toBeDecompressed) {
            System.out.println("STO DECOMPRIMENDO " + b);
            // If the byte is 0 and the current number is 0, add 0 to the numbers list
            if (b == 0 && n == 0) {
                numbers.add(0);
            }
            // If the byte is less than 128, update the current number by multiplying it by 128 and adding the byte value
            else if ((b & 0xff) < 128) {
                n = 128 * n + b;
                System.out.println("PRINTANDO N " + n);
            }
            // If the byte is greater than or equal to 128, calculate the decompressed number and add it to the numbers list
            else {
                int num = (128 * n + ((b - 128) & 0xff));
                numbers.add(num);
                n = 0;
            }
        }

        return numbers;
    }


    public static List<Integer> decompressArray(byte[] compressedData){

        ArrayList<Integer> decompressedArray = new ArrayList<>();

        int i = 0; //index of the current byte
        int number = 0; //number to decompress

        while (i<compressedData.length) {
            if (compressedData[i] >= 0) {
                if(i>0){ //if the current byte is positive, and it is not the first byte, we have to add the number to the array, because it is the first byte of the next number
                    decompressedArray.add(number); //add the number to the array
                    number = 0; //reset the number
                }
                number = 128 * number + compressedData[i]; //multiply the number by 128 and add the current byte

            } else {
                number = 128 * number + (compressedData[i] + 128); //multiply the number by 128 and add the current byte, adding 128 because the current byte is negative
            }
            i++;
        }
        decompressedArray.add(number); //the last values is not been added in the cycle for, so we add it here

        return decompressedArray;
    }



}
