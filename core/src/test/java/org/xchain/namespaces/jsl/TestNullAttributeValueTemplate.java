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
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Christian Trimble
 */
public class TestNullAttributeValueTemplate
  extends BaseTestSaxEvents
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/jsl/test-null-attribute-value-template.xchain";
  public static String JSL_NAMESPACE_URI = "http://www.xchain.org/jsl/1.0";
  public static QName NULL_STRING = new QName(JSL_NAMESPACE_URI, "null-string");
  public static QName NULL_INTEGER = new QName(JSL_NAMESPACE_URI, "null-integer");
  public static QName NESTED_NULL_STRING = new QName(JSL_NAMESPACE_URI, "nested-null-string");
  public static QName NESTED_NULL_INTEGER = new QName(JSL_NAMESPACE_URI, "nested-null-integer");

  protected Command command = null;

  public TestNullAttributeValueTemplate()
  {
    catalogUri = CATALOG_URI;
  }

  /**
   * Creates a NullTestObject and returns it.
   */
  public Object createContextObject()
  {
    NullTestObject object = new NullTestObject();
    object.setNested(new NullTestObject());
    return object;
  }

  /**
   * Validates that selecting a null string will produce the output "" instead of "null".
   */
  @Test public void testNullString()
    throws Exception
  {
    executeAttributeValueTemplate(NULL_STRING);
  }

  @Test public void testNullInteger()
    throws Exception
  {
    executeAttributeValueTemplate(NULL_INTEGER);
  }

  @Test public void testNestedNullString()
    throws Exception
  {
    executeAttributeValueTemplate(NESTED_NULL_STRING);
  }

  @Test public void testNestedNullInteger()
    throws Exception
  {
    executeAttributeValueTemplate(NESTED_NULL_INTEGER);
  }

   public void executeAttributeValueTemplate(QName templateName)
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(templateName);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackCharactersEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    AttributesImpl attributes = new AttributesImpl();
    attributes.addAttribute("", "attribute", "attribute", "CDATA", "");

    // check the document.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "element", attributes);
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  public static class NullTestObject
  {
    private Object nested;
    public String getString() { return null; }
    public Integer getInteger() { return null; }
    public Object getNested() { return nested; }
    public void setNested( Object nested ) { this.nested = nested; }
  }
}
