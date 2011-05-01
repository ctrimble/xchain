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
 * @author Devon Tackett
 * @author Christian Trimble
 */
public class TestVariableCommand 
{
	public static final String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/core/variable-command.xchain";
	public static final String XCHAIN_NAMESPACE_URI = "http://www.xchain.org/core/1.0";
	public static final QName DECLARE_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "delcare-test");
	public static final QName MODIFY_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "modify-test");
	public static final QName REQUEST_DECLARE_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "request-declare-test");
	public static final QName REQUEST_MODIFY_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "request-modify-test");
  public static final QName EXECUTION_SCOPE_DECLARE_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "execution-scope-declare-test");
  public static final QName EXECUTION_SCOPE_SELECT_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "execution-scope-select-test");
  public static final QName EXECUTION_SCOPE_MODIFY_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "execution-scope-modify-test");	
	public static final QName CHAIN_SCOPE_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "chain-scope-test");
	public static final QName CHAIN_MODIFY_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "chain-modify-test");
	public static final QName SELECT_COMPONENT_EXCEPTION_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "select-component-exception-test");
	public static final QName SELECT_FUNCTION_EXCEPTION_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "select-function-exception-test");
	public static final String RESULT = "result";

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
	  
	@Test public void testDeclare() 
		throws Exception 
	{
	    // Get the command.
	    Command command = catalog.getCommand(DECLARE_COMMAND);

	    // execute the command.
	    command.execute(context);
	    
	    // Get the result.
	    String resultString = (String)context.getValue("$" + RESULT, String.class);
	    
	    assertEquals("The declaration of a variable failed.", "string1", resultString);
	}	

	@Test public void testModify() 
		throws Exception 
	{
		// Declare the variable at the 'request' level.
		JXPathContext requestContext = context;
		// Dig up to the 'request' context.
		while (requestContext.getParentContext() != null)
			requestContext = requestContext.getParentContext();
		
		// Declare the variable.
		requestContext.getVariables().declareVariable(RESULT, "string1");		
		
		// Get the command.
		Command command = catalog.getCommand(MODIFY_COMMAND);
		
    // execute the command.
    command.execute(context);
		
		// Get the result.
		String resultString = (String)context.getValue("$" + RESULT, String.class);
		
		assertEquals("The modification of a declared variable failed.", "string2", resultString);
	}
	
	@Test public void testRequestDeclare() 
		throws Exception 
	{
	    // Get the command.
	    Command command = catalog.getCommand(REQUEST_DECLARE_COMMAND);

	    // execute the command.
	    command.execute(context);
	    
	    // Get the result.
	    String resultString = (String)context.getValue("$" + RESULT, String.class);
	    
	    assertEquals("The declaration of a variable failed.", "string1", resultString);		
	}
	
	@Test public void testRequestModify() 
		throws Exception 
	{
		// Declare the variable at the 'request' level.
		JXPathContext requestContext = context;
		// Dig up to the 'request' context.
		while (requestContext.getParentContext() != null)
			requestContext = requestContext.getParentContext();
		
		// Declare the variable.
		requestContext.getVariables().declareVariable(RESULT, "string1");		
		
		// Get the command.
		Command command = catalog.getCommand(REQUEST_MODIFY_COMMAND);
		
    // execute the command.
    command.execute(context);
		
		// Get the result.
		String resultString = (String)context.getValue("$" + RESULT, String.class);
		
		assertEquals("The modification of a declared variable failed.", "string2", resultString);
	}
	
	/**
	 * Test that a variable declared at the execution level does not exist outside of the execution.
	 */
	@Test public void testExecutionScopeDeclare()
	  throws Exception
  {   
    // Get the command.
    Command command = catalog.getCommand(EXECUTION_SCOPE_DECLARE_COMMAND);
    
    // execute the command.
    command.execute(context);

    // Assert that the variable does not exist.
    assertFalse("The scope of an execution level variable failed.", context.getVariables().isDeclaredVariable(RESULT));  
  }
	
	/**
	 * Test that an execution scope variable can be selected.
	 */
  @Test public void testExecutionScopeSelect()
    throws Exception
  {
    // Get the command.
    Command command = catalog.getCommand(EXECUTION_SCOPE_SELECT_COMMAND);

    // execute the command.
    command.execute(context);
    
    // Get the result.
    String resultString = (String)context.getValue("$" + RESULT, String.class);
    
    assertEquals("The selecting of a execution scope variable failed.", "execution-scope", resultString);       
  }	
	
  /**
   * Test that an execution scope variable can be modified.
   */
	@Test public void testExecutionScopeModify()
	  throws Exception
  {
    // Get the command.
    Command command = catalog.getCommand(EXECUTION_SCOPE_MODIFY_COMMAND);

    // execute the command.
    command.execute(context);
    
    // Get the result.
    String resultString = (String)context.getValue("$" + RESULT, String.class);
    
    assertEquals("The modification of a execution scope variable failed.", "final-value", resultString);     
  }
	
	@Test public void testChainScope() 
		throws Exception 
	{
	    // Get the command.
	    Command command = catalog.getCommand(CHAIN_SCOPE_COMMAND);

	    // execute the command.
	    command.execute(context);
	    
	    // Get the result.
	    String resultString = (String)context.getValue("$" + RESULT, String.class);
	    
	    assertEquals("The scoping of a chain variable failed.", "string1", resultString);			
	}
	
	@Test public void testChainModify() 
		throws Exception 
	{
	    // Get the command.
	    Command command = catalog.getCommand(CHAIN_MODIFY_COMMAND);

	    // execute the command.
	    command.execute(context);
	    
	    // Get the result.
	    String resultString = (String)context.getValue("$" + RESULT, String.class);
	    
	    assertEquals("The storing or selection of a chain variable failed.", "string2", resultString);		
	}

  /**
   * This case insures that a varialbe elements select attribute do not swollow component exceptions thrown in it.
   */
  @Test public void testSelectComponentException()
    throws Exception
  {
    Command command = catalog.getCommand(SELECT_COMPONENT_EXCEPTION_COMMAND);
    try {
      command.execute(context);
      fail("The variable command's select attribute is swollowing component exceptions.");
    }
    catch( Exception e ) {
      // success.
    }
  }

  /**
   * This case insures that a varialbe elements select attribute do not swollow function exceptions thrown in it.
   */
  @Test public void testSelectFunctionException()
    throws Exception
  {
    Command command = catalog.getCommand(SELECT_FUNCTION_EXCEPTION_COMMAND);
    try {
      command.execute(context);
      fail("The variable command's select attribute is swollowing function exceptions.");
    }
    catch( Exception e ) {
      // success.
    }
  }

}
