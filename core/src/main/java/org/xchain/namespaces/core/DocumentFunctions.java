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
package org.xchain.namespaces.core;

import java.net.URL;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.xchain.framework.net.UrlFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Trimble
 * @author Jason Rose
 * @author Josh Kennedy
 */
public class DocumentFunctions
{
  public static Logger log = LoggerFactory.getLogger( DocumentFunctions.class );
    public static Document document( String systemId )
    {
      URL url = null;

      try {
        url = UrlFactory.getInstance().newUrl(systemId);
      }
      catch( Exception e ) {
        if( log.isDebugEnabled() ) {
          log.debug("Could not create document for system id '"+systemId+".", e);
        }
        throw new RuntimeException("Could not create document.", e);
      }

      return document(url);
    }

    public static Document document( URL url )
    {
      InputStream documentIn = null;
      try {
      // get the document builder factory.
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

      // configure the factory to be namespace aware.
      factory.setNamespaceAware( true );

      // get the document builder.
      DocumentBuilder documentBuilder = factory.newDocumentBuilder();

      // create an input stream for the document.
      documentIn = url.openStream();

      // build the document.
      Document document = documentBuilder.parse(documentIn);

      // return the document.
      return document;
      }
      catch( Exception e ) {
        if( log.isDebugEnabled() ) {
          log.debug("Could not create document for system id '"+url.toExternalForm()+".", e);
        }
        throw new RuntimeException("Could not create document.", e);

      }
      finally {
        if( documentIn != null ) {
          try {
            documentIn.close();
          }
          catch( Exception e ) {
            log.warn("Failed to close input stream.", e);
          }
        }
      }
    }
}
