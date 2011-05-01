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
package org.xchain.test.locator;

import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.apache.commons.jxpath.JXPathContext;
import org.xchain.Command;
import org.xchain.Locatable;
import org.xchain.framework.jxpath.ScopedQNameVariables;
import org.xchain.framework.jxpath.Scope;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 */
@Element(localName="get-locator")
public abstract class GetLocatorCommand
  implements Command
{
  @Attribute(localName="variable", type=AttributeType.QNAME)
  public abstract String getVariable( JXPathContext context );

  public boolean execute( JXPathContext context )
    throws Exception
  {
    ((ScopedQNameVariables)context.getVariables()).declareVariable(getVariable(context), ((Locatable)this).getLocator(), Scope.request);
    return false;
  }
}
