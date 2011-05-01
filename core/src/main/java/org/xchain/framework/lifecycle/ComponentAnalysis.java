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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.xchain.annotations.PrefixMapping;
import org.xchain.framework.jxpath.Scope;

/**
 * Analysis of an xchain component.  This holds information about the dependency injections that the component requires.
 *
 * @author Devon Tackett
 */
public class ComponentAnalysis {
  private final Class componentClass;
  private String localName;
  private Scope scope;
  
  private Set<InjectionAnalysis> injections = new HashSet<InjectionAnalysis>();
  
  public static class InjectionAnalysis {
    private final String select;
    private final PrefixMapping[] prefixMappings;
    private final Method method;
    private final boolean nullable;
    private final Field field;
    
    public InjectionAnalysis(String select, PrefixMapping[] prefixMappings, boolean nullable, Method method) {
      this(select, prefixMappings, nullable, method, null);
    }
    
    public InjectionAnalysis(String select, PrefixMapping[] prefixMappings, boolean nullable, Field field) {
      this(select, prefixMappings, nullable, null, field);
    }
    
    private InjectionAnalysis(String select, PrefixMapping[] prefixMappings, boolean nullable, Method method, Field field) {
      this.select = select;
      this.prefixMappings = prefixMappings;
      this.method = method;
      this.field = field;
      this.nullable = nullable;
    }

    /**
     * @return The select path for the injection. 
     */
    public String getSelect() {
      return select;
    }

    /**
     * @return Any prefix mappings for the select path.
     */
    public PrefixMapping[] getPrefixMappings() {
      return prefixMappings;
    }

    /**
     * @return The method to inject into.  Null if this analysis is not for a method.
     */
    public Method getMethod() {
      return method;
    }

    /**
     * @return The field to inject into.  Null if this analysis is not for a field.
     */
    public Field getField() {
      return field;
    }
    
    /**
     * @return Whether the dependency can be null.
     */
    public boolean isNullable() {
      return nullable;
    }
  }
  
  public ComponentAnalysis(Class componentClass) {
    this.componentClass = componentClass;
  }

  /**
   * @return The component class this analysis represents.
   */
  public Class getComponentClass() {
    return componentClass;
  }

  /**
   * @return The local name for the component.
   */
  public String getLocalName() {
    return localName;
  }

  /**
   * Set the local name for the component.
   */
  public void setLocalName(String localName) {
    this.localName = localName;
  }

  /**
   * @return The scope for the component.
   */
  public Scope getScope() {
    return scope;
  }

  /**
   * Set the scope for the component.
   */
  public void setScope(Scope scope) {
    this.scope = scope;
  }

  /**
   * @return Set of injections for the component.
   */
  public Set<InjectionAnalysis> getInjections() {
    return Collections.unmodifiableSet(injections);
  }
  
  /**
   * Add an injection for the component.
   */
  public void addInjection(InjectionAnalysis injection) {
    injections.add(injection);
  }
}
