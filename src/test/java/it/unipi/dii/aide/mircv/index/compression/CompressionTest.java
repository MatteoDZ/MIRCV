package it.unipi.dii.aide.mircv.index.compression;

import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CompressionTest {


    @Test
    public void testConvertToUnary() {
        int[] input = {1, 5, 10, 15, 20};
        byte[] expected = {(byte) 0b01111011, (byte) 0b11111110, (byte) 0b11111111,
                (byte) 0b11111101, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11000000};
        byte[] result = UnaryCompressor.integerArrayCompression(input);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testConvertFromUnary() {
        byte[] input = {(byte) 0b01111011, (byte) 0b11111110, (byte) 0b11111111,
                (byte) 0b11111101, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111110};
        assertEquals(List.of((short)1, (short)5, (short)10, (short)15, (short)25)
                , UnaryCompressor.integerArrayDecompression(input, 5));
    }


    @Test
    void VariableByteEncodeTest() {

        int[] input = {1, 127, 128, 255, 256, 16383, 16384};
        byte[][] expected = {
                {(byte) 0b00000001},
                {(byte) 0b01111111},
                {(byte) 0b00000001, (byte) 0b10000000},
                {(byte) 0b00000001, (byte) 0b11111111},
                {(byte) 0b00000010, (byte) 0b10000000},
                {(byte) 0b01111111, (byte) 0b11111111},
                {(byte) 0b00000001, (byte) 0b10000000, (byte) 0b10000000}
        };
        byte[] expectedA = {
                (byte) 0b00000001,
                (byte) 0b01111111,
                (byte) 0b00000001, (byte) 0b10000000,
                (byte) 0b00000001, (byte) 0b11111111,
                (byte) 0b00000010, (byte) 0b10000000,
                (byte) 0b01111111, (byte) 0b11111111,
                (byte) 0b00000001, (byte) 0b10000000, (byte) 0b10000000
        };

        assertArrayEquals(expectedA,
                VariableByteCompressor.encode(List.of(1, 127, 128, 255, 256, 16383, 16384)));

        for (int i = 0; i < input.length; i++) {
            byte[] result = VariableByteCompressor.encode(Collections.singletonList(input[i]));
            assertArrayEquals(expected[i], result);
        }

    }

    @Test
    void VariableByteDecodeTest() {
        byte[] inputA = {
                (byte) 0b00000001,
                (byte) 0b01111111,
                (byte) 0b00000001, (byte) 0b10000000,
                (byte) 0b00000001, (byte) 0b11111111,
                (byte) 0b00000010, (byte) 0b10000000,
                (byte) 0b01111111, (byte) 0b11111111,
                (byte) 0b00000001, (byte) 0b10000000, (byte) 0b10000000
        };
        assertEquals(List.of(1, 127, 128, 255, 256, 16383, 16384),
                VariableByteCompressor.decode(inputA));
    }




    @Test
    //test the variable byte compressor
    public void testVariableByteCompressor(){
        //test compressInt
        byte[] expected = {5, 2, -72, 4, -125, -48}; //expected outputs
        assertArrayEquals(expected, VariableByteCompressor.encode(List.of(5, 312, 66000)));

        //test compressInt
        byte[] expected2 = {5, 2, -72, 4, -125, -48, 1, -127, 2, -128, -126}; //expected outputs
        assertArrayEquals(expected2, VariableByteCompressor.encode(List.of(5, 312, 66000, 129, 32770)));

        //test compressArrayInt
        assertEquals(List.of(5, 312, 66000, 129, 32770),
                VariableByteCompressor.decode(new byte[]{5, 2, -72, 4, -125, -48, 1, -127, 2, -128, -126}));
    }


    @Test
    public void testCoherence(){
        assertEquals(List.of(5, 312, 66000),
                VariableByteCompressor.decode(VariableByteCompressor.encode(List.of(5, 312, 66000))));
        assertEquals(List.of(1, 127, 128, 255, 256, 16383, 16384),
                VariableByteCompressor.decode(VariableByteCompressor.encode(List.of(1, 127, 128, 255, 256, 16383, 16384))));

        assertEquals(List.of((short)5, (short)312, (short)32000, (short)129, (short)32770),
                UnaryCompressor.integerArrayDecompression(UnaryCompressor.integerArrayCompression(new int[]{5, 312, 32000, 129, 32770}), 5));

    }

    @Test
    public void atomicTest(){
        List<Integer> list = List.of(0, 1, 2, 3, 5, 6, 8, 708, 729, 730, 733, 1705, 1709, 1710, 1711, 2577,
                7249, 8177, 8630, 9070, 9190, 9246, 9247, 9255, 9628, 9739, 9773, 9780, 9990, 10056, 10057, 10058, 10059,
                10060, 10061, 10063, 10064, 12640, 12643, 13842, 13892, 13893, 13894, 13897, 13898, 14030, 14031, 14032,
                14038, 17227, 18218, 20066, 25991, 27018, 27298, 27299, 27302, 27303, 27304, 29016, 33491, 33496, 33912,
                33914, 34868, 34872, 35223, 36334, 37737, 38820, 39066, 39502, 42512, 42515, 45918, 47356, 47357, 47358,
                47359, 47361, 47363, 47365, 49260, 49264, 49265, 49267, 49269, 50873, 55118, 55121, 55122, 55125, 57519,
                58556, 60926, 60927, 60928, 60930, 60931, 60932, 60933, 60934, 61816, 61817, 61818, 61820, 61821, 61822,
                61823, 61824, 61825, 63934, 63937, 63938, 63940, 63941, 63942, 65246, 69676, 70213, 71384, 71485, 71486,
                71487, 71488, 71489, 71490, 71491, 71492, 71493, 71494, 72502, 75369, 79276, 79804, 79806, 79807, 79810,
                81365, 81367, 86761, 88625, 88626, 88630, 92230, 92553, 92557, 92559, 92560, 94673, 94752);

        assertEquals(list, VariableByteCompressor.decode(VariableByteCompressor.encode(list)));

    }


}
