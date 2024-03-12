package it.unipi.dii.aide.mircv.index.merge;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Merge {
    private List<BlockReader> readers = new ArrayList<>();
    private final String pathLexicon, pathDocIds, pathFreqs;
    private final Integer blockSize;

    //altri cosi per fare il dizionario
    public Merge(List<String> paths, String pathLexicon, String pathDocIds, String pathFreqs, Integer blockSize) {
        paths.forEach(path -> {
            try {
                readers.add(new BlockReader(path));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        this.pathFreqs = pathFreqs;
        this.pathDocIds = pathDocIds;
        this.pathLexicon = pathLexicon;
        this.blockSize = blockSize;
    }

    public void write(String path, boolean compress) throws IOException {
        for (BlockReader reader : readers) {
            try {
                reader.readTerm();
            } catch (IOException e) {
                System.out.println("Block does not exist: " + path);
            }

        }

        List<BlockReader> daRimuovere=new ArrayList<>();
        List<Integer> docs=new ArrayList<>();
        List<Integer> freqs=new ArrayList<>();
        String termToWrite;

        InvertedIndexWriter inv = new InvertedIndexWriter(path, pathDocIds, pathFreqs, blockSize);
        Lexicon lexicon = new Lexicon(pathLexicon);
        while(!readers.isEmpty()){

            termToWrite = getFirst();
            for (BlockReader reader : readers) {

                if (termToWrite.equals(reader.lastWord)) {
                    List<Integer> docIdsToAdd = reader.readNumbers();
                    List<Integer> freqsToAdd = reader.readNumbers();
                    if(termToWrite.equals("a")){
                        System.out.println(docs);
                        System.out.println(freqs);

                        System.out.println(docIdsToAdd);
                        System.out.println(freqsToAdd);
                    }
                    docs.addAll(docIdsToAdd);
                    freqs.addAll(freqsToAdd);
                    if (reader.readTerm().equals("block terminated")) { //con questo si attiva automaticamente il readterm
                        daRimuovere.add(reader); //sennò il metodo è comunque stato chiamato quindi i blocchi si aggiornano
                    }
                }
            }
            // inserire qui le cose da fare
            long offsetTerm = inv.write(docs, freqs, compress);

            //inserire qui le cose da fare
            lexicon.writeFixed(termToWrite, offsetTerm, docs, freqs);

            //chiamata a oggetto che scrive le freqs e restituisce gli offsets di ciascun blocco. (i metodi di scrittura cerchiamo di tenerli su binaryFile)
            // tiene aperto e gestisce il descrittore del file freqs

            //chiamata a oggetto che scrive numero di blocchi,upper bounds, puntatori ai blocchi di doc id, puntatori ai blocchi  di freqs
            // ottenuti dalla chiamata precedente e ai blocchi di doc ids contenuti nel file. restituisce solo il puntatore al primo
            //punto di scrittura

            //chiamata a oggetto che scrive sul lexicon termine, offset nell'inv restituito precedentemente, collection freqs and so
            // on altre statistiche descrittive

            docs.clear();
            freqs.clear();
            readers.removeAll(daRimuovere);
            daRimuovere.clear();
        }

    }



    String getFirst(){
        String parolaMinima=readers.get(0).lastWord;
        for(BlockReader b: readers){
            if(b.lastWord.compareTo(parolaMinima)<0){
                parolaMinima=b.lastWord;
            }
        }
        return parolaMinima;
    }

}