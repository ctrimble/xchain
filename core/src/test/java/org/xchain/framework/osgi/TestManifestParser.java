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
package org.xchain.framework.osgi;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;
import org.junit.Ignore;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author Christian Trimble
 * @author John Trimble
 */
public class TestManifestParser
{
  //
  // Tests for the unescapeQuotedString method.
  //

  /**
   * Test unescaping a quoted string with no escape sequences.
   */
  @Test public void unescapeQuotedStringNoEscapes()
    throws Exception
  {
    testUnescapeQuotedString("\"test\"", "test");
  }

  @Test public void unescapeQuotedStringQuoteEscapes()
    throws Exception
  {
    testUnescapeQuotedString("test \\\"this\\\"", "test \"this\"");
  }

  @Test public void unescapeQuotedStringSlashEscapes()
    throws Exception
  {
    testUnescapeQuotedString("\"test \\\\'s that are escaped.\"", "test \\'s that are escaped.");
  }

  @Test public void unescapeQuotedStringSlashEscapesAtEnd()
    throws Exception
  {
    testUnescapeQuotedString("\"test \\\\'s that are at the end\\\\\"", "test \\'s that are at the end\\");
  }

  /**
   * A utility method for testing escape strings.
   */
  @Ignore public void testUnescapeQuotedString( String quotedString, String unquotedString )
    throws Exception
  {
    String result = ManifestParser.unescapeQuotedString(quotedString);
    assertEquals("Unescaping of a quoted string failed.", unquotedString, result);
  }

  //
  // Tests for the parseArgument method.
  //
  @Test public void argumentQuotedString()
    throws Exception
  {
    testParseArgument("\"test argument\"", "test argument", "");
  }

  @Test public void argumentQuotedStringWithTailingCharacters()
    throws Exception
  {
    testParseArgument("\"test argument\"\"another quoted string\"", "test argument", "\"another quoted string\"");
  }

  @Test public void argumentExtended()
    throws Exception
  {
    testParseArgument("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWZYZ0123456789_-.",
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWZYZ0123456789_-.", ""); 
  }

  @Test public void argumentExtendedWithTailingCharacters()
    throws Exception
  {
    testParseArgument("abc123_-.$#abc123_-.", "abc123_-.", "$#abc123_-.");
  }

  @Ignore public void testParseArgument( String argumentString, String expectedValue, String remainder )
    throws Exception
  {
    Pattern pattern = Pattern.compile(".*");
    Matcher matcher = pattern.matcher(argumentString);
    String argument = ManifestParser.parseArgument(matcher);
    assertEquals("The parsing of an argument failed.", expectedValue, argument);
    matcher.find();
    assertEquals("After parsing an argument, the remainder of the string was incorrect.", remainder, matcher.group());
  }

  //
  // Tests for the parseParameter method.
  //

  @Test public void parameterDirective()
    throws Exception
  {
    testParseParameter("name:=abc123_-.", "name", ParameterType.DIRECTIVE, "abc123_-.", "");
  }

  @Test public void parameterDirectiveWithTailingCharacters()
    throws Exception
  {
    testParseParameter("name:=abc123_-.$#", "name", ParameterType.DIRECTIVE, "abc123_-.", "$#");
  }

  @Test public void parameterAttribute()
    throws Exception
  {
    testParseParameter("name=abc123_-.", "name", ParameterType.ATTRIBUTE, "abc123_-.", "");
  }

  @Test public void parameterAttributeWithTailingCharacters()
    throws Exception
  {
    testParseParameter("name=abc123_-.$#", "name", ParameterType.ATTRIBUTE, "abc123_-.", "$#");
  }

  @Test public void parameterAttributeWithWhiteSpace()
    throws Exception
  {
    testParseParameter("name = abc123_-.", "name", ParameterType.ATTRIBUTE, "abc123_-.", "");
  }

  @Ignore public void testParseParameter( String parameterString, String expectedName, ParameterType expectedType, String expectedValue, String remainder )
    throws Exception
  {
    Pattern pattern = Pattern.compile(".*");
    Matcher matcher = pattern.matcher(parameterString);
    ParsedParameter parameter = ManifestParser.parseParameter(matcher);
    assertEquals("The name of a parsed parameter was wrong.", expectedName, parameter.getName());
    assertEquals("The type of a parsed parameter was wrong.", expectedType, parameter.getType());
    assertEquals("The value of a parsed parameter was wrong.", expectedValue, parameter.getValue());
    matcher.find();
    assertEquals("After parsing a parameter, the remainder of the string was incorrect.", remainder, matcher.group());
  }

  //
  // Test the parseTarget method.
  //
  @Test public void targetQuotedPath()
    throws Exception
  {
    testParseTarget("\"path\"", "path", "");
  }

  @Test public void targetQuotedSeparator()
    throws Exception
  {
    testParseTarget("\"/\"", "/", "");
  }

  @Test public void targetQuotedSeparatorPath()
    throws Exception
  {
    testParseTarget("\"/path\"", "/path", "");
  }

  @Test public void targetQuotedPathSeparatorPath()
    throws Exception
  {
    testParseTarget("\"path/path\"", "path/path", "");
  }

  @Test public void targetQuotedSeparatorPathSeperatorPath()
    throws Exception
  {
    testParseTarget("\"/path/path\"", "/path/path", "");
  }

  @Test public void targetQuotedSeparatorPathSeperatorPathWithAttribute()
    throws Exception
  {
    testParseTarget("\"/path/path\";name=value", "/path/path", ";name=value");
  }

  @Test public void targetQuotedSeparatorPathSeperatorPathWithDirective()
    throws Exception
  {
    testParseTarget("\"/path/path\" ; name := value", "/path/path", " ; name := value");
  }

  @Test public void targetUnquotedPath()
    throws Exception
  {
    testParseTarget("path", "path", "");
  }

  @Test public void targetUnquotedSeparator()
    throws Exception
  {
    testParseTarget("/", "/", "");
  }

  @Test public void targetUnquotedSeparatorPath()
    throws Exception
  {
    testParseTarget("/path", "/path", "");
  }

  @Test public void targetUnquotedPathSeparatorPath()
    throws Exception
  {
    testParseTarget("path/path", "path/path", "");
  } 
  
  @Test public void targetUnquotedSeparatorPathSeperatorPath()
    throws Exception
  {
    testParseTarget("/path/path", "/path/path", "");
  }

  @Test public void targetUnquotedSeparatorPathSeperatorPathWithAttribute()
    throws Exception
  {
    testParseTarget("/path/path;name=value", "/path/path", ";name=value");
  }

  @Test public void targetUnquotedSeparatorPathSeperatorPathWithDirective()
    throws Exception
  {
    testParseTarget("/path/path ; name := value", "/path/path", "; name := value");
  }

  @Ignore public void testParseTarget( String targetString, String expectedTarget, String remainder )
    throws Exception
  {
    Pattern pattern = Pattern.compile(".*");
    Matcher matcher = pattern.matcher(targetString);
    String target = ManifestParser.parseTarget(matcher);
    assertEquals("A parsed target was wrong.", expectedTarget, target);
    matcher.find();
    assertEquals("After parsing a target, the remainder of the string was incorrect.", remainder, matcher.group());
  }

  //
  // Test the parseBundleClassPath method.
  //

  @Ignore public void testParseBundleClassPath( String bundleClassPath, List<ParsedClassPathEntry> expectedEntryList )
    throws Exception
  {
    List<ParsedClassPathEntry> parsedEntryList = ManifestParser.parseClassPathEntries(bundleClassPath);
    
  }
}
