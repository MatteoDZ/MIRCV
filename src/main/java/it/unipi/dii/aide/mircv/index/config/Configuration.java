package it.unipi.dii.aide.mircv.index.config;

import java.io.*;
import java.io.FileInputStream;
import java.util.Objects;
import java.util.Properties;

public class Configuration {

    public static final String PATH_DOCUMENTS = load("path_documents");
    public static final String PATH_DOCUMENTS_TEST = load("path_documents_test");
    public static final String PATH_QUERIES = load("path_queries");
    public static final String PATH_STOPWORDS = load("path_stopwords");
    public static String PATH_BLOCKS = load("path_blocks");
    public static String DIRECTORY_TEMP_FILES = load("directory_temporary_files");
    public static final String DIRECTORY_TEST = load("directory_test");
    public static Boolean DYNAMIC_PRUNING = Boolean.parseBoolean(load("dynamic_pruning"));
    public static  String PATH_DOCID = load("path_docids");
    public static  String PATH_FREQ = load("path_freqs");
    public static  String PATH_LEXICON = load("path_lexicon");
    public static  String PATH_STATISTICS = load("path_statistics");
    public static  String PATH_DOC_TERMS = load(    "path_doc_terms");
    public static  String SKIPPING_BLOCK_PATH = load(    "skipping_block");
    public static final Integer LEXICON_CACHE_SIZE =  Integer.parseInt(Objects.requireNonNull(load("lexicon_cache_size")));
    public static final Integer BLOCK_SIZE = Integer.parseInt(Objects.requireNonNull(load("block_size")));
    public static final Boolean COMPRESSION = Boolean.parseBoolean(load("compression"));
    public static final Boolean STEMMING_AND_STOPWORDS = Boolean.parseBoolean(load("stemmingAndStopwords"));
    public static final Float BM25_K1 = Float.parseFloat(Objects.requireNonNull(load("BM25_K1")));
    public static final Float BM25_B = Float.parseFloat(Objects.requireNonNull(load("BM25_B")));




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
            if(!prop.containsKey(key))
                throw new RuntimeException("Key " + key + " not found in " + configFilePath + " file");
            return prop.getProperty(key);
        }
        return null;
    }

    /**
     * Set up paths for testing purposes.
     */
    public static void setUpPathTest(){
        DIRECTORY_TEMP_FILES = Configuration.DIRECTORY_TEST + "/tmp";
        PATH_BLOCKS = DIRECTORY_TEMP_FILES + "/testBlocks.bin";
        PATH_DOCID = Configuration.DIRECTORY_TEST + "/testDoc";
        PATH_FREQ = Configuration.DIRECTORY_TEST + "/testFreq";
        SKIPPING_BLOCK_PATH = Configuration.DIRECTORY_TEST + "/testSkipping";
        PATH_LEXICON = Configuration.DIRECTORY_TEST + "/testLexicon";
        PATH_STATISTICS = Configuration.DIRECTORY_TEST + "/testStatistics";
        PATH_DOC_TERMS = Configuration.DIRECTORY_TEST + "/docTerms";
    }


}