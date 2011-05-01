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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.xchain.framework.util.ParserUtil.attributePattern;

import java.util.regex.Matcher;

import org.junit.Test;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class TestAttributePattern
{
  /**
   * Test name="value" attributes.
   */
  @Test public void testSimpleAttribute()
    throws Exception
  {
    // basic case.
    assertAttributeParameterValue("name=\"value\"", "name", "value");
    assertAttributeParameterValue("name='value'", "name", "value");
  }

  /**
   * Test name="" attributes
   */
  @Test public void testEmptyAttributeValue()
    throws Exception
  {
    // empty value
    assertAttributeParameterValue("name=\"\"", "name", "");
    assertAttributeParameterValue("name=''", "name", "");
  }

  /**
   * Test name="'" attributes.
   */
  @Test public void testAttributeValueOtherQuote()
    throws Exception
  {
    // value with other quote
    assertAttributeParameterValue("name=\"'\"", "name", "'");
    assertAttributeParameterValue("name='\"'", "name", "\"");
  }

  /**
   * Test {uri}local-name="value" attributes.
   */
  @Test public void testAttributeNameWithNamespace()
    throws Exception
  {
    // qname.
    assertAttributeParameterValue("{uri}name=\"value\"", "{uri}name", "value");
    assertAttributeParameterValue("{uri}name='value'", "{uri}name", "value");
  }

  /**
   * Test {uri}local-name  =  "value" attributes.
   */
  @Test public void testAttributeWhitespaceAroundEqual()
    throws Exception
  {
    // one whitespace before the equals
    assertAttributeParameterValue("{uri}name =\"value\"", "{uri}name", "value");
    assertAttributeParameterValue("{uri}name ='value'", "{uri}name", "value");

    // many whitespace before the equals
    assertAttributeParameterValue("{uri}name   =\"value\"", "{uri}name", "value");
    assertAttributeParameterValue("{uri}name   ='value'", "{uri}name", "value");

    // one whitespace after equals
    assertAttributeParameterValue("{uri}name= \"value\"", "{uri}name", "value");
    assertAttributeParameterValue("{uri}name= 'value'", "{uri}name", "value");

    // many whitespace after equals
    assertAttributeParameterValue("{uri}name=   \"value\"", "{uri}name", "value");
    assertAttributeParameterValue("{uri}name=   'value'", "{uri}name", "value");
  }

  /**
   * Tests strings that should not be attributes.
   */
  @Test public void testNotAttribute()
    throws Exception
  {
    assertNotAttribute("name name");
    assertNotAttribute("name = name");
    assertNotAttribute("name='value\"");
    assertNotAttribute("");
    assertNotAttribute("name=\"value");
    assertNotAttribute("name name=\"value\"");
  }

  /**
   * Parses the provided attribute and asserts that the attribute pattern finds the provided
   * name and value.
   */
  protected void assertAttributeParameterValue( String attribute, String name, String value )
    throws Exception
  {
    Matcher matcher = attributePattern.matcher(attribute);

    if( !matcher.lookingAt() ) {
      fail("Could not parse the attribute string:"+attribute);
    }

    assertEquals("The name of the attribute was not properly parsed.", name, matcher.group(1));
    assertEquals("The value of the attribute was not properly parsed.", value, matcher.group(3));
  }

  public void assertNotAttribute( String attribute )
    throws Exception
  {
    if( attributePattern.matcher(attribute).lookingAt() ) {
      fail("A non attribute string matched the attribute pattern:"+attribute);
    }
  }
}
