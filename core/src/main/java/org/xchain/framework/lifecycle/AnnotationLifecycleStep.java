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
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.xchain.framework.jxpath.ScopedJXPathContextImpl;

/**
 * <p>This class represents a lifecycle step that is defined using annotations.</p>
 *
 * <p>The start method for this lifecycle step can be defined using setStartMethod(Method).  The start method should take at most one argument of type LifecycleContext.  If the
 * method throws an exception of a type other than LifecycleException, then the exception will be wrapped with a LifecycleException.</p>
 *
 * <p>The end method for this lifecycle step can be defined using setStopMethod(Method).  The stop method should take at most one argument of type LifecycleContext.  If the
 * stop method throws an exception of a type other than RuntimeException, then the exception will be wrapped with a RuntimeException.</p>
 *
 * <p>If either the start or stop method is an instance method, then a static method for accessing the lifecycle class must be defined.  This method will be called before
 * calling an instance start or stop method, to get access to the lifecycle instance.</p>
 *
 * @author Christian Trimble
 * @author John Trimble
 */
class AnnotationLifecycleStep
  implements LifecycleStep
{
  /** The start method for this lifecycle step.  This method can be null. */
  private Method startMethod;

  /** The stop method for this lifecycle step.  This method can be null. */
  private Method stopMethod;

  /** The method that returns the lifecycle singleton.  If the start method or the stop method is an instance method, then this method must not be null. */
  private Method lifecycleAccessor;
  
  /** The prefix bindings to set on the ConfigDocumentContext for the start method.  This map can be null. */
  private Map<String, String> prefixMappings;

  private QName qName;

  /**
   * <p>Constructs a new annotation lifecycle step that does not have a start method, a stop method, a lifecycle accessor, or a QName.</p>
   */
  AnnotationLifecycleStep()
  {

  }
  /**
   * <p>Constructs a new annotation lifecycle step that does not have a start method, a stop method, or a lifecycle accessor.</p>
   */
  AnnotationLifecycleStep(QName qName) {
    this();
    this.setQName(qName);
  }

  /**
   * <p>Sets the QName for this step.</p>
   */
  public void setQName(QName qName) { this.qName = qName; }
  /**
   * <p>Returns the QName for this step.</p>
   */
  public QName getQName() { return this.qName; }
  /**
   * <p>Sets the start method for this annotated lifecycle step.  The method set should take at most one argument of type LifecycleContext.</p>
   */
  public void setStartMethod( Method startMethod ) { this.startMethod = startMethod; }
  /**
   * <p>Returns the start method.</p>
   */
  public Method getStartMethod() { return this.startMethod; }
  /**
   * <p>Sets the start method for this annotated lifecycle step.  The method set should take at most one argument of type LifecycleContext.</p>
   */
  public void setStopMethod( Method stopMethod ) { this.stopMethod = stopMethod; }
  /**
   * <p>Returns the stop method for this annotated lifecycle step.</p>
   */
  public Method getStopMethod() { return this.stopMethod; }
  /**
   * <p>Sets the method for accessing the lifecycle class instance. LifecycleAccessor methods must be static and take zero arguments.  If either the start method or the stop methods is an
   * instance method, then this method must be defined.</p>
   */
  public void setLifecycleAccessor( Method lifecycleAccessor ) { this.lifecycleAccessor = lifecycleAccessor; }
  /**
   * <p>Returns the method for accessing the lifecycle class instance.</p>
   */
  public Method getLifecycleAccessor() { return this.lifecycleAccessor; }
  /**
   * <p>Sets the prefix mappings to use with the DocumentConfigContext for the start method. This should be a mapping of xml prefixes to valid namespaces.</p>
   */
  public void setStartMethodPrefixMappings(Map<String, String> prefixMappings) { this.prefixMappings = prefixMappings; }

  /**
   * <p>If the start method is defined, then this method will invoke the start method, otherwise this method does nothing.</p>
   * 
   * @param context The current LifecycleContext.
   * 
   * @throws LifecycleException If an exception is encountered starting this Lifecycle step.
   */
  public void startLifecycle(LifecycleContext context, ConfigDocumentContext configDocContext)
    throws LifecycleException
  {
    if( startMethod != null ) {
      Map<String, String> savedPrefixMappings = null;
      try {
        // map prefixes
        if( this.prefixMappings != null ) {
          savedPrefixMappings = new HashMap<String, String>();
          definePrefixMappings(configDocContext, prefixMappings, savedPrefixMappings);
        }
        invokeStepMethod(startMethod, lifecycleAccessor, context, configDocContext);
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
          throw new LifecycleException("The start method "+qName+" threw an unknown exception type.", ite);
        }
      }
      catch( Exception e ) {
        throw new LifecycleException("Could not execute lifecycle start step method "+qName+".", e);
      } finally {
        // unmap prefixes 
        if( this.prefixMappings != null ) {
          undefinePrefixMappings(configDocContext, prefixMappings, savedPrefixMappings);
        }
      }
    }
  }
  
  private static void definePrefixMappings(ScopedJXPathContextImpl context, Map<String,String> prefixMappings, Map<String,String> savedPrefixMappings) 
  {
    for( Map.Entry<String, String> entry : prefixMappings.entrySet() ) {
      String uri = context.getNamespaceURI(entry.getKey());
      // save old mapping
      if( uri != null )
        savedPrefixMappings.put(entry.getKey(), uri);
      // set prefix mapping
      context.registerNamespace(entry.getKey(), entry.getValue());
    }
  }
  
  private static void undefinePrefixMappings(ScopedJXPathContextImpl context, Map<String,String> prefixMappings, Map<String,String> savedPrefixMappings) {
    // unregister prefix mappings
    for( Map.Entry<String, String> entry : prefixMappings.entrySet() ) {
      context.registerNamespace(entry.getKey(), null);
    }
    // restore saved mappings
    for( Map.Entry<String, String> entry : savedPrefixMappings.entrySet() ) {
      context.registerNamespace(entry.getKey(), entry.getValue());
    }
  }

  /**
   * <p>If the end method is defined, then this method will invoke the start method, otherwise this method does nothing.</p>
   * 
   * @param context The current LifecycleContext.
   */
  public void stopLifecycle(LifecycleContext context)
  {
    try {
      if( stopMethod != null ) {
        invokeStepMethod(stopMethod, lifecycleAccessor, context, null);
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
        throw new RuntimeException("The stop method "+qName+" threw an unknown exception type.", ite);
      }
    }
    catch( RuntimeException re ) {
      throw re;
    }
    catch( Exception e ) {
      throw new RuntimeException("The lifecycle stop step "+qName+" threw an exception.", e);
    }
  }
  
  private static Object invokeStepMethod(Method stepMethod, Method lifecycleAccessor, LifecycleContext lifecycleContext, ConfigDocumentContext configContext) 
    throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, LifecycleException 
  {
    Object lifecycleObject = null;
    if( !Modifier.isStatic(stepMethod.getModifiers()) ) {
      if( lifecycleAccessor == null ) {
        throw new LifecycleException("Cannot execute step method '"+stepMethod.getName()+"' of class '"+stepMethod.getDeclaringClass().getName()+"' without an accessor method.");
      }
      lifecycleObject = lifecycleAccessor.invoke(null);

      if( lifecycleObject == null ) {
        throw new LifecycleException("The accessor method '"+lifecycleAccessor+"' of class '"+lifecycleAccessor.getDeclaringClass().getName()+"' returned a null lifecycle object.");
      }
    }
    
    Class<?>[] paramTypes = stepMethod.getParameterTypes();
    Object[] values = new Object[paramTypes.length];
    for( int i = 0; i < paramTypes.length; i++ ) {
      if( paramTypes[i].isInstance(lifecycleContext) )
        values[i] = lifecycleContext;
      else if( paramTypes[i].isInstance(configContext) ) 
        values[i] = configContext;
    }
    return stepMethod.invoke(lifecycleObject, values);
  }
}
