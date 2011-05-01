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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.Command;
import org.xchain.impl.ChainImpl;
import org.xchain.framework.jxpath.Scope;
import org.xchain.framework.jxpath.ScopedQNameVariables;
import org.xchain.annotations.Element;
import org.xchain.annotations.ParentElement;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import javax.xml.namespace.QName;
/**
 * <p>The <code>validate</code> command ensures that objects have their proper hibernate restrictions.  If the objects are valid
 * then the child <code>valid</code> command is executed.  If the objects are invalid then the child <code>invalid</code>
 * command is executed.</p>
 * 
 * <code class="source">
 * &lt;xchain:validate select="/some/xpath" xmlns:xchain="http://www.xchain.org/hibernate/1.0"&gt;
 *  &lt;xchain:valid&gt;
 *   ...
 *  &lt;/xchain:valid&gt;
 *  &lt;xchain:invalid&gt;
 *   ...
 *  &lt;/xchain:invalid&gt;
 * &lt;/xchain:session&gt;
 * </code> 
 *
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
@Element(localName="validate")
public abstract class ValidateCommand
  extends ChainImpl
{
  public static Logger log = LoggerFactory.getLogger(ValidateCommand.class);
  private static Map<Class, ClassValidator> validatorCache = new HashMap<Class, ClassValidator>();

  /**
   * The entity to validate. 
   */
  @Attribute(localName="select", type=AttributeType.JXPATH_VALUE)
  public abstract Object getSelect( JXPathContext context );
  public abstract boolean hasSelect();

  /**
   * A list of entities to validate. 
   */
  @Attribute(localName="select-nodes", type=AttributeType.JXPATH_SELECT_NODES)
  public abstract List getSelectNodes( JXPathContext context );
  public abstract boolean hasSelectNodes();

  /**
   * The entity to validate. 
   */
  @Attribute(localName="select-single-node", type=AttributeType.JXPATH_SELECT_SINGLE_NODE)
  public abstract Object getSelectSingleNode( JXPathContext context );
  public abstract boolean hasSelectSingleNode();

  /**
   * Where to store the validation messages. 
   */
  @Attribute(localName="validation-messages",
             type=AttributeType.QNAME,
             defaultValue="{http://www.xchain.org/hibernate/1.0}validation-messages")
  public abstract QName getMessagesQName( JXPathContext context );


  public boolean execute( JXPathContext context )
    throws Exception
  {
    boolean valid = true;
    List<InvalidValue> invalidValues = new ArrayList<InvalidValue>();

    // validate using hibernate select attribute
    if( hasSelect() ) {
      Object bean = getSelect( context );
      if(!valid( bean, invalidValues )) {
        valid = false;
      }
    }

    // validate using hibernate select-nodes attribute
    if( hasSelectNodes() ) {
      List beans = getSelectNodes( context );
      for( Iterator it = beans.iterator(); it.hasNext(); ) {
        Object bean = it.next();
        if(!valid( bean, invalidValues )) {
          valid = false;
        }
      }
    }

    // validate using hibernate select-single-node attribute
    if( hasSelectSingleNode() ) {
      Object bean = getSelectSingleNode( context );
      if(!valid( bean, invalidValues )) {
        valid = false;
      }
    }

    if(!valid) {
      ((ScopedQNameVariables)context.getVariables()).declareVariable( getMessagesQName( context ), invalidValues, Scope.chain );
    }

    // iterate over the children clauses looking for a either the valid / invalid clause
    // if a match is found, then execute the associated chain.
    Iterator<Command> childIterator = getCommandList().iterator();
    while( childIterator.hasNext() ) {
      Command clause = childIterator.next();

      if( valid && clause instanceof ValidClause ) {
        return clause.execute(context);
      }
      else if( !valid && clause instanceof InvalidClause ) {
        return clause.execute(context);
      }
    }

    // if we got this far, then just return null.
    return false;
  }

  public static boolean valid( Object bean, List<InvalidValue> invalidValues ) {

    boolean valid = true;
    ClassValidator beanValidator = null;

    try {
      // lookup cached validator, if none found create a new validator
      if( validatorCache.containsKey( bean.getClass() ) ) {
        beanValidator = validatorCache.get( bean.getClass() );
      }
      else {
        beanValidator = new ClassValidator( bean.getClass() );
        validatorCache.put( bean.getClass(), beanValidator );
      }

      // validate bean
      InvalidValue[] messages = beanValidator.getInvalidValues( bean );

      if (messages != null && messages.length > 0) {
        valid = false;

        // add validation
        for ( InvalidValue message : messages ) {
          invalidValues.add( message );
        }
      }

    }
    catch (Throwable ex) {
      log.error( "Exception validation bean '"+bean.getClass()+"'", ex);
    }

    return valid;
  }

  /**
   * The <code>valid</code> command chain is executed if the parent validation command passes.
   */
  @Element(localName="valid", parentElements={@ParentElement(localName="validate", namespaceUri="http://www.xchain.org/hibernate/1.0")})
  public abstract static class ValidClause
    extends ChainImpl
  {
    // the extension is all that we need.
  }

  /**
   * The <code>invalid</code> command chain is executed if the parent validation command fails.
   */  
  @Element(localName="invalid", parentElements={@ParentElement(localName="validate", namespaceUri="http://www.xchain.org/hibernate/1.0")})
  public static class InvalidClause
    extends ChainImpl
  {
    // the extension is all that we need.
  }
}
