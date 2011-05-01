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

import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.framework.lifecycle.Execution;

/**
 * The <code>for-each</code> will execute its child commands for each element selected by the <code>select</code> attribute.
 *
 * <code class="source">
 * &lt;xchain:for-each xmlns:xchain="http://www.xchain.org/core/1.0" select="/some/xpath"&gt;
 *   ...
 * &lt;/xchain:for-each&gt;
 * </code>
 * <code class="source">
 * &lt;xchain:for-each xmlns:xchain="http://www.xchain.org/core/1.0" select-nodes="/some/xpath"&gt;
 *   ...
 * &lt;/xchain:for-each&gt;
 * </code>
 *
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
@Element(localName="for-each")
public abstract class ForEachCommand
  extends ChainCommand
{
  public static Logger log = LoggerFactory.getLogger(ForEachCommand.class);

  /**
   * An XPath to a list.
   */
  @Attribute(localName="select", type=AttributeType.JXPATH_ITERATE_POINTERS)
  public abstract Iterator<Pointer> iterateSelect( JXPathContext context );
  public abstract boolean hasSelect();

  /**
   * Executes the children of this node in the context of the values found by select or select nodes.
   */
  public boolean execute( JXPathContext context )
    throws Exception
  {
	  Iterator<Pointer> iterator = iterateSelect(context);;

	  boolean result = false;

	  while( iterator.hasNext() ) {
		  Pointer next = iterator.next();

                  context = Execution.startContextPointer(context, next);
                  try {
		  
		  // Execute the children with the context.
		  result = super.execute(context);

		  // Check if any of the children returned true.
		  if( result == true ) {
			  return result;
		  }
                  }
                  finally {
                    context = Execution.stopContextPointer(context);
                  }
	  }

		  // if the select returned false, then return false and let the other chains execute.
	  return false;
  }
}
