import argparse
import unittest
from util import *


class test_ec_action(unittest.TestCase):

  @classmethod
  def tearDownClass(cls):
    subprocess.call(f"hdfs dfs -rm -r {HDFS_TEST_DIR}", shell=True)

  # test XOR-2-1-1024k
  def test_ec(self):
    file_path = create_random_file(FILE_SIZE)
    print("The file path for test is {}.".format(file_path))
    # submit action
    action_str = "ec -file {} -policy {}".format(file_path, POLICY)
    print(f"Run action {action_str}")
    # Activate actions
    cid = submit_cmdlet(action_str)
    cmd = wait_for_cmdlet(cid)
    self.assertTrue(cmd['state'] == "DONE", f"Test failed for ec action with {POLICY} policy.")

    # submit action
    action_str = "unec -file {}".format(file_path)
    # Activate actions
    cid = submit_cmdlet(action_str)
    cmd = wait_for_cmdlet(cid)
    self.assertTrue(cmd['state'] == "DONE", "Test failed for unec action.")


if __name__ == '__main__':
  parser = argparse.ArgumentParser()
  parser.add_argument('-size', default='1MB',
                      help="size of file, Default Value 1MB.")
  parser.add_argument('-policy', default='XOR-2-1-1024k',
                      help="EC policy, Default Value XOR-2-1-1024k.")
  parser.add_argument('unittest_args', nargs='*')
  args, unknown_args = parser.parse_known_args()
  sys.argv[1:] = unknown_args
  print("The file size for test is {}.".format(args.size))
  FILE_SIZE = convert_to_byte(args.size)
  print("The EC policy for test is {}.".format(args.policy))
  POLICY = args.policy

  unittest.main()