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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xchain.Catalog;
import org.xchain.Command;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.framework.lifecycle.Lifecycle;
import org.xchain.namespaces.hibernate.test.om.Alphabet;
import org.xchain.namespaces.hibernate.test.om.Person;
import org.xchain.namespaces.hibernate.test.om.User;

/**
 * @author Devon Tackett
 * @author Christian Trimble
 */
public class TestGetCommand
	extends BaseDatabaseTest
{
	private static final String RESULT = "result";

	private static final String catalogName = "resource://context-class-loader/org/xchain/namespaces/hibernate/test-get-command.xchain";
	protected JXPathContext context = null;
	protected Catalog catalog = null;	

	@BeforeClass public static void setupCommand()
	throws Exception
	{
		Lifecycle.startLifecycle();
		// Populate person data.  People are keyed with an Integer.
		populatePersonData();
		populateAlphabetData();
		populateUserData();
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

	@Test public void testGetCommandPersonVariableLong()
	throws Exception
	{
		long id = 4;
		context.getVariables().declareVariable("id", id);
		Command command = catalog.getCommand("test-get-command-person-variable");
		command.execute(context);

		// Get the result.
		Person resultPerson = (Person)context.getValue("$" + RESULT, Person.class);

		assertEquals("Result wasn't as expected.", "Mary", resultPerson.getName());
	}
	
	@Test public void testGetCommandPersonVariableInteger()
	throws Exception
	{
		int id = 4;
		context.getVariables().declareVariable("id", id);
		Command command = catalog.getCommand("test-get-command-person-variable");
		command.execute(context);

		// Get the result.
		Person resultPerson = (Person)context.getValue("$" + RESULT, Person.class);

		assertEquals("Result wasn't as expected.", "Mary", resultPerson.getName());
	}	
	
	@Test public void testGetCommandPersonVariableString()
	throws Exception
	{
		String id = "4";
		context.getVariables().declareVariable("id", id);
		Command command = catalog.getCommand("test-get-command-person-variable");
		command.execute(context);

		// Get the result.
		Person resultPerson = (Person)context.getValue("$" + RESULT, Person.class);

		assertEquals("Result wasn't as expected.", "Mary", resultPerson.getName());
	}	
	
	@Test public void testGetCommandPersonValue()
	throws Exception
	{
		Command command = catalog.getCommand("test-get-command-person-value");
		command.execute(context);

		// Get the result.
		Person resultPerson = (Person)context.getValue("$" + RESULT, Person.class);

		assertEquals("Result wasn't as expected.", "Mary", resultPerson.getName());
	}	
	
	@Test public void testGetCommandPersonString()
	throws Exception
	{
		Command command = catalog.getCommand("test-get-command-person-string");
		command.execute(context);

		// Get the result.
		Person resultPerson = (Person)context.getValue("$" + RESULT, Person.class);

		assertEquals("Result wasn't as expected.", "Mary", resultPerson.getName());
	}		
	
	@Test public void testGetCommandPersonNonExistent()
	throws Exception
	{
		Command command = catalog.getCommand("test-get-command-person-non-existent");
		command.execute(context);

		// Get the result.
		Person resultPerson = (Person)context.getValue("$" + RESULT, Person.class);

		assertEquals("Result wasn't as expected.", null, resultPerson);
	}	
	
	@Test public void testGetCommandAlphabetVariableString()
	throws Exception
	{
		String id = "a";
		context.getVariables().declareVariable("id", id);
		Command command = catalog.getCommand("test-get-command-alphabet-variable");
		command.execute(context);

		// Get the result.
		Alphabet resultAlphabet = (Alphabet)context.getValue("$" + RESULT, Alphabet.class);

		assertEquals("Result wasn't as expected.", "Alphabet a", resultAlphabet.getName());
	}
	
	@Test public void testGetCommandAlphabetVariableCharacter()
	throws Exception
	{
		Character id = 'a';
		context.getVariables().declareVariable("id", id);
		Command command = catalog.getCommand("test-get-command-alphabet-variable");
		command.execute(context);

		// Get the result.
		Alphabet resultAlphabet = (Alphabet)context.getValue("$" + RESULT, Alphabet.class);

		assertEquals("Result wasn't as expected.", "Alphabet a", resultAlphabet.getName());
	}	
	
	@Test public void testGetCommandAlphabetString()
	throws Exception
	{
		Command command = catalog.getCommand("test-get-command-alphabet-string");
		command.execute(context);

		// Get the result.
		Alphabet resultAlphabet = (Alphabet)context.getValue("$" + RESULT, Alphabet.class);

		assertEquals("Result wasn't as expected.", "Alphabet a", resultAlphabet.getName());
	}	
	
	@Test public void testGetCommandUserVariableString()
	throws Exception
	{
		String id = "batman";
		context.getVariables().declareVariable("id", id);
		Command command = catalog.getCommand("test-get-command-user-variable");
		command.execute(context);

		// Get the result.
		User resultUser = (User)context.getValue("$" + RESULT, User.class);

		assertEquals("Result wasn't as expected.", "Bruce", resultUser.getName());
	}	
	
	@Test public void testGetCommandUserString()
	throws Exception
	{
		Command command = catalog.getCommand("test-get-command-user-string");
		command.execute(context);

		// Get the result.
		User resultUser = (User)context.getValue("$" + RESULT, User.class);

		assertEquals("Result wasn't as expected.", "Bruce", resultUser.getName());
	}	
}
