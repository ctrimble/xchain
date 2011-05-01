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
package org.xchain.namespaces.sax;

import static org.junit.Assert.assertEquals;

import javax.xml.namespace.QName;
import java.util.Iterator;
import org.xchain.framework.sax.SaxEventRecorder;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xchain.Catalog;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.Command;
import org.xchain.framework.lifecycle.Lifecycle;

/**
 * @author Christian Trimble
 */
public class TestTransformFactory
  extends BaseTestSaxEvents
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/sax/transform-factory.xchain";
  public static String XCHAIN_NAMESPACE_URI = "http://www.xchain.org/core/1.0";
  public static QName DEFAULT_TRANSFORMER_FACTORY = new QName("", "default-transformer-factory");
  public static QName XALAN_TRANSFORMER_FACTORY = new QName("", "xalan-transformer-factory");
  public static QName XSLTC_TRANSFORMER_FACTORY = new QName("", "xsltc-transformer-factory");
  public static QName SAXON_TRANSFORMER_FACTORY = new QName("", "saxon-transformer-factory");
  public static QName JOOST_TRANSFORMER_FACTORY = new QName("", "joost-transformer-factory");
  public static QName MIXED_TRANSFORMER_FACTORY = new QName("", "mixed-transformer-factory");

  protected Command command = null;

  public TestTransformFactory()
  {
    this.catalogUri = CATALOG_URI;
  }

  @Test public void testDefaultTransformerFactory()
    throws Exception
  {
    executeAndTest(DEFAULT_TRANSFORMER_FACTORY, "1");
  }

  @Test public void testXalanTransformerFactory()
    throws Exception
  {
    executeAndTest(XALAN_TRANSFORMER_FACTORY, "1");
  }

  @Test public void testXsltcTransformerFactory()
    throws Exception
  {
    executeAndTest(XSLTC_TRANSFORMER_FACTORY, "1");
  }

  @Test public void testSaxonTransformerFactory()
    throws Exception
  {
    executeAndTest(SAXON_TRANSFORMER_FACTORY, "1");
  }

  @Test public void testJoostTransformerFactory()
    throws Exception
  {
    executeAndTest(JOOST_TRANSFORMER_FACTORY, "1");
  }

  @Test public void testMixedTransformerFactory()
    throws Exception
  {
    executeAndTest(MIXED_TRANSFORMER_FACTORY, "5");
  }

  private void executeAndTest( QName catalogName, String transformCount )
    throws Exception
  {
    Command command = catalog.getCommand(catalogName);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackCharactersEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the sax events that were produced.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "transform-count", null);
    assertCharacters(eventIterator, transformCount);
    assertEndElement(eventIterator, "", "transform-count");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

}
  
