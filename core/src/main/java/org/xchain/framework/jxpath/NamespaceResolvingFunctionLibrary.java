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
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.PackageFunctions;
import org.apache.commons.jxpath.ri.NamespaceResolver;

/**
 * This function library was added to provide namespace prefix lookup for Functions classes.  The
 * standard reference implementation of JXPath does not resolve the namespace prefix before looking up a function.
 *
 * @author Christian Trimble
 * @author John Trimble
 */
public class NamespaceResolvingFunctionLibrary
  extends FunctionLibrary
{
  private NamespaceResolver resolver = null;
  private PackageFunctions packageFunctions = new GenericsWisePackageFunctions("", null);

  public NamespaceResolvingFunctionLibrary( NamespaceResolver resolver )
  {
    this.resolver = resolver;
    this.addFunctions(packageFunctions);
  }

  public Function getFunction( String prefix, String name, Object[] parameters )
  {
    Function function = null;
    if( prefix == null ) {
      function = packageFunctions.getFunction(prefix, name, parameters);
    }
    else {
      String namespace = resolver.getNamespaceURI(prefix);
      if( namespace != null ) {
        function = super.getFunction( namespace, name, parameters );
      }
    }
    if( function == null ) {
      /*
      StringBuilder sb = new StringBuilder();
      sb.append("The function ").append(prefix).append(":").append(name).append("(");
      for( int i = 0; i < parameters.length; i++ ) {
        sb.append(parameters[i]!=null?parameters[i].getClass().getName():"null");
        if( i < parameters.length - 1 ) {
          sb.append(", ");
        }
      }
      sb.append("} is not defined.");
      throw new JXPathException(sb.toString());
      */
    }

    return function;
  }
}
