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
import org.xchain.framework.jxpath.Scope;
import org.xchain.framework.jxpath.ScopedQNameVariables;

/**
 * @author Christian Trimble
 */
public class TestXmlnsValueOf
  extends BaseTestSaxEvents
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/jsl/xmlns-value-of.xchain";
  public static String JSL_NAMESPACE_URI = "http://www.xchain.org/jsl/1.0";
  public static String TEST_NAMESPACE_URI = "test";
  public static QName TEST_XMLNS = new QName(JSL_NAMESPACE_URI, "test-xmlns");

  protected Command command = null;

  public TestXmlnsValueOf()
  {
    catalogUri = CATALOG_URI;
  }

  /**
   * Validates that selecting a null string will produce the output "" instead of "null".
   */
  @Test public void testXmlns()
    throws Exception
  {
    ((ScopedQNameVariables)context.getVariables()).declareVariable( QName.valueOf("{"+TEST_NAMESPACE_URI+"}variable"), "Value", Scope.request );

    // get the command.
    Command command = catalog.getCommand(TEST_XMLNS);

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
    assertCharacters(eventIterator, "Value");
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);

    
  }
}
