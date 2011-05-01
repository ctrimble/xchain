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
package org.xchain.framework.osgi;

import org.xchain.framework.lifecycle.CCLPolicy;
import org.xchain.framework.lifecycle.LifecycleClass;

/**
 * A context class loader policy that will set the context class loader to be the class loader that loaded XChain. This
 * will have the effect that, when XChain is embedded in an application bundle, the application bundle's class loader
 * will always be the context class loader.
 *  
 * @author John Trimble
 */
public class OSGiCCLPolicy implements CCLPolicy {
  
  /** The old context class loader for this thread. */
  private ThreadLocal<CCLState> cclStateTl = new ThreadLocal<CCLState>();
  
  public void bindCCL() { 
    assertCCLNotBound();
    CCLState cclState = new CCLState(Thread.currentThread().getContextClassLoader(), LifecycleClass.class.getClassLoader());
    cclStateTl.set(cclState);
    Thread.currentThread().setContextClassLoader(cclState.boundCCL);
  }

  public void unbindCCL() {
    assertCCLBound();
    CCLState cclState = cclStateTl.get();
    Thread.currentThread().setContextClassLoader(cclState.previousCCL);
    cclStateTl.remove();
  }

  private void assertCCLNotBound() {
    if( isCCLBound() )
      throw new IllegalStateException("Context Class Loader is already bound.");
  }
  
  private void assertCCLBound() { 
    if( !isCCLBound() )
      throw new IllegalStateException("Context class loader is not bound.");
  }
  
  private class CCLState {
    ClassLoader previousCCL;
    ClassLoader boundCCL;
    
    CCLState(ClassLoader previousCCL, ClassLoader boundCCL) {
      this.previousCCL = previousCCL;
      this.boundCCL = boundCCL;
    }
  }

  public boolean isCCLBound() {
    return this.cclStateTl.get() != null;
  }
}
