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
import java.util.Set;

/**
 * An <code>Identity</code> represents an authenticated user. <code>Identity</code>s have a <code>Set</code> of <code>Permission</code>s that dictate what actions they are allowed to perform.
 * 
 * @author Jason Rose
 * @author Christian Trimble
 */
public interface Identity extends Serializable {
  /**
   * Returns a unique identifier for this <code>Identity</code>.
   * @return A unique identifier for this <code>Identity</code>.
   */
  Principal getPrincipal();

  /**
   * Returns all the <code>Permission</code>s that this <code>Identity</code> has.
   * @return The <code>Set</code> of <code>Permission</code>s this <code>Identity</code> has.
   */
  Set<Permission> getPermissions();
}
