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

import java.util.HashMap;
import javax.xml.namespace.QName;
import org.apache.commons.jxpath.JXPathContext;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobDataMap;
import org.xchain.Command;
import org.xchain.CatalogNotFoundException;
import org.xchain.CommandNotFoundException;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.framework.jxpath.QNameVariables;

/**
 * <p>A Quartz job that executes an XChain command.  This job takes takes the following entries in the &lt;job-data-map&gt;:</p>
 * <table border="1">
 *   <tr>
 *     <th align="left">Key</th>
 *     <th align="left">Type</th>
 *     <th align="left">Description</th>
 *   </tr>
 *   <tr>
 *     <td>catalog-system-id</td>
 *     <td>URL</td>
 *     <td>The URL of the catalog that contains the command.</td>
 *   </tr>
 *   <tr>
 *     <td>command-name</td>
 *     <td>QName</td>
 *     <td>The QName of the command to execute.  See the {@link javax.xml.namespace.QName#valueOf(String)} method for the format of this field.</td>
 *   </tr>
 * </table>
 *
 * <p>Example Configuration:<p/>
 * <pre>
 *  &lt;job-detail&gt;
 *    &lt;name&gt;JOB_NAME&lt;/name&gt;
 *    &lt;group&gt;JOB_GROUP&lt;/group&gt;
 *    &lt;description&gt;JOB_DESCRIPTION&lt;/description&gt;
 *    &lt;job-class&gt;org.xchain.framework.quartz.CommandJob&lt;/job-class&gt;
 *    &lt;job-data-map allows-transient-data="false"&gt;
 *      &lt;entry&gt;
 *        &lt;key&gt;catalog-system-id&lt;/key&gt;
 *        &lt;value&gt;URL&lt;/value&gt;
 *      &lt;/entry&gt;
 *      &lt;entry&gt;
 *        &lt;key&gt;command-name&lt;/key&gt;
 *        &lt;value&gt;{NAMESPACE_URI}LOCAL_NAME&lt;/value&gt;
 *      &lt;/entry&gt;
 *    &lt;/job-data-map&gt;
 *  &lt;/job-detail&gt;
 * </pre>
 *
 * @author Christian Trimble
 *
 * @see javax.xml.namespace.QName#valueOf(String)
 */
public class CommandJob
  extends ThreadLifecycleJob
{
  /**
   * <p>The key of the job data map entry for the command name.</p>
   */
  public static final String COMMAND_NAME = "command-name";
  /**
   * <p>The key of the job data map entry for the catalog system id.</p>
   */
  public static final String CATALOG_SYSTEM_ID = "catalog-system-id";

  /**
   * <p>Loads the job specified in the job data map and executes it.</p>
   * @param jobContext the context of the job execution.
   */
  public void executeInThreadLifecycle( JobExecutionContext jobContext )
    throws JobExecutionException
  {
    JobDataMap dataMap = jobContext.getJobDetail().getJobDataMap();

    String catalogName = getCatalogSystemId(jobContext);
    QName commandName = getCommandName(jobContext);
    Command command = getCommand(catalogName, commandName);
    JXPathContext commandContext = createJXPathContext( jobContext );

    try {
      command.execute(commandContext);
    }
    catch( Exception e ) {
      throw new JobExecutionException("The command '"+commandName+"' in the catalog '"+catalogName+"' threw an exception.", e);
    }
  }

  /**
   * <p>Creates a JXPathContext for a JobExecutionContext.</p>
   * @param jobContext the job execution context for which the jxpath context will be created.
   * @return the JXPathContext created for the job execution context.
   */
  private JXPathContext createJXPathContext( JobExecutionContext jobContext )
  {
    JXPathContext commandContext = JXPathContext.newContext( new HashMap() );
    ((QNameVariables)commandContext.getVariables()).declareVariable( new QName( Constants.LIFECYCLE_URI, Constants.JOB_EXECUTION_CONTEXT ), jobContext );
    return commandContext;
  }

  /**
   * <p>Gets the command name out of the JobExecutionContext's JobDataMap.</p>
   */
  private QName getCommandName( JobExecutionContext jobContext )
    throws JobExecutionException
  {
    JobDataMap dataMap = jobContext.getJobDetail().getJobDataMap();

    // get the name of the command.
    String commandNameString = dataMap.getString(COMMAND_NAME);
    QName commandName = null;

    if( commandNameString == null ) {
      throw new JobExecutionException("The command name was not specified in the job data map key 'command'.");
    }

    try {
      commandName = QName.valueOf(commandNameString);
    }
    catch( Exception e ) {
      throw new JobExecutionException("The command name '"+commandNameString+"' is not a valid QName.", e);
    }
    return commandName;
  }

  /**
   * <p>Gets the catalog name from the JobExecutionContext's JobDataMap.</p>
   */
  private String getCatalogSystemId( JobExecutionContext jobContext )
    throws JobExecutionException
  {
    JobDataMap dataMap = jobContext.getJobDetail().getJobDataMap();

    // get the name of the catalog.
    String catalogName = dataMap.getString(CATALOG_SYSTEM_ID);

    if( catalogName == null ) {
      throw new JobExecutionException("The catalog name was not specifed in the job data map key 'catalog'.");
    }

    return catalogName;
  }

  /**
   * <p>Gets the command for the catalog name and command name.</p>
   */
  private Command getCommand( String catalogName, QName commandName )
    throws JobExecutionException
  {
    Command command = null;

    try {
      command = CatalogFactory.getInstance().getCatalog(catalogName).getCommand(commandName);
    }
    catch( CatalogNotFoundException catalogNotFound ) {
      throw new JobExecutionException("The catalog '"+catalogName+"' could not be found.", catalogNotFound );
    }
    catch( CommandNotFoundException commandNotFound ) {
      throw new JobExecutionException("The command '"+commandName+"' in the catalog '"+catalogName+"' could not be found.", commandNotFound);
    }
    catch( Exception e ) {
      throw new JobExecutionException("The command '"+commandName+"' in the catalog '"+catalogName+"' could not be loaded, due to an exception.", e);
    }

    return command;
  }
}
