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

import java.net.URL;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.plugins.vfs.VirtualFileURLConnection;

/**
 * <p>A protocol handler for jboss 5.0.0 urls with protocols "vfszip", "vfsfile", and "vfsmemory".  This protocol handler interfaces with the VirtualFile objects exposed by
 * jboss to build a tree of nodes to scan.</p>
 *
 * @author Christian Trimble
 */
public class VfsProtocolScanner
  implements ProtocolScanner
{
  /**
   * <p>Scans the VFS URL and adds the nodes in that url to the root node.</p>
   *
   * @param rootNode the root node of the resource tree.
   * @param vfsUrl the root URL to scan.  The openConnection() method of this URL must return a connection of type VirtualFileURLConnection.
   */
  public void scan( ScanNode rootNode, URL vfsUrl )
    throws Exception
  {
    // get the virtual file from the connection.
    VirtualFileURLConnection vfConn = (VirtualFileURLConnection)vfsUrl.openConnection();
    VirtualFile vFile = vfConn.getContent();

    // ASSERT: We now have the virtual file object.  We just need to recurse over it.

    for( VirtualFile child : vFile.getChildren() ) {
      insertDirectoryNodes( rootNode, child );
    }
  }

  /**
   * <p>Adds scan nodes to the parent node for the specified virtual file and all of its children.</p>
   *
   * @param parentNode the parent node of the 
   */
  void insertDirectoryNodes( ScanNode parentNode, VirtualFile vFile )
    throws Exception
  {
    ScanNode scanNode = parentNode.getChildMap().get(vFile.getName());
    if( scanNode == null ) {
      scanNode = new ScanNode();
      scanNode.setName(vFile.getName());
      scanNode.setResourceName("".equals(parentNode.getResourceName())?vFile.getName():parentNode.getResourceName()+"/"+vFile.getName());
      scanNode.setDirectory(!vFile.isLeaf());
      scanNode.setFile(vFile.isLeaf());
      parentNode.getChildMap().put(vFile.getName(), scanNode);
    }

    if( !vFile.isLeaf() ) {
      for( VirtualFile child : vFile.getChildren() ) {
        insertDirectoryNodes( scanNode, child );
      }
    }
  }
}
