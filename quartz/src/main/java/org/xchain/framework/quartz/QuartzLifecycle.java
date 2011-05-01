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
package org.xchain.framework.quartz;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import javax.xml.namespace.QName;

import org.xchain.framework.lifecycle.LifecycleAccessor;
import org.xchain.framework.lifecycle.LifecycleContext;
import org.xchain.framework.lifecycle.LifecycleClass;
import org.xchain.framework.lifecycle.LifecycleException;
import org.xchain.framework.lifecycle.StartStep;
import org.xchain.framework.lifecycle.StopStep;
import org.xchain.framework.lifecycle.ConfigDocumentContext;
import org.xchain.annotations.Function;

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.xml.JobSchedulingDataProcessor;
import org.quartz.simpl.ThreadContextClassLoadHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;

/**
 * <p>A lifecycle class that starts quartz, if the quartz configuration files are available on the
 * class path.</p>
 *
 * @author Christian Trimble
 * @author Josh Kennedy
 * @author Devon Tackett
 * @author John Trimble
 */
@LifecycleClass(uri=Constants.LIFECYCLE_URI)
public class QuartzLifecycle {
  public static final Logger log = LoggerFactory.getLogger( QuartzLifecycle.class );

  private static QuartzLifecycle instance = new QuartzLifecycle();
  public  static final String DEFAULT_QUARTZ_PROPERTIES = "quartz.properties";
  public  static final String DEFAULT_QUARTZ_JOB_CONFIG = "quartz-jobs.xml";

  /**
   * <p>Returns the singleton instance of the quartz lifecycle.</p>
   */
  @LifecycleAccessor
  public static QuartzLifecycle getInstance() { return instance; }

  private boolean disabled = false;
  private Scheduler scheduler = null;
  private Integer startDelay = null;

  private boolean autoStart;

  /**
   * <p>Returns the current default scheduler.</p>
   */
  @Function(localName="scheduler")
  public Scheduler getScheduler()
  {
    return this.scheduler;
  }

  /**
   * <p>Prevents multiple instances of this lifecycle from being created.</p>
   */
  private QuartzLifecycle() { }

  /**
   * <p>Configures the quartz lifecycle.  This method will take several different approches to configuring the lifecycle:</p>
   * <ol>
   *   <li>If the quartz apis cannot be found, then the QuartzLifecycle is set to disabled and this method returns.</li>
   *   <li>If the XChain config resource (META-INF/xchain-config.xml) contains the {http://xchain.org/quartz-config}disabled element and it has a value of "true",
   *     then the disabled flag is set to true and this method returns.</li>
   *   <li>If the XChain config resource does not contain any {http://xchain.org/quartz-config}scheduler elements, then the default scheduler is created.</li>
   *   <li>If the XChain config resource does contain one or more {http://xchain.org/quartz-config}scheduler elements, then schedulers are loaded for each element.</li>
   * </ol>
   *
   * @param context the lifecycle context.
   * @param configDocContext the context of the configuration document.
   */
  @StartStep(localName="config", after={"{http://www.xchain.org/framework/lifecycle}config"}, xmlns={"xmlns:config='http://www.xchain.org/framework/quartz-config'"})
  public void startConfig(LifecycleContext context, ConfigDocumentContext configDocContext)
    throws LifecycleException
  {
    // test to see if the quartz lifecycle has been disabled.
    Boolean disabledValue = getDisabled( configDocContext );
    disabled = disabledValue!=null?disabledValue.booleanValue():false;

    // if this lifecycle has been explicitly disabled, then terminate.
    if( disabled ) {
      if( log.isInfoEnabled() ) {
        log.info("The quartz lifecycle has been disabled in the configuration file.");
      }
      return;
    }
    
    String propertiesResourceUrl = (String)configDocContext.getValue("/*/config:properties-url", String.class);
    
    if (propertiesResourceUrl == null) {
      log.debug("properties-url not provided. Using default location for quartz.properties");
      propertiesResourceUrl = DEFAULT_QUARTZ_PROPERTIES;
    }
    
    this.startDelay = (Integer)configDocContext.getValue("/*/config:start-delay", Integer.class);
    this.autoStart = (Boolean)getValue(configDocContext, "/*/config:auto-start", true, Boolean.class);
    
    // create the scheduler factory.
    StdSchedulerFactory factory = null;
    InputStream defaultQuartzConfig = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesResourceUrl);
    if( defaultQuartzConfig != null ) {
      log.info("Loading Quartz Config from {}", propertiesResourceUrl);
      Properties properties = loadProperties(defaultQuartzConfig);
      try {
        factory = new StdSchedulerFactory(properties);
      }
      catch( Exception e ) {
        throw new LifecycleException("Could not load StdSchedulerFactory for '"+propertiesResourceUrl+"'.", e);
      }
    }
    else {
      log.info("Unable to load Quartz Config. Using default scheduler factory.");
      try {
        factory = new StdSchedulerFactory();
      }
      catch( Exception e ) {
        throw new LifecycleException("Could not load StdSchedulerFactory for quartz default configuration file.", e);
      }
    }

    try {
      scheduler = factory.getScheduler();
    }
    catch( Exception e ) {
      throw new LifecycleException("Could not get the default scheduler from the scheduler factory.", e);
    }
    
    try {
      // if there are any quartz-job.xml files, then load them into the scheduler.      
      Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(DEFAULT_QUARTZ_JOB_CONFIG);      
      
      JobSchedulingDataProcessor xmlProcessor = new JobSchedulingDataProcessor(new ThreadContextClassLoadHelper(), false, false);
      
      // Process the resources in reverse order to keep proper precedence in the classpath  
      Stack<URL> urlStack = new Stack<URL>();      
      while (urls.hasMoreElements()) {
        urlStack.push(urls.nextElement());
      }
      
      for (URL resourceURL : urlStack) {
        log.debug("Loading job resource {}", resourceURL.getPath());
        xmlProcessor.processStream(resourceURL.openStream(), resourceURL.getPath());
        xmlProcessor.scheduleJobs(xmlProcessor.getScheduledJobs(), scheduler, true);
      }
    } catch (Exception ex) {
      throw new LifecycleException("Could not scheduler jobs from '"+DEFAULT_QUARTZ_JOB_CONFIG+"'.", ex);
    } finally {
      try {
        defaultQuartzConfig.close();
      } catch (Exception ignore) { }
    }
  }
  
  @SuppressWarnings("unchecked")
  public <E> E getValue(ConfigDocumentContext config, String xpath, E defaultValue, Class<E> type) {
    E value = (E) config.getValue(xpath, type);
    return value == null? defaultValue : value;
  }

  /**
   * <p>Unconfigures the quartz lifecycle.  The map of schedulers is cleared and the disabled flag is set back to false.</p>
   */
  @StopStep(localName="config")
  public void stopConfig()
  {
    //schedulerMap.clear();
    scheduler = null;
    disabled = false;
  }

  /**
   * <p>Starts all of the schedulers registered with the quartz lifecycle.</p>
   */
  @StartStep(localName="run", after={"config"})
  public void startSchedulers( LifecycleContext context ) 
    throws LifecycleException
  {
    if( !disabled && autoStart ) {
      try {
        if( this.startDelay == null )
          scheduler.start();
        else
          scheduler.startDelayed(this.startDelay);
      }
      catch( Exception e ) {
        throw new LifecycleException("Could not start Scheduler.", e);
      }
    }
  }

  /**
   * <p>Stops all of the schedulers registered with the quartz lifecycle.</p>
   */
  @StopStep(localName="run")
  public void stopSchedulers( LifecycleContext context )
  {
    if( !disabled ) {
      try {
        scheduler.shutdown();
      }
      catch( Exception e ) {
        if( log.isInfoEnabled() ) {
          log.info("An exception was thrown while shutting down a quartz scheduler.", e);
        }
      }
    }
  }

  /**
   * <p>Loads properties from the input stream.  The input stream will be closed after this operation.</p>
   */
  protected static Properties loadProperties( InputStream in )
    throws LifecycleException
  {
    Properties properties = new Properties();
    try {
      properties.load(in);
    }
    catch( Exception e ) {
      try { in.close(); } catch( Exception ignore ) { }
    }
    return properties;
  }

  protected static Boolean getDisabled( ConfigDocumentContext context )
  {
    return (Boolean)context.getValue("/*/config:disabled", Boolean.class);
  }

  protected static Iterator<Pointer> getSchedulerPointerIterator( ConfigDocumentContext context )
  {
    return (Iterator<Pointer>)context.iteratePointers("*/config:scheduler");
  }
}
