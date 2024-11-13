# Multimedia Information Retrieval and Computer Vision Project

This repository contains the source code for a project developed during the Multimedia Information Retrieval and Computer Vision course, part of the Master's Degree in Artificial Intelligence and Data Engineering at the University of Pisa, for the 2023/2024 academic year.

## Project Overview

The project involves the creation of an information retrieval system designed to efficiently process, index, and query large collections of textual data. It incorporates a powerful indexing mechanism, advanced query processing algorithms, and various optimization techniques to ensure both high performance and scalability.

## Indexing execution

To run this project download the collection.tar.gz and msmarco-test2019-queries.tsv.gz files found at https://microsoft.github.io/msmarco/TREC-Deep-Learning-2020 and place them into the [data](data) folder.
In order to perform the indexing operation, run the main method in [IndexingMain.java](src/main/java/it/unipi/dii/aide/mircv/index/IndexingMain.java), to create all files needed to execute queries.

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
Compile the project using mvn clean install and then execute it with java -jar .\target\MIRCV_project-1.0.jar.

If it is the first time running it, the user will have to perform the indexing, selecting if to enable compression and preprocessing, and provinding the path for the collection.
The default option enables both compression and preprocessing and uses the collection.tar.gz file.

To run a query the user will have to input the query terms, select whether to use dynamic pruning or not, select the type of query (conjunctive or disjunctive) and select the scoring funcion. All the selection are not case sensitive.

To run a performance evaluation the user have to input execute_performace_evaluation as a query term.

## Authors
Leonardo Bargiotti and Matteo Dal Zotto
