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

import org.apache.commons.jxpath.JXPathContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xchain.Catalog;
import org.xchain.Command;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.framework.lifecycle.Lifecycle;

/**
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class TestTransactionCommand
{
  public static String SUCCESS_VARIABLE_1 = "test-success-1";

  protected JXPathContext context = null;
  protected Catalog catalog = null;

  @Before public void setUp()
    throws Exception
  {
    Lifecycle.startLifecycle();

    // get the catalog.
    catalog = CatalogFactory.getInstance().getCatalog("resource://context-class-loader/org/xchain/namespaces/hibernate/test-transaction-command.xchain");

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

  @Test public void testTransactionCommand1()
    throws Exception
  {
    Command command = catalog.getCommand("test-transaction-command-1");
    command.execute(context);
    assertTrue("Transaction creation failed.", context.getVariables().isDeclaredVariable(SUCCESS_VARIABLE_1));
  }

}
