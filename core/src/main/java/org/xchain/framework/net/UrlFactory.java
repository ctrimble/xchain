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
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import java.util.Map;
import java.util.HashMap;

import org.xchain.framework.net.protocol.resource.ResourceUrlStreamHandlerFactory;
import org.xchain.framework.net.protocol.http.HttpUrlStreamHandlerFactory;

/**
 * This factory provides a mechanism to register url protocols when you cannot set the URLStreamHandlerFactory on the URL class.
 * This occurs in Web Applications, since most containers set the URLStreamHandlerFactory when they initialize.
 *
 * When used in a Web Application, this class should always reside in the WEB-INF/lib directory of the application.  If this class were
 * deployed in the containers class loader, then protocols would be registered across web applications and this is not the desired effect
 * of this class.
 *
 * @author Christian Trimble
 * @author Jason Rose
 * @author Josh Kennedy
 */
public final class UrlFactory
{
  /** The log for this class. */
  public static Logger log = LoggerFactory.getLogger(UrlFactory.class);

  /** The pattern object for parsing url protocols. */
  protected static Pattern protocolPattern = null;

  /** The pattern string for parsing url protocols. */
  protected static String protocolPatternString = "\\A([a-zA-Z][-a-zA-Z0-9\\.\\+]+):.*\\Z";

  /* Compile the protocol pattern. */
  static {
    try {
      protocolPattern = Pattern.compile(protocolPatternString);
    }
    catch( PatternSyntaxException pse ) {
      log.error( "Could not compile protocol pattern '"+protocolPatternString+"'.", pse );
    }
  }

  /** The singleton instance of this class. */
  private static UrlFactory instance = new UrlFactory();

  static {
    try {
      // NOTE: This should be done using xml configuration files.

      Map<String, URLStreamHandlerFactory> protocolMap = UrlFactory.getInstance().getProtocolMap();

      synchronized( protocolMap ) {
        protocolMap.put(ResourceUrlStreamHandlerFactory.RESOURCE_PROTOCOL, new ResourceUrlStreamHandlerFactory());
        protocolMap.put(HttpUrlStreamHandlerFactory.HTTP_PROTOCOL, new HttpUrlStreamHandlerFactory());
      }

      if( log.isDebugEnabled() ) {
        log.debug("Loaded protocol '"+ResourceUrlStreamHandlerFactory.RESOURCE_PROTOCOL+"'.");
      }
    }
    catch( Exception e ) {
      log.error( "Could not load default protocols.", e );
    }
  }

  /**
   * Returns the UrlFactory singleton.
   */
  public static UrlFactory getInstance()
  {
    return instance;
  }

  /**
   * This URLStreamHandlerFactory maps protocols to URLStreamHandlerFactories.  This allows overriding in cases where one handler factory
   * implements several protocols.  
   */
  protected MappedUrlStreamHandlerFactory factory = null;

  /**
   * The map of protocols currently defined for this url factory.
   */
  protected Map<String, URLStreamHandlerFactory> protocolMap = java.util.Collections.synchronizedMap(new HashMap<String, URLStreamHandlerFactory>());

  private UrlFactory()
  {
    this.factory = new MappedUrlStreamHandlerFactory();
  }

  /**
   * Returns the mapping of protocols to URLStreamHandlerFactory classes.  This mapping is synchronized with the java.util.Collections class, please
   * refer to its documentation when editing this map.
   */
  public Map<String, URLStreamHandlerFactory> getProtocolMap()
  {
    return protocolMap;
  }

  /**
   * Creates a new url based on a spec.
   */
  public URL newUrl( String spec )
    throws MalformedURLException
  {
    if( log.isDebugEnabled() ) {
      log.debug("newUrl(String spec)");
    }
    return newUrl( (URL)null, spec, (URLStreamHandler)null );
  }

  public URL newUrl( String spec, URLStreamHandler handler )
    throws MalformedURLException
  {
    if( log.isDebugEnabled() ) {
      log.debug("newUrl( String spec, URLStreamHandler handler )");
    }
    return newUrl( (URL)null, spec, handler );
  }

  /**
   * Creates a new URL based on the parts of a spec.
   */
  public URL newUrl( String protocol, String host, int port, String file)
    throws MalformedURLException
  {
    if( log.isDebugEnabled() ) {
      log.debug("newUrl( String protocol, String host, int port, String file)");
    }

    // return a new url with this handler.
    return newUrl( protocol, host, port, file, (URLStreamHandler)null );
  }

  public URL newUrl( String protocol, String host, int port, String file, URLStreamHandler handler )
    throws MalformedURLException
  {
    if( log.isDebugEnabled() ) {
      log.debug("newUrl( String protocol, String host, int port, String file, URLStreamHandler handler )");
    }
    return new URL( protocol, host, port, file, lookupUrlStreamHandler(protocol, handler) );
  }

  /**
   * Creates a new URL based on the parts of a spec.
   */
  public URL newUrl( String protocol, String host, String file )
    throws MalformedURLException
  {
    if( log.isDebugEnabled() ) {
      log.debug("newUrl( String protocol, String host, String file )");
    }

    // return a new url with this handler.
    return newUrl( protocol, host, -1, file, (URLStreamHandler)null );
  }

  public URL newUrl( String protocol, String host, String file, URLStreamHandler handler )
    throws MalformedURLException
  {
    if( log.isDebugEnabled() ) {
      log.debug("newUrl( String protocol, String host, String file, URLStreamHandler handler )");
    }
    return newUrl( protocol, host, -1, file, handler );
  }

  /**
   * This method loads the URLStreamHandler for the spec (or context if the spec is relative) and then passes them
   * to the newUrl( URL, String, URLStreamHandler ) method.
   */
  public URL newUrl( URL context, String spec )
    throws MalformedURLException
  {
    if( log.isDebugEnabled() ) {
      log.debug("newUrl( URL context, String spec )");
    }

    // return a new url with this handler.
    return newUrl( context, spec, (URLStreamHandler)null );
  }

  /**
   * Returns a new URL object by passing these arguments to the matching URL constructor.
   */
  public URL newUrl( URL context, String spec, URLStreamHandler handler )
    throws MalformedURLException
  {
    if( log.isDebugEnabled() ) {
      log.debug("newUrl( URL context, String spec, URLStreamHandler handler )");
    }

    // find the protocol.
    String protocol = parseProtocol(spec, false);
    if( protocol == null && context != null ) {
      protocol = context.getProtocol();
    }

    if( protocol == null ) {
      throw new MalformedURLException("No protocol specified.");
    }

    // create the url.
    return new URL( (URL)null, RelativeUrlUtil.resolve(context, spec), lookupUrlStreamHandler( protocol, handler ) );
  }

  /**
   * This method creates a URL object for the supplied context and then passes the arguments to the
   * newUrl( URL, String ) method.
   */
  public URL newUrl( String context, String spec )
    throws MalformedURLException
  {
    if( log.isDebugEnabled() ) {
      log.debug("newUrl( String context, String spec )");
    }
    URL contextUrl = null;
    if( context != null ) {
      contextUrl = newUrl(context);
    }

    // return a new url with this handler.
    return newUrl(contextUrl, spec, (URLStreamHandler)null );
  }

  /**
   * This method creates a new URL object for the context using the provided handler and then passes the
   * aguments to the newUrl( URL, String, URLStreamHandler ) method to create the URL.
   */
  public URL newUrl( String context, String spec, URLStreamHandler handler )
    throws MalformedURLException
  {
    if( log.isDebugEnabled() ) {
      log.debug("newUrl( String context, String spec, URLStreamHandler handler )");
    }

    // create a url object for the context.
    URL contextUrl = null;
    if( context != null ) {
      contextUrl = newUrl( context, handler );
    }

    // call newUrl( URL, String, URLStreamHandler )
    return newUrl( contextUrl, spec, handler );
  }

  /**
   * Returns the url stream handler to pass to new URL().  If the handler is not null, then
   * the handler is returned.  If the handler is null, then the registered handler for the
   * specified protocol is returned.  If the handler is null and there is no handler registered
   * for the protocol, then null is returned.
   */
  protected URLStreamHandler lookupUrlStreamHandler( String protocol, URLStreamHandler handler )
  {
    if( handler != null ) {
      return handler;
    }

    if( factory != null ) {
      return factory.createURLStreamHandler(protocol);
    }

    return null;
  }

  /**
   * Parses the protocol from the spec.  This method supports the scheme syntax found in rfc2396.
   * If a valid protocol cannot be parsed from the provided spec, then null is returned.
   */
  public String parseProtocol( String spec )
    throws MalformedURLException
  {
    return parseProtocol( spec, false );
  }

  public String parseProtocol( String spec, boolean malformedIfNoProtocol )
    throws MalformedURLException
  {
    Matcher protocolMatcher = protocolPattern.matcher( spec );

    if( protocolMatcher.find() ) {
      return protocolMatcher.group(1);
    }
    else if( malformedIfNoProtocol ) {
      throw new MalformedURLException("Could not parse protocol from url '"+spec+"'.");
    }
    else {
      return null;
    }
  }

  /**
   * The class used to route protocols to the proper URLStreamHandlerFactory.
   */
  public class MappedUrlStreamHandlerFactory
    implements URLStreamHandlerFactory
  {

    private MappedUrlStreamHandlerFactory()
    {
    }

    public URLStreamHandler createURLStreamHandler( String protocol )
    {
      URLStreamHandler handler = null;
      URLStreamHandlerFactory targetFactory = protocolMap.get(protocol);

      if( targetFactory != null ) {
        handler = targetFactory.createURLStreamHandler(protocol);
      }

      return handler;
    }
  }
}
