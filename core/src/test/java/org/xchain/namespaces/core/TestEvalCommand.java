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
import org.xchain.framework.jxpath.ScopedQNameVariables;

/**
 * @author Devon Tackett
 * @author Christian Trimble
 */
public class TestEvalCommand 
{
  public static final String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/core/eval-command.xchain";
  public static final String XCHAIN_NAMESPACE_URI = "http://www.xchain.org/core/1.0";
  public static final QName CONTEXT_SETTER_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "test-context-setter");
  public static final QName CONTEXT_GETTER_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "test-context-getter");
  public static final QName VARIABLE_SETTER_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "test-variable-setter");
  public static final QName VARIABLE_GETTER_COMMAND = new QName(XCHAIN_NAMESPACE_URI, "test-variable-getter");
  public static final QName OBJECT_VARIABLE = new QName("object");

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
    context = JXPathContext.newContext(new DataObject());
    ((ScopedQNameVariables)context.getVariables()).declareVariable("data-object", new DataObject());
    catalog = CatalogFactory.getInstance().getCatalog(CATALOG_URI);
  }
  
  @After public void tearDown() 
    throws Exception 
  {
      context = null;
  }

  private void testSetter( QName commandName, String dataObjectXPath )
    throws Exception
  {
    Command command = catalog.getCommand(commandName);

    command.execute(context);

    DataObject dataObject = (DataObject)context.getValue(dataObjectXPath, DataObject.class);

    assertEquals("The setter was not called for object at '"+dataObjectXPath+"'.", true, dataObject.dataSetterCalled());
  }

  private void testGetter( QName commandName, String dataObjectXPath )
    throws Exception
  {
    Command command = catalog.getCommand(commandName);

    command.execute(context);

    DataObject dataObject = (DataObject)context.getValue(dataObjectXPath, DataObject.class);

    assertEquals("The getter was not called for object at '"+dataObjectXPath+"'.", true, dataObject.dataGetterCalled());
  }
    
  @Test public void testContextSetter() 
    throws Exception 
  {
    testSetter(CONTEXT_SETTER_COMMAND, ".");
  }

  @Test public void testVariableSetter()
    throws Exception
  {
    testSetter(VARIABLE_SETTER_COMMAND, "$data-object");
  }

  @Test public void testContextGetter()
    throws Exception
  {
    testGetter(CONTEXT_GETTER_COMMAND, ".");
  }

  @Test public void testVariableGetter()
    throws Exception
  {
    testGetter(VARIABLE_GETTER_COMMAND, "$data-object");
  }

  public static class DataObject
  {
    boolean getterCalled = false;
    boolean setterCalled = false;
    private String data = "unset";

    public String getData()
    {
      getterCalled = true; 
      return data;
    }

    public void setData( String data )
    {
      setterCalled = true;
      this.data = data;
    }

    public boolean dataGetterCalled() { return getterCalled; }
    public boolean dataSetterCalled() { return setterCalled; }
  }
}
