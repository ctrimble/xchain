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
package org.xchain.framework.net;

import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This uri resolver uses the UrlFactory to resolve input sources.
 *
 * @author Christian Trimble
 * @author Josh Kennedy
 */
public class UrlFactoryUriResolver
  implements URIResolver
{
  public static Logger log = LoggerFactory.getLogger(UrlFactoryUriResolver.class);
  public Source resolve( String href, String base )
    throws TransformerException
  {
    Source source = null;

    try {
      // get a reference to the factory.
      UrlFactory urlFactory = UrlFactory.getInstance();
      URL url = null;

      if( base != null && href != null ) {
        // get the url for the href and base.
        url = urlFactory.newUrl( base, href );
      }
      else if( href != null ) {
        url = urlFactory.newUrl( href );
      }
      else if( base != null ) {
        url = urlFactory.newUrl( base );
      }
      else {
        throw new TransformerException("Cannot resolve a uri where both the href and base supplied are null.");
      }

      // log this.
      if( log.isDebugEnabled() ) {
        log.debug("Resolved uri '"+href+"' in context '"+base+"' as url '"+url.toExternalForm()+"'.");
      }

      // create a source for the base and href.
      source = UrlSourceUtil.createTransformSource( url );
    }
    catch( Exception e ) {
      throw new TransformerException("Could not create source for href '"+href+"' and base '"+base+"'.", e);
    }

    // return the source.
    return source;
  }
}
