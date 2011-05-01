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
package org.xchain.test.namespace;

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
 * @author Devon Tackett
 */
public class TestNamespaceMapping
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/test/namespace/namespace.xchain";
  public static String NAMESPACE_URI = "http://www.xchain.org/test/namespace";
  public static String TEST1_NAMESPACE_URI = "http://www.xchain.org/test/namespace/test1";
  public static String TEST2_NAMESPACE_URI = "http://www.xchain.org/test/namespace/test2";
  public static String XCHAIN_NAMESPACE_URI = "http://www.xchain.org/core/1.0";
  public static QName NAMESPACE_ON_TAG_COMMAND = new QName(NAMESPACE_URI, "test-namespaces-on-tag");
  public static QName NAMESPACE_ON_OUTER_TAG_COMMAND = new QName(NAMESPACE_URI, "test-namespace-on-outer-tag");
  public static QName NAMESPACE_UNMAPPED_COMMAND = new QName(NAMESPACE_URI, "test-namespace-unmapped");

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

  @Test public void testNamespaceOnTag()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(NAMESPACE_ON_TAG_COMMAND);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    String test1Namespace = (String)context.getValue("$namespace-test1", String.class);

    assertEquals("A namespace set on the current tag was not correct.", TEST1_NAMESPACE_URI, test1Namespace);
  }

  @Test public void testNamespaceOnOuterTag()
    throws Exception
  {
    Command command = catalog.getCommand(NAMESPACE_ON_OUTER_TAG_COMMAND);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    String test1Namespace = (String)context.getValue("$namespace-test1", String.class);
    String test2Namespace = (String)context.getValue("$namespace-test2", String.class);
    String xchainNamespace = (String)context.getValue("$namespace-xchain", String.class);

    assertEquals("A namespace set on the current tag was not correct.", TEST1_NAMESPACE_URI, test1Namespace);
    assertEquals("A namespace set on the current tag was not correct.", TEST2_NAMESPACE_URI, test2Namespace);
    assertEquals("A namespace set on the current tag was not correct.", XCHAIN_NAMESPACE_URI, xchainNamespace);
  }

  @Test public void testNamespaceUnmapped()
    throws Exception
  {
    Command command = catalog.getCommand(NAMESPACE_UNMAPPED_COMMAND);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    String test1NamespaceBefore = (String)context.getValue("$before-namespace-test1", String.class);
    String test2NamespaceBefore = (String)context.getValue("$before-namespace-test2", String.class);
    String test1Namespace = (String)context.getValue("$namespace-test1", String.class);
    String test2Namespace = (String)context.getValue("$namespace-test2", String.class);
    String test1NamespaceAfter = (String)context.getValue("$after-namespace-test1", String.class);
    String test2NamespaceAfter = (String)context.getValue("$after-namespace-test2", String.class);

    assertEquals("A namespace set before the inner tag was not correct.", TEST2_NAMESPACE_URI, test1NamespaceBefore);
    assertEquals("A namespace set before the inner tag was not correct.", null, test2NamespaceBefore);
    assertEquals("A namespace set on the current tag was not correct.", TEST1_NAMESPACE_URI, test1Namespace);
    assertEquals("A namespace set on the current tag was not correct.", TEST2_NAMESPACE_URI, test2Namespace);
    assertEquals("A namespace set after the inner tag was not correct.", TEST2_NAMESPACE_URI, test1NamespaceAfter);
    assertEquals("A namespace set after the inner tag was not correct.", null, test2NamespaceAfter);
  }


}
