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
public class TestNullValueOf
  extends BaseTestSaxEvents
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/jsl/test-null-value-of.xchain";
  public static String JSL_NAMESPACE_URI = "http://www.xchain.org/jsl/1.0";
  public static QName VALUE_OF_STRING = new QName(JSL_NAMESPACE_URI, "value-of-string");
  public static QName VALUE_OF_INTEGER = new QName(JSL_NAMESPACE_URI, "value-of-integer");
  public static QName VALUE_OF_NESTED_STRING = new QName(JSL_NAMESPACE_URI, "value-of-nested-string");
  public static QName VALUE_OF_NESTED_INTEGER = new QName(JSL_NAMESPACE_URI, "value-of-nested-integer");

  protected Command command = null;

  public TestNullValueOf()
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
  @Test public void testValueOfString()
    throws Exception
  {
    executeValueOf(VALUE_OF_STRING);
  }

  @Test public void testValueOfInteger()
    throws Exception
  {
    executeValueOf(VALUE_OF_INTEGER);
  }

  @Test public void testValueOfNestedString()
    throws Exception
  {
    executeValueOf(VALUE_OF_NESTED_STRING);
  }

  @Test public void testValueOfNestedInteger()
    throws Exception
  {
    executeValueOf(VALUE_OF_NESTED_INTEGER);
  }

   public void executeValueOf(QName templateName)
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

    // check the document.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "element", null);
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
