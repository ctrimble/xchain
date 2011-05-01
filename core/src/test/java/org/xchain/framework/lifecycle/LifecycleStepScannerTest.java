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

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xchain.framework.scanner.ScanException;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 * @author John Trimble
 * @author Josh Kennedy
 */
@LifecycleClass(uri="http://www.xchain.org/lifecycle/test-scanner-step-order")
public class LifecycleStepScannerTest
{
  public static Logger log = LoggerFactory.getLogger(LifecycleStepScannerTest.class);
  protected LifecycleContext context;
  
  @Before public void setUp()
  {
    context = new LifecycleContext();
    context.setClassLoader(new LifecycleClassLoader(Thread.currentThread().getContextClassLoader()));
  
  }

  @After public void tearDown()
  {
  }

  @Test public void testLifecycleStepClasses()
    throws Exception
  {
    Iterator<LifecycleStep> stepIterator = getStepIterator();
    Stack<QName> stepStack = new Stack<QName>();
    stepStack.push(QName.valueOf("{http://www.xchain.org/lifecycle/test-scanner-step-order}step4"));
    stepStack.push(QName.valueOf("{http://www.xchain.org/lifecycle/test-scanner-step-order}step3"));
    stepStack.push(QName.valueOf("{http://www.xchain.org/lifecycle/test-scanner-step-order}step2"));
    stepStack.push(QName.valueOf("{http://www.xchain.org/lifecycle/test-scanner-step-order}step1"));
    while( stepIterator.hasNext() && !stepStack.isEmpty() ) {
      LifecycleStep step = stepIterator.next();
      if( log.isDebugEnabled() ) log.debug("Step: "+step.getQName());
      if( step.getQName().equals(stepStack.peek()) )
        log.debug("Pop: "+stepStack.pop().toString());
    }
    assertTrue("Lifecycle step order incorrect.", stepStack.isEmpty());
  }
  
  @Test public void testCoreLifecycleStepOrder()
    throws Exception
  {
    String uri = "http://www.xchain.org/framework/lifecycle";
    List<LifecycleStep> stepList = getStepList();
    isOrderedCorrectly(stepList, 
        new QName(uri, "default-xml-factory"), 
        new QName(uri, "xml-factory-lifecycle"), 
        new QName(uri, "create-config-document-context"), 
        new QName(uri, "config"), 
        new QName(uri, "command-engineering")
    );
  }
  
  @Test public void testImplicitConfigDependency() 
    throws ScanException 
  {
    String lifecycleUri = "http://www.xchain.org/framework/lifecycle";
    String testUri = "http://www.xchain.org/lifecycle/test-scanner-step-order";
    List<LifecycleStep> stepList = getStepList();
    isOrderedCorrectly(stepList,
        new QName(lifecycleUri, "default-xml-factory"), 
        new QName(testUri, "a-nonconfig-step"), // depends on no one.
        new QName(lifecycleUri, "xml-factory-lifecycle"), 
        new QName(lifecycleUri, "create-config-document-context"),
        new QName(lifecycleUri, "config"), 
        new QName(testUri, "a-config-step"), // implicitly depends upon create-config-document-context.
        new QName(lifecycleUri, "command-engineering")
    );    
  }
  
  private String stepListToString(List<LifecycleStep> list) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("[");
    for( LifecycleStep step : list )
      buffer.append(step.getQName().getLocalPart()).append(", ");
    buffer.append("]");
    return buffer.toString();
  }
  
  public void isOrderedCorrectly(List<LifecycleStep> stepList, QName... expectedOrder) {
    Stack<QName> stepStack = new Stack<QName>();
    for( int i = expectedOrder.length - 1; i >= 0; i-- )
      stepStack.push(expectedOrder[i]);
    for( LifecycleStep step : stepList ) {
      if( stepStack.isEmpty() ) break;
      if( stepStack.peek().equals(step.getQName()) )
        stepStack.pop();
    }
    assertEquals("Steps were either not executed or executed in the wrong order.", true, stepStack.isEmpty());
  }
  
  @StartStep(localName="step2", before={"step3"}, after={"step1"})
  public static void startStep2() { }
  
  @StartStep(localName="step4")
  public static void startStep4() { }
  
  @StartStep(localName="step1")
  public static void startStep1() { }
  
  @StartStep(localName="step3", before={"step4"})
  public static void startStep3() { }

  @StartStep(localName="a-config-step")
  public static void startAConfigStep1(LifecycleContext context, ConfigDocumentContext configContext) { }
  
  @StartStep(localName="a-nonconfig-step") 
  public static void startANonconfigStep(LifecycleContext context) { }
  
  public List<LifecycleStep> getStepList() throws ScanException {
    LifecycleStepScanner scanner = new LifecycleStepScanner(context);
    scanner.scan();
    return scanner.getLifecycleStepList();
  }
  
  public Iterator<LifecycleStep> getStepIterator() throws ScanException {
    List<LifecycleStep> stepList = getStepList();
    Iterator<LifecycleStep> stepIterator = stepList.iterator();
    return stepIterator;
  }
}
