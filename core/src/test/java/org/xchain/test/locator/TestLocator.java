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
package org.xchain.test.locator;

import static org.junit.Assert.assertEquals;

import javax.xml.namespace.QName;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xchain.Catalog;
import org.xchain.Command;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.framework.lifecycle.Lifecycle;
import org.xml.sax.Locator;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class TestLocator
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/test/locator/locator.xchain";
  public static String NAMESPACE_URI = "http://www.xchain.org/test/locator";
  public static QName GET_LOCATOR_COMMAND = new QName(NAMESPACE_URI, "get-locator");

  protected JXPathContext context = null;
  protected Catalog catalog = null;
  protected Command command = null;
  
  @BeforeClass public static void setUpLifecycle()
  	throws Exception
  {
	  Lifecycle.startLifecycle();	  
  }

  @AfterClass public static void tearDownLifecycle()
  	throws Exception
  {
	  Lifecycle.stopLifecycle();  
  }   

  @Before public void setUp()
    throws Exception
  {
    context = JXPathContext.newContext(new Object());
    catalog = CatalogFactory.getInstance().getCatalog(CATALOG_URI);
  }

  @After public void tearDown()
    throws Exception
  {
    context = null;
  }

  @Test public void testGetLocator()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(GET_LOCATOR_COMMAND);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    Locator locator = (Locator)context.getValue("$locator", Locator.class);

    assertEquals("The system id of the locator was not correct.", CATALOG_URI, locator.getSystemId());
    assertEquals("The line number of the command was not correct.", 23, locator.getLineNumber());
  }
}
