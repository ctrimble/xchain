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

import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.hibernate.Session;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.framework.hibernate.HibernateLifecycle;

/**
 * <p>The <code>delete</code> command deletes the provided entity or list of entities.</p>
 * 
 * <p>This must reference an active <code>session</code>.</p>
 * 
 * <code class="source">
 * &lt;xchain:session xmlns:xchain="http://www.xchain.org/hibernate/1.0"&gt;
 *  ...
 *  &lt;xchain:delete select="/some/xpath"/&gt;
 *  ...
 * &lt;/xchain:session&gt;
 * </code>
 *
 * @author Devon Tackett
 * @author Christian Trimble
 * 
 * @see SessionCommand
 * @see org.hibernate.Session#delete(Object)
 */
@Element(localName="delete")
public abstract class DeleteCommand 
	extends AbstractSessionCommand
{	  
  /**
   * An XPath to the entity to delete. 
   */
  @Attribute(localName="select", type=AttributeType.JXPATH_VALUE)
  public abstract Object getSelect( JXPathContext context );
  public abstract boolean hasSelect();

  /**
   * An XPath to a list of entities to delete. 
   */
  @Attribute(localName="select-nodes", type=AttributeType.JXPATH_SELECT_NODES)
  public abstract List getSelectNodes( JXPathContext context );
  public abstract boolean hasSelectNodes();

  /**
   * An XPath to a the entity to delete. 
   */
  @Attribute(localName="select-single-node", type=AttributeType.JXPATH_SELECT_SINGLE_NODE)
  public abstract Object getSelectSingleNode( JXPathContext context );
  public abstract boolean hasSelectSingleNode();  
	  
	  /**
	   * Deletes the entity at the given QName.
	   */
	  public boolean execute( JXPathContext context )
	    throws Exception
	  { 		  
      // Get the session from the context.
      Session session = HibernateLifecycle.getCurrentSession(getName(context));
      
		  if (hasSelect()) {
		    session.delete(getSelect(context));
		  } else if (hasSelectNodes()) {
		    List nodeList = getSelectNodes(context);
		    for (Object entity : nodeList) {
		      session.delete(entity);
		    }
		  } else if (hasSelectSingleNode()) {
		    session.delete(getSelectNodes(context));
		  }
	    
	    // The delete is executed.
	    return false;
	  }	  
}
