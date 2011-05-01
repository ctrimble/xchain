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
package org.xchain.framework.lifecycle;

import java.util.List;
import java.util.ListIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.framework.osgi.OSGiCCLPolicy;

/**
 * This class manages the lifecycle of threads that execute xchains code.
 *
 * @author Christian Trimble
 * @author Josh Kennedy
 * @author John Trimble
 */
@LifecycleClass(uri="http://www.xchain.org/thread")
public class ThreadLifecycle
{
  /** The log for the thread lifecycle class. */
  private Logger log = LoggerFactory.getLogger(ThreadLifecycle.class);

  /** The static instance of this lifecycle class. */
  private static ThreadLifecycle instance = new ThreadLifecycle();

  /** Returns the ThreadLifecycle singleton. */
  @LifecycleAccessor
  public static ThreadLifecycle getInstance() { return instance; }

  /** The thread local that holds context objects. */
  private ThreadLocal<ThreadContext> contextTl = new ThreadLocal<ThreadContext>();

  /** The list of thread steps that are called before a thread executes. */
  private List<ThreadStep> threadStepList = null;

  /** Strategy for setting the context class loader. */
  private CCLPolicy cclPolicy = new NOPCCLPolicy();

  /**
   * This private constructor forces users of this class to access it through the singleton instance.
   */
  private ThreadLifecycle() {

  }

  public boolean inThread()
  {
    return this.contextTl.get() != null;
  }

  /**
   * Returns the current thread context.
   */
  public ThreadContext getThreadContext()
  {
    return this.contextTl.get();
  }
  
  /**
   * Returns the strategy used for setting the context class loader. By default, the context class loader is not altered.
   * @return
   */
  public CCLPolicy getCCLPolicy() {
    return this.cclPolicy;
  }
  
  public void setCCLPolicy(CCLPolicy cclPolicy) {
    this.cclPolicy = cclPolicy;
  }

  /**
   * This mehtod must be called by a thread before interacting with xchains.
   * @throws LifecycleException of the thread lifecycle could not be started.  All thread related resources will be cleaned up
   *         before this exception is thrown.  If this exception is thrown, stopThreadLifecycle should not be called.
   */
  public void startThread(ThreadContext threadContext)
    throws LifecycleException
  {
    this.getCCLPolicy().bindCCL();

    // set the current context for the thread.
    contextTl.set(threadContext);

    // create the iterator for the steps.
    ListIterator<ThreadStep> iterator = threadStepList.listIterator();

    // iterate the steps.
    try {
      while( iterator.hasNext() ) {
        ThreadStep step = iterator.next();
        step.startThread(threadContext);
      }
    }
    catch( Throwable t ) {
      if( log.isDebugEnabled() ) {
        log.debug("An exception was thrown while starting a thread context.", t);
      }
      iterator.previous();
      while( iterator.hasPrevious() ) {
        ThreadStep stopStep = iterator.previous();
        try {
          stopStep.stopThread(threadContext);
        }
        catch( Throwable t2 ) {
          if( log.isWarnEnabled() ) {
            log.warn("An error was thrown while cleaning up another exception in the ThreadLifecycle.", t2);
          }
        }
      }

      contextTl.remove();

      this.getCCLPolicy().unbindCCL();

      if( t instanceof LifecycleException ) {
        throw (LifecycleException)t;
      }
      else {
        throw new LifecycleException("An exception was thrown while starting a thread context.", t);
      }
    }
  }

  /**
   * This method must be called by a thread after interacting with xchains.
   */
  public void stopThread(ThreadContext threadContext)
    throws LifecycleException
  {
    // iterate through the steps is reverse order, passing the thread context into each step.
    ListIterator<ThreadStep> iterator = threadStepList.listIterator(threadStepList.size());
    while( iterator.hasPrevious() ) {
      ThreadStep stopStep = iterator.previous();
      try {
        stopStep.stopThread(threadContext);
      }
      catch( Throwable t ) {
        if( log.isWarnEnabled() ) {
          log.warn("An error was thrown while stopping a thread context.", t);
        }
      }
    }

    // remove the context.
    contextTl.remove();

    this.getCCLPolicy().unbindCCL();
  }

  @StartStep(localName="scan")
  public void startLifecycle( LifecycleContext lifecycleContext )
    throws LifecycleException
  {
    ThreadStepScanner scanner = new ThreadStepScanner( lifecycleContext );
    try {
      scanner.scan();
    }
    catch( Exception e ) {
      if( log.isErrorEnabled() ) {
        log.error("An exception was thrown while while scanning for thread step methods.", e);
      }
      throw new LifecycleException("An exception was thrown while scanning for thread step methods.", e);
    }
    threadStepList = scanner.getThreadStepList();

    if( log.isInfoEnabled() ) {
      StringBuilder message = new StringBuilder();
      message.append("Found "+threadStepList.size()+" thread lifecycle steps.\n");
      for( ThreadStep step : threadStepList ) {
        message.append("  ").append(step.getQName().toString()).append("\n");
      }
      log.info(message.toString());
    }
  }

  @StopStep(localName="scan")
  public void stopLifecycle( LifecycleContext lifecycleContext )
  {
    threadStepList = null;
  }
}
