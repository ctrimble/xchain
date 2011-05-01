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
package org.xchain.namespaces.core;

import org.xchain.Catalog;
import org.xchain.Command;
import org.xchain.CommandNotFoundException;
import org.xchain.annotations.Element;
import javax.xml.namespace.QName;
import javax.xml.XMLConstants;

import org.xchain.framework.util.CompositeMap;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import java.util.Map;
import java.util.HashMap;

/**
 * The default implementation of Catalog for the XChains package.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 */
@Element(localName="catalog")
public class XChainCatalog
  implements Catalog
{
  /** The map of commands in this catalog. */
  protected Map<QName, Command> commandMap = new HashMap<QName, Command>();

  /** A list of all command maps attached to this catalog through import.  The command maps are sorted by precedent. */
  protected LinkedList<Map<QName, Command>> commandMapList = new LinkedList<Map<QName, Command>>();

  public XChainCatalog()
  {
    commandMapList.add(commandMap);
  }

  /**
   * A composite map that will look values up from the command map list.
   */
  protected CompositeMap<QName, Command> compositeCommandMap = new CompositeMap<QName, Command>()
  {
    public List<Map<QName, Command>> mapList() { return commandMapList; }
  };

  /**
   * Gets the command for the name specified in the default namespace, or null if the command was not found.
   */
  public Command getCommand( String name )
    throws CommandNotFoundException
  {
    return getCommand(new QName(XMLConstants.NULL_NS_URI, name));
  }

  /**
   * Gets the command for the QName specified, or null if the command was not found.
   */
  public Command getCommand( QName name )
    throws CommandNotFoundException
  {
    Command command = compositeCommandMap.get(name);
    
    if (command == null)
      throw new CommandNotFoundException("Could not find command '" + name + "'");
    
    return command;    
  }

  /**
   * Adds a command to this catalog in the default namespace.
   */
  public void addCommand( String name, Command command )
  {
    commandMap.put(new QName(XMLConstants.NULL_NS_URI, name), command);
  }

  public void addCommand( QName qName, Command command )
  {
    commandMap.put(qName, command);
  }

  public Map<QName, Command> getCommandMap()
  {
    return compositeCommandMap;
  }

  public void addImport( Catalog catalog )
  {
    commandMapList.add(Collections.unmodifiableMap(catalog.getCommandMap()));
  }
}
