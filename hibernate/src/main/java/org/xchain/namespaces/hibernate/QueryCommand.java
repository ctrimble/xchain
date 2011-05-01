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

import javax.xml.namespace.QName;

import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.framework.jxpath.ScopedQNameVariables;
import org.xchain.framework.jxpath.Scope;
import org.xchain.framework.hibernate.HibernateLifecycle;

/**
 * <p>The <code>query</code> command starts a new query.</p>
 * 
 * <p>This must reference an active <code>session</code>.</p>
 * 
 * <code class="source">
 * &lt;xchain:session xmlns:xchain="http://www.xchain.org/hibernate/1.0"&gt;
 *  &lt;xchain:transaction&gt;
 *    ...
 *    &lt;xchain:query query="'from entity'"&gt;
 *      ...
 *    &lt;xchain:query/&gt;
 *    ...
 *  &lt;/xchain:transaction&gt;
 * &lt;/xchain:session&gt;
 * </code> 
 *
 * @author Devon Tackett
 * @author Christian Trimble
 * @author Josh Kennedy
 * 
 * @see org.hibernate.Query
 */
@Element(localName="query")
public abstract class QueryCommand
	extends AbstractSessionCommand
{  
  public static Logger log = LoggerFactory.getLogger(QueryCommand.class);
  
  /**
   * Where the query is stored. 
   */
  @Attribute(localName="result",
      type=AttributeType.QNAME,
      defaultValue= "{" + Constants.URI + "}" + Constants.QUERY)      
  public abstract QName getResult( JXPathContext context );
  
  /**
   * The actual HQL query. 
   */
  @Attribute(localName="query", type=AttributeType.JXPATH_VALUE)
  public abstract String getQuery( JXPathContext context );
  public abstract boolean hasQuery();  
  
  public boolean execute( JXPathContext context )
    throws Exception
  {
    Session session = HibernateLifecycle.getCurrentSession(getName(context));
    Query query = null;
    QName variable = null;
    
    if (!hasQuery())
      throw new Exception("A query command requires a query.");
    
    if (session == null)
      throw new Exception("Query requires a session.");
  
    variable = getResult( context );

    query = session.createQuery(getQuery(context));

    // put the query into the context.
    ((ScopedQNameVariables)context.getVariables()).declareVariable( variable, query, Scope.chain );

    // execute the chain 
    return super.execute( context );

  }  
}
