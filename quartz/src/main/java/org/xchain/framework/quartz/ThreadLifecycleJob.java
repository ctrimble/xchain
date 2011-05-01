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
package org.xchain.framework.quartz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.xchain.framework.lifecycle.ThreadLifecycle;
import org.xchain.framework.lifecycle.LifecycleException;

/**
 * The base class for XChain jobs.  This job will start the thread lifecycle and create a JXPathContext
 * to execute inside.
 *
 * @author Christian Trimble
 * @author Josh Kennedy
 */
public abstract class ThreadLifecycleJob
  implements Job
{
  private static Logger log = LoggerFactory.getLogger(ThreadLifecycleJob.class);

  /**
   * <p>Starts the thread lifecycle, Calls executeInThreadContext(JobExecutionContext), and then cleans up the thread lifecycle.</p>
   */
  public final void execute(JobExecutionContext quartzContext)
    throws JobExecutionException
  {
    JobThreadContext threadContext = new JobThreadContext(quartzContext);
    try {
      ThreadLifecycle.getInstance().startThread(threadContext);
      executeInThreadLifecycle(quartzContext);
    }
    catch( LifecycleException le ) {
      throw new JobExecutionException("Could not start the quartz job thread due to an exception.", le);
    }
    finally {
      try {
        ThreadLifecycle.getInstance().stopThread(threadContext);
      }
      catch( LifecycleException le ) {
        if( log.isWarnEnabled() ) {
          log.warn("An exception was thrown while cleaning up a thread.", le);
        }
      }
    }
  }

  /**
   * <p>Executes the quartz job inside of an xchains thread lifecyce.
   */
  public abstract void executeInThreadLifecycle( JobExecutionContext quartzContext )
    throws JobExecutionException;
}
