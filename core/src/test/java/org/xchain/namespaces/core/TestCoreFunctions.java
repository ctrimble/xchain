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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
public class TestCoreFunctions 
{
  public static final String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/core/core-functions.xchain";
  public static final String XCHAIN_NAMESPACE_URI = "http://www.xchain.org/core/1.0";
  public static final QName SYSTEM_ID_FUNCTION = new QName(XCHAIN_NAMESPACE_URI, "system-id-function");
  public static final QName VALUE_OF_FUNCTION_WITHOUT_TYPE = new QName(XCHAIN_NAMESPACE_URI, "value-of-function-without-type");
  public static final QName VALUE_OF_FUNCTION_WITH_TYPE = new QName(XCHAIN_NAMESPACE_URI, "value-of-function-with-type");
  public static final String RESULT = "result";

  protected JXPathContext context = null;
  protected ContextBean contextBean = null;
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
    contextBean = new ContextBean();
    context = JXPathContext.newContext(contextBean);
    catalog = CatalogFactory.getInstance().getCatalog(CATALOG_URI);
  }
  
  @After public void tearDown() 
    throws Exception 
  {
      context = null;
  }
    
  @Test public void testSystemIdFunction() 
    throws Exception 
  {
    // Get the command.
    Command command = catalog.getCommand(SYSTEM_ID_FUNCTION);

    // execute the command.
    command.execute(context);
      
    // Get the result.
    String resultString = (String)context.getValue("$" + RESULT, String.class);
      
    assertEquals("The xchain:system-id() function failed.", CATALOG_URI, resultString);
  }  

  @Test public void testValueOfFunctionWithoutType()
    throws Exception
  {
    contextBean.setPath("1");
    Command command = catalog.getCommand(VALUE_OF_FUNCTION_WITHOUT_TYPE);
    command.execute(context);
    Object result = context.getValue("$" + RESULT);
    assertTrue("The result of xchain:value-of(xpath) is returning the wrong type of object.", result instanceof String );
    assertEquals("The result of xchain:value-of(xpath) returned the wrong value.", "1", result);
  }

  @Test public void testValueOfFunctionWithType()
    throws Exception
  {
    contextBean.setPath("1");
    Command command = catalog.getCommand(VALUE_OF_FUNCTION_WITH_TYPE);
    command.execute(context);
    Object result = context.getValue("$" + RESULT);
    assertTrue("The result of xchain:value-of(xpath) is returning the wrong type of object.", result instanceof Integer );
    assertEquals("The result of xchain:value-of(xpath) returned the wrong value.", new Integer(1), result);
  }

  public static class ContextBean
  {
    private Object path = null;

    public void setPath( Object path ) { this.path = path; }
    public Object getPath() { return this.path; }
  }
}
