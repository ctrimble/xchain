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

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>SecurityManager</code> allows the application to verify that a specified <code>Identity</code> has a <code>Permission</code> that will allow the specified <code>Permission</code>.  It throws a <code>SecurityException</code> if the action is invalid.
 * 
 * @author Jason Rose
 * @author Christian Trimble
 * @author Josh Kennedy
 */
public class SecurityManager {

  private static SecurityManager securityManager = new SecurityManager();
  private static final Logger log = LoggerFactory.getLogger(SecurityManager.class);

  private SecurityManager() {
  }

  public static SecurityManager instance() {
    return securityManager;
  }

  /**
   * Checks the <code>Permission</code> against the current <code>Identity</code>, as provided by the <code>IdentityManager</code>.
   * @throws SecurityException if the <code>Identity</code> doesn't have permission.
   */
  public void checkPermission(Permission permission) {
    Identity identity = IdentityManager.instance().getIdentityService().getIdentity();
    if( log.isDebugEnabled() ) {
      log.debug(String.format("Checking if identity '%s' has permission '%s'.", identity, permission));
    }
    boolean hasPermission = false;
    Set<Permission> permissions = identity.getPermissions();
    if( permissions != null ) {
      for( Permission identityPermission : permissions ) {
        if( identityPermission.implies(permission) ) {
          hasPermission = true;
          break;
        }
      }
    }
    if( !hasPermission ) {
      throw new SecurityException(String.format("Identity '%s' does not have permission to do '%s'.", identity, permission));
    }
  }

  public boolean hasPermission(Permission permission)
  {
    Identity identity = IdentityManager.instance().getIdentityService().getIdentity();
    boolean hasPermission = false;
    Set<Permission> permissions = identity.getPermissions();
    if( permissions != null ) {
      for( Permission identityPermission : permissions ) {
        if( identityPermission.implies(permission) ) {
          return true;
        }
      }
    }
    return false;
  }
}
