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
package org.smartdata.metastore.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdata.metastore.MetaStore;
import org.smartdata.metastore.MetaStoreException;

public abstract class TableEvictor {
  public static final Logger LOG = LoggerFactory.getLogger(TableEvictor.class);
  private final MetaStore metaStore;

  public TableEvictor(MetaStore metaStore) {
    this.metaStore = metaStore;
  }

  public void dropTable(AccessCountTable accessCountTable) {
    try {
      metaStore.dropTable(accessCountTable.getTableName());
      metaStore.deleteAccessCountTable(accessCountTable);
      LOG.debug("Dropped access count table " + accessCountTable.getTableName());
    } catch (MetaStoreException e) {
      LOG.error("Drop access count table {} failed", accessCountTable.getTableName(), e);
    }
  }

  abstract void evictTables(AccessCountTableDeque tables, long lastAggregatedIntervalEndTimestamp);
}
