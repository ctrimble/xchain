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

/**
 * The <code>UsernamePrincipal</code> takes a name via the constructor and stores it.
 * 
 * @author Jason Rose
 * @author Christian Trimble
 * @author Josh Kennedy
 * @author Devon Tackett
 * @author John Trimble
 */
public class UsernamePrincipal implements Principal {

  private String name;

  public UsernamePrincipal(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
  
  @Override
  public String toString() {
    return String.format("Principal '%s'", getName());
  }
  
  @Override
  public boolean equals(Object o) {
    boolean equals = false;
    if( o != null && o.getClass().equals(getClass()) ) {
      UsernamePrincipal p = (UsernamePrincipal) o;
      equals = getName().equals(p.getName());      
    }
    return equals;
  }
  
  @Override
  public int hashCode() {
    return getName().hashCode() + getClass().hashCode();
  }

}
