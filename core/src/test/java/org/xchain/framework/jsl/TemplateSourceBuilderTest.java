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
package org.xchain.framework.jsl;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
public class TemplateSourceBuilderTest
{
  public static Logger log = LoggerFactory.getLogger(TemplateSourceBuilderTest.class);

  @Before public void setUp()
  {
  }

  @After public void tearDown()
  {
  }

  @Test public void testSingleElementTemplate()
    throws Exception
  {
    TemplateSourceBuilder sourceBuilder = new TemplateSourceBuilder();

    sourceBuilder.startSource(new HashMap<String, String>(), new HashSet<String>(), true);

    sourceBuilder.startVirtualChain();
    sourceBuilder.appendCommandCall();
    sourceBuilder.startVirtualChain();
    sourceBuilder.startStartElement();
    sourceBuilder.appendAttributeValueTemplate("uri", "localName", "p:localName", "{/path}");
    sourceBuilder.appendStartElement("uri\r\\\t\"\'", "localName", "p:qName");
    sourceBuilder.endStartElement();
    sourceBuilder.startEndElement();
    sourceBuilder.appendEndElement("uri", "localName", "p:qName");
    sourceBuilder.endEndElement();
    sourceBuilder.endVirtualChain();
    sourceBuilder.endVirtualChain();
    
    //System.out.println(sourceBuilder.endSource().getSource());
  }

  @Test public void testAttributeValueTemplate()
    throws Exception
  {
    List<String> parsedExpression = TemplateSourceBuilder.parseAttributeValueTemplate("No attribute value template");

    assertEquals("The number of parts parsed was incorrect.", 1, parsedExpression.size());
    assertEquals("Incorrect value for fixed part.", "No attribute value template", parsedExpression.get(0));

    parsedExpression = TemplateSourceBuilder.parseAttributeValueTemplate("No attribute value template{/xpath}");

    assertEquals("The number of parts parsed was incorrect.", 2, parsedExpression.size());
    assertEquals("Incorrect value for fixed part.", "No attribute value template", parsedExpression.get(0));
    assertEquals("Incorrect value for dynamic part.", "/xpath", parsedExpression.get(1));

    parsedExpression = TemplateSourceBuilder.parseAttributeValueTemplate("No attribute value template {/xpath} more template.");

    assertEquals("The number of parts parsed was incorrect.", 3, parsedExpression.size());
    assertEquals("Incorrect value for fixed part.", "No attribute value template ", parsedExpression.get(0));
    assertEquals("Incorrect value for dynamic part.", "/xpath", parsedExpression.get(1));
    assertEquals("Incorrect value for fixed part.", " more template.", parsedExpression.get(2));

    parsedExpression = TemplateSourceBuilder.parseAttributeValueTemplate("No attribute {{value}} template {/xpath} more template.");

    assertEquals("The number of parts parsed was incorrect.", 3, parsedExpression.size());
    assertEquals("Incorrect value for fixed part.", "No attribute {value} template ", parsedExpression.get(0));
    assertEquals("Incorrect value for dynamic part.", "/xpath", parsedExpression.get(1));
    assertEquals("Incorrect value for fixed part.", " more template.", parsedExpression.get(2));

    parsedExpression = TemplateSourceBuilder.parseAttributeValueTemplate("");

    assertEquals("The number of parts parsed was incorrect.", 1, parsedExpression.size());
    assertEquals("Incorrect value for fixed part.", "", parsedExpression.get(0));

    parsedExpression = TemplateSourceBuilder.parseAttributeValueTemplate("{}");

    assertEquals("The number of parts parsed was incorrect.", 2, parsedExpression.size());
    assertEquals("Incorrect value for fixed part.", "", parsedExpression.get(0));
    assertEquals("Incorrect value for dynamic part.", "", parsedExpression.get(1));
  }
}
