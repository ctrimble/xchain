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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.xml.namespace.QName;

/**
 * The implementation of ThreadStep for annotated methods.
 *
 * @author Christian Trimble
 */
class AnnotationThreadStep
  implements ThreadStep
{
  /** The method to be called before a thread accesses XChains. */
  private Method startMethod;

  /** The method to be called after a thread acceses XChains. */
  private Method stopMethod;

  /**
   * The static method used to get an instance to call the start and stop methods on.  This is required if the start method,
   * or the stop method is an instance level method.
   */
  private Method lifecycleAccessor;

  /** The unique name of this step. */
  private QName qName;

  /**
   * Created a new AnnotationThreadStep.
   */
  AnnotationThreadStep()
  {

  }

  /**
   * Creates a new AnnotationThreadStep with the specified name.
   */
  AnnotationThreadStep(QName qName) {
    this();
    this.setQName(qName);
  }

  /**
   * Sets the unique name for this step.
   */
  public void setQName(QName qName) { this.qName = qName; }

  /**
   * Returns the unique name for this step.
   */
  public QName getQName() { return this.qName; }

  /**
   * Sets the method that is called when a thread is started.
   */
  public void setStartMethod( Method startMethod ) { this.startMethod = startMethod; }

  /**
   * Returns the method that is called when a thread is started.
   */
  public Method getStartMethod() { return this.startMethod; }

  /**
   * Sets the method that is called when a thread is stopped.
   */
  public void setStopMethod( Method stopMethod ) { this.stopMethod = stopMethod; }

  /**
   * Returns the method that is called when a thread is stopped.
   */
  public Method getStopMethod() { return this.stopMethod; }

  /**
   * Sets the static method that is used to find the target of non-static start and stop methods.
   */
  public void setLifecycleAccessor( Method lifecycleAccessor ) { this.lifecycleAccessor = lifecycleAccessor; }

  /**
   * Returns the static method that is used to find the target of non-static start and stop methods.
   */
  public Method getLifecycleAccessor() { return this.lifecycleAccessor; }

  /**
   * Calls the start method for this step, if it is defined.
   */
  public void startThread(ThreadContext context)
    throws LifecycleException
  {
    if( startMethod != null ) {
      try {
        invokeStepMethod(startMethod, lifecycleAccessor, context);
      }
      catch( InvocationTargetException ite ) {
        if( ite.getCause() instanceof LifecycleException ) {
          throw (LifecycleException)ite.getCause();
        }
        else if( ite.getCause() instanceof RuntimeException ) {
          throw (RuntimeException)ite.getCause();
        }
        else if( ite.getCause() instanceof Error ) {
          throw (Error)ite.getCause();
        }
        else {
          throw new LifecycleException("A start method threw an unknown exception type.", ite);
        }
      }
      catch( Exception e ) {
        throw new LifecycleException("Could not execute lifecycle start step method.", e);
      }
    }
  }
  
  /**
   * Calls the stop method for this step, if it is defined.
   */
  public void stopThread(ThreadContext context)
  {
    try {
      if( stopMethod != null ) {
        invokeStepMethod(stopMethod, lifecycleAccessor, context);
      }
    }
    catch( InvocationTargetException ite ) {
      if( ite.getCause() instanceof RuntimeException ) {
        throw (RuntimeException)ite.getCause();
      }
      else if( ite.getCause() instanceof Error ) {
        throw (Error)ite.getCause();
      }
      else {
        throw new RuntimeException("A start thread method threw an unknown exception type.", ite);
      }
    }
    catch( RuntimeException re ) {
      throw re;
    }
    catch( Exception e ) {
      throw new RuntimeException("A thread stop step threw an exception.", e);
    }
  }

  /**
   * Invokes a start or stop method for the specified thread context.
   */
  private static Object invokeStepMethod(Method stepMethod, Method lifecycleAccessor, ThreadContext threadContext) 
    throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, LifecycleException 
  {
    // decide what object we will be invoking the method on.
    Object lifecycleObject = null;
    if( !Modifier.isStatic(stepMethod.getModifiers()) ) {
      if( lifecycleAccessor == null ) {
        throw new LifecycleException(
          "Cannot execute step method '"+stepMethod.getName()+"' of class '"+stepMethod.getDeclaringClass().getName()+"' without an accessor method.");
      }
      lifecycleObject = lifecycleAccessor.invoke(null);
    }

    // create the parameters that we will pass to the object.
    Class<?>[] paramTypes = stepMethod.getParameterTypes();
    Object[] values = new Object[paramTypes.length];
    for( int i = 0; i < paramTypes.length; i++ ) {
      if( paramTypes[i].isInstance(threadContext) ) {
        values[i] = threadContext;
      }
    }

    // invoke the method and return it's value.
    return stepMethod.invoke(lifecycleObject, values);
  }
}
