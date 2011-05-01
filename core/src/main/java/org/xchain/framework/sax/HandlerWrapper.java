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

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Josh Kennedy
 */
public class HandlerWrapper
  implements ContentHandler, LexicalHandler, DTDHandler
{
  public static Logger log = LoggerFactory.getLogger(HandlerWrapper.class);

  protected ContentHandler contentHandler = null;
  protected LexicalHandler lexicalHandler = null;
  protected DTDHandler dtdHandler = null;

  public void setWrappedContentHandler( ContentHandler contentHandler ) { this.contentHandler = contentHandler; }
  public ContentHandler getWrappedContentHandler() { return this.contentHandler; }
  public void setWrappedLexicalHandler( LexicalHandler lexicalHandler ) { this.lexicalHandler = lexicalHandler; }
  public LexicalHandler getWrappedLexicalHandler() { return this.lexicalHandler; }
  public void setWrappedDtdHandler( DTDHandler dtdHandler ) { this.dtdHandler = dtdHandler; }
  public DTDHandler getWrappedDtdHandler() { return this.dtdHandler; }

  public void setWrappedHandler( Object handler )
  {
    if( log.isDebugEnabled() ) {
      log.debug("Setting wrapped handler.");
    }

    if( handler instanceof ContentHandler ) {
      contentHandler = (ContentHandler)handler;
    }
    else {
      contentHandler = null;
    }
    if( handler instanceof LexicalHandler ) {
      lexicalHandler = (LexicalHandler)handler;
    }
    else {
      lexicalHandler = null;
    }

    if( handler instanceof DTDHandler ) {
      dtdHandler = (DTDHandler)handler;
    }
    else {
      dtdHandler = null;
    }

    if( log.isDebugEnabled() ) {
      log.debug("The content handler is "+((contentHandler==null)?"null":"not null")+".");
      log.debug("The lexical handler is "+((lexicalHandler==null)?"null":"not null")+".");
      log.debug("The dtd handler is "+((dtdHandler==null)?"null":"not null")+".");
    }
  }
  

  /*
   * ContentHandler methods.
   */
  public void characters( char[] characters, int start, int length )
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling characters on wrapped handler.");
    }
    if( contentHandler != null ) {
      contentHandler.characters( characters, start, length );
    }
  }

  public void endDocument()
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling endDocument on wrapped handler.");
    }
    if( contentHandler != null ) {
      contentHandler.endDocument();
    }
  }

  public void endElement( String namespaceUri, String localName, String qName )
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling endElement on wrapped handler. ns:'"+namespaceUri+"' local-name:'"+localName+"'.");
    }

    if( contentHandler != null ) {
      contentHandler.endElement( namespaceUri, localName, qName );
    }
  }

  public void endPrefixMapping( String prefix )
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling endPrefixMapping on wrapped handler. prefix:'"+prefix+"'.");
    }

    if( contentHandler != null ) {
      contentHandler.endPrefixMapping( prefix );
    }
  }

  public void ignorableWhitespace( char[] characters, int start, int length )
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling ignorable whitespace on wrapped handler.");
    }

    if( contentHandler != null ) {
      contentHandler.ignorableWhitespace( characters, start, length );
    }
  }

  public void processingInstruction( String target, String data )
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling processing instruction on wrapped handler.");
    }

    if( contentHandler != null ) {
      contentHandler.processingInstruction( target, data );
    }
  }

  public void setDocumentLocator( Locator locator )
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling document locator on wrapped handler.");
    }

    if( contentHandler != null ) {
      contentHandler.setDocumentLocator( locator );
    }
  }

  public void skippedEntity( String name )
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling skipped entity on wrapped handler.");
    }

    if( contentHandler != null ) {
      contentHandler.skippedEntity( name );
    }
  }

  public void startDocument( )
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling startDocument on wrapped handler.");
    }
    if( contentHandler != null ) {
      contentHandler.startDocument( );
    }
  }

  public void startElement( String namespaceUri, String localName, String qName, Attributes attributes )
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling startElement on wrapped handler. ns:'"+namespaceUri+"' local-name:'"+localName+"'.");
    }
    if( contentHandler != null ) {
      contentHandler.startElement( namespaceUri, localName, qName, attributes );
    }
  }

  public void startPrefixMapping( String prefix, String uri )
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling startPrefixMapping on wrapped handler. prefix:'"+prefix+"' uri:'"+uri+"'.");
    }

    if( contentHandler != null ) {
      contentHandler.startPrefixMapping( prefix, uri );
    }
  }

  /*
   * LexicalHandler methods.
   */

  public void comment( char[] characters, int start, int length )
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling comment on wrapped handler.");
    }

    if( lexicalHandler != null ) {
      lexicalHandler.comment( characters, start, length );
    }
  }

  public void endCDATA( )
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling end CDATA on wrapped handler.");
    }

    if( lexicalHandler != null ) {
      lexicalHandler.endCDATA(  );
    }
  }

  public void endDTD(  )
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling endDTD on wrapped handler.");
    }

    if( lexicalHandler != null ) {
      lexicalHandler.endDTD(  );
    }
  }

  public void endEntity( String name )
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling enEdntity on wrapped handler.");
    }

    if( lexicalHandler != null ) {
      lexicalHandler.endEntity( name );
    }
  }

  public void startCDATA( )
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling startCDATA on wrapped handler.");
    }

    if( lexicalHandler != null ) {
      lexicalHandler.startCDATA(  );
    }
  }

  public void startDTD( String name, String publicId, String systemId )
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling startDTD on wrapped handler.");
    }

    if( lexicalHandler != null ) {
      lexicalHandler.startDTD( name, publicId, systemId );
    }
  }

  public void startEntity( String name )
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling startEntity on wrapped handler.");
    }

    if( lexicalHandler != null ) {
      lexicalHandler.startEntity( name );
    }
  }

  /*
   * DTDHandler methods.
   */
  public void notationDecl( String name, String publicId, String systemId )
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling notationDecl on wrapped handler.");
    }

    if( dtdHandler != null ) {
      dtdHandler.notationDecl( name, publicId, systemId );
    }
  }

  public void unparsedEntityDecl( String name, String publicId, String systemId, String notationName )
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling unparsedEntityDecl on wrapped handler.");
    }

    if( dtdHandler != null ) {
      dtdHandler.unparsedEntityDecl( name, publicId, systemId, notationName );
    }
  }
}
