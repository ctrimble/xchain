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
import static org.junit.Assert.fail;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.xchain.Command;
import org.xchain.framework.sax.SaxEventRecorder;

/**
 * @author Christian Trimble
 */
public class TestException
  extends BaseTestSaxEvents
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/jsl/test-exception.xchain";
  public static String JSL_NAMESPACE_URI = "http://www.xchain.org/jsl/1.0";
  public static QName EXCEPTION_IN_JSL_TEMPLATE = new QName(JSL_NAMESPACE_URI, "exception-in-jsl-template");
  public static QName EXCEPTION_IN_ELEMENT = new QName(JSL_NAMESPACE_URI, "exception-in-element");

  protected Command command = null;

  public TestException()
  {
    catalogUri = CATALOG_URI;
  }

  /**
   * Varifies that exceptions thrown inside the &lt;jsl:template/&gt; element are propagated.
   */
  @Test public void testExceptionInJslTemplate()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(EXCEPTION_IN_JSL_TEMPLATE);

    try {
      // execute the command.
      command.execute(context);
      fail("An exception thrown in a jsl template did not come out of the chain.");
    }
    catch( Exception e ) {
      // this should happen.
    }
  }
}
