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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * <p>Tests the protocol handler for file urls.</p>
 * @author Christian Trimble
 */
public class ScanNodeAssert
{
  public static void assertDirectoryNode( ScanNode node, String name )
    throws Exception
  {
    assertNotNull("Could not find the node for '"+name+"'.", node);
    assertEquals("The name for a directory node is wrong.", name, node.getName());
    assertTrue("The directory node '"+name+"' is not marked as a directory.", node.isDirectory());
    assertFalse("The directory node '"+name+"' is marked as a file.", node.isFile());
  }

  public static void assertFileNode( ScanNode node, String name )
    throws Exception
  {
    assertNotNull("Could not find the node for '"+name+"'.", node);
    assertEquals("The name for a file node is wrong.", name, node.getName());
    assertFalse("The file node '"+name+"' is marked as a directory.", node.isDirectory());
    assertTrue("The file node '"+name+"' is not marked as a file.", node.isFile());
  }
}
