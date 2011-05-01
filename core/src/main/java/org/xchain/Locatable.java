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
package org.xchain;

import org.xml.sax.Locator;

/**
 * This interface is added to Command and Catalog classes that are created by the catalog factory, if it is
 * not already on the class being loaded.  Commands and Catalogs that provide implementations for
 * getLocator(), will have their implementations replaced by the CatalogFactory.
 *
 *
 * @author Christian Trimble
 */
public interface Locatable
{
  /**
   * Returns a Locator for Commands and Catalogs.
   */
  public Locator getLocator();

  public void setLocator( Locator locator );
}
