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

import static org.junit.Assert.fail;

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

/**
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class TestUnknownElement
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/core/unknown-element.xchain";
  public static String XCHAIN_NAMESPACE_URI = "http://www.xchain.org/core/1.0";
  public static QName UNKNOWN_ELEMENT = new QName(XCHAIN_NAMESPACE_URI, "unknown-element");

  protected JXPathContext context = null;
  
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
  }

  @After public void tearDown()
    throws Exception
  {
    context = null;
  }

  @Test public void testUnknownElement()
    throws Exception
  {
    try {
      Catalog catalog = CatalogFactory.getInstance().getCatalog(CATALOG_URI);

      // get the command.
      Command command = catalog.getCommand(UNKNOWN_ELEMENT);

      // execute the command.
      command.execute(context);

      fail("The catalog should have failed to load due to an unknown tag.");
    }
    catch( Exception e ) {
      // this should happen.
    }
  }
}
