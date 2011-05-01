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
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class ConfigContext
{
  /** The maximum size of the catalog cache. */
  protected int catalogCacheSize = 200;
  /** The maximum size of the template cache */
  protected int templatesCacheSize = 200;
  /** Whether cached files should have their sources monitored for changes. */
  protected boolean monitored = false;
  protected List<URL> resourceUrlList = new ArrayList<URL>();
  protected List<URL> sourceUrlList = new ArrayList<URL>();
  protected List<URL> webappUrlList = new ArrayList<URL>();

  public int getCatalogCacheSize() { return this.catalogCacheSize; }
  public void setCatalogCacheSize(int catalogCacheSize) { this.catalogCacheSize = catalogCacheSize; }

  public int getTemplatesCacheSize() { return this.templatesCacheSize; }
  public void setTemplatesCacheSize(int templatesCacheSize) { this.templatesCacheSize = templatesCacheSize; }

  public boolean isMonitored() { return this.monitored; }
  public void setMonitored(boolean monitor) { this.monitored = monitor; }

  public List<URL> getResourceUrlList() { return this.resourceUrlList; }
  public List<URL> getSourceUrlList() { return this.sourceUrlList; }
  public List<URL> getWebappUrlList() { return this.webappUrlList; }
}
