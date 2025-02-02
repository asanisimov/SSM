# Disaster Recovery Performance Test on Large Namespace
Intuitively, storing large amount of records/data in database may lead to performance degrade. In our cases, large namespace (large amount of files on HDFS) may lead to performance degrade of SSM. Because SSM fetches all namespace from namenode and stores them in database (default is Postgresql). In this test, we want to evalute this issue on SSM Data Sync.

## Objectives
1. Evaluating SSM Data Sync's performance on large namespace (from 10M~100M).
2. Find/solve bottlenecks in SSM.
3. Find/solve bottlenecks in Data Sync module.

**Baseline: DistCp**

## Metrics and Analysis
### Environment
**10 Nodes are required:**

1. 8 nodes for 2 HDFS Clusters (primary and standby clusters, each contains 1 namenode and 3 datanodes)
2. 1 node for SSM Server. SSM Agents are deployed on datanode of Primary HDFS.
3. 1 node for Metastore DB.

Note that if there are less than 10 nodes available, we can merge Postgresql and SSM Server into 1 node (total 9 nodes). Also, we can remove one HDFS cluster, and copy among different dirs (5-6 nodes).

**Hardware/Software requirement**

**Hardware requirement:**

1. Namenode: at least 20GB memory and 15GB disk space are required. Please change the default heap size in `hadoop-env.sh`. Please change the location of `name` and `checkpoint` in `hdfs-site.xml` (Each `fsimage`/checkpoint will be at least 5GB).
2. Datanode: at least 2GB memory is required by Datanode.
3. Postgresql: at least 20GB memory and 30GB disk space are required. Otherwise, Postgresql will become the main bottleneck of SSM.

Note that some of our test cases may reach peak write IOPS in standby HDFS namenode. So, we suggest to put both primary and standby namenode meta to SSD to avoid this bottleneck.

**Software requirement:**

1. Hadoop (Hadoop-3.2.X/ADH 3.2.4_arenadata2_b1 or higher)
2. Postgresql (12.X or higher/ADPG 14 or higher)
3. SSM (1.6 or higher)


### Variables and Metrics
All variables about HDFS, SSM and DistCp, bold as default.

**Main Variables:**

1. Namespace size: 0M, 10M, 25M, 50M, 75M and 100M
2. Task Concurrency: 30, **60**, 90

**SSM Variables:**

1. Rule Check period: 100ms, **200ms**, 500ms, 1s
2. Cmdlet batch size: 100, **200**, 300
3. Data Sync Check period: **100ms**, 200ms
4. Data Sync diff batch size: 100, **200**, 300
5. Database: local postgresql, **remote postgresql**

Note that lots of variables may effect SSM Data Sync's performance. I think we can fix some variables and focus on main variables.

### Metrics to Collect
**System Metrics:**

1. Total Running Time
2. Trigger Time for each incremental change
3. Average Running Time for each task
4. Scheduler Time
5. CPU usage
6. Memory usage
7. Disk I/O

## Test Cases

### Case 1: Batch ASync
Copy **a batch of files** from primary HDFS cluster to standby HDFS cluster, and check the **total running time**, **scheduler time** and **system metrics** on these cases:

1. 1_000_000 * 10KB
2. **1_000_000 * 1MB**
3. 100_000 * 10MB
4. 10_000 * 128MB
5. 5_000 * 256MB
6. 2_000 * 1GB

 Note that system cache should be cleaned before running test case.

**Baseline: DistCp**

### Case 2: Incremental ASync
####  2a: Incremental Change ASync one file
Incremental change **a single of file** in primary HDFS cluster, and check the **total running time** and **scheduler time** (trigger time) needed to async these changes to standby HDFS cluster, on these cases:

1. Create 10KB file, Append 10KB, Rename, Delete
2. Create 1MB file, Append 1MB, Rename, Delete
3. Create 100MB file, Append 100MB, Rename, Delete
4. Create 1GB file, Append 1GB, Rename, Delete

Note that the running time and scheduler time is not stable, multiple tests (at least 10) are necessary.

####  2b: Incremental Change ASync one file

Run file generation and parallel asynchronous replication from the primary HDFS cluster to the standby HDFS cluster and check **total uptime**, **scheduler time** and **system metrics** in the following cases:
1. 1_000_000 * 10KB
2. **1_000_000 * 1MB**
3. 100_000 * 10MB
4. 10_000 * 128MB
5. 5_000 * 256MB
6. 2_000 * 1GB

### Case 3: Single File ASync
Copy **a single of file** from primary HDFS cluster to standby HDFS cluster, and check the **total running time**, **scheduler time** and **system metrics** on these cases:

1. 1MB
2. **100MB**
3. 1GB
4. 2GB
5. 5GB
6. 10GB

**Baseline: DistCp**

## Trouble Shooting
