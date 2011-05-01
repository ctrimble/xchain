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
package org.xchain.framework.sax;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.IOException;

import java.net.URLConnection;
import org.xchain.framework.net.UrlFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Josh Kennedy
 */
public class UrlFactoryEntityResolver
  implements EntityResolver
{
  public static Logger log = LoggerFactory.getLogger(UrlFactoryEntityResolver.class);

  public UrlFactoryEntityResolver()
  {
    super();
  }

  public InputSource resolveEntity( String publicId, String systemId )
    throws SAXException, IOException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Resolving system id '"+systemId+"'.");
    }
    URLConnection connection = UrlFactory.getInstance().newUrl(systemId).openConnection();

    InputSource inputSource = new InputSource();

    inputSource.setPublicId( publicId );
    inputSource.setSystemId( systemId );
    inputSource.setByteStream(connection.getInputStream());

    return inputSource;
  }
}
