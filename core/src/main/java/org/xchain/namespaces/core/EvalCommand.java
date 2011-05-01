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

import org.apache.commons.jxpath.JXPathContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xchain.Command;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;

import org.xchain.framework.jxpath.Scope;
import org.xchain.framework.jxpath.ScopedQNameVariables;

import java.util.List;

import javax.xml.namespace.QName;

/**
 * <p>The <code>eval</code> command will evaluate an xpath expression.
 *
 * <code class="source">
 * &lt;xchain:eval xmlns:xchain="http://www.xchain.org/core/1.0" expression="/some/xpath"/&gt;
 * </code>
 *
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
@Element(localName="eval")
public abstract class EvalCommand
  implements Command
{
  /**
   * The xpath expression to be evaluated using context.getValue(context, Object.class);
   */
  @Attribute(localName="expression", type=AttributeType.JXPATH_VALUE)
  public abstract Object getExpression( JXPathContext context )
    throws Exception;
  
  public boolean execute( JXPathContext context )
    throws Exception
  {
    getExpression(context);

    return false;
  }
}
