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
public class TestFilter 
{
	public static final String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/core/test-filter.xchain";
	public static final String XCHAIN_NAMESPACE_URI = "http://www.xchain.org/core/1.0";
	public static final QName FILTER_IN_CHAIN_COMMAND_CHAIN = new QName(XCHAIN_NAMESPACE_URI, "filter-in-chain-test");
	public static final QName FILTER_IN_FILTER_CHAIN_COMMAND_CHAIN = new QName(XCHAIN_NAMESPACE_URI, "filter-in-filter-chain-test");
	public static final QName FILTER_IN_SUB_CHAIN_COMMAND_CHAIN = new QName(XCHAIN_NAMESPACE_URI, "filter-in-sub-chain-test");
	public static final QName FILTER_IN_FILTER_SUB_CHAIN_COMMAND_CHAIN = new QName(XCHAIN_NAMESPACE_URI, "filter-in-filter-sub-chain-test");
	public static final String RESULT_VARIABLE = "result";
	public static final String RESULT_VALUE = "value";
	public static final String RESULT_INITIAL_VALUE = "result_initial_value";
	public static final String FILTER_RESULT = "filter_result";
	public static final String FILTER_INITIAL_VALUE = "filter_initial_value";

	protected JXPathContext context = null;
	protected Catalog catalog = null;
	protected Command command = null;
	
	@BeforeClass public static void setUpLifecycle()
		throws Exception
	{
	    Lifecycle.startLifecycle();		
	}

	@Before public void setUp() 
		throws Exception 
	{
		context = JXPathContext.newContext(new Object());
		
		// Declare the result variables.
		context.getVariables().declareVariable(RESULT_VARIABLE, RESULT_INITIAL_VALUE);
		context.getVariables().declareVariable(FILTER_RESULT, FILTER_INITIAL_VALUE);
		
	    catalog = CatalogFactory.getInstance().getCatalog(CATALOG_URI);
	}
	
	@AfterClass public static void tearDownLifecycle()
		throws Exception
	{
	    Lifecycle.stopLifecycle();	
	}
	
	@After public void tearDown() 
		throws Exception 
	{
	    context = null;
	}
	  
	@Test public void testFilterInChain() 
		throws Exception 
	{
	    // Get the command chain.
	    Command command = catalog.getCommand(FILTER_IN_CHAIN_COMMAND_CHAIN);

	    // execute the command.
	    command.execute(context);
	    
	    // Get the result.
	    String resultString = (String)context.getValue("$" + RESULT_VARIABLE, String.class);
	    
	    assertEquals("The post process on a filter command did not run.", RESULT_VALUE, resultString);
	}
	
	@Test public void testFilterInFilterChain() 
		throws Exception 
	{
	    // Get the command chain.
	    Command command = catalog.getCommand(FILTER_IN_FILTER_CHAIN_COMMAND_CHAIN);
	
	    // execute the command.
	    command.execute(context);
	    
	    // Get the result.
	    String resultString = (String)context.getValue("$" + RESULT_VARIABLE, String.class);
	    
	    assertEquals("The post process on a filter command did not run.", RESULT_VALUE, resultString);
	}	
	
	@Test public void testFilterInSubChain()
		throws Exception
	{
	    // Get the command chain.
	    Command command = catalog.getCommand(FILTER_IN_SUB_CHAIN_COMMAND_CHAIN);
	
	    // execute the command.
	    command.execute(context);
	    
	    // Get the result.
	    String resultString = (String)context.getValue("$" + RESULT_VARIABLE, String.class);
	    String filterResultString = (String)context.getValue("$" + FILTER_RESULT, String.class);
	    
	    assertEquals("The post process on a filter command did not run as expected.", RESULT_VALUE, resultString);
	    assertEquals("The post process order on a filter in a sub chain did not execute as expected.", RESULT_VALUE, filterResultString);
	}
	
	@Test public void testFilterInFilterSubChain()
		throws Exception
	{
	    // Get the command chain.
	    Command command = catalog.getCommand(FILTER_IN_FILTER_SUB_CHAIN_COMMAND_CHAIN);
	
	    // execute the command.
	    command.execute(context);
	    
	    // Get the result.
	    String resultString = (String)context.getValue("$" + RESULT_VARIABLE, String.class);
	    String filterResultString = (String)context.getValue("$" + FILTER_RESULT, String.class);
	    
	    assertEquals("The post process on a filter command in a sub chain did not run as expected.", RESULT_VALUE, resultString);
	    assertEquals("The post process order on a filter in a sub chain did not execute as expected.", RESULT_INITIAL_VALUE, filterResultString);
	}	
}
