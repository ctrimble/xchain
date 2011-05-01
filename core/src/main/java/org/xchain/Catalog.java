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
package org.xchain;

import java.util.Map;
import javax.xml.namespace.QName;

/**
 * The interface for JXPathContext catalogs.  A catalog is a collection of commands, bound to qNames.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 */
public interface Catalog
{
  /**
   * Adds a command to this catalog in the default namespace.
   * 
   * @param localName The name of the command.
   * 
   * @param command The command to add.
   */
  public void addCommand( String localName, Command command );

  /**
   * Adds a command to this catalog with the specified QName.
   * 
   * @param name The name of the command.
   * 
   * @param command The command to add.
   */  
  public void addCommand( QName name, Command command );

  /**
   * Gets a command from this catalog in the default namespace.
   * 
   * @param localName The name of the command.
   * 
   * @return The command for the given name.
   * @throws CommandNotFoundException If no command for the given name could be found.
   */
  public Command getCommand( String localName ) throws CommandNotFoundException;

  /**
   * Gets a command from this catalog with the specified QName.
   * 
   * @param name The name of the command.
   * 
   * @return The command for the given name.
   * @throws CommandNotFoundException If no command for the given name could be found.
   */  
  public Command getCommand( QName name ) throws CommandNotFoundException;

  /**
   * Returns a map of all the commands defined in this catalog.
   */
  public Map<QName, Command> getCommandMap();
}
