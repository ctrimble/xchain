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

import java.io.Serializable;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.xchain.framework.security.DefaultIdentity;
import org.xchain.framework.security.Identity;
import org.xchain.framework.security.IdentityImpl;
import org.xchain.framework.security.IdentityService;
import org.xchain.framework.security.Permission;
import org.xchain.framework.security.UsernamePrincipal;

/**
 * @author Jason Rose
 * @author Christian Trimble
 * @author Josh Kennedy
 * @author Devon Tackett
 * @author John Trimble
 */
@Ignore
public class IdentityServiceTestImpl implements IdentityService {

  private Identity identity;
  private final Map<Principal, Identity> identityMap;
  private static final long serialVersionUID = 1L;
  
  public IdentityServiceTestImpl() {
    identityMap = new HashMap<Principal, Identity>();
    Identity guest = new DefaultIdentity();
    identityMap.put(guest.getPrincipal(), guest);
  }

  public Identity getIdentity(Serializable principal) {
    return identityMap.get(principal);
  }
  
  public Identity create(String name, Permission... permissions) {
    Principal principal = new UsernamePrincipal(name);
    IdentityImpl identity = new IdentityImpl();
    identity.setPrincipal(principal);
    identity.getPermissions().addAll(Arrays.asList(permissions));
    identityMap.put(principal, identity);
    return identity;
  }

  public Identity getIdentity() {
    return identity;
  }

  public void loggedIn(Principal principal) throws SecurityException {
    Identity identity = identityMap.get(principal);
    if( identity == null ) {
      throw new SecurityException(String.format("There is no Identity with principal '%s'.", principal.getName()));
    }
    this.identity = identity;
  }

  public void loggedOut() {
    this.identity = null;
  }

}
