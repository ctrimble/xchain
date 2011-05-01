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
package org.xchain.namespaces.core;

import org.xchain.Command;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;

import org.apache.commons.jxpath.JXPathContext;

/**
 * <p>A command that can stop the execution of an xchain by reporting that it handled the request.  The command will return true when its test
 * condition is true.</p>
 *
 * <code class="source">
 *   &lt;xchain:chain xmlns:xchain="http://www.xchain.org/core/1.0"&gt;
 *     &lt;xchain:handled test="true()"/&gt;
 *     &lt;-- A command here will never execute --&gt;
 *   &lt;/xchain:chain&gt;
 * </code>
 *
 * @author Christian Trimble
 */
@Element(localName="handled")
public abstract class HandledCommand
  implements Command
{
  @Attribute(localName="test", type=AttributeType.JXPATH_VALUE)
  public abstract Boolean getTest( JXPathContext context );

  public boolean execute( JXPathContext context )
    throws Exception
  {
    return getTest(context);
  }
}
