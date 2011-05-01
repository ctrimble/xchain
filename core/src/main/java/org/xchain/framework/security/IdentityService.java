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

/**
 * The <code>IdentityService</code> defines a pluggable way to retrieve <code>Identity</code>s.  Applications who want a strategy different than the <code>DefaultIdentityService</code> should create their own service and preempt the default with their own <code>@ThreadStartStep</code> and <code>@ThreadStopStep</code> annotated methods.
 *
 * @author Jason Rose
 * @author Christian Trimble
 */
public interface IdentityService extends Serializable {
  /**
   * Retrieves the currently authenticated <code>Identity</code>.
   * @return The authenticated <code>Identity</code>.
   */
  Identity getIdentity();

  /**
   * Stores the <code>Identity</code> for the already-authenticated <code>Principal</code>.  Use <code>getIdentity()</code> to retrieve it.
   * @param principal A unique identifier for an <code>Identity</code>.
   * @throws SecurityException if there is a problem with the <code>Identity</code> retrieval.
   */
  void loggedIn(Principal principal) throws SecurityException;

  /**
   * Flushes the authenticated <code>Identity</code>.
   */
  void loggedOut();
}
