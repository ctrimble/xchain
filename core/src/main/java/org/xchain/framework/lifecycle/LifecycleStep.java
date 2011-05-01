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

import javax.xml.namespace.QName;

/**
 * Interface for LifecycleSteps.
 *
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Devon Tackett
 * @author John Trimble
 */
interface LifecycleStep
{
  /**
   * Invoked when the Lifecycle is starting.
   * 
   * @param context The current LifecycleContext.
   * 
   * @throws LifecycleException If an exception is encountered starting this Lifecycle step.
   */
  public void startLifecycle(LifecycleContext context, ConfigDocumentContext configDocContext)
    throws LifecycleException;

  /**
   * Invoked when the Lifecycle is ending.
   * 
   * @param context The current LifecycleContext.
   */
  public void stopLifecycle(LifecycleContext context);

  public QName getQName();
  
}
