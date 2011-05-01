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
import org.xchain.namespaces.hibernate.test.om.Person;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.apache.commons.jxpath.JXPathContext;
import org.xchain.annotations.Element;
import org.xchain.framework.hibernate.HibernateLifecycle;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Mike Moulton
 * @author Christian Trimble
 */
@Element(localName="test-validate-3")
public abstract class TestValidate3
  extends AbstractSessionCommand
{

  public boolean execute( JXPathContext context )
    throws Exception
  {
    Session session = HibernateLifecycle.getCurrentSession(getName( context ));
    Map contextHolder = new HashMap();
    List beans = new LinkedList();

    Person bean1 = new Person();
    beans.add(bean1);

    Person bean2 = new Person();
    beans.add(bean2);

    Person bean3 = new Person();
    bean3.setId( new Integer( 1234 ) );
    bean3.setName( "Test Person" );
    beans.add(bean3);

    contextHolder.put("beans", beans);
    context.getVariables().declareVariable("test-validate-beans", contextHolder);

    // execute the chain 
    return super.execute( context );
  }

}
