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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xchain.Catalog;
import org.xchain.framework.hibernate.CriteriaEntityPermission;
import org.xchain.framework.hibernate.EntityOperation;
import org.xchain.framework.hibernate.EntityPermission;
import org.xchain.framework.hibernate.HibernateLifecycle;
import org.xchain.framework.lifecycle.Lifecycle;
import org.xchain.framework.lifecycle.ThreadContext;
import org.xchain.framework.lifecycle.ThreadLifecycle;
import org.xchain.framework.security.IdentityManager;
import org.xchain.framework.security.SecurityManager;
import org.xchain.framework.security.UsernamePrincipal;
import org.xchain.namespaces.hibernate.test.om.User;
import org.xchain.namespaces.hibernate.test.om.UserNote;

/**
 * @author Jason Rose
 * @author Josh Kennedy
 */
public class TestCriteriaEntityPermission extends BaseDatabaseTest {

  public static final Logger log = LoggerFactory.getLogger(TestCriteriaEntityPermission.class);

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
  public void testGetCriteria() throws Exception {
    Session session = HibernateLifecycle.getCurrentSession();
    IdentityManager.instance().loggedIn(new UsernamePrincipal(userList.get(0).getUsername()));
    Transaction t = session.beginTransaction();
    Criteria notesCriteria = session.createCriteria(UserNote.class);
    notesCriteria.setProjection(Projections.projectionList().add(Projections.count("text")));
    CriteriaEntityPermission p = new CriteriaEntityPermission(EntityOperation.LOAD, UserNote.class, notesCriteria, null, null, null, null);
    assertEquals(p.getCriteria(), notesCriteria);
    t.rollback();
  }
  
  @Test
  public void testSetupCorrect() throws Exception {
    DetachedCriteria criteria = DetachedCriteria.forClass(UserNote.class);
    assertTrue(list(criteria).size() > 0);
    assertTrue(ThreadLifecycle.getInstance().inThread());
    DetachedCriteria userCriteria = DetachedCriteria.forClass(User.class);
    assertTrue(list(userCriteria).size() > 0);
    assertNull(IdentityManager.instance().getIdentity());
  }

  @Test
  public void testNullCriteria() throws Exception {
    CriteriaEntityPermission p = new CriteriaEntityPermission(EntityOperation.ALL, UserNote.class, null, null, null, null, null);
    IdentityManager.instance().loggedIn(new UsernamePrincipal(userList.get(0).getUsername()));
    UserNote instance = (UserNote) userList.get(0).getUserNoteSet().toArray()[0];
    EntityPermission<UserNote> instancePermission = new EntityPermission<UserNote>(EntityOperation.LOAD, instance.getId(), instance);
    try {
      SecurityManager.instance().checkPermission(instancePermission);
      fail();
    } catch (SecurityException e) {
    }
    try {
      IdentityManager.instance().getIdentity().getPermissions().add(p);
      SecurityManager.instance().checkPermission(instancePermission);
      fail();
    } catch (Exception e) {
    }
    try {
      p = new CriteriaEntityPermission(EntityOperation.ALL, UserNote.class, null, null, null, "foo", null);
      IdentityManager.instance().getIdentity().getPermissions().add(p);
      SecurityManager.instance().checkPermission(instancePermission);
      fail();
    } catch (SecurityException e) {
    }
    try {
      p = new CriteriaEntityPermission(EntityOperation.ALL, UserNote.class, null, null, null, null, "foo");
      IdentityManager.instance().getIdentity().getPermissions().add(p);
      SecurityManager.instance().checkPermission(instancePermission);
      fail();
    } catch (SecurityException e) {
    }
  }
  
  @Test
  public void testEmptyResultCriteria() throws Exception {
    Session session = HibernateLifecycle.getCurrentSession();
    IdentityManager.instance().loggedIn(new UsernamePrincipal(userList.get(0).getUsername()));
    UserNote instance = (UserNote) userList.get(0).getUserNoteSet().toArray()[0];
    Transaction t = session.beginTransaction();
    Criteria notesCriteria = session.createCriteria(UserNote.class);
    notesCriteria.setProjection(Projections.projectionList().add(Projections.count("text")));
    notesCriteria.add(Restrictions.isNull("text"));
    CriteriaEntityPermission p = new CriteriaEntityPermission(EntityOperation.LOAD, UserNote.class, notesCriteria, null, null, null, null);
    EntityPermission<UserNote> instancePermission = new EntityPermission<UserNote>(EntityOperation.LOAD, instance.getId(), instance);
    try {
      SecurityManager.instance().checkPermission(instancePermission);
      fail();
    } catch (SecurityException e) {
    }
    try {
      IdentityManager.instance().getIdentity().getPermissions().add(p);
      SecurityManager.instance().checkPermission(instancePermission);
      fail();
    } catch (Exception e) {
    }
    t.rollback();
  }
  
  @Test
  public void testOnlyRootCriteria() throws Exception {
    Session session = HibernateLifecycle.getCurrentSession();
    IdentityManager.instance().loggedIn(new UsernamePrincipal(userList.get(0).getUsername()));
    UserNote instance = (UserNote) userList.get(0).getUserNoteSet().toArray()[0];
    Transaction t = session.beginTransaction();
    Criteria notesCriteria = session.createCriteria(UserNote.class);
    notesCriteria.setProjection(Projections.projectionList().add(Projections.count("text")));
    CriteriaEntityPermission p = new CriteriaEntityPermission(EntityOperation.LOAD, UserNote.class, notesCriteria, null, null, null, null);
    EntityPermission<UserNote> instancePermission = new EntityPermission<UserNote>(EntityOperation.LOAD, instance.getId(), instance);
    try {
      SecurityManager.instance().checkPermission(instancePermission);
      fail();
    } catch (SecurityException e) {
    }
    try {
      IdentityManager.instance().getIdentity().getPermissions().add(p);
      SecurityManager.instance().checkPermission(instancePermission);
    } catch (Exception e) {
      fail(e.getMessage());
    }
    t.rollback();
  }
  
  @Test
  public void testRootAndEntityAreSameCriteria() throws Exception {
    Session session = HibernateLifecycle.getCurrentSession();
    IdentityManager.instance().loggedIn(new UsernamePrincipal(userList.get(0).getUsername()));
    UserNote instance = (UserNote) userList.get(0).getUserNoteSet().toArray()[0];
    Transaction t = session.beginTransaction();
    Criteria notesCriteria = session.createCriteria(UserNote.class);
    notesCriteria.setProjection(Projections.projectionList().add(Projections.count("text")));
    Criteria userCriteria = notesCriteria.createCriteria("user");
    CriteriaEntityPermission p = new CriteriaEntityPermission(EntityOperation.LOAD, UserNote.class, notesCriteria, userCriteria, notesCriteria, "username", "id");
    EntityPermission<UserNote> instancePermission = new EntityPermission<UserNote>(EntityOperation.LOAD, instance.getId(), instance);
    try {
      SecurityManager.instance().checkPermission(instancePermission);
      fail();
    } catch (SecurityException e) {
    }
    try {
      IdentityManager.instance().getIdentity().getPermissions().add(p);
      SecurityManager.instance().checkPermission(instancePermission);
    } catch (Exception e) {
      fail(e.getMessage());
    }
    t.rollback();
  }
  
  @Test
  public void testRootAndPrincipalAreSameCriteria() throws Exception {
    Session session = HibernateLifecycle.getCurrentSession();
    IdentityManager.instance().loggedIn(new UsernamePrincipal(userList.get(0).getUsername()));
    User instance = userList.get(0);
    Transaction t = session.beginTransaction();
    Criteria userCriteria = session.createCriteria(User.class);
    userCriteria.setProjection(Projections.projectionList().add(Projections.count("username")));
    CriteriaEntityPermission p = new CriteriaEntityPermission(EntityOperation.LOAD, User.class, userCriteria, userCriteria, null, "username", "id");
    EntityPermission<User> instancePermission = new EntityPermission<User>(EntityOperation.LOAD, instance.getUsername(), instance);
    try {
      SecurityManager.instance().checkPermission(instancePermission);
      fail();
    } catch (SecurityException e) {
    }
    try {
      IdentityManager.instance().getIdentity().getPermissions().add(p);
      SecurityManager.instance().checkPermission(instancePermission);
    } catch (Exception e) {
      fail(e.getMessage());
    }
    t.rollback();
  }
  
  @Test
  public void testAllCriteriaAreSame() throws Exception {
    Session session = HibernateLifecycle.getCurrentSession();
    IdentityManager.instance().loggedIn(new UsernamePrincipal(userList.get(0).getUsername()));
    User instance = userList.get(0);
    Transaction t = session.beginTransaction();
    Criteria userCriteria = session.createCriteria(User.class);
    userCriteria.setProjection(Projections.projectionList().add(Projections.count("username")));
    CriteriaEntityPermission p = new CriteriaEntityPermission(EntityOperation.LOAD, User.class, userCriteria, userCriteria, userCriteria, "username", "id");
    EntityPermission<User> instancePermission = new EntityPermission<User>(EntityOperation.LOAD, instance.getUsername(), instance);
    try {
      SecurityManager.instance().checkPermission(instancePermission);
      fail();
    } catch (SecurityException e) {
    }
    try {
      IdentityManager.instance().getIdentity().getPermissions().add(p);
      SecurityManager.instance().checkPermission(instancePermission);
    } catch (Exception e) {
      fail(e.getMessage());
    }
    t.rollback();
  }
  
  @Test
  public void testAllCriteriaAreDifferent() throws Exception {
    
  }
  
  private List<?> list(DetachedCriteria criteria) {
    Session session = HibernateLifecycle.getCurrentSession();
    List<?> results = null;
    if( session.getTransaction() == null || !session.getTransaction().isActive() ) {
      session.beginTransaction();
      results = criteria.getExecutableCriteria(session).list();
      session.getTransaction().commit();
    }
    return results;
  }
}
