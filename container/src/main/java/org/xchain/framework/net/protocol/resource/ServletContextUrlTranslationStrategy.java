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
import javax.servlet.ServletContext;
import org.xchain.framework.net.UrlTranslationStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UrlTranslationStrategy implementation to load resources from the servlet context.  The servlet context must be set prior to use for urls to be properly translated.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
public class ServletContextUrlTranslationStrategy
  implements UrlTranslationStrategy
{
  public static Logger log = LoggerFactory.getLogger( ServletContextUrlTranslationStrategy.class );

  /** The servlet context to translate from. */
  protected static ServletContext servletContext = null;

  public static void setServletContext( ServletContext servletContext ) { ServletContextUrlTranslationStrategy.servletContext = servletContext; }
  public static ServletContext getServletContext() { return ServletContextUrlTranslationStrategy.servletContext; }

  public URL translateUrl( URL resourceUrl )
  {
    if( servletContext == null ) {
      return null;
    }

    try {
      // get the resource from the defined servlet context.
      return servletContext.getResource(urlToServletResourcePath(resourceUrl));
    }
    catch( Exception e ) {
      if( log.isDebugEnabled() ) {
        log.debug("Could not find resource for resource url '"+resourceUrl+"'.", e);
      }
      return null;
    }
  }

  public static String urlToServletResourcePath( URL url )
  {
    String path = url.getPath();

    if( path == null || path.equals("") ) {
      path = "/";
    }

    return path;
  }
}
