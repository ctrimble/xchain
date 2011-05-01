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
package org.xchain.test.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xchain.Catalog;
import org.xchain.CatalogNotFoundException;
import org.xchain.Command;
import org.xchain.CommandNotFoundException;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.framework.lifecycle.Lifecycle;
import org.xchain.framework.strategy.CachingLoadStrategy;
import org.xchain.framework.strategy.CatalogConsumerStrategy;
import org.xchain.framework.strategy.InputSourceSourceStrategy;
import org.xml.sax.InputSource;

/**
 * @author Christian Trimble
 * @author Mike Moulton
 * @author Devon Tackett
 */
public class TestBasic
{

  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/test/basic/basic.xchain";
  public static String COMMAND_NAME_1 = "test-basic-1";
  public static String VARIABLE_NAME = "TEST_BASIC_1";
  public static String VARIABLE_VALUE = "success";

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
  }

  @After public void tearDown()
    throws Exception
  {
    context = null;
  }
  
  @Test public void testCatalogLookup()
    throws Exception
  {
	  catalog = new CachingLoadStrategy<Catalog, InputSource>(10).getObject(CATALOG_URI, new InputSourceSourceStrategy(), new CatalogConsumerStrategy());
    assertNotNull("Catalog is null.", catalog);
  }  
  
  @Test (expected = CatalogNotFoundException.class) public void testUnknownUrlCatalogLookup()
    throws Exception
  {
    catalog = CatalogFactory.getInstance().getCatalog( "resource://context-class-loader/unknown" );    
  }
  
  @Test (expected = CatalogNotFoundException.class) public void testMalformedCatalogLookup()
    throws Exception
  {
    catalog = CatalogFactory.getInstance().getCatalog( "unknown" );
  }  
  
  @Test (expected = CommandNotFoundException.class) public void testUnknownCommandLookup()
    throws Exception
  {
    QName name = new QName(XMLConstants.NULL_NS_URI, "Unknown");
    catalog = CatalogFactory.getInstance().getCatalog( CATALOG_URI );
    command = catalog.getCommand(name);
  }  

  @Test public void testCommandLookup()
    throws Exception
  {
    QName name = new QName(XMLConstants.NULL_NS_URI, COMMAND_NAME_1);
    catalog = CatalogFactory.getInstance().getCatalog( CATALOG_URI );
    command = catalog.getCommand(name);
    assertNotNull("Command is null.", command);
  }
}
