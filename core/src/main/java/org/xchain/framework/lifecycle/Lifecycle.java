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

import static org.xchain.framework.lifecycle.XmlFactoryLifecycle.DEFAULT_SAX_PARSER_FACTORY_NAME;
import static org.xchain.framework.lifecycle.XmlFactoryLifecycle.DEFAULT_TRANSFORMER_FACTORY_NAME;
import static org.xchain.framework.lifecycle.XmlFactoryLifecycle.DEFAULT_DOCUMENT_BUILDER_FACTORY_NAME;
import static org.xchain.framework.lifecycle.XmlFactoryLifecycle.SAXON_FACTORY_CLASS_NAME;
import static org.xchain.framework.lifecycle.XmlFactoryLifecycle.SAXON_FACTORY_NAME;
import static org.xchain.framework.lifecycle.XmlFactoryLifecycle.XALAN_FACTORY_CLASS_NAME;
import static org.xchain.framework.lifecycle.XmlFactoryLifecycle.XALAN_FACTORY_NAME;
import static org.xchain.framework.lifecycle.XmlFactoryLifecycle.XSLTC_FACTORY_CLASS_NAME;
import static org.xchain.framework.lifecycle.XmlFactoryLifecycle.XSLTC_FACTORY_NAME;
import static org.xchain.framework.lifecycle.XmlFactoryLifecycle.JOOST_FACTORY_CLASS_NAME;
import static org.xchain.framework.lifecycle.XmlFactoryLifecycle.JOOST_FACTORY_NAME;
import static org.xchain.framework.lifecycle.XmlFactoryLifecycle.getSaxParserFactoryFactory;
import static org.xchain.framework.lifecycle.XmlFactoryLifecycle.getTransformerFactoryFactory;
import static org.xchain.framework.lifecycle.XmlFactoryLifecycle.getDocumentBuilderFactory;
import static org.xchain.framework.lifecycle.XmlFactoryLifecycle.putSaxParserFactoryFactory;
import static org.xchain.framework.lifecycle.XmlFactoryLifecycle.putTransformerFactoryFactory;
import static org.xchain.framework.lifecycle.XmlFactoryLifecycle.putDocumentBuilderFactoryFactory;
import static org.xchain.framework.lifecycle.XmlFactoryLifecycle.removeSaxParserFactoryFactory;
import static org.xchain.framework.lifecycle.XmlFactoryLifecycle.removeTransformerFactoryFactory;
import static org.xchain.framework.lifecycle.XmlFactoryLifecycle.removeDocumentBuilderFactoryFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xchain.framework.scanner.ScanException;
import org.xchain.framework.scanner.ScannerLifecycle;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xchain.framework.jxpath.Scope;
import org.xchain.framework.net.UrlSourceUtil;
import org.xchain.framework.net.protocol.resource.ContextClassLoaderUrlTranslationStrategy;
import org.xchain.framework.net.protocol.resource.ResourceUrlConnection;
import org.xchain.framework.net.strategy.BaseUrlUrlTranslationStrategy;
import org.xchain.framework.net.strategy.CompositeUrlTranslationStrategy;
import org.xchain.framework.util.QNameConverter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The main class of the xchain framework.  When the lifecycle is started the LifecycleStepScanner will scan for LifecycleSteps
 * in the current context class loader.  All LifecycleSteps will then be started with the Lifecycle.  When the Lifecycle stops
 * all the LifecycleSteps started when the Lifecycle started will be stopped in a LIFO manner.
 * 
 * @author Christian Trimble
 * @author Devon Tackett
 * @author John Trimble
 * @author Josh Kennedy
 *
 * @see LifecycleStep
 * @see LifecycleStepScanner
 */
@LifecycleClass(uri="http://www.xchain.org/framework/lifecycle")
public final class Lifecycle
{
  public static Logger log = LoggerFactory.getLogger(Lifecycle.class);
  public static String XCHAIN_CONFIG = "META-INF/xchain-config.xml";
  
  private static ConfigDocumentContext configDocumentContext = null;
  private static LifecycleContext context = null;
  private static List<LifecycleStep> lifecycleStepList = null;
  
  private static boolean saxParserFactoryCreated = false;
  private static boolean transformerFactoryCreated = false;
  private static boolean documentBuilderFactoryCreated = false;
  private static boolean xalanFactoryCreated = false;
  private static boolean xsltcFactoryCreated = false;
  private static boolean saxonFactoryCreated = false;
  private static boolean joostFactoryCreated = false;

  private static Converter oldQNameConverter = null;

  /**
   * Start the lifecycle.  Loading all lifecycle steps found by the LifecycleStepScanner.
   *
   * @see LifecycleStepScanner
   */
  public static void startLifecycle()
    throws LifecycleException
  {
    try {
      ThreadLifecycle.getInstance().getCCLPolicy().bindCCL();
      
    synchronized( Lifecycle.class ) {
      // if xchains is currently running, then throw a lifecycle exception.
      if( isRunning() ) {
        throw new LifecycleException("Start Lifecycle called while xchains is running.");
      }

      try {
        // create a new Lifecycle context.
        context = new LifecycleContext();
        context.setClassLoader(new LifecycleClassLoader(Thread.currentThread().getContextClassLoader()));

        // scan for the lifecycle listeners.
        startLifecycleSteps();
        // Clear the scanner cache now that all the lifecycles are done using it.
        ScannerLifecycle.getInstance().clearCache();
      }
      catch( LifecycleException le ) {
        context = null;
        throw le;
      }
      catch( Throwable t ) {
        context = null;
        throw new LifecycleException("Could not start the lifecycle due to a throwable.", t);
      }
    }

    }
    finally {
      ThreadLifecycle.getInstance().getCCLPolicy().unbindCCL();
    }
  }

  /**
   * Shutdown the lifecycle.
   */
  public static void stopLifecycle()
    throws LifecycleException
  {
    synchronized( Lifecycle.class ) {

      // stop the lifecycle steps.
      stopLifecycleSteps();

      // remove the context to null.
      context = null;
    }
  }

  /**
   * Restart the lifecycle.  This effectively shuts down all current lifecycle steps and restarts the lifecycle.
   */
  public static void restartLifecycle()
    throws LifecycleException
  {
    synchronized( Lifecycle.class ) {
      stopLifecycle();
      startLifecycle();
    }
  }

  /**
   * Returns true if the life cycle
   */
  public static boolean isRunning()
  {
    synchronized( Lifecycle.class ) {
      return context != null;
    }
  }

  /**
   * Returns the current LifecycleContext.  Null if the Lifecycle is not running.
   */
  public static LifecycleContext getLifecycleContext()
  {
    return context;
  }

  /**
   * Start all lifecycle steps found by the LifecycleStepScanner.
   * 
   * @throws LifecycleException If an exception was encountered attempting to start the lifecycle steps.
   * @see LifecycleStepScanner
   */
  private static void startLifecycleSteps()
    throws LifecycleException
  {
    // Create a scanner to find the lifecycle steps in the current classpath.
    LifecycleStepScanner scanner = new LifecycleStepScanner(context);
    try {
      scanner.scan();
      lifecycleStepList = scanner.getLifecycleStepList();
    }
    catch( ScanException se ) {
      throw new LifecycleException("An exception was thrown while scanning for lifecycle steps.", se);
    }

    if( log.isInfoEnabled() ) {
      StringBuilder message = new StringBuilder();
      message.append("Found ").append(lifecycleStepList.size()).append(" lifecycle steps:\n");
      for( LifecycleStep lifecycleStep : lifecycleStepList ) {
        message.append("  ").append(lifecycleStep.getQName()).append("\n");
      }
      log.info(message.toString());
    }

    // Start all the found lifecycle steps.
    ListIterator<LifecycleStep> iterator = lifecycleStepList.listIterator();
    try {
      while( iterator.hasNext() ) {
        LifecycleStep lifecycleStep = iterator.next();

        if( log.isInfoEnabled() ) {
          log.info("Starting Lifecycle Step '"+lifecycleStep.getQName()+"'.");
        }
        lifecycleStep.startLifecycle(context, Lifecycle.configDocumentContext);
        if( log.isInfoEnabled() ) {
          log.info("Finished Lifecycle Step '"+lifecycleStep.getQName()+"'.");
        }
      }
    }
    catch( LifecycleException le ) {
      if( log.isErrorEnabled() ) {
        log.error("Stopping the lifecycle startup due to a lifecycle exception.", le);
      }
      iterator.previous();
      while( iterator.hasPrevious() ) {
        LifecycleStep lifecycleStep = iterator.previous();
        try {
          lifecycleStep.stopLifecycle(context);
        }
        catch( Throwable t ) {
          if( log.isWarnEnabled() ) {
            log.warn("An exception was thrown while stopping a lifecycle exception.", t);
          }
        }
      }

      // clear the lifecycle step list.
      lifecycleStepList.clear();

      // we should throw the lifecycle exception here.
      throw le;
    } 
    finally {
      // make sure the configuration DOM gets garbage collected, no reason to keep it around once everyone is configured.
      Lifecycle.configDocumentContext = null;
    }
    
  }

  /**
   * Stop all current lifecycle steps.  The first step to be stopped is the last one started.
   */
  private static void stopLifecycleSteps()
  {
    ListIterator<LifecycleStep> iterator = lifecycleStepList.listIterator(lifecycleStepList.size());
    while( iterator.hasPrevious() ) {
      LifecycleStep step = iterator.previous();
      try {
        if( log.isInfoEnabled() ) {
          log.info("Stopping Lifecycle Step '"+step.getQName()+"'.");
        }
        step.stopLifecycle(context);
        if( log.isInfoEnabled() ) {
          log.info("Finished Lifecycle Step '"+step.getQName()+"'.");
        }
      }
      catch( Throwable t ) {
        if( log.isWarnEnabled() ) {
          log.warn("An exception was thrown while stopping a lifecycle exception.", t);
        }
      }
    }
    lifecycleStepList.clear();
  }
  
  /**
   * This step creates a ConfigDocumentContext instance which will be passed to all lifecycle start steps, which take a
   * ConfigDocumentContext, when Lifecycle.startLifecycle() is called. Consequently, all lifecycle steps that take a
   * ConfigDocumentContext have an implicit dependency upon this step such that they always run after it. 
   * 
   * @throws LifecycleException
   */
  @StartStep(localName="create-config-document-context", after={"xml-factory-lifecycle"})
  public static void startCreateConfigDocumentContext() 
    throws LifecycleException 
  {
    Lifecycle.configDocumentContext = createConfigurationContext(XCHAIN_CONFIG);
  }

  /**
   * Lifecycle Step to load the xchain configuration.
   */
  @StartStep(localName="config", xmlns={"xmlns:config='http://xchain.org/config/1.0'"})
  public static void startConfiguration(LifecycleContext context, ConfigDocumentContext configDocContext) 
    throws MalformedURLException 
  {
    // Read DOM and set values on configContext
    ConfigContext configContext = Lifecycle.getLifecycleContext().getConfigContext();
    Boolean monitor = (Boolean)configDocContext.getValue("/config:config/config:monitor", Boolean.class);
    if( monitor != null ) configContext.setMonitored(monitor);
    Integer catalogCacheSize = (Integer)configDocContext.getValue("/config:config/config:catalog-cache-size", Integer.class);
    if( catalogCacheSize != null ) configContext.setCatalogCacheSize(catalogCacheSize);
    Integer templateCacheSize = (Integer)configDocContext.getValue("/config:config/config:templates-cache-size", Integer.class);
    if( templateCacheSize != null ) configContext.setTemplatesCacheSize(templateCacheSize);
    
    addUrls(configDocContext, "/config:config/config:resource-base-url/@config:system-id", configContext.getResourceUrlList());
    addUrls(configDocContext, "/config:config/config:source-base-url/@config:system-id", configContext.getSourceUrlList());
    addUrls(configDocContext, "/config:config/config:webapp-base-url/@config:system-id", configContext.getWebappUrlList());
    
    // configure the URLFactory for file monitoring if requested
    if( configContext.isMonitored() ) {

      if( log.isDebugEnabled() ) {
        log.debug( "Config: Monitoring is enabled, configuring URL translation strategies..." );
      }

      // configure the resource protocol 'context-class-loader' authority for monitoring
      if( !configContext.getResourceUrlList().isEmpty() ) {

        CompositeUrlTranslationStrategy contextClassLoaderStrategy = new CompositeUrlTranslationStrategy();
  
        for( Iterator<URL> it = configContext.getResourceUrlList().iterator(); it.hasNext(); ) {
          URL baseUrl = it.next();
          BaseUrlUrlTranslationStrategy baseUrlStrategy =
            new BaseUrlUrlTranslationStrategy( baseUrl, BaseUrlUrlTranslationStrategy.URL_FACTORY_URL_SOURCE );
          contextClassLoaderStrategy.getTranslatorList().add( baseUrlStrategy );
          if( log.isDebugEnabled() ) {
            log.debug( "    Adding resource URL: " + baseUrl );
          }
        }
  
        // now add the standard context class loader strategy
        contextClassLoaderStrategy.getTranslatorList().add( new ContextClassLoaderUrlTranslationStrategy() );
  
        // override the standard strategy with the new composite strategy
        ResourceUrlConnection.registerUrlTranslationStrategy( ResourceUrlConnection.CONTEXT_CLASS_LOADER_ATHORITY,
                                                              contextClassLoaderStrategy );
      }
    }
  }
  
  /**
   * The lifecycle step that engineers command classes.  This step creates a ClassScanner for the context's class loader and
   * calls its scan method.
   *
   * @see org.xchain.framework.lifecycle.ClassScanner
   */
  @StartStep(localName="command-engineering", after={"config"})
  public static void startCommandEngineering(LifecycleContext context) 
  {
    // for each class that is a Catalog or Command, create an entry for those in the context.
    ClassScanner classScanner = new ClassScanner(context);
    classScanner.scan();
  }
  
  /**
   * Calls XmlFactoryLifecycle.startLifecycle( ... ).
   * 
   * @param context
   * @throws LifecycleException
   */
  @StartStep(localName="xml-factory-lifecycle", before={"config"})
  public static void startXmlFactory(LifecycleContext context) 
    throws LifecycleException 
  {
    XmlFactoryLifecycle.startLifecycle( context );
  }
  
  /**
   * Calls XmlFactoryLifecycle.stopLifecycle( ... ).
   * 
   * @param context
   */
  @StopStep(localName="xml-factory-lifecycle")
  public static void stopXmlFactory(LifecycleContext context) 
  {
    XmlFactoryLifecycle.stopLifecycle( context );
  }
  
  /**
   * Sets the default SAX, XSLT, and DOM implementations to use on the XmlFactoryLifecycle for those that are not 
   * already set. To override any of these defaults, create a chain that runs before this one and sets the factories as
   * desired on the XmlFactoryLifecycle.
   *  
   * @param context
   */
  @StartStep(localName="default-xml-factory", before={"xml-factory-lifecycle"})
  public static void startDefaultXmlFactory(LifecycleContext context)
  {
    // add the default xml parser factory step.
    if( getSaxParserFactoryFactory(DEFAULT_SAX_PARSER_FACTORY_NAME) == null ) {
      saxParserFactoryCreated = true;
      putSaxParserFactoryFactory(DEFAULT_SAX_PARSER_FACTORY_NAME, new DefaultSaxParserFactoryFactory());
    }

    // add the default xml transformer factory.
    if( getTransformerFactoryFactory(DEFAULT_TRANSFORMER_FACTORY_NAME) == null ) {
      transformerFactoryCreated = true;
      putTransformerFactoryFactory(DEFAULT_TRANSFORMER_FACTORY_NAME, new DefaultTransformerFactoryFactory());
    }

    // add the default document builder factory
    if( getDocumentBuilderFactory(DEFAULT_DOCUMENT_BUILDER_FACTORY_NAME) == null ) {
      documentBuilderFactoryCreated = true;
      putDocumentBuilderFactoryFactory(DEFAULT_DOCUMENT_BUILDER_FACTORY_NAME, new DefaultDocumentBuilderFactoryFactory());
    }

    // add the default xml transformer factory.
    if( getTransformerFactoryFactory(XALAN_FACTORY_NAME) == null && factoryClassExists(XALAN_FACTORY_NAME, XALAN_FACTORY_CLASS_NAME) ) {
      xalanFactoryCreated = true;
      putTransformerFactoryFactory(XALAN_FACTORY_NAME, new BasicTransformerFactoryFactory(XALAN_FACTORY_CLASS_NAME));
    }

    // add the default xml transformer factory.
    if( getTransformerFactoryFactory(XSLTC_FACTORY_NAME) == null && factoryClassExists(XSLTC_FACTORY_NAME, XSLTC_FACTORY_CLASS_NAME) ) {
      xsltcFactoryCreated = true;
      putTransformerFactoryFactory(XSLTC_FACTORY_NAME, new BasicTransformerFactoryFactory(XSLTC_FACTORY_CLASS_NAME));
    }

    // add the default xml transformer factory.
    if( getTransformerFactoryFactory(SAXON_FACTORY_NAME) == null && factoryClassExists(SAXON_FACTORY_NAME, SAXON_FACTORY_CLASS_NAME) ) {
      saxonFactoryCreated = true;
      putTransformerFactoryFactory(SAXON_FACTORY_NAME, new BasicTransformerFactoryFactory(SAXON_FACTORY_CLASS_NAME));
    }

    if( getTransformerFactoryFactory(JOOST_FACTORY_NAME) == null && factoryClassExists(JOOST_FACTORY_NAME, JOOST_FACTORY_CLASS_NAME) ) {
      joostFactoryCreated = true;
      putTransformerFactoryFactory(JOOST_FACTORY_NAME, new BasicTransformerFactoryFactory(JOOST_FACTORY_CLASS_NAME));
    }
  }

  /**
   * Unsets any defaults set by the default-xml-factory start step.
   * @param context
   */
  @StopStep(localName="default-xml-factory")
  public static void stopDefaultXmlFactory(LifecycleContext context)
  {
    // remove the joost transformer factory if it was created by this step.
    if( joostFactoryCreated ) {
      joostFactoryCreated = false;
      removeTransformerFactoryFactory(JOOST_FACTORY_NAME);
    }

    // remove the default xml transformer factory if it was created by this step.
    if( saxonFactoryCreated ) {
      saxonFactoryCreated = false;
      removeTransformerFactoryFactory(SAXON_FACTORY_NAME);
    }

    // remove the default xml transformer factory if it was created by this step.
    if( xsltcFactoryCreated ) {
      xsltcFactoryCreated = false;
      removeTransformerFactoryFactory(XSLTC_FACTORY_NAME);
    }

    // remove the default xml transformer factory if it was created by this step.
    if( xalanFactoryCreated ) {
      xalanFactoryCreated = false;
      removeTransformerFactoryFactory(XALAN_FACTORY_NAME);
    }

    // remove the default document builder factory if it was created by this step.
    if( documentBuilderFactoryCreated ) {
      documentBuilderFactoryCreated = false;
      removeDocumentBuilderFactoryFactory(DEFAULT_DOCUMENT_BUILDER_FACTORY_NAME);
    }
    
    // remove the default xml transformer factory if it was created by this step.
    if( transformerFactoryCreated ) {
      transformerFactoryCreated = false;
      removeTransformerFactoryFactory(DEFAULT_TRANSFORMER_FACTORY_NAME);
    }

    // remove the default xml parser factory if it was created by this step.
    if( saxParserFactoryCreated ) {
      saxParserFactoryCreated = false;
      removeSaxParserFactoryFactory(DEFAULT_SAX_PARSER_FACTORY_NAME);
    }
  }

  /**
   * Sets up the default conversion objects in the bean utils.
   */
  @StartStep(localName="default-conversions")
  public static void startDefaultConversions(LifecycleContext lifecycleContext)
  {
    oldQNameConverter = ConvertUtils.lookup(QName.class);
    ConvertUtils.register(new QNameConverter(), QName.class);
  }

  /**
   * Removes the standard conversion objects in the bean utils.
   */
  @StopStep(localName="default-conversions")
  public static void stopDefaultConversions(LifecycleContext lifecycleContext)
  {
    if( oldQNameConverter != null ) {
      ConvertUtils.register(oldQNameConverter, QName.class);
    }
    else {
      ConvertUtils.deregister(QName.class);
    }
    oldQNameConverter = null;
  }
  
  
  /**
   * Creates a new ConfigContext of the DOM produced by parsing the indicated resource.
   * 
   * @throws IOException 
   * @throws SAXException 
   * @throws ParserConfigurationException 
   */
  private static ConfigDocumentContext createConfigurationContext(String resource) 
    throws LifecycleException 
  {
    try {
      Object contextBean = null;
      URL configUrl = Thread.currentThread().getContextClassLoader().getResource(resource);
      if( configUrl == null ) {
        // we can't find the config so we just an instance of Object for the ConfigDocumentContext.
        if( log.isDebugEnabled() )
          log.debug("Cannot find configuration at resource '"+resource+"'.");
        contextBean = new Object();
      } else {
        // we have a config, create a DOM out of it and use it for the ConfigDocumentContext.
        if( log.isDebugEnabled() ) {
          log.debug("Loading xchain config file for url: "+configUrl.toExternalForm());
        }
        
        // get the document builder.
        DocumentBuilder documentBuilder = XmlFactoryLifecycle.newDocumentBuilder();
        
        InputSource configInputSource = UrlSourceUtil.createSaxInputSource(configUrl);
        Document document = documentBuilder.parse(configInputSource);
        contextBean = document;
      }
      // create the context
      ConfigDocumentContext configDocumentContext = new ConfigDocumentContext(null, contextBean, Scope.chain);
      configDocumentContext.setConfigUrl(configUrl);
      configDocumentContext.setLenient(true);
      return configDocumentContext;
    } catch( Exception e ) {
      throw new LifecycleException("Error loading configuration from resource '"+resource+"'.", e);
    }
  }
  
  private static void addUrls(ConfigDocumentContext configDocContext, String urlxpath, List<URL> urlList) 
    throws MalformedURLException 
  {
    Iterator<?> urlStringIterator = configDocContext.iterate(urlxpath);
    while( urlStringIterator.hasNext() ) {
      String urlString = urlStringIterator.next().toString();
      if( !"".equals(urlString) ) {
        urlList.add(new URL(urlString));
      }
    }
  }
  
  private static boolean factoryClassExists( QName factoryName, String className )
  {
    boolean result = false;
    try {
      Thread.currentThread().getContextClassLoader().loadClass(className);
      result = true;
    }
    catch( ClassNotFoundException cnfe ) {
      if( log.isInfoEnabled() ) {
        log.info("Not creating default transformer factory for '"+factoryName+"' because its driver class '"+className+"' is not in the context class loader.");
      }
    }
    return result;
  }
}
