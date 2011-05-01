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
 * Interface for ThreadSteps.
 *
 * @author Christian Trimble
 */
interface ThreadStep
{
  /**
   * Invoked when the Thread is starting.
   * 
   * @param context The current ThreadContext.
   * 
   * @throws LifecycleException If an exception is encountered starting this thread step.
   */
  public void startThread(ThreadContext context)
    throws LifecycleException;

  /**
   * Invoked when the Thread is ending.
   * 
   * @param context The current ThreadContext.
   */
  public void stopThread(ThreadContext context);

  public QName getQName();
  
}
