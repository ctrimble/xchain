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

import org.xchain.impl.ChainImpl;
import org.xchain.annotations.Element;

/**
 * <p>A general container for xchain commands.  Children of a chain are executed, in order, until one of them
 * returns <code>true</code> or throws an exception.</p>
 * <p>If one of the chain's children returns <code>true</code>, then each child that was executed will have its post process
 * executed and then the chain will return <code>true</code>.</p>
 * <p>If one of the chain's children throws an exception, then each children that was executed, including the
 * command that threw the exception, will have its post process executed with the exception that was thrown.
 * If none of the children's post process returns <code>true</code>, then the exception will be rethrown by this chain.
 * If any of the childrens post process returns <code>true</code>, then this chain will return <code>false</code> and its next
 * sibling will execute.</p>
 *
 * <code class="source">
 * &lt;xchain:chain xmlns:xchain="http://www.xchain.org/core/1.0"&gt;
 *   ...
 * &lt;/xchain:chain&gt;
 * </code>
 *
 * @author Christian Trimble
 * @author Devon Tackett
 */
@Element(localName="chain")
public class ChainCommand
  extends ChainImpl
{
}
