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
package org.xchain.framework.util;

import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

/**
 * A least recently used cache map.  When adding a new entry to this map, if the number of entries exceeds the configured maximum size then the eldest entry will be dropped.
 *
 * @param <T> The class to key upon.
 * @param <S> The data class.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
public class LruCacheMap<T, S>
  extends LinkedHashMap<T, S>
{
  public static Logger log = LoggerFactory.getLogger( LruCacheMap.class );

  protected int cacheSize;

  /**
   * @param cacheSize The maximum number of entries in the cache map.
   */
  public LruCacheMap( int cacheSize )
  {
    super( cacheSize+1, 1);
    this.cacheSize = cacheSize;
  }

  /**
   * The eldest entry should be removed if the current size exceeds the maximum size.
   */
  protected boolean removeEldestEntry( Map.Entry<T, S> eldest )
  {
    boolean remove = size() > this.cacheSize;

    if( remove && log.isDebugEnabled() ) {
      log.debug("Removing cache entry for '"+eldest.getKey()+"'.");
    }

    return remove;
  }
}
