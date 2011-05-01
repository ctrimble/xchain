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

import org.junit.Test;

/**
 * @author Devon Tackett
 * @author Christian Trimble
 */
public class TestParserUtil
{

  /**
   * XML unescape a simple string.
   */
  @Test public void xmlNoEscapeTest()
    throws Exception
  {
    String simple = "test";
    
    String result = ParserUtil.unescapeXML(simple);
    
    assertEquals("Unescaping a simple string failed.", simple, result);
  }
  
  /**
   * XML unescape a simple string with escape entities.
   */
  @Test public void xmlEscapeTest()
    throws Exception
  {
    String data = "&lt;this &quot;is&apos; a test&gt;";
    
    String result = ParserUtil.unescapeXML(data);
    
    assertEquals("Unescaping failed.", "<this \"is' a test>", result);  
  }
  
  /**
   * XML unescape a simple string with escape characters.
   */
  @Test public void xmlEscapeCharacterTest()
    throws Exception
  {
    String data = "&#84;&#101;&#x73;&#x74;";
    
    String result = ParserUtil.unescapeXML(data);
    
    assertEquals("Unescaping failed.", "Test", result);  
  }  
  
  /**
   * XML unescape an invalid entity.
   */
  @Test(expected = ParseException.class) public void xmlEscapeBadEntityFailureTest()
    throws Exception
  {
    String data = "this &bob; is invalid";
    
    ParserUtil.unescapeXML(data);
  }
  
  /**
   * XML unescape with a non-terminated escape.
   */
  @Test(expected = ParseException.class) public void xmlEscapeInvalidEscapeFailureTest()
    throws Exception
  {
    String data = "this &amp is invalid";
    
    ParserUtil.unescapeXML(data);
  }   
  
  /**
   * Parameter unescape a simple string.
   */
  @Test public void parameterNoEscapeTest()
    throws Exception
  {
    String simple = "test";
    
    String result = ParserUtil.unescapeParameter(simple);
    
    assertEquals("Unescaping a simple string failed.", simple, result);
  }
  
  /**
   * Parameter unescape a string with escape characters.
   */
  @Test public void parameterEscapeTest()
    throws Exception
  {
    String data = "\\\\te\\\"st\\'";
    
    String result = ParserUtil.unescapeParameter(data);
    
    assertEquals("Unescaping failed.", "\\te\"st'", result);   
  }  
}
