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
import org.xchain.annotations.Element;

/**
 * <p>The <code>update</code> executes the query as an update.</p>
 * 
 * <p>This must reference a <code>query</code>.</p>
 * 
 * <code class="source">
 * &lt;xchain:session xmlns:xchain="http://www.xchain.org/hibernate/1.0"&gt;
 *  &lt;xchain:transaction&gt;
 *    ...
 *    &lt;xchain:query query="'update Entity set value = 1 where value = 1'"&gt;
 *      ...
 *      &lt;xchain:update result="$myEntity"/&gt;
 *      ...
 *    &lt;/xchain:query&gt;
 *    ...
 *  &lt;/xchain:transaction&gt;
 * &lt;/xchain:session&gt;
 * </code> 
 *
 * @author Jason Rose
 * 
 * @see QueryCommand
 * @see org.hibernate.Query
 * @see org.hibernate.Query#executeUpdate()
 */
@Element(localName="update")
public abstract class UpdateCommand 
	extends AbstractQueryResultCommand
{ 	  
	  /**
	   * Evaluates the query as a unique-result.
	   */
	  public boolean execute( JXPathContext context )
	    throws Exception
	  { 
	    Query query = getQuery(context);
	    
		  if (query == null) {
			  throw new Exception("An update command must have a query.");
		  }
		  
		  int result = query.executeUpdate();
    	// Execute the query and store the result.
		  if( hasResult() || hasVariable() ) {
		    storeValue(context, result);
		  }
	    
	    // The query is built and executed.
	    return false;
	  }  
}
