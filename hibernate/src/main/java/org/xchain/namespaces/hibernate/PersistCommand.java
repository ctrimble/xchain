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
import org.hibernate.Session;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.framework.hibernate.HibernateLifecycle;

/**
 * <p>The <code>persist</code> command will execute a Hibernate persist on the session with the specified entity.
 * For more information about the <code>persist</code> method, refer to the Hibernate documentation.</p>
 *
 * <p>This must reference an active <code>session</code>.</p>
 * 
 * <code class="source">
 * &lt;xchain:session xmlns:xchain="http://www.xchain.org/hibernate/1.0"&gt;
 *  &lt;xchain:transaction&gt;
 *    ...
 *    &lt;xchain:persist entity="$myEntity"&gt;
 *    ...
 *  &lt;/xchain:transaction&gt;
 * &lt;/xchain:session&gt;
 * </code>
 *
 * @author Devon Tackett
 * @author Christian Trimble
* 
* @see SessionCommand
* @see org.hibernate.Session#persist(Object)
*/
@Element(localName="persist")
public abstract class PersistCommand 
	extends AbstractSessionCommand
{	  
    /**
     * The entity to persist. 
     */
	  @Attribute(localName="entity", type=AttributeType.QNAME)
	  public abstract String getEntity( JXPathContext context);
	  public abstract boolean hasEntity();
	  
	  /**
	   * Persist the entity at the given QName.  Note that persist will only perform an
	   * insert if it is within a transaction.
	   */
	  public boolean execute( JXPathContext context )
	    throws Exception
	  { 		  
		  if (!hasEntity())
			  throw new Exception("A persist command must reference an entity.");
		  
	    // Get the session from the context.
	    Session session = HibernateLifecycle.getCurrentSession(getName(context));
	    
	    // Get the entity.
	    Object entity = context.getValue(getEntity(context));

	    if (entity == null)
	    	throw new Exception("Unable to find entity at: " + getEntity(context));
	    
	    // Perform the persist.
	    session.persist(entity);
	    
	    // The persist is executed.
	    return false;
	  }	  
}
