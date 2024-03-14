package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.posting.InvertedIndex;
import it.unipi.dii.aide.mircv.index.posting.Posting;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Merge {
    private final List<BlockReader> readers = new ArrayList<>();
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
        HashMap<Integer, Integer> docs_freqs = new HashMap<>();
        InvertedIndex ii = new InvertedIndex();
        String termToWrite;

        InvertedIndexFile inv = new InvertedIndexFile(path, pathDocIds, pathFreqs, blockSize);
        Lexicon lexicon = new Lexicon(pathLexicon);

        while(!readers.isEmpty()){

            termToWrite = getFirst();
            for (BlockReader reader : readers) {

                if (termToWrite.equals(reader.lastWord)) {
                    List<Integer> docIdsToAdd = reader.readNumbers();
                    List<Integer> freqsToAdd = reader.readNumbers();


                    assert(docIdsToAdd.size() == freqsToAdd.size());
                    for(int i = 0; i < docIdsToAdd.size(); i++){
                        if(docs_freqs.containsKey(docIdsToAdd.get(i))){
                            int frequencies = docs_freqs.get(docIdsToAdd.get(i));
                            frequencies += freqsToAdd.get(i);
                            docs_freqs.put(docIdsToAdd.get(i), frequencies);
                        }
                        else{
                            docs_freqs.put(docIdsToAdd.get(i), freqsToAdd.get(i));
                        }
                    }

                    if (reader.readTerm().equals("block terminated")) { //con questo si attiva automaticamente il readterm
                        daRimuovere.add(reader); //sennò il metodo è comunque stato chiamato quindi i blocchi si aggiornano
                    }
                }
            }
            // inserire qui le cose da fare
            List<Integer> docs = docs_freqs.keySet().stream().toList();
            List<Integer> freqs = docs_freqs.values().stream().toList();


            for(int i = 0; i < docs.size(); i++){
                Posting p = new Posting(docs.get(i), freqs.get(i));
                ii.addNew(termToWrite, p);
            }

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

            docs_freqs.clear();

            readers.removeAll(daRimuovere);
            daRimuovere.clear();
        }

        PostingIndex pd = ii.searchTerm("hello");
        System.out.println("PORCODDIO E LA MADONNA PUTTANA SURGELATA" + pd.getPostings().toString());
        long off = lexicon.findTerm("hello");
        System.out.println("PORCODDIO E LA MADONNA PUTTANA SURGELATA" + inv.getDocIds(off, Configuration.COMPRESSION).toString());

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