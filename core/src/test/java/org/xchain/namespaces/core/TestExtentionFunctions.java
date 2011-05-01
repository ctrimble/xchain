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
import static org.junit.Assert.assertFalse;

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
 */
public class TestExtentionFunctions 
{
  public static final String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/core/extention-functions.xchain";
  public static final String XCHAIN_NAMESPACE_URI = "http://www.xchain.org/core/1.0";
  public static final QName NAME_CONSTRUCTOR = new QName(XCHAIN_NAMESPACE_URI, "name-constructor");
  public static final QName DEFAULT_NAME_CONSTRUCTOR = new QName(XCHAIN_NAMESPACE_URI, "default-name-constructor");
  public static final QName DESCRIPTION_CONSTRUCTOR = new QName(XCHAIN_NAMESPACE_URI, "description-constructor");
  public static final QName DEFAULT_DESCRIPTION_CONSTRUCTOR = new QName(XCHAIN_NAMESPACE_URI, "default-description-constructor");
  public static final QName NAME_INSTANCE_FUNCTION = new QName(XCHAIN_NAMESPACE_URI, "name-instance-function");
  public static final QName DESCRIPTION_INSTANCE_FUNCTION = new QName(XCHAIN_NAMESPACE_URI, "name-instance-function");
  public static final QName STATIC_FUNCTION = new QName(XCHAIN_NAMESPACE_URI, "static-function");
  public static final String RESULT = "result";

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

  private void testPathValue( QName commandName, String path, String expectedValue )
    throws Exception
  {
    // Get the command.
    Command command = catalog.getCommand(commandName);

    // execute the command.
    command.execute(context);
      
    // Get the result.
    String value = (String)context.getValue(path, String.class);
      
    assertEquals("The expected value for "+path+" failed.", expectedValue, value);
  }

  @Test public void testDefaultNameConstructor()
    throws Exception
  {
    testPathValue( DEFAULT_NAME_CONSTRUCTOR, "$name-object/name", "unset" );
  }

  @Test public void testNameConstructor()
    throws Exception
  {
    testPathValue( NAME_CONSTRUCTOR, "$name-object/name", "name" );
  }

  @Test public void testDefaultDescriptionConstructor()
    throws Exception
  {
    testPathValue( DEFAULT_DESCRIPTION_CONSTRUCTOR, "$description-object/name", "unset" );
    testPathValue( DEFAULT_DESCRIPTION_CONSTRUCTOR, "$description-object/description", "unset" );
  }

  @Test public void testDescriptionConstructor()
    throws Exception
  {
    testPathValue( DESCRIPTION_CONSTRUCTOR, "$description-object/name", "name" );
    testPathValue( DESCRIPTION_CONSTRUCTOR, "$description-object/description", "description" );
  }
}
