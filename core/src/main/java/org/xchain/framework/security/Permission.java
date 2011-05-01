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

/**
 * A <code>Permission</code> represents what an <code>Identity</code> is allowed to do.
 * 
 * @author Jason Rose
 * @author Christian Trimble
 */
public interface Permission extends Serializable {

  /**
   * Implies is the mechanism by which we verify that an authenticated <code>Identity</code> can perform an action.
   * 
   * @param permission The operation being checked.
   * @return <code>true</code> if the Identity is able to perform this operation, or a superset of this operation's functionality; <code>false</code> otherwise.
   */
  boolean implies(Permission permission);
}
