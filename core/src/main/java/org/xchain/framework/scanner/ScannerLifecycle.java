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
package org.xchain.framework.scanner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.net.URL;

import org.xchain.framework.lifecycle.LifecycleClass;
import org.xchain.framework.lifecycle.LifecycleAccessor;
import org.apache.commons.collections.map.LRUMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The lifecycle scanner provides utilities for scanning the class loader of an xcahins application.
 *
 * @author Christian Trimble
 * @author Josh Kennedy
 * @author John Trimble
 */
@LifecycleClass(uri="http://www.xchain.org/scanner")
public class ScannerLifecycle
{
  /*
   * Flag to turn on/off memoization of ScannerLifecycle.scanNode().
   */
  private static final boolean MEMOIZE_SCAN_NODE = true;
  private static Logger log = LoggerFactory.getLogger(ScannerLifecycle.class);

  private static ScannerLifecycle instance = new ScannerLifecycle();
  private Map<ClassLoader, Map<RootUrlLocator, ScanNode>> cache;
  

  @LifecycleAccessor
  public static ScannerLifecycle getInstance()
  {
    return instance;
  }

  private Map<String, ProtocolScanner> protocolMap = new HashMap<String, ProtocolScanner>();

  public ScannerLifecycle()
  {
    protocolMap.put("file", new FileProtocolScanner());
    protocolMap.put("jar", new JarProtocolScanner());

    // define the vfszip, vfsfile, and vfsmemory protocols if the jboss virtual file system classes are on the classpath.
    if( isVfsDefined() ) {
      protocolMap.put("vfszip", new VfsProtocolScanner());
      protocolMap.put("vfsfile", new VfsProtocolScanner());
      protocolMap.put("vfsmemory", new VfsProtocolScanner());
    }
    if( isZipDefined() ) {
      protocolMap.put("zip", new ZipProtocolScanner());
    }
    if( isBundleDefined() ) {
      protocolMap.put("bundle", new BundleProtocolScanner());
      protocolMap.put("bundleresource", new BundleProtocolScanner());
    }
    
    if( MEMOIZE_SCAN_NODE ) {
      cache = Collections.synchronizedMap(new WeakHashMap<ClassLoader, Map<RootUrlLocator,ScanNode>>());
    }
  }

  /**
   * Returns the root scan node.  Using this node, all of the nodes on the
   * classpath can be visited.
   */
  public ScanNode scanNode()
    throws Exception
  {
    return scanNode( Thread.currentThread().getContextClassLoader(), new MarkerResourceLocator("META-INF/xchain.xml") );
  }

  /**
   * <p>Scans the root urls found by the locator and returns the root scan node for those roots.</p>
   */
  public ScanNode scanNode( RootUrlLocator locator )
    throws Exception
  {
    return scanNode( Thread.currentThread().getContextClassLoader(), locator );
  }

  public ScanNode scanNode( ClassLoader classLoader, RootUrlLocator locator )
    throws Exception
  {
    if( MEMOIZE_SCAN_NODE ) {
      ScanNode cachedScanNode = getCached(classLoader, locator);
      if( cachedScanNode != null ) {
        log.debug("Using cached ScanNode instance '{}' for ClassLoader '{}' and RootUrlLocator '{}'.", new Object[] {cachedScanNode, classLoader, locator});
        return cachedScanNode;
      }
    }
    
    ScanNode rootScanNode = new ScanNode();
    Set<URL> roots = locator.findRoots(Thread.currentThread().getContextClassLoader());

    for( URL root : roots ) {
      // find the protocol scanner for this root url.
      String protocol = root.getProtocol();
      ProtocolScanner scanner = protocolMap.get(protocol);
      if( scanner == null ) {
        if( log.isDebugEnabled() ) {
          log.debug("Could not scan protocol "+protocol+" of url "+root+", because a scanner for this protocol is not defined.");
        }
        continue;
      }
      scanner.scan(rootScanNode, root);
    }
    
    if( MEMOIZE_SCAN_NODE ) {
      putCached(classLoader, locator, rootScanNode);
    }
    return rootScanNode;
  }
  
  /**
   * Clears internal cache of ScanNode instances.
   */
  public void clearCache() {
    if( MEMOIZE_SCAN_NODE )
      this.cache.clear();
  }
  
  /*
   * Returns the cached ScanNode instance mapped by the (classLoader, locator) tuple.
   */
  private ScanNode getCached( ClassLoader classLoader, RootUrlLocator locator ) {
    Map<RootUrlLocator, ScanNode> locatorScanNodeMap = cache.get(classLoader);
    if( locatorScanNodeMap != null )
      return locatorScanNodeMap.get(locator);
    return null;
  }
  
  /*
   * Maps a (ClassLoader,RootUrlLocator) tuple to a ScanNode instance to cache.
   */
  private void putCached( ClassLoader classLoader, RootUrlLocator locator, ScanNode scanNode ) {
    Map<RootUrlLocator, ScanNode> locatorScanNodeMap = this.cache.get(classLoader);
    if( locatorScanNodeMap == null ) {
      locatorScanNodeMap = Collections.synchronizedMap(new LRUMap(20));
      this.cache.put(classLoader, locatorScanNodeMap);
    }
    locatorScanNodeMap.put(locator, scanNode);
  }

  private boolean isVfsDefined() {
    try {
      Thread.currentThread().getContextClassLoader().loadClass("org.jboss.virtual.VirtualFile");
      // ASSERT: the virtual file class loaded, so we are on jboss 5.
      return true;
    }
    catch( Exception e ) {
      return false;
    }
  }

  private boolean isZipDefined() {
    try {
      Thread.currentThread().getContextClassLoader().loadClass("weblogic.utils.zip.ZipURLConnection");
      return true;
    }
    catch( Exception e ) {
      return false;
    }
  }

  private boolean isBundleDefined() {
    try {
      Thread.currentThread().getContextClassLoader().loadClass("org.osgi.framework.Bundle");
      return true;
    }
    catch( Exception e ) {
      return false;
    }
  }
}
