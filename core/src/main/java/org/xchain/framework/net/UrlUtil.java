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
package org.xchain.framework.net;

import java.net.URL;
import java.net.URLConnection;
import java.net.JarURLConnection;
import java.util.jar.JarEntry;
import java.util.Collections;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.io.IOException;
import org.xchain.framework.net.protocol.resource.ResourceNotFoundException;
import org.xchain.framework.net.strategy.JarUrlExistsStrategy;
import org.xchain.framework.net.strategy.FileUrlExistsStrategy;
import org.xchain.framework.net.strategy.ResourceUrlExistsStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for Url related methods.
 *
 * @author Christian Trimble
 * @author Mike Moulton
 * @author Jason Rose
 * @author Devon Tackett
 * @author Josh Kennedy
 */
public class UrlUtil
{
  /** The log for the url util. */
  public static Logger log = LoggerFactory.getLogger( UrlUtil.class );

  public static UrlUtil instance = new UrlUtil();

  public static UrlUtil getInstance()
  {
    return instance;
  }

  public Map<String, UrlExistsStrategy> existsStrategyMap = Collections.synchronizedMap(new HashMap<String, UrlExistsStrategy>());

  /**
   * Register the given UrlExistsStrategy.  UrlExistsStrategies must be registered before a URL can be checked on that protocol.
   * 
   * @param strategy The strategy to register.
   */
  private void registerExistsStrategy(UrlExistsStrategy strategy )
  {
    existsStrategyMap.put( strategy.getProtocol(), strategy );
  }

  static {
    // Register UrlExistsStrategies.
    UrlUtil.getInstance().registerExistsStrategy(new FileUrlExistsStrategy());
    UrlUtil.getInstance().registerExistsStrategy(new JarUrlExistsStrategy());
    UrlUtil.getInstance().registerExistsStrategy(new ResourceUrlExistsStrategy());
  }

  /**
   * Check if the given systemId references something that exists.
   * 
   * @param systemId The systemId to check.
   */
  public boolean exists( String systemId )
    throws Exception
  {
    return exists(UrlFactory.getInstance().newUrl(systemId));
  }

  /**
   * Check if the given URL exists.  This uses UrlExistsStrategy implementations to determine if the given URL exists.
   * 
   * @param url The URL to check.
   * 
   * @return True if the given URL exists.
   */
  public boolean exists( URL url )
    throws Exception
  {
    // get the protocol.
    String protocol = url.getProtocol();

    // get the strategy for this protocol.
    UrlExistsStrategy strategy = (UrlExistsStrategy)existsStrategyMap.get(protocol);

    if( log.isDebugEnabled() ) {
      log.debug("Checking if '"+url.toExternalForm()+"' is exists ("+ protocol+").");
    }

    if( strategy != null ) {
      return strategy.exists(url);
    }
    else {
      URLConnection connection = null;
      try {
        connection = url.openConnection();
        connection.connect();
        String statusCode = connection.getHeaderField("Status-Code");
        if( statusCode == null ) {
          if( log.isDebugEnabled() ) {
            log.debug("The status code for '"+url.toExternalForm()+"' is null.");
          }
          return false;
        }
        else {
          return statusCode.matches("2\\d\\d");
        }
      }
      catch (ResourceNotFoundException rnfe) {
        return false;
      }
      finally {
        closeConnection(connection);
      }
    }
  }

  /**
   * @return The last modified date for the url.
   */
  public long lastModified( URL url )
    throws IOException
  {
    URLConnection connection = null;
    try {
       connection = url.openConnection();
    
       return lastModified(connection);
    }
    finally {
      closeConnection(connection);
    }
  }

  /**
   * Returns the last modified date for this url connection.
   */
  public long lastModified( URLConnection connection )
  {
    long lastModified = 0;

    // if this url connection is a jar url, then we need to try and get the date from the entry.
    try {
      if( connection instanceof JarURLConnection ) {
        try {
          JarURLConnection jarConnection = (JarURLConnection)connection;
          JarEntry jarEntry = jarConnection.getJarEntry();
          if( jarEntry != null ) {
            lastModified = jarEntry.getTime();
          }
        }
        catch( Exception e ) {
          if( log.isWarnEnabled() ) {
            log.warn("Could not get date from jar entry.", e);
          }
        }
      }

      // if the last modified date was not set, then return the standard last modified date.
      if( lastModified <= 0 ) {
        lastModified = connection.getLastModified();
      }
    }
    finally {
      closeConnection(connection);
    }

    return lastModified;
  }

  /**
   * Determine if a given url was last modified before another given url.
   * 
   * @param url The url to check if it was last modified before.
   * @param otherUrl The url to check against.
   * 
   * @return True if the url was last modified before the otherUrl.
   */
  public boolean lastModifiedBefore( URL url, URL otherUrl )
    throws IOException
  {
    URLConnection connection = null;
    URLConnection otherConnection = null;
    try {
      connection = url.openConnection();
      long lastModified = lastModified(connection);

      if( log.isDebugEnabled() ) {
        log.debug("Url '"+url+"' has last modified date "+lastModified+".");
      }

      otherConnection = otherUrl.openConnection();
      long otherLastModified = lastModified(otherConnection);

      if( log.isDebugEnabled() ) {
        log.debug("Url '"+otherUrl+"' has last modified date "+otherLastModified+".");
      }
      return lastModified < otherLastModified;
    }
    finally {
      closeConnection(connection);
      closeConnection(otherConnection);
    }
    /*
    UrlModifiedDateStrategy urlModifiedDateStrategy = (UrlModifiedDateStrategy)modifiedDateStrategyMap.get(url.getProtocol());
    UrlModifiedDateStrategy otherUrlModifiedDateStrategy = (UrlModifiedDateStrategy)modifiedDateStrategyMap.get(otherUrl.getProtocol());

    // either is null, then set the default.
    if( urlModifiedDateStrategy == null ) { urlModifiedDateStrategy = defaultUrlModifiedDateStrategy; }
    if( otherUrlModifiedDateStrategy == null ) { otherUrlModifiedDateStrategy = defaultUrlModifiedDateStrategy; }

    return urlModifiedDateStrategy.getLongDate( url ) > 
    */
  }

  /**
   * Determine if the given url was last modified before the given set of urls.
   * 
   * @param url The url to check with.
   * @param urlSet The set of urls to check against.
   * 
   * @return True if the given url was last modified before any of the urls in the given set.
   */
  public boolean lastModifiedBefore( URL url, Set<URL> urlSet )
    throws IOException
  {
    boolean result = false;
    URLConnection connection = null;
    URLConnection otherConnection = null;
    
    try {
      // Open the connection.
      connection = url.openConnection();
      // Get the last modified date.
      long lastModified = lastModified(connection);
      if( log.isDebugEnabled() ) {
        log.debug("Url '"+url+"' has last modified date "+lastModified+".");
      }
  
      // Iterate over the given set of URLs.
      Iterator<URL> urlIterator = urlSet.iterator();
      while( urlIterator.hasNext() && !result ) {
        URL otherUrl = urlIterator.next();
        // Open the other connection.
        otherConnection = otherUrl.openConnection();
        // Get the last modified date.
        long otherLastModified = lastModified(otherConnection);
        if( log.isDebugEnabled() ) {
          log.debug("Url '"+otherUrl+"' has last modified date "+otherLastModified+".");
        }
        // Close the other connection.
        closeConnection(otherConnection);
        result = lastModified < otherLastModified;
      }
    } finally {
      // Make sure both connections are closed.
      closeConnection(connection);
      closeConnection(otherConnection);
    }

    return result;
  }

  /**
   * Check if the given URL has been modified after the given date.
   * 
   * @param url The URL to check.
   * @param lastModified The last modified date to check on.
   * 
   * @return True if the given URL has been modified after the given date.
   */
  public boolean lastModifiedAfter( URL url, long lastModified )
    throws IOException
  {
    // get the load time for the url.
    long urlLastModified = lastModified(url);

    // if the url last modified is after the load date, then a reload is required.
    return lastModified < urlLastModified;

  }

  /**
   * Check if the given collection of URLs has been modified after the given date.
   * 
   * @param urlCollection The collection of URLs to check.
   * @param lastModified The last modified date to check on.
   * 
   * @return True if any of the given URLs has been modified after the given date.
   */
  public boolean lastModifiedAfter( Collection<URL> urlCollection, long lastModified )
    throws IOException
  {
    boolean result = false;

    // if the load date is before any of the urls in the dependency set, then we need to reload.
    Iterator<URL> urlIterator = urlCollection.iterator();
    while( urlIterator.hasNext() && !result ) {
      result = lastModifiedAfter(urlIterator.next(), lastModified);
    }

    return result;
  }

  /**
   * Unwrap any level of wrapped UrlConnectionWrappers.
   * 
   * @param connection The URLConnection to unwrap.
   * 
   * @return A URLConnection that is not wrapped by a UrlConnectionWrapper.
   */
  public URLConnection unwrapConnection( URLConnection connection )
  {
    while( connection instanceof UrlConnectionWrapper ) {
      connection = ((UrlConnectionWrapper)connection).getWrapped();
    }
    return connection;
  }

  /**
   * Close the input and output streams for the given URLConnection.
   * 
   * @param connection The connection to close.
   */
  public void closeConnection( URLConnection connection )
  {
    if( connection != null ) {
      closeInputStream(connection);
      closeOutputStream(connection);
    }
  }

  /**
   * Closes the input stream for a url connection.
   */
  public static void closeInputStream( URLConnection urlConnection )
  {
    if( urlConnection != null && urlConnection.getDoInput() ) {
      try {
        urlConnection.getInputStream().close();
      }
      catch( Exception ioe ) {
        // do nothing.
        if( log.isDebugEnabled() ) {
          log.debug("Exception thrown while closing connection.", ioe);
        }
      }
    }
  }

  /**
   * Closes the output stream for a url connection.
   */
  public static void closeOutputStream( URLConnection urlConnection )
  {
    if( urlConnection != null && urlConnection.getDoOutput() ) {
      try {
        urlConnection.getInputStream().close();
      }
      catch( Exception ioe ) {
        // do nothing.
        if( log.isDebugEnabled() ) {
          log.debug("Exception thrown while closing connection.", ioe);
        }
      }
    }
  }
}
