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

import static org.junit.Assert.fail;

import java.security.Principal;

import org.junit.Test;

/**
 * @author Jason Rose
 * @author Christian Trimble
 * @author Josh Kennedy
 * @author Devon Tackett
 * @author John Trimble
 */
public class TestIdentityService {
  
  @Test
  public void testLoggedIn() {
    IdentityManager.instance().setIdentityService(new DefaultIdentityService());
    final Principal principal = new UsernamePrincipal("test");
    try {
      IdentityManager.instance().loggedIn(principal);
    } catch (UnsupportedOperationException e) {
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }
  
  @Test
  public void testLoggedOut() {
    IdentityManager.instance().setIdentityService(new DefaultIdentityService());
    try {
      IdentityManager.instance().loggedOut();
    } catch (UnsupportedOperationException e) {
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }
}
