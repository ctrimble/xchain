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
package org.xchain.framework.lifecycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

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
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class ExecutionTraceTest
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/framework/lifecycle/execution-trace.xchain";
  public static String NAMESPACE_URI = "http://www.xchain.org/test/1.0";
  public static QName ONE_ENTRY = new QName(NAMESPACE_URI, "one-entry");
  public static QName ONE_ENTRY_NESTED = new QName(NAMESPACE_URI, "one-entry-nested");
  public static QName TWO_ENTRIES = new QName(NAMESPACE_URI, "two-entries");
  public static QName TWO_ENTRIES_NESTED = new QName(NAMESPACE_URI, "two-entries-nested");
  public static QName ONE_ENTRY_THROWN = new QName(NAMESPACE_URI, "one-entry-thrown");
  public static QName ONE_ENTRY_THROWN_NESTED = new QName(NAMESPACE_URI, "one-entry-thrown-nested");
  public static QName TWO_ENTRIES_THROWN = new QName(NAMESPACE_URI, "two-entries-thrown");
  public static QName TWO_ENTRIES_THROWN_NESTED = new QName(NAMESPACE_URI, "two-entries-thrown-nested");

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

  /**
   * Tests a literal enumeration with its first value.
   */
  @Test public void testOneEntry()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(ONE_ENTRY);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    List<ExecutionTraceElement> executionTrace = (List<ExecutionTraceElement>)context.getValue("$result", List.class);

    assertEquals("The wrong number of trace elements were found.", 1, executionTrace.size());
  }

  @Test public void testOneEntryNested()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(ONE_ENTRY_NESTED);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    List<ExecutionTraceElement> executionTrace = (List<ExecutionTraceElement>)context.getValue("$result", List.class);

    assertEquals("The wrong number of trace elements were found.", 1, executionTrace.size());
  }

  @Test public void testTwoEntries()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(TWO_ENTRIES);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    List<ExecutionTraceElement> executionTrace = (List<ExecutionTraceElement>)context.getValue("$result", List.class);

    assertEquals("The wrong number of trace elements were found.", 2, executionTrace.size());
  }

  @Test public void testTwoEntriesNested()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(TWO_ENTRIES_NESTED);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    List<ExecutionTraceElement> executionTrace = (List<ExecutionTraceElement>)context.getValue("$result", List.class);

    //printExecutionTrace(executionTrace);

    assertEquals("The wrong number of trace elements were found.", 2, executionTrace.size());
  }

  @Test public void testOneEntryThrown()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(ONE_ENTRY_THROWN);

    try {
      command.execute( context );

      fail("No exception thrown.");
    }
    catch( ExecutionException ee ) {
      // get the execution trace.
      List<ExecutionTraceElement> executionTrace = ee.getExecutionTrace();

      // assert that there is one element.
      assertEquals("There was the wrong number of trace elements.", 1, executionTrace.size());
    }
    catch( Exception e ) {
      fail("The exception was not wrapped:"+e.getClass().getName());
    }
  }

  @Test public void testOneEntryThrownNested()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(ONE_ENTRY_THROWN_NESTED);

    try {
      command.execute( context );

      fail("No exception thrown.");
    }
    catch( ExecutionException ee ) {
      // get the execution trace.
      List<ExecutionTraceElement> executionTrace = ee.getExecutionTrace();

      // assert that there is one element.
      assertEquals("There was the wrong number of trace elements.", 1, executionTrace.size());
    }
    catch( Exception e ) {
      fail("The exception was not wrapped:"+e.getClass().getName());
    }
  }

  @Test public void testTwoEntriesThrown()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(TWO_ENTRIES_THROWN);

    try {
      command.execute( context );

      fail("No exception thrown.");
    }
    catch( ExecutionException ee ) {
      // get the execution trace.
      List<ExecutionTraceElement> executionTrace = ee.getExecutionTrace();

      // assert that there is one element.
      assertEquals("There was the wrong number of trace elements.", 2, executionTrace.size());
    }
    catch( Exception e ) {
      e.printStackTrace();
      fail("The exception was not wrapped:"+e.getClass().getName());
    }
  }

  @Test public void testTwoEntriesThrownNested()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(TWO_ENTRIES_THROWN_NESTED);

    try {
      command.execute( context );

      fail("No exception thrown.");
    }
    catch( ExecutionException ee ) {
      // get the execution trace.
      List<ExecutionTraceElement> executionTrace = ee.getExecutionTrace();

      // assert that there is one element.
      assertEquals("There was the wrong number of trace elements.", 2, executionTrace.size());

      //printExecutionTrace(executionTrace);
    }
    catch( Exception e ) {
      e.printStackTrace();
      fail("The exception was not wrapped:"+e.getClass().getName());
    }
  }

  private static void printExecutionTrace( List<ExecutionTraceElement> executionTrace )
    throws Exception
  {
    for( ExecutionTraceElement element : executionTrace ) {
      System.out.println(element.getSystemId()+":"+element.getQName()+":"+locatorToString(element.getLocator()));
    }
  }

  private static String locatorToString( Locator locator )
  {
    if( locator != null ) {
      return "["+locator.getSystemId()+":"+locator.getLineNumber()+":"+locator.getColumnNumber()+"]";
    }
    else {
      return "<no locator>";
    }
  }
}
