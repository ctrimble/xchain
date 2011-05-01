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
import org.junit.Test;
import org.xchain.Catalog;
import org.xchain.Command;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.framework.lifecycle.Lifecycle;

/**
 * @author Christian Trimble
 * @author Mike Moulton
 * @author Devon Tackett
 */
public class TestChooseCommand
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/core/choose-command.xchain";
  public static String XCHAIN_NAMESPACE_URI = "http://www.xchain.org/core/1.0";
  public static String TEST_NAMESPACE_URI = "http://www.xchain.org/test";
  private static final String TEST_NAMESPACE = "different_namespace";
  public static QName TOP_LEVEL_CHOOSE_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "test-top-level");
  public static QName NESTED_CHOOSE_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "test-nested");
  public static QName PREFIX_MAPPING_CHOOSE_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "test-prefix-mapping");

  protected JXPathContext context = null;
  protected Catalog catalog = null;
  protected Command command = null;

  @Before public void setUp()
    throws Exception
  {
    context = JXPathContext.newContext(new Object());
    context.getVariables().declareVariable("call-first-when", Boolean.FALSE);
    context.getVariables().declareVariable("call-second-when", Boolean.FALSE);
    context.getVariables().declareVariable("first-when-result", Boolean.FALSE);
    context.getVariables().declareVariable("second-when-result", Boolean.FALSE);
    context.getVariables().declareVariable("otherwise-result", Boolean.FALSE);
    context.registerNamespace(TEST_NAMESPACE, TEST_NAMESPACE_URI);
    context.getVariables().declareVariable(TEST_NAMESPACE + ":call-first-when", Boolean.FALSE);
    context.getVariables().declareVariable(TEST_NAMESPACE + ":call-second-when", Boolean.FALSE);
    context.getVariables().declareVariable(TEST_NAMESPACE + ":first-when-result", Boolean.FALSE);
    context.getVariables().declareVariable(TEST_NAMESPACE + ":second-when-result", Boolean.FALSE);
    context.getVariables().declareVariable(TEST_NAMESPACE + ":otherwise-result", Boolean.FALSE);
    context.registerNamespace(TEST_NAMESPACE, null);
    catalog = CatalogFactory.getInstance().getCatalog(CATALOG_URI);	
/**
    Map map = catalog.getCommandMap();
    for( Object key : map.keySet() ) {
      System.out.println("IF COMMAND KEY"+key);
    }
**/
  }
  
  @BeforeClass public static void setupLifecycle()
  	throws Exception
  {
	    Lifecycle.startLifecycle();  
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

  @Test public void testTopLevelChooseFirstWhen()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(TOP_LEVEL_CHOOSE_COMMAND);

    // set the call-first-when variable to true.
    context.setValue("$call-first-when", Boolean.TRUE);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    Boolean firstWhenResult = (Boolean)context.getValue("$first-when-result", Boolean.class);
    Boolean secondWhenResult = (Boolean)context.getValue("$second-when-result", Boolean.class);
    Boolean otherwiseResult = (Boolean)context.getValue("$otherwise-result", Boolean.class);

    assertEquals("The first when clause did not execute when it should have.", Boolean.TRUE, firstWhenResult);
    assertEquals("The second when clause did execute when it should not have.", Boolean.FALSE, secondWhenResult);
    assertEquals("The otherwise clause did execute when it should not have.", Boolean.FALSE, otherwiseResult);
  }

  @Test public void testTopLevelChooseSecondWhen()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(TOP_LEVEL_CHOOSE_COMMAND);

    // set the call-first-when variable to true.
    context.setValue("$call-second-when", Boolean.TRUE);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    Boolean firstWhenResult = (Boolean)context.getValue("$first-when-result", Boolean.class);
    Boolean secondWhenResult = (Boolean)context.getValue("$second-when-result", Boolean.class);
    Boolean otherwiseResult = (Boolean)context.getValue("$otherwise-result", Boolean.class);

    assertEquals("The first when clause did not execute when it should have.", Boolean.FALSE, firstWhenResult);
    assertEquals("The second when clause did execute when it should not have.", Boolean.TRUE, secondWhenResult);
    assertEquals("The otherwise clause did execute when it should not have.", Boolean.FALSE, otherwiseResult);
  }

  @Test public void testTopLevelChooseOtherwise()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(TOP_LEVEL_CHOOSE_COMMAND);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    Boolean firstWhenResult = (Boolean)context.getValue("$first-when-result", Boolean.class);
    Boolean secondWhenResult = (Boolean)context.getValue("$second-when-result", Boolean.class);
    Boolean otherwiseResult = (Boolean)context.getValue("$otherwise-result", Boolean.class);

    assertEquals("The first when clause did not execute when it should have.", Boolean.FALSE, firstWhenResult);
    assertEquals("The second when clause did execute when it should not have.", Boolean.FALSE, secondWhenResult);
    assertEquals("The otherwise clause did execute when it should not have.", Boolean.TRUE, otherwiseResult);
  }

  @Test public void testNestedChooseFirstWhen()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(NESTED_CHOOSE_COMMAND);

    // set the call-first-when variable to true.
    context.setValue("$call-first-when", Boolean.TRUE);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    Boolean firstWhenResult = (Boolean)context.getValue("$first-when-result", Boolean.class);
    Boolean secondWhenResult = (Boolean)context.getValue("$second-when-result", Boolean.class);
    Boolean otherwiseResult = (Boolean)context.getValue("$otherwise-result", Boolean.class);

    assertEquals("The first when clause did not execute when it should have.", Boolean.TRUE, firstWhenResult);
    assertEquals("The second when clause did execute when it should not have.", Boolean.FALSE, secondWhenResult);
    assertEquals("The otherwise clause did execute when it should not have.", Boolean.FALSE, otherwiseResult);
  }

  @Test public void testNestedChooseSecondWhen()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(NESTED_CHOOSE_COMMAND);

    // set the call-first-when variable to true.
    context.setValue("$call-second-when", Boolean.TRUE);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    Boolean firstWhenResult = (Boolean)context.getValue("$first-when-result", Boolean.class);
    Boolean secondWhenResult = (Boolean)context.getValue("$second-when-result", Boolean.class);
    Boolean otherwiseResult = (Boolean)context.getValue("$otherwise-result", Boolean.class);

    assertEquals("The first when clause did not execute when it should have.", Boolean.FALSE, firstWhenResult);
    assertEquals("The second when clause did execute when it should not have.", Boolean.TRUE, secondWhenResult);
    assertEquals("The otherwise clause did execute when it should not have.", Boolean.FALSE, otherwiseResult);
  }

  @Test public void testNestedChooseOtherwise()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(NESTED_CHOOSE_COMMAND);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    Boolean firstWhenResult = (Boolean)context.getValue("$first-when-result", Boolean.class);
    Boolean secondWhenResult = (Boolean)context.getValue("$second-when-result", Boolean.class);
    Boolean otherwiseResult = (Boolean)context.getValue("$otherwise-result", Boolean.class);

    assertEquals("The first when clause did not execute when it should have.", Boolean.FALSE, firstWhenResult);
    assertEquals("The second when clause did execute when it should not have.", Boolean.FALSE, secondWhenResult);
    assertEquals("The otherwise clause did execute when it should not have.", Boolean.TRUE, otherwiseResult);
  }

  @Test public void testPrefixMappingFirstWhen()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(PREFIX_MAPPING_CHOOSE_COMMAND);

    // set the call-first-when variable to true.
    context.registerNamespace(TEST_NAMESPACE, TEST_NAMESPACE_URI);
    context.setValue("$" + TEST_NAMESPACE + ":call-first-when", Boolean.TRUE);
    context.registerNamespace(TEST_NAMESPACE, null);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    context.registerNamespace(TEST_NAMESPACE, TEST_NAMESPACE_URI);
    Boolean firstWhenResult = (Boolean)context.getValue("$" + TEST_NAMESPACE + ":first-when-result", Boolean.class);
    Boolean secondWhenResult = (Boolean)context.getValue("$" + TEST_NAMESPACE + ":second-when-result", Boolean.class);
    Boolean otherwiseResult = (Boolean)context.getValue("$" + TEST_NAMESPACE + ":otherwise-result", Boolean.class);
    context.registerNamespace(TEST_NAMESPACE, null);

    assertEquals("The first when clause did not execute when it should have.", Boolean.TRUE, firstWhenResult);
    assertEquals("The second when clause did execute when it should not have.", Boolean.FALSE, secondWhenResult);
    assertEquals("The otherwise clause did execute when it should not have.", Boolean.FALSE, otherwiseResult);
  }

  @Test public void testPrefixMappingChooseSecondWhen()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(PREFIX_MAPPING_CHOOSE_COMMAND);

    // set the call-first-when variable to true.
    context.registerNamespace(TEST_NAMESPACE, TEST_NAMESPACE_URI);
    context.setValue("$" + TEST_NAMESPACE + ":call-second-when", Boolean.TRUE);
    context.registerNamespace(TEST_NAMESPACE, null);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    context.registerNamespace(TEST_NAMESPACE, TEST_NAMESPACE_URI);
    Boolean firstWhenResult = (Boolean)context.getValue("$" + TEST_NAMESPACE + ":first-when-result", Boolean.class);
    Boolean secondWhenResult = (Boolean)context.getValue("$" + TEST_NAMESPACE + ":second-when-result", Boolean.class);
    Boolean otherwiseResult = (Boolean)context.getValue("$" + TEST_NAMESPACE + ":otherwise-result", Boolean.class);
    context.registerNamespace(TEST_NAMESPACE, null);

    assertEquals("The first when clause did not execute when it should have.", Boolean.FALSE, firstWhenResult);
    assertEquals("The second when clause did execute when it should not have.", Boolean.TRUE, secondWhenResult);
    assertEquals("The otherwise clause did execute when it should not have.", Boolean.FALSE, otherwiseResult);
  }

  @Test public void testPrefixMappingChooseOtherwise()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(PREFIX_MAPPING_CHOOSE_COMMAND);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    context.registerNamespace(TEST_NAMESPACE, TEST_NAMESPACE_URI);
    Boolean firstWhenResult = (Boolean)context.getValue("$" + TEST_NAMESPACE + ":first-when-result", Boolean.class);
    Boolean secondWhenResult = (Boolean)context.getValue("$" + TEST_NAMESPACE + ":second-when-result", Boolean.class);
    Boolean otherwiseResult = (Boolean)context.getValue("$" + TEST_NAMESPACE + ":otherwise-result", Boolean.class);
    context.registerNamespace(TEST_NAMESPACE, null);

    assertEquals("The first when clause did not execute when it should have.", Boolean.FALSE, firstWhenResult);
    assertEquals("The second when clause did execute when it should not have.", Boolean.FALSE, secondWhenResult);
    assertEquals("The otherwise clause did execute when it should not have.", Boolean.TRUE, otherwiseResult);
  }
}
