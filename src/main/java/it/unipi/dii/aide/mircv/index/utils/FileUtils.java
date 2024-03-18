package it.unipi.dii.aide.mircv.index.utils;

import it.unipi.dii.aide.mircv.index.config.Configuration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        if(!searchIfExists(path)){
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
                        .forEach(file -> removeFile(file.getPath()));
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
    public static String createPathFileBlockN(String path, int blockNumber){
        return path.split("\\.")[0] + "_" + blockNumber + "." + path.split("\\.")[1];
    }


    /**
     * Generates a path for the test file
     *
     * @return              a path for a new block
     */
    public static String createPathTestFile(String path){
        if(path.split("//*").length == 2)
            return path.split("//*")[0] + "/test/" + path.split("//*")[1];
        else
            return path.split("//*")[0] + "/test/" + path.split("//*")[1] + "/" + path.split("//*")[2];
    }



}
