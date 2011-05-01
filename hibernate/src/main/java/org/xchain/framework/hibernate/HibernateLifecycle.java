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
package org.xchain.framework.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.DefaultComponentSafeNamingStrategy;

import org.xchain.annotations.Function;
import org.xchain.framework.lifecycle.LifecycleClass;
import org.xchain.framework.lifecycle.LifecycleContext;
import org.xchain.framework.lifecycle.StartStep;
import org.xchain.framework.lifecycle.StopStep;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import javax.xml.namespace.QName;

/**
 * Manages Hibernate.
 *
 * @author Mike Moulton
 * @author Devon Tackett
 * @author Christian Trimble
 * @author John Trimble
 * @author Josh Kennedy
 */
@LifecycleClass(uri="http://www.xchain.org/hibernate/1.0")
public class HibernateLifecycle
{
  public static final QName DEFAULT_NAME = new QName("http://www.xchain.org/hibernate", "session-factory");

  private static Logger log = LoggerFactory.getLogger(HibernateLifecycle.class);

  private static boolean started = false;
  private static Map<QName, SessionFactory> sessionFactoryMap = new HashMap<QName, SessionFactory>();
  private static Map<QName, Configuration>  configurationMap  = new HashMap<QName, Configuration>();
  private static Map<SessionFactory, QName> sessionFactoryToQName = new HashMap<SessionFactory, QName>();
  private static Map<Configuration, QName> configurationToQName = new HashMap<Configuration, QName>();
  private static boolean cleanUp = false;

  /**
   * Configures a hibernate using the default hibernate configuration file
   * located on the classpath.
   */
  @StartStep(localName="hibernate", after="config")
  public static void startLifecycle()
  {
    if( log.isDebugEnabled() ) {
      log.debug( "Starting lifecycle step: hibernate" );
    }

    synchronized( HibernateLifecycle.class ) {
      if (!started) {
        if ( log.isDebugEnabled() ) {
          log.debug("Starting HibernateLifecycle");
        }
  
        try {
          for( Map.Entry<QName, Configuration> entry : configurationMap.entrySet() ) {
            if( entry.getValue() != null ) {
              //setXChainDefaults(entry.getKey(), entry.getValue());
              mapSessionFactory(entry.getKey(), entry.getValue().buildSessionFactory());
            }
          }
    
          started = true;
        }
        catch (RuntimeException ex) {
          if( log.isErrorEnabled() ) {
            log.error("Starting HibernateLifecycle failed", ex);
          }
          throw ex;
        }
      }
    }
  }

  /**
   * Shutdown the session factory to release all resources.
   */
  @StopStep(localName="hibernate")
  public static void stopLifecycle()
  {
    if( log.isDebugEnabled() ) {
      log.debug( "Stopping lifecycle step: hibernate" );
    }
    
    synchronized( HibernateLifecycle.class ) {
      if (started) {
        if ( log.isDebugEnabled() ) log.debug("Stopping HibernateLifecycle");
        try {
          started = false;
          for( Map.Entry<QName, SessionFactory> entry : sessionFactoryMap.entrySet() ) {
            close(entry.getValue());
          }
        }
        finally {
          sessionFactoryMap.clear();
          sessionFactoryToQName.clear();
        }
      }
    }
  }
  
  /**
   * Does Hibernate configuration.
   * 
   * @param context
   */
  @StartStep(localName="hibernate-configuration", before="hibernate")
  public static void startHibernateConfig( LifecycleContext context ) {
    if( installDefaultConfiguration() ) {
      if( log.isInfoEnabled() ) {
        log.info("Reading default Hibernate configuration from '/hibernate.cfg.xml' and '/hibernate.properties'.");
      }

      // Uses the hibernate annotation configuration, replace with Configuration() if you
      // don't use annotations or JDK 5.0
      Configuration configuration = new AnnotationConfiguration();

      ((AnnotationConfiguration)configuration).setNamingStrategy(new DefaultComponentSafeNamingStrategy());

      // Read not only hibernate.properties, but also hibernate.cfg.xml
      configuration.configure();

      // set the configuration.
      HibernateLifecycle.setConfiguration(configuration);

      // we will need to clean this up after the lifecycle has started.
      cleanUp = true;
    }
  }
  
  @StopStep(localName="hibernate-configuration")
  public static void stopHibernateConfig( LifecycleContext context ) {
    // clean up the configuration.
    if( cleanUp == true ) {
      HibernateLifecycle.setConfiguration(null);
      cleanUp = false;
    }
  }
  
  /**
   * Returns true if we need to create a default configuration.
   */
  private static boolean installDefaultConfiguration()
  {
    return HibernateLifecycle.getConfiguration() == null && (hasResource("hibernate.cfg.xml") || hasResource("hibernate.properties"));
  }

  /**
   * Sets the default configuration.
   */
  public static void setConfiguration( Configuration configuration )
  {
    synchronized( HibernateLifecycle.class ) {
      setConfiguration( DEFAULT_NAME, configuration );
    }
  }
  
  /**
   * Returns true if the resource path is defined in the context class loader.
   */
  private static boolean hasResource(String resourcePath)
  {
    return Thread.currentThread().getContextClassLoader().getResource(resourcePath) != null;
  }

  /**
   * Sets a named configuration.
   */
  public static void setConfiguration( QName name, Configuration configuration )
  {
    synchronized( HibernateLifecycle.class ) {
      if( name == null ) {
        setConfiguration(configuration);
      } 
      if( !HibernateLifecycle.started ) {
        HibernateLifecycle.configurationMap.put(name, configuration);
        HibernateLifecycle.configurationToQName.put(configuration, name);
      }
      else {
        throw new IllegalStateException("A named hibernate configuration cannot be set while the lifecycle is running.");
      }
    }
  }

  private static void setXChainDefaults( QName name, Configuration configuration )
  {
    String dataSourceJndi = configuration.getProperty(Environment.DATASOURCE);
    String connectionProvider = configuration.getProperty(Environment.CONNECTION_PROVIDER);

    if( dataSourceJndi != null && connectionProvider == null ) {
      if( log.isInfoEnabled() ) {
        log.info("Adding the property '"+Environment.CONNECTION_PROVIDER+"' with value 'org.xchain.framework.hibernate.RebindingDataSourceConnectionProvider' to the hibernate configuration of datasource '"+dataSourceJndi+"'.");
      }
      configuration.setProperty(Environment.CONNECTION_PROVIDER,  "org.xchain.framework.hibernate.RebindingDataSourceConnectionProvider");
    }
  }

  private static void mapSessionFactory( QName name, SessionFactory sessionFactory )
  {
    synchronized( HibernateLifecycle.class ) {
      HibernateLifecycle.sessionFactoryMap.put(name, sessionFactory);
      HibernateLifecycle.sessionFactoryToQName.put(sessionFactory, name);
    }
  }

  /**
   * Returns the Hibernate configuration.
   *
   * @return Configuration
   */
  public static Configuration getConfiguration() {
    return getConfiguration(DEFAULT_NAME);
  }

  public static Configuration getConfiguration( QName name )
  {
    synchronized( HibernateLifecycle.class ) {
      if( name == null ) {
        name = DEFAULT_NAME;
      }
      return HibernateLifecycle.configurationMap.get(name);
    }
  }

  /**
   * Returns the Hibernate Session Factory.
   *
   * @return SessionFactory
   * @throws SessionFactoryNotFoundException if there is not a session factory defined for the default QName.
   */
  public static SessionFactory getSessionFactory() {
    return getSessionFactory(DEFAULT_NAME);
  }

  /**
   * Returns a named Hibernate Session Factory.
   *
   * @param name the name of the session factory.
   *
   * @return the SessionFactory with the specified name.
   * @throws SessionFactoryNotFoundException if there is not a session factory defined for the specified QName.
   */
  public static SessionFactory getSessionFactory( QName name )
  {
    SessionFactory sessionFactory = null;
    synchronized( HibernateLifecycle.class ) {
      if( name == null ) {
        name=DEFAULT_NAME;
      }
      sessionFactory = HibernateLifecycle.sessionFactoryMap.get(name);

      if( sessionFactory == null ) {
        throw new SessionFactoryNotFoundException(name);
      }

      return sessionFactory;
    }
  }

  public static Set<QName> getSessionFactoryNames()
  {
    synchronized( HibernateLifecycle.class ) {
      return new HashSet<QName>(sessionFactoryMap.keySet());
    }
  }

  public static QName getQName( SessionFactory sessionFactory )
  {
    synchronized( HibernateLifecycle.class ) {
      return sessionFactoryToQName.get(sessionFactory);
    }
  }

  public static QName getQName( Configuration configuration )
  {
    synchronized( HibernateLifecycle.class ) {
      return configurationToQName.get(configuration);
    }
  }

  /**
   * Get the current Hibernate session.
   *
   * NOTE: This method is not compatible with sessions created with SessionFactory.openSession().  This includes the current &lt;hibernate:session&gt; command, since
   * it uses SessionFactory.openSession() to get its sessions.
   * 
   * @return Session
   */
  @Function(localName="current-session")
  public static Session getCurrentSession() {
    return getSessionFactory(DEFAULT_NAME).getCurrentSession();
  }

  /**
   * Gets the current Hibernate session for the named session factory.  If the name is null,
   * then getCurrentSession() is returned.
   *
   * @return the current Session for the named SessionFactory.
   */
  @Function(localName="current-session")
  public static Session getCurrentSession( QName name ) {
    synchronized( HibernateLifecycle.class ) {
      if( name == null ) {
        name = DEFAULT_NAME;
      }

      return getSessionFactory(name).getCurrentSession();
    }
  }

  private static void close( SessionFactory sessionFactory )
  {
    if( sessionFactory != null ) {
      try {
        sessionFactory.close();
      }
      catch( Throwable e ) {
        if( log.isWarnEnabled() ) {
          log.warn("A hibernate session factory could not be closed.", e);
        }
      }
    }
  }
}

