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
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.net.JarURLConnection;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

/**
 * <p>This protocol scanner scans the protocol "jar:" and returns the root node of the scan.  The root node of the scan
 * can be used to discover other nodes in the jar.</p>
 *
 * @author Christian Trimble
 */
public class JarProtocolScanner
  implements ProtocolScanner
{
  /**
   * <p>Scans the entire jar at the specified url and adds all of the entries of that jar into the tree rooted at rootNode.  Missing
   * directory nodes will be added.</p>
   *
   * @param rootNode the root node of the scan.
   * @param jarUrl the url of the jar with entries to be added to the scan node.
   */
  public void scan( ScanNode rootNode, URL jarUrl )
    throws IOException
  {
    // NOTE: we are not closing this connection, because it was causing cached jars to be closed.  If this is causing problems with other
    // jars that are not on the classpath, we may need to use JarInputStream to do this work.

    // get the jar from the url.
    JarURLConnection conn = (JarURLConnection)jarUrl.openConnection();
    JarFile jarFile = conn.getJarFile();

    // for each of the enties, get a jar entry and add it to the tree.
    Enumeration<JarEntry> jarEntryEnum = jarFile.entries();
    while( jarEntryEnum.hasMoreElements() ) {
      JarEntry entry = jarEntryEnum.nextElement();
      insertEntryNodes( rootNode, entry );
    }
  }

  /**
   * <p>Adds an entry to the root node for a JarEntry.  This method is declared to be protected, so that test cases an interact with it.
   * In general, this method should be treaded as private.</p>
   *
   * @param rootNode the root node of the scan.
   * @param entry the jar entry to place under the root node.
   */
  void insertEntryNodes( ScanNode rootNode, JarEntry entry )
  {
    String name = entry.getName();
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
      currNode.setDirectory(entry.isDirectory()?true:false);
      currNode.setFile(entry.isDirectory()?false:true);
      parentNode.getChildMap().put(parts[parts.length-1], currNode);
    }
    else {
      if( entry.isDirectory() ) {
        currNode.setDirectory(true);
      }
      else {
        currNode.setFile(true);
      }
    }
  }
}
