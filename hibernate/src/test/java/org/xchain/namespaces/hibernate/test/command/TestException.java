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
package org.xchain.namespaces.hibernate.test.command;

import org.apache.commons.jxpath.JXPathContext;
import org.xchain.Command;
import org.xchain.annotations.Element;

/**
 * @author Devon Tackett
 * @author Christian Trimble
 */
@Element(localName="test-exception")
public abstract class TestException
  implements Command
{
  public boolean execute( JXPathContext context )
    throws Exception
  {
    throw new ExpectedException("This is an expected exception.");
  }
  
  public class ExpectedException extends Exception {
	  ExpectedException(String message) {
		  super(message);
	  }
  }
}
