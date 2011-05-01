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
import java.util.HashSet;
import java.util.Set;

/**
 * The <code>IdentityImpl</code> is a base implementation of <code>Identity</code>.  It has no <code>Permission</code>s.
 * 
 * @author Christian Trimble
 * @author Jason Rose
 */
public class IdentityImpl implements Identity {

  private static final long serialVersionUID = 3974694797086401714L;
  
  private Set<Permission> permissions;
  private Principal principal;

  /**
   * Creates an Identity with no permissions.
   */
  public IdentityImpl() {
    permissions = new HashSet<Permission>();
  }

  public Set<Permission> getPermissions() {
    return permissions;
  }

  public Principal getPrincipal() {
    return principal;
  }

  public void setPermissions(Set<Permission> permissions) {
    if( permissions == null ) {
      throw new IllegalArgumentException("Permissions cannot be null.");
    } else {
      this.permissions = permissions;
    }
  }

  public void setPrincipal(Principal principal) {
    this.principal = principal;
  }
  
  @Override
  public boolean equals(Object o) {
    boolean isEquals = false;
    if( o != null && o.getClass().equals(getClass()) ) {
      IdentityImpl i = (IdentityImpl) o;
      isEquals = (getPrincipal() == null && i.getPrincipal() == null || getPrincipal() != null && getPrincipal().equals(i.getPrincipal())) && getPermissions().containsAll(i.getPermissions()) && i.getPermissions().containsAll(getPermissions());
    }
    return isEquals;
  }
  
  @Override
  public String toString() {
    String principal = null;
    if( getPrincipal() != null ) {
      principal = getPrincipal().getName();
    }
    return String.format("Identity '%s'", principal);
  }
  
  @Override
  public int hashCode() {
    int hashcode = getClass().hashCode();
    if( getPrincipal() != null ) {
      hashcode += getPrincipal().hashCode();
    }
    for( Permission permission : getPermissions() ) {
      hashcode += permission.hashCode();
    }
    return hashcode;
  }

}
