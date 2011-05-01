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
import org.xchain.EngineeredCommand;
import org.xchain.impl.ChainImpl;
import org.apache.commons.jxpath.JXPathContext;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.annotations.Element;
import org.xchain.annotations.ParentElement;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;

/**
 * <p>The <code>choose</code> command allows the execution of one chain based on tested conditions.
 * Only <code>when</code> and <code>otherwise</code> commands can be direct children of the <code>choose</code> command.  
 * Each <code>when</code> command has a <code>test</code> attribute.  The first
 * <code>when</code> command whose <code>test</code> attribute evaluates to <code>true</code> will be executed and no other
 * child <code>when</code> or <code>otherwise</code> commands
 * will be executed or have their test condition evaluated.  <code>otherwise</code> commands will always be executed if encountered.</p>
 *
 * <code class="source">
 * &lt;xchain:choose xmlns:xchain="http://www.xchain.org/core/1.0"&gt;
 *   &lt;xchain:when test="/some/xpath"&gt;
 *     ...
 *   &lt;/xchain:when&gt;
 *   &lt;xchain:when test="/some/other/xpath"&gt;
 *     ...
 *   &lt;/xchain:when&gt;
 *   &lt;xchain:otherwise&gt;
 *     ...
 *   &lt;/xchain:otherwise&gt;
 * &lt;/xchain:choose&gt;
 * </code>
 *
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
@Element(localName="choose")
public abstract class ChooseCommand
  extends ChainImpl
{
  public static Logger log = LoggerFactory.getLogger(ChooseCommand.class);

  public boolean execute( JXPathContext context )
    throws Exception
  {
    if( log.isDebugEnabled() ) {
      log.debug("Calling Choose.");
    }

    // iterate over the when clauses looking for a match.
    // if a match is found, then execute the associated chain.
    Iterator<Command> childIterator = getCommandList().iterator();
    while( childIterator.hasNext() ) {
      Command clause = childIterator.next();

      if( clause instanceof WhenClause ) {
        WhenClause whenClause = (WhenClause)clause;

        if( Boolean.TRUE.equals(whenClause.getTest(context)) ) {
          return whenClause.execute(context);
        }
      }
      else if( clause instanceof OtherwiseClause ) {
        return clause.execute(context);
      }
      else {
        throw new Exception("The choose command has a child command '"+clause.getClass().getName()+"'.  Only when and otherwise clauses are allowed here.");
      }
    }

    // if we got this far, then just return false.
    return false;
  }

  /**
   * <p><code>choose</code> commands can have one or more <code>when</code> commands nested in it.  The first <code>when</code> command 
   * whose <code>test</code> evaluates to <code>true</code> will be executed.</p>
   */
  @Element(localName="when", parentElements={@ParentElement(localName="choose", namespaceUri="http://www.xchain.org/core/1.0")})
  public abstract static class WhenClause
    extends ChainImpl
    implements EngineeredCommand
  {
    /**
     * <p>The test for this <code>when</code> clause.  If the test evaluates to <code>true</code>, then the when clause is executed.  
     * If the test evaluates to <code>false</code>, then the 
     * next <code>when</code> or <code>otherwise</code> command in the choose will be tested.</p>
     */
    @Attribute(localName="test", type=AttributeType.JXPATH_VALUE)
    public abstract Boolean getTest( JXPathContext context );
  }

  /**
   * <p><code>choose</code> commands can include an <code>otherwise</code> command.  This command will be executed if none of the 
   * <code>when</code> clauses associated with the same <code>choose</code> command executed.</p>
   */
  @Element(localName="otherwise", parentElements={@ParentElement(localName="choose", namespaceUri="http://www.xchain.org/core/1.0")})
  public static class OtherwiseClause
    extends ChainImpl
  {
    // the extension is all that we need.
  }
}
