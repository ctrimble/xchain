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
public class TestReturnTrue
  extends BaseTestSaxEvents
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/jsl/return-true.xchain";
  public static String JSL_NAMESPACE_URI = "http://www.xchain.org/jsl/1.0";
  public static QName RETURN_CHILD = new QName(JSL_NAMESPACE_URI, "return-child");
  public static QName RETURN_BEFORE_SIBLING = new QName(JSL_NAMESPACE_URI, "return-before-sibling");
  public static QName RETURN_AFTER_SIBLING = new QName(JSL_NAMESPACE_URI, "return-after-sibling");
  public static QName RETURN_BETWEEN_SIBLING = new QName(JSL_NAMESPACE_URI, "return-between-siblings");
  public static QName RETURN_NESTED_BEFORE_SIBLING = new QName(JSL_NAMESPACE_URI, "return-nested-before-sibling");
  public static QName RETURN_NESTED_AFTER_SIBLING = new QName(JSL_NAMESPACE_URI, "return-nested-after-sibling");

  protected Command command = null;

  public TestReturnTrue()
  {
    catalogUri = CATALOG_URI;
  }

  @Test public void testReturnChild()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(RETURN_CHILD);

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
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  @Test public void testReturnBeforeSibling()
    throws Exception
  {
    Command command = catalog.getCommand(RETURN_BEFORE_SIBLING);

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
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  @Test public void testReturnAfterSibling()
    throws Exception
  {
    Command command = catalog.getCommand(RETURN_AFTER_SIBLING);

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
    assertStartElement(eventIterator, "", "element", null);
    assertEndElement(eventIterator, "", "element");
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  @Test public void testReturnBetweenSibling()
    throws Exception
  {
    Command command = catalog.getCommand(RETURN_BETWEEN_SIBLING);

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
    assertStartElement(eventIterator, "", "element", null);
    assertEndElement(eventIterator, "", "element");
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }
}
