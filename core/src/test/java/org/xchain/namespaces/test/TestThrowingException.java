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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
 */
public class TestThrowingException
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/test/exception-test.xchain";
  public static String NAMESPACE_URI = "http://www.xchain.org/test/1.0";
  public static QName UNDECLARED_RUNTIME_EXCEPTION = new QName(NAMESPACE_URI, "undeclared-runtime-exception");
  public static QName DECLARED_RUNTIME_EXCEPTION = new QName(NAMESPACE_URI, "declared-runtime-exception");
  public static QName UNDECLARED_EXCEPTION = new QName(NAMESPACE_URI, "undeclared-exception");
  public static QName DECLARED_EXCEPTION = new QName(NAMESPACE_URI, "declared-exception");

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
    context.registerNamespace("test", NAMESPACE_URI);
    catalog = CatalogFactory.getInstance().getCatalog(CATALOG_URI);
  }

  @After public void tearDown()
    throws Exception
  {
    context = null;
  }

  protected <T extends Throwable> void testThrowingException( QName commandName, Class<T> expectedType )
    throws Exception
  {
    Command command = catalog.getCommand(commandName);
    command.execute(context);
    Throwable result = (Throwable)context.getValue("$test:result", Throwable.class);
    assertNotNull( "No throwable was thrown by command "+commandName+".");
    assertTrue( "Expected type castable to "+expectedType+", but found type "+result.getClass()+".", expectedType.isAssignableFrom(result.getClass()));
  }

  /**
   */
  @Test public void testUndeclaredRuntimeException()
    throws Exception
  {
    testThrowingException(UNDECLARED_RUNTIME_EXCEPTION, java.lang.SecurityException.class);
  }

  /**
   */
  @Test public void testDeclaredRuntimeException()
    throws Exception
  {
    testThrowingException(DECLARED_RUNTIME_EXCEPTION, java.lang.SecurityException.class);
  }

  /**
   */
  @Test public void testUndeclaredException()
    throws Exception
  {
    testThrowingException(UNDECLARED_EXCEPTION, org.apache.commons.jxpath.JXPathException.class);
  }

  /**
   */
  @Test public void testDeclaredException()
    throws Exception
  {
    testThrowingException(DECLARED_EXCEPTION, java.lang.ClassNotFoundException.class);
  }
}
