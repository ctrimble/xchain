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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.lang.reflect.Method;

import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.Functions;
import org.apache.commons.jxpath.functions.MethodFunction;
import org.apache.commons.jxpath.util.MethodLookupUtils;

/**
 * <p>This class handles methods for an xchain namespace.  During the scanning process, methods with the org.xchain.annotations.Function annotation are added
 * to instances of this class.</p>
 *
 * <p>The implementation of this class is based on the implementation of ClassFunctions from the jxpath project by Dmitri Plotnikov.</p>
 *
 * @author Christian Trimble
 */
public class LifecycleFunctionLibrary
  implements Functions
{
  private static final Object[] EMPTY_ARRAY = new Object[0];

  private LifecycleContext lifecycleContext;

  public LifecycleFunctionLibrary( LifecycleContext lifecycleContext  )
  {
    this.lifecycleContext = lifecycleContext;
  }

  public Set getUsedNamespaces() {
    return lifecycleContext.getNamespaceContextMap().keySet();
  }

  public Function getFunction( String namespace, String name, Object[] parameters )
  {
    // make sure that we are requesting the correct namespace.
    if (namespace == null) {
      return null;
    }

    NamespaceContext namespaceContext = lifecycleContext.getNamespaceContextMap().get(namespace);

    if( namespaceContext == null ) { return null; }

    return namespaceContext.getFunctionLibrary().getFunction( namespace, name, parameters );
  }
}
