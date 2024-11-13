package it.unipi.dii.aide.mircv.index;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.merge.Merge;
import it.unipi.dii.aide.mircv.index.spimi.Spimi;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;

import java.io.*;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

public class IndexingMain {

    /**
     * Reads lines from a TAR.GZ file, processes each line, and writes the results to a new file.
     *
     * @throws IOException If an I/O error occurs while reading or writing files.
     */
    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        Configuration.COMPRESSION = true;
        Configuration.STEMMING_AND_STOPWORDS = true;
        Configuration.PATH_DOCUMENTS = "data/collection.tar.gz";

        String compresison, stopwords, path;

        System.out.print("Perform the indexing with compression? [Y/n]: ");
        compresison = scanner.nextLine();
        compresison = compresison.toUpperCase(Locale.ROOT);
        Configuration.COMPRESSION = !compresison.equals("N");

        System.out.println("Perform the indexing with stemming and stopwords removal? [Y/n]: ");
        stopwords = scanner.nextLine();
        stopwords = stopwords.toUpperCase(Locale.ROOT);
        Configuration.STEMMING_AND_STOPWORDS = !stopwords.equals("N");

        System.out.print("Enter the path of the documents, to use the default one leave empty: ");
        path = scanner.nextLine();
        if (!path.trim().isEmpty()) {
            Configuration.PATH_DOCUMENTS = path;
        }

        if (!FileUtils.searchIfExists(Configuration.DIRECTORY_TEMP_FILES)) {
            FileUtils.createDirectory(Configuration.DIRECTORY_TEMP_FILES);
        }

        if (FileUtils.getNumberFiles(Configuration.DIRECTORY_TEMP_FILES) <= 0) {
            long startTime_spimi = System.currentTimeMillis();
            System.out.println("Spimi is starting....");
            Spimi.spimi(Configuration.PATH_DOCUMENTS);
            long endTime_spimi = System.currentTimeMillis();
            System.out.println(printCompletionTime("Spimi", startTime_spimi, endTime_spimi));
        }
        System.out.println("Number of blocks created in spimi part: " + FileUtils.getNumberFiles(Configuration.DIRECTORY_TEMP_FILES));

        if(!FileUtils.filesExist(Configuration.SKIPPING_BLOCK_PATH, Configuration.PATH_DOCID, Configuration.PATH_FREQ, Configuration.PATH_LEXICON)){
            FileUtils.deleteFiles(Configuration.SKIPPING_BLOCK_PATH, Configuration.PATH_DOCID, Configuration.PATH_FREQ, Configuration.PATH_LEXICON);
            long startTime_merge = System.currentTimeMillis();
            System.out.println("Merge is starting....");
            Merge merge = new Merge(Objects.requireNonNull(FileUtils.getFilesOfDirectory(Configuration.DIRECTORY_TEMP_FILES)), Configuration.BLOCK_SIZE);
            merge.write(Configuration.COMPRESSION);
            long endTime_merge = System.currentTimeMillis();
            System.out.println(printCompletionTime("Merge", startTime_merge, endTime_merge));
        }

    }

    public static String printCompletionTime(String phase, Long startTime, Long endTime){
        long executionTimeInSeconds = (endTime - startTime) / 1000;
        long minutes = executionTimeInSeconds / 60;
        long remainingSeconds = executionTimeInSeconds % 60;
        return phase + " is completed in : " + minutes + " minutes and  " + remainingSeconds + " seconds";
    }
}