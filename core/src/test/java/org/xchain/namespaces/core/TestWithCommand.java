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

import javax.xml.namespace.QName;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.xchain.Catalog;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.Command;
import org.xchain.framework.lifecycle.Lifecycle;

/**
 * @author Devon Tackett
 * @author Christian Trimble
 */
public class TestWithCommand {
  public static final String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/core/with-command.xchain";
  public static final String XCHAIN_NAMESPACE_URI = "http://www.xchain.org/core/1.0";
  public static final QName WITH_BASIC = new QName(XCHAIN_NAMESPACE_URI, "with-basic-test");
  public static final QName WITH_ROOT_SELECT = new QName(XCHAIN_NAMESPACE_URI, "with-root-select");
  public static final QName WITH_SCOPE = new QName(XCHAIN_NAMESPACE_URI, "with-scope");
  public static final QName CONTEXT_CLASS = new QName(XCHAIN_NAMESPACE_URI, "context-class");
  public static final QName CHILD_CONTEXT_CLASS = new QName(XCHAIN_NAMESPACE_URI, "child-context-class");
  public static final QName VARIABLE_CONTEXT_CLASS = new QName(XCHAIN_NAMESPACE_URI, "variable-context-class");
  public static final String RESULT = "result";
  public static final String RESULT_PERSON = "person-result";
  public static final String FINAL_CHAIN_RESULT = "final-chain-result";

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
    ChildObject child = new ChildObject("child");
    ParentObject parent = new ParentObject(child);

    context = JXPathContext.newContext(parent);
    catalog = CatalogFactory.getInstance().getCatalog(CATALOG_URI);
  }

  @After public void tearDown() 
    throws Exception 
  {
    context = null;
  }

  @Test public void testBasicWith()
    throws Exception
  {
    Person person = new Person("Joe");

    context.getVariables().declareVariable("testPerson", person);

    // Get the command.
    Command command = catalog.getCommand(WITH_BASIC);

    // execute the command.
    command.execute(context);

    // Get the result.
    String resultString = (String)context.getValue("$" + RESULT, String.class);

    assertEquals("Name not properly selected.", person.getName(), resultString);		
  }

  @Test public void testWithRootSelect()
    throws Exception
  {
    Person joe = new Person("Joe");

    context.getVariables().declareVariable("joe", joe);

    // Get the command.
    Command command = catalog.getCommand(WITH_ROOT_SELECT);

    // execute the command.
    command.execute(context);;

    // Get the result.
    Person personResult = (Person)context.getValue("$" + RESULT_PERSON, Person.class);

    assertEquals("Person not as expected.", joe, personResult);	
  }

  @Test public void testWithScope()
    throws Exception
  {
    Person joe = new Person("Joe");

    context.getVariables().declareVariable("joe", joe);

    // Get the command.
    Command command = catalog.getCommand(WITH_SCOPE);

    // execute the command.
    command.execute(context);

    // Get the result.
    String result = (String)context.getValue("$" + RESULT, String.class);
    String chainResult = (String)context.getValue("$" + FINAL_CHAIN_RESULT, String.class);

    assertEquals("Name not as expected.", result, joe.getName());		
    assertEquals("Chain result not as expected.", chainResult, "valid");		
  }

  @Ignore("This is broken in both JXPath 1.2 and JXPath 1.3 releases.")
  @Test public void testContextClass()
    throws Exception
  {
    Command command = catalog.getCommand(CONTEXT_CLASS);
    command.execute(context);
    Class result = (Class)context.getValue("$" + RESULT, Class.class);
    assertEquals("The wrong class was returned for the child context.", ParentObject.class, result);
  }

  @Ignore("This is broken in both JXPath 1.2 and JXPath 1.3 releases.")
  @Test public void testChildContextClass()
    throws Exception
  {
    Command command = catalog.getCommand(CHILD_CONTEXT_CLASS);
    command.execute(context);
    Class result = (Class)context.getValue("$" + RESULT, Class.class);
    assertEquals("The wrong class was returned for the child context.", ChildObject.class, result);
  }

  @Ignore("This is broken in both JXPath 1.2 and JXPath 1.3 releases.")
  @Test public void testVariableContextClass()
    throws Exception
  { 
    ChildObject child = new ChildObject("var-child");
    ParentObject parent = new ParentObject(child);
    context.getVariables().declareVariable("parent", parent);
    Command command = catalog.getCommand(VARIABLE_CONTEXT_CLASS);
    command.execute(context);
    Class result = (Class)context.getValue("$" + RESULT, Class.class);
    assertEquals("The wrong class was returned for the child context.", ParentObject.class, result);
  }

  public class Person {
    private String name;

    public Person(String name) {
      super();
      this.name = name;
    }		

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  public static class ParentObject
  {
    private ChildObject child;

    public ParentObject( ChildObject child )
    {
      this.child = child;
    }

    public ChildObject getChild()
    {
      return this.child;
    }
  }

  public static class ChildObject
  {
    private String name;

    public ChildObject( String name )
    {
      this.name = name;
    }

    public String getName()
    {
      return this.name;
    }
  }
}
