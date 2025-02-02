/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zeppelin.utils;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.zeppelin.conf.ZeppelinConfiguration;
import org.apache.zeppelin.realm.LdapRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * Tools for securing Zeppelin
 */
public class SecurityUtils {

  private static final String ANONYMOUS = "anonymous";
  private static final HashSet<String> EMPTY_HASHSET = Sets.newHashSet();
  private static boolean isEnabled = false;
  private static final Logger LOG = LoggerFactory.getLogger(SecurityUtils.class);
  
  public static void initSecurityManager(String shiroPath) {
    try {
      IniWebEnvironment environment = new IniWebEnvironment();
      environment.setConfigLocations("file:" + shiroPath);
      environment.init();
      SecurityManager securityManager = environment.getSecurityManager();
      org.apache.shiro.SecurityUtils.setSecurityManager(securityManager);
    }
    catch (Exception e) {
      LOG.error("Cannot init shiro", e);
      throw e;
    }
    isEnabled = true;
  }

  public static Boolean isValidOrigin(String sourceHost, ZeppelinConfiguration conf)
      throws UnknownHostException, URISyntaxException {

    String sourceUriHost = "";

    if (sourceHost != null && !sourceHost.isEmpty()) {
      sourceUriHost = new URI(sourceHost).getHost();
      sourceUriHost = (sourceUriHost == null) ? "" : sourceUriHost.toLowerCase();
    }

    sourceUriHost = sourceUriHost.toLowerCase();
    String currentHost = InetAddress.getLocalHost().getHostName().toLowerCase();

    return conf.getAllowedOrigins().contains("*") ||
        currentHost.equals(sourceUriHost) ||
        "localhost".equals(sourceUriHost) ||
        conf.getAllowedOrigins().contains(sourceHost);
  }

  /**
   * Return the authenticated user if any, otherwise returns "anonymous"
   *
   * @return shiro principal
   */
  public static String getPrincipal() {
    if (!isEnabled) {
      return ANONYMOUS;
    }
    Subject subject = org.apache.shiro.SecurityUtils.getSubject();

    String principal;
    if (subject.isAuthenticated()) {
      principal = subject.getPrincipal().toString();
    } else {
      principal = ANONYMOUS;
    }
    return principal;
  }

  public static Collection getRealmsList() {
    if (!isEnabled) {
      return Collections.emptyList();
    }
    DefaultWebSecurityManager defaultWebSecurityManager;
    String key = ThreadContext.SECURITY_MANAGER_KEY;
    defaultWebSecurityManager = (DefaultWebSecurityManager) ThreadContext.get(key);
    Collection<Realm> realms = defaultWebSecurityManager.getRealms();
    return realms;
  }

  /**
   * Return the roles associated with the authenticated user if any otherwise returns empty set
   * TODO(prasadwagle) Find correct way to get user roles (see SHIRO-492)
   *
   * @return shiro roles
   */
  public static HashSet<String> getRoles() {
    if (!isEnabled) {
      return EMPTY_HASHSET;
    }
    Subject subject = org.apache.shiro.SecurityUtils.getSubject();
    HashSet<String> roles = new HashSet<>();
    Map allRoles = null;

    if (subject.isAuthenticated()) {
      Collection realmsList = SecurityUtils.getRealmsList();
      for (Iterator<Realm> iterator = realmsList.iterator(); iterator.hasNext(); ) {
        Realm realm = iterator.next();
        String name = realm.getClass().getName();
        if (name.equals("org.apache.shiro.realm.text.IniRealm")) {
          allRoles = ((IniRealm) realm).getIni().get("roles");
          break;
        } else if (name.equals("org.apache.zeppelin.realm.LdapRealm")) {
          allRoles = ((LdapRealm) realm).getListRoles();
          break;
        }
      }
      if (allRoles != null) {
        Iterator it = allRoles.entrySet().iterator();
        while (it.hasNext()) {
          Map.Entry pair = (Map.Entry) it.next();
          if (subject.hasRole((String) pair.getKey())) {
            roles.add((String) pair.getKey());
          }
        }
      }
    }
    return roles;
  }

  /**
   * Checked if shiro enabled or not
   */
  public static boolean isAuthenticated() {
    if (!isEnabled) {
      return false;
    }
    return org.apache.shiro.SecurityUtils.getSubject().isAuthenticated();
  }
}
