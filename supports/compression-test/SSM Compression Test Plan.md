# SSM Compression Test Plan [Not ready]
With the benefits of compression codec, SSM Compression promised less storage usage on the same data. But, we still need to evalute SSM Compression's benefits on big data workloads (better or worse performance on these workloads).

## Objectives
1. Evaluating SSM Compression's performance on micro-benchmark
    - I/O Benchmark DFSIO
    - Computation Benchmark Terasort
2. Evaluating Compression ratio on different data and codec (optional)

**Baseline: DistCp**

## Metrics and Analysis
### Environment
**4-10 Nodes are required:**

1. 4-10 nodes for HDFS Clusters (1 namenode and 3-9 datanodes)
2. SSM Server and postgresql are placed on namenode. SSM Agents are deployed on datanodes.

**Software requirement:**

1. Hadoop (Hadoop 3.2.X/ADH 3.2.4_arenadata2_b1 or higher)
2. Metastore DB (Postgresql14/ADPG 14 or higher)
3. SSM (1.6.0 or higher)

## Test Cases

### Case 1: DFSIO Sequential Read 1TB
Generate 1TB random data with DFSIO write, and compress `io_data` with SSM Compression. Then, run DFSIO read on compressed data.


**Baseline: DFSIO Sequential Read 1TB**

### Case 2: DFSIO Random Read 1TB
Generate 1TB random data with DFSIO write, and compress `io_data` with SSM Compression. Then, run DFSIO random read on compressed data.

**Baseline: DFSIO Random Read 1TB**

### Case 3: Terasrot 1TB
Generate 1TB random data with Teragen, and compress generated data with SSM Compression. Then, run Terasort on compressed data.

**Baseline: Terasrot 1TB**

## Trouble Shooting
