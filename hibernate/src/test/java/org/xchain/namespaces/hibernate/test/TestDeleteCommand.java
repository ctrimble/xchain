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
import static org.junit.Assert.fail;

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
import org.xchain.framework.lifecycle.ExecutionException;
import org.xchain.namespaces.hibernate.test.om.Person;

/**
 * @author Devon Tackett
 * @author Christian Trimble
 */
public class TestDeleteCommand
	extends BaseDatabaseTest
{
	private static final String RESULT = "result";

	private static final String catalogName = "resource://context-class-loader/org/xchain/namespaces/hibernate/test-delete-command.xchain";
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

	@Test public void testDeleteCommand()
	throws Exception
	{
		Command command = catalog.getCommand("test-delete-command");
		command.execute(context);

		// Get the result.
		Person resultPerson = (Person)context.getValue("$" + RESULT, Person.class);

		assertEquals("Object was not deleted.", null, resultPerson);
	}		
	
	@Test public void testDeleteCommandNonExistent()
	throws Exception
	{
		Command command = catalog.getCommand("test-delete-command-non-existent");

                try {
		  command.execute(context);
                  fail("This command should have thrown an exception.");
                }
                catch( ExecutionException ee ) {
                  assertEquals("Expected a StaleStateException.", org.hibernate.StaleStateException.class, ee.getCause().getClass());
                }
                catch( Exception e ) {
                  fail("Wrong exception thrown type thrown:"+e.getClass().getName());
                }
                
		
		// This is expected to throw a StaleStateException as the object does not exist.
	}	
}
