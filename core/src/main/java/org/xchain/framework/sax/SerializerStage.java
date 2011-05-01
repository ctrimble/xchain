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

import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.apache.xml.serializer.OutputPropertiesFactory;
import java.util.Properties;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.ContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This stage utilizes the Apache Xalan / Xerces Serializer to serialize the Result into an OutputStream
 * or a Writer.
 *
 * This stage must be the last stage in a pipeline and the result must be a StreamResult using either a OutputStream
 * or a Writer as it's destination.
 *
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Josh Kennedy
 */
public class SerializerStage
  implements Stage
{
  public static Logger log = LoggerFactory.getLogger( SerializerStage.class );

  // the apache xalan/xerces serializer
  protected Serializer serializer = null;

  public SerializerStage()
  {
    Properties outputProperties = OutputPropertiesFactory.getDefaultMethodProperties( "xml" );
    outputProperties.setProperty( "media-type", "text/xml" );
    serializer = SerializerFactory.getSerializer( outputProperties );
  }

  public SerializerStage( String method )
  {
    this(method, null);
  }

  public SerializerStage( String method, Boolean indent )
  {
    Properties outputProperties = OutputPropertiesFactory.getDefaultMethodProperties( method );

    if ( method.toLowerCase().equals("html") ) {
      if (indent == null) indent = true; // default to indenting mode
      outputProperties.setProperty( "media-type", "text/html" );
      outputProperties.setProperty( "doctype-system", "http://www.w3.org/TR/html4/loose.dtd" );
      outputProperties.setProperty( "doctype-public", "-//W3C//DTD HTML 4.01 Transitional//EN" );
    }
    else if ( method.toLowerCase().equals("xhtml") ) {
      if (indent == null) indent = true; // default to indenting mode
      outputProperties.setProperty( "media-type", "application/xhtml+xml" );
      outputProperties.setProperty( "omit-xml-declaration", "yes" ); // todo: should be browser sensitive
      outputProperties.setProperty( "doctype-system", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" );
      outputProperties.setProperty( "doctype-public", "-//W3C//DTD XHTML 1.0 Transitional//EN" );    
    }
    else if ( method.toLowerCase().equals("xml") ) {
      outputProperties.setProperty( "media-type", "text/xml" );
    }
    else if ( method.toLowerCase().equals("text") ) {
      outputProperties.setProperty( "media-type", "text/plain" );
    }

    if ( Boolean.TRUE.equals( indent )) {
      outputProperties.setProperty( "indent", "yes" );
      outputProperties.setProperty( "{http://xml.apache.org/xalan}indent-amount", "2" );
    }
    else {
      outputProperties.setProperty( "indent", "no" );
    }

    try {
      serializer = SerializerFactory.getSerializer( outputProperties );
    } catch (org.apache.xml.serializer.utils.WrappedRuntimeException e) {
      log.error("Serializer threw wrapped exception", e.getException());
      throw e;
    }
  }

  public ContentHandler getContentHandler()
  {
    ContentHandler handler = null;
    try {
      handler = serializer.asContentHandler();
    }
    catch( Exception e ) {
      // ignore
    }
    return handler;
  }

  public void setResult( Result result )
  {
    if( !(result instanceof StreamResult) ) {
      throw new IllegalStateException("The Serializer stage must be the last stage of a pipeline.");
    }
    if( ((StreamResult)result).getOutputStream() != null ) {
      serializer.setOutputStream( ((StreamResult)result).getOutputStream() );
    }
    else if( ((StreamResult)result).getWriter() != null ) {
      serializer.setWriter( ((StreamResult)result).getWriter() );
    }
    else if( ((StreamResult)result).getSystemId() != null ) {
      throw new IllegalStateException("The Serializer stage does not currently support a 'system-id' as the result.");
    }
  }

  /**
   * Returns the internet media type for this stage
   */
  public String getMediaType()
  {
    return (String)serializer.getOutputFormat().get("media-type");
  }

  /**
   * Returns the character encoding for this stage
   */
  public String getEncoding()
  {
    return (String)serializer.getOutputFormat().get("encoding");
  }

}
