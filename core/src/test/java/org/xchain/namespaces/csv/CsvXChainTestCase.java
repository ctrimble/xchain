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
package org.xchain.namespaces.csv;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xchain.Catalog;
import org.xchain.Command;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.framework.lifecycle.Lifecycle;

/**
 * @author John Trimble
 */
public class CsvXChainTestCase {
  private static final String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/csv/csv-test-basic.xchain";
  
  @BeforeClass 
  public static void setupLifecycle()
    throws Exception
  {
      Lifecycle.startLifecycle();  
  }
  
  @AfterClass 
  public static void tearDownLifecycle()
    throws Exception
  {
   Lifecycle.stopLifecycle();
  }
  
  protected void declareTestVariables(JXPathContext context) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    context.getVariables().declareVariable("output", outputStream);
    context.getVariables().declareVariable("double-quote", "\"");
    context.getVariables().declareVariable("single-quote", "'");
  }
  
  protected JXPathContext createTestContext() {
    JXPathContext context = JXPathContext.newContext(new Object());
    declareTestVariables(context);
    return context;
  }
  
  protected String getOutputString(JXPathContext context) {
    return ((ByteArrayOutputStream)context.getVariables().getVariable("output")).toString();
  }
  
  protected JXPathContext runTestChain(String testChain) throws Exception {
    Catalog catalog = CatalogFactory.getInstance().getCatalog(CATALOG_URI);
    Command command = catalog.getCommand(testChain);
    JXPathContext context = createTestContext();
    command.execute(context);
    return context;
  }
  
  @Test
  public void testBasic() throws Exception {
    JXPathContext context = runTestChain("test00");
    assertEquals(getOutputString(context), "\"cell 1\",\"cell 2\"\n\"cell 3\",\"cell 4\"\n");
  }
  
  @Test
  public void testEscape() throws Exception {
    JXPathContext context = runTestChain("test01");
    assertEquals(getOutputString(context), "\"a double quote\"\"\",\"a single quote'\",\"\"\n\"Comma ,\",\"a non column\"\",\"\"another non column\",\"just another cell\"\n");
  }
  
  @Test
  public void testVariableRowLengths() throws Exception {
    JXPathContext context = runTestChain("test02");
    assertEquals(getOutputString(context), "\"00\"\n\"10\",\"11\"\n\"20\",\"21\",\"22\"\n");
  }
  
  @Test
  public void testObjectStringCoercion() throws Exception {
    JXPathContext context = runTestChain("test03");
    assertEquals(getOutputString(context), "\"5\"\n");
  }
}
