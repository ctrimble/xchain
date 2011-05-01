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
import org.xchain.annotations.Element;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.apache.commons.jxpath.JXPathContext;
import java.util.LinkedList;

/**
 * <p>The <code>if</code> command implemented as a filter.  This command will execute its child commands if the <code>test</code> 
 * attribute evaluates to <code>true</code>.</p>

 * 
 * <code class="source">
 * &lt;xchain:filter-if xmlns:xchain="http://www.xchain.org/core/1.0" test="/some/xpath"&gt;
 *   ...
 * &lt;/xchain:filter-if&gt;
 * </code>
 *
 * @author Christian Trimble
 * @author Devon Tackett
 * 
 * @see IfCommand
 * @see Filter
 */
@Element(localName="filter-if")
public abstract class FilterIfCommand
  extends FilterChainCommand
{
  protected ThreadLocal<LinkedList<Execution>> executionStackThreadLocal = new ThreadLocal<LinkedList<Execution>>();

  /**
   * The test for the if command.  This should evaluate to a boolean. 
   */
  @Attribute(localName="test", type=AttributeType.JXPATH_VALUE)
  public abstract Boolean getTest( JXPathContext context );

  public boolean execute( JXPathContext context )
    throws Exception
  {
    // push the execution
    Execution execution = new Execution();
    pushExecution(execution);

    // test the condition.
    Boolean result = getTest( context );

    // execute the nested chains.
    if( result != null && result.booleanValue() == true ) {
      execution.setExecuteCalled(true);
      return super.execute(context);
    }

    // if the test returned false, then return false and let the other chains execute.
    return false;
  }

  public boolean postProcess( JXPathContext context, Exception exception )
  {
    if( popExecution().getExecuteCalled() ) {
      return super.postProcess( context, exception );
    }
    return false;
  }

  public LinkedList<Execution> getExecutionStack()
  {
    LinkedList<Execution> executionStack = executionStackThreadLocal.get();

    if( executionStack == null ) {
      executionStack = new LinkedList<Execution>();
      executionStackThreadLocal.set(executionStack);
    }

    return executionStack;
  }

  public void pushExecution( Execution execution )
  {
    getExecutionStack().addFirst(execution);
  }

  public Execution popExecution()
  {
    return (Execution)getExecutionStack().removeFirst();
  }

  private static class Execution
  {
    protected boolean executeCalled = false;

    public boolean getExecuteCalled() { return this.executeCalled; }
    public void setExecuteCalled( boolean executeCalled ) { this.executeCalled = executeCalled; }
  }
}
