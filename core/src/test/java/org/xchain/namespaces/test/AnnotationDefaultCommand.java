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
import org.xchain.framework.jxpath.ScopedQNameVariables;
import org.xchain.framework.jxpath.Scope;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
@Element(localName="annotation-default")
public abstract class AnnotationDefaultCommand
  implements Command
{
  public static Logger log = LoggerFactory.getLogger(LiteralEnumCommand.class);

  @Attribute(
    localName="name",
    type=AttributeType.QNAME,
    defaultValue="prefix:result",
    defaultPrefixMappings={
      @PrefixMapping(prefix="prefix", uri="http://www.xchain.org/test/1.0")
    }
  )
  public abstract QName getName( JXPathContext context );

  @Attribute(
    localName="value",
    type=AttributeType.JXPATH_VALUE,
    defaultValue="$prefix:input",
    defaultPrefixMappings={
      @PrefixMapping(prefix="prefix", uri="http://www.xchain.org/test/1.0")
    }
  )
  public abstract String getValue( JXPathContext context );

  public boolean execute( JXPathContext context )
    throws Exception
  {
    QName variableName = getName(context);
    Object variableValue = getValue(context);

    // set the value of the expression to the variable name.
    ((ScopedQNameVariables)context.getVariables()).declareVariable(variableName, variableValue, Scope.request);

    // return false and allow other chains to execute.
    return false;
  }
}
