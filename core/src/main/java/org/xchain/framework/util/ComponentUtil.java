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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.xchain.annotations.Begin;
import org.xchain.annotations.Component;
import org.xchain.annotations.End;
import org.xchain.annotations.In;
import org.xchain.annotations.PrefixMapping;
import org.xchain.framework.jxpath.ScopedJXPathContextImpl;
import org.xchain.framework.lifecycle.ComponentAnalysis;
import org.xchain.framework.lifecycle.ComponentAnalysis.InjectionAnalysis;

/**
 * Utility class for Components.
 *
 * @author Devon Tackett
 * @author John Trimble
 */
public class ComponentUtil {
  
  /**
   * Perform an analysis of a component class.
   * 
   * @param componentClass The component class to analyze.
   * 
   * @return An analysis of the component class.
   */
  public static ComponentAnalysis createAnalysis(Class componentClass) {
    ComponentAnalysis analysis = new ComponentAnalysis(componentClass);
    
    // Get the component annotation
    Component componentAnnotation = (Component)componentClass.getAnnotation(Component.class);
    
    // Get the localname and scope.
    analysis.setLocalName(componentAnnotation.localName());
    analysis.setScope(componentAnnotation.scope());
    
    // Get the method based dependency injections.  This will only work with public methods on the class.
    for (Method method : componentClass.getMethods()) {
      In inAnnotation = method.getAnnotation(In.class);
      if (inAnnotation != null) {
        analysis.addInjection(new InjectionAnalysis(inAnnotation.select(), inAnnotation.prefixMappings(), inAnnotation.nullable(), method));
      }
    }
    
    // Build field based dependency injections.
    analyzeFields(componentClass, analysis);
    
    return analysis;
  }
  
  /**
   * Gather field based dependency injections.  This will find any private, protected or public fields with dependency injections.
   * 
   * @param componentClass The component class to analyze.
   * @param analysis The analysis to build upon.
   */
  private static void analyzeFields(Class componentClass, ComponentAnalysis analysis) {
    for (Field field : componentClass.getDeclaredFields()) {
      In inAnnotation = field.getAnnotation(In.class);
      if (inAnnotation != null) {
        field.setAccessible(true);
        analysis.addInjection(new InjectionAnalysis(inAnnotation.select(), inAnnotation.prefixMappings(), inAnnotation.nullable(), field));
      }
    }
    
    // Check any parent interfaces.
    for (Class parentClass : componentClass.getInterfaces()) {
      analyzeFields(parentClass, analysis);
    }
    
    // Check the super class.
    if (componentClass.getSuperclass() != null) {
      analyzeFields(componentClass.getSuperclass(), analysis);
    }
  }
  
  /**
   * Create a new component from the given analysis.  This does not perform dependency injection nor begin the component.
   */
  public static Object createComponent(ComponentAnalysis analysis)
    throws InstantiationException, IllegalAccessException
  {
    return analysis.getComponentClass().newInstance();
  }
  
  /**
   * Perform dependency injection on the given component.
   */
  public static void doInjection(Object component, ComponentAnalysis analysis, JXPathContext context) 
    throws IllegalAccessException, InvocationTargetException
  {
    for (InjectionAnalysis injection : analysis.getInjections()) {
      Map<String, String> originalPrefixMapping = pushPrefixMap(context, injection.getPrefixMappings());
      
      try {
        if (injection.getField() != null) {
          injection.getField().set(component, context.getValue(injection.getSelect()));
        } else if (injection.getMethod() != null){
          injection.getMethod().invoke(component, context.getValue(injection.getSelect()));
        }
      } catch (Exception ex) {
        if (!injection.isNullable()) {
          // Unable to inject value on a non-nullable field.
          String failurePoint = null;
          if (injection.getField() != null)
            failurePoint = "field '" + injection.getField().getName() + "'";
          else if (injection.getMethod() != null)
            failurePoint = "method '" + injection.getMethod().getName() + "'";
          String failurePath = " from path '" + injection.getSelect() + "'";
          if( context instanceof ScopedJXPathContextImpl ) {
        	  ScopedJXPathContextImpl impl = (ScopedJXPathContextImpl)context;
        	  failurePath += " at scope '" + impl.getScope() + "'."; 
          }
          throw new DependencyInjectionException("Unable to inject value into " + failurePoint + failurePath, ex);
        }
      }
      
      popPrefixMap(context, originalPrefixMapping);
    }
  }
  
  /**
   * Add the given array of prefix mappings to the context.
   * 
   * @param context The working context.
   * @param prefixMappings The prefix mappings to add.
   * 
   * @return A prefix to namespace uri mapping of prefixes that were replaced. 
   */
  private static Map<String, String> pushPrefixMap(JXPathContext context, PrefixMapping prefixMappings[]) {
    Map<String, String> originalPrefixMapping = null;
    
    if (prefixMappings.length > 0) {
      originalPrefixMapping = new HashMap<String, String>();
      // Perform prefix mappings
      for (PrefixMapping prefixMap : prefixMappings) {
        // Store the original namespace for the prefix.
        originalPrefixMapping.put(prefixMap.prefix(), context.getNamespaceURI(prefixMap.prefix()));
        // Register the new namespace.
        context.registerNamespace(prefixMap.prefix(), prefixMap.uri());
      }
    }
    
    return originalPrefixMapping;
  }
  
  /**
   * Restore the given prefix to namespace uri mapping onto the context.
   * 
   * @param context The working context.
   * @param originalPrefixMapping The mapping to restore.
   */
  private static void popPrefixMap(JXPathContext context, Map<String, String> originalPrefixMapping) {
    if (originalPrefixMapping != null) {
      // Reverse prefix mappings
      for (String prefix : originalPrefixMapping.keySet()) {
        // Restore the original namespace for the prefix.
        context.registerNamespace(prefix, originalPrefixMapping.get(prefix));
      }
    }
  }  
  
  /**
   * Invoke the begin method on the given component. 
   */
  public static void doBegin(Object component)
    throws InvocationTargetException, IllegalAccessException
  {
    for (Method method : component.getClass().getMethods()) {
      Begin beginAnnotation = method.getAnnotation(Begin.class);
      if (beginAnnotation != null) {
        method.invoke(component);
      }
    }
  }
   
  /**
   * Invoke the end method on the given component. 
   */
  public static void doEnd(Object component)
    throws InvocationTargetException, IllegalAccessException
  {
    for (Method method : component.getClass().getMethods()) {
      End beginAnnotation = method.getAnnotation(End.class);
      if (beginAnnotation != null) {
        method.invoke(component);
      }
    }
  }
}
