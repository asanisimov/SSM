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
package org.smartdata.server.engine.cmdlet.agent;

import akka.actor.ActorSystem;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Before;

public abstract class ActorSystemHarness {

  private ActorSystem system;

  @Before
  public void startActorSystem() {
    system = ActorSystem.apply("Test", ConfigFactory.load(AgentConstants.AKKA_CONF_FILE));
  }

  @After
  public void stopActorSystem() {
     system.terminate();
  }

  public ActorSystem getActorSystem() {
    return system;
  }
}
