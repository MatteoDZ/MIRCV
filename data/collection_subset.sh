#!/bin/bash

if [ "$#" -lt 1 ]; then
    echo "Usage: $0 <number_of_rows>"
    exit 1
fi

# Extract the original collection.tar.gz
tar -xzvf collection.tar.gz

n_rows="$1"
tsv_filename="collection_subset_top${n_rows}.tsv"
targz_filename="collection_subset_top${n_rows}.tar.gz"

# Select the first n rows
head -n "$n_rows" collection.tsv > "${tsv_filename}"

# Create a new tar.gz file for the subset
tar -czvf ${targz_filename} ${tsv_filename}

# Clean up temporary files
rm collection.tsv

echo "Subset creation complete. Output: ${targz_filename}"
