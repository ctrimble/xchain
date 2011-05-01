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
package org.xchain.framework.doclets;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Test;
import org.w3c.tidy.Tidy;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class TestJTidy
{
  /**
   */
  @Test public void testEscapeHtmlFragment()
    throws Exception
  {
    StringBuffer sb = new StringBuffer();
    sb.append("This is a javadoc. It contains a lot of text.<br> ");
    sb.append("There is going to be images <img src=\"\"> and other single ");
    sb.append("tags in javadocs that need to have end tags added.");

    InputStream in = new ByteArrayInputStream(sb.toString().getBytes());
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    Tidy tidy = new Tidy();
    tidy.setPrintBodyOnly(true);
    tidy.setXmlOut(true);
    tidy.setSmartIndent(false);
    tidy.setQuiet(true);
    tidy.parse(in, out);

    StringBuffer expected = new StringBuffer();
    expected.append("This is a javadoc. It contains a lot of text.\n<br /> ");
    expected.append("There is going to be images \n<img src=\"\" /> and other single ");
    expected.append("tags in javadocs that need to have\nend tags added.\n");

    assertEquals("The output text was not properly formatted.", expected.toString(), out.toString());
  }
}
