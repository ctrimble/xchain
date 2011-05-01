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
package org.xchain.framework.jxpath;

import javax.xml.namespace.QName;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.jxpath.JXPathContext;
import org.xchain.annotations.Begin;
import org.xchain.annotations.End;
import org.xchain.annotations.In;
import org.xchain.annotations.PrefixMapping;
import org.xchain.framework.lifecycle.ComponentAnalysis;
import org.xchain.framework.lifecycle.Lifecycle;
import org.xchain.framework.lifecycle.LifecycleContext;
import org.xchain.framework.lifecycle.NamespaceContext;
import org.xchain.framework.util.ComponentUtil;
import org.xchain.framework.util.DependencyInjectionException;

import static org.xchain.framework.util.JXPathContextUtil.*;

/**
 * An implementation of the ScopedQNameVariables interface.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class ScopedQNameVariablesImpl
  extends QNameVariablesImpl
  implements ScopedQNameVariables
{
  protected ScopedQNameVariables parentVariables = null;
  protected Map<QName, Object> componentMap = new HashMap<QName, Object>();
  protected Scope scope = null;

  /**
   * Creates a new QName variables object that does not have a context assigned for qname lookups.
   */
  public ScopedQNameVariablesImpl( ScopedQNameVariables parentVariables, Scope scope)
  {
    this.parentVariables = parentVariables;
    this.scope = scope;
  }

  /**
   * Creates a new QName variables object that has a context assigned for qname lookups.
   */
  public ScopedQNameVariablesImpl( JXPathContext context, ScopedQNameVariables parentVariables, Scope scope )
  {
    super( context );
    this.parentVariables = parentVariables;    
    this.scope = scope;
  }

  /**
   * Creates a new QName variables object that shares its variable map with another qname variables object.
   */
  protected ScopedQNameVariablesImpl( JXPathContext context, Map variableMap, ScopedQNameVariables parentVariables, Scope scope )
  {
    super( context, variableMap );
    this.parentVariables = parentVariables;
    this.scope = scope;   
  }

  public void declareVariable( String qName, Object value )
  {
    declareVariable( stringToQName(context,qName), value );
  }
  
  public void declareVariable(QName varName, Object value) {
    if (getComponentAnalysis(varName) != null)      
      throw new IllegalStateException("Unable to declare the variable " + varName + " as it conflicts with a registered component.");
    
    super.declareVariable(varName, value);
  }

  public void declareVariable( String varName, Object value, Scope scope )
  {
    declareVariable( stringToQName(context,varName), value, scope );
  }

  public void declareVariable( QName varName, Object value, Scope scope )
  {
    if (this.scope == scope) {
      declareVariable(varName, value);
    } else if (parentVariables != null) {
      parentVariables.declareVariable(varName, value, scope);
    } else {
      throw new IllegalStateException("Unable to access scope " + scope);
    }
  }

  public Object getVariable( String qName )
  {
    return getVariable( stringToQName(context, qName) );
  }
    
  public Object getVariable(QName varName) {
    Object variable = null;
    
    // Check if a component analysis exists for the QName
    ComponentAnalysis componentAnalysis = getComponentAnalysis(varName);
    if (componentAnalysis != null) {
      // Check if the component is for the current scope
      if (componentAnalysis.getScope() != scope) {
        if (parentVariables != null) {
          // Check the parent variables
          variable = parentVariables.getVariable(varName);
        } else {
          // No parent variables and the component is not for this scope.  Throw an exception.
          throw new IllegalStateException("Unable to access component for scope " + componentAnalysis.getScope());
        }
      } else {
        // Get the component at the current scope.
        variable = getComponent(varName);
      }
    }
    
    if (variable == null)
      variable = super.getVariable(varName);
    
    return variable;
  }

  public Object getVariable( String qName, Scope scope )
  {
    return getVariable( stringToQName(context, qName), scope );
  }

  public Object getVariable( QName qName, Scope scope )
  {
    if (this.scope == scope) {
      return getVariable(qName);
    } else if (parentVariables != null) {
      return parentVariables.getVariable(qName, scope);
    } else {
      throw new IllegalStateException("Unable to access scope " + scope);
    }
  }

  public boolean isDeclaredVariable( String varName )
  {
    return isDeclaredVariable( stringToQName(context, varName) );
  }
  
  public boolean isDeclaredVariable(QName varName) {
    // Consider the variable declared if a component exists for the QName at this scope.
    ComponentAnalysis componentAnalysis = getComponentAnalysis(varName);
    if (componentAnalysis != null && componentAnalysis.getScope() == scope)
      return true;
    
    return super.isDeclaredVariable(varName);
  }

  /**
   * Determine if a variable with the given name is declared at the given scope.
   * 
   * @param varName The name of the variable.
   * @param scope The scope to search on.
   * 
   * @return True if the variable is declared.  False if it is not.
   */
  public boolean isDeclaredVariable( String varName, Scope scope )
  {
    return isDeclaredVariable( stringToQName(context,varName), scope );
  }

  public boolean isDeclaredVariable( QName varName, Scope scope )
  {
    if (this.scope == scope) {
      return isDeclaredVariable(varName);
    } else if (parentVariables != null) {
      return parentVariables.isDeclaredVariable(varName, scope);
    } else {
      throw new IllegalStateException("Unable to access scope " + scope);
    }
  }

  public void undeclareVariable( String varName )
  {
    undeclareVariable( stringToQName(context,varName) );
  }

  public void undeclareVariable( String varName, Scope scope )
  {
    undeclareVariable( stringToQName(context,varName), scope );
  }

  public void undeclareVariable( QName varName, Scope scope )
  {
    if (this.scope == scope) {
      undeclareVariable(varName);
    } else if (parentVariables != null) {
      parentVariables.undeclareVariable(varName, scope);
    } else {
      throw new IllegalStateException("Unable to access scope " + scope);
    }
  }
  
  /**
   * Release all components that were declared.  Any methods annotated with End will be called on the Compoent instance.
   */
  public void releaseComponents() {
    for (Object component : componentMap.values()) {
      try {
        // Run the end method (if present)
        ComponentUtil.doEnd(component);
      } catch (Exception ex) {
        if (log.isErrorEnabled()) {
          log.error("Error releasing component.", ex);
        }
      }
    }
    
    // Clear out the component map
    componentMap.clear();
  }
  
  /**
   * Get an instance of a component with the given QName.
   * 
   * @param componentName The QName of the component to load.
   * 
   * @return An instance of the component for the given QName.  Null if there is no component
   * for the given QName.
   */
  private Object getComponent(QName componentName) {
    if (componentMap.containsKey(componentName)) {
      // Component already instantiated.
      return componentMap.get(componentName);
    } else {
      Object component = null;
      
      // Attempt to find the component class
      ComponentAnalysis analysis = getComponentAnalysis(componentName);      
      if (analysis != null) {
        try {
          // Create the component
          component = ComponentUtil.createComponent(analysis);
          // Perform dependency injection
          ComponentUtil.doInjection(component, analysis, context);
          // Run the begin method (if present)
          ComponentUtil.doBegin(component);
          
          // Add the component to the component map.
          componentMap.put(componentName, component);
        } catch (DependencyInjectionException ex) {
          throw ex;
        } catch (Exception ex) {          
          if (log.isErrorEnabled()) {
            log.error("Error creating component: " + componentName, ex);
          }
          
          throw new IllegalArgumentException("Error creating component: " + componentName, ex);
        }
      }
      
      return component;
    }
  }
  
  /**
   * Get the component class for the given QName.
   * 
   * @param componentName
   * 
   * @return The component class for the given QName.  Null if the QName does not reference a known component.
   */
  private ComponentAnalysis getComponentAnalysis(QName componentName) {
    NamespaceContext namespaceContext = null;

    // Get the lifecycle context
    LifecycleContext lifecycleContext = Lifecycle.getLifecycleContext();
    if (lifecycleContext != null) {
      // Get the namespace context.
      namespaceContext = lifecycleContext.getNamespaceContextMap().get(componentName.getNamespaceURI());
    }
    
    if (namespaceContext != null) {
      // Get the component from the namespace context.
      return namespaceContext.getComponentMap().get(componentName.getLocalPart());
    } else {
      // Namespace is not defined.  No component could exist.
      return null;
    }
  }

  @Override
  protected void finalize() throws Throwable {
    if (componentMap.size() != 0) {
      if (log.isWarnEnabled()) {
        log.warn("Components at scope: " + scope + " were not properly released.");
      }
      
      releaseComponents();
    }
  }  
  
  
}
