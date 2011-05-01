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

import java.io.IOException;

import java.net.URI;
import java.net.URL;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xchain.framework.lifecycle.XmlFactoryLifecycle;
import org.xchain.framework.factory.TemplatesFactory;
import org.xchain.framework.strategy.TemplatesConsumerStrategy;
import org.xchain.framework.strategy.SourceSourceStrategy;
import org.xchain.framework.strategy.InputSourceSourceStrategy;
import org.xchain.framework.util.ParserUtil;
import org.xchain.framework.util.ParseException;
import org.xchain.framework.util.ParsedTransformer;
import org.xchain.framework.net.DependencyTracker;
import org.xchain.framework.net.UrlFactoryUriResolver;
import org.xchain.framework.net.UrlFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.DTDHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * This filter handles <?xchain-stylesheet?> processing instructions.  These can be used to style the contents
 * of the xchain before it is loaded.
 * <?xchain-stylesheet system-id="" parameters="qname='value', qname='value', qname='value'"?>
 *
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Jason Rose
 * @author Josh Kennedy
 */
public class XChainDeclFilter
  extends XMLFilterImpl
{
  private static Logger log = LoggerFactory.getLogger(XChainDeclFilter.class);

  public static String XCHAIN_STYLESHEET_TARGET = "xchain-stylesheet";

  /** True if we are in the prolog, false otherwise. */
  private boolean inProlog = false;

  /** The list of filters that we need to add into the filter chain. */
  private List<TransformerHandler> handlerList = new ArrayList<TransformerHandler>();

  /** The list of sax events that we are caching until the prolog is finished. */
  private List<SaxEvent> eventList = new ArrayList<SaxEvent>();

  private ContentHandler contentHandler;
  private ErrorHandler errorHandler;
  private EntityResolver entityResolver;
  private DTDHandler dtdHandler;
  private Locator locator;
  private String baseUri;

  public void setContentHandler( ContentHandler contentHandler ) { this.contentHandler = contentHandler; }
  public ContentHandler getContentHandler() { return this.contentHandler; }
  public void setErrorHandler( ErrorHandler errorHandler ) { this.errorHandler = errorHandler; }
  public ErrorHandler getErrorHandler() { return this.errorHandler; }
  public void setEntityResolver( EntityResolver entityResolver ) { this.entityResolver = entityResolver; }
  public EntityResolver getEntityResolver() { return this.entityResolver; }
  public void setDTDHandler( DTDHandler dtdHandler ) { this.dtdHandler = dtdHandler; }
  public DTDHandler getDTDHandler() { return this.dtdHandler; }
  public void setDocumentLocator( Locator locator ) { this.locator = locator; }
  public Locator getDocumentLocator() { return this.locator; }

  public void parse( String systemId )
    throws SAXException, IOException
  {
    baseUri = systemId;
    super.parse(systemId);
  }

  public void parse( InputSource source )
    throws SAXException, IOException
  {
    baseUri = source.getSystemId();
    super.parse(source);
  }

  /**
   * Prepares the filter to start tracking prolog events and caches the start document event.
   */
  public void startDocument()
    throws SAXException
  {
    // we are now in the prolog.
    inProlog = true;
    handlerList.clear();
    eventList.clear();

    // track the start document event.
    eventList.add(new StartDocumentEvent());
  }

  public void endDocument()
    throws SAXException
  {
    if( inProlog ) {
      endProlog();
    }

    super.endDocument();
  }

  /**
   * Handles <?xchain-stylesheet?> processing instructions and caches all others.
   */
  public void processingInstruction(String target, String data)
    throws SAXException
  {
    if( XCHAIN_STYLESHEET_TARGET.equals(target) && inProlog ) {
      addTransformerHandler(data);
    }
    else if( inProlog ) {
      // cache the processing instruction.
      eventList.add(new ProcessingInstructionEvent(target, data));
    }
    else {
      super.processingInstruction(target, data);
    }
  }

  /**
   * Caches notation decl events until the prolog is finished.
   */
  public void notationDecl(String name, String publicId, String systemId)
    throws SAXException
  {
    if( inProlog ) {
      eventList.add(new NotationDeclEvent(name, publicId, systemId));
    }
    else {
      super.notationDecl(name, publicId, systemId);
    }
  }

  /**
   * Caches unparsed entity decl events until the prolog is finished.
   */
  public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
    throws SAXException
  {
    if( inProlog ) {
      eventList.add(new UnparsedEntityDeclEvent(name, publicId, systemId, notationName));
    }
    else {
      super.unparsedEntityDecl(name, publicId, systemId, notationName);
    }
  }

  /**
   * If this start element is the end of the prolog, then endProlog() is called.  Then start
   * elements are passed along.
   */
  public void startElement( String uri, String localName, String qName, Attributes attributes )
    throws SAXException
  {
    if( inProlog ) {
      endProlog();
    }

    super.startElement( uri, localName, qName, attributes );
  }

  /**
   * If this start prefix mapping is the end of the prolog, then endProlog() is called.  Then
   * the start prefix mapping event is passed along.
   */
  public void startPrefixMapping( String prefix, String uri )
    throws SAXException
  {
    if( inProlog ) {
      endProlog();
    }

    super.startPrefixMapping( prefix, uri );
  }

  /**
   * Parses a data section and creates a new transformer handler.
   */
  private void addTransformerHandler( String data )
    throws SAXException
  {
    ParsedTransformer parsedTransformer = null;
    TransformerHandler handler = null;
    try {
      parsedTransformer = ParserUtil.parseTransformer(data);
    }
    catch( ParseException pe ) {
      throw new SAXException("Could not parse the xchain-stylesheet processing instruction.", pe);
    }

    String systemId = parsedTransformer.getAttributes().get("system-id");

    // we must have a system id.
    if( systemId == null ) {
      throw new SAXException("<?xchain-stylesheet?> processing instructions require the system-id attribute.");
    }

    // resolve the systemId against our parents locator.
    if( baseUri != null ) {
      systemId = URI.create(baseUri).resolve(systemId).toString();
    }

    DependencyTracker.getInstance().startTracking();

    // create the strategies for loading templates.
    try {
      DependencyTracker.getInstance().dependencyFound(UrlFactory.getInstance().newUrl(systemId));
      // create the transformer handler for the templates object.
      handler = XmlFactoryLifecycle.newTransformerHandler(systemId);

      Transformer transformer = handler.getTransformer();
      //transformer.setURIResolver(DependencyTracker.getInstance().createDependencyUriResolver(new UrlFactoryUriResolver()));
      transformer.setURIResolver(new LoggingUriResolver(DependencyTracker.getInstance().createDependencyUriResolver(new UrlFactoryUriResolver())));
      for( Map.Entry<String, String> parameter : parsedTransformer.getParameters().entrySet() ) {
        transformer.setParameter(parameter.getKey(), parameter.getValue());
      }
      for( Map.Entry<String, String> outputProperty : parsedTransformer.getOutputProperties().entrySet() ) {
        transformer.setOutputProperty(outputProperty.getKey(), outputProperty.getValue());
      }
    }
    //catch( SAXException saxe ) {
      //throw saxe;
    //}
    catch( Exception e ) {
      throw new SAXException("Could not create transformer for system id '"+systemId+"' due to an exception.", e);
    }
    finally {
      Set<URL> dependencies = DependencyTracker.getInstance().stopTracking();
      if( log.isDebugEnabled() ) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("The system id '"+systemId+"' has the following dependencies:\n");
        for( URL url : dependencies ) {
          logBuilder.append(url.toExternalForm()).append("\n");
        }
        log.debug(logBuilder.toString());
      }
    }

    handlerList.add(handler);
  }

  private String resolveEntities( String data )
  {
    return data;
  }

  /**
   * Starts passing events to the filter.
   */
  private void endProlog()
    throws SAXException
  {
    inProlog = false;

    // if the filter list is not empty, then reconfigure the filter chain.
    if( handlerList.isEmpty() ) {
      super.setContentHandler(contentHandler);
      super.setDTDHandler(dtdHandler);
      super.setEntityResolver(entityResolver);
      super.setErrorHandler(errorHandler);
      contentHandler.setDocumentLocator(locator);
    }
    else {
      // this instance if the parent of the first filter.
      // the first through nth filters are attached to the previous filter.
      for( int i = 0; i < handlerList.size()-1; i++ ) {
        handlerList.get(i).setDocumentLocator(locator);
        handlerList.get(i).setResult(new SAXResult(handlerList.get(i+1)));
      }

      handlerList.get(handlerList.size()-1).setResult(new SAXResult(contentHandler));

      handlerList.get(0).setDocumentLocator(locator);
      super.setContentHandler(handlerList.get(0));
      if( handlerList.get(0) instanceof ErrorHandler ) {
        super.setErrorHandler((ErrorHandler)handlerList.get(0));
      }
      if( handlerList.get(0) instanceof EntityResolver ) {
        super.setEntityResolver((EntityResolver)handlerList.get(0));
      }
      if( handlerList.get(0) instanceof DTDHandler ) {
        super.setDTDHandler((DTDHandler)handlerList.get(0));
      }
    }

    // pass all of the cached events to the handlers.
    for( SaxEvent event : eventList ) {
      event.flushEvent();
    }

    eventList.clear();
  }

  /**
   * The interface for cached sax events.
   */
  private interface SaxEvent
  {
    /**
     * Flushes this event through to the parent implementation.
     */
    public void flushEvent()
      throws SAXException;
  }

  private class ProcessingInstructionEvent
    implements SaxEvent
  {
    private String target;
    private String data;
    public ProcessingInstructionEvent( String target, String data )
    {
      this.target = target;
      this.data = data;
    }

    public void flushEvent()
      throws SAXException
    {
      XChainDeclFilter.super.processingInstruction( target, data );
    }
  }

  private class StartDocumentEvent
    implements SaxEvent
  {
    public void flushEvent()
      throws SAXException
    {
      XChainDeclFilter.super.startDocument();
    }
  }

  private class NotationDeclEvent
    implements SaxEvent
  {
    private String name;
    private String publicId;
    private String systemId;

    public NotationDeclEvent( String name, String publicId, String systemId )
    {
      this.name = name;
      this.publicId = publicId;
      this.systemId = systemId;
    }

    public void flushEvent()
      throws SAXException
    {
      XChainDeclFilter.super.notationDecl(name, publicId, systemId);
    }
  }

  private class UnparsedEntityDeclEvent
    implements SaxEvent
  {
    private String name;
    private String publicId;
    private String systemId;
    private String notationName;

    public UnparsedEntityDeclEvent( String name, String publicId, String systemId, String notationName )
    {
      this.name = name;
      this.publicId = publicId;
      this.systemId = systemId;
      this.notationName = notationName;
    }

    public void flushEvent()
      throws SAXException
    {
      XChainDeclFilter.super.unparsedEntityDecl(name, publicId, systemId, notationName);
    }
  }

  public static class LoggingUriResolver
    implements URIResolver
  {
    private URIResolver wrapped;

    public LoggingUriResolver( URIResolver wrapped )
    {
      this.wrapped = wrapped;
    }

    public Source resolve(String href, String base)
      throws TransformerException
    {
      try {
        return wrapped.resolve(href, base);
      }
      catch( TransformerException te ) {
        if( log.isDebugEnabled() ) {
          log.debug("Could not load document for href '"+href+"' and base '"+base+"'.", te);
        }
        throw te;
      }
      catch( RuntimeException re ) {
        if( log.isDebugEnabled() ) {
          log.debug("Could not load document for href '"+href+"' and base '"+base+"'.", re);
        }
        throw re;
      }
    }
  }
  
}
