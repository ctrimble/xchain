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

import org.quartz.JobExecutionContext;
import org.xchain.framework.lifecycle.ThreadContext;

/**
 * <p>The thread context object for commands being executed by quartz.</p>
 *
 * @author Christian Trimble
 */
public class JobThreadContext
  extends ThreadContext
{
  private JobExecutionContext jobExecutionContext = null;

  /**
   * <p>Constructs a new JobThreadContext for the specified JobExecutionContext.</p>
   */
  public JobThreadContext( JobExecutionContext jobExecutionContext )
  {
    this.jobExecutionContext = jobExecutionContext;
  }

  /**
   * <p>Returns the JobExecutionContext for job that is executing in this thread.</p>
   * @rerturn the JobExecutionContext for the job that is executing in this thread.
   */
  public JobExecutionContext getJobExecutionContext()
  {
    return this.jobExecutionContext;
  }
}
