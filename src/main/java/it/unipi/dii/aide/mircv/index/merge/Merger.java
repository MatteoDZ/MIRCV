package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.apache.commons.compress.compressors.lz77support.LZ77Compressor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Merger {
    //classe merger per mergiare correttamente i blocchi. gli oggetti di base sono i reader. ce ne vuole uno per blocco
    //i reader hanno un metodo leggi termine e uno leggi pl
    // poi dal metodo centrale della classe si procede coi reader. è possibile selezionare vari metodi di scrittura su file(compresso o no)
    List<BlockReader> readers = new ArrayList<>();
    int numReaders; //probabilmente inutile
    List<Integer> offsets;

    //altri cosi per fare il dizionario
    public Merger(List<String> paths) throws FileNotFoundException {
        for(String path :paths){ //per ogni path fornito creo un lettore associato e lo aggiungo alla lista di lettori
            readers.add(new BlockReader(path));
        }

    }

    public void writeAll(String path) throws IOException {

        for (BlockReader reader : readers) {
            try {
                reader.readTerm();
            } catch (IOException e) {
                System.out.println("blocco assente");
            }

        }

        List<BlockReader> daRimuovere=new ArrayList<>();
        List<Integer> docs=new ArrayList<>();
        List<Integer> freqs=new ArrayList<>();
        String termToWrite;
        while(!readers.isEmpty()){

            termToWrite = getFirst();
            for (BlockReader reader : readers) {

                if (termToWrite.equals(reader.lastWord)) { //quando indiceFirst è uguale al valore di i, i doc e le freq vengono aggiunte. per cui è gestito anche il caso in cui ci sia un solo blocco con la parola che va inserita e non doppioni
                    docs.addAll(reader.readNumbers()); //dovrebbe unire le liste
                    freqs.addAll(reader.readNumbers());
                    if (reader.readTerm().equals("blocco terminato")) { //con questo si attiva automaticamente il readterm
                        daRimuovere.add(reader); //sennò il metodo è comunque stato chiamato quindi i blocchi si aggiornano
                    }
                }
            }
            // inserire qui le cose da fare

            BinaryFile.appendToBinaryFile(path, termToWrite, docs, freqs);

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