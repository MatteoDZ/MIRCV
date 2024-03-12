package it.unipi.dii.aide.mircv.index.spimi;

import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
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
                Statistics statistics = new Statistics();
                String line;
                int blockNumber = 0, i = 0;
                float doclen = 0;
                InvertedIndex inv = new InvertedIndex();
                // List<Integer> doc_lens = new ArrayList<>();
                Preprocess.readStopwords();
                while ((line = br.readLine()) != null) { //loop giusto. sotto c'è quello provvisorio per vedere se va il tutto
                    String[] parts = line.split("\t");
                    List<String> term = Preprocess.processText(parts[1]);
                    term.removeAll(List.of("", " "));

                    doclen += term.size();
                    // doc_lens.add(term.size());

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
                statistics.setNumdocs(i);
                statistics.setAvg_doc_length(doclen/i);
                // statistics.setDocs_length(doc_lens);
                //statistics.writeToDisk();
            }
        }
    }
}
