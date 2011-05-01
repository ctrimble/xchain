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
package org.xchain.example.namespaces.guide;

import javax.xml.namespace.QName;

import org.apache.commons.jxpath.JXPathContext;

import org.xchain.Command;
import org.xchain.annotations.Element;
import org.xchain.framework.jxpath.Scope;
import org.xchain.framework.jxpath.ScopedQNameVariables;

/**
 * <p>Definition of the {http://www.xchain.org/guide}hello-world command.  This command sets the variable {http://www.xchain.org/guide}hello
 * to the value "Hello World".</p>
 *
 * @author Christian Trimble
 */
@Element(localName="hello-world")
public abstract class HelloWorldCommand
  implements Command
{
  /**
   * This method will be called when an {http://www.xchain.org/guide}hello-world element is encountered.
   *
   * @param context the context in which this command is called.
   */
  public boolean execute( JXPathContext context )
    throws Exception
  {
    // create a QName for the variable that we are going to set.
    QName name = QName.valueOf("{http://www.xchain.org/guide}hello");

    // declare the variable.  Out of the box, JXPath does not cleanly implement variables with
    // QNames and it does not have a concept of scope, so we will need to cast the variables
    // class to a org.xchain.framework.jxpath.ScopedQNameVariables class.
    ((ScopedQNameVariables)context.getVariables()).declareVariable(name, "Hello World", Scope.chain);

    // return false, so that other chains will execute.
    return false;
  }
}
