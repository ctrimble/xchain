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

import java.util.Collection;

import org.apache.commons.jxpath.JXPathContext;
import org.hibernate.Query;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;

/**
 * <p>The <code>parameter</code> command will specify a parameter for a query.</p>
 *
 * <p>This must reference a <code>query</code>.</p>
 * 
 * <code class="source">
 * &lt;xchain:session xmlns:xchain="http://www.xchain.org/hibernate/1.0"&gt;
 *  &lt;xchain:transaction&gt;
 *    ...
 *    &lt;xchain:query query="'from entity where value = :myvalue'"&gt;
 *      ...
 *      &lt;xchain:parameter name="'myvalue'" value="/some/xpath"/&gt;
 *      ...
 *    &lt;/xchain:query&gt;
 *    ...
 *  &lt;/xchain:transaction&gt;
 * &lt;/xchain:session&gt;
 * </code>
 *
 * @author Devon Tackett
 * @author Christian Trimble
 * @author Jason Rose
* 
* @see QueryCommand
* @see org.hibernate.Query#setParameter(String, Object)
*/
@Element(localName="parameter")
public abstract class ParameterCommand 
	extends AbstractQueryCommand
{
    /**
     * The name of the parameter in the query. 
     */
	  @Attribute(localName="name", type=AttributeType.JXPATH_VALUE)
	  public abstract String getName( JXPathContext context );
	  public abstract boolean hasName();

	  /**
	   * The value of the parameter in the query. 
	   */
	  @Attribute(localName="value", type=AttributeType.JXPATH_VALUE)
	  public abstract Object getValue( JXPathContext context );
	  public abstract boolean hasValue();
	  
	  public boolean execute( JXPathContext context )
	    throws Exception
	  {
	    Query query = getQuery(context);
	    String parameterName = null;
	    Object parameterValue = null;
	    
	    if (query == null) {
	      throw new Exception("A Parameter must have a query.");
	    }
	    
	    if (!hasName()) {
	    	throw new Exception("A Parameter must have a name.");
	    }
	    
	    if (!hasValue()) {
	      throw new Exception("Parameter '" + parameterName + "' must have a select value.");
	    }
	    
	    parameterName = getName(context);
	    
	    parameterValue = getValue(context);
	    
	    if( parameterValue instanceof Collection ) {
	      query.setParameterList(parameterName, (Collection) parameterValue);
	    } else if( parameterValue.getClass().isArray() ) {
	      if( Object[].class.isAssignableFrom(parameterValue.getClass()) ) {
	        query.setParameterList(parameterName, (Object[]) parameterValue);
	      } else {
	        throw new IllegalArgumentException(String.format("The array type (%s) must be an array of references.", parameterValue.getClass().getName()));
	      }
	    } else {
	      query.setParameter(parameterName, parameterValue);	      
	    }
	    
	    // return false and allow other chains to execute.
	    return false;
	  }	  
}
