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
import java.io.File;
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
public class FileProtocolScanner
  implements ProtocolScanner
{
  public FileProtocolScanner()
  {
  }

  /**
   * <p>Scans the entire directory at the specified url and adds all of the entries of that jar into the tree rooted at rootNode.  Missing
   * directory nodes will be added.</p>
   *
   * @param rootNode the root node of the scan.
   * @param jarUrl the url of the jar with entries to be added to the scan node.
   */
  public void scan( ScanNode rootNode, URL fileUrl )
    throws Exception
  {
    // get the file object for the url object.
    File rootDir = new File(fileUrl.toURI());

    // ASSERT: we now have a file handle for the file url.

    // add the children of this file to the root node.
    for( File child : rootDir.listFiles() ) {
      insertDirectoryNodes( rootNode, child );
    }
  }

  void insertDirectoryNodes( ScanNode parentNode, File file )
  {
    // create a node for this file, if it is missing.
    ScanNode scanNode = parentNode.getChildMap().get(file.getName());
    if( scanNode == null ) {
      scanNode = new ScanNode();
      scanNode.setResourceName("".equals(parentNode.getResourceName())?file.getName():parentNode.getResourceName()+"/"+file.getName());
      scanNode.setName(file.getName());
      scanNode.setDirectory(file.isDirectory());
      scanNode.setFile(file.isFile());
      parentNode.getChildMap().put(file.getName(), scanNode);
    }

    if( file.isDirectory() ) {
      for( File child : file.listFiles() ) {
        insertDirectoryNodes( scanNode, child );
      }
    }
  }
}
