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
package org.xchain.framework.strategy;

import java.net.URL;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.framework.net.DependencyTracker;
import org.xchain.framework.net.UrlFactory;
import org.xchain.framework.net.UrlUtil;
import org.xchain.framework.util.LruCacheMap;
import org.xchain.framework.lifecycle.Lifecycle;

/**
 * A LoadStrategy implementation that performs caching.
 *
 * @param <T> The class being cached.
 * @param <S> The source type for the objects.
 *
 * @author Devon Tackett
 * @author Christian Trimble
 * @author Mike Moulton
 * @author Josh Kennedy
 */
public class CachingLoadStrategy<T, S> implements LoadStrategy<T, S> {
	public static Logger log = LoggerFactory.getLogger( CachingLoadStrategy.class );	
	private LruCacheMap<String, CachedObject<T>> objectCache;
	
	/**
	 * @param size The maximum size for the LruMap.
	 */
	public CachingLoadStrategy(int size) {
		super();
		this.objectCache = new LruCacheMap<String, CachedObject<T>>(size);
	}

	public T getObject(String systemId, SourceStrategy<S> sourceStrategy,
			ConsumerStrategy<T, S> consumerStrategy)
		throws Exception
	{
		
		CachedObject<T> cachedObject = null;
		
		// Synchronize on the objectCache to only allow one lookup at a time.
		synchronized(objectCache) {
			cachedObject = objectCache.get(systemId);
			
			if (cachedObject == null) {
				// The request object was not found in the cache.
				// Create a new entry for the cache.
				cachedObject = new CachedObject<T>();
				cachedObject.setSystemId(systemId);
				objectCache.put(systemId, cachedObject);
			}
		}
		
		// Synchronize on the cachedObject to only allow one loading of the requested object.
		synchronized(cachedObject) {
			if (cachedObject.getObject() == null || isStale(cachedObject)) {
				// Either the object could not be found or it should be reloaded
				loadObject(cachedObject, sourceStrategy, consumerStrategy);
			}
		}
		
		// Return the loaded object.
		return cachedObject.getObject();
	}
	
	/**
	 * Check if the given CachedObject should be reloaded.
	 * 
	 * @param cachedObject The cachedObject entry to check.
	 *  
	 * @return Whether the object needs to be reloaded.
	 */
	protected boolean isStale(CachedObject<T> cachedObject)
		throws Exception
	{
		if ( Lifecycle.getLifecycleContext().getConfigContext().isMonitored() ) {
		    // get an instance of the url util.
		    UrlUtil urlUtil = UrlUtil.getInstance();
		    
		    // get the url for the system id.
		    URL url = UrlFactory.getInstance().newUrl(cachedObject.getSystemId());
		    
			return urlUtil.lastModifiedAfter( url, cachedObject.getLastModified() ) 
			|| urlUtil.lastModifiedAfter( cachedObject.getDependencySet(), cachedObject.getLastModified() );
		} else {
			// Changes are not being monitored, always return false.
			return false;
		}
	}
	
	/**
	 * Load the object from the using the given source strategy and consumer strategy.
	 * 
	 * @param cachedObject The cached object representation to be loaded.
	 * @param sourceStrategy The strategy to determine the source of the object.
	 * @param consumerStrategy The strategy to transform the source data into a proper object.
	 */
	private void loadObject(CachedObject<T> cachedObject, SourceStrategy<S> sourceStrategy, ConsumerStrategy<T, S> consumerStrategy)
		throws Exception
	{
		DependencyTracker tracker = DependencyTracker.getInstance();
		long lastModified = System.currentTimeMillis();
		
	    // start tracking.
	    Set<URL> dependencySet = null;
	    T object = null;
	    tracker.startTracking();		
		
		try {
			object = consumerStrategy.consume(cachedObject.getSystemId(), sourceStrategy, tracker);
			
			if( log.isDebugEnabled() ) {
				log.debug("The catalog for system id '" + cachedObject.getSystemId() + "' is "+(object==null?"null":"not null")+".");
			}			
		} finally {
			dependencySet = tracker.stopTracking();
		}
		
		cachedObject.setObject(object);
		cachedObject.setDependencySet(dependencySet);
		cachedObject.setLastModified(lastModified);		
	}
	
	/**
	 * This keeps track of when the object was last modified, the system id of the object and the dependency set
	 * for the object.
	 *
	 * @param <OBJ> The type of object to cache.
	 */
	private class CachedObject<OBJ> {
		// Timestamp of when the object was last modified.
		private long lastModified = 0;
		// The system identifier for the object.
		private String systemId = null;
		// A set of dependencies for the object.
		private Set<URL> dependencySet = null;
		// The object itself.
		private OBJ object;
		
		public long getLastModified() {
			return lastModified;
		}
		
		public void setLastModified(long lastModified) {
			this.lastModified = lastModified;
		}
		
		public String getSystemId() {
			return systemId;
		}
		
		public void setSystemId(String systemId) {
			this.systemId = systemId;
		}
		
		public Set<URL> getDependencySet() {
			return dependencySet;
		}
		
		public void setDependencySet(Set<URL> dependencySet) {
			this.dependencySet = dependencySet;
		}
		
		public OBJ getObject() {
			return object;
		}
		
		public void setObject(OBJ object) {
			this.object = object;
		}
	}	
}
