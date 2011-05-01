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
import static org.junit.Assert.assertNotNull;

import java.security.Principal;

import org.junit.Test;

/**
 * @author Jason Rose
 */
public class TestPrincipal {

  @Test
  public void testDefaultPrincipalName() {
    final Principal defaultPrincipal = new DefaultPrincipal();
    assertNotNull(defaultPrincipal.getName());
  }
  
  @Test
  public void testDefaultPrincipalEquals() {
    final Principal defaultPrincipal = new DefaultPrincipal();
    assertFalse(defaultPrincipal.equals(null));
    assertFalse(defaultPrincipal.equals(new UsernamePrincipal(defaultPrincipal.getName())));
    assertEquals(defaultPrincipal, new DefaultPrincipal());
  }
  
  @Test
  public void testDefaultPrincipalToString() {
    assertNotNull(new DefaultPrincipal().toString());
  }
  
  @Test
  public void testUsernamePrincipalToString() {
    final String name = "test";
    final String wrongName = "foo";
    final Principal usernamePrincipal = new UsernamePrincipal(name);
    final Principal wrongPrincipal = new UsernamePrincipal(wrongName);
    final String toString = usernamePrincipal.toString();
    final String wrongString = wrongPrincipal.toString();
    assertNotNull(toString);
    assertFalse(toString.equals(wrongString));
  }
}
