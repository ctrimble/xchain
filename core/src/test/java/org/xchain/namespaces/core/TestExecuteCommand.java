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
import static org.junit.Assert.assertEquals;

/**
 * @author Devon Tackett
 * @author Christian Trimble
 */
public class TestExecuteCommand 
{
	public static final String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/core/execute-command.xchain";
	public static final String XCHAIN_NAMESPACE_URI = "http://www.xchain.org/core/1.0";
	public static final QName EXTERNAL_EXECUTE_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "execute-command-external");
	public static final QName INTERNAL_EXECUTE_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "execute-command-internal");
	public static final QName DEEP_INTERNAL_EXECUTE_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "execute-command-deep-internal");
	public static final QName RELATIVE_EXECUTE_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "execute-command-relative");
	public static final QName TRANSLATED_EXECUTE_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "execute-command-translated");
	public static final String RESULT = "result";
	public static final String RESULT_VALUE_INITIAL = "initial";
	public static final String RESULT_VALUE_EXTERNAL = "external";
	public static final String RESULT_VALUE_INTERNAL = "internal";

	protected JXPathContext context = null;
	protected Catalog catalog = null;
	protected Command command = null;
	
	@BeforeClass public static void setupLifecycle()
		throws Exception {
		Lifecycle.startLifecycle();
	}

	@Before public void setUp() 
		throws Exception 
	{
		context = JXPathContext.newContext(new Object());
		
		// Declare the result to the initial value.
		context.getVariables().declareVariable(RESULT, RESULT_VALUE_INITIAL);

	    catalog = CatalogFactory.getInstance().getCatalog(CATALOG_URI);
	}
	
	@AfterClass public static void tearDownLifecycle()
		throws Exception {
	    Lifecycle.stopLifecycle();		
	}
	
	@After public void tearDown() 
		throws Exception 
	{
	    context = null;
	}
	  
	@Test public void testInternal() 
		throws Exception 
	{
	    // Get the command.
	    Command command = catalog.getCommand(INTERNAL_EXECUTE_COMMAND);

	    // execute the command.
	    command.execute(context);
	    
	    // Get the result.
	    String resultString = (String)context.getValue("$" + RESULT, String.class);
	    
	    assertEquals("A execute command to an internal command chain did not execute as expected.", RESULT_VALUE_INTERNAL, resultString);
	}	
	
	@Test public void testExternal() 
		throws Exception 
	{
	    // Get the command.
	    Command command = catalog.getCommand(EXTERNAL_EXECUTE_COMMAND);
	
	    // execute the command.
	    command.execute(context);
	    
	    // Get the result.
	    String resultString = (String)context.getValue("$" + RESULT, String.class);
	    
	    assertEquals("A execute command to an external command chain did not execute as expected.", RESULT_VALUE_EXTERNAL, resultString);
	}	

  @Test public void testRelativeSystemId()
    throws Exception
  {
    // Get the command.
    Command command = catalog.getCommand(RELATIVE_EXECUTE_COMMAND);

    // execute the command.
    command.execute(context);

    // Get the result.
    String resultString = (String)context.getValue("$" + RESULT, String.class);

    assertEquals("A execute command to an external command chain did not execute as expected.", RESULT_VALUE_EXTERNAL, resultString);
  }

  @Test public void testDeepInternal() 
    throws Exception 
  {
    // Get the command.
    Command command = catalog.getCommand(DEEP_INTERNAL_EXECUTE_COMMAND);

    // execute the command.
    command.execute(context);
	    
    // Get the result.
    String resultString = (String)context.getValue("$" + RESULT, String.class);
	    
    assertEquals("A execute command to a deep internal command chain did not execute as expected.", RESULT_VALUE_INTERNAL, resultString);
 }	

  @Test public void testTranslated() 
    throws Exception 
  {
    // Get the command.
    Command command = catalog.getCommand(TRANSLATED_EXECUTE_COMMAND);

    // execute the command.
    command.execute(context);

    // Get the result.
    String resultString = (String)context.getValue("$" + RESULT, String.class);

    assertEquals("A execute command to an internal command chain did not execute as expected.", RESULT_VALUE_INTERNAL, resultString);
  }	
}
