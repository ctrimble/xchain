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

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DependencyTracker is used to track dependencies.  Dependency tracking is thread safe, but a thread can only track dependencies for a single entity at a time.
 * Dependency tracking is started with the startTracking() method. Dependencies will be registered with dependencyFound() or dependencySetFound methods or
 * when a URI is resolved with a wrapped URIResolver.  Wrapped URIResolvers are created with the createDependencyUriResolver() method.
 * Dependency tracking is stopped with the stopTracking() method.  This method will return a set of URLs which are the tracked dependencies.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
public class DependencyTracker
{
  protected static Logger log = LoggerFactory.getLogger( DependencyTracker.class );

  protected static DependencyTracker instance = new DependencyTracker();

  private DependencyTracker()
  {
  }

  /**
   * @return The current instance of the DependencyTracker.
   */
  public static DependencyTracker getInstance()
  {
    return instance;
  }

  protected ThreadLocal<LinkedList<List<URL>>> dependencyListStackThreadLocal = new ThreadLocal<LinkedList<List<URL>>>() {
    protected LinkedList<List<URL>> initialValue()
    {
      return new LinkedList<List<URL>>();
    }
  };

  protected LinkedList<List<URL>> getDependencyListStack()
  {
    return dependencyListStackThreadLocal.get();
  }

  /**
   * Start tracking dependency usages on the current thread.
   */
  public void startTracking()
  {
    LinkedList<List<URL>> dependencyListStack = getDependencyListStack();
    List<URL> dependencyList = null;

    if( dependencyListStack.isEmpty() ) {
      dependencyList = new ArrayList<URL>();
    }
    else {
      dependencyList = (List<URL>)dependencyListStack.getFirst();
      int size = dependencyList.size();
      dependencyList = dependencyList.subList( size, size );
    }

    dependencyListStack.addFirst(dependencyList);
  }

  /**
   * Stop tracking dependencies on the current thread.
   * 
   * @return The set of URL dependencies tracked.
   */
  public Set<URL> stopTracking()
  {
    LinkedList<List<URL>> dependencyListStack = getDependencyListStack();

    if( !dependencyListStack.isEmpty() ) {
      List<URL> dependencyList = dependencyListStack.removeFirst();
      return new HashSet<URL>(dependencyList);
    }
    else {
      return Collections.EMPTY_SET;
    }
  }

  /**
   * Create a wrapped URIResolver for dependency tracking.  Any URLs resolved through
   * the returned URIResolve will be considered dependencies while tracking is on.
   * 
   * @param uriResolver The original URI resolver to wrap.
   * 
   * @return The wrapped URI resolver.
   */
  public URIResolver createDependencyUriResolver( URIResolver uriResolver )
  {
    DependencyUriResolver dependencyUriResolver = null;

    if( uriResolver != null && uriResolver instanceof DependencyUriResolver ) {
      dependencyUriResolver = (DependencyUriResolver)uriResolver;
    }
    else {
      dependencyUriResolver = new DependencyUriResolver(uriResolver);
    }

    return dependencyUriResolver;
  }

  /**
   * Register the given URL as a dependency.
   * 
   * @param dependency The dependency URL.
   */
  public void dependencyFound( URL dependency )
  {
    LinkedList<List<URL>> dependencyListStack = getDependencyListStack();

    if( !dependencyListStack.isEmpty() ) {
      List<URL> dependencyList = dependencyListStack.getFirst();

      if( dependencyList != null ) {
        dependencyList.add(dependency);
      }
    }
  }

  /**
   * Register the given set of dependency URLs.
   * 
   * @param dependencySet The set of dependencies to register.
   */
  public void dependencySetFound( Set<URL> dependencySet )
  {
    LinkedList<List<URL>> dependencyListStack = getDependencyListStack();
    if( !dependencyListStack.isEmpty() ) {
      List<URL> dependencyList = dependencyListStack.getFirst();

      if( dependencyList != null ) {
        dependencyList.addAll(dependencySet);
      }
    }
  }

  /**
   * Wrapped URIResolver to track dependencies.
   */
  public class DependencyUriResolver
    implements URIResolver
  {
    public URIResolver wrapped;
    public DependencyUriResolver( URIResolver wrapped )
    {
      this.wrapped = wrapped;
    }

    /**
     * Resolve the given URI.  The URI will be considered a dependency if used while dependency tracking is on.
     */
    public Source resolve( String href, String base )
      throws TransformerException
    {
      if( log.isDebugEnabled() ) {
        log.debug("Tracking href '"+href+"' and base '"+base+"'.");
      }

      // get the url.
      try {
        URL dependency = UrlFactory.getInstance().newUrl( base, href );

        // track the url.
        dependencyFound( dependency ); 
      }
      catch( Exception e ) {
        throw new TransformerException("Could not track dependency for base '"+base+"' and href '"+href+"'.", e);
      }

      // return the value of the wrapped resolver.
      if( wrapped != null ) {
        return wrapped.resolve( href, base );
      }
      else {
        return null;
      }
    }
  }
}
