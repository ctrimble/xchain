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
package org.xchain.namespaces.hibernate;

import org.apache.commons.jxpath.JXPathContext;
import org.hibernate.Query;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;

/**
 * <p>The <code>first-result</code> command sets the FirstResult on the parent Query.</p>
 * 
 * <p>This must reference a <code>query</code>.</p>
 * 
 * <code class="source">
 * &lt;xchain:session xmlns:xchain="http://www.xchain.org/hibernate/1.0"&gt;
 *  &lt;xchain:transaction&gt;
 *    ...
 *    &lt;xchain:query query="'from entity'"&gt;
 *      ...
 *      &lt;xchain:first-result start="'10'"/&gt;
 *      ...
 *    &lt;/xchain:query&gt;
 *    ...
 *  &lt;/xchain:transaction&gt;
 * &lt;/xchain:session&gt;
 * </code>
 *
 * @author Devon Tackett
 * @author Christian Trimble
 * 
 * @see QueryCommand
 * @see org.hibernate.Query#setFirstResult(int)
 */
@Element(localName="first-result")
public abstract class FirstResultCommand 
	extends AbstractQueryCommand
{
    /**
     * The start index to retrieve results.
     */
	  @Attribute(localName="start", type=AttributeType.JXPATH_VALUE)
	  public abstract Integer getStart( JXPathContext context );
	  public abstract boolean hasStart();
	  
	  public boolean execute( JXPathContext context )
	    throws Exception
	  {	    
	    if (!hasStart())
	    	throw new Exception("A FirstResult must have a start.");
	    
	    Query query = getQuery(context);
	    
	    if (query == null)
	      throw new Exception("A query must be specified.");
	    
	    query.setFirstResult(getStart(context));
	    
	    // return false and allow other chains to execute.
	    return false;
	  }	  
}
