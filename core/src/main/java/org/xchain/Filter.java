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
 * The interface for JXPathContext filters.  Filters extend command and provide a hook, postProcess(JXPathContext, Exception),
 * to clean up the command after it finishes executing. This method will be called once for every call to execute on the command.  
 * 
 * If the parent command encounters an exception the postProcess will be invoked on any Filters that have run.  Each filter will have
 * a chance to handle the exception in the post process.  If any filter inside the parent command handles the exception then execution
 * will continue to the next sibling of the parent command.  If no filter handles the exception then the exception will be thrown to
 * the parent command's parent.
 *
 * @author Devon Tackett
 * @author Christian Trimble
 */
public interface Filter
  extends Command
{
	/**
	 * Invoked after the parent command has completed execution or an exception is encountered.
	 * 
	 * @param context The current context for the command chain.
	 * @param exception The exception the parent command encountered.  Null if no exception was encountered.
	 * 
	 * @return Whether the given exception was handled. If no exception was encountered, the result is ignored.
	 */
  public boolean postProcess( JXPathContext context, Exception exception );
}
