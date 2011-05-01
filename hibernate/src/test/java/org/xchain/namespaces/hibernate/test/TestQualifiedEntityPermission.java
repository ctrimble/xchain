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

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
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
import org.xchain.framework.hibernate.HqlEntityPermission;
import org.xchain.framework.lifecycle.Lifecycle;
import org.xchain.framework.lifecycle.LifecycleClass;
import org.xchain.framework.lifecycle.StartThreadStep;
import org.xchain.framework.lifecycle.StopThreadStep;
import org.xchain.framework.lifecycle.ThreadContext;
import org.xchain.framework.lifecycle.ThreadLifecycle;
import org.xchain.framework.security.DefaultIdentityService;
import org.xchain.framework.security.Identity;
import org.xchain.framework.security.IdentityImpl;
import org.xchain.framework.security.IdentityManager;
import org.xchain.framework.security.IdentityService;
import org.xchain.framework.security.Permission;
import org.xchain.framework.security.SecurityManager;
import org.xchain.framework.security.UsernamePrincipal;
import org.xchain.namespaces.hibernate.test.om.User;
import org.xchain.namespaces.hibernate.test.om.UserNote;

/**
 * @author Jason Rose
 * @author Josh Kennedy
 */
public class TestQualifiedEntityPermission extends BaseDatabaseTest {

  public static final Logger log = LoggerFactory.getLogger(TestQualifiedEntityPermission.class);

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
    threadContext = new TestThreadContext();
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
    CriteriaEntityPermission p = new CriteriaEntityPermission(EntityOperation.ALL, UserNote.class, null, null, null, null, null);
    assertEquals(UserNote.class, p.getEntityClass());
  }
  
  @Test
  public void testNotEntityPermission() throws Exception {
    CriteriaEntityPermission p = new CriteriaEntityPermission(EntityOperation.ALL, UserNote.class, null, null, null, null, null);
    IdentityManager.instance().loggedIn(new UsernamePrincipal(userList.get(0).getUsername()));
    Permission notEntityPermission = new Permission() { public boolean implies(Permission permission) {return true;}; };
    try {
      SecurityManager.instance().checkPermission(notEntityPermission);
      fail();
    } catch (SecurityException e) {
    }
    try {
      IdentityManager.instance().getIdentity().getPermissions().add(p);
      SecurityManager.instance().checkPermission(notEntityPermission);
      fail();
    } catch (Exception e) {
    }
  }
  
  @Test
  public void testEntityNotAssignable() throws Exception {
    Session session = HibernateLifecycle.getCurrentSession();
    IdentityManager.instance().loggedIn(new UsernamePrincipal(userList.get(0).getUsername()));
    UserNote instance = (UserNote) userList.get(0).getUserNoteSet().toArray()[0];
    Transaction t = session.beginTransaction();
    Criteria notesCriteria = session.createCriteria(UserNote.class);
    notesCriteria.setProjection(Projections.projectionList().add(Projections.count("text")));
    CriteriaEntityPermission p = new CriteriaEntityPermission(EntityOperation.LOAD, UserNote.class, notesCriteria, null, null, null, null);
    EntityPermission<User> instancePermission = new EntityPermission<User>(EntityOperation.LOAD, instance.getId(), userList.get(0));
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
  public void testPermissionNotImplied() throws Exception {
    Session session = HibernateLifecycle.getCurrentSession();
    IdentityManager.instance().loggedIn(new UsernamePrincipal(userList.get(0).getUsername()));
    UserNote instance = (UserNote) userList.get(0).getUserNoteSet().toArray()[0];
    Transaction t = session.beginTransaction();
    Criteria notesCriteria = session.createCriteria(UserNote.class);
    notesCriteria.setProjection(Projections.projectionList().add(Projections.count("text")));
    CriteriaEntityPermission p = new CriteriaEntityPermission(EntityOperation.LOAD, UserNote.class, notesCriteria, null, null, null, null);
    EntityPermission<UserNote> instancePermission = new EntityPermission<UserNote>(EntityOperation.UPDATE, instance.getId(), instance);
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
  public void testProcessResult() throws Exception {
    HibernateLifecycle.getCurrentSession().beginTransaction();
    final String noResultHql = String.format("select count(entity) from %s entity where entity.id = :id and 0=1", UserNote.class.getName());
    final String arrayResultHql = String.format("select entity.id, entity.text from %s entity where entity.id = :id", UserNote.class.getName());
    final String booleanResultHql = String.format("select new java.lang.Boolean('false') from %s entity where entity.id = :id", UserNote.class.getName());
    final String booleanResultHql2 = String.format("select new java.lang.Boolean('true') from %s entity where entity.id = :id", UserNote.class.getName());
    final String integerResultHql = String.format("select count(entity) from %s entity where entity.id = :id", UserNote.class.getName());
    final String longResultHql = String.format("select entity.id from %s entity where entity.id = :id", UserNote.class.getName());
    final String objectResultHql = String.format("select entity from %s entity where entity.id = :id", UserNote.class.getName());
    final String[] hqlArray = new String[] { noResultHql, arrayResultHql, booleanResultHql, booleanResultHql2, integerResultHql, longResultHql, objectResultHql };
    IdentityManager.instance().loggedIn(new UsernamePrincipal(userList.get(0).getUsername()));
    UserNote instance = (UserNote) userList.get(0).getUserNoteSet().toArray()[0];
    Permission entityPermission = new EntityPermission<UserNote>(EntityOperation.LOAD, instance.getId(), instance);
    for( String hql : hqlArray ) {
      try {
        Set<Permission> permissions = IdentityManager.instance().getIdentity().getPermissions();
        permissions.clear();
        Permission p = new HqlEntityPermission(EntityOperation.ALL, UserNote.class, hql);
        permissions.add(p);
        SecurityManager.instance().checkPermission(entityPermission);
      } catch (SecurityException e) {
      } catch (IllegalArgumentException e) {
      } catch (Exception e) {
        fail(e.getMessage());
      }
    }
    HibernateLifecycle.getCurrentSession().getTransaction().rollback();
  }
  
  @LifecycleClass(uri = "http://www.xchain.org/security/1.0")
  public static class TestIdentityService implements IdentityService {

    private static final long serialVersionUID = 1L;
    private static boolean started = false;
    private static boolean cleanup = false;
    
    private Map<Principal, Identity> identities;
    private Identity currentIdentity;

    public TestIdentityService() {
      identities = new HashMap<Principal, Identity>();
      for(User user : userList) {
        IdentityImpl identity = new IdentityImpl();
        identity.setPrincipal(new UsernamePrincipal(user.getUsername()));
        identities.put(identity.getPrincipal(), identity);
      }
    }

    public Identity getIdentity() {
      return currentIdentity;
    }

    public void loggedIn(Principal principal) throws SecurityException {
      currentIdentity = identities.get(principal);
    }

    public void loggedOut() {
      currentIdentity = null;
    }
    
    @StartThreadStep(localName = "criteria-test-identity-service", before = DefaultIdentityService.IDENTITY_LIFECYCLE_STEP_NAME)
    public static void startStep(ThreadContext foo) {
      log.debug("Starting lifecycle step: test-identity-service");
      if( foo.getClass().equals(TestThreadContext.class) ) {
        synchronized( TestIdentityService.class ) {
          if( !started ) {
            log.debug("Starting TestIdentityService.");
            if( IdentityManager.instance().getIdentityService() == null ) {
              log.debug("Installing TestIdentityService.");
              IdentityManager.instance().setIdentityService(new TestIdentityService());
              cleanup = true;
            }
            started = true;
          }
        }
      }
    }

    @StopThreadStep(localName = "criteria-test-identity-service")
    public static void stopStep(ThreadContext foo) {
      log.debug("Stopping lifecycle step: test identity service.");
      if( foo.getClass().equals(TestThreadContext.class) ) {
        synchronized( TestIdentityService.class ) {
          if( started ) {
            log.debug("Stopping TestIdentityService.");
            if( cleanup ) {
              IdentityManager.instance().setIdentityService(null);
              cleanup = false;
            }
            started = false;
          }
        }
      }
    }
    
    @Override
    public boolean equals(Object o) {
      return o.getClass().equals(getClass());
    }

  }
  
  public static class TestThreadContext extends ThreadContext {

  }
}
