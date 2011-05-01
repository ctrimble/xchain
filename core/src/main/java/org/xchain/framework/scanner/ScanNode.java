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
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * A scan node represents a file or directory in the class path.  All protocol scanners build scan nodes of this type and 
 *
 * @author Christian Trimble
 */
public class ScanNode
{
  /** The resource name of the node. */
  private String resourceName = "";

  /** The name of this node relative to its parent. */
  private String name = "";

  /** The map of the children of this node. */
  private LinkedHashMap<String, ScanNode> childMap = new LinkedHashMap<String, ScanNode>();

  /** The parent of this node.  This is null for the root node. */
  private ScanNode parentNode = null;

  /** Indicates if this node is a directory. */
  private boolean isDirectory = false;

  /** Indicates if this node is a file. */
  private boolean isFile = false;

  /**
   * <p>Constructs a new scan node that needs to be configured.</p>
   */
  public ScanNode()
  {

  }

  /**
   * <p>Returns the resource name for this node.  This is the fully qualified resource name, ready to be passed into the ClassLoader to get a URL for this node.</p>
   */
  public String getResourceName()
  {
    return resourceName;
  }

  /**
   * <p>Sets the resource name for this node.  This is the fully qualified resource name, ready to be passed into the ClassLoader to get a URL for this node.</p>
   */
  public void setResourceName( String resourceName )
  {
    this.resourceName = resourceName;
  }

  /**
   * <p>Returns the name of this node relative to the parent.  This string does not contain any path seperators.</p>
   */
  public void setName( String name )
  {
    this.name = name;
  }

  /**
   * <p>Sets the name of this node relative to the parent.  This string does not contain any path seperators.</p>
   */
  public String getName()
  {
    return name;
  }

  /**
   * <p>Returns the child nodes for this node.</p>
   */
  public Map<String, ScanNode> getChildMap()
  {
    return this.childMap;
  }

  /**
   * Returns the parent node for this node.  If this ScanNode is the root node for the class loader, then this method returns null.
   */
  public ScanNode getParentNode()
  {
    return  this.parentNode;
  }

  /**
   * Returns true if this node represents a directory.  Since URLs can behave both as directories and files, this method returning true does not imply that isFile() will
   * return false.
   */
  public boolean isDirectory()
  {
    return this.isDirectory;
  }

  /**
   * <p>Sets the isDirectory flag.</p>
   */
  public void setDirectory( boolean isDirectory )
  {
    this.isDirectory = isDirectory;
  }

  /**
   * <p>Returns true if this node represents a file.  Since URLs can behave both as directories and files, this method returning true does not imply that isDirectory() will
   * return false.</p>
   */
  public boolean isFile()
  {
    return this.isFile;
  }

  /**
   * <p>Sets the isFile flag.</p>
   */
  public void setFile( boolean isFile )
  {
    this.isFile = isFile;
  }
}
