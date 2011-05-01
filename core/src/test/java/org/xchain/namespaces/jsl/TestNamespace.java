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
public class TestNamespace
  extends BaseTestSaxEvents
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/jsl/namespaces.xchain";
  public static String JSL_NAMESPACE_URI = "http://www.xchain.org/jsl/1.0";
  public static String XHTML_NAMESPACE_URI = "http://www.w3.org/1999/xhtml";
  public static QName NAMESPACE_ON_ANCESTOR = new QName(JSL_NAMESPACE_URI, "namespace-on-ancestor");
  public static QName NAMESPACE_ON_JSL_TEMPLATE_ELEMENT = new QName(JSL_NAMESPACE_URI, "namespace-on-jsl-template-element");
  public static QName NAMESPACE_ON_XCHAIN = new QName(JSL_NAMESPACE_URI, "namespace-on-xchain");
  public static QName NAMESPACE_ON_TEMPLATE_ELEMENT = new QName(JSL_NAMESPACE_URI, "namespace-on-template-element");

    protected Command command = null;

  public TestNamespace()
  {
    catalogUri = CATALOG_URI;
  }

  @Test public void testNamespaceOnAncestor()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(NAMESPACE_ON_ANCESTOR);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);

    // execute the command.
    command.execute(context);

    // get the boolean value.
    context.registerNamespace("xhtml", XHTML_NAMESPACE_URI);
    Boolean variable = (Boolean)context.getValue("$xhtml:variable", Boolean.class);
    assertEquals("The variable {"+XHTML_NAMESPACE_URI+"}variable did not get set.", Boolean.TRUE, variable);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the sax events that were produced.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, XHTML_NAMESPACE_URI, "html", null);
    assertEndElement(eventIterator, XHTML_NAMESPACE_URI, "html");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  @Test public void testNamespaceOnJslTemplateElement()
    throws Exception
  {
    Command command = catalog.getCommand(NAMESPACE_ON_JSL_TEMPLATE_ELEMENT);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);

    // execute the command.
    command.execute(context);

    // get the boolean value.
    context.registerNamespace("xhtml", XHTML_NAMESPACE_URI);
    Boolean variable = (Boolean)context.getValue("$xhtml:variable", Boolean.class);
    assertEquals("The variable {"+XHTML_NAMESPACE_URI+"}variable did not get set.", Boolean.TRUE, variable);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the sax events that were produced.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, XHTML_NAMESPACE_URI, "html", null);
    assertEndElement(eventIterator, XHTML_NAMESPACE_URI, "html");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  @Test public void testNamespaceOnXChain()
    throws Exception
  {
    Command command = catalog.getCommand(NAMESPACE_ON_XCHAIN);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);

    // execute the command.
    command.execute(context);

    // get the boolean value.
    context.registerNamespace("xhtml", XHTML_NAMESPACE_URI);
    Boolean variable = (Boolean)context.getValue("$xhtml:variable", Boolean.class);
    assertEquals("The variable {"+XHTML_NAMESPACE_URI+"}variable did not get set.", Boolean.TRUE, variable);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the sax events that were produced.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, XHTML_NAMESPACE_URI, "html", null);
    assertEndElement(eventIterator, XHTML_NAMESPACE_URI, "html");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  @Test public void testNamespaceOnTemplateElement()
    throws Exception
  {
    Command command = catalog.getCommand(NAMESPACE_ON_TEMPLATE_ELEMENT);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);

    // execute the command.
    command.execute(context);

    // get the boolean value.
    context.registerNamespace("xhtml", XHTML_NAMESPACE_URI);
    Boolean variable = (Boolean)context.getValue("$xhtml:variable", Boolean.class);
    assertEquals("The variable {"+XHTML_NAMESPACE_URI+"}variable did not get set.", Boolean.TRUE, variable);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the sax events that were produced.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, XHTML_NAMESPACE_URI, "html", null);
    assertEndElement(eventIterator, XHTML_NAMESPACE_URI, "html");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }
}
