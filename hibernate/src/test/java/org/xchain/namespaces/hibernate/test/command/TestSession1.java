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

import org.xchain.namespaces.hibernate.AbstractSessionCommand;
import org.hibernate.Session;
import org.apache.commons.jxpath.JXPathContext;
import org.xchain.annotations.Element;
import org.xchain.framework.hibernate.HibernateLifecycle;
import javax.xml.namespace.QName;

/**
 * @author Mike Moulton
 * @author Christian Trimble
 */
@Element(localName="test-session-1")
public abstract class TestSession1
  extends AbstractSessionCommand
{

  public boolean execute( JXPathContext context )
    throws Exception
  {
    Session session = HibernateLifecycle.getCurrentSession(getName( context ));
    boolean result  = false;

    // test validity of session
    if (session != null && session.isOpen()) {
      // assert that the command succeeded.
      context.getVariables().declareVariable("test-success", "true");
    }

    // execute the chain 
    return super.execute( context );
  }

}
