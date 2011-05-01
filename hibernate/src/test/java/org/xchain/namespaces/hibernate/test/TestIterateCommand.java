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

import static org.junit.Assert.assertTrue;

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
public class TestIterateCommand
	extends BaseDatabaseTest
{
	private static final String catalogName = "resource://context-class-loader/org/xchain/namespaces/hibernate/test-iterate-command.xchain";
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

	@Test public void testIterateCommand()
		throws Exception
	{
		context.getVariables().declareVariable("source-iterator", personList.iterator());
		Command command = catalog.getCommand("test-iterate-command");
		command.execute(context);
		
		Boolean result = (Boolean)context.getValue("$result", Boolean.class);
		
		assertTrue("The iterators were not equal.", result);
	}
	
	@Test public void testIterateCommandEmpty()
		throws Exception
	{
		Command command = catalog.getCommand("test-iterate-command-empty");
		command.execute(context);

		Boolean result = (Boolean)context.getValue("$result", Boolean.class);

		assertTrue("The iterator was not empty.", result);
	}	
}
