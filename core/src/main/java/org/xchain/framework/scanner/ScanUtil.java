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

/**
 * @author Christian Trimble
 * @author Josh Kennedy
 * @author Devon Tackett
 * @author John Trimble
 */
public class ScanUtil
{
  /**
   * <p>Removes the resource name from the resource url to compute the url of the root resource.</p>
   *
   * @param resourceUrl a resource url obtained from ClassLoader.getResource(String) or ClassLoader.getResources(String)
   * @param resourceName the resource name that was used to obtain the resourceUrl.
   * @return the root url for the artifact that contains the resource.
   */
  public static URL computeResourceRoot( URL resourceUrl, String resourceName )
    throws Exception
  {
    String resourceString = resourceUrl.toString();

    // correct the root relative path if it ends in a different "/" character than the resource string.
    if( resourceString.endsWith("/") && !resourceName.endsWith("/") ) {
      resourceName = resourceName + "/";
    }
    else if( !resourceString.endsWith("/") && resourceName.endsWith("/") ) {
      resourceName = resourceName.substring(0, resourceName.length() - 1);
    }

    // ASSERT: the ends in the same "/" character as the resource string.

    // replaces the path with the correct number of ".." characters to make a relative path that points at the root.
    String relativePath = "./" + resourceName.replaceAll("\\A(?:(.+)/[^/]*|([^/]*))\\Z", "$1").replaceAll("[^/]+", "..");

    return new URL(resourceUrl, relativePath);
  }

}
