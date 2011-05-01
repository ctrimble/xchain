/**
 *    Copyright 2011 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.xchain.namespaces.hibernate.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xchain.Catalog;
import org.xchain.framework.hibernate.EntityOperation;
import org.xchain.framework.hibernate.EntityPermission;
import org.xchain.framework.hibernate.HibernateLifecycle;
import org.xchain.framework.hibernate.HqlEntityPermission;
import org.xchain.framework.hibernate.QualifiedEntityPermission;
import org.xchain.framework.lifecycle.Lifecycle;
import org.xchain.framework.lifecycle.ThreadContext;
import org.xchain.framework.lifecycle.ThreadLifecycle;
import org.xchain.framework.security.IdentityManager;
import org.xchain.framework.security.Permission;
import org.xchain.framework.security.SecurityManager;
import org.xchain.framework.security.UsernamePrincipal;
import org.xchain.namespaces.hibernate.test.om.UserNote;

/**
 * @author Jason Rose
 * @author Josh Kennedy
 */
public class TestHqlEntityPermission extends BaseDatabaseTest {

  public static final Logger log = LoggerFactory.getLogger(TestHqlEntityPermission.class);

  protected JXPathContext context = null;
  protected ThreadContext threadContext = null;
  protected Catalog catalog = null; 

  @BeforeClass public static void setupCommand()
  throws Exception
  {
    Lifecycle.startLifecycle();
    populateUserData();
    populateUserNoteData();
  }

  @AfterClass public static void teardownCommand()
  throws Exception
  {
    Lifecycle.stopLifecycle();
  }

  @Before public void setupTest()
  throws Exception
  {
    // create the context.
    context = JXPathContext.newContext(new Object());
    threadContext = new TestQualifiedEntityPermission.TestThreadContext();
    ThreadLifecycle.getInstance().startThread(threadContext);
  }

  @After public void teardownTest() throws Exception {
    context = null;
    catalog = null;
    ThreadLifecycle.getInstance().stopThread(threadContext);
    threadContext = null;
  }
  
  @Test
  public void testGetEntityClass() throws Exception {
    QualifiedEntityPermission p = new HqlEntityPermission(EntityOperation.ALL, UserNote.class, null);
    assertEquals(UserNote.class, p.getEntityClass());
  }
  
  @Test
  public void testGetHql() throws Exception {
    final String hql = String.format("select count(id) from %s group by id", UserNote.class.getName());
    HqlEntityPermission p = new HqlEntityPermission(EntityOperation.ALL, UserNote.class, hql);
    assertEquals(hql, p.getHql());
  }
  
  @Test
  public void testIdParameter() throws Exception {
    HibernateLifecycle.getCurrentSession().beginTransaction();
    final String hql = String.format("select count(id) from %s where id = :id group by id", UserNote.class.getName());
    Permission p = new HqlEntityPermission(EntityOperation.ALL, UserNote.class, hql);
    UserNote instance = (UserNote) userList.get(0).getUserNoteSet().toArray()[0];
    IdentityManager.instance().loggedIn(new UsernamePrincipal(userList.get(0).getUsername()));
    Permission entityPermission = new EntityPermission<UserNote>(EntityOperation.LOAD, instance.getId(), instance);
    try {
      SecurityManager.instance().checkPermission(entityPermission);
      fail();
    } catch (SecurityException e) {
    }
    try {
      IdentityManager.instance().getIdentity().getPermissions().add(p);
      SecurityManager.instance().checkPermission(entityPermission);
    } catch (Exception e) {
      fail(e.getMessage());
    }
    HibernateLifecycle.getCurrentSession().getTransaction().rollback();
  }
  
  @Test
  public void testPrincipalParameter() throws Exception {
    HibernateLifecycle.getCurrentSession().beginTransaction();
    final String hql = String.format("select count(user.username) from %s entity join entity.user user where user.username = :principal group by user.username", UserNote.class.getName());
    Permission p = new HqlEntityPermission(EntityOperation.ALL, UserNote.class, hql);
    UserNote instance = (UserNote) userList.get(0).getUserNoteSet().toArray()[0];
    IdentityManager.instance().loggedIn(new UsernamePrincipal(userList.get(0).getUsername()));
    Permission entityPermission = new EntityPermission<UserNote>(EntityOperation.LOAD, instance.getId(), instance);
    try {
      SecurityManager.instance().checkPermission(entityPermission);
      fail();
    } catch (SecurityException e) {
    }
    try {
      IdentityManager.instance().getIdentity().getPermissions().add(p);
      SecurityManager.instance().checkPermission(entityPermission);
    } catch (Exception e) {
      fail(e.getMessage());
    }
    HibernateLifecycle.getCurrentSession().getTransaction().rollback();
  }
  
  @Test
  public void testUnknownParameter() throws Exception {
    HibernateLifecycle.getCurrentSession().beginTransaction();
    final String hql = String.format("select count(user.username) from %s entity join entity.user user where user.username = :foo group by user.username", UserNote.class.getName());
    Permission p = new HqlEntityPermission(EntityOperation.ALL, UserNote.class, hql);
    UserNote instance = (UserNote) userList.get(0).getUserNoteSet().toArray()[0];
    IdentityManager.instance().loggedIn(new UsernamePrincipal(userList.get(0).getUsername()));
    Permission entityPermission = new EntityPermission<UserNote>(EntityOperation.LOAD, instance.getId(), instance);
    try {
      SecurityManager.instance().checkPermission(entityPermission);
      fail();
    } catch (SecurityException e) {
    }
    try {
      IdentityManager.instance().getIdentity().getPermissions().add(p);
      SecurityManager.instance().checkPermission(entityPermission);
      fail();
    } catch (SecurityException e) {
    }
    HibernateLifecycle.getCurrentSession().getTransaction().rollback();
  }
}
