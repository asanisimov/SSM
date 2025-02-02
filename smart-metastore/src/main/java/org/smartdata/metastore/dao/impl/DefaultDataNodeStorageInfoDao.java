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
package org.smartdata.metastore.dao.impl;

import org.smartdata.metastore.dao.AbstractDao;
import org.smartdata.metastore.dao.DataNodeStorageInfoDao;
import org.smartdata.model.DataNodeStorageInfo;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultDataNodeStorageInfoDao extends AbstractDao implements DataNodeStorageInfoDao {
  private static final String TABLE_NAME = "datanode_storage_info";

  public DefaultDataNodeStorageInfoDao(DataSource dataSource) {
    super(dataSource, TABLE_NAME);
  }

  @Override
  public List<DataNodeStorageInfo> getAll() {
    return jdbcTemplate.query("SELECT * FROM " + TABLE_NAME,
        new DataNodeStorageInfoRowMapper());
  }

  @Override
  public List<DataNodeStorageInfo> getByUuid(String uuid) {
    return jdbcTemplate.query(
        "SELECT * FROM " + TABLE_NAME + " WHERE uuid = ?",
        new Object[]{uuid}, new DataNodeStorageInfoRowMapper());
  }

  @Override
  public List<DataNodeStorageInfo> getBySid(int sid) {
    return jdbcTemplate.query("SELECT * FROM " + TABLE_NAME + " WHERE sid = ?",
        new Object[]{sid}, new DataNodeStorageInfoRowMapper());
  }

  @Override
  public void insert(DataNodeStorageInfo dataNodeStorageInfo) {
    insert(dataNodeStorageInfo, this::toMap);
  }

  @Override
  public void insert(DataNodeStorageInfo[] dataNodeStorageInfos) {
    insert(dataNodeStorageInfos, this::toMap);
  }

  @Override
  public void insert(List<DataNodeStorageInfo> dataNodeStorageInfos) {
    insert(dataNodeStorageInfos, this::toMap);
  }

  @Override
  public void delete(String uuid) {
    final String sql = "DELETE FROM " + TABLE_NAME + " WHERE uuid = ?";
    jdbcTemplate.update(sql, uuid);
  }

  @Override
  public void deleteAll() {
    final String sql = "DELETE FROM " + TABLE_NAME;
    jdbcTemplate.update(sql);
  }

  private Map<String, Object> toMap(DataNodeStorageInfo dataNodeStorageInfo) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("uuid", dataNodeStorageInfo.getUuid());
    parameters.put("sid", dataNodeStorageInfo.getSid());
    parameters.put("state", dataNodeStorageInfo.getState());
    parameters.put("storage_id", dataNodeStorageInfo.getStorageId());
    parameters.put("failed", dataNodeStorageInfo.getFailed());
    parameters.put("capacity", dataNodeStorageInfo.getCapacity());
    parameters.put("dfs_used", dataNodeStorageInfo.getDfsUsed());
    parameters.put("remaining", dataNodeStorageInfo.getRemaining());
    parameters.put("block_pool_used", dataNodeStorageInfo.getBlockPoolUsed());
    return parameters;
  }

  private static class DataNodeStorageInfoRowMapper implements RowMapper<DataNodeStorageInfo> {

    @Override
    public DataNodeStorageInfo mapRow(ResultSet resultSet, int i) throws SQLException {
      return DataNodeStorageInfo.newBuilder()
          .setUuid(resultSet.getString("uuid"))
          .setSid(resultSet.getLong("sid"))
          .setState(resultSet.getLong("state"))
          .setStorageId(resultSet.getString("storage_id"))
          .setFailed(resultSet.getBoolean("failed"))
          .setCapacity(resultSet.getLong("capacity"))
          .setDfsUsed(resultSet.getLong("dfs_used"))
          .setRemaining(resultSet.getLong("remaining"))
          .setBlockPoolUsed(resultSet.getLong("block_pool_used"))
          .build();
    }
  }
}
