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
package org.xchain.namespaces.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.HashMap;
import javax.xml.namespace.QName;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xchain.Catalog;
import org.xchain.Command;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.framework.jxpath.QNameVariables;
import org.xchain.framework.lifecycle.Lifecycle;

/**
 * @author Christian Trimble
 */
public class TestPathNotFound
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/test/path-not-found.xchain";
  public static String NAMESPACE_URI = "http://www.xchain.org/test/1.0";
  public static QName UNDEFINED_VARIABLE = new QName(NAMESPACE_URI, "undefined-variable");
  public static QName UNDEFINED_VARIABLE_PATH = new QName(NAMESPACE_URI, "undefined-variable-path");
  public static QName UNDEFINED_PATH = new QName(NAMESPACE_URI, "undefined-path");
  public static enum HandlingType { NULL, EXCEPTION }
  public static Map<QName, HandlingType> handlingTypeMap = new HashMap<QName, HandlingType>();
  static {
    handlingTypeMap.put(UNDEFINED_VARIABLE, HandlingType.EXCEPTION);
    handlingTypeMap.put(UNDEFINED_VARIABLE_PATH, HandlingType.NULL);
    handlingTypeMap.put(UNDEFINED_PATH, HandlingType.NULL);
  }

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
    context.getVariables().declareVariable("variable", new Object());
    catalog = CatalogFactory.getInstance().getCatalog(CATALOG_URI);
  }

  @After public void tearDown()
    throws Exception
  {
    context = null;
  }

  @Test public void testNullVsException()
    throws Exception
  {
    for( Map.Entry<QName, HandlingType> entry : handlingTypeMap.entrySet() ) {
      testUndefinedPath(entry.getKey(), entry.getValue());
    }
  }

  @Test public void testBoolenTestDefinedVariable()
  {
    try {
      Boolean value = (Boolean)context.getValue( "boolean($variable and $variable/path)", Boolean.class );
      assertNotNull("The value was null.", value);
      assertFalse("The value was not true.", value);
    }
    catch( Exception e ) {
      e.printStackTrace();
      fail("Testing an existing variable caused an exception.");
    }
  }

  /**
   * 
   */
  private void testUndefinedPath(QName commandName, HandlingType handlingType )
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(commandName);

    try {
      // execute the command.
      command.execute(context);

      // if we got here, then we should fail.
      if( HandlingType.EXCEPTION == handlingType ) {
        fail("The command '"+commandName+"' should have thrown an exception.");
      }
    }
    catch( Exception e ) {
      if( HandlingType.NULL == handlingType ) {
        fail("The command '"+commandName+"' should not have thrown an exception.");
      }
    }
  }
}
