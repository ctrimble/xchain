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
 * <p>The <code>max-result</code> command sets the MaxResults on the parent Query.</p>
 * 
 * <p>This must reference a <code>query</code>.</p>
 * 
 * <code class="source">
 * &lt;xchain:session xmlns:xchain="http://www.xchain.org/hibernate/1.0"&gt;
 *  &lt;xchain:transaction&gt;
 *    ...
 *    &lt;xchain:query query="'from entity'"&gt;
 *      ...
 *      &lt;xchain:max-result start="'10'"/&gt;
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
 * @see org.hibernate.Query#setMaxResults(int)
 */
@Element(localName="max-result")
public abstract class MaxResultCommand 
	extends AbstractQueryCommand
{
    /**
     * The maximum number of entries. 
     */
	  @Attribute(localName="size", type=AttributeType.JXPATH_VALUE)
	  public abstract Integer getSize( JXPathContext context );
	  public abstract boolean hasSize();
	  
	  public boolean execute( JXPathContext context )
	    throws Exception
	  {	    
	    if (!hasSize())
	    	throw new Exception("max-result must have a size.");
	    
	    Query query = getQuery(context);
	    
	    if (query == null)
	      throw new Exception("max-result requires a query.");
	    
	    // Set the max result on the query.
	    query.setMaxResults(getSize(context));
	    
	    // return false and allow other chains to execute.
	    return false;
	  }	  
}
