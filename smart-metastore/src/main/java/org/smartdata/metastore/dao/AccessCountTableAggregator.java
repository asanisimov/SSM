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

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class AccessCountTableAggregator {
  private final MetaStore metaStore;
  public static final Logger LOG =
      LoggerFactory.getLogger(AccessCountTableAggregator.class);

  public AccessCountTableAggregator(MetaStore metaStore) {
    this.metaStore = metaStore;
  }

  public void aggregate(AccessCountTable destinationTable,
                        List<AccessCountTable> tablesToAggregate) throws MetaStoreException {
    if (tablesToAggregate.isEmpty()) {
      return;
    }

    ReentrantLock accessCountLock = metaStore.getAccessCountLock();
    accessCountLock.lock();
    try {
      metaStore.aggregateTables(destinationTable, tablesToAggregate);
      metaStore.insertAccessCountTable(destinationTable);
    } finally {
      accessCountLock.unlock();
    }
  }
}
