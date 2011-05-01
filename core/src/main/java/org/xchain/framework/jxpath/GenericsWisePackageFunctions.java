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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.jxpath.functions.ConstructorFunction;
import org.apache.commons.jxpath.functions.MethodFunction;
import org.xchain.framework.jxpath.MethodLookupUtils;
import org.apache.commons.jxpath.util.TypeUtils;
import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.NodeSet;
import org.apache.commons.jxpath.PackageFunctions;
import org.apache.commons.jxpath.Pointer;

/**
 * Almost an exact copy of <code>org.apache.commons.jxpath.PackageFunctions</code> implementation
 * written by Dimitri Plotnikov. This implementation is Adjusted to use a different method lookup
 * utility class that better handles inheritance oddities introduced in Java 1.5 when inheriting 
 * from a generic type.
 * 
 * @author Dimitri Plotnikov
 * @author John Trimble
 */
public class GenericsWisePackageFunctions extends PackageFunctions {
  private static final Object[] EMPTY_ARRAY = new Object[0];
  private String namespace;
  private String classPrefix;

  public GenericsWisePackageFunctions(String classPrefix, String namespace) {
    super(classPrefix, namespace);
    this.namespace = namespace;
    this.classPrefix = classPrefix;
  }
  
  public Set getUsedNamespaces() {
      return Collections.singleton(namespace);
  }

  public Function getFunction(
      String namespace,
      String name,
      Object[] parameters) {
      if ((namespace == null && this.namespace != null) //NOPMD
          || (namespace != null && !namespace.equals(this.namespace))) {
          return null;
      }

      if (parameters == null) {
          parameters = EMPTY_ARRAY;
      }

      if (parameters.length >= 1) {
          Object target = TypeUtils.convert(parameters[0], Object.class);
          if (target != null) {
              Method method =
                  MethodLookupUtils.lookupMethod(
                      target.getClass(),
                      name,
                      parameters);
              if (method != null) {
                  return new MethodFunction(method);
              }

              if (target instanceof NodeSet) {
                  target = ((NodeSet) target).getPointers();
              }

              method =
                  MethodLookupUtils.lookupMethod(
                      target.getClass(),
                      name,
                      parameters);
              if (method != null) {
                  return new MethodFunction(method);
              }

              if (target instanceof Collection) {
                  Iterator iter = ((Collection) target).iterator();
                  if (iter.hasNext()) {
                      target = iter.next();
                      if (target instanceof Pointer) {
                          target = ((Pointer) target).getValue();
                      }
                  }
                  else {
                      target = null;
                  }
              }
          }
          if (target != null) {
              Method method =
                  MethodLookupUtils.lookupMethod(
                      target.getClass(),
                      name,
                      parameters);
              if (method != null) {
                  return new MethodFunction(method);
              }
          }
      }

      String fullName = classPrefix + name;
      int inx = fullName.lastIndexOf('.');
      if (inx == -1) {
          return null;
      }

      String className = fullName.substring(0, inx);
      String methodName = fullName.substring(inx + 1);

      Class functionClass;
      try {
          functionClass = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
      }
      catch (ClassNotFoundException ex) {
          throw new JXPathException(
              "Cannot invoke extension function "
                  + (namespace != null ? namespace + ":" + name : name),
              ex);
      }

      if (methodName.equals("new")) {
          Constructor constructor =
              MethodLookupUtils.lookupConstructor(functionClass, parameters);
          if (constructor != null) {
              return new ConstructorFunction(constructor);
          }
      }
      else {
          Method method =
              MethodLookupUtils.lookupStaticMethod(
                  functionClass,
                  methodName,
                  parameters);
          if (method != null) {
              return new MethodFunction(method);
          }
      }
      return null;
  }
}
