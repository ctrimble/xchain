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

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author John Trimble
 */
public class ConfigContextTest {
  @BeforeClass
  public static void setUpLifecycle() throws LifecycleException {
    Lifecycle.startLifecycle();
  }
  
  @AfterClass 
  public static void tearDownLifecycle() throws LifecycleException {
    Lifecycle.stopLifecycle();
  }
  
  @Test
  public void testCatalogCacheSize() {
    int cacheSize = Lifecycle.getLifecycleContext().getConfigContext().getCatalogCacheSize();
    assertEquals(501, cacheSize);
  }
  
  @Test
  public void testTemplatesCacheSize() {
    int cacheSize = Lifecycle.getLifecycleContext().getConfigContext().getTemplatesCacheSize();
    assertEquals(502, cacheSize);
  }
  
  @Test
  public void testMonitor() {
    boolean monitor = Lifecycle.getLifecycleContext().getConfigContext().isMonitored();
    assertEquals(true, monitor);
  }
}
