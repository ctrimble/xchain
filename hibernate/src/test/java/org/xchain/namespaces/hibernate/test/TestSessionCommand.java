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
package org.xchain.namespaces.hibernate.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xchain.Catalog;
import org.xchain.Command;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.framework.lifecycle.ExecutionException;
import org.xchain.framework.lifecycle.Lifecycle;
import org.xchain.framework.hibernate.SessionFactoryNotFoundException;
import javax.xml.namespace.QName;

/**
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class TestSessionCommand
{
  public static final QName TEST_DEFAULT_SESSION = new QName("test-default-session");
  public static final QName TEST_DEFINED_NAMED_SESSION = new QName("", "test-defined-named-session");
  public static final QName TEST_UNDEFINED_NAMED_SESSION = new QName("", "test-undefined-named-session");
  public static final QName TEST_MULTIPLE_DEFINED_NAMED_SESSIONS = new QName("", "test-multiple-defined-named-sessions");

  public static String SUCCESS_VARIABLE = "test-success";

  protected JXPathContext context = null;
  protected Catalog catalog = null;

  @Before public void setUp()
    throws Exception
  {
    Lifecycle.startLifecycle();

    // get the catalog.
    catalog = CatalogFactory.getInstance().getCatalog("resource://context-class-loader/org/xchain/namespaces/hibernate/test-session-command.xchain");

    // create the context.
    context = JXPathContext.newContext(new Object());
  }

  @After public void tearDown()
    throws Exception
  {
    context = null;
    catalog = null;

    Lifecycle.stopLifecycle();
  }

  @Test public void testDefaultSession()
    throws Exception
  {
    Command command = catalog.getCommand(TEST_DEFAULT_SESSION);
    command.execute(context);
    assertTrue("Session creation failed.", context.getVariables().isDeclaredVariable(SUCCESS_VARIABLE) );
  }

  @Test public void testNamedSession()
    throws Exception
  {
    Command command = catalog.getCommand(TEST_DEFINED_NAMED_SESSION);
    command.execute(context);
    assertTrue("Session creation failed.", context.getVariables().isDeclaredVariable(SUCCESS_VARIABLE) );
  }

  @Test public void testUndefinedNamedSession()
    throws Exception
  {
    Command command = catalog.getCommand(TEST_UNDEFINED_NAMED_SESSION);

    try {
      command.execute(context);
    }
    catch( Throwable t ) {
      assertTrue("The session command threw the type '"+t.getClass().getName()+"' when asked to create an undefined named session.", t instanceof ExecutionException);
      assertNotNull("The session command threw an exception with a null cause.", t.getCause());
      assertTrue("The session command threw an exception with a cause of type '"+t.getCause().getClass().getName()+"' when asked to create an undefined session.", t.getCause() instanceof SessionFactoryNotFoundException);
      return;
    }
    fail("The session command created a command for an undefined named session.");
  }

  @Test public void testMultipleDefinedNamedSession()
    throws Exception
  {
    Command command = catalog.getCommand(TEST_MULTIPLE_DEFINED_NAMED_SESSIONS);
    command.execute(context);
    assertTrue("Session creation failed.", context.getVariables().isDeclaredVariable(SUCCESS_VARIABLE) );
  }
}
