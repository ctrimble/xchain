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

import org.quartz.StatefulJob;

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
 *    &lt;job-class&gt;org.xchain.framework.quartz.StatefulCommandJob&lt;/job-class&gt;
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
 * @author Jason Rose
 *
 * @see javax.xml.namespace.QName#valueOf(String)
 */
public class StatefulCommandJob
  extends CommandJob implements StatefulJob
{
}
