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
package org.xchain.framework.net.protocol.resource;

import java.net.URL;
import org.xchain.framework.net.UrlTranslationStrategy;

/**
 * Base UrlTranslationStrategy implementation for ClassLoaders.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 */
public abstract class AbstractClassLoaderUrlTranslationStrategy
  implements UrlTranslationStrategy
{
  /**
   * Translate the given url to a resource path.  This will remove everything up
   * to the first '/'.
   * 
   * @param url The url to translate.
   * @return The url relative to the resource path.
   */
  public static String urlToResourcePath( URL url )
    throws Exception
  {
    String path = url.getPath();
  
    if( path == null ) {
      return path;
    }
    else {
      path = path.replaceFirst("\\A/", "");
    }

    return path;
  }
}
