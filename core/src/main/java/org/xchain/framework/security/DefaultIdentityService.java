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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.framework.lifecycle.LifecycleClass;
import org.xchain.framework.lifecycle.StartThreadStep;
import org.xchain.framework.lifecycle.StopThreadStep;
import org.xchain.framework.lifecycle.ThreadContext;
import org.xchain.namespaces.security.SecurityConstants;

/**
 * The <code>DefaultIdentityService</code> always returns the <code>DefaultIdentity</code>. It cannot perform authentication. Applications that want to support authentication and <code>Identity</code> management should implement <code>IdentityService</code> and install their own service in a
 * lifecycle step before this.
 * 
 * @author Jason Rose
 * @author Christian Trimble
 * @author Josh Kennedy
 */
@LifecycleClass(uri = SecurityConstants.URI)
public class DefaultIdentityService implements IdentityService {
  private static final long serialVersionUID = -6718252062432887675L;
  private static final Logger log = LoggerFactory.getLogger(DefaultIdentityService.class);
  private static ThreadLocal<DefaultIdentityService> defaultServiceTl = new ThreadLocal<DefaultIdentityService>();
  
  /**
   * Preempt this Service from installing itself by declaring a step to run before the one with this localName.
   */
  public static final String IDENTITY_LIFECYCLE_STEP_NAME = "default-identity-service";

  /**
   * Returns the default
   */
  public Identity getIdentity() {
    return new DefaultIdentity();
  }

  /**
   * Unsupported.
   */
  public void loggedIn(Principal principal) throws SecurityException {
    throw new UnsupportedOperationException("Cannot do authentication with the default identity service.");
  }

  /**
   * Unsupported.
   */
  public void loggedOut() {
    throw new UnsupportedOperationException("Cannot do authentication with the default identity service.");
  }

  @StartThreadStep(localName = IDENTITY_LIFECYCLE_STEP_NAME)
  public static void startThreadStep(ThreadContext context) {
    boolean installDefault = IdentityManager.instance().getIdentityService() == null;
    if( installDefault ) {
      DefaultIdentityService defaultService = new DefaultIdentityService();
      IdentityManager.instance().setIdentityService(defaultService);
      defaultServiceTl.set(defaultService);
    }
  }

  @StopThreadStep(localName = IDENTITY_LIFECYCLE_STEP_NAME)
  public static void stopThreadStep(ThreadContext context) {
    if( defaultServiceTl.get() != null ) {
      defaultServiceTl.remove();
      IdentityManager.instance().setIdentityService(null);
    }
  }

  @Override
  public boolean equals(Object o) {
    return o.getClass().equals(getClass());
  }

}
