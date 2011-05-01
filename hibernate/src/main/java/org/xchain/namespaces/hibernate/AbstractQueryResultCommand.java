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
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.framework.jxpath.ScopedQNameVariables;
import org.xchain.framework.jxpath.Scope;

/**
 * Base query result command.  Any commands which generate a result for a query should extend this class.
 *
 * @author Devon Tackett
 */
public abstract class AbstractQueryResultCommand
  extends AbstractQueryCommand
{
  /**
   * The QName of where to store the result.  The QName must already exist.
   */
  @Attribute(localName="result", type=AttributeType.QNAME)
  public abstract String getResult( JXPathContext context);
  public abstract boolean hasResult();
  
  /**
   * The variable of where to store the result.  If the variable does not yet exist, a variable will be declared. 
   */
  @Attribute(localName="variable", type=AttributeType.QNAME)
  public abstract String getVariable( JXPathContext context);
  public abstract boolean hasVariable();
  
  /**
   * The scope of the variable.
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
}
