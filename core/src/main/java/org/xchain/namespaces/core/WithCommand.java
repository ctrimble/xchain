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

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.framework.lifecycle.Execution;
import org.xchain.impl.ChainImpl;

/**
 * <p>
 * The <code>with</code> command will execute its child commands with the selected element as the root context.
 * </p>
 * <code class="source">
 * &lt;xchain:with xmlns:xchain="http://www.xchain.org/core/1.0" select="/some/xpath"&gt;
 *   ...
 * &lt;/xchain:with&gt;
 * </code>
 *
 * @author Devon Tackett
 * @author Christian Trimble
 */
@Element(localName="with")
public abstract class WithCommand
  extends ChainImpl
{
  /**
   * The XPath use as the root context.
   */
  @Attribute(localName="select", type=AttributeType.JXPATH_POINTER)
  public abstract Pointer getSelect( JXPathContext context );
  public abstract boolean hasSelect();

  public boolean execute( JXPathContext context )
    throws Exception
  {
    Pointer contextBean = getSelect(context);

    // Create a new context with the selected pointer as the contextBean.
    JXPathContext newContext = Execution.startContextPointer(context, contextBean);

    boolean result = false;

    try {
      // execute the children.
      result = super.execute(newContext);
    } finally {
      // Stop the local context.
      Execution.stopContextPointer(newContext);
    }

    // return the result of the children.
    return result;
  }	  
}
