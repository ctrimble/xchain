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
import org.apache.commons.jxpath.CompiledExpression;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
import org.xchain.Catalog;
import org.xchain.Command;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.framework.lifecycle.Lifecycle;

/**
 * @author Christian Trimble
 */
public class TestUnknownFunction
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/core/unknown-function.xchain";
  public static String XCHAIN_NAMESPACE_URI = "http://www.xchain.org/core/1.0";
  public static QName DEFAULT_PREFIX = new QName(XCHAIN_NAMESPACE_URI, "default-prefix");

  protected JXPathContext context = null;
  protected Catalog catalog = null;
  
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

  @Ignore @Test public void testUnknownFunctionFromContext()
  {
    try {
      Object value = context.getValue("unknown()");
      fail("Unknown function should have thrown an exception.");
    }
    catch( Exception e ) {
      // success.
    }
  }

  @Ignore @Test public void testUnknownFunctionFromExpression()
  {
    try {
      CompiledExpression expression = JXPathContext.compile("unknown()");
      expression.getValue(context);
      fail("Unknown function should have thrown an exception.");
    }
    catch( Exception e ) {
      // success.
    }
  }

  @Test public void testDefaultPrefix()
    throws Exception
  {
    try {
      // get the command.
      Command command = catalog.getCommand(DEFAULT_PREFIX);

      // execute the command.
      command.execute(context);

      fail("The catalog should have failed to load due to an unknown function.");
    }
    catch( Exception e ) {
      e.printStackTrace();
      // this should happen.
    }
  }
}
