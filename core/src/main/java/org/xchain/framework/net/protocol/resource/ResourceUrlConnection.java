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

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.net.URL;
import org.xchain.framework.net.UrlConnectionWrapper;
import org.xchain.framework.net.UrlTranslationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides url connections for the resource protocol.  There are two authorities defined by default:
 * <ul>
 *   <li>system-class-loader - Loads resources from the system class loader.</li>
 *   <li>context-class-loader - Loads resources from the context class loader.</li>
 * </ul>
 *
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
public class ResourceUrlConnection
  extends UrlConnectionWrapper
{
  public static Logger log = LoggerFactory.getLogger( ResourceUrlConnection.class );

  public static String SYSTEM_CLASS_LOADER_ATHORITY  = "system-class-loader";
  public static String CONTEXT_CLASS_LOADER_ATHORITY = "context-class-loader";  

  protected static Map<String, UrlTranslationStrategy> urlTranslationStrategyMap = Collections.synchronizedMap(new HashMap<String, UrlTranslationStrategy>());

  public static void registerUrlTranslationStrategy( String authority, UrlTranslationStrategy strategy )
  {
    if( log.isDebugEnabled() ) {
      log.debug("Registering url transltion strategy for type '"+strategy.getClass().getName()+"' to authority '"+authority+"'.");
    }

    urlTranslationStrategyMap.put( authority, strategy );
  }

  public static Map<String, UrlTranslationStrategy> getUrlTranslationStrategyMap()
  {
    return urlTranslationStrategyMap;
  }

  public static UrlTranslationStrategy getUrlTranslationStrategy( String authority )
  {
    return (UrlTranslationStrategy)getUrlTranslationStrategyMap().get(authority);
  }

  static {
    registerUrlTranslationStrategy( SYSTEM_CLASS_LOADER_ATHORITY, new SystemClassLoaderUrlTranslationStrategy() );
    registerUrlTranslationStrategy( CONTEXT_CLASS_LOADER_ATHORITY, new ContextClassLoaderUrlTranslationStrategy() );
  }

  public ResourceUrlConnection( URL url )
  {
    super(url);

    // get the authority from the url.
    String authority = url.getAuthority();

    // just make sure that the authority isn't null when not specified.
    if( authority == null ) {
      authority = "";
    }

    // get the url translation strategy for this authority.
    UrlTranslationStrategy urlTranslationStrategy = getUrlTranslationStrategy( authority );

    if( urlTranslationStrategy == null ) {
      throw new RuntimeException("Could not find url translation strategy for url '"+url.toExternalForm()+"'.");
    }

    if( log.isDebugEnabled() ) {
      log.debug("Starting translation of url '"+url.toExternalForm()+"' with translation strategy of type '"+urlTranslationStrategy.getClass().getName()+"'.");
    }

    // translate the url.
    URL translatedUrl = urlTranslationStrategy.translateUrl( url );

    // if the translated url is null, then the resource does not exist.
    if( translatedUrl == null ) {
      this.wrapped = new ResourceNotFoundUrlConnection( url );
    }
    else {

      // log the translated url we are using.
      if( log.isDebugEnabled() ) {
        log.debug("The resource url '"+url.toExternalForm()+"' translated to '"+translatedUrl.toExternalForm()+"'.");
      }

      try {
        this.wrapped = translatedUrl.openConnection();
      }
      catch( Exception e ) {
        throw new RuntimeException("Could not open a connection to the resource '"+url.toExternalForm()+"'.");
      }
    }
  }
}
