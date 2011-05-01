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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.Functions;
import org.apache.commons.jxpath.functions.MethodFunction;
import org.apache.commons.jxpath.functions.ConstructorFunction;
import org.apache.commons.jxpath.util.MethodLookupUtils;
import org.apache.commons.jxpath.ExpressionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This class handles methods for an xchain namespace.  During the scanning process, methods with the org.xchain.annotations.Function annotation are added
 * to instances of this class.</p>
 *
 * <p>The implementation of this class is based on the implementation of ClassFunctions from the jxpath project by Dmitri Plotnikov.</p>
 *
 * @author Christian Trimble
 * @author Josh Kennedy
 */
public class NamespaceFunctionLibrary
  implements Functions
{
  private static Logger log = LoggerFactory.getLogger(NamespaceFunctionLibrary.class);

  private static final Object[] EMPTY_ARRAY = new Object[0];
  private Map<String, Set<MethodInfo>> staticMethodInfoMap = new HashMap<String, Set<MethodInfo>>();
  private Map<String, Set<MethodInfo>> instanceMethodInfoMap = new HashMap<String, Set<MethodInfo>>();
  private Map<String, Set<Class>> constructorClassMap = new HashMap<String, Set<Class>>();
  private String namespace;

  public NamespaceFunctionLibrary( String namespace )
  {
    this.namespace = namespace;
  }

  public void addStaticFunction( String localName, Class methodClass, String methodName )
  {
    Set<MethodInfo> methodInfoSet = staticMethodInfoMap.get(localName);
    if( methodInfoSet == null ) {
      staticMethodInfoMap.put(localName, (methodInfoSet = new LinkedHashSet<MethodInfo>()));
    }
    methodInfoSet.add(new MethodInfo(methodClass, methodName));
  }

  public void addInstanceFunction( String localName, Class methodClass, String methodName )
  {
    Set<MethodInfo> methodInfoSet = instanceMethodInfoMap.get(localName);
    if( methodInfoSet == null ) {
      instanceMethodInfoMap.put(localName, (methodInfoSet = new LinkedHashSet<MethodInfo>()));
    }
    methodInfoSet.add(new MethodInfo(methodClass, methodName));
  }

  public void addSingletonInstanceFunction( String localName, Class methodClass, String methodName, Method accessorMethod )
  {
    Set<MethodInfo> methodInfoSet = instanceMethodInfoMap.get(localName);
    if( methodInfoSet == null ) {
      instanceMethodInfoMap.put(localName, (methodInfoSet = new LinkedHashSet<MethodInfo>()));
    }
    methodInfoSet.add(new MethodInfo(methodClass, methodName, accessorMethod));
  }

  public void addConstructorFunction( String localName, Class constructorClass )
  {
    Set<Class> constructorClassSet = constructorClassMap.get(localName);
    if( constructorClassSet == null ) {
      constructorClassMap.put( localName, (constructorClassSet = new LinkedHashSet<Class>()) );
    }
    constructorClassSet.add(constructorClass);
  }

  public Set getUsedNamespaces() {
    return Collections.singleton(namespace);
  }

  public Function getFunction( String namespace, String name, Object[] parameters )
  {
    // make sure that we are requesting the correct namespace.
    if (namespace == null && this.namespace != null) {
      return null;
    }
    else if (!namespace.equals(this.namespace)) {
      return null;
    }

    if (parameters == null) {
      parameters = EMPTY_ARRAY;
    }

    Set<Class> constructorClassSet = constructorClassMap.get(name);
    Constructor constructor;
    if( constructorClassSet != null ) {
      for( Class constructorClass : constructorClassSet ) {
        constructor = MethodLookupUtils.lookupConstructor( constructorClass, parameters );
        if( constructor != null ) {
          return new ConstructorFunction(constructor);
        }
      }
    }

    Set<MethodInfo> methodInfoSet = staticMethodInfoMap.get(name);
    Method method;

    if( methodInfoSet != null ) {
      for( MethodInfo methodInfo : methodInfoSet ) {
        method = MethodLookupUtils.lookupStaticMethod(methodInfo.getMethodClass(), methodInfo.getMethodName(), parameters);
        if (method != null) {
          return new MethodFunction(method);
        }
      }
    }

    methodInfoSet = instanceMethodInfoMap.get(name);

    if( methodInfoSet != null ) {
      for( MethodInfo methodInfo : methodInfoSet ) {
        Object[] instanceParameters = parameters;
        Method singletonAccessor = methodInfo.getSingletonAccessor();
        if( singletonAccessor != null ) {
          try {
            Object singleton = singletonAccessor.invoke(null, EMPTY_ARRAY);
            instanceParameters = new Object[parameters.length+1];
            instanceParameters[0] = singleton;
            for( int i = 0; i < parameters.length; i++ ) {
              instanceParameters[i+1] = parameters[i];
            }
          method = MethodLookupUtils.lookupMethod(methodInfo.getMethodClass(), methodInfo.getMethodName(), instanceParameters);
          if (method != null) {
            return new SingletonMethodFunction(method, singleton);
          }
          }
          catch( Exception e ) {
            if( log.isDebugEnabled() ) {
              log.debug("Could not find singleton for class '"+methodInfo.getMethodClass().getName()+"'.", e);
            }
          }
        }
        else {
          method = MethodLookupUtils.lookupMethod(methodInfo.getMethodClass(), methodInfo.getMethodName(), instanceParameters);
          if (method != null) {
            return new MethodFunction(method);
          }
        }
      }
    }

    return null;
  }

  public static class MethodInfo
  {
    private Class methodClass;
    private String methodName;
    private Method singletonAccessor = null;

    public MethodInfo(Class methodClass, String methodName)
    {
      this.methodClass = methodClass;
      this.methodName = methodName;
    }

    public MethodInfo(Class methodClass, String methodName, Method singletonAccessor )
    {
      this( methodClass, methodName );
      this.singletonAccessor = singletonAccessor;
    }

    public int hashCode() {
      return methodClass.hashCode()/2 + methodClass.hashCode()/2;
    }

    public boolean equals( Object o )
    {
      if( o instanceof MethodInfo ) {
        MethodInfo mi = (MethodInfo)o;
        return methodClass.equals(mi.getMethodClass()) && methodName.equals(mi.getMethodName());
      }
      else {
        return false;
      }
    }

    public Class getMethodClass() { return this.methodClass; }
    public String getMethodName() { return this.methodName; }
    public Method getSingletonAccessor() { return this.singletonAccessor; }
  }

  public static class SingletonMethodFunction
    extends MethodFunction
  {
    private Object singleton;
    public SingletonMethodFunction( Method method, Object singleton )
    {
      super(method);
      this.singleton = singleton;
    }
    public Object invoke( ExpressionContext context, Object[] parameters )
    {
      if( parameters == null ) {
        parameters = EMPTY_ARRAY;
      }
      Object[] newParameters = new Object[parameters.length+1];
      newParameters[0] = singleton;
      for( int i = 0; i < parameters.length; i++ ) {
        newParameters[i+1] = parameters[i];
      }
      Object result = super.invoke( context, newParameters );

      return result;
    }
  }
}
