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

/**
 * The <code>AllPermission</code> implies everything. It will return <code>true</code> for all <code>Permission</code>s. It is intended for superusers.
 * 
 * @author Jason Rose
 * @author Christian Trimble
 */
public class AllPermission implements Permission {

  private static final long serialVersionUID = -2752532150779555987L;
  private static final String toString = "Permission to do everything";

  /**
   * This will return the truth value of </code>permission != null</code>.
   * @param permission the <code>Permission</code> to check.
   * @return <code>true</code> if the parameter is not null; <code>false</code> otherwise.
   */
  public boolean implies(Permission permission) {
    return permission != null;
  }

  @Override
  public boolean equals(Object o) {
    boolean isEquals = false;
    if( o != null && o.getClass().equals(getClass()) ) {
      isEquals = true;
    }
    return isEquals;
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public String toString() {
    return toString;
  }

}
