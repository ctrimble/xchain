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
package org.xchain.framework.lifecycle;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * A context for a namespace.  Namespaces are identified by a unique URI and contain a series of commands, catalogs and components.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class NamespaceContext
{
  /**
   * The namespace uri.
   */
  protected String namespaceUri = null;
  
  /**
   * List of commands in the namespace.
   */
  protected List<Class> commandList = new ArrayList<Class>();
  
  /**
   * List of catalogs in the namespace.
   */
  protected List<Class> catalogList = new ArrayList<Class>();

  /**
   * The function library for this namespace.
   */
  protected NamespaceFunctionLibrary functionLibrary;
  
  /**
   * Mapping of component localnames to component analysis in the namespace.
   */
  protected Map<String, ComponentAnalysis> componentMap = new HashMap<String, ComponentAnalysis>();

  public NamespaceContext( String namespaceUri )
  {
    this.namespaceUri = namespaceUri;
    this.functionLibrary = new NamespaceFunctionLibrary(namespaceUri);
  }

  /**
   * @return The list of commands in the namespace.
   */
  public List<Class> getCommandList() { return this.commandList; }
  
  /**
   * @return The list of catalogs in the namespace.
   */
  public List<Class> getCatalogList() { return this.catalogList; }
  
  /**
   * @return The mapping of component localnames to component analysis in the namespace.
   */
  public Map<String, ComponentAnalysis> getComponentMap() { return this.componentMap; }
  
  /**
   * @return The namespace uri.
   */
  public String getNamespaceUri() { return this.namespaceUri; }

  public NamespaceFunctionLibrary getFunctionLibrary() { return this.functionLibrary; }
}
