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

import static org.xchain.framework.scanner.ScanNodeAssert.assertDirectoryNode;
import static org.xchain.framework.scanner.ScanNodeAssert.assertFileNode;

import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <p>Tests the protocol handler for file urls.</p>
 * @author Christian Trimble
 */
public class TestFileProtocolScanner
{
  private static URL rootUrl;
  private static String FILE_RESOURCE = "org/xchain/framework/scanner/TestFileProtocolScanner.class";

  /**
   * <p>Finds the root url for the resource that is known to be under a file url root.</p>
   */
  @BeforeClass
  public static void findFileUrl()
    throws Exception
  {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL testResource = cl.getResource(FILE_RESOURCE);
    rootUrl = ScanUtil.computeResourceRoot(testResource, FILE_RESOURCE);
  }

  /**
   * <p>Tests the nodes created when adding a resource that is known to be under a file url.</p>
   */
  @Test
  public void testFileScanNode()
    throws Exception
  {
    FileProtocolScanner scanner = new FileProtocolScanner();
    ScanNode scanNode = new ScanNode();
    scanner.scan(scanNode, rootUrl);

    ScanNode currentNode = scanNode.getChildMap().get("org");
    assertDirectoryNode( currentNode, "org" );
    
    currentNode = currentNode.getChildMap().get("xchain");
    assertDirectoryNode( currentNode, "xchain" );
    
    currentNode = currentNode.getChildMap().get("framework");
    assertDirectoryNode( currentNode, "framework" );
    
    currentNode = currentNode.getChildMap().get("scanner");
    assertDirectoryNode( currentNode, "scanner" );
    
    currentNode = currentNode.getChildMap().get("TestFileProtocolScanner.class");
    assertFileNode( currentNode, "TestFileProtocolScanner.class" );
  }
}
