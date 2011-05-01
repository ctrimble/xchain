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
package org.xchain.framework.net.strategy;

import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import org.xchain.framework.net.UrlFactory;
import org.xchain.framework.net.UrlTranslationStrategy;

/**
 * @author Christian Trimble
 */
public  class CachedUrlTranslationStrategy
  implements UrlTranslationStrategy
{
  protected Map<String, String> cache = Collections.synchronizedMap(new HashMap<String, String>());

  public void registerCachedUrl( String url, String cacheUrl )
  {
    cache.put( url, cacheUrl );
  }

  /**
   * Translates a resource url into the actual url on the system.  If this strategy cannot translate the resouce url, 
   * then this method should return null.
   */
  public URL translateUrl( URL sourceUrl )
  {
    String externalUrl = sourceUrl.toExternalForm();
    String localUrl = cache.get(externalUrl);

    if( localUrl == null ) {
      return null;
    }
    else {
      try {
        return UrlFactory.getInstance().newUrl( localUrl );
      }
      catch( Exception e ) {
        throw new RuntimeException("Could not create local url '"+localUrl+"'.");
      }
    }
  }
}
