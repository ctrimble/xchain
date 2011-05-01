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
package org.xchain.framework.util;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Properties;

import org.w3c.tidy.Tidy;

/**
 * Utility class for converting HTML fragments into proper XML.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class HtmlUtil
{
  /**
   * Convert the given HTML fragment into proper XML.
   * @param htmlFragment The HTML fragment to convert.
   * @return The equivalent HTML fragment in proper XML.
   * @throws Exception
   */
  public static String htmlFragmentToXmlFragment( String htmlFragment )
    throws Exception
  {
    StringReader in = new StringReader(htmlFragment);
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    Properties properties = new Properties();

    properties.setProperty("show-body-only", "true");
    properties.setProperty("output-xml", "true");
    properties.setProperty("quiet", "true");
    properties.setProperty("wrap", "0");
    properties.setProperty("new-pre-tags", "code");

    Tidy tidy = new Tidy();
    tidy.setConfigurationFromProps(properties);
    tidy.parse(in, out);

    return out.toString();
  }
}
