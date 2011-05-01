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

import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.util.NamingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A data source connection provider that will rebind the data source if it fails to create a connection.  When using the 'hibernate.connection.datasource' property, XChains will use this
 * connection provider, unless the property 'hibernate.connection.provider_class' has been specified in the configuration.
 *
 * @author Christian Trimble
 * @author Josh Kennedy
 */
public class RebindingDataSourceConnectionProvider
  implements ConnectionProvider
{
  /** The log for this data source provider. */
  private static Logger log = LoggerFactory.getLogger(RebindingDataSourceConnectionProvider.class);

  /** The jndi name of the data source. */
  private String jndiName = null;

  /** The user name for creating connections. */
  private String userName = null;

  /** The password for creating connections. */
  private String password = null;

  /** The initial context for looking up data sources. */
  private InitialContext initialContext = null;

  /** The current data source for creating connections. */
  private DataSource dataSource = null;

  /**
   * Configures this connection provider with the specified properties.  This method will only fail with a hibernate exception if the 'hibernate.connection.datasource' property is not specified,
   * or if the JNDI initial context cannot be created.  Otherwise, this method will log any problems with the configuration, or looking up the data source and return normally.
   *
   * @throws HibernateException if the property 'hibernate.connection.datasource' property is not specified, or if the JNDI initial context cannot be created.
   */
  public void configure(Properties properties)
    throws HibernateException
  {
    jndiName = properties.getProperty( Environment.DATASOURCE );
    userName = properties.getProperty( Environment.USER );
    password = properties.getProperty( Environment.PASS );

    if( jndiName == null ) {
      if( log.isErrorEnabled() ) {
        log.error("The JNDI name of the JDBC data source was not specified.  Please set the hibernate property '"+Environment.DATASOURCE+"'.");
      }
      throw new HibernateException("The JNDI name of the JDBC data source was not specified.  Please set the hibernate property '"+Environment.DATASOURCE+"'.");
    }

    if( userName != null && password == null && log.isWarnEnabled() ) {
      log.warn("A JDBC user name and password will not be used because the hibernate property '"+Environment.PASS+"' is not set.");
    }

    if( userName == null && password != null && log.isWarnEnabled() ) {
      log.warn("A JDBC user name and password will not be used because the hibernate property '"+Environment.USER+"' is not set.");
    }

    try {
      initialContext = NamingHelper.getInitialContext(properties);
    }
    catch( NamingException ne ) {
      throw new HibernateException("Could not create initial context for looking up datasource.", ne);
    }

    try {
      initializeDataSource();
    }
    catch( SQLException sqle ) {
      // this excetion has been logged.  let startup continue, as the application will start responding once the configuration issues are resolved.
    }
  }

  /**
   * Initialized, or reinitializes, the data source for this connection provider.  This method logs any errors that prevented the initialization of the data source and will only update
   * the datasource reference if a new datasource could be obtained.
   *
   * @throws SQLException if the data source could not be loaded from JNDI for any reason.
   */
  private void initializeDataSource()
    throws SQLException
  {
    Object jndiObject = null;

    // Look up the object bound to the jndi name.
    try {
      jndiObject = initialContext.lookup(jndiName);
    }
    catch( NamingException ne ) {
      if( log.isErrorEnabled() ) {
        log.error("A JNDI exception was thrown while looking up the JDBC data source at '"+jndiName+"'.", ne);
      }
      throw new SQLException("A JNDI exception was thrown while looking up the JDBC data source at '"+jndiName+"'.");
    }

    // If we did not find an object, bail out.
    if( jndiObject == null ) {
      if( log.isErrorEnabled() ) {
        log.error("There is not a javax.sql.DataSource bound to the JNDI name '"+jndiName+"'.  This JNDI name is not bound to an object.");
      }
      throw new SQLException("There is not a javax.sql.DataSource bound to the JNDI name '"+jndiName+"'.  This JNDI name is not bound to an object.");
    }

    // if the object is not a data source, bail out.
    if( !(jndiObject instanceof DataSource) ) {
      if( log.isErrorEnabled() ) {
        log.error("There is not a javax.sql.DataSource bound to the JNDI name '"+jndiName+"'.  This JNDI name is currently bound to an object of type '"+jndiObject.getClass().getName()+"'.");
      }
      throw new SQLException("There is not a javax.sql.DataSource bound to the JNDI name '"+jndiName+"'.  This JNDI name is currently bound to an object of type '"+jndiObject.getClass().getName()+"'.");
    }

    // We are ready to set this data source.
    dataSource = (DataSource)jndiObject;
  }

  /**
   * Returns a connection from the currently configured JDBC data source.
   *
   * @throws SQLException if the data source is null, or if the data source throws an exception while creating the connection.
   * @return a connection from the currently configured JDBC data source.
   */
  private Connection getConnectionFromDataSource()
    throws SQLException
  {
    if( dataSource == null ) {
      throw new SQLException("A JDBC data source could not be found at JNDI name '"+jndiName+"'.");
    }

    if( userName != null && password != null ) {
      return dataSource.getConnection(userName, password);
    }
    else {
      return dataSource.getConnection();
    }
  }

  /**
   * Gets a connection from the datasource bound to jndi.  If getting the connection fails, this method will attempt to rebind the datasource once, then fail if rebinding fails or the newly bound
   * DataSource fails.  This method is synchronized, so that not more than one thread attempts to rebind the datasource at a time.  This should not cause a major performance hit, since the datasource
   * pool will most likely be synchronized as well.
   *
   * @throws SQLException if a fresh copy of the datasource from JNDI caused an exception to be thrown.
   * @return a connection to the data source, creating a new data source if required.
   */
  public synchronized Connection getConnection()
    throws SQLException
  {
    try {
      return getConnectionFromDataSource();
    }
    catch( Exception exception ) { 
      if( log.isWarnEnabled() ) {
        log.warn("Attempting to rebind to the jndi datasource due to an exception.", exception);
      }
      initializeDataSource();
      return getConnectionFromDataSource();
    }
  }

  /**
   * Closes the specified connection.
   *
   * @param connection the connection to close.
   * @throws SQLException if an exception is thrown while closing the connection.
   */
  public void closeConnection( Connection connection )
    throws SQLException
  {
    connection.close();
  }

  /**
   * Closes this connection provider.
   */
  public void close()
  {
    initialContext = null;
    dataSource = null;
    jndiName = null;
    userName = null;
    password = null;
  }

  public boolean supportsAggressiveRelease()
  {
    return true;
  }
}
