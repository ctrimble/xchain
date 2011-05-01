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

import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Variables;

/**
 * An extension the Variables interface that properly resolves namespace prefixes.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 */
public interface QNameVariables
  extends Variables
{
  /**
   * Sets the JXPathContext that is used to lookup namespace uris.
   *
   * @param context the JXPathContext that will be used to lookup namespace uris.
   */
  public void setJXPathContext( JXPathContext context );

  /**
   * Returns the JXPathContext that is used to lookup namespace uris.
   *
   * @return the JXPathContext that is used to lookup namespace uris.
   */
  public JXPathContext getJXPathContext();

  /**
   * Declares a variable for the specified qName and value.
   *
   * @param qName the name of the variable being declared or replaced.
   * @param value the value of the variable being declared or replaced.
   */
  public void declareVariable( QName qName, Object value );

  /**
   * Gets a variable by the specified qName.
   *
   * @param qName the name of the variable to retrieve.
   * @return the value of the variable or null if the variable could not be found.
   */
  public Object getVariable( QName qName );

  /**
   * Returns true if the specified qName is defined, false otherwise.
   *
   * @param qName the name of the variable being tested.
   * @return true if the variable is declared, false otherwise.
   */
  public boolean isDeclaredVariable( QName qName );

  /**
   * Undeclares the variable for the specified qName.
   *
   * @param qName the name of the variable to undeclare.
   */
  public void undeclareVariable( QName qName );

  /**
   * @return A mapping of variable QName to variable values.
   */
  public Map<QName, Object> getVariableMap();
}
