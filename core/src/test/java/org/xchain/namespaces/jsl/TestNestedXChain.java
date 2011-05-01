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
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class TestNestedXChain
  extends BaseTestSaxEvents
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/jsl/nested-xchain.xchain";
  public static String JSL_NAMESPACE_URI = "http://www.xchain.org/jsl/1.0";
  public static QName XCHAIN_TEMPLATE = new QName(JSL_NAMESPACE_URI, "xchain-template");
  public static QName TEMPLATE_XCHAIN = new QName(JSL_NAMESPACE_URI, "template-xchain");
  public static QName XCHAIN_TEMPLATE_XCHAIN= new QName(JSL_NAMESPACE_URI, "xchain-template-xchain");
  public static QName TEMPLATE_XCHAIN_TEMPLATE = new QName(JSL_NAMESPACE_URI, "template-xchain-template");
  public static QName TEMPLATE_XCHAIN_TEXT = new QName(JSL_NAMESPACE_URI, "template-xchain-text");

  protected Command command = null;

  public TestNestedXChain()
  {
    catalogUri = CATALOG_URI;
  }

  @Test public void testXChainTemplate()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(XCHAIN_TEMPLATE);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "element", null);
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  @Test public void testTemplateXChain()
    throws Exception
  {
    Command command = catalog.getCommand(TEMPLATE_XCHAIN);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "element", null);
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);

    // make sure that the nested command executed.
    assertEquals("The value was not set.", Boolean.TRUE, context.getValue("$executed", Boolean.class));
  }

  @Test public void testTemplateXChainTemplate()
    throws Exception
  {
    Command command = catalog.getCommand(TEMPLATE_XCHAIN_TEMPLATE);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackCharactersEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();
    AttributesImpl attributes = new AttributesImpl();

    // check the document.
    assertStartDocument(eventIterator);

    attributes.addAttribute("", "id", "id", "CDATA", "3");
    assertStartElement(eventIterator, "", "element", attributes);
    attributes.clear();

    attributes.addAttribute("", "id", "id", "CDATA", "4");
    assertStartElement(eventIterator, "", "element", attributes);
    attributes.clear();

    assertCharacters(eventIterator, "text");

    assertEndElement(eventIterator, "", "element");
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  @Test public void testTemplateXChainText()
    throws Exception
  {
    Command command = catalog.getCommand(TEMPLATE_XCHAIN_TEXT);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackCharactersEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "element", null);
    assertCharacters(eventIterator, "text");
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }


  @Test public void testXChainTemplateXChain()
    throws Exception
  {
    Command command = catalog.getCommand(XCHAIN_TEMPLATE_XCHAIN);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "element", null);
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }
}
