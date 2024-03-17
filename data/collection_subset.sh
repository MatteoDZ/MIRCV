#!/bin/bash

# Verifica il numero corretto di argomenti
if [ "$#" -lt 1 ]; then
    echo "Usage: $0 <number_of_rows> [-r]"
    exit 1
fi

# Estrai il file tsv dall'archivio
tar -xzOf collection.tar.gz collection.tsv > collection.tsv

# Seleziona il numero desiderato di righe e, se richiesto, mescola prima di selezionarle
if [ "$2" == "-r" ]; then
    shuf collection.tsv | head -n "$1" > "collection_subset_top${1}.tsv"
else
    head -n "$1" collection.tsv > "collection_subset_top${1}.tsv"
fi

# Comprimi il file tsv risultante
tar -czf "collection_subset_top${1}.tar.gz" "collection_subset_top${1}.tsv"

# Pulisci i file temporanei
rm collection.tsv "collection_subset_top${1}.tsv"

echo "Subset creation complete. Output: collection_subset_top${1}.tsv"
