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

import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The <code>DefaultIdentity</code> is an <code>Identity</code> with the <code>AllPermission</code>. It <code>implies</code> everything, which allows the Security API to work out of the box. Applications should either directly implement <code>Identity</code> or subclass <code>IdentityImpl</code> for
 * more functionality.
 * 
 * @author Jason Rose
 * @author Christian Trimble
 */
public final class DefaultIdentity extends IdentityImpl {

  private static final long serialVersionUID = 3974694797086401714L;

  /**
   * Creates an <code>Identity</code> with the permission to do everything.
   */
  public DefaultIdentity() {
    Set<Permission> permissions = new HashSet<Permission>();
    permissions.add(new AllPermission());
    super.setPermissions(Collections.unmodifiableSet(permissions));
    Principal guestPrincipal = new DefaultPrincipal();
    setPrincipal(guestPrincipal);
  }

  /**
   * Unsupported.
   */
  @Override
  public void setPermissions(Set<Permission> permissions) {
    throw new UnsupportedOperationException("Cannot set permissions on this Identity.");
  }

}
