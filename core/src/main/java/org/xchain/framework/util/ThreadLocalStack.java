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
package org.xchain.framework.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A stack that has a different state for each thread that uses it.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class ThreadLocalStack<E>
{
  protected ThreadLocal<LinkedList<E>> stackThreadLocal = new ThreadLocal<LinkedList<E>>();

  /**
   * Pushes an item onto the top of the stack.
   *
   * @param item the item to push onto the stack.
   */
  public void push(E item)
  {
    LinkedList<E> stack = stackThreadLocal.get();

    if( stack == null ) {
      stack = new LinkedList<E>();
      stackThreadLocal.set(stack);
    }

    stack.addFirst(item);
  }

  /**
   * Pops the top element off of the stack and returns it.
   *
   * @return the top element from the stack.
   * @throws NoSuchElementException if the stack is empty.
   */
  public E pop()
  {
    LinkedList<E> stack = stackThreadLocal.get();

    if( stack == null ) {
      throw new NoSuchElementException("pop() called on an empty stack.");
    }

    // if the stack is empty, remove the thread local.
    if( stack.size() == 1 ) {
      stackThreadLocal.set(null);
    }

    return stack.removeFirst();
  }

  /**
   * Peeks at the element on top of the stack.  This call is the same as peek(0).
   *
   * @return the element on the top of the stack.
   * @throws NoSuchElementException if the stack is empty.
   */
  public E peek()
  {
    return peek(0);
  }

  /**
   * Peeks at an element in the stack.
   *
   * @param depth the depth of the element to peek at.
   * @return the element at depth in the stack.
   * @throws NoSuchElementException if there is not an element at depth in the stack.
   */
  public E peek( int depth )
  {
    LinkedList<E> stack = stackThreadLocal.get();

    if( stack == null || stack.size() <= depth ) {
      throw new NoSuchElementException("peek() called on element that is not in the stack.");
    }

    return stack.get(depth);
  }

  /**
   * Returns the size of the stack.
   *
   * @return the size of the stack.
   */
  public int size()
  {
    LinkedList<E> stack = stackThreadLocal.get();

    if( stack == null ) {
      return 0;
    }

    return stack.size();
  }

  /**
   * Returns true if the stack for this thread is empty, false otherwise.
   */
  public boolean isEmpty()
  {
    return stackThreadLocal.get() == null;
  }

  /**
   * Clears the stack.
   */
  public void clear()
  {
    stackThreadLocal.set(null);
  }

  /**
   * Return a copy of the current threads stack as a list, with the top of the stack at index 0.
   */
  public List<E> toList()
  {
    if( stackThreadLocal.get() == null ) {
      return new ArrayList<E>();
    }
    else {
      return new ArrayList<E>(stackThreadLocal.get());
    }
  }
}
