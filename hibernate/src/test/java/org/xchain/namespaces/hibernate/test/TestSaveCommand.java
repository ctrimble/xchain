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

import org.apache.commons.jxpath.JXPathContext;
import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xchain.Catalog;
import org.xchain.Command;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.framework.hibernate.HibernateLifecycle;
import org.xchain.framework.lifecycle.Lifecycle;
import org.xchain.framework.lifecycle.ExecutionException;
import org.xchain.namespaces.hibernate.test.command.TestException.ExpectedException;
import org.xchain.namespaces.hibernate.test.om.Person;

/**
 * @author Devon Tackett
 * @author Christian Trimble
 */
// TODO The save command is not exhaustively tested.
public class TestSaveCommand
	extends BaseDatabaseTest
{
	private static final String RESULT = "result";

	private static final String catalogName = "resource://context-class-loader/org/xchain/namespaces/hibernate/test-save-command.xchain";
	protected JXPathContext context = null;
	protected Catalog catalog = null;	

	@BeforeClass public static void setupCommand()
	throws Exception
	{
		Lifecycle.startLifecycle();
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

	/**
	 * Test saving outside of a transaction.
	 */
	@Test public void testSaveCommand()
	throws Exception
	{
		Person newPerson = new Person();
		newPerson.setName("Alfred");
		
		context.getVariables().declareVariable("person", newPerson);
		
		Command command = catalog.getCommand("test-save-command");
		command.execute(context);

		// Get the result.
		Person resultPerson = (Person)context.getValue("$" + RESULT, Person.class);

		assertEquals("Result wasn't as expected.", "Alfred", resultPerson.getName());
	}	

	/**
	 * Test saving inside of transaction.
	 */
	@Test public void testSaveCommandTransaction()
	throws Exception
	{
		Person newPerson = new Person();
		newPerson.setName("Kevin");
		
		context.getVariables().declareVariable("person", newPerson);
		
		Command command = catalog.getCommand("test-save-command-transaction");
		command.execute(context);

		// Get the result.
		Person resultPerson = (Person)context.getValue("$" + RESULT, Person.class);

		assertEquals("Result wasn't as expected.", "Kevin", resultPerson.getName());
	}		
	
	/**
	 * Test saving while inside a transaction but before an exception is thrown.
	 */
	@Test public void testSaveCommandRollback()
	throws Exception
	{
		Person newPerson = new Person();
		newPerson.setName("John");
		
		context.getVariables().declareVariable("person", newPerson);
		
		Command command = catalog.getCommand("test-save-command-rollback");
		try {
			command.execute(context);
		} catch (ExecutionException ignore) {
			// Ignore the ExcpectedException and continue on.
		}
		
		// Ensure that 'John' does not exist in Hibernate.
		Session session = null;
		
		try {
			session = HibernateLifecycle.getSessionFactory().openSession();
			Query personQuery = session.createQuery("from Person where name = :name");
			personQuery.setParameter("name", "John");
			Person person = (Person)personQuery.uniqueResult();
			
			assertEquals("The entity still exists.", person, null);
			
		} finally {
			if (session != null)
			session.close();
		}		
	}	
}
