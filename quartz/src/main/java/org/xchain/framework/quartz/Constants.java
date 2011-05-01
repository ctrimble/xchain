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

import javax.xml.namespace.QName;

/**
 * <p>The constants for the Quartz package.</p>
 *
 * @author Christian Trimble
 */
public class Constants
{
  /**
   * <p>The resource name of the default quartz configuration file.</p>
   */
  public static final String DEFAULT_QUARTZ_CONFIG     = "META-INF/quartz.xml";

  /**
   * <p>The resource name of the default quartz jobs file.</p>
   */
  public static final String DEFAULT_QUARTZ_JOB_CONFIG = "META-INF/quartz-jobs.xml";

  /**
   * <p>The namespace URI of the configuration elements for the quartz lifecycle.</p>
   */
  public static final String CONFIG_URI                = "http://www.xchain.org/framework/quartz-config";

  /**
   * <p>The namespace URI of the quartz lifecycle.</p>
   */
  public static final String LIFECYCLE_URI             = "http://www.xchain.org/framework/quartz";

  /**
   * <p>The local name of the JobExecutionContext when executing a command from the CommandJob.</p>
   */
  public static final String JOB_EXECUTION_CONTEXT     = "context";
}
