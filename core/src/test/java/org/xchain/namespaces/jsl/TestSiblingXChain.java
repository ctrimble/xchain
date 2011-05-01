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

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.xchain.Command;
import org.xchain.framework.sax.SaxEventRecorder;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class TestSiblingXChain
  extends BaseTestSaxEvents
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/jsl/sibling-xchain.xchain";
  public static String JSL_NAMESPACE_URI = "http://www.xchain.org/jsl/1.0";
  public static QName XCHAIN_TEMPLATE = new QName(JSL_NAMESPACE_URI, "xchain-template");
  public static QName TEMPLATE_XCHAIN = new QName(JSL_NAMESPACE_URI, "template-xchain");
  public static QName XCHAIN_TEMPLATE_XCHAIN= new QName(JSL_NAMESPACE_URI, "xchain-template-xchain");
  public static QName TEMPLATE_XCHAIN_TEMPLATE = new QName(JSL_NAMESPACE_URI, "template-xchain-template");

  protected Command command = null;

  public TestSiblingXChain()
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
    recorder.setTrackCharactersEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "element", null);
    assertStartElement(eventIterator, "", "first", null);
    assertEndElement(eventIterator, "", "first");
    assertStartElement(eventIterator, "", "second", null);
    assertEndElement(eventIterator, "", "second");
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
    recorder.setTrackCharactersEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "element", null);
    assertStartElement(eventIterator, "", "first", null);
    assertEndElement(eventIterator, "", "first");
    assertStartElement(eventIterator, "", "second", null);
    assertEndElement(eventIterator, "", "second");
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
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

    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "element", null);
    assertStartElement(eventIterator, "", "first", null);
    assertEndElement(eventIterator, "", "first");
    assertStartElement(eventIterator, "", "second", null);
    assertEndElement(eventIterator, "", "second");
    assertStartElement(eventIterator, "", "third", null);
    assertEndElement(eventIterator, "", "third");
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
    recorder.setTrackCharactersEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "element", null);
    assertStartElement(eventIterator, "", "first", null);
    assertEndElement(eventIterator, "", "first");
    assertStartElement(eventIterator, "", "second", null);
    assertEndElement(eventIterator, "", "second");
    assertStartElement(eventIterator, "", "third", null);
    assertEndElement(eventIterator, "", "third");
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }
}
