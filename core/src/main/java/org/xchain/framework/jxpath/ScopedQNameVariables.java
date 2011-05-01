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

import org.apache.commons.jxpath.JXPathContext;

/**
 * An extension of the QNameVariables to work within a given scope.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 */
public interface ScopedQNameVariables
  extends QNameVariables
{ 
  /**
   * Declares a variable for the specified name and value.  If the variable name is of the form 'prefix:local-name',
   * then the prefix is resolved.  If the variable name is of the form '{uri}local-name', then the uri is taken from the
   * variable name.  Otherwise, the entire variable name is considered a local name and the uri is the null namespace uri.
   *
   * @param varName The name of the variable with the format 'prefix:local-name', '{uri}local-name', or 'local-name'.
   * @param value The value to be set for this variable.
   * @param scope The scope of the variable.
   */
  public void declareVariable( String varName, Object value, Scope scope );
  
  /**
   * Declares a variable for the specified QName and value.
   *
   * @param varName The QName of the variable .
   * @param value The value to be set for this variable.
   * @param scope The scope of the variable.
   */  
  public void declareVariable( QName varName, Object value, Scope scope );

  /**
   * Get the variable value at the given name for the given scope.
   * 
   * @param varName The name of the variable.
   * @param scope The scope of the variable.
   * 
   * @return The variable for the given name at the given scope.  Null if the variable does not exist.
   */
  public Object getVariable( String varName, Scope scope );
  
  /**
   * Get the variable value at the given QName for the given scope.
   * 
   * @param varName The QName of the variable.
   * @param scope The scope of the variable.
   * 
   * @return The variable for the given QName at the given scope.  Null if the variable does not exist.
   */  
  public Object getVariable( QName varName, Scope scope );
  
  /**
   * Determine if the variable with the given QName exists at the given scope.
   * 
   * @param varName The QName of the variable.
   * @param scope The scope to check at.
   * 
   * @return True if the variable is declared at the given scope.  False if not.
   */
  public boolean isDeclaredVariable( QName varName, Scope scope );
  
  /**
   * Remove the declaration of the variable with the given name at the given scope.
   * 
   * @param varName The name of the variable to undeclare.
   * @param scope The scope at which to undeclare the variable.
   */
  public void undeclareVariable( String varName, Scope scope );
  
  /**
   * Remove the declaration of the variable with the given QName at the given scope.
   * 
   * @param varName The QName of the variable to undeclare.
   * @param scope The scope at which to undeclare the variable.
   */  
  public void undeclareVariable( QName varName, Scope scope );
  
  /**
   * Release all components created at the scope that these variables are for.
   */
  public void releaseComponents();
}
