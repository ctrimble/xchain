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
package org.xchain.framework.sax;

import static org.junit.Assert.assertEquals;

import javax.xml.namespace.QName;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xchain.Catalog;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.Command;
import org.xchain.framework.lifecycle.Lifecycle;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class TestXChainStylesheetDecl
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/framework/sax/xchain-stylesheet-decl.xchain";
  public static String XCHAIN_NAMESPACE_URI = "http://www.xchain.org/core/1.0";
  public static QName GENERATED = new QName(XCHAIN_NAMESPACE_URI, "generated");
  public static QName PROVIDED = new QName(XCHAIN_NAMESPACE_URI, "provided");
  public static QName RELATIVE_EXECUTE = new QName(XCHAIN_NAMESPACE_URI, "relative-execute");

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
    context.getVariables().declareVariable("result", Boolean.FALSE);
    catalog = CatalogFactory.getInstance().getCatalog(CATALOG_URI);
  }

  @After public void tearDown()
    throws Exception
  {
    context = null;
  }

  @Test public void testGeneratedCommand()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(GENERATED);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    Boolean value = (Boolean)context.getValue("$result", Boolean.class);

    assertEquals("The generated command was not found.", Boolean.TRUE, value);
  }

  @Test public void testProvidedCommand()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(PROVIDED);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    Boolean value = (Boolean)context.getValue("$result", Boolean.class);

    assertEquals("The provided command was not found.", Boolean.TRUE, value);
  }

  /*
  @Test public void testRelativeExecute()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(RELATIVE_EXECUTE);

    // execute the command.
    CommandUtil.execute( command, context );

    // get the value for the variable.
    Boolean value = (Boolean)context.getValue("$result", Boolean.class);

    assertEquals("The provided command was not found.", Boolean.TRUE, value);
  }
   */
}
