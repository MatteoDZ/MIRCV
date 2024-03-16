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
import java.util.List;
import java.util.Objects;

public class Spimi {

    public static void spimi(String pathCollection) throws IOException {
        try (TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(Objects.requireNonNull(pathCollection))))) {
            tarInput.getNextTarEntry();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(tarInput))) {
                Statistics statistics = new Statistics(Configuration.PATH_STATISTICS);
                String line;
                int blockNumber = 0, numDocs = 0, total_length = 0;
                InvertedIndex inv = new InvertedIndex();
                Preprocess.readStopwords();
                while ((line = br.readLine()) != null) { //loop giusto. sotto c'è quello provvisorio per vedere se va il tutto
                    String[] parts = line.split("\t");
                    List<String> term = Preprocess.processText(parts[1]);
                    term.removeAll(List.of("", " "));
                    total_length += term.size();
                    if (!parts[1].isEmpty() || !term.isEmpty()) { //è sufficiente che una delle due non sia empty per fare inserire il tutto
                        inv.add(term, Integer.parseInt(parts[0]));
                        if (numDocs % 1000000 == 0) {
                            System.out.println("Now at document: " + numDocs + " and block: " + blockNumber);
                        }
                        numDocs++;
                        if (Runtime.getRuntime().freeMemory() < (Runtime.getRuntime().totalMemory() * 20 / 100)) { //if giusto che tiene conto della memoria occupata
                            String pathBlockN = FileUtils.createPathFileBlockN(blockNumber);
                            BinaryFile.writeBlock(inv, pathBlockN);
                            blockNumber++;
                            inv.clean();
                            System.gc();
                        }
                    }
                }
                statistics.setTotalLenDoc(total_length);
                statistics.setNumDocs(numDocs);
                statistics.setAvgDocLen((double)total_length / numDocs);
                statistics.writeToDisk();
            }
        }
    }
}
