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
package org.xchain.namespaces.jsl;

import org.xchain.Locatable;
import org.xchain.EngineeredCommand;
import org.xchain.impl.ChainImpl;
import org.apache.commons.jxpath.JXPathContext;
import org.xchain.annotations.Element;

/**
 * Almost an exact port of the class org.apache.commons.chain.impl.ChainBase, except the
 * nested chains are represented with a java.util.ArrayList instead of an array.
 *
 * @author Christian Trimble
 */
@Element(localName="temporary")
public abstract class TemporaryCommand
  extends ChainImpl
  implements Locatable, EngineeredCommand
{
  public boolean execute( JXPathContext context )
    throws Exception
  {
    throw new Exception("A temporary template failed to be replaced.");
  }
}
