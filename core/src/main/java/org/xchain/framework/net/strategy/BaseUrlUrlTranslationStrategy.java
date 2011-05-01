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

import org.xchain.framework.net.UrlFactory;
import org.xchain.framework.net.UrlUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xchain.framework.net.UrlTranslationStrategy;

/**
 * @author Christian Trimble
 * @author Mike Moulton
 * @author Josh Kennedy
 */
public class BaseUrlUrlTranslationStrategy
  implements UrlTranslationStrategy
{
  public static Logger log = LoggerFactory.getLogger( BaseUrlUrlTranslationStrategy.class );

  public static String URL_FACTORY_URL_SOURCE = "url-factory";
  public static String CONSTRUCTOR_URL_SOURCE = "constructor";

  protected URL baseUrl;
  protected String urlSource = URL_FACTORY_URL_SOURCE;

  public BaseUrlUrlTranslationStrategy() {}

  public BaseUrlUrlTranslationStrategy(URL baseUrl, String urlSource)
  {
    this.baseUrl = baseUrl;
    this.urlSource = urlSource;
  }

  public void setBaseUrl( URL baseUrl ) { this.baseUrl = baseUrl; }
  public URL getBaseUrl() { return this.baseUrl; }
  public void setUrlSource( String urlSource ) { this.urlSource = urlSource; }
  public String getUrlSource() { return this.urlSource; }

  public URL translateUrl( URL url )
  {
    StringBuffer relativePath = new StringBuffer();

    relativePath.append( baseUrl.toExternalForm() );

    // get the path from the url.
    String path = url.getPath();
    if( path != null ) {
      relativePath.append(path);
    }

    // get the query string.
    String query = url.getQuery();
    if( query != null ) {
      relativePath.append("?").append(query);
    }

    // get the reference.
    String ref = url.getRef();
    if( ref != null ) {
      relativePath.append("#").append(ref);
    }

    URL translatedUrl = null;

    try {
      if( URL_FACTORY_URL_SOURCE.equals(urlSource) ) {
        translatedUrl = UrlFactory.getInstance().newUrl( relativePath.toString() );
      }
      else if( CONSTRUCTOR_URL_SOURCE.equals(urlSource) ) {
        translatedUrl = new URL( relativePath.toString() );
      }
      else {
        throw new IllegalStateException("The url source '"+urlSource+"' is not a defined url source.");
      }

      if( log.isDebugEnabled() ) {
        log.debug("The url '"+url.toExternalForm()+"' translated into url '"+translatedUrl.toExternalForm()+"' for base url '"+baseUrl.toExternalForm()+"'.");
      }

      // make sure that the url exists.
      if( UrlUtil.getInstance().exists(translatedUrl)) {
        return translatedUrl;
      }
    }
    catch( Exception e ) {
      if( log.isDebugEnabled() ) {
        log.debug("Could not translate url '"+url.toExternalForm()+"' to the base url '"+baseUrl.toExternalForm()+"', because an exception was thrown.", e);
      }

      return null;
    }

    // the translated url does not exists, return null.
    if( log.isDebugEnabled() ) {
      log.debug("The url '"+url.toExternalForm()+"' could not be translated to the base url '"+baseUrl+"' because the resulting url does not exist.");
    }

    return null;
  }
}
