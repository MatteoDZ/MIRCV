package it.unipi.dii.aide.mircv.index;

import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.merge.InvertedIndexWriter;
import it.unipi.dii.aide.mircv.index.merge.Merger;
import it.unipi.dii.aide.mircv.index.posting.InvertedIndex;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import it.unipi.dii.aide.mircv.index.preprocess.Preprocess;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.util.List;
import java.util.Objects;

public class Main {

    /**
     * Reads lines from a TAR.GZ file, processes each line, and writes the results to a new file.
     *
     * @throws IOException If an I/O error occurs while reading or writing files.
     */
    public static void main(String[] args) throws IOException {
        //FileUtils.deleteDirectory(Configuration.DIRECTORY_TEMP_FILES);
        //FileUtils.createDirectory(Configuration.DIRECTORY_TEMP_FILES);

        if (!FileUtils.searchIfExists(Configuration.DIRECTORY_TEMP_FILES)) {
            FileUtils.createDirectory(Configuration.DIRECTORY_TEMP_FILES);
        }

        System.out.println("Number of files in temporary directory: " + FileUtils.getNumberFiles(Configuration.DIRECTORY_TEMP_FILES));

        if (FileUtils.getNumberFiles(Configuration.DIRECTORY_TEMP_FILES) <= 0) {

            InvertedIndex inv = new InvertedIndex();
            long startTime_spimi = System.currentTimeMillis();
            System.out.println("Spimi is starting....");

            try (TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(Objects.requireNonNull(Configuration.PATH_DOCUMENTS))))) {
                tarInput.getNextTarEntry();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(tarInput))) {
                    String line;
                    int blockNumber = 0, i = 0;
                    Preprocess.readStopwords();
                    while ((line = br.readLine()) != null) { //loop giusto. sotto c'è quello provvisorio per vedere se va il tutto
                        String[] parts = line.split("\t");
                        List<String> term = Preprocess.processText(parts[1]);
                        term.removeAll(List.of("", " "));
                        if (!parts[1].isEmpty() || !term.isEmpty()) { //è sufficiente che una delle due non sia empty per fare inserire il tutto
                            inv.add(term, Integer.parseInt(parts[0]));
                            if (i % 1000000 == 0) {
                                System.out.println("Now at document: " + i + " and block: " + blockNumber);
                            }
                            i++;
                            if (Runtime.getRuntime().freeMemory() < (Runtime.getRuntime().totalMemory() * 20 / 100)) { //if giusto che tiene conto della memoria occupata
                                String pathBlockN = FileUtils.createPathFileBlockN(blockNumber);
                                BinaryFile.writeBlock(inv, pathBlockN);
                                blockNumber++;
                                inv.clean();
                                System.gc();
                            }
                        }
                    }
                }
            }

            long endTime_spimi = System.currentTimeMillis();
            System.out.println(printTime("Spimi", startTime_spimi, endTime_spimi));
        }
        /*if(!FileUtils.searchIfExists(Configuration.PATH_INVERTED_INDEX)){
            long startTime_merge = System.currentTimeMillis();
            System.out.println("Merge is starting....");
            FileUtils.removeFile(Configuration.PATH_INVERTED_INDEX);
            Merger merge = new Merger(Objects.requireNonNull(FileUtils.getFilesOfDirectory(Configuration.DIRECTORY_TEMP_FILES)));
            merge.writeAll(Configuration.PATH_INVERTED_INDEX);
            long endTime_merge = System.currentTimeMillis();
            System.out.println(printTime("Merge", startTime_merge, endTime_merge));
        }*/

        if(!FileUtils.searchIfExists(Configuration.PATH_INVERTED_INDEX_OFFSETS)){
            long startTime_merge = System.currentTimeMillis();
            System.out.println("New Merge is starting....");
            Merger merge = new Merger(Objects.requireNonNull(FileUtils.getFilesOfDirectory(Configuration.DIRECTORY_TEMP_FILES)));
            merge.writeAllNew(Configuration.PATH_INVERTED_INDEX_OFFSETS, false);
            long endTime_merge = System.currentTimeMillis();
            System.out.println(printTime("Merge", startTime_merge, endTime_merge));
        }

        InvertedIndexWriter invRead = new InvertedIndexWriter(Configuration.PATH_INVERTED_INDEX_OFFSETS, Configuration.PATH_DOCIDS, Configuration.PATH_FEQUENCIES, Configuration.BLOCK_SIZE);
        invRead.read();

        /*List<PostingIndex> lst = BinaryFile.readBlock(Configuration.PATH_INVERTED_INDEX);
        System.out.println(lst.get(10).toString());*/


        /*
        //soluzione non usata (per ora) per controllare la heap occupata

        long usedMemory=0;
        long maxMemory=0;
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean(); //con queste due righe determiniamo quanta heap è occupata
        MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage(); //
        maxMemory=heapMemoryUsage.getMax();
        usedMemory= heapMemoryUsage.getUsed();
         */
    }

    public static String printTime(String phase, long startTime, long endTime){
        long executionTimeInSeconds = (endTime - startTime) / 1000;
        long minutes = executionTimeInSeconds / 60;
        long remainingSeconds = executionTimeInSeconds % 60;
        return phase + " is completed in : " + minutes + " minutes and  " + remainingSeconds + " seconds";
    }
}