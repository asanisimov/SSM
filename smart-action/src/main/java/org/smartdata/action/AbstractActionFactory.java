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
package org.smartdata.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdata.action.annotation.ActionSignature;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A common action factory for action providers to use.
 */
public abstract class AbstractActionFactory implements ActionFactory {
  static final Logger LOG = LoggerFactory.getLogger(AbstractActionFactory.class);
  private static Map<String, Class<? extends SmartAction>> supportedActions = new HashMap<>();

  static {
    addAction(EchoAction.class);
    addAction(SleepAction.class);
    addAction(SyncAction.class);
    addAction(ExecAction.class);
  }

  protected static void addAction(Class<? extends SmartAction> actionClass) {
    ActionSignature actionSignature = actionClass.getAnnotation(ActionSignature.class);
    if (actionSignature != null) {
      String actionId = actionSignature.actionId();
      if (!supportedActions.containsKey(actionId)) {
        supportedActions.put(actionId, actionClass);
      } else {
        LOG.error("There is already an Action registered with id {}.", actionId);
      }
    } else {
      LOG.error("Action {} does not has an ActionSignature.", actionClass.getName());
    }
  }

  @Override
  public Map<String, Class<? extends SmartAction>> getSupportedActions() {
    return Collections.unmodifiableMap(supportedActions);
  }
}
