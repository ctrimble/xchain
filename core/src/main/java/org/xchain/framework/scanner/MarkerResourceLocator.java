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
package org.xchain.framework.scanner;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.Enumeration;

/**
 * <p>A locator that finds urls based on the existance of a file under the root URL.</p>
 *
 * @author Christian Trimble
 * @author John Trimble
 */
public class MarkerResourceLocator
  implements RootUrlLocator
{
  private String resourceName = null;
  private int hashCode;

  public MarkerResourceLocator( String resourceName )
  {
    this.resourceName = resourceName;
    this.hashCode = (MarkerResourceLocator.class.getName()+resourceName).hashCode();
  }

  public Set<URL> findRoots( ClassLoader cl )
    throws Exception
  {
    Set<URL> resultSet = new HashSet<URL>();

    // get all of the urls that have the resource name.
    Enumeration<URL> urlEnum = cl.getResources(resourceName);

    // process all of the urls that we found.
    while( urlEnum.hasMoreElements() ) {
      resultSet.add(ScanUtil.computeResourceRoot(urlEnum.nextElement(), resourceName));
    }

    return resultSet;
  }

  public String toString()
  {
    return "MarkerResourceLocator[resourceName:"+resourceName+"]";
  }
  
  @Override
  public int hashCode() {
    return this.hashCode;
  }

  @Override
  public boolean equals(Object o) 
  {
    if( o instanceof MarkerResourceLocator ) {
      MarkerResourceLocator loc = (MarkerResourceLocator)o; 
      // return true if the two resource names are the same, taking into account the resource name might be null.
      return this.resourceName != null && this.resourceName.equals(loc.resourceName) || this.resourceName == loc.resourceName;
    }
    return false;
  }
}
