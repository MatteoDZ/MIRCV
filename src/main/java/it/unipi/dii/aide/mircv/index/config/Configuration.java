package it.unipi.dii.aide.mircv.index.config;

import java.io.*;
import java.io.FileInputStream;
import java.util.Properties;

public class Configuration {

    public static final String PATH_DOCUMENTS = load("path_documents");
    public static final String PATH_DOCUMENTS_PREPROCESSED = load("path_documents_preprocessed");
    public static final String PATH_STOPWORDS = load("path_stopwords");
    public static final String PATH_BINARY = load("path_binary");
    public static final String DIRECTORY_TEMP_FILES = load("directory_temporary_files");
    public static final String PATH_INVERTED_INDEX = load("path_inverted_index");
    public static final String PATH_DOCIDS = load("path_docids");
    public static final String PATH_FEQUENCIES = load("path_freqs");
    public static final String PATH_LEXICON = load("path_lexicon");
    public static final int BLOCK_SIZE = 512;
    public static final boolean COMPRESSION = false;

    /**
     * Loads the value associated with the given key from the configuration properties file.
     *
     * @param  key  the key whose value is to be loaded
     * @return      the value associated with the given key
     */
    private static String load(String key) {
        Properties prop = new Properties();
        String configFilePath = "config.properties";
        if (new File(configFilePath).exists()) {
            try (FileInputStream propsInput = new FileInputStream(configFilePath)) {
                prop.load(propsInput);
            } catch (IOException e) {
                throw new RuntimeException("Error loading properties file", e);
            }
            return prop.getProperty(key);
        }
        return null;
    }

}