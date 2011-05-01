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

import org.xchain.Filter;
import org.xchain.Command;
import org.xchain.impl.FilterChainImpl;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.annotations.ParentElement;
import org.apache.commons.jxpath.JXPathContext;
import java.util.LinkedList;

/**
 * <p>The <code>choose</code> command implemented as a filter.  This command allows the execution of one chain based on tested conditions.
 * Only <code>when</code> and <code>otherwise</code> commands can be direct children of the <code>choose</code> command.  Each <code>when</code> 
 * command has a <code>test</code> attribute.  The first
 * <code>when</code> command whose <code>test</code> attribute evaluates to <code>true</code> will be executed and no other child 
 * <code>when</code> or <code>otherwise</code> commands
 * will be executed or have their <code>test</code> condition evaluated.  <code>otherwise</code> commands will always be executed if encountered.</p>
 *
 * <code class="source">
 * &lt;xchain:filter-choose xmlns:xchain="http://www.xchain.org/core/1.0"&gt;
 *   &lt;xchain:when test="/some/xpath"&gt;
 *     ...
 *   &lt;/xchain:when&gt;
 *   &lt;xchain:when test="/some/other/xpath"&gt;
 *     ...
 *   &lt;/xchain:when&gt;
 *   &lt;xchain:otherwise&gt;
 *     ...
 *   &lt;/xchain:otherwise&gt;
 * &lt;/xchain:filter-choose&gt;
 * </code>
 *
 * @author Christian Trimble
 * @author Devon Tackett
 * 
 * @see ChooseCommand
 * @see Filter
 */
@Element(localName="filter-choose")
public class FilterChooseCommand
  extends FilterChainImpl
{
  protected ThreadLocal<LinkedList<Execution>> executionStackThreadLocal = new ThreadLocal<LinkedList<Execution>>()
  {
    public LinkedList<Execution> initialValue() {
      return new LinkedList<Execution>();
    }
  };

  public boolean execute( JXPathContext context )
    throws Exception
  {
    Execution execution = new Execution();
    pushExecution(execution);

    // iterate over the when clauses looking for a match.
    // if a match is found, then execute the associated chain.
    for( Command command : getCommandList() ) {

      if( command instanceof WhenClause ) {
        WhenClause whenClause = (WhenClause)command;
        if( whenClause.getTest( context ) ) {
          // track the execution.
          execution.setExecutedClause(whenClause);

          // execute the clause.
          return whenClause.execute( context );
        }
      }

      // if a match was not found and there is an otherwise clause defined,
      // then execute the otherwise clause.
      else if( command instanceof OtherwiseClause ) {
        OtherwiseClause otherwiseClause = (OtherwiseClause)command;

        // track the execution.
        execution.setExecutedClause(otherwiseClause);

        // execute the clause.
        return otherwiseClause.execute( context );
      }
      else {
        throw new RuntimeException("The command '"+command.getClass().getName()+"' is not allowed inside a filter-choose command.");
      }
    }

    // if we got this far, then just return null.
    return false;
  }

  public boolean postProcess( JXPathContext context, Exception e )
  {
    Filter executedClause = popExecution().getExecutedClause();

    if( executedClause != null ) {
      return executedClause.postProcess(context, e);
    }

    return false;
  }

  protected void pushExecution( Execution execution )
  {
    executionStackThreadLocal.get().addFirst(execution);
  }

  protected Execution popExecution()
  {
    Execution execution = executionStackThreadLocal.get().removeFirst();
    if( executionStackThreadLocal.get().size() == 0 ) {
      executionStackThreadLocal.remove();
    }
    return execution;
  }

  private static class Execution
  {
    protected Filter executedClause = null;

    public void setExecutedClause( Filter executedClause ) { this.executedClause = executedClause; }
    public Filter getExecutedClause() { return this.executedClause; }
  }
  
  /**
   * <p><code>filter-choose</code> commands can have one or more when commands nested in it.  The first <code>when</code> 
   * command whose <code>test</code> evaluates to <code>true</code> will be executed.</p>
   */
  @Element(localName="when", parentElements={@ParentElement(localName="filter-choose", namespaceUri="http://www.xchain.org/core/1.0")})
  public static abstract class WhenClause
    extends FilterChainImpl
  {
    /**
     * <p>The test for this <code>when</code> clause.  If the test evaluates to <code>true</code>, then the <code>when</code> clause is 
     * executed.  If the test evaluates to <code>false</code>, then the next <code>when</code> or <code>otherwise</code> command in the 
     * <code>filter-choose</code> will be tested.</p>
     */    
    @Attribute(localName="test", type=AttributeType.JXPATH_VALUE)
    public abstract Boolean getTest( JXPathContext context );
  }

  /**
   * <p><code>filter-choose</code> commands can include an <code>otherwise</code> command.  This command will be executed if none of the 
   * <code>when</code> clauses associated with the same <code>filter-choose</code> command executed.</p>
   */  
  @Element(localName="otherwise", parentElements={@ParentElement(localName="filter-choose", namespaceUri="http://www.xchain.org/core/1.0")})
  public static class OtherwiseClause
    extends FilterChainImpl
  {
    // the extension is all that we need.
  }
}
