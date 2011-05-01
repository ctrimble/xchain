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

import org.xchain.Command;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;

import org.apache.commons.jxpath.JXPathContext;

/**
 * @author Christian Trimble
 */
@Element(localName="throw")
public abstract class ThrowCommand
  implements Command
{
  @Attribute(localName="select", type=AttributeType.JXPATH_VALUE)
  public abstract Exception getException( JXPathContext context );

  public boolean execute( JXPathContext context )
    throws Exception
  {
    Exception exception = getException(context);
    throw exception;
  }
}
