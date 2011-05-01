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
import java.util.Map;
import org.apache.commons.jxpath.Functions;

/**
 * The lifecycle context for the xchain framework.  This class holds the class loader that is used to load commands,
 * the configuration context, and a mapping of namespace uris to namespace contexts.
 *
 * @author Christian Trimble
 * @author Mike Moulton
 * @author Devon Tackett
 */
public class LifecycleContext
{
  protected ClassLoader classLoader;
  protected ConfigContext configContext = new ConfigContext();
  protected Map<String, NamespaceContext> namespaceContextMap = new HashMap<String, NamespaceContext>();
  protected Functions functionLibrary = new LifecycleFunctionLibrary(this); 

  /**
   * Sets the class loader that engineered commands are loaded into. 
   *
   * NOTE: This method should be restricted to this package.
   *
   * @param classLoader the new class loader that engineered commands are loaded into.
   */
  public void setClassLoader( ClassLoader classLoader ) { this.classLoader = classLoader; }

  /**
   * Returns the class loader that engineered commands are loaded into.
   *
   * @return the class loader that engineered commands are loaded into.
   */
  public ClassLoader getClassLoader() { return this.classLoader; }

  public ConfigContext getConfigContext() { return this.configContext; }

  /**
   * Returns the mapping of namespace uris to namespaces that have been loaded into the lifecycle.
   *
   * @return the mapping of namespace uris to namespaces that have been loaded into the lifecycle.
   */
  public Map<String, NamespaceContext> getNamespaceContextMap() { return this.namespaceContextMap; }

  public Functions getFunctionLibrary() { return this.functionLibrary; }

}
