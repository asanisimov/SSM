import requests
import random
import time
import uuid
import sys
import os
import subprocess

# Server info
# ATTENTION
# Unset Proxy before use RESTful API
SSM_BASE_URL = os.environ.get("SSM_BASE_URL", "http://localhost:7045")

# Restapi root
REST_ROOT = SSM_BASE_URL + "/smart/api/v1"
RULE_ROOT = REST_ROOT + "/rules"
CMDLET_ROOT = REST_ROOT + "/cmdlets"
ACTION_ROOT = REST_ROOT + "/actions"
CLUSTER_ROOT = REST_ROOT + "/cluster"
SYSTEM_ROOT = REST_ROOT + "/system"
CONF_ROOT = REST_ROOT + "/conf"
PRIMARY_ROOT = REST_ROOT + "/primary"

MOVE_TYPES = ['archive', 'alldisk', 'onedisk', 'allssd', 'onessd', 'cache', 'uncache']

HDFS_TEST_DIR = os.environ.get("HDFS_TEST_DIR", "/ssmtest/")


def convert_to_byte(file_size):
    if file_size.endswith('GB'):
        file_size = file_size.replace('GB', '')
        return int(file_size)*1024*1024*1024
    elif file_size.endswith("MB"):
        file_size = file_size.replace("MB", "")
        return int(file_size)*1024*1024
    elif file_size.endswith('KB'):
        file_size = file_size.replace('KB', '')
        return int(file_size)*1024;
    elif file_size.endswith('B'):
        file_size = file_size.replace('B', '')
        return int(file_size)
    else:
        return file_size


def cpu_count():
    '''Returns the number of CPUs in the system
    '''
    num = 1
    if sys.platform == 'win32':
        try:
            num = int(os.environ['NUMBER_OF_PROCESSORS'])
        except (ValueError, KeyError):
            pass
    elif sys.platform == 'darwin':
        try:
            num = int(os.popen('sysctl -n hw.ncpu').read())
        except ValueError:
            pass
    else:
        try:
            num = os.sysconf('SC_NPROCESSORS_ONLN')
        except (ValueError, OSError, AttributeError):
            pass
    return num


def exec_commands(cmds):
    '''Exec commands in parallel in multiple process
    (as much as we have CPU)
    '''
    if not cmds:
        # empty list
        return

    def done(p):
        return p.poll() is not None

    def success(p):
        return p.returncode == 0

    def fail():
        sys.exit(1)

    # get core number
    max_task = cpu_count()
    processes = []
    while True:
        while cmds and len(processes) < max_task:
            task = cmds.pop()
            print(task)
            processes.append(subprocess.Popen(task, shell=True))
        for p in processes:
            if done(p):
                if success(p):
                    processes.remove(p)
                else:
                    fail()
        if not processes and not cmds:
            break
        else:
            time.sleep(0.05)


def random_file_path():
    return HDFS_TEST_DIR + random_string()


def random_string():
    return str(uuid.uuid4())


def check_post_resp(resp):
    if resp.status_code != 201:
        raise IOError("Post fails")


def check_get_resp(resp):
    if resp.status_code != 200:
        raise IOError("Get fails")


def all_success(cmds):
    for cmd in cmds:
        try:
            if cmd is None or cmd['state'] == "FAILED":
                return False
        except Exception:
            return False
    return True


def move_cmdlet(mover_type, file_path):
    return submit_cmdlet(mover_type + " -file " + file_path)


def submit_cmdlet(cmdlet_str):
    """
    submit cmdlet then return cid
    """
    resp = requests.post(CMDLET_ROOT + "/submit", data=cmdlet_str)
    return resp.json()["body"]


def get_cmdlet(cid):
    """
    get cmdlet json with cid
    """
    resp = requests.get(CMDLET_ROOT + "/" + str(cid) + "/info")
    return resp.json()["body"]


def wait_for_cmdlet(cid, period=60):
    """
    wait at most 60 seconds for cmdlet to be done
    """
    timeout = time.time() + period
    while True:
        cmdlet = get_cmdlet(cid)
        if cmdlet['state'] == "PENDING" or cmdlet['state'] == "EXECUTING":
            time.sleep(1)
        elif cmdlet['state'] == "DONE" or cmdlet['state'] == "FAILED":
            return cmdlet
        if time.time() >= timeout:
            return None


def wait_for_cmdlets(cids, period=60):
    failed_cids = []
    while len(cids) != 0:
        cmd = wait_for_cmdlet(cids[0], period)
        if cmd is None or cmd['state'] == 'FAILED':
            failed_cids.append(cids[0])
        cids.pop(0)
    return failed_cids


def wait_cmdlets(cids):
    while len(cids) != 0:
        i = 0
        while i < len(cids):
            cmdlet = get_cmdlet(cids[i])
            time.sleep(.01)
            if cmdlet['state'] == "DONE" or cmdlet['state'] == "FAILED":
                cids.pop(i)
            else:
                i += 1


def get_rule(rid):
    resp = requests.get(RULE_ROOT + "/" + str(rid) + "/info",
                        data=str(rid))
    return resp.json()["body"]


def list_rule():
    resp = requests.get(RULE_ROOT + "/list")
    return resp.json()["body"]


def submit_rule(rule_str):
    resp = requests.post(RULE_ROOT + "/add", data={'ruleText': rule_str})
    return resp.json()["body"]


def stop_rule(rid):
    requests.post(RULE_ROOT + "/") + str(rid) + "/stop"


def delete_rule(rid):
    requests.post(RULE_ROOT + "/" + str(rid) + "/delete")


def start_rule(rid):
    requests.post(RULE_ROOT + "/" + str(rid) + "/start")


def stop_rule(rid):
    requests.post(RULE_ROOT + "/" + str(rid) + "/stop")


def list_sync():
    resp = requests.get(RULE_ROOT + "/list/sync")
    return resp.json()["body"]


def get_sync_info_by_rid(rid):
    infos = list_sync()
    for info in infos:
        if info["id"] is rid:
            return info
    return None


def get_cmdlets_of_rule(rid):
    resp = requests.get(RULE_ROOT + "/" + str(rid) + "/cmdlets")
    return resp.json()["body"]


def get_cids_of_rule(rid):
    cids = []
    for cmdlet in get_cmdlets_of_rule(rid):
        cids.append(cmdlet['cid'])
    return cids


def get_action(aid):
    resp = requests.get(ACTION_ROOT + "/" + str(aid) + "/info")
    return resp.json()["body"]


def list_action():
    resp = requests.get(ACTION_ROOT + "/list")
    return resp.json()["body"]


def read_file(file_path):
    cmdlet_str = "read -file " + file_path
    return submit_cmdlet(cmdlet_str)


def create_file(file_path, length=1024):
    cmdlet_str = "write -file " + file_path + " -length " + str(length)
    return submit_cmdlet(cmdlet_str)


def create_random_file(length=1024):
    """
    create a random file in /ssmtest/
    """
    file_path = HDFS_TEST_DIR + random_string()
    cmdlet_str = "write -file " + \
                 file_path + " -length " + str(length)
    wait_for_cmdlet(submit_cmdlet(cmdlet_str))
    return file_path


def create_random_file_parallel(length=1024, dest_path=HDFS_TEST_DIR):
    """
    create a random file in dest_path, e.g., /ssmtest/
    """
    file_path = dest_path + random_string()
    cmdlet_str = "write -file " + \
                 file_path + " -length " + str(length)
    return file_path, submit_cmdlet(cmdlet_str)


def copy_file_to_s3(file_path, dest_path):
    """
    copy file to S3
    """
    cmdlet_str = "copy2s3 -file " + \
                 file_path + " -dest " + dest_path
    return submit_cmdlet(cmdlet_str)


def delete_file(file_path, recursivly=True):
    cmdlet_str = "delete -file " + file_path
    return submit_cmdlet(cmdlet_str)


def append_file(file_path, length=1024):
    """
    append random content to file_path
    """
    cmdlet_str = "append -file " + file_path + " -length " + str(length)
    return submit_cmdlet(cmdlet_str)


def compress_file(src_file, codec: str = None):
    """
    compress :src_file with :codec (Default - smart.compression.codec)
    """
    cmdlet_str = "compress -file " + src_file
    if codec:
        cmdlet_str += f" -codec {codec}"
    return submit_cmdlet(cmdlet_str)


def compact_small_file(src_files, container_file):
    """
    compact small files into container_file
    """
    cmdlet_str = "compact -file " + src_files + \
                 " -containerFile " + container_file
    return submit_cmdlet(cmdlet_str)


def uncompact_small_file(container_file):
    """
    uncompact small files into container_file
    """
    cmdlet_str = "uncompact -containerFile " + container_file
    return submit_cmdlet(cmdlet_str)


def random_move_test_file(file_path):
    index = random.randrange(len(MOVE_TYPES))
    resp = requests.post(CMDLET_ROOT + "/submit",
                         data=MOVE_TYPES[index] + " -file  " + file_path)
    return resp.json()["body"]


def check_storage(file_path):
    resp = requests.post(CMDLET_ROOT + "/submit",
                         data="checkstorage -file  " + file_path)
    cid = resp.json()["body"]
    cmdlet = wait_for_cmdlet(cid)
    aid = cmdlet['aids']
    return get_action(aid[0])


def move_random_file(mover_type, length):
    file_path = HDFS_TEST_DIR + random_string()
    cmd_create = wait_for_cmdlet(create_file(file_path, length))
    cmd_move = wait_for_cmdlet(move_cmdlet(mover_type, file_path))
    return cmd_create, cmd_move


def move_random_file_twice(mover_type_1, mover_type_2, length):
    file_path = HDFS_TEST_DIR + random_string()
    cmd_create = wait_for_cmdlet(create_file(file_path, length))
    cmd_move_1 = wait_for_cmdlet(move_cmdlet(mover_type_1, file_path))
    cmd_move_2 = wait_for_cmdlet(move_cmdlet(mover_type_2, file_path))
    return cmd_create, cmd_move_1, cmd_move_2


def move_randomly(file_path):
    """
    Randomly move blocks of a given file
    """
    index = random.randrange(len(MOVE_TYPES))
    return submit_cmdlet(MOVE_TYPES[index] + " -file " + file_path)


def continually_move(moves, file_path):
    cmds = []
    for move in moves:
        cmds.append(wait_for_cmdlet(move_cmdlet(move, file_path)))
    return cmds


def random_move_list(length=10):
    """
    Generate a rabdin move list with given length.
    Note that neighbor moves must be different.
    """
    moves = []
    last_move = -1
    while length > 0:
        random_index = random.randrange(len(MOVE_TYPES))
        if random_index != last_move:
            last_move = random_index
            moves.append(MOVE_TYPES[random_index])
            length -= 1
    return moves


def random_move_list_totally(length=10):
    """
    Generate a rabdin move list with given length.
    """
    moves = []
    while length > 0:
        random_index = random.randrange(len(MOVE_TYPES))
        moves.append(MOVE_TYPES[random_index])
        length -= 1
    return moves


def move_random_task_list(file_size):
    """
    Generate a random file with given size, and
    generate rand a move list (nearbor move is different).
    Then, move this file continualy.
    """
    file_path = random_file_path()
    wait_for_cmdlet(create_file(file_path, file_size))
    # check_storage(file_path)
    # use a list to save the result
    # record the last task
    moves = random_move_list(random.randrange(10, 21))
    return continually_move(moves, file_path)


def move_random_task_list_totally(file_size):
    """
    Generate a random file with given size, and
    generate rand a move list.
    Then, move this file continualy.
    """
    file_path = random_file_path()
    wait_for_cmdlet(create_file(file_path, file_size))
    # check_storage(file_path)
    # use a list to save the result
    # record the last task
    moves = random_move_list_totally(random.randrange(10, 21))
    return continually_move(moves, file_path)
