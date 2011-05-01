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
package org.xchain.test.component;

import static org.junit.Assert.assertEquals;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xchain.Catalog;
import org.xchain.Command;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.framework.lifecycle.ExecutionException;
import org.xchain.framework.lifecycle.Lifecycle;

/**
 * @author Devon Tackett
 */
public class TestComponent {
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/test/component/component.xchain";
  public static String BASIC_COMPONENT_COMMAND = "basic-component-test";
  public static String BASIC_COMPONENT_PARAM_COMMAND = "basic-component-param-test";
  public static String JAVA_OBJECT_COMPONENT_PARAM_COMMAND = "java-object-component-param-test";
  public static String BEGIN_COMPONENT_COMMAND = "begin-component-test";
  public static String END_COMPONENT_COMMAND = "end-component-test";
  public static String COUNT_COMPONENT_COMMAND = "count-component-test";
  public static String FIELD_COMPONENT_COMMAND = "field-component-test";
  public static String SIMPLE_METHOD_COMPONENT_COMMAND = "simple-method-component-test";
  public static String NAMESPACE_FIELD_COMPONENT_COMMAND = "namespace-field-component-test";
  public static String CHILD_FIELD_COMPONENT_COMMAND = "child-field-component-test";
  public static String CHILD_METHOD_COMPONENT_COMMAND = "child-method-component-test";
  public static String CHAIN_SCOPE_COMPONENT_COMMAND = "chain-scope-component-test";
  public static String EXECUTION_SCOPE_COMPONENT_COMMAND = "execution-scope-component-test";
  public static String REQUEST_SCOPE_COMPONENT_COMMAND = "request-scope-component-test";
  public static String CHAIN_SCOPE_COMPONENT_EXCEPTION_COMMAND = "chain-scope-exception-component-test";
  public static String EXECUTION_SCOPE_COMPONENT_EXCEPTION_COMMAND = "execution-scope-exception-component-test";
  public static String FAILED_INJECTION_EXCEPTION_COMPONENT_COMMAND = "failed-injection-exception-component-test";
  public static String ALLOWED_INJECTION_EXCEPTION_COMPONENT_COMMAND = "allowed-injection-exception-component-test";

  protected JXPathContext context = null;
  protected Catalog catalog = null;
  protected Command command = null;
  
  public class TestParamClass {
    private String value = "initial";
    
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
  }
  
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
    catalog = CatalogFactory.getInstance().getCatalog(CATALOG_URI);
    context = JXPathContext.newContext(new Object());
  }

  @After public void tearDown()
    throws Exception
  {
    context = null;
  }
  
  @Test public void testBasicComponent() 
    throws Exception 
  {    
    // Get the command.
    Command command = catalog.getCommand(BASIC_COMPONENT_COMMAND);
    
    // execute the command.
    command.execute(context);
    
    // get the value for the variable.
    String value = (String)context.getValue("$result", String.class);

    assertEquals("The result of the basic component was not correct.", BasicComponent.BASIC_RESULT, value);    
  }
  
  @Test public void testBasicParamComponent() 
    throws Exception 
  {    
    // Get the command.
    Command command = catalog.getCommand(BASIC_COMPONENT_PARAM_COMMAND);
    
    // execute the command.
    command.execute(context);
    
    // get the value for the variable.
    String value = (String)context.getValue("$result", String.class);
  
    assertEquals("The result of the basic param component was not correct.", "initial" + BasicComponent.PARAM_RESULT, value);    
  }  
  
  @Test public void testJavaObjectParamComponent() 
    throws Exception 
  {    
    // Create the java object
    TestParamClass paramObject = new TestParamClass();
    
    // Put the object in the context.
    context.getVariables().declareVariable("object-param", paramObject);
    
    // Get the command.
    Command command = catalog.getCommand(JAVA_OBJECT_COMPONENT_PARAM_COMMAND);
    
    // execute the command.
    command.execute(context);
    
    // get the value for the variable.
    paramObject = (TestParamClass)context.getValue("$result", TestParamClass.class);
  
    assertEquals("The result of the java object param component was not correct.", BasicComponent.OBJECT_PARAM_TEST_RESULT, paramObject.getValue());    
  }    
  
  @Test public void testBeginComponent() 
    throws Exception 
  {    
    // Get the command.
    Command command = catalog.getCommand(BEGIN_COMPONENT_COMMAND);
    
    // execute the command.
    command.execute(context);
    
    // get the value for the variable.
    String value = (String)context.getValue("$result", String.class);
  
    assertEquals("The result of the begin component was not correct.", BeginComponent.BEGIN_RESULT, value);    
  }  
  
  @Test public void testCountComponent() 
    throws Exception 
  {    
    // Get the command.
    Command command = catalog.getCommand(COUNT_COMPONENT_COMMAND);
    
    // execute the command.
    command.execute(context);
    
    // get the value for the variable.
    Integer value = (Integer)context.getValue("$result", Integer.class);
  
    assertEquals("The result of the count component was not correct.", CountComponent.START_COUNT + 5, value.intValue());    
  }    
  
  @Test public void testEndComponent() 
    throws Exception 
  {    
    // Create the java object
    TestParamClass paramObject = new TestParamClass();
    
    // Put the object in the context.
    context.getVariables().declareVariable("object-param", paramObject);
    
    // Get the command.
    Command command = catalog.getCommand(END_COMPONENT_COMMAND);
    
    // execute the command.
    command.execute(context);
  
    assertEquals("The result of the end component was not correct.", EndComponent.END_RESULT, paramObject.getValue());    
  }    
  
  @Test public void testFieldComponent() 
    throws Exception 
  {       
    // Put the field in the context.
    context.getVariables().declareVariable("private-field", FieldComponent.FINAL_PRIVATE_VALUE);
    context.getVariables().declareVariable("protected-field", FieldComponent.FINAL_PROTECTED_VALUE);
    context.getVariables().declareVariable("public-field", FieldComponent.FINAL_PUBLIC_VALUE);
    
    // Get the command.
    Command command = catalog.getCommand(FIELD_COMPONENT_COMMAND);
    
    // execute the command.
    command.execute(context);
    
    // get the value for the variable.
    String privateResult = (String)context.getValue("$private-result", String.class);    
    String protectedResult = (String)context.getValue("$protected-result", String.class);    
    String publicResult = (String)context.getValue("$public-result", String.class);    
  
    assertEquals("The result of the private field component was not correct.", FieldComponent.FINAL_PRIVATE_VALUE, privateResult);    
    assertEquals("The result of the protected field component was not correct.", FieldComponent.FINAL_PROTECTED_VALUE, protectedResult);    
    assertEquals("The result of the public field component was not correct.", FieldComponent.FINAL_PUBLIC_VALUE, publicResult);    
  }    
  
  @Test public void testSimpleMethodComponent() 
    throws Exception 
  {       
    // Put the field in the context.
    context.getVariables().declareVariable("method", SimpleMethodComponent.FINAL_VALUE);
    
    // Get the command.
    Command command = catalog.getCommand(SIMPLE_METHOD_COMPONENT_COMMAND);
    
    // execute the command.
    command.execute(context);
    
    // get the value for the variable.
    String value = (String)context.getValue("$result", String.class);    
  
    assertEquals("The result of the simple method component was not correct.", SimpleMethodComponent.FINAL_VALUE, value);    
  }     
  
  @Test public void testNamespaceFieldComponent() 
    throws Exception 
  {       
    // Put the field in the context.
    context.getVariables().declareVariable("{" + NamespaceFieldComponent.TEST_NAMESPACE_URI + "}field", NamespaceFieldComponent.FINAL_VALUE);
    
    // Get the command.
    Command command = catalog.getCommand(NAMESPACE_FIELD_COMPONENT_COMMAND);
    
    // execute the command.
    command.execute(context);
    
    // get the value for the variable.
    String value = (String)context.getValue("$result", String.class);    
  
    assertEquals("The result of the namespace field component was not correct.", NamespaceFieldComponent.FINAL_VALUE, value);    
  }   
  
  @Test public void testChildFieldComponent() 
    throws Exception 
  {       
    // Put the field in the context.
    context.getVariables().declareVariable("public-field", ParentFieldComponent.FINAL_PUBLIC_VALUE);
    context.getVariables().declareVariable("protected-field", ParentFieldComponent.FINAL_PROTECTED_VALUE);
    
    // Get the command.
    Command command = catalog.getCommand(CHILD_FIELD_COMPONENT_COMMAND);
    
    // execute the command.
    command.execute(context);
    
    // get the value for the variable.
    String publicValue = (String)context.getValue("$public-result", String.class);    
    String protectedValue = (String)context.getValue("$protected-result", String.class);    
  
    assertEquals("The result of the public field in the child component was not correct.", ParentFieldComponent.FINAL_PUBLIC_VALUE, publicValue);    
    assertEquals("The result of the protected field in the child component was not correct.", ParentFieldComponent.FINAL_PROTECTED_VALUE, protectedValue);    
  }   
  
  @Test public void testChildMethodComponent() 
    throws Exception 
  {    
    // Put the field in the context.
    context.getVariables().declareVariable("value", ParentMethodComponent.FINAL_RESULT);
    
    // Get the command.
    Command command = catalog.getCommand(CHILD_METHOD_COMPONENT_COMMAND);
    
    // execute the command.
    command.execute(context);
    
    // get the value for the variable.   
    String result = (String)context.getValue("$result", String.class);    
  
    assertEquals("The result of the child method component was not correct.", ParentMethodComponent.FINAL_RESULT, result);       
  }    
  
  @Test public void testChainScopeComponent() 
    throws Exception 
  {        
    // Get the command.
    Command command = catalog.getCommand(CHAIN_SCOPE_COMPONENT_COMMAND);
    
    // execute the command.
    command.execute(context);
    
    // get the value for the variable.   
    String firstResult = (String)context.getValue("$first-chain-result", String.class);    
    String secondResult = (String)context.getValue("$second-chain-result", String.class);    
  
    assertEquals("The result of the first chain scoped component was not correct.", "first", firstResult);       
    assertEquals("The result of the second chain scoped component was not correct.", "second", secondResult);       
  }   
  
  @Test public void testExecutionScopeComponent() 
    throws Exception 
  {        
    // Put the field in the context.
    context.getVariables().declareVariable("value", ExecutionScopeComponent.FINAL_RESULT);
    
    // Get the command.
    Command command = catalog.getCommand(EXECUTION_SCOPE_COMPONENT_COMMAND);
    
    // execute the command.
    command.execute(context);
    
    // get the value for the variable.   
    String result = (String)context.getValue("$result", String.class);        
  
    assertEquals("The result of the execution scoped component was not correct.", ExecutionScopeComponent.FINAL_RESULT, result);  
  }    
  
  @Test public void testRequestScopeComponent() 
    throws Exception 
  {        
    // Get the command.
    Command command = catalog.getCommand(REQUEST_SCOPE_COMPONENT_COMMAND);
    
    // execute the command.
    command.execute(context);
    
    context.registerNamespace("basic", "http://www.xchain.org/test/component");
    // get the value for the variable.   
    RequestScopeComponent requestComponent = (RequestScopeComponent)context.getValue("$basic:request-scope-component", RequestScopeComponent.class);    
         
    assertEquals("The result of the request scoped component was not correct.", "final-value", requestComponent.getResult());       
  }    
  
  @Test public void testChainScopeException() 
    throws Exception 
  {        
    // Put the field in the context.
    context.getVariables().declareVariable("value", "dummy");
    
    // Create the java object
    TestParamClass paramObject = new TestParamClass();
    
    // Put the object in the context.
    context.getVariables().declareVariable("test-param", paramObject);
    
    // Get the command.
    Command command = catalog.getCommand(CHAIN_SCOPE_COMPONENT_EXCEPTION_COMMAND);
    
    try {
      // execute the command.
      command.execute(context);
    } catch (Exception ignore) {
      // Ignore the outcoming exception as it was intentional
    }
  
    assertEquals("The chain scoped component was not properly ended.", "clean", paramObject.getValue());    
  }    
  
  @Test public void testExecutionScopeException() 
    throws Exception 
  {        
    // Put the field in the context.
    context.getVariables().declareVariable("value", "dummy");
    
    // Create the java object
    TestParamClass paramObject = new TestParamClass();
    
    // Put the object in the context.
    context.getVariables().declareVariable("test-param", paramObject);
    
    // Get the command.
    Command command = catalog.getCommand(EXECUTION_SCOPE_COMPONENT_EXCEPTION_COMMAND);
    
    try {
      // execute the command.
      command.execute(context);
    } catch (Exception ignore) {
      // Ignore the outcoming exception as it was intentional
    }
  
    assertEquals("The execution scoped component was not properly ended.", "clean", paramObject.getValue());    
  }   
    
  @Test(expected=ExecutionException.class) public void testFailedInjection() 
    throws Exception 
  {            
    // Get the command.
    Command command = catalog.getCommand(FAILED_INJECTION_EXCEPTION_COMPONENT_COMMAND);
    
    // execute the command.
    command.execute(context);    
  }  
  
  @Test public void testAllowedFailedInjection() 
    throws Exception 
  {            
    // Get the command.
    Command command = catalog.getCommand(ALLOWED_INJECTION_EXCEPTION_COMPONENT_COMMAND);
    
    // execute the command.
    command.execute(context);    
  }    
}
