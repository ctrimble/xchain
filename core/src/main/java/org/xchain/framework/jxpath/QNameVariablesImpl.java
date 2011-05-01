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
package org.xchain.framework.jxpath;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.HashMap;
import org.apache.commons.jxpath.JXPathContext;
import static org.xchain.framework.util.JXPathContextUtil.*;

/**
 * An implementation of the Variables interface that properly resolves namespace prefixes.
 *
 * @author Christian Trimble
 */
public class QNameVariablesImpl
  implements QNameVariables
{
  protected JXPathContext context = null;
  protected Map<QName, Object> variableMap = null;

  /**
   * Creates a new QName variables object that does not have a context assigned for qname lookups.
   */
  public QNameVariablesImpl()
  {
    this.variableMap = new HashMap<QName, Object>();
  }

  /**
   * Creates a new QName variables object that has a context assigned for qname lookups.
   */
  public QNameVariablesImpl( JXPathContext context )
  {
    this.context = context;
    this.variableMap = new HashMap<QName, Object>();
  }

  /**
   * Creates a new QName variables object that shares its variable map with another qname variables object.
   */
  protected QNameVariablesImpl( JXPathContext context, Map variableMap )
  {
    this.context = context;
    this.variableMap = variableMap;
  }

  /**
   * Creates a new QNameVariables object that shares its variables map with this QNameVariables object.
   */
  public QNameVariables createSharedVariables( JXPathContext newContext )
  {
    return new QNameVariablesImpl( newContext, variableMap );
  }

  /**
   * Sets the JXPathContext that is used to lookup namespace uris.
   *
   * @param context the JXPathContext that will be used to lookup namespace uris.
   */
  public void setJXPathContext( JXPathContext context )
  {
    this.context = context;
  }

  /**
   * Returns the JXPathContext that is used to lookup namespace uris.
   *
   * @return the JXPathContext that is used to lookup namespace uris.
   */
  public JXPathContext getJXPathContext()
  {
    return this.context;
  }

  /**
   * Declares a variable for the specified name and value.  If the variable name is of the form 'prefix:local-name',
   * then the prefix is resolved.  If the variable name is of the form '{uri}local-name', then the uri is taken from the
   * variable name.  Otherwise, the entire variable name is considered a local name and the uri is the null namespace uri.
   *
   * @param varName the name of the variable with the format 'prefix:local-name', '{uri}local-name', or 'local-name'.
   * @param value the value to be set for this variable.
   */
  public void declareVariable( String varName, Object value )
  {
    variableMap.put(stringToQName(context,varName), value);
  }

  /**
   * Declares a variable for the specified qName and value.
   */
  public void declareVariable( QName varName, Object value )
  {
    variableMap.put(varName, value);
  }

  public Object getVariable( String varName )
  {
    return variableMap.get(stringToQName(context,varName));
  }

  public Object getVariable( QName varName )
  {
    return variableMap.get(varName);
  }

  public boolean isDeclaredVariable( String varName )
  {
    return variableMap.containsKey(stringToQName(context,varName));
  }

  public boolean isDeclaredVariable( QName varName )
  {
    return variableMap.containsKey(varName);
  }

  public void undeclareVariable( String varName )
  {
    variableMap.remove(stringToQName(context,varName));
  }

  public void undeclareVariable( QName varName )
  {
    variableMap.remove(varName);
  }

  public Map<QName, Object> getVariableMap()
  {
    return variableMap;
  }

  public String toString() { return variableMap.toString(); }
}
