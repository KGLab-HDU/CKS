# Random Walk based Key-members Finding over Large Homogeneous Graphs
1 Introduction
This is a description of the code used for the experiments described in the paper titled A Random Walk based Key-members Finding over Large Homogeneous Graphs. The code is available at https://anonymous.4open.science/r/Random-Walk-based-Key-members-Finding-over-Large-Homogeneous-Graphs-1C7C/
2 Algorithms
We evaluate 4 representative truss decomposition algorithms, and the source and publication year of their paper are given in Table 1.
Table 1: The original papers information of truss decomposition algorithms
| ALGORITHM | PAPER SOURCE | YEAR |
|---|---|---|
| Exact-TD-bottomup	  | VLDB | 2012 |
| Exact-TD-topdown | VLDB | 2012 |
| Exact-AccTD	 | VLDB | 2020 |
| Exact-TCP-Index | SIGMOD | 2014 |
We provide 5 versions of our own methods, namely RW-B, RW-AS, RW-Skew, RW-TB, RW-TB- RF.
Table 2 Our Methods
| ALGORITHM |
|-----------|
| RW-B      |
| RW-AS     |
| RW-Skew   |
| RW-TB     |
| RW-TB_RF  |
3 Requirements
The experiments have been run on a Linux server with a Intel(R) Core(TM) i9-10900X CPU at 3.70GHz, and a 128G memory. All programs are written in Java and make use of the ejml libraries.
4 Datasets
Our experiment involves four datasets popularly deployed by existing works. Each dataset represents a homogeneous graph, and each row represents information about an edge, in the form of dot-space-dot.
5 Usage
    5.1 Compare Methods
        We compare our method with different methods, that are, Exact-TD-           bottomup, Exact-TD-topdown, Exact-AccTD and Exact-TCP-Index. To run these algorithms, we need to run the following commands separately:
Exact-TD-bottomup: java -cp CCNQ.jar Baseline BottomUp < dataset_name > <query_node>
Exact-TD-topdown: java -cp CCNQ.jar Baseline TopDown < dataset_name > <query_node>
Exact-AccTD: java -cp CCNQ.jar Baseline ATD < dataset_name > <query_node>
Exact-TCP-Index: java -cp CCNQ.jar Index TCP < dataset_name > <query_node>
For example:
java -cp CCNQ.jar Baseline BottomUp facebook.txt 2000
Output:
For each method, we output the following statistical results and running time respectively. Here is the output of Exact-TD-bottomup for the above query example:
k of k-truss : 97
the size of k-truss: 139
nodes: 2560, 2561, 2307, 2564, 2308, 2309, 2059, 2573, 2064, 2578, 2323, 2324, 2069, 2326, 2073, 2586, 2074……
runtime: 4637 (ms)
5.2 Our Method
We give 5 versions of random walk-based methods, the numbers and corresponding algorithms are RW-B corresponding to 0, RW-AS corresponding to 1, RW-Skew corresponding to 2, RW-TB corresponding to 3, RW-TB- RF corresponds to 4.
RW-B: java -cp CCNQ.jar MyApproximate Mymethod  < dataset_name > 0 <query_node> <bound> <k>
RW-AS: java -cp CCNQ.jar MyApproximate Mymethod < dataset_name > 1 <query_node> <bound> <k>
RW-Skew: java -cp CCNQ.jar MyApproximate Mymethod < dataset_name > 2 <query_node> <bound> <k>
RW-TB: java -cp CCNQ.jar MyApproximate Mymethod < dataset_name > 3 <query_node> <bound> <k>
RW-TB-RF: java -cp CCNQ.jar MyApproximate Mymethod < dataset_name > 4 <query_node> <bound> <k>
For example:
java -cp CCNQ.jar MyApproximate Mymethod facebook.txt 3 2000 2 139
Output:
For each method, we output the following statistical results and running time respectively. Here is the output of RW-TB for the above query example:
Topk-nodes: 2560, 2561, 2307, 2564, 2308, 2309, 2059, 2573, 2064, 2578, 2323, 2324, 2069, 2326, 2073, 2586, 2074……
runtime: 82 (ms)
