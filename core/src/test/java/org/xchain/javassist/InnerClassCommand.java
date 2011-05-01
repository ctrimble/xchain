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
package org.xchain.javassist;

import org.xchain.Command;
import org.apache.commons.jxpath.JXPathContext;

/**
 * @author Christian Trimble
 */
public class InnerClassCommand
  implements Command
{
  public StaticInnerCommand staticInnerCommand = new StaticInnerCommand();
  public InnerCommand innerCommand = new InnerCommand();

  public boolean execute( JXPathContext context )
    throws Exception
  {
    String testNamespace = context.getNamespaceURI("test");
    context.getVariables().declareVariable("namespace", testNamespace);
    staticInnerCommand.execute(context);
    innerCommand.execute(context);
    return true;
  }

  public static class StaticInnerCommand
    implements Command
  {
    public boolean execute( JXPathContext context )
      throws Exception
    {
      String testNamespace = context.getNamespaceURI("test");
      context.getVariables().declareVariable("static-inner-namespace", testNamespace);
      return true;
    }
  }

  public static class InnerCommand
    implements Command
  {
    public boolean execute( JXPathContext context )
      throws Exception
    {
      String testNamespace = context.getNamespaceURI("test");
      context.getVariables().declareVariable("inner-namespace", testNamespace);
      return true;
    }
  }
}

