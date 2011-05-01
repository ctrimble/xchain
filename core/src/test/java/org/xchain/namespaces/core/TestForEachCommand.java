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

import java.util.ArrayList;
import java.util.List;

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
public class TestForEachCommand 
{
	public static final String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/core/for-each-command.xchain";
	public static final String XCHAIN_NAMESPACE_URI = "http://www.xchain.org/core/1.0";
	public static final String TEST_NAMESPACE_URI = "http://www.xchain.org/test";
	public static final String TEST_NAMESPACE = "different-name";
	public static final QName SELECT_COUNT_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "select-count-test");
	public static final QName RELATIVE_PATH = new QName(XCHAIN_NAMESPACE_URI, "relative-path");
	public static final QName ROOT_PATH = new QName(XCHAIN_NAMESPACE_URI, "root-path");
	public static final String COUNT_VARIABLE = "count";
        public static final String VALUE_VARIABLE = "value";
	public static final String TEST_LIST = "testList";
	
	private static final int MULTIPLE_ENTRY_COUNT = 99;

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
	    Node rootNode = new Node("root");
	    Node childNode = new Node("child");
	    rootNode.getChildList().add(childNode);
	    
	    context = JXPathContext.newContext(rootNode);
	    // Initialize the count variable to zero.
	    context.getVariables().declareVariable(COUNT_VARIABLE, 0);

	    catalog = CatalogFactory.getInstance().getCatalog(CATALOG_URI);
	}
	
	@After public void tearDown() 
		throws Exception 
	{
	    context = null;
	}
	  
	@Test public void testSelectSingle() 
		throws Exception 
	{
	    // Get the command.
	    Command command = catalog.getCommand(SELECT_COUNT_COMMAND);
	    
	    // Define the test list with a single entry.
	    List<Object> testList = new ArrayList<Object>();
	    testList.add(new Object());
	    context.getVariables().declareVariable(TEST_LIST, testList);

	    // execute the command.
	    command.execute(context);
	    
	    // Get the count.
	    int countValue = (Integer)context.getValue("$" + COUNT_VARIABLE, Integer.class);
	    
	    assertEquals("The count for list with single entry failed.", 1, countValue);
	}
	
	@Test public void testSelectMultiple() 
		throws Exception 
	{
	    // Get the command.
	    Command command = catalog.getCommand(SELECT_COUNT_COMMAND);
	    
	    // Define the test list with a single entry.
	    List<Object> testList = new ArrayList<Object>();
	    
	    // Add count entries.
	    for (int count = 0; count < MULTIPLE_ENTRY_COUNT; count++)
	    	testList.add(new Object());
	    
	    context.getVariables().declareVariable(TEST_LIST, testList);

	    // execute the command.
	    command.execute(context);
	    
	    // Get the count.
	    int countValue = (Integer)context.getValue("$" + COUNT_VARIABLE, Integer.class);
	    
	    assertEquals("The count for list with " + MULTIPLE_ENTRY_COUNT + " entries failed.", MULTIPLE_ENTRY_COUNT, countValue);
	}	
	
	@Test public void testSelectEmpty() 
		throws Exception 
	{
	    // Get the command.
	    Command command = catalog.getCommand(SELECT_COUNT_COMMAND);
	    
	    // Define the test list with a single entry.
	    List<Object> testList = new ArrayList<Object>();
	    context.getVariables().declareVariable(TEST_LIST, testList);

	    // execute the command.
	    command.execute(context);
	    
	    // Get the count.
	    int countValue = (Integer)context.getValue("$" + COUNT_VARIABLE, Integer.class);
	    
	    assertEquals("The count for list with no entries failed.", 0, countValue);
	}

  @Test public void testRelativePath()
    throws Exception
  {
    // Get the command.
    Command command = catalog.getCommand(RELATIVE_PATH);

    // execute the command.
    command.execute(context);
	    
    // get the value.
    String value = (String)context.getValue("$value", String.class);

    // assert that the value is the name of the child.
    assertEquals("A relative path did not point to the correct value.", "child", value);
  }

  @Test public void testRootPath()
    throws Exception
  {
    // Get the command.
    Command command = catalog.getCommand(ROOT_PATH);

    // execute the command.
    command.execute(context);
	    
    // get the value.
    String value = (String)context.getValue("$value", String.class);

    // assert that the value is the name of the child.
    assertEquals("A relative path did not point to the correct value.", "root", value);
  }

  public static class Node
  {
    protected String name = null;
    protected List<Node> childList = new ArrayList<Node>();

    public Node( String name ) { this.name = name; }

    public String getName() { return this.name; }
    public List<Node> getChildList() { return this.childList; }
  }
}
