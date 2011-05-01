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
package org.xchain.impl;

import org.xchain.Command;
import org.xchain.Chain;
import org.xchain.Filter;
import org.apache.commons.jxpath.JXPathContext;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Almost an exact port of the class org.apache.commons.chain.impl.ChainBase, except the
 * nested chains are represented with a java.util.ArrayList instead of an array.
 *
 * @author Craig R. McClanahan
 * @author Christian Trimble
 */
public class ChainImpl
  implements Chain
{
  protected List<Command> commandList = new ArrayList<Command>();

  public boolean execute( JXPathContext context )
    throws Exception
  {
    // Verify our parameters
    if (context == null) {
      throw new IllegalArgumentException();
    }

    // Execute the commands in this list until one returns true
    // or throws an exception
    boolean saveResult = false;
    Exception saveException = null;
    ListIterator<Command> iterator = commandList.listIterator();
    while( iterator.hasNext() ) {
      try {
         saveResult = iterator.next().execute(context);
         if (saveResult) {
           break;
         }
      } catch (Exception e) {
         saveException = e;
         break;
      }
    }

    boolean handled = false;
    boolean result = false;
    while( iterator.hasPrevious() ) {
      Object previous = iterator.previous();
      if (previous instanceof Filter) {
         try {
           result = ((Filter) previous).postProcess(context, saveException);
           if (result) {
             handled = true;
           }
         }
         catch (Exception e) {
           // Silently ignore
         }
      }
    }

    // Return the exception or result state from the last execute()
    if ((saveException != null) && !handled) {
      throw saveException;
    }
    else {
      return (saveResult);
    }
  }

  public List<Command> getCommandList() {
    return commandList;
  }

  public void addCommand( Command command )
  {
    getCommandList().add(command);
  }
}
