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
package org.xchain.framework.digester;

import org.xchain.framework.sax.HandlerWrapper;
import org.xchain.framework.sax.util.XHtmlHandler;

import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.apache.xml.serializer.OutputPropertiesFactory;

import org.apache.commons.digester.Rule;
import org.apache.commons.digester.Digester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.io.StringWriter;

import java.util.Iterator;
import java.util.Properties;
import java.util.Map;

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Christian Trimble
 * @author Josh Kennedy
 */
public class SerializationRule
  extends Rule
{
  public static Logger log = LoggerFactory.getLogger( SerializationRule.class );

  //public static String HTML_METHOD = Method.HTML;
  //public static String TEXT_METHOD = Method.TEXT;
  //public static String XML_METHOD  = Method.XML;

  private class RuleSerializationHandler
    extends HandlerWrapper
  {
    private int depth = 0;

    public void startElement( String namespaceUri, String localName, String qName, Attributes atts)
      throws SAXException
    {
      depth++;

      // if we are not including the containing element, then do not pass it on.  Otherwise, pass this event on to super.
      if( depth == 1 && includeContainingElement || depth > 1 ) {
        super.startElement( namespaceUri, localName, qName, atts );
      }

      // if the depth is ever less than or equal to zero, then things are broken.
      else if( depth <= 0 ) {
        throw new IllegalStateException("Unmatched start and end elements detected.");
      }
    }

    public void endElement( String namespaceUri, String localName, String qName )
      throws SAXException
    {
      // if we are not including the containing element, then do not pass it on.
      if( depth == 1 && includeContainingElement || depth > 1 ) {
        super.endElement( namespaceUri, localName, qName );
      }

      // decrement the depth.
      depth--;

      // close down the document and return control to the rule.
      if( depth <= 0 ) {
        Digester digester = getDigester();

        // reset the handlers.
        digester.setCustomContentHandler(oldCustomContentHandler);
        oldCustomContentHandler = null;
        if( digester instanceof ExtendedDigester ) {
          ((ExtendedDigester)digester).setCustomLexicalHandler(oldCustomLexicalHandler);
          oldCustomLexicalHandler = null;
        }

        // close all of the namespace mappings.
        Iterator currentNamespaceIterator = digester.getCurrentNamespaces().entrySet().iterator();
        while( currentNamespaceIterator.hasNext() ) {
          Map.Entry currentNamespace = (Map.Entry)currentNamespaceIterator.next();
          handler.endPrefixMapping((String)currentNamespace.getKey());
        }

        // terminate the document.
        super.endDocument();

        // pass control back to the digester.
        digester.endElement( namespaceUri, localName, qName );
      }
    }
  }

  /** The handler that will create the serialized form of the nodes. */
  protected RuleSerializationHandler handler = null;

  /** The old custom handler that we displaced. */
  protected ContentHandler oldCustomContentHandler;

  /** The old lexical handler that we displaced. */
  protected LexicalHandler oldCustomLexicalHandler;

  /** The method that we will be using to render the output. */
  protected String method = "text";

  protected Boolean indent = Boolean.TRUE;

  /** The budder that we will be writting to. */
  protected StringBuffer buffer = null;

  /**
   *   If true, the element that caused this rule to file will be passed to the serializer, otherwise the next element
   * after this rule will be sent.
   */
  protected boolean includeContainingElement = false;

  public void begin(String namespaceUri, String name, Attributes attributes)
    throws Exception
  {
    // get the digester.
    Digester digester = getDigester();

    // store the old content handlers.
    oldCustomContentHandler = digester.getCustomContentHandler();
    if( digester instanceof ExtendedDigester ) {
      oldCustomLexicalHandler = ((ExtendedDigester)digester).getCustomLexicalHandler();
    }

    // create the handler.
    handler = new RuleSerializationHandler();
 
    // set up the handlers that will do the serialization.
    handler.setWrappedHandler(newHandler());

    // set the new handlers.
    digester.setCustomContentHandler(handler);
    if( digester instanceof ExtendedDigester ) {
      ((ExtendedDigester)digester).setCustomLexicalHandler(handler);
    }

    // push the buffer onto the stack.
    digester.push(buffer);

    // start the document.
    handler.startDocument();

    // set all of the namespaces that are defined on the digester.
    Iterator currentNamespaceIterator = digester.getCurrentNamespaces().entrySet().iterator();
    while( currentNamespaceIterator.hasNext() ) {
      Map.Entry currentNamespace = (Map.Entry)currentNamespaceIterator.next();
      handler.startPrefixMapping((String)currentNamespace.getKey(), (String)currentNamespace.getValue());
    }

    // send the current element to the handler.
    if( digester.getNamespaceAware() ) {
      handler.startElement(namespaceUri, name, digester.getCurrentElementName(), attributes);
    }
    else {
      handler.startElement(namespaceUri, name, name, attributes);
    }
  }

  public void end()
    throws Exception
  {
    getDigester().pop();
  }

  /**
   * Returns a new handler for the body of a serializer block.
   */
  protected ContentHandler newHandler()
    throws Exception
  {
    Serializer serializer = newSerializer();
    if( method.toLowerCase().equals("html") ) {
      // wrap the html handler.
      XHtmlHandler xhtmlHandler = new XHtmlHandler();
      xhtmlHandler.setWrappedHandler(serializer);
      return xhtmlHandler;
    }
    else {
      return serializer.asContentHandler();
    }
  }

  /**
   * Sets up the wrapped handler.
   */
  protected Serializer newSerializer()
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
      Serializer serializer = SerializerFactory.getSerializer( outputProperties );
      serializer.setWriter(newStringWriter());
      return serializer;
    } catch (org.apache.xml.serializer.utils.WrappedRuntimeException e) {
      log.error("Serializer threw wrapped exception", e.getException());
      throw e;
    }

  /*
    if( TEXT_METHOD.equals(method ) ) {
      // create the content handler.
      ToTextStream toTextStream = new ToTextStream();
      toTextStream.setOutputFormat(newTextProperties());

      // set the writer.
      toTextStream.setWriter(newStringWriter());

      // return the handler.
      return toTextStream;
    }
    else if( HTML_METHOD.equals(method) ) {
      // create the handler.
      ToHTMLStream toHtmlStream = new ToHTMLStream();

      // configure the handler for html output.
      toHtmlStream.setOutputFormat(newHtmlProperties());

      // set the writer.
      toHtmlStream.setWriter(newStringWriter());

      // wrap the html handler.
      XHtmlHandler xhtmlHandler = new XHtmlHandler();
      xhtmlHandler.setWrappedHandler(toHtmlStream);

      return xhtmlHandler;
    }
    else if( XML_METHOD.equals(method) ) {
      // create the handler.
      ToXMLStream toXmlStream = new ToXMLStream();

      // configure the handler for html output.
      toXmlStream.setOutputFormat(newHtmlProperties());

      // set the writer.
      toXmlStream.setWriter(newStringWriter());

      return toXmlStream;
    }
    else {
      throw new IllegalStateException("Invalid method specified.");
    }
    */
  }

  /*
  protected Properties newTextProperties()
  {
    return OutputPropertiesFactory.getDefaultMethodProperties(Method.TEXT);
  }

  protected Properties newHtmlProperties()
  {
    Properties properties = OutputPropertiesFactory.getDefaultMethodProperties(Method.HTML);
    properties.put(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "2");
    return properties;
  }

  protected Properties newXmlProperties()
  {
    Properties properties = OutputPropertiesFactory.getDefaultMethodProperties(Method.XML);
    properties.put(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "2");
    return properties;
  }
   */

  protected StringWriter newStringWriter()
  {
    StringWriter stringWriter = new StringWriter();
    buffer = stringWriter.getBuffer();
    return stringWriter;
  }
}
