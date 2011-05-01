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
package org.xchain.namespaces.test;

import javax.xml.namespace.QName;

import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.Command;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.annotations.PrefixMapping;
import org.xchain.annotations.Function;
import org.xchain.framework.jxpath.ScopedQNameVariables;
import org.xchain.framework.jxpath.Scope;

/**
 * @author Christian Trimble
 */
@Element(localName="exception-thrower")
public abstract class ExceptionThrowerCommand
  implements Command
{
  public static Logger log = LoggerFactory.getLogger(ExceptionThrowerCommand.class);

  @Attribute(
    localName="jxpath-value-with-exception",
    type=AttributeType.JXPATH_VALUE)
  public abstract Object getJXPathValueWithException( JXPathContext context )
    throws Exception;
  public abstract boolean hasJXPathValueWithException();

  @Attribute(
    localName="jxpath-value-without-exception",
    type=AttributeType.JXPATH_VALUE)
  public abstract Object getJXPathValueWithoutException( JXPathContext context );
  public abstract boolean hasJXPathValueWithoutException();

  public boolean execute( JXPathContext context )
    throws Exception
  {
    Object value = null;

    if( hasJXPathValueWithException() ) {
      value = getJXPathValueWithException(context);
    }
    else if( hasJXPathValueWithoutException() ) {
      value = getJXPathValueWithoutException(context);
    }

    return false;
  }

  @Function(localName="throw")
  public static void throwException( Throwable throwable )
    throws Throwable
  {
    throw throwable;
  }
}
