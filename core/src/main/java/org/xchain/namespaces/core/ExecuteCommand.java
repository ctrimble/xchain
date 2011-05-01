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

import java.net.URI;

import org.xchain.Command;
import org.xchain.Filter;
import org.xchain.Locatable;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.framework.lifecycle.Execution;
import org.xchain.framework.util.ThreadLocalStack;
import javax.xml.namespace.QName;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;

import org.apache.commons.jxpath.JXPathContext;

/**
 * <p>The <code>execute</code> command can execute another command chain.  The command can be in the current
 * catalog or in a different catalog.  The command chain will continue if and only if the referenced command
 * would continue the command chain.</p>
 *
 * <code class="source">
 * &lt;xchain:chain xmlns:xchain="http://www.xchain.org/core/1.0"&gt;
 *   ...
 *   &lt;xchain:execute system-id="$myCatalog" name="$myCommand"&gt;
 *   ...
 * &lt;/xchain:choose&gt;
 * </code>
 *
 * @author Christian Trimble
 * @author Devon Tackett
 */
@Element(localName="execute")
public abstract class ExecuteCommand
  implements Filter, Locatable
{
  protected static ThreadLocalStack<Command> callStack = new ThreadLocalStack<Command>();

  /**
   * The system id of the catalog to search for the command.  If no system id is provided, then the current catalog is
   * searched. If the system id is relative, then it is resolved against the current catalog's system id.
   *
   * @param context the context that the command is executed in.
   * @return the system id of the catalog.
   */
  @Attribute(localName="system-id", type=AttributeType.JXPATH_VALUE)
  public abstract String getSystemId( JXPathContext context );
  public abstract boolean hasSystemId();

  /**
   * The qname of the command to execute.  The qname is required.
   *
   * @param context the context that the command is executed in.
   * @return the qname of the command.
   */
  @Attribute(localName="name", type=AttributeType.JXPATH_VALUE)
  public abstract QName getName( JXPathContext context );
  public abstract boolean hasName();

  /**
   * Executes the command specified by the system-id and name attributes.
   */
  public boolean execute( JXPathContext context )
    throws Exception
  {
    Command command = null;
    boolean result = false;
    Exception exception = null;

    if( hasSystemId() && hasName() ) {
      command = CatalogFactory.getInstance().getCatalog(resolveSystemId(getSystemId(context))).getCommand(getName(context));
    }
    else if( hasName() ) {
      command = CatalogFactory.getInstance().getCatalog(Execution.getSystemId()).getCommand(getName(context));
    }
    else {
      throw new Exception("Call command must specify both a system id and a name.");
    }

    // set the context for the command, if needed.
    try {
      result = command.execute(context);
    }
    catch( Exception e ) {
      exception = e;
    }
    finally {
      callStack.push(command);
    }

    if( exception != null ) {
      throw exception;
    }

    return result;
  }

  public boolean postProcess( JXPathContext context, Exception exception )
  {
    Command command = callStack.pop();
    boolean result = false;

    if( command instanceof Filter ) {
      // set the context for the command.
      try {
        result = ((Filter)command).postProcess( context, exception );
      }
      catch( Exception e ) {
        // ignore this exception.
      }
    }

    return result;
  }

  public String resolveSystemId( String systemId )
  {
    return URI.create(Execution.getSystemId()).resolve(systemId).toString();
  }
}
