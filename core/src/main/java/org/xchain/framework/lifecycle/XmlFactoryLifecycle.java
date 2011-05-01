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
package org.xchain.framework.lifecycle;

import java.util.Map;
import java.util.HashMap;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xchain.framework.factory.TemplatesFactory;

import org.xchain.framework.net.UrlFactory;
import org.xchain.framework.net.UrlSourceUtil;

import org.xchain.framework.sax.XChainTemplatesHandler;
import org.xchain.framework.sax.SaxTemplatesHandler;
import org.xchain.framework.sax.SaxTemplates;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * This class provides a common place to manage factories for the TrAX and SAX APIs, by allowing the creation
 * of several named factories.
 *
 * @author Christian Trimble
 * @author John Trimble
 * @author Josh Kennedy
 */
public class XmlFactoryLifecycle
{
  private static Logger log = LoggerFactory.getLogger(XmlFactoryLifecycle.class);

  /** The qname for the default sax parser factory - {http://www.xchain.org/core}default-sax-parser-factory */
  public static final QName DEFAULT_SAX_PARSER_FACTORY_NAME = new QName("http://www.xchain.org/core", "default-sax-parser-factory");

  /** The qname for the default transformer factory - {http://www.xchain.org/core}default-transformer-factory */
  public static final QName DEFAULT_TRANSFORMER_FACTORY_NAME = new QName("http://www.xchain.org/core", "default-transformer-factory");

  /** The qname for the default document build factory - {http://www.xchain.org/core}default-document-builder-factory */
  public static final QName DEFAULT_DOCUMENT_BUILDER_FACTORY_NAME = new QName("http://www.xchain.org/core", "default-document-builder-factory");
  
  /** The qname for the default xml reader factory - {http://www.xchain.org/core}default-xml-reader-factory */
  public static final QName DEFAULT_XML_READER_FACTORY_NAME = new QName("http://www.xchain.org/core", "default-xml-reader-factory");

  /** The qname for xalan's transformer factory, if it is on the class path - {http://www.xchain.org/core}xalan */
  public static final QName XALAN_FACTORY_NAME = new QName("http://www.xchain.org/core", "xalan");

  /** The qname for xsltc's transformer factory, if it is on the class path - {http://www.xchain.org/core}xsltc */
  public static final QName XSLTC_FACTORY_NAME = new QName("http://www.xchain.org/core", "xsltc");

  /** The qname for saxon's transformer factory, if it is on the class path - {http://www.xchain.org/core}saxon */
  public static final QName SAXON_FACTORY_NAME = new QName("http://www.xchain.org/core", "saxon");

  /** The qname for joost's transformer factory, if it is on the class path - {http://www.xchain.org/core}joost */
  public static final QName JOOST_FACTORY_NAME = new QName("http://www.xchain.org/core", "joost");

  /** The class name of xalan's transformer factory - {@value} */
  public static final String XALAN_FACTORY_CLASS_NAME = "org.apache.xalan.processor.TransformerFactoryImpl";

  /** The class name of xsltc's transformer factory - {@value} */
  public static final String XSLTC_FACTORY_CLASS_NAME = "org.apache.xalan.xsltc.trax.TransformerFactoryImpl";

  /** The class name of saxon's transformer factory - {@value} */
  public static final String SAXON_FACTORY_CLASS_NAME = "net.sf.saxon.TransformerFactoryImpl";

  /** The class name of joost's transformer factory - {@value} */
  public static final String JOOST_FACTORY_CLASS_NAME = "net.sf.joost.trax.TransformerFactoryImpl";

  public static final String XSLT_NAMESPACE = "http://www.w3.org/1999/XSL/Transform";
  public static final String STX_NAMESPACE= "http://stx.sourceforge.net/2002/ns";

  private static boolean started = false;

  private static Map<QName, Factory<SAXParserFactory>> saxParserFactoryMap = new HashMap<QName, Factory<SAXParserFactory>>();
  private static Map<QName, Factory<DocumentBuilderFactory>> documentBuilderFactoryMap = new HashMap<QName, Factory<DocumentBuilderFactory>>();
  private static Map<QName, Factory<SAXTransformerFactory>> transformerFactoryMap = new HashMap<QName, Factory<SAXTransformerFactory>>();
  /** We should provide a factory for validators. */

  public static boolean isStarted()
  {
    return started;
  }

  public static void startLifecycle( LifecycleContext context )
    throws LifecycleException
  {
    synchronized( XmlFactoryLifecycle.class ) {
      started = true;

      // log all of the factories that have been added to this lifecycle.
      if( log.isInfoEnabled() ) {
        StringBuilder status = new StringBuilder();
        status.append("XML Lifecycle State\n");
        if( saxParserFactoryMap.isEmpty() ) {
          status.append("  There are no Factory<SAXParserFactory> objects defined.\n");
        }
        else {
          status.append("  SAXParserFactories:\n");
          for( Map.Entry<QName, Factory<SAXParserFactory>> entry : saxParserFactoryMap.entrySet() ) {
            status.append("    ").append(entry.getKey()).append(" => ").append(entry.getValue().getClass().getName()).append("\n");
          }
        }
        if( transformerFactoryMap.isEmpty() ) {
          status.append("  There are no Factory<TransfromerFactory> objects defined.\n");
        }
        else {
          status.append("  SAXTransformerFactories:\n");
          for( Map.Entry<QName, Factory<SAXTransformerFactory>> entry : transformerFactoryMap.entrySet() ) {
            status.append("    ").append(entry.getKey()).append(" => ").append(entry.getValue().getClass().getName()).append("\n");
          }
        }
        log.info(status.toString());
      }
    }
  }

  public static void stopLifecycle( LifecycleContext context )
  {
    started = false;
  }

  /**
   * Puts a new SAXParserFactory factory into the XmlFactoryLifecycle with the specified name.  If a factory is already bound to the
   * specified name, it is removed.
   */
  public static void putSaxParserFactoryFactory( QName name, Factory<SAXParserFactory> factory )
  {
    synchronized( XmlFactoryLifecycle.class ) {
      if( started ) {
        throw new IllegalStateException("You may not add factories to the sax parser factory while the xml lifecycle is on.");
      }
      synchronized( saxParserFactoryMap ) {
        saxParserFactoryMap.put( name, factory );
      }
    }
  }

  /**
   * Returns the SAXParserFactory factory for the specified name.
   */
  public static Factory<SAXParserFactory> getSaxParserFactoryFactory( QName name )
  {
    synchronized( saxParserFactoryMap ) {
      return saxParserFactoryMap.get( name );
    }
  }

  /**
   * Removes the SAXParserFactory factory for the specified name.
   */
  public static Factory<SAXParserFactory> removeSaxParserFactoryFactory( QName name )
  {
    synchronized( XmlFactoryLifecycle.class ) {
      if( started ) {
        throw new IllegalStateException("You may not add factories to the sax parser factory while the xml lifecycle is on.");
      }
      synchronized( saxParserFactoryMap ) {
        return saxParserFactoryMap.remove( name );
      }
    }
  }

  /**
   * Returns a new SAXParserFactory from the SAXParserFactory factory bound to the specified name.
   */
  public static SAXParserFactory newSaxParserFactory( QName name )
  {
    return getSaxParserFactoryFactory( name ).newInstance();
  }

  public static SAXParser newSaxParser()
    throws ParserConfigurationException, SAXException
  {
    return newSaxParser(DEFAULT_SAX_PARSER_FACTORY_NAME);
  }

  /**
   * Returns a new SAXParser for the specified SAXParserFactory factory name.
   */
  public static SAXParser newSaxParser( QName name )
    throws ParserConfigurationException, SAXException
  {
    return newSaxParserFactory( name ).newSAXParser();
  }

  public static XMLReader newXmlReader()
    throws ParserConfigurationException, SAXException
  {
    return newXmlReader(DEFAULT_SAX_PARSER_FACTORY_NAME);
  }

  /**
   * Returns a new XMLReader for the specified SAXParserFactory factory name.
   */
  public static XMLReader newXmlReader( QName name )
    throws ParserConfigurationException, SAXException
  {
    return newSaxParser( name ).getXMLReader();
  }

  public static void putTransformerFactoryFactory( QName name, Factory<SAXTransformerFactory> factory )
  {
    synchronized( XmlFactoryLifecycle.class ) {
      if( started ) {
        throw new IllegalStateException("You may not add factories to the sax parser factory while the xml lifecycle is on.");
      }
      synchronized( transformerFactoryMap ) {
        transformerFactoryMap.put( name, factory );
      }
    }
  }

  /**
   * Returns the Factory<SAXTransformerFactory> that is mapped to the specified name, or null if there is no such
   * factory.
   * @param name the name of the factory to return.
   * @return the Factory<SAXTransformerFactory> that is mapped to the specified name, or null if there is no such factory.
   */
  public static Factory<SAXTransformerFactory> getTransformerFactoryFactory( QName name )
  {
    synchronized( transformerFactoryMap ) {
      return transformerFactoryMap.get( name );
    }
  }

  /**
   * Removes the Factory<SAXTransformerFactory> with the specifed name.  The factory that was removed is returned.
   * @param name the name of the Factory<SAXTransformerFactory> that will be removed.
   * @return the factory that was removed, or null if there was not a factory with that specified name.
   * @throws IllegalStateException if this lifecycle is running when this method is called.
   */
  public static Factory<SAXTransformerFactory> removeTransformerFactoryFactory( QName name )
  {
    synchronized( XmlFactoryLifecycle.class ) {
      if( started ) {
        throw new IllegalStateException("You may not add factories to the sax parser factory while the xml lifecycle is on.");
      }
      synchronized( transformerFactoryMap ) {
        return transformerFactoryMap.remove( name );
      }
    }
  }

  /**
   * Creates a new SAXTransformerFactory based on the Factory registered with the specified name.
   * @param name the name of the Factory<SAXTransformerFactory> to use.
   * @return the new SAXTransformerFactory that was created.
   * @throws RuntimeException if there is not a factory with the specified name.
   */
  public static SAXTransformerFactory newTransformerFactory( QName name )
  {
    Factory<SAXTransformerFactory> factory = getTransformerFactoryFactory( name );

    if( factory == null ) {
      throw new RuntimeException("There is no Factory<SAXTransformerFactory> named '"+name+".");
    }

    return factory.newInstance();
  }

  public static SAXTransformerFactory newTransformerFactory( String uri, String localName, Attributes attributes )
  {
    // TODO: replace this with some kind of configurable strategy for looking up the local name.
    // if the uri is equal to the xslt uri, then use the default xslt factory.
    if( XSLT_NAMESPACE.equals(uri) ) {
      return newTransformerFactory(SAXON_FACTORY_NAME);
    }
    else if( STX_NAMESPACE.equals(uri) ) {
      return newTransformerFactory(JOOST_FACTORY_NAME);
    }
    else {
      throw new RuntimeException("There is no Factory<SAXTransformerFactory> for '"+uri+"', '"+localName+"'.");
    }
  }

  /**
   * Returns a new Templates object for the specified systemId.
   */
  public static SaxTemplates newTemplates( String systemId )
    throws TransformerConfigurationException
  {
    try {
      return TemplatesFactory.getInstance().getTemplates(systemId);
    }
    catch( Exception e ) {
      throw new TransformerConfigurationException("An exception was thrown while loading the templates object for system id '"+systemId+"'.", e);
    }
  }

  public static TransformerHandler newTransformerHandler( Templates templates )
    throws TransformerConfigurationException
  {
    if( !( templates instanceof SaxTemplates ) ) {
      throw new TransformerConfigurationException("Wrong type of templates object.");
    }
    return ((SaxTemplates)templates).newTransformerHandler();
  }

  public static TransformerHandler newTransformerHandler( String systemId )
    throws TransformerConfigurationException
  {
    return newTransformerHandler(newTemplates( systemId ));
  }

  /**
   * Creates a templates handler that does not cache the templates object that is returned.  The templates objects from this
   * method cannot be cached, as there is no reliable way to create the sax stream that is associated with the use of this templates
   * handler.  If you want to create a cacheable and reloadable templates object, please ask for the templates object by system id from
   * the newTemplates( String ) method.  This will create a sax stream for the templates object, cache the object, and watch for changes to
   * the 
   */
  public static SaxTemplatesHandler newTemplatesHandler()
  {
    return new XChainTemplatesHandler();
  }
  
  /**
   * Puts a new DocumentBuilderFactory factory into the XmlFactoryLifecycle with the specified name.  If a factory is already bound to the 
   * specified name, it is removed.
   */
  public static void putDocumentBuilderFactoryFactory( QName name, Factory<DocumentBuilderFactory> factory)
  {
    synchronized( XmlFactoryLifecycle.class ) {
      if( started ) {
        throw new IllegalStateException("You may not add factories to the document builder factory while the xml lifecycle is on.");
      }
      synchronized( documentBuilderFactoryMap ) {
        documentBuilderFactoryMap.put( name, factory );
      }
    }
  }
  
  /**
   * Returns the DocumentBuilderFactory factory for the specified name.
   */
  public static Factory<DocumentBuilderFactory> getDocumentBuilderFactory( QName name )
  {
    synchronized( documentBuilderFactoryMap ) {
      return documentBuilderFactoryMap.get( name );
    }
  }
  
  /**
   * Removes the DocumentBuilderFactory factory for the specified name.
   */
  public static Factory<DocumentBuilderFactory> removeDocumentBuilderFactoryFactory( QName name )
  {
    synchronized( XmlFactoryLifecycle.class ) {
      if( started ) {
        throw new IllegalStateException("You may not add factories to the document builder factory while the xml lifecycle is on.");
      }
      synchronized( documentBuilderFactoryMap ) {
        return documentBuilderFactoryMap.remove( name );
      }
    }
  }

  /**
   * Returns a new DocumentBuilderFactory from the DocumentBuilderFactory factory bound to the specified name.
   */
  public static DocumentBuilderFactory newDocumentBuilderFactory( QName name )
  {
    return getDocumentBuilderFactory( name ).newInstance();
  }
  
  public static DocumentBuilder newDocumentBuilder() 
    throws ParserConfigurationException
  {
    return newDocumentBuilder( DEFAULT_DOCUMENT_BUILDER_FACTORY_NAME );
  }
  
  /**
   * Returns a new DocumentBuilder for the specified DocumentBuilderFactory factory name.
   * @throws ParserConfigurationException 
   */
  public static DocumentBuilder newDocumentBuilder( QName name ) 
    throws ParserConfigurationException
  {
    return newDocumentBuilderFactory( name ).newDocumentBuilder();
  }
}
