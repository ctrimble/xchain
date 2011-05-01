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

import org.xchain.Command;
import org.xchain.framework.factory.CatalogFactory;

import org.apache.commons.jxpath.JXPathContext;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.ext.LexicalHandler;
import java.io.IOException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Josh Kennedy
 */
public class CommandXmlReader
  implements XMLReader
{
  public static Logger log = LoggerFactory.getLogger(CommandXmlReader.class);

  protected Command command = null;
  protected CommandHandler commandHandler = new CommandHandlerImpl();
  protected ContentHandler contentHandler = null;
  protected DTDHandler dtdHandler = null;
  protected ErrorHandler errorHandler = null;
  protected EntityResolver entityResolver = null;

  /**
   * Creates a command xml reader using a catalog name and a command name.
   */
  public CommandXmlReader( String catalogName, String commandName )
    throws SAXException
  {
    try {
      this.command = CatalogFactory.getInstance().getCatalog( catalogName ).getCommand( commandName );
    }
    catch( Exception e ) {
      throw new SAXException("Could not find command for catalog '"+catalogName+"' and command name '"+commandName+"'.", e);
    }
  }

  /**
   * Creates a command xml reader using the provided command.
   */
  public CommandXmlReader( Command command )
  {
    this.command = command;
  }

  /**
   * Returns the handler that commands should use to output events.
   */
  public CommandHandler getCommandHandler() { return commandHandler; }

  public void setContentHandler( ContentHandler contentHandler ) { this.contentHandler = contentHandler; }
  public ContentHandler getContentHandler() { return this.contentHandler; }

  public void setDTDHandler( DTDHandler dtdHandler ) { this.dtdHandler = dtdHandler; }
  public DTDHandler getDTDHandler() { return this.dtdHandler; }

  public void setErrorHandler( ErrorHandler errorHandler ) { this.errorHandler = errorHandler; }
  public ErrorHandler getErrorHandler() { return this.errorHandler; }

  public void setEntityResolver( EntityResolver entityResolver ) { this.entityResolver = entityResolver; }
  public EntityResolver getEntityResolver() { return this.entityResolver; }

  public boolean sendEvents() { return true; }

  public boolean getFeature( String name )
    throws SAXNotRecognizedException, SAXNotSupportedException
  {
    throw new SAXNotRecognizedException("Property '"+name+"' is not supported.");
  }

  public void setFeature( String name, boolean value )
    throws SAXNotRecognizedException, SAXNotSupportedException
  {
    throw new SAXNotRecognizedException("Property '"+name+"' is not supported.");
  }

  public void setProperty( String name, Object value )
    throws SAXNotRecognizedException, SAXNotSupportedException
  {
    throw new SAXNotRecognizedException("Property '"+name+"' is not supported.");
  }

  public Object getProperty( String name )
    throws SAXNotRecognizedException, SAXNotSupportedException
  {
    throw new SAXNotRecognizedException("Property '"+name+"' is not supported.");
  }

  public void parse( InputSource inputSource )
    throws IOException, SAXException
  {
    if( !(inputSource instanceof ContextInputSource) ) {
      throw new IllegalArgumentException("CommandXmlReaders can only parse ContextInputSources.");
    }

    // get the context from the input source.
    ContextInputSource contentInputSource = (ContextInputSource)inputSource;
    JXPathContext context = contentInputSource.getContext();

    // execute the command.
    try {
      // start the document.
      commandHandler.startDocument();

      // execute the command.
      command.execute(context);

      // end the document.
      commandHandler.endDocument();
    }
    catch( SAXException saxe ) {
      throw saxe;
    }
    catch( IOException ioe ) {
      throw ioe;
    }
    catch( Exception e ) {
      //log.error("Exception thrown while parsing context.", e);
      throw new SAXException("Exception thrown while parsing context.", e);
    }
  }

  public void parse( String systemId )
    throws IOException, SAXException
  {
    throw new UnsupportedOperationException("The CommandXmlReader cannot parse a systemId.");
  }

  /**
   * This is the class that sax event commands write to.  Calling methods on this class passes the events on to
   * the content handler of the containing xml reader.
   */
  public class CommandHandlerImpl
    extends CommandHandler
  {
    public ContentHandler contentHandler()
    {
      return contentHandler;
    }

    public DTDHandler dtdHandler()
    {
      return dtdHandler;
    }

    public LexicalHandler lexicalHandler()
    {
      if( contentHandler instanceof LexicalHandler ) {
        return (LexicalHandler)contentHandler;
      }

      return null;
    }
  }
}
