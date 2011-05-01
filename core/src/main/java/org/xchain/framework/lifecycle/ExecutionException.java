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
package org.xchain.framework.lifecycle;

import java.util.List;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.IOException;
import org.xml.sax.Locator;

/**
 * An exception that represents an unhandled exception that was thrown during an execution.
 *
 * @author Christian Trimble
 */
public class ExecutionException
  extends Exception
{
  protected List<ExecutionTraceElement> executionTrace = null;

  public ExecutionException( String message, List<ExecutionTraceElement> executionTrace, Throwable cause )
  {
    super(message, cause);
    this.executionTrace = executionTrace;
  }

  public ExecutionException( String message, List<ExecutionTraceElement> executionTrace )
  {
    super(message);
    this.executionTrace = executionTrace;
  }

  public ExecutionException( List<ExecutionTraceElement> executionTrace )
  {
    super();
    this.executionTrace = executionTrace;
  }

  public List<ExecutionTraceElement> getExecutionTrace() { return this.executionTrace; }

  public void printStackTrace( PrintStream s )
  {
    printStackTrace( new PrintWriter(s) );
  }

  public void printStackTrace( PrintWriter s )
  {
      s.println(getMessage());
      for( ExecutionTraceElement element : executionTrace ) {
        s.print("        at ");
        Locator locator = element.getLocator();
        if( locator != null ) {
          s.print(locator.getSystemId());
          s.print(":");
          s.print(locator.getLineNumber());
          s.print(":");
          s.print(locator.getColumnNumber());
        }
        else {
          s.print(element.getSystemId());
          s.print(":");
          s.print(element.getQName());
        }
        s.println();
      }
      if( getCause() != null ) {
        getCause().printStackTrace(s);
      }
  }
}
