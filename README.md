# Multimedia Information Retrieval and Computer Vision Project

This repository contains the source code for a project developed during the Multimedia Information Retrieval and Computer Vision course, part of the Master's Degree in Artificial Intelligence and Data Engineering at the University of Pisa, for the 2023/2024 academic year.

## Project Overview

The project involves the creation of an information retrieval system designed to efficiently process, index, and query large collections of textual data. It incorporates a powerful indexing mechanism, advanced query processing algorithms, and various optimization techniques to ensure both high performance and scalability.

## Indexing execution

To execute this project download the collection.tar.gz file found at https://microsoft.github.io/msmarco/TREC-Deep-Learning-2020 and place it into the [data](data) folder.
In order to perform the indexing operation, running the main method in [IndexingMain.java](src/main/java/it/unipi/dii/aide/mircv/index/IndexingMain.java).

## Performing queries

After the indexing phase, the system is ready to receive queries. By executing the main method of the class [QueryMain.java](src/main/java/it/unipi/dii/aide/mircv/query/QueryMain.java)  the user can interact with the system and perform queries, selecting between several options:

- Conjunctive or disjunctive query
- DAAT or MaxScore algorithm
- TFIDF or BM25 scoring function

## Evaluating the system
The user can evaluate system running the main method in [PerformanceEvaluationMain.java](src/main/java/it/unipi/dii/aide/mircv/performanceEvaluation/PerformanceEvaluationMain.java), 
the output of this operation is four files txt, which can be found inside an automatically created folder.

These files contain, for each query in the qrel file, the results retrieved by the search engine with the relative score obtained: 
that can be submitted to trec_eval to verify the performance metrics obtained by the system.

## How to run
Compile the project using mvn clean install and then execute it with java -jar .\target\MIRCV-1.0-SNAPSHOT.jar
If it is the first time running it, it will perform the indexing.