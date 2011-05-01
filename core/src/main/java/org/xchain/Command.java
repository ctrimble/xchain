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
package org.xchain;

import org.apache.commons.jxpath.JXPathContext;

/**
 * The interface for JXPathContext commands.  A command defines a single method, execute(JXPathContext), that returns
 * true if the command handled the request.
 *
 * @author Christian Trimble
 */
public interface Command
{
  /**
   * Executes this command.  Returns true if this command completed handling this request, false otherwise.
   */
  public boolean execute( JXPathContext context )
    throws Exception;
}
