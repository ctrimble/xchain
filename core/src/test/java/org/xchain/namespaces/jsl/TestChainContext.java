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
package org.xchain.namespaces.jsl;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.xchain.Command;
import org.xchain.framework.sax.SaxEventRecorder;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class TestChainContext
  extends BaseTestSaxEvents
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/jsl/test-chain-context.xchain";
  public static String JSL_NAMESPACE_URI = "http://www.xchain.org/jsl/1.0";
  public static QName EXECUTE_TEMPLATE = new QName(JSL_NAMESPACE_URI, "execute-template");
  public static QName CHAIN_VARIABLE = new QName(JSL_NAMESPACE_URI, "chain-variable");

  protected Command command = null;

  public TestChainContext()
  {
    catalogUri = CATALOG_URI;
  }

  @Test public void testLocalVariable()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(EXECUTE_TEMPLATE);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackCharactersEvents(true);

    context.getVariables().declareVariable("template-name", CHAIN_VARIABLE);
    context.getVariables().declareVariable("variable", "request");

    // execute the command.
    command.execute(context);

    // check the value of the variable.
    assertEquals("The request variable was changed.", "request", context.getValue("$variable"));

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "element", null);
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }
}
