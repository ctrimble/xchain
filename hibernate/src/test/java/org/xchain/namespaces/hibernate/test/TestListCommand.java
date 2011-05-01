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
package org.xchain.namespaces.hibernate.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

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
import org.xchain.namespaces.hibernate.test.om.Person;

/**
 * @author Devon Tackett
 * @author Christian Trimble
 */
public class TestListCommand
	extends BaseDatabaseTest
{
	private static final String RESULT = "result";
	
	private static final String catalogName = "resource://context-class-loader/org/xchain/namespaces/hibernate/test-list-command.xchain";
	protected JXPathContext context = null;
	protected Catalog catalog = null;	

	@BeforeClass public static void setupCommand()
	throws Exception
	{
		Lifecycle.startLifecycle();
		populatePersonData();
	}

	@AfterClass public static void teardownCommand()
	throws Exception
	{
		Lifecycle.stopLifecycle();
	}

	@Before public void setupTest()
	throws Exception
	{
		// get the catalog.
		catalog = CatalogFactory.getInstance().getCatalog(catalogName);

		// create the context.
		context = JXPathContext.newContext(new Object());
	}

	@After public void teardownTest() {
		context = null;
		catalog = null;
	}
	
	@Test public void testListCommandSimple()
	throws Exception
	{
		Command command = catalog.getCommand("test-list-command-simple");
		command.execute(context);
		
		// Get the result.
		String resultString = (String)context.getValue("$" + RESULT, String.class);
		
		assertEquals("First result wasn't as expected.", "Bob", resultString);
	}	
	
	@Test public void testListCommandSimpleVariable()
	throws Exception
	{
		Command command = catalog.getCommand("test-list-command-simple-variable");
		command.execute(context);
		
		// Get the result.
		String resultString = (String)context.getValue("$" + RESULT, String.class);
		
		assertEquals("First result wasn't as expected.", "Bob", resultString);
	}	
	
	@Test public void testListCommandParameters()
	throws Exception
	{
		Command command = catalog.getCommand("test-list-command-parameters");
		command.execute(context);
		
		// Get the result.
		String resultString = (String)context.getValue("$" + RESULT, String.class);
		
		assertEquals("Unable to find specified name.", "Sarah", resultString);
	}	
	
	@Test public void testListCommandEmpty()
	throws Exception
	{
		Command command = catalog.getCommand("test-list-command-empty");
		command.execute(context);
		
		// Get the result.
		List resultList = (List)context.getValue("$" + RESULT, ArrayList.class);
		
		assertEquals("Result list was not empty.", null, resultList);
	}		
	
	@Test public void testListCommandMaxResult()
	throws Exception
	{
		Command command = catalog.getCommand("test-list-command-max-result");
		command.execute(context);
		
		// Get the result.
		List<Person> resultList = (List<Person>)context.getValue("$" + RESULT, List.class);
		
		assertEquals("Result list not as expected.", 2, resultList.size());
	}	
	
	@Test public void testListCommandFirstResult()
	throws Exception
	{
		Command command = catalog.getCommand("test-list-command-first-result");
		command.execute(context);
		
		// Get the result.
		List<Person> resultList = (List<Person>)context.getValue("$" + RESULT, List.class);
		
		assertEquals("First result was not as expected.", "John", resultList.get(0).getName());
	}	
}
