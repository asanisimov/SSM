# Disaster Recovery Performance Test

## Pre-requests
### Python Environment
Python 3 (3.10 or higher) with installed requirements libs from `support/requirements.txt`.
```
python3 --version
python3 -m venv venv
source venv/bin/activate
pip install -r ../requirements.txt
```

### HDFS and SSM Environment
Make sure SSM and two HDFS clusters are correctly installed. Before executing test scripts, please set SSM's IP address in SSM_BASE_URL environment and hdfs dir for test in HDFS_TEST_DIR.
```
export SSM_BASE_URL=http://{SSM_Server}:7045
export HDFS_TEST_DIR=/ssmtest/
```

`{SSM_Server}` is the IP address of SSM. Default - `http://localhost:7045`

## Init/Rest Test Environment
Assume you have two HDFS clusters, SSM is connected with one of them. We named the cluster connected with SSM as primary HDFS cluster, the other HDFS cluster as remote HDFS.

1. Create test directories and files in HDFS clusters
  [Optional] Create test directories in both clusters:
```
hdfs dfs -mkdir /1MB
hdfs dfs -mkdir /10MB
hdfs dfs -mkdir /100MB
```

[Optional] Create test files in test directories in primary cluster:
```
python reset_env.py ResetEnv.test_create_10000_1MB
python reset_env.py ResetEnv.test_create_10000_10MB
python reset_env.py ResetEnv.test_create_1000_100MB
```

These commands will create 10K * 1MB in `1MB`, 10K * 10MB in `10MB` and 1K * 100MB in `100MB`.

2. [Optional] Remove existing files in test directories of remote cluster:
```
HDFS dfs -rm /1MB/*
HDFS dfs -rm /10MB/*
HDFS dfs -rm /100MB/*
```

## Test SSM Disaster Recovery module
### Test Disaster Recovery through GUI
1. Submit a sync rule in SSM notebook
```
file : path matches "/{src}/*" | sync -dest hdfs://{remote HDFS}/{dest}
```

- {src} is the src directory of primary cluster, such as `/10MB/`.
- {remote HDFS} is the IP address and port of remote HDFS cluster, such as `192.168.0.1:9000`.
- {dest} is the destination directory of remote cluster, such as `/10MB/`.

2. Start/stop sync rule in Data Sync page
  After start sync rule, you can see the number of files are processed by SSM.

3. Check file status in remote HDFS cluster
  Check file size and checksum between primary and remote HDFS cluster.

### Test Disaster Recovery through Script
[TODO] Python script is not finished yet. :)
