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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.xchain.framework.osgi.ManifestParser;
import org.xchain.framework.osgi.ParsedClassPathEntry;

import static java.lang.String.format;

/**
 * @author Christian Trimble
 * @author John Trimble
 */
public class BundleProtocolScanner
  implements ProtocolScanner
{
  private static Logger log = LoggerFactory.getLogger(BundleProtocolScanner.class);
  private static BundleContext context = null;

  public static void setBundleContext( BundleContext context )
  {
    BundleProtocolScanner.context = context;
  }

  /**
   * <p>Scans the entries in a Weblogic Zip URL and adds all of the entries of that jar into the tree rooted at rootNode.  Missing
   * directory nodes will be added.</p>
   *
   * @param rootNode the root node of the scan.
   * @param jarUrl the url of the jar with entries to be added to the scan node.
   */
  public void scan( ScanNode rootNode, URL bundleUrl )
    throws IOException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Bundle url "+bundleUrl.toExternalForm());
    }

    // get the bundle for this class.  This is used to get other bundles.
    Bundle scannerBundle = FrameworkUtil.getBundle(BundleProtocolScanner.class);
    BundleContext scannerBundleContext = scannerBundle.getBundleContext();

    // get the class path entry for this bundle.
    String classPathHeader = (String)scannerBundle.getHeaders().get("Bundle-ClassPath");

    // parse the header.
    List<ParsedClassPathEntry> classPathEntryList = null;
    try {
      classPathEntryList = ManifestParser.parseClassPathEntries( classPathHeader );
    }
    catch( Exception e ) {
      throw new IOException("Could not scan "+bundleUrl);
    }

    for( ParsedClassPathEntry entry : classPathEntryList ) {
      for( String target : entry.getTargetList() ) {

        if( target.endsWith(".jar") ) {
          scanJar( rootNode, target, scannerBundle );
        }
        else {
          scanDirectory( rootNode, target, scannerBundle );
        }
      }
    }
  }

  public void scanJar( ScanNode scanNode, String path, Bundle bundle )
    throws IOException
  {
    URL jarUrl = bundle.getEntry(path);
    JarInputStream in = null;
    try {
      in = new JarInputStream(jarUrl.openStream());
      JarEntry entry = null;
      while( (entry = in.getNextJarEntry()) != null ) {
        String resourceName = entry.getName();
        if( entry.isDirectory() && !resourceName.endsWith("/") )
          resourceName = resourceName + "/";
        insertEntryNodes( scanNode, resourceName );
      }
    }
    catch( Exception e ) {
      e.printStackTrace();
      log.warn(format("Error occurred while processing JAR '%s'.", path), e);
    }

  }

  /**
   * <p>Adds an entry to the root node for a JarEntry.  This method is declared to be protected, so that test cases an interact with it.
   * In general, this method should be treaded as private.</p>
   *
   * @param rootNode the root node of the scan.
   * @param entry the jar entry to place under the root node.
   */
  void insertEntryNodes( ScanNode rootNode, String name )
  {
    // TODO: verify that this works on windows.
    String[] parts = name.split("\\/");
    int depth = 0;
    ScanNode parentNode = rootNode;

    // create any missing directory nodes.
    for( int i = 0; i < parts.length - 1; i++ ) {
      ScanNode currNode = parentNode.getChildMap().get(parts[i]);
      if( currNode == null ) {
        currNode = new ScanNode();
        currNode.setResourceName("".equals(parentNode.getResourceName())?parts[i]:parentNode.getResourceName()+"/"+parts[i]);
        currNode.setName(parts[i]);
        currNode.setDirectory(true);
        currNode.setFile(false);
        parentNode.getChildMap().put(parts[i], currNode);
      }
      else {
        currNode.setDirectory(true);
      }
      parentNode = currNode;
    }

    // ASSERT: parentNode is now the parent node of this entry.

    // create the entry node if it is not already in the scan tree.
    ScanNode currNode = parentNode.getChildMap().get(parts[parts.length-1]);
    if( currNode == null ) {
      currNode = new ScanNode();
      currNode.setResourceName("".equals(parentNode.getResourceName())?parts[parts.length-1]:parentNode.getResourceName()+"/"+parts[parts.length-1]);
      currNode.setName(parts[parts.length-1]);
      currNode.setDirectory(isDirectory(name)?true:false);
      currNode.setFile(isDirectory(name)?false:true);
      parentNode.getChildMap().put(parts[parts.length-1], currNode);
    }
    else {
      if( isDirectory(name) ) {
        currNode.setDirectory(true);
      }
      else {
        currNode.setFile(true);
      }
    }
  }
  
  private boolean isDirectory(String path) 
  {
    return path.endsWith("/");
  }
  
  public void scanDirectory( ScanNode scanNode, String path, Bundle bundle )
    throws IOException
  {
    // The following excerpt from section 3.8.6 of the OSGi 4.2 Core Specification relates to bundle entry URLs:
    //
    //   The getPath method for a bundle entry URL must return an absolute path (a path that starts with '/') to a 
    //   resource or entry in a bundle. For example, the URL returned from getEntry("myimages/test.gif") must have a 
    //   path of /myimages/test.gif.
    
    // normalize the path
    try {
      path = new URI("/" + path + "/").normalize().getPath();
    } catch( URISyntaxException e ) { 
      throw new IllegalArgumentException(String.format("Given path '%s' has an invalid syntax.", path), e);
    }
    
    if( ".".equals(path) ) { path = ""; }
    Enumeration<URL> pathEnum = (Enumeration<URL>)bundle.findEntries(path, "*", true);
    // make sure we have a normalized path.
    if( pathEnum != null ) {
      while( pathEnum.hasMoreElements() ) {
        URL entry = pathEnum.nextElement();
        if( entry.getPath().startsWith(path) ) {
          String resourceName = entry.getPath().substring(path.length());
          insertEntryNodes(scanNode, resourceName);
        }
      }
    } 
  }
}
