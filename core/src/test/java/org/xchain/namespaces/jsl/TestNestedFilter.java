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
 */
public class TestNestedFilter
  extends BaseTestSaxEvents
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/jsl/nested-filter.xchain";
  public static String JSL_NAMESPACE_URI = "http://www.xchain.org/jsl/1.0";
  public static QName SHALLOW_FILTER = new QName(JSL_NAMESPACE_URI, "shallow-filter");
  public static QName DEEP_FILTER = new QName(JSL_NAMESPACE_URI, "deep-filter");

  protected Command command = null;

  public TestNestedFilter()
  {
    catalogUri = CATALOG_URI;
  }

  /**
   * Tests filters nested one level deep inside a jsl template.  Each jsl template element should work like a chain that outputs
   * its start element, then executes it's children, then runs the children's filters in reverse order, then output's it's end element.
   *
   * Template:
   * <element>
   *   <element/>
   *   <test-filter name="first-filter"/>
   *   <element/>
   *   <test-filter name="second-filter"/>
   *   <element/>
   * </element>
   *
   * Output:
   * <element>
   *   <element/>
   *   <first-filter>
   *     <element/>
   *     <second-filter>
   *       <element/>
   *     </second-filter>
   *   </first-filter>
   * </element>
   */
  @Test public void testShallowFilter()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(SHALLOW_FILTER);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);

    // the element that we are testings start element.
    assertStartElement(eventIterator, "", "element", null);

    // the first element in the chain, this is a jsl template element.
    assertStartElement(eventIterator, "", "element", null);
    assertEndElement(eventIterator, "", "element");

    // this element is created by the first filter.
    assertStartElement(eventIterator, "", "first-filter", null);

    // this element is created by the second jsl template in the top level tag.
    assertStartElement(eventIterator, "", "element", null);
    assertEndElement(eventIterator, "", "element");

    // this element is created by the second filter
    assertStartElement(eventIterator, "", "second-filter", null);

    // this element is created byt the final jsl template in the top level tag.
    assertStartElement(eventIterator, "", "element", null);
    assertEndElement(eventIterator, "", "element");

    // these two lines test the nested filters being rolled out.
    assertEndElement(eventIterator, "", "second-filter");
    assertEndElement(eventIterator, "", "first-filter");

    // this tests the closing of the top level element.
    assertEndElement(eventIterator, "", "element");

    // the document should be done and no more elements should be found.
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  /**
   *    <first-element>
   *      <second-element/>
   *      <third-element>
   *        <forth-element/>
   *        <test:filter name="first-filter"/>
   *        <fifth-element/>
   *        <test:filter name="second-filter"/>
   *        <sixth-element/>
   *      </third-element>
   *      <seventh-element/>
   *    </first-element>
   */
  @Test public void testDeepFilter()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(DEEP_FILTER);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);

    // the element that we are testings start element.
    assertStartElement(eventIterator, "", "first-element", null);

    // the first element in the chain, this is a jsl template element.
    assertStartElement(eventIterator, "", "second-element", null);
    assertEndElement(eventIterator, "", "second-element");

    assertStartElement(eventIterator, "", "third-element", null);

    // the first element in the chain, this is a jsl template element.
    assertStartElement(eventIterator, "", "forth-element", null);
    assertEndElement(eventIterator, "", "forth-element");

    // this element is created by the first filter.
    assertStartElement(eventIterator, "", "first-filter", null);

    // this element is created by the second jsl template in the top level tag.
    assertStartElement(eventIterator, "", "fifth-element", null);
    assertEndElement(eventIterator, "", "fifth-element");

    // this element is created by the second filter
    assertStartElement(eventIterator, "", "second-filter", null);

    // this element is created byt the final jsl template in the top level tag.
    assertStartElement(eventIterator, "", "sixth-element", null);
    assertEndElement(eventIterator, "", "sixth-element");

    // these two lines test the nested filters being rolled out.
    assertEndElement(eventIterator, "", "second-filter");
    assertEndElement(eventIterator, "", "first-filter");

    assertEndElement(eventIterator, "", "third-element");

    assertStartElement(eventIterator, "", "seventh-element", null);
    assertEndElement(eventIterator, "", "seventh-element");

    // this tests the closing of the top level element.
    assertEndElement(eventIterator, "", "first-element");

    // the document should be done and no more elements should be found.
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }
}
