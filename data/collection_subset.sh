#!/bin/bash

if [ "$#" -lt 1 ]; then
    echo "Usage: $0 <number_of_rows> [-r]"
    echo "  -r: Optional flag to shuffle the lines before selecting the first n rows"
    exit 1
fi


# Extract the original collection.tar.gz
tar -xzvf collection.tar.gz

n_rows="$1"
tsv_filename="collection_subset_top${n_rows}.tsv"
targz_filename="collection_subset_top${n_rows}.tar.gz"

# Check if the -r flag is provided
if [ "$2" == "-r" ]; then
    # Shuffle the lines and select the first n rows
    shuf collection.tsv | head -n "$n_rows" > ${tsv_filename} 
else
    # Select the first n rows without shuffling
    head -n "$n_rows" collection.tsv > "${tsv_filename}"
fi

# Create a new tar.gz file for the subset
tar -czvf ${targz_filename} ${tsv_filename}

# Clean up temporary files
rm collection.tsv ${tsv_filename}

echo "Subset creation complete. Output: collection_subset.tar.gz"
