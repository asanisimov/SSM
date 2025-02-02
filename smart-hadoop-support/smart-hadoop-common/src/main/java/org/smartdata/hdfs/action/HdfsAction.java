/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartdata.hdfs.action;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.DFSClient;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.smartdata.action.ActionType;
import org.smartdata.action.SmartAction;
import org.smartdata.conf.SmartConfKeys;
import org.smartdata.model.CmdletDescriptor;

/**
 * Base class for all HDFS actions.
 */
public abstract class HdfsAction extends SmartAction {
  public static final String FILE_PATH = CmdletDescriptor.HDFS_FILE_PATH;
  protected ActionType actionType;
  // SmartDFSClient
  protected DFSClient dfsClient = null;

  public void setDfsClient(DFSClient dfsClient) {
    this.dfsClient = dfsClient;
  }

  protected void withDefaultFs() {
    Configuration conf = getContext().getConf();
    String nameNodeURL = conf.get(SmartConfKeys.SMART_DFS_NAMENODE_RPCSERVER_KEY);
    conf.set(DFSConfigKeys.FS_DEFAULT_NAME_KEY, nameNodeURL);
  }
}
