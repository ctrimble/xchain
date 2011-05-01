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

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xchain.framework.security.AllPermission;
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
public class SecurityManagerTest {

  private static Logger log = LoggerFactory.getLogger(SecurityManagerTest.class);

  @Before
  public void setup() throws Exception {
    IdentityManager.instance().setIdentityService(new IdentityServiceTestImpl());
  }
  
  @After
  public void after() throws Exception {
    IdentityManager.instance().setIdentityService(null);
  }

  @Test
  public void testNullIdentity() throws Exception {
    IdentityManager.instance().getIdentityService().loggedOut();
    try {
      SecurityManager.instance().checkPermission(new AllPermission());
      fail();
    } catch (Exception e) {
      log.error("Unhandled Exception", e);
    }
  }
  
  private Identity createIdentity(Permission... permissions) {
    Serializable id = System.currentTimeMillis();
    Identity identity = ((IdentityServiceTestImpl)IdentityManager.instance().getIdentityService()).create(id.toString(), permissions);
    return identity;
  }
  
}
