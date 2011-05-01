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

import java.net.URL;

import org.apache.commons.jxpath.JXPathContext;
import org.xchain.framework.jxpath.Scope;
import org.xchain.framework.jxpath.ScopedJXPathContextImpl;

/**
 * @author John Trimble
 * @author Josh Kennedy
 */
public class ConfigDocumentContext extends ScopedJXPathContextImpl {
  private URL configUrl;
  
  public URL getConfigUrl() { return this.configUrl; }
  public void setConfigUrl(URL configUrl) { this.configUrl = configUrl; }
  
  public ConfigDocumentContext(JXPathContext parentContext, Object contextBean,
      Scope scope) {
    super(parentContext, contextBean, scope);
  }
  
  public <E extends Object> E getConfigEntry(String jxpath, Class<E> type, E defaultValue) throws LifecycleException {
    E value = getConfigEntry(jxpath, type);
    return value != null ? value : defaultValue;
  }
  
  @SuppressWarnings("unchecked")
  public <E extends Object> E getConfigEntry(String jxpath, Class<E> type) throws LifecycleException {
	Object value = null;
	try {
	  value = this.getValue(jxpath, type);
	} catch (Exception e) {
	  throw new LifecycleException("Could not load the configuration object at " + jxpath + " due to an exception.", e);
	}

	return (E) value;
  }

}
