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
package org.xchain.framework.lifecycle;

/**
 * A Context Class Loader strategy that does not change the context class loader.
 *  
 * @author John Trimble
 */
public class NOPCCLPolicy implements CCLPolicy {
  ThreadLocal<Boolean> boundCCLTl = new ThreadLocal<Boolean>() {
    @Override
    protected Boolean initialValue() {
      return Boolean.FALSE;
    }
  };
  
  public void bindCCL() { 
    if( isCCLBound() )
      throw new IllegalStateException("Context class loader is already set.");
    boundCCLTl.set(Boolean.TRUE);
  }
  
  public void unbindCCL() { 
    if( !isCCLBound() )
      throw new IllegalStateException("Context class loader is not bound.");
    boundCCLTl.remove();
  }
  
  public boolean isCCLBound() { return boundCCLTl.get(); }
}
