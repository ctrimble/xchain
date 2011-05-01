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
import org.xchain.annotations.Function;
import org.xchain.framework.lifecycle.LifecycleClass;
import org.xchain.framework.lifecycle.LifecycleAccessor;
import org.xchain.namespaces.security.SecurityConstants;

/**
 * The <code>IdentityManager</code> contains a service that stores the authenticated <code>Identity</code> of the user of the application.  Services are stored per thread because the "user" of the application might change, as thread such as quartz will interact as a different <code>Identity</code>.
 *
 * @author Jason Rose
 * @author Christian Trimble
 */
@LifecycleClass(uri=SecurityConstants.URI)
public class IdentityManager {
  private static IdentityManager identityManager = new IdentityManager();
  private ThreadLocal<IdentityService> identityService;

  private IdentityManager() {
    identityService = new ThreadLocal<IdentityService>();
  }

  /**
   * Returns the singleton instance.
   */
  @LifecycleAccessor
  public static IdentityManager instance() {
    return identityManager;
  }

  /**
   * Returns the service used to load <code>Identity</code>s for this thread.
   * @return the service used to load <code>Identity</code>s for this thread.
   */
  public IdentityService getIdentityService() {
    return identityService.get();
  }

  public void setIdentityService(IdentityService identityService) {
    this.identityService.set(identityService);
  }
  
  /**
   * A convenience method for <code>getIdentityService().getIdentity()</code>.
   * @return The currently authenticated <code>Identity</code>, as supplied by the <code>IdentityService</code>.
   */
  @Function(localName="identity")
  public Identity getIdentity() {
    return getIdentityService().getIdentity();
  }
  
  /**
   * A convenience method for <code>getIdentityService().loggedIn(Principal)</code>.
   */
  @Function(localName="logged-in")
  public void loggedIn(Principal principal) {
    getIdentityService().loggedIn(principal);
  }
  
  /**
   * A convenience method for <code>getIdentityService().loggedOut()</code>.
   */
  @Function(localName="logged-out")
  public void loggedOut() {
    getIdentityService().loggedOut();
  }
  
  /**
   * A convenience method for <code>getIdentityService().getIdentity().getPrincipal()</code>.
   * @return The currently authenticated <code>Identity</code>'s <code>Principal</code>, as supplied by the <code>IdentityService</code>.
   */
  @Function(localName="principal")
  public Principal getPrincipal() {
    return getIdentity().getPrincipal();
  }
  
  /**
   * A convenience method for <code>getIdentityService().getIdentity().getPrincipal().getName()</code>.
   * @return The currently authenticated <code>Identity</code>'s <code>Principal</code>'s name, as supplied by the <code>IdentityService</code>.
   */
  @Function(localName="principal-name")
  public String getPrincipalName() {
    return getPrincipal().getName();
  }
}
