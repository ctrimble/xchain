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
import static org.junit.Assert.fail;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Before;
import org.junit.Test;
import org.xchain.framework.security.AllPermission;
import org.xchain.framework.security.DefaultPrincipal;
import org.xchain.framework.security.Identity;
import org.xchain.framework.security.IdentityManager;
import org.xchain.framework.security.SecurityManager;
import org.xchain.framework.security.Permission;

/**
 * @author Jason Rose
 * @author Christian Trimble
 * @author Josh Kennedy
 * @author Devon Tackett
 * @author John Trimble
 */
public class PermissionTest {

  private static Logger log = LoggerFactory.getLogger(PermissionTest.class);

  @Before
  public void setup() throws Exception {
    IdentityManager identityManager = IdentityManager.instance();
    identityManager.setIdentityService(new IdentityServiceTestImpl());
  }

  @Test
  public void testGuestIdentity() throws Exception {
    IdentityManager.instance().getIdentityService().loggedIn(new DefaultPrincipal());
    try {
      SecurityManager.instance().checkPermission(new NoPermission());
      SecurityManager.instance().checkPermission(new AllPermission());
    } catch (SecurityException e) {
      log.error("Security Exception", e);
      fail();
    }
  }

  @Test
  public void testAllPermission() throws Exception {
    Identity admin = createIdentity(new AllPermission());
    IdentityManager.instance().getIdentityService().loggedIn(admin.getPrincipal());
    try {
      SecurityManager.instance().checkPermission(new NoPermission());
      SecurityManager.instance().checkPermission(new AllPermission());
    } catch (SecurityException e) {
      fail(e.getMessage());
    }
    try {
      SecurityManager.instance().checkPermission(null);
      fail();
    } catch (SecurityException e) {
    }
    assertEquals(new AllPermission(), new AllPermission());
    assertFalse(new AllPermission().equals(new NoPermission()));
  }

  @Test
  public void testImplication() throws Exception {
    Identity impliesNoPermissionIdentity = createIdentity(new ImpliesNoPermission());
    IdentityManager.instance().getIdentityService().loggedIn(impliesNoPermissionIdentity.getPrincipal());
    try {
      SecurityManager.instance().checkPermission(new NoPermission());
      SecurityManager.instance().checkPermission(new ImpliesNoPermission());
      impliesNoPermissionIdentity.getPermissions().add(new NoPermission());
      SecurityManager.instance().checkPermission(new NoPermission());
      SecurityManager.instance().checkPermission(new ImpliesNoPermission());
    } catch (SecurityException e) {
      log.error("Security Exception", e);
      fail();
    }
    Identity noPermissionIdentity = createIdentity(new NoPermission());
    IdentityManager.instance().getIdentityService().loggedOut();
    IdentityManager.instance().getIdentityService().loggedIn(noPermissionIdentity.getPrincipal());
    try {
      SecurityManager.instance().checkPermission(new NoPermission());
      SecurityManager.instance().checkPermission(new ImpliesNoPermission());
      fail();
    } catch (SecurityException e) {
      log.error("Security Exception", e);
    }
    Identity doesntImplyNoPermissionEntity = createIdentity(new DoesntImplyNoPermission());
    IdentityManager.instance().getIdentityService().loggedOut();
    IdentityManager.instance().getIdentityService().loggedIn(doesntImplyNoPermissionEntity.getPrincipal());
    try {
      SecurityManager.instance().checkPermission(new ImpliesNoPermission());
      fail();
    } catch (SecurityException e) {
      log.error("Security Exception", e);
    }
  }

  private Identity createIdentity(Permission... permissions) {
    Serializable id = System.currentTimeMillis();
    Identity identity = ((IdentityServiceTestImpl)IdentityManager.instance().getIdentityService()).create(id.toString(), permissions);
    return identity;
  }

  private static class NoPermission implements Permission {
    private static final long serialVersionUID = 1L;
    private static final String permission = "Permission to do nothing";

    public boolean implies(Permission permission) {
      return false;
    }

    public String toString() {
      return permission;
    }

  }
  
  private static class ImpliesNoPermission extends NoPermission {
    private static final long serialVersionUID = 1L;
    private static final String permission = "Implies Permission to do nothing";

    public boolean implies(Permission permission) {
      return permission instanceof NoPermission;
    }

    public String toString() {
      return permission;
    }
  }

  private static class DoesntImplyNoPermission implements Permission {
    private static final long serialVersionUID = 1L;
    private static final String permission = "Doesn't Imply Permission to do nothing";

    public boolean implies(Permission permission) {
      return !(permission instanceof NoPermission);
    }

    public String toString() {
      return permission;
    }
  }

  
}
