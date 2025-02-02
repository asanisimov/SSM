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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.smartdata.metastore.TestDaoBase;
import org.smartdata.model.FileDiff;
import org.smartdata.model.FileDiffState;
import org.smartdata.model.FileDiffType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TestFileDiffDao extends TestDaoBase {
  private FileDiffDao fileDiffDao;

  @Before
  public void initFileDiffDAO() {
    fileDiffDao = daoProvider.fileDiffDao();
  }

  @Test
  public void testInsertAndGetSingleRecord() {
    FileDiff fileDiff = new FileDiff();
    fileDiff.setParameters(new HashMap<String, String>());
    fileDiff.getParameters().put("-test", "test");
    fileDiff.setSrc("test");
    fileDiff.setState(FileDiffState.PENDING);
    fileDiff.setDiffType(FileDiffType.APPEND);
    fileDiff.setCreateTime(1);
    fileDiffDao.insert(fileDiff);
    Assert.assertTrue(fileDiffDao.getAll().get(0).equals(fileDiff));
  }

  @Test
  public void testBatchUpdateAndQuery() {
    FileDiff[] fileDiffs = new FileDiff[2];
    fileDiffs[0] = new FileDiff();
    fileDiffs[0].setDiffId(1);
    fileDiffs[0].setParameters(new HashMap<String, String>());
    fileDiffs[0].setSrc("test");
    fileDiffs[0].setState(FileDiffState.RUNNING);
    fileDiffs[0].setDiffType(FileDiffType.APPEND);
    fileDiffs[0].setCreateTime(1);

    fileDiffs[1] = new FileDiff();
    fileDiffs[1].setDiffId(2);
    fileDiffs[1].setParameters(new HashMap<String, String>());
    fileDiffs[1].setSrc("src");
    fileDiffs[1].setState(FileDiffState.PENDING);
    fileDiffs[1].setDiffType(FileDiffType.APPEND);
    fileDiffs[1].setCreateTime(1);

    fileDiffDao.insert(fileDiffs);
    List<FileDiff> fileInfoList = fileDiffDao.getAll();
    for (int i = 0; i < 2; i++) {
      Assert.assertTrue(fileInfoList.get(i).equals(fileDiffs[i]));
    }

    //update
    List<Long> dids = new ArrayList<>();
    dids.add(1L);
    dids.add(2L);
    List<String> parameters = new ArrayList<>();
    parameters.add(fileDiffs[0].getParametersJsonString());
    parameters.add(fileDiffs[1].getParametersJsonString());
    List<FileDiffState> fileDiffStates = new ArrayList<>();
    fileDiffStates.add(FileDiffState.APPLIED);
    fileDiffStates.add(fileDiffs[1].getState());

    fileDiffDao.batchUpdate(dids, fileDiffStates, parameters);

    fileInfoList = fileDiffDao.getAll();

    Assert.assertTrue(fileInfoList.get(0).getState().equals(FileDiffState.APPLIED));
    fileDiffDao.batchUpdate(dids, FileDiffState.MERGED);
    Assert.assertTrue(fileDiffDao.getAll().get(0).getState().equals(FileDiffState.MERGED));

  }

  @Test
  public void testBatchInsertAndQuery() {
    List<FileDiff> fileDiffs = new ArrayList<>();
    FileDiff fileDiff = new FileDiff();
    fileDiff.setParameters(new HashMap<String, String>());
    fileDiff.setSrc("test");
    fileDiff.setState(FileDiffState.RUNNING);
    fileDiff.setDiffType(FileDiffType.APPEND);
    fileDiff.setCreateTime(1);
    fileDiffs.add(fileDiff);

    fileDiff = new FileDiff();
    fileDiff.setParameters(new HashMap<String, String>());
    fileDiff.setSrc("src");
    fileDiff.setState(FileDiffState.PENDING);
    fileDiff.setDiffType(FileDiffType.APPEND);
    fileDiff.setCreateTime(1);
    fileDiffs.add(fileDiff);

    fileDiffDao.insert(fileDiffs);
    List<FileDiff> fileInfoList = fileDiffDao.getAll();
    for (int i = 0; i < 2; i++) {
      Assert.assertTrue(fileInfoList.get(i).equals(fileDiffs.get(i)));
    }
    List<String> paths = fileDiffDao.getSyncPath(0);
    Assert.assertTrue(paths.size() == 1);
    Assert.assertTrue(fileDiffDao.getPendingDiff("src").size() == 1);
    Assert.assertTrue(fileDiffDao.getByState("test", FileDiffState.RUNNING).size() == 1);
  }

  @Test
  public void testUpdate() {
    FileDiff[] fileDiffs = new FileDiff[2];
    fileDiffs[0] = new FileDiff();
    fileDiffs[0].setDiffId(1);
    fileDiffs[0].setRuleId(1);
    fileDiffs[0].setParameters(new HashMap<String, String>());
    fileDiffs[0].setSrc("test");
    fileDiffs[0].setState(FileDiffState.PENDING);
    fileDiffs[0].setDiffType(FileDiffType.APPEND);
    fileDiffs[0].setCreateTime(1);

    fileDiffs[1] = new FileDiff();
    fileDiffs[1].setDiffId(2);
    fileDiffs[0].setRuleId(1);
    fileDiffs[1].setParameters(new HashMap<String, String>());
    fileDiffs[1].setSrc("src");
    fileDiffs[1].setState(FileDiffState.PENDING);
    fileDiffs[1].setDiffType(FileDiffType.APPEND);
    fileDiffs[1].setCreateTime(1);

    fileDiffDao.insert(fileDiffs);
    fileDiffDao.update(1, FileDiffState.RUNNING);
    fileDiffs[0].setState(FileDiffState.RUNNING);

    Assert.assertTrue(fileDiffDao.getById(1).equals(fileDiffs[0]));
    Assert.assertTrue(fileDiffDao.getPendingDiff().size() == 1);
    fileDiffs[0].getParameters().put("-offset", "0");
    fileDiffs[0].setSrc("test1");
    fileDiffs[1].setCreateTime(2);
    fileDiffs[1].setRuleId(2);
    fileDiffs[1].setDiffType(FileDiffType.RENAME);
    fileDiffDao.update(fileDiffs);
    Assert.assertTrue(fileDiffDao.getById(1).equals(fileDiffs[0]));
    Assert.assertTrue(fileDiffDao.getById(2).equals(fileDiffs[1]));
  }

  @Test
  public void testDeleteUselessRecords() {
    FileDiff[] fileDiffs = new FileDiff[2];
    fileDiffs[0] = new FileDiff();
    fileDiffs[0].setDiffId(1);
    fileDiffs[0].setRuleId(1);
    fileDiffs[0].setParameters(new HashMap<String, String>());
    fileDiffs[0].setSrc("test");
    fileDiffs[0].setState(FileDiffState.PENDING);
    fileDiffs[0].setDiffType(FileDiffType.APPEND);
    fileDiffs[0].setCreateTime(1);

    fileDiffs[1] = new FileDiff();
    fileDiffs[1].setDiffId(2);
    fileDiffs[0].setRuleId(1);
    fileDiffs[1].setParameters(new HashMap<String, String>());
    fileDiffs[1].setSrc("src");
    fileDiffs[1].setState(FileDiffState.PENDING);
    fileDiffs[1].setDiffType(FileDiffType.APPEND);
    fileDiffs[1].setCreateTime(2);

    fileDiffDao.insert(fileDiffs);
    Assert.assertEquals(fileDiffDao.getUselessRecordsNum(), 0);
    fileDiffDao.update(1, FileDiffState.APPLIED);
    Assert.assertEquals(fileDiffDao.getUselessRecordsNum(), 1);
    fileDiffDao.update(2, FileDiffState.FAILED);
    Assert.assertEquals(fileDiffDao.getUselessRecordsNum(), 2);
    fileDiffDao.update(2, FileDiffState.DELETED);
    Assert.assertEquals(fileDiffDao.getUselessRecordsNum(), 2);
    fileDiffDao.deleteUselessRecords(1);
    Assert.assertEquals(fileDiffDao.getAll().size(), 1);
  }
}
