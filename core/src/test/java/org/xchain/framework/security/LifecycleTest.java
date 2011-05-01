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
package org.xchain.framework.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xchain.framework.lifecycle.Lifecycle;
import org.xchain.framework.lifecycle.LifecycleClass;
import org.xchain.framework.lifecycle.LifecycleException;
import org.xchain.framework.lifecycle.StartThreadStep;
import org.xchain.framework.lifecycle.StopThreadStep;
import org.xchain.framework.lifecycle.ThreadContext;
import org.xchain.framework.lifecycle.ThreadLifecycle;

/**
 * @author Jason Rose
 * @author Christian Trimble
 * @author Josh Kennedy
 */
public class LifecycleTest {

  public static final Logger log = LoggerFactory.getLogger(LifecycleTest.class);

  @Before
  public void setup() throws LifecycleException {
    Lifecycle.startLifecycle();
    IdentityManager.instance().setIdentityService(null);
  }

  @After
  public void tearDown() throws LifecycleException {
    Lifecycle.stopLifecycle();
    try {
      ThreadLifecycle.getInstance().stopThread(null);
    } catch (Exception e) { }
    try {
      ThreadLifecycle.getInstance().stopLifecycle(null);
    } catch (Exception e) { }
    IdentityManager.instance().setIdentityService(null);
  }

  @Test
  public void testThreadLifecycle() throws Exception {
    ThreadLifecycle.getInstance().startLifecycle(Lifecycle.getLifecycleContext());
    try {
      assertFalse(ThreadLifecycle.getInstance().inThread());
      ThreadContext context = new ThreadContext();
      ThreadLifecycle.getInstance().startThread(context);
      try {
        assertTrue(ThreadLifecycle.getInstance().inThread());
      }
      finally {
        ThreadLifecycle.getInstance().stopThread(context);
      }
    }
    finally {
      ThreadLifecycle.getInstance().stopLifecycle(Lifecycle.getLifecycleContext());
    }
    assertFalse(ThreadLifecycle.getInstance().inThread());
  }

  @Test
  public void testDefaultIdentityService() throws Exception {
    assertNull(IdentityManager.instance().getIdentityService());
    ThreadLifecycle.getInstance().startLifecycle(Lifecycle.getLifecycleContext());
    try {
      ThreadContext context = new ThreadContext();
      ThreadLifecycle.getInstance().startThread(context);
      try {
        assertEquals(IdentityManager.instance().getIdentityService(), new DefaultIdentityService());
      }
      finally {
        ThreadLifecycle.getInstance().stopThread(context);
      }
    } finally {
      ThreadLifecycle.getInstance().stopLifecycle(Lifecycle.getLifecycleContext());
    }
    assertNull(IdentityManager.instance().getIdentityService());
  }

  @Test
  public void testPreemptIdentityService() throws Exception {
    assertNull(IdentityManager.instance().getIdentityService());
    ThreadLifecycle.getInstance().startLifecycle(Lifecycle.getLifecycleContext());
    ThreadContext context = new TestThreadContext();
    ThreadLifecycle.getInstance().startThread(context);
    assertEquals(IdentityManager.instance().getIdentityService(), new TestIdentityService());
    ThreadLifecycle.getInstance().stopThread(context);
    ThreadLifecycle.getInstance().stopLifecycle(Lifecycle.getLifecycleContext());
    assertNull(IdentityManager.instance().getIdentityService());
  }

  @LifecycleClass(uri = "http://www.xchain.org/security/1.0")
  // this class preempts the default identity service if the context is of the correct type
  public static class TestIdentityService implements IdentityService {

    private static final long serialVersionUID = 1L;
    private static boolean started = false;
    private static boolean cleanup = false;

    public Identity getIdentity() {
      return new IdentityImpl();
    }

    public void loggedIn(Principal principal) throws SecurityException {
    }

    public void loggedOut() {
    }

    @StartThreadStep(localName = "test-identity-service", before = DefaultIdentityService.IDENTITY_LIFECYCLE_STEP_NAME)
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

    @StopThreadStep(localName = "test-identity-service")
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
