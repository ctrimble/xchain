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

import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.Functions;
import org.apache.commons.jxpath.PackageFunctions;
import org.apache.commons.jxpath.functions.MethodFunction;
import org.apache.commons.jxpath.util.MethodLookupUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.lang.reflect.Method;

/**
 * @author Christian Trimble
 */
public class StandardFunctions
  implements Functions
{
  private String namespace;
  private PackageFunctions packageFunctions = new PackageFunctions("", null);

  public StandardFunctions( String namespace ) {
    this.namespace = namespace;
  }

  public Set<String> getUsedNamespaces() {
    return Collections.singleton(namespace);
  }

  public Function getFunction( String namespace, String name, Object[] parameters )
  {
    if( (this.namespace == null && namespace != null) || (this.namespace != null && !this.namespace.equals(namespace)) ) {
      return null;
    }

    if (parameters == null) { parameters = new Object[]{}; }

    if( "string-join".equals( name ) ) {
      Method method = MethodLookupUtils.lookupStaticMethod( StandardFunctions.class, "stringJoin", parameters );

      if( method == null ) {
        return packageFunctions.getFunction( namespace, name, parameters );
      }

      return new MethodFunction( method );
    }

    return packageFunctions.getFunction( namespace, name, parameters );
  }

  public static String stringJoin( Collection<Object> collection, String seperator )
  {
    StringBuilder stringBuilder = new StringBuilder();
    if( collection != null ) {
    Iterator<Object> iterator = collection.iterator();
    while( iterator.hasNext() ) {
      stringBuilder.append(iterator.next());
      if( iterator.hasNext() ) {
        stringBuilder.append(seperator);
      }
    }
    }
    return stringBuilder.toString();
  }

  public static String stringJoin( Object[] array, String seperator )
  {
    StringBuilder stringBuilder = new StringBuilder();
    for( int i = 0; i < array.length; i++ ) {
      stringBuilder.append(array[i]);
      if( i < array.length + 1 ) {
        stringBuilder.append(seperator);
      }
    }
    return stringBuilder.toString();
  }
}
