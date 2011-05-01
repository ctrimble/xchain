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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>The <code>list</code> evaluates the Query as a list.</p>
 * 
 * <p>This must reference a <code>query</code>.</p>
 * 
 * <code class="source">
 * &lt;xchain:session xmlns:xchain="http://www.xchain.org/hibernate/1.0"&gt;
 *  &lt;xchain:transaction&gt;
 *    ...
 *    &lt;xchain:query query="'from Entity'"&gt;
 *      ...
 *      &lt;list result="$myList"/&gt;
 *      ...
 *    &lt;/xchain:query&gt;
 *    ...
 *  &lt;/xchain:transaction&gt;
 * &lt;/xchain:session&gt;
 * </code> 
 *
 * @author Devon Tackett
 * @author Christian Trimble
 * @author Josh Kennedy
 * 
 * @see SessionCommand
 * @see org.hibernate.Query#list()
 */
@Element(localName="list")
public abstract class ListCommand 
	extends AbstractQueryResultCommand
{	  	  
  private static Logger log = LoggerFactory.getLogger( ListCommand.class );
	  /**
	   * Evaluate the query as a list.
	   */
	  public boolean execute( JXPathContext context )
	    throws Exception
	  { 
            try {
      Query query = getQuery(context);
      
      if (query == null) {
        if( log.isDebugEnabled() ) {
          log.debug("Could not get query for list command.");
			  throw new Exception("A list command must have a query.");
        }
      }
	    	
    	// Execute the query and store the result.
    	storeValue(context, query.list());
            }
            catch( Exception e ) {
              if( log.isErrorEnabled() ) {
                log.error("Could not execute query.", e);
              }
              throw e;
            }
	    
	    // The query is built and executed.
	    return false;
	  }	  
}
