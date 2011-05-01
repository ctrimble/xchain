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
import org.xchain.framework.lifecycle.Lifecycle;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class TestLiteralAttributes
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/test/literal-attributes.xchain";
  public static String NAMESPACE_URI = "http://www.xchain.org/test/1.0";
  public static QName ENUM_ATTRIBUTE_FIRST = new QName(NAMESPACE_URI, "enum-attribute-first");
  public static QName ENUM_ATTRIBUTE_SECOND = new QName(NAMESPACE_URI, "enum-attribute-second");
  public static QName ENUM_ATTRIBUTE_DEFAULT = new QName(NAMESPACE_URI, "enum-attribute-default");
  public static QName ENUM_ATTRIBUTE_NULL = new QName(NAMESPACE_URI, "enum-attribute-null");
  public static QName STRING_ATTRIBUTE = new QName(NAMESPACE_URI, "string-attribute");

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
  @Test public void testEnumAttributeFirst()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(ENUM_ATTRIBUTE_FIRST);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    NumberEnum value = (NumberEnum)context.getValue("$result", NumberEnum.class);

    assertEquals("The attribute did not have the correct value.", NumberEnum.FIRST, value);
  }

  /**
   * Tests a literal enumeration with its second value.
   */
  @Test public void testEnumAttributeSecond()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(ENUM_ATTRIBUTE_SECOND);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    NumberEnum value = (NumberEnum)context.getValue("$result", NumberEnum.class);

    assertEquals("The attribute did not have the correct value.", NumberEnum.SECOND, value);
  }

  /**
   * Tests a literal enumeration with its default value.
   */
  @Test public void testEnumAttributeDefault()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(ENUM_ATTRIBUTE_DEFAULT);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    NumberEnum value = (NumberEnum)context.getValue("$result", NumberEnum.class);

    assertEquals("The attribute did not have the correct value.", NumberEnum.FIRST, value);
  }

  /**
   * Tests a literal enumeration without a value.
   */
  @Test public void testEnumAttributeNull()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(ENUM_ATTRIBUTE_NULL);

    // execute the command.
    command.execute(context);

    // get the value for the variable.
    NumberEnum value = (NumberEnum)context.getValue("$result", NumberEnum.class);

    // get the value for the variable.
    assertEquals("The attribute did not have the correct value.", null, value);
  }
}
