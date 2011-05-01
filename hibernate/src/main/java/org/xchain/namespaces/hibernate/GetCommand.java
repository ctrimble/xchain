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

import java.io.Serializable;

import org.apache.commons.jxpath.JXPathContext;
import org.hibernate.Session;
import org.hibernate.metadata.ClassMetadata;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.framework.jxpath.ScopedQNameVariables;
import org.xchain.framework.jxpath.Scope;
import org.xchain.framework.hibernate.HibernateLifecycle;

/**
 * <p>The <code>get</code> command starts executes the Get method on the current session.  The <code>class-name</code> attribute identifies
 * the class to be loaded.  The <code>id</code> attribute identifies the unique identifier for the class to be loaded.</p>
 * 
 * <p>This must reference an active <code>session</code>.</p>
 * 
 * <code class="source">
 * &lt;xchain:session xmlns:xchain="http://www.xchain.org/hibernate/1.0"&gt;
 *  &lt;xchain:transaction&gt;
 *    ...
 *    &lt;xchain:get id="'1'" class-name="'my.package.entity'" variable="result" scope="request"/&gt;
 *    ...
 *  &lt;/xchain:transaction&gt;
 * &lt;/xchain:session&gt;
 * </code>
 *
 * @author Devon Tackett
 * @author Christian Trimble
 * 
 * @see SessionCommand
 * @see org.hibernate.Session#get(Class, Serializable)
 */
@Element(localName="get")
public abstract class GetCommand 
	extends AbstractSessionCommand
{ 
    /**
     * ID of the entity to retrieve. 
     */
	  @Attribute(localName="id", type=AttributeType.JXPATH_VALUE)
	  public abstract Serializable getId( JXPathContext context, Class type );
	  public abstract boolean hasId();	
	  
	  /**
	   * The class of the entity to retrieve. 
	   */
	  @Attribute(localName="class-name", type=AttributeType.JXPATH_VALUE)
	  public abstract String getClassName( JXPathContext context );
	  public abstract boolean hasClassName();	  
	  
	  /**
	   * Where to store the retrieved entity. 
	   */
	  @Attribute(localName="result", type=AttributeType.QNAME)
	  public abstract String getResult( JXPathContext context);
	  public abstract boolean hasResult();
	  
	  /**
	   * Variable of where to store the entity.
	   */
	  @Attribute(localName="variable", type=AttributeType.QNAME)
	  public abstract String getVariable( JXPathContext context);
	  public abstract boolean hasVariable();
	  
	  /**
	   * Scope of the variable storing the entity.
	   * @see Scope
	   */
	  @Attribute(localName="scope", type=AttributeType.LITERAL, defaultValue="execution")
	  public abstract Scope getScope(JXPathContext context);
	  
	  protected void storeValue(JXPathContext context, Object value) throws Exception {     
	    if (hasResult()) {
	      // TODO This should check that the path exists first.  If it doesn't exist, create it.
	      context.setValue(getResult(context), value);
	    } else if (hasVariable()) {
	      ((ScopedQNameVariables)context.getVariables()).declareVariable( getVariable(context), value, getScope(context) );
	    } else {
	      throw new Exception("Result or Variable must be given.");
	    }
	  }	  
	  
	  /**
	   * Performs a Get for the given class-name with the given id and stores the result in the given
	   * result QName.
	   */
	  public boolean execute( JXPathContext context )
	    throws Exception
	  { 
		  if (!hasId())
			  throw new Exception("A get command must have an id.");
		  
		  if (!hasClassName())
			  throw new Exception("A get command must have a class-name.");
		  
	    // Get the session from the context.
	    Session session = HibernateLifecycle.getCurrentSession(getName(context));
	    
	    // Get the metadata of the entity to be loaded from hibernate 
	    ClassMetadata metadata = session.getSessionFactory().getClassMetadata(getClassName(context));
	    
	    storeValue(context, session.get(getClassName(context), getId(context, metadata.getIdentifierType().getReturnedClass())));
	    
	    // The get is executed.
	    return false;
	  }	  
}
