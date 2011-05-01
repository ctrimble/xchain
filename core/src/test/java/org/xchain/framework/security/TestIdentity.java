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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * @author Jason Rose
 */
public class TestIdentity {

  @Test
  public void testDefaultIdentity() throws Exception {
    DefaultIdentity identity = new DefaultIdentity();
    assertEquals(identity, new DefaultIdentity());
    assertFalse(identity.equals(new IdentityImpl()));
    try {
      identity.setPermissions(null);
      fail();
    } catch (UnsupportedOperationException e) {
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }
  
  @Test
  public void testIdentityImpl() throws Exception {
    IdentityImpl impl = new IdentityImpl();
    assertEquals(impl, new IdentityImpl());
    assertFalse(impl.equals(new DefaultIdentity()));
    assertFalse(impl.equals(null));
    try {
      impl.setPermissions(null);
    } catch (IllegalArgumentException e) {
    } catch (Throwable t) {
      fail(t.getMessage());
    }
    Set<Permission> permissions = new HashSet<Permission>();
    try {
      impl.setPermissions(permissions);
    } catch (Throwable t) {
      fail(t.getMessage());
    }
  }
  
  @Test
  public void testIdentityImplEquals() throws Exception {
    IdentityImpl impl = new IdentityImpl();
    IdentityImpl impl2 = new IdentityImpl();
    assertEquals(impl, impl2);
    assertEquals(impl, impl);
    assertEquals(impl2, impl);
    impl.setPrincipal(new UsernamePrincipal("test"));
    assertFalse(impl.equals(impl2));
    assertFalse(impl2.equals(impl));
    assertTrue(impl.equals(impl));
    impl2.setPrincipal(new UsernamePrincipal("test"));
    assertEquals(impl, impl2);
    assertEquals(impl2, impl);
    assertEquals(impl, impl);
    impl.getPermissions().add(new AllPermission());
    assertFalse(impl.equals(impl2));
    assertFalse(impl2.equals(impl));
    assertTrue(impl.equals(impl));
    impl2.getPermissions().add(new AllPermission());
    assertEquals(impl, impl2);
    assertEquals(impl2, impl);
    assertEquals(impl, impl);
  }
  
  @Test
  public void testIdentityImplHashcode() throws Exception {
    IdentityImpl impl = new IdentityImpl();
    IdentityImpl impl2 = new IdentityImpl();
    assertEquals(impl.hashCode(), impl2.hashCode());
    impl.setPrincipal(new UsernamePrincipal("test"));
    assertFalse(impl.hashCode() == impl2.hashCode());
    impl2.setPrincipal(new UsernamePrincipal("test"));
    assertEquals(impl.hashCode(), impl2.hashCode());
    impl.getPermissions().add(new AllPermission());
    assertFalse(impl.hashCode() == impl2.hashCode());
    impl2.getPermissions().add(new AllPermission());
    assertEquals(impl.hashCode(), impl2.hashCode());
  }
  
  @Test
  public void testIdentityImplToString() throws Exception {
    IdentityImpl impl = new IdentityImpl();
    final String noPrincipalString = impl.toString();
    impl.setPrincipal(new UsernamePrincipal("foo"));
    final String principalString = impl.toString();
    assertFalse(noPrincipalString.equals(principalString));
  }
}
