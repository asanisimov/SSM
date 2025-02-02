/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartdata.metastore.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.smartdata.metastore.dao.AbstractDao;
import org.smartdata.metastore.dao.CmdletDao;
import org.smartdata.model.CmdletInfo;
import org.smartdata.model.CmdletState;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultCmdletDao extends AbstractDao implements CmdletDao {
  private static final String TABLE_NAME = "cmdlet";
  private final String terminatedStates;

  public DefaultCmdletDao(DataSource dataSource) {
    super(dataSource, TABLE_NAME);
    terminatedStates = getTerminatedStatesString();
  }

  @Override
  public List<CmdletInfo> getAll() {
    return jdbcTemplate.query("SELECT * FROM " + TABLE_NAME, new CmdletRowMapper());
  }

  @Override
  public List<CmdletInfo> getAPageOfCmdlet(long start, long offset,
                                           List<String> orderBy, List<Boolean> isDesc) {
    boolean ifHasAid = false;
    StringBuilder sql =
        new StringBuilder("SELECT * FROM " + TABLE_NAME + " ORDER BY ");
    for (int i = 0; i < orderBy.size(); i++) {
      if (orderBy.get(i).equals("cid")) {
        ifHasAid = true;
      }
      sql.append(orderBy.get(i));
      if (isDesc.size() > i) {
        if (isDesc.get(i)) {
          sql.append(" desc ");
        }
        sql.append(",");
      }
    }
    if (!ifHasAid) {
      sql.append("cid,");
    }
    //delete the last char
    sql = new StringBuilder(sql.substring(0, sql.length() - 1));
    //add limit
    sql.append(" LIMIT ").append(offset).append(" OFFSET ").append(start).append(";");
    return jdbcTemplate.query(sql.toString(), new CmdletRowMapper());
  }

  @Override
  public List<CmdletInfo> getAPageOfCmdlet(long start, long offset) {
    String sql = "SELECT * FROM " + TABLE_NAME + " LIMIT " + start + " OFFSET " + offset + ";";
    return jdbcTemplate.query(sql, new CmdletRowMapper());
  }

  @Override
  public List<CmdletInfo> getByIds(List<Long> aids) {
    return jdbcTemplate.query(
        "SELECT * FROM " + TABLE_NAME + " WHERE aid IN (?)",
        new Object[]{StringUtils.join(aids, ",")},
        new CmdletRowMapper());
  }

  @Override
  public CmdletInfo getById(long cid) {
    return jdbcTemplate.queryForObject(
        "SELECT * FROM " + TABLE_NAME + " WHERE cid = ?",
        new Object[]{cid},
        new CmdletRowMapper());
  }

  @Override
  public List<CmdletInfo> getByRid(long rid) {
    return jdbcTemplate.query(
        "SELECT * FROM " + TABLE_NAME + " WHERE rid = ?",
        new Object[]{rid},
        new CmdletRowMapper());
  }

  @Override
  public long getNumByRid(long rid) {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE rid = ?",
        new Object[]{rid},
        Long.class);
  }

  @Override
  public List<CmdletInfo> getByRid(long rid, long start, long offset) {
    String sql = "SELECT * FROM " + TABLE_NAME + " WHERE rid = " + rid
        + " LIMIT " + offset + " OFFSET " + start + ";";
    return jdbcTemplate.query(sql, new CmdletRowMapper());
  }

  @Override
  public List<CmdletInfo> getByRid(long rid, long start, long offset,
                                   List<String> orderBy, List<Boolean> isDesc) {
    boolean ifHasAid = false;
    StringBuilder sql =
        new StringBuilder("SELECT * FROM " + TABLE_NAME + " WHERE rid = " + rid
            + " ORDER BY ");
    for (int i = 0; i < orderBy.size(); i++) {
      if (orderBy.get(i).equals("cid")) {
        ifHasAid = true;
      }
      sql.append(orderBy.get(i));
      if (isDesc.size() > i) {
        if (isDesc.get(i)) {
          sql.append(" desc ");
        }
        sql.append(",");
      }
    }
    if (!ifHasAid) {
      sql.append("cid,");
    }

    //delete the last char
    sql = new StringBuilder(sql.substring(0, sql.length() - 1));
    //add limit
    sql.append(" LIMIT ").append(offset).append(" OFFSET ").append(start).append(";");
    return jdbcTemplate.query(sql.toString(), new CmdletRowMapper());
  }

  @Override
  public List<CmdletInfo> getByState(CmdletState state) {
    return jdbcTemplate.query(
        "SELECT * FROM " + TABLE_NAME + " WHERE state = ?",
        new Object[]{state.getValue()},
        new CmdletRowMapper());
  }

  @Override
  public int getNumCmdletsInTerminiatedStates() {
    String query = "SELECT count(*) FROM " + TABLE_NAME
        + " WHERE state IN (" + terminatedStates + ")";
    return jdbcTemplate.queryForObject(query, Integer.class);
  }

  @Override
  public List<CmdletInfo> getByCondition(
      String cidCondition, String ridCondition, CmdletState state) {
    String sqlPrefix = "SELECT * FROM " + TABLE_NAME + " WHERE ";
    String sqlCid = (cidCondition == null) ? "" : "AND cid " + cidCondition;
    String sqlRid = (ridCondition == null) ? "" : "AND rid " + ridCondition;
    String sqlState = (state == null) ? "" : "AND state = " + state.getValue();
    String sqlFinal = "";
    if (cidCondition != null || ridCondition != null || state != null) {
      sqlFinal = sqlPrefix + sqlCid + sqlRid + sqlState;
      sqlFinal = sqlFinal.replaceFirst("AND ", "");
    } else {
      sqlFinal = sqlPrefix.replaceFirst("WHERE ", "");
    }
    return jdbcTemplate.query(sqlFinal, new CmdletRowMapper());
  }

  @Override
  public void delete(long cid) {
    final String sql = "DELETE FROM " + TABLE_NAME + " WHERE cid = ?";
    jdbcTemplate.update(sql, cid);
  }

  @Override
  public int[] batchDelete(final List<Long> cids) {
    final String sql = "DELETE FROM " + TABLE_NAME + " WHERE cid = ?";
    return jdbcTemplate.batchUpdate(
        sql,
        new BatchPreparedStatementSetter() {
          public void setValues(PreparedStatement ps, int i) throws SQLException {
            ps.setLong(1, cids.get(i));
          }

          public int getBatchSize() {
            return cids.size();
          }
        });
  }

  @Override
  public int deleteBeforeTime(long timestamp) {

    final String querysql = "SELECT cid FROM " + TABLE_NAME
        + " WHERE  generate_time < ? AND state IN (" + terminatedStates + ")";
    List<Long> cids = jdbcTemplate.queryForList(querysql, new Object[]{timestamp}, Long.class);
    if (cids.isEmpty()) {
      return 0;
    }
    final String deleteCmds = "DELETE FROM " + TABLE_NAME
        + " WHERE generate_time < ? AND state IN (" + terminatedStates + ")";
    jdbcTemplate.update(deleteCmds, timestamp);
    final String deleteActions = "DELETE FROM action WHERE cid IN ("
        + StringUtils.join(cids, ",") + ")";
    jdbcTemplate.update(deleteActions);
    return cids.size();
  }

  @Override
  public int deleteKeepNewCmd(long num) {
    final String queryCids = "SELECT cid FROM " + TABLE_NAME
        + " WHERE state IN (" + terminatedStates + ")"
        + " ORDER BY generate_time DESC LIMIT 100000 OFFSET " + num;
    List<Long> cids = jdbcTemplate.queryForList(queryCids, Long.class);
    if (cids.isEmpty()) {
      return 0;
    }
    String deleteCids = StringUtils.join(cids, ",");
    final String deleteCmd = "DELETE FROM " + TABLE_NAME + " WHERE cid IN (" + deleteCids + ")";
    jdbcTemplate.update(deleteCmd);
    final String deleteActions = "DELETE FROM action WHERE cid IN (" + deleteCids + ")";
    jdbcTemplate.update(deleteActions);
    return cids.size();
  }

  @Override
  public void deleteAll() {
    final String sql = "DELETE FROM " + TABLE_NAME;
    jdbcTemplate.execute(sql);
  }

  @Override
  public void insert(CmdletInfo cmdletInfo) {
    insert(cmdletInfo, this::toMap);
  }

  @Override
  public void insert(CmdletInfo[] cmdletInfos) {
    insert(cmdletInfos, this::toMap);
  }

  @Override
  public int[] replace(final CmdletInfo[] cmdletInfos) {
    String sql = "REPLACE INTO " + TABLE_NAME
        + "(cid, "
        + "rid, "
        + "aids, "
        + "state, "
        + "parameters, "
        + "generate_time, "
        + "state_changed_time)"
        + " VALUES(?, ?, ?, ?, ?, ?, ?)";

    return jdbcTemplate.batchUpdate(
        sql,
        new BatchPreparedStatementSetter() {
          public void setValues(PreparedStatement ps, int i) throws SQLException {
            ps.setLong(1, cmdletInfos[i].getCid());
            ps.setLong(2, cmdletInfos[i].getRid());
            ps.setString(3, StringUtils.join(cmdletInfos[i].getAidsString(), ","));
            ps.setLong(4, cmdletInfos[i].getState().getValue());
            ps.setString(5, cmdletInfos[i].getParameters());
            ps.setLong(6, cmdletInfos[i].getGenerateTime());
            ps.setLong(7, cmdletInfos[i].getStateChangedTime());
          }

          public int getBatchSize() {
            return cmdletInfos.length;
          }
        });
  }

  @Override
  public int update(long cid, int state) {
    String sql =
        "UPDATE " + TABLE_NAME + " SET state = ?, state_changed_time = ? WHERE cid = ?";
    return jdbcTemplate.update(sql, state, System.currentTimeMillis(), cid);
  }

  @Override
  public int update(long cid, String parameters, int state) {
    String sql =
        "UPDATE "
            + TABLE_NAME
            + " SET parameters = ?, state = ?, state_changed_time = ? WHERE cid = ?";
    return jdbcTemplate.update(sql, parameters, state, System.currentTimeMillis(), cid);
  }

  @Override
  public int update(final CmdletInfo cmdletInfo) {
    List<CmdletInfo> cmdletInfos = new ArrayList<>();
    cmdletInfos.add(cmdletInfo);
    return update(cmdletInfos)[0];
  }

  @Override
  public int[] update(final List<CmdletInfo> cmdletInfos) {
    String sql = "UPDATE " + TABLE_NAME + " SET  state = ?, state_changed_time = ? WHERE cid = ?";
    return jdbcTemplate.batchUpdate(
        sql,
        new BatchPreparedStatementSetter() {
          public void setValues(PreparedStatement ps, int i) throws SQLException {
            ps.setInt(1, cmdletInfos.get(i).getState().getValue());
            ps.setLong(2, cmdletInfos.get(i).getStateChangedTime());
            ps.setLong(3, cmdletInfos.get(i).getCid());
          }

          public int getBatchSize() {
            return cmdletInfos.size();
          }
        });
  }

  @Override
  public long getMaxId() {
    Long ret = jdbcTemplate.queryForObject("SELECT MAX(cid) FROM " + TABLE_NAME, Long.class);
    if (ret == null) {
      return 0;
    } else {
      return ret + 1;
    }
  }

  protected Map<String, Object> toMap(CmdletInfo cmdletInfo) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("cid", cmdletInfo.getCid());
    parameters.put("rid", cmdletInfo.getRid());
    parameters.put("aids", StringUtils.join(cmdletInfo.getAidsString(), ","));
    parameters.put("state", cmdletInfo.getState().getValue());
    parameters.put("parameters", cmdletInfo.getParameters());
    parameters.put("generate_time", cmdletInfo.getGenerateTime());
    parameters.put("state_changed_time", cmdletInfo.getStateChangedTime());
    return parameters;
  }

  private String getTerminatedStatesString() {
    String finishedState = "";
    for (CmdletState cmdletState : CmdletState.getTerminalStates()) {
      finishedState = finishedState + cmdletState.getValue() + ",";
    }
    return finishedState.substring(0, finishedState.length() - 1);
  }

  private static class CmdletRowMapper implements RowMapper<CmdletInfo> {

    @Override
    public CmdletInfo mapRow(ResultSet resultSet, int i) throws SQLException {
      CmdletInfo.Builder builder = CmdletInfo.newBuilder();
      builder.setCid(resultSet.getLong("cid"));
      builder.setRid(resultSet.getLong("rid"));
      builder.setAids(convertStringListToLong(resultSet.getString("aids").split(",")));
      builder.setState(CmdletState.fromValue((int) resultSet.getByte("state")));
      builder.setParameters(resultSet.getString("parameters"));
      builder.setGenerateTime(resultSet.getLong("generate_time"));
      builder.setStateChangedTime(resultSet.getLong("state_changed_time"));
      return builder.build();
    }

    private List<Long> convertStringListToLong(String[] strings) {
      List<Long> ret = new ArrayList<>();
      try {
        for (String s : strings) {
          ret.add(Long.valueOf(s));
        }
      } catch (NumberFormatException e) {
        // Return empty
        ret.clear();
      }
      return ret;
    }
  }
}
