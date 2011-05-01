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
import org.xchain.Filter;
import org.apache.commons.jxpath.JXPathContext;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.LinkedList;

/**
 * Almost an exact port of the class org.apache.commons.chain.impl.ChainBase, except the
 * nested chains are represented with a java.util.ArrayList instead of an array.
 *
 * @author Craig R. McClanahan
 * @author Christian Trimble
 */
public class FilterList
  extends ArrayList<Command>
  implements Filter, Command
{
  public static ThreadLocal<LinkedList<ListIterator<Command>>> iteratorStackThreadLocal = new ThreadLocal<LinkedList<ListIterator<Command>>>();

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
    boolean handled = false;
    Exception saveException = null;
    ListIterator<Command> iterator = listIterator();
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

    pushIterator(iterator);

    if( saveException != null ) {
      throw saveException;
    }

    return saveResult;
  }

  public boolean postProcess( JXPathContext context, Exception exception )
  {
    ListIterator<Command> iterator = popIterator();

    boolean handled = false;
    boolean result = false;
    while( iterator.hasPrevious() ) {
      Command previous = iterator.previous();
      if (previous instanceof Filter) {
         try {
           result = ((Filter) previous).postProcess(context, exception);
           if (result) {
             handled = true;
           }
         }
         catch (Exception e) {
           // Silently ignore
         }
      }
    }
    return handled;
  }

  protected LinkedList<ListIterator<Command>> getIteratorStack()
  {
    LinkedList<ListIterator<Command>> iteratorStack = iteratorStackThreadLocal.get();

    if( iteratorStack == null ) {
      iteratorStack = new LinkedList<ListIterator<Command>>();
      iteratorStackThreadLocal.set(iteratorStack);
    }

    return iteratorStack;
  }

  protected void pushIterator( ListIterator<Command> iterator )
  {
    getIteratorStack().addFirst(iterator);
  }

  protected ListIterator<Command> popIterator()
  {
    return getIteratorStack().removeFirst();
  }

}
