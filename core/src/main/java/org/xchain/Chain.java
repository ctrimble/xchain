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

import java.util.List;

/**
 * The interface for JXPathContext chains.
 *
 * @author Christian Trimble
 */
public interface Chain
  extends Command
{
  /**
   * Adds a command to the end of this chain.
   *
   * @param command the command to add.
   */
  public void addCommand( Command command );

  /**
   * Returns the list of commands that make up this chain.
   *
   * @return the list of commands in this chain.
   */
  public List<Command> getCommandList();
}
