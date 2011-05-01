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
package org.xchain.namespaces.core;

import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.Filter;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.framework.jxpath.ScopedQNameVariables;
import org.xchain.framework.jxpath.Scope;

/**
 * <p>The <code>variable</code> command implemented as a filter.  The <code>filter-variable</code> will declare and set a variable in the context.
 * The <code>name</code> attribute is the QName of the variable.
 * The <code>select</code>, <code>select-nodes</code>, or <code>select-single-node</code> attribute will be the value of the variable.
 * The <code>scope</code> attribute will determine the scope of the variable.  A 'chain' scope variable will only exist in the current
 * context.  A 'request' scope variable will exist for every context.  A request scope is assumed if no scope attribute is
 * provided.</p>
 * <p>During post process the original value at the QName will be restored.  If no value existed at the QName then the variable will be
 * undeclared.</p>
 *
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 *
 * <code class="source">
 * &lt;xchain:filter-variable xmlns:xchain="http://www.xchain.org/core/1.0" name="/some/xpath" select="/some/xpath"/ scope="chain"&gt;
 * </code>
 */
@Element(localName="filter-variable")
public abstract class FilterVariableCommand
  implements Filter
{
  protected ThreadLocal<LinkedList<Object>> valueStackThreadLocal = new ThreadLocal<LinkedList<Object>>();

  public static Logger log = LoggerFactory.getLogger(VariableCommand.class);

  /**
   * The QName of the variable.
   */
  @Attribute(localName="name", type=AttributeType.QNAME)
  public abstract QName getName( JXPathContext context );
  public abstract boolean hasName();

  /**
   * The value of the variable. 
   */
  @Attribute(localName="select", type=AttributeType.JXPATH_VALUE)
  public abstract Object getSelect( JXPathContext context );
  public abstract boolean hasSelect();

  /**
   * The value of the variable. 
   */  
  @Attribute(localName="select-nodes", type=AttributeType.JXPATH_SELECT_NODES)
  public abstract List getSelectNodes( JXPathContext context );
  public abstract boolean hasSelectNodes();

  /**
   * The value of the variable. 
   */  
  @Attribute(localName="select-single-node", type=AttributeType.JXPATH_SELECT_SINGLE_NODE)
  public abstract Object getSelectSingleNode( JXPathContext context );
  public abstract boolean hasSelectSingleNode();
  
  /**
   * The scope of the variable.  Can either be the literal request, exeuction or chain. 
   */
  @Attribute(localName="scope", type=AttributeType.LITERAL, defaultValue="request")
  public abstract Scope getScope(JXPathContext context);

  public boolean execute( JXPathContext context )
    throws Exception
  {
    QName variableName = getName(context);
    Object variableValue = null;

    if( hasSelect() ) {
      variableValue = getSelect(context);
    }
    else if( hasSelectNodes() ) {
      variableValue = getSelectNodes(context);
    }
    else if( hasSelectSingleNode() ) {
      variableValue = getSelectSingleNode(context);
    }
    else {
      throw new Exception( "Variable '"+variableName+"' must have a select attribute (select, select-nodes, or select-single-node)" );
    }

    // get the scope.
    Scope scope = getScope(context);

    if( log.isDebugEnabled() ) {
      log.debug("Setting variable name '"+variableName+"' to value '"+variableValue+"' in scope '"+scope+"'.");
    }
    
    ScopedQNameVariables variables = (ScopedQNameVariables)context.getVariables();
    QName name = getName(context);

    if( variables.isDeclaredVariable(name, scope) ) {
      pushValue(variables.getVariable(name, scope));
    }
    else {
      pushValue(new UndeclaredVariable());
    }

    // declare the variable.
    ((ScopedQNameVariables)context.getVariables()).declareVariable( variableName, variableValue, scope );

    // return false and allow other chains to execute.
    return false;    

  }

  public boolean postProcess( JXPathContext context, Exception e )
  {
    QName name = getName(context);
    // get the scope.
    Scope scope = getScope(context);    

    ScopedQNameVariables variables = (ScopedQNameVariables)context.getVariables();
    Object value = popValue();

    if( value instanceof UndeclaredVariable ) {
      variables.undeclareVariable(name, scope);
    }
    else {
      variables.declareVariable(name, value, scope);
    }

    // since we didn't handle the exception, just return false.
    return false;
  }

  public LinkedList<Object> getValueStack()
  {
    LinkedList<Object> valueStack = valueStackThreadLocal.get();

    if( valueStack == null ) {
      valueStack = new LinkedList<Object>();
      valueStackThreadLocal.set(valueStack);
    }

    return valueStack;
  }

  public void pushValue( Object value )
  {
    getValueStack().addFirst(value);
  }

  public Object popValue()
  {
    return getValueStack().removeFirst();
  }

  private static class UndeclaredVariable {};
}
