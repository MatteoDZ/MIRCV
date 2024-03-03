package it.unipi.dii.aide.mircv.index.utils;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * utility class used to create, clear and remove files
 */
public class FileUtils {


    /**
     * Creates the file if it does not already exist.
     * @param path The path of the file to be created.
     */
    public static void createIfNotExists(String path) throws IOException {
        if(searchIfExists(path)){
            Files.createFile(Paths.get(path));
        }
    }

    /**
     * Checks if a file or directory exists at the given path.
     *
     * @param  path  the path to check
     * @return       true if the file or directory exists, false otherwise
     */
    public static boolean searchIfExists(String path) {
        return new File(path).exists();
    }

    /**
     * removes a file
     *
     * @param path the path of the file to be removed
     */
    public static void removeFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            if (!file.delete()) {
                throw new RuntimeException("Failed to delete file: " + path);
            }
        }
    }

    /** Creates a directory at the specified path.
     *
     * @param path The path where the directory should be created.
     */
    public static void createDirectory(String path) {
        try {
            Path dirPath = Paths.get(path);
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory: " + path);
        }
    }

    /**
     * Deletes a directory and all its contents given a directory path.
     *
     * @param path The path of the directory to be deleted.
     */
    public static void deleteDirectory(String path) {
        if (searchIfExists(path)) {
            try (Stream<Path> paths = Files.walk(Paths.get(path))) {
                paths.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(file -> {
                            if (!file.delete()) {
                                throw new RuntimeException("Failed to delete file: " + file.getPath());
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete directory: " + path);
            }
        }
    }

    /**
     * Returns the number of files in the specified directory.
     *
     * @param  path  the path of the directory
     * @return       the number of files in the directory
     */
    public static int getNumberFiles(String path) {
        if(searchIfExists(path)) {
            try(Stream<Path> files = Files.list(Paths.get(path))) {
                return (int) files.count();
            } catch(IOException e) {
                throw new RuntimeException("Failed to get number of files in directory: " + path);
            }
        }
        return 0;
    }

    /**
     * Retrieves a list of file paths within a specified directory.
     *
     * @param  path  the path of the directory
     * @return       a list of file paths within the directory
     */
    public static List<String> getFilesOfDirectory(String path) {
        if(searchIfExists(path)) {
            try(Stream<Path> files = Files.list(Paths.get(path))) {
                return files.map(Path::toString).toList();
            } catch(IOException e) {
                throw new RuntimeException("Failed to get number of files in directory: " + path);
            }
        }
        return null;
    }


    /**
     * Generates a path for a new block using the block number.
     *
     * @param  blockNumber  the number of the block
     * @return              a path for a new block
     */
    public static String createPathFileBlockN(int blockNumber){
        assert Configuration.PATH_BINARY != null;
        return Configuration.PATH_BINARY.split("\\.")[0] + "_" + blockNumber + "." + Configuration.PATH_BINARY.split("\\.")[1];
    }

    /**
     * Writes the given Term_PostingList to the specified file path.
     *
     * @param  path  the file path to write the Term_PostingList to
     * @param  term  the Term_PostingList to write
     */
    public static void writeTerm(String path, PostingIndex term) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path), StandardOpenOption.APPEND)) {
            writer.write(term.toString());
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads a file and returns a list of Term_PostingList objects.
     *
     * @param  path  the path of the file to be read
     * @return       a list of Term_PostingList objects
     */
    public static List<PostingIndex> read(String path) {
        List<PostingIndex> list = new ArrayList<>();
        try(BufferedReader reader = Files.newBufferedReader(Paths.get(path))) {
            for (String line; (line = reader.readLine()) != null; ) {
                String term = line.split("\t")[0];
                List<Integer> docIds = Arrays.stream(line.split(" ")[1].replace("[", "").replace("]", "").split(",")).map(Integer::parseInt).toList();
                List<Integer> frequencies = Arrays.stream(line.split(" ")[2].replace("[", "").replace("]", "").split(",")).map(Integer::parseInt).toList();
                list.add(new PostingIndex(term, docIds, frequencies));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;
    }


}
