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

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.sax.TemplatesHandler;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xchain.framework.lifecycle.XmlFactoryLifecycle;
import org.xchain.framework.factory.TemplatesFactory;
import org.xchain.framework.strategy.TemplatesConsumerStrategy;
import org.xchain.framework.strategy.SourceSourceStrategy;
import org.xchain.framework.util.ParserUtil;
import org.xchain.framework.util.ParseException;
import org.xchain.framework.util.ParsedTransformerFactory;
import org.xchain.framework.net.DependencyTracker;
import org.xchain.framework.net.UrlFactoryUriResolver;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.DTDHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.ext.LexicalHandler;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * This filter handles <?xchain-transformer-factory?> processing instructions.  These can be used to style the contents
 * of the xchain before it is loaded.
 * <?xchain-stylesheet system-id="" parameters="qname='value', qname='value', qname='value'"?>
 *
 * @author Christian Trimble
 * @author Josh Kennedy
 */
public class XChainTemplatesHandler
  extends HandlerWrapper
  implements SaxTemplatesHandler
{
  private static Logger log = LoggerFactory.getLogger(XChainTemplatesHandler.class);

  public static String XCHAIN_TRANSFORMER_FACTORY_TARGET = "xchain-transformer-factory";

  /** True if we are in the prolog, false otherwise. */
  private boolean inProlog = false;

  /** The list of sax events that we are caching until the prolog is finished. */
  private List<SaxEvent> eventList = new ArrayList<SaxEvent>();

  /** The locator object for this sax stream. */
  private Locator locator;

  /** The system id for the templates object created by this handler. */
  private String systemId;

  /** The name of the transformer factory that will load the templates object for this handler. */
  private QName transformerFactoryName = XmlFactoryLifecycle.DEFAULT_TRANSFORMER_FACTORY_NAME;

  /** The sax transformer handler that was used to load the templates object for the stream of sax events passed to this handler. */
  private SAXTransformerFactory transformerFactory;

  /** The templates handler that was created to handle this stream, based on the processing instructions found in the sax stream. */
  private TemplatesHandler templatesHandler;

  /**
   * Sets the system id for the templates classes created by this handler.
   *
   * @param systemId the system id of this templates object.
   */
  public void setSystemId( String systemId ) { this.systemId = systemId; }

  /**
   * Returns the system id for the templates class created by this handler.
   *
   * @return system id for the templates object created by this handler.
   */
  public String getSystemId() { return this.systemId; }

  /**
   * Returns a non-reloading SaxTemplates object for the stream of sax events that were passed to this handler.
   */
  public SaxTemplates getTemplates() {
    Templates templates = templatesHandler.getTemplates();

    // this is where we need a reloading reference.
    return new SaxTemplatesImpl(templates, transformerFactory); 
  }

  /**
   * Sets the locator object for this handler.
   */
  public void setDocumentLocator( Locator locator )
  {
    super.setDocumentLocator(locator);
    this.locator = locator;
  }

  /**
   * Return sthe locator object for this handler.
   */
  public Locator getDocumentLocator() { return this.locator; }

  /**
   * Prepares the filter to start tracking prolog events and caches the start document event.
   */
  public void startDocument()
    throws SAXException
  {
    // we are now in the prolog.
    inProlog = true;
    eventList.clear();

    // track the start document event.
    eventList.add(new StartDocumentEvent());
  }

  public void endDocument()
    throws SAXException
  {
    if( inProlog ) {
      throw new SAXException("The template document does not contain a root element.");
    }

    super.endDocument();
  }

  /**
   * Handles <?xchain-stylesheet?> processing instructions and caches all others.
   */
  public void processingInstruction(String target, String data)
    throws SAXException
  {
    if( XCHAIN_TRANSFORMER_FACTORY_TARGET.equals(target) && inProlog ) {
      if( templatesHandler != null ) {
        if( log.isWarnEnabled() ) {
          log.warn("More than one xchain-transformer-factory processing instructions were found in the prolog of '"+systemId+"'.");
        }
      }
      else {
        loadTemplatesHandler(data);
      }
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
      endProlog( uri, localName, qName, attributes );
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
      // TODO: map the prefix.
      eventList.add(new StartPrefixMappingEvent(prefix, uri));
    }
    else {
      super.startPrefixMapping( prefix, uri );
    }
  }

  private String resolveEntities( String data )
  {
    return data;
  }

  /**
   * Starts passing events to the filter.
   */
  private void endProlog( String uri, String localName, String qName, Attributes attributes )
    throws SAXException
  {
    inProlog = false;

    //if( transformerFactoryName == null ) {
      // then we need to use the uri, localName, qName, and attributes to select the default using the uri, localName, and attributes.
      //throw new SAXException("The transformer factory name is null.");
    //}

    // load the actual templates handler.
    if( transformerFactoryName != null ) {
    try {
      transformerFactory = XmlFactoryLifecycle.newTransformerFactory(transformerFactoryName);
    }
    catch( Exception e ) {
      throw new SAXException("Could not create transformer factory for name '"+transformerFactoryName+"'.", e);
    }
    }
    else {
      try {
        transformerFactory = XmlFactoryLifecycle.newTransformerFactory(uri, localName, attributes);
      }
      catch( Exception e ) {
        throw new SAXException("Could not load the default factory for the template starting with {"+uri+"}"+localName+".", e);
      }
    }

    try {
      templatesHandler = transformerFactory.newTemplatesHandler();
    }
    catch( Exception e ) {
      throw new SAXException("Could not load templates handler from transformer factory '"+transformerFactoryName+"'.", e);
    }

    // check all of the interfaces implemented by this templates factory.
    contentHandler = templatesHandler;
    contentHandler.setDocumentLocator(locator);

    if( templatesHandler instanceof LexicalHandler ) {
      lexicalHandler = (LexicalHandler)templatesHandler;
    }
    if( templatesHandler instanceof DTDHandler ) {
      dtdHandler = (DTDHandler)templatesHandler;
    }
      
    // pass all of the cached events to the handlers.
    for( SaxEvent event : eventList ) {
      event.flushEvent();
    }

    eventList.clear();
  }

  private void loadTemplatesHandler( String data )
    throws SAXException
  {
    try {
      // NOTE: this needs to be connected to the parsing utility method added to ParserUtil.
      ParsedTransformerFactory parsed = ParserUtil.parseTransformerFactory(data);

      String nameValue = parsed.getFields().get("name");
      if( nameValue != null ) {
        transformerFactoryName = QName.valueOf(nameValue);
      }
      else {
        transformerFactoryName = XmlFactoryLifecycle.DEFAULT_TRANSFORMER_FACTORY_NAME;
      }
    }
    catch( Exception e ) {
      throw new SAXException("Could not parse xchain-transformer-factory processing instrution.", e);
    }
  }

  /**
   * An implementation of the SaxTemplates interface that provides access to transformer handlers from a templates object.
   */
  private static class SaxTemplatesImpl
    implements SaxTemplates
  {
    /** The templates object that was loaded from the sax stream. */
    private Templates templates;

    /** The transformer factory that was specified in the sax stream. */
    private SAXTransformerFactory factory;

    /**
     * Creates a new SaxTemplatesImpl that is used to provide access to the creation of transformer handlers from
     * the templates object.
     */
    public SaxTemplatesImpl( Templates templates, SAXTransformerFactory factory )
    {
      this.templates = templates;
      this.factory = factory;
    }

    /**
     * Returns the result of newTransformer() from the contained templates object.
     */
    public Transformer newTransformer()
      throws TransformerConfigurationException
    {
      return templates.newTransformer();
    }

    /**
     * Returns the result of getOutputProperties() from the contained templates object.
     */
    public Properties getOutputProperties()
    {
      return templates.getOutputProperties();
    }

    /**
     * Returns the transformer handler created by the contained factory and templates objects.
     */
    public TransformerHandler newTransformerHandler()
      throws TransformerConfigurationException
    {
      synchronized(factory) {
        return factory.newTransformerHandler(templates);
      }
    }
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
      XChainTemplatesHandler.super.processingInstruction( target, data );
    }
  }

  private class StartDocumentEvent
    implements SaxEvent
  {
    public void flushEvent()
      throws SAXException
    {
      XChainTemplatesHandler.super.startDocument();
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
      XChainTemplatesHandler.super.notationDecl(name, publicId, systemId);
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
      XChainTemplatesHandler.super.unparsedEntityDecl(name, publicId, systemId, notationName);
    }
  }

  private class StartPrefixMappingEvent
    implements SaxEvent
  {
    private String prefix;
    private String uri;

    public StartPrefixMappingEvent( String prefix, String uri )
    {
      this.prefix = prefix;
      this.uri = uri;
    }

    public void flushEvent()
      throws SAXException
    {
      XChainTemplatesHandler.super.startPrefixMapping(prefix, uri);
    }
  }
}
