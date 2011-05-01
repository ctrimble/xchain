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
package org.xchain.namespaces.test;

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
import org.xchain.framework.jxpath.QNameVariables;
import org.xchain.framework.lifecycle.Lifecycle;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class TestAnnotationDefault
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/test/annotation-default.xchain";
  public static String NAMESPACE_URI = "http://www.xchain.org/test/1.0";
  public static QName ANNOTATION_DEFAULT = new QName(NAMESPACE_URI, "annotation-default");
  public static QName INPUT = new QName(NAMESPACE_URI, "input");
  public static QName RESULT = new QName(NAMESPACE_URI, "result");

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
  @Test public void testAnnotationDefault()
    throws Exception
  {
    ((QNameVariables)context.getVariables()).declareVariable(INPUT, "value");

    // get the command.
    Command command = catalog.getCommand(ANNOTATION_DEFAULT);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    context.registerNamespace("test", NAMESPACE_URI);
    String value = (String)context.getValue("$test:result", String.class);

    assertEquals("The attribute did not have the correct value.", "value", value);
  }
}
