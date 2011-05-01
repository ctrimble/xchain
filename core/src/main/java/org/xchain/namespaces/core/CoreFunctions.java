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
package org.xchain.namespaces.core;

import org.xchain.framework.lifecycle.Execution;
import org.xchain.annotations.Function;
import org.apache.commons.jxpath.ExpressionContext;

/**
 * @author Christian Trimble
 */
public class CoreFunctions
{
  /**
   * Returns the system id of the current catalog.
   */
  @Function(localName="system-id")
  public static String getSystemId()
  {
    return Execution.getSystemId();
  }

  @Function(localName="value-of")
  public static Object getValueOf( ExpressionContext context, String xPath )
  {
    return context.getJXPathContext().getValue(xPath);
  }

  @Function(localName="value-of")
  public static <T> T getValueOf( ExpressionContext context, String xPath, Class<T> type )
  {
    return (T)context.getJXPathContext().getValue(xPath, type);
  }

  @Function(localName="class")
  public static Class getClass( String className )
    throws ClassNotFoundException
  {
    return Thread.currentThread().getContextClassLoader().loadClass(className);
  }
}
