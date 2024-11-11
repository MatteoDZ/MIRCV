package it.unipi.dii.aide.mircv.index.spimi;

import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.posting.InvertedIndex;
import it.unipi.dii.aide.mircv.index.preprocess.Preprocess;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import it.unipi.dii.aide.mircv.index.utils.Statistics;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

public class Spimi {

    public static void spimi(String pathCollection) throws IOException {
        final FileChannel fc;
        try {
            fc = FileChannel.open(Paths.get(Configuration.PATH_DOC_TERMS),
                    StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing to the " + Configuration.PATH_DOC_TERMS + " file.");
        }
        try (TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(Objects.requireNonNull(pathCollection))))) {
            tarInput.getNextTarEntry();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(tarInput))) {
                Statistics statistics = new Statistics();
                String line;
                int blockNumber = 0, numDocs = 0, total_length = 0;
                InvertedIndex inv = new InvertedIndex();
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\t");
                    List<String> term = Preprocess.processText(parts[1], Configuration.STEMMING_AND_STOPWORDS);
                    term.removeAll(List.of("", " "));
                    total_length += term.size();
                    BinaryFile.writeIntToBuffer(fc, term.size());
                    if (!parts[1].trim().isEmpty() || !term.isEmpty()) {
                        inv.add(term, Integer.parseInt(parts[0]));
                        if (numDocs % 1000000 == 0) {
                            System.out.println("Now at document: " + numDocs + " and block: " + blockNumber);
                        }
                        numDocs++;
                        if (Runtime.getRuntime().freeMemory() < (Runtime.getRuntime().totalMemory() * 20 / 100)) {
                            String pathBlockN = FileUtils.createPathFileBlockN(Objects.requireNonNull(Configuration.PATH_BLOCKS),blockNumber);
                            BinaryFile.writeBlock(inv, pathBlockN);
                            blockNumber++;
                            inv.clean();
                            System.gc();
                        }
                    }
                }
                if(!inv.getInvertedIndex().isEmpty()){
                    String pathBlockN = FileUtils.createPathFileBlockN(Objects.requireNonNull(Configuration.PATH_BLOCKS),blockNumber);
                    BinaryFile.writeBlock(inv, pathBlockN);
                    inv.clean();
                    System.gc();
                }
                statistics.setTotalLenDoc(total_length);
                statistics.setNumDocs(numDocs);
                statistics.setAvgDocLen((double)total_length / numDocs);
                statistics.writeSpimiToDisk();
            }
        }
    }
}
