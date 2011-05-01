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

import static org.xchain.framework.util.ParserUtil.advanceRegion;
import static org.xchain.framework.util.RegExUtil.compilePattern;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Matcher;
import org.xchain.framework.util.ParseException;

/**
 * A parser for OSGi Manifest headers.
 * TODO: Rework error handling in this file.
 * TODO: Remove unneeded regular expressions from file.
 * TODO: Combine expressions where possible to reduce the ammount of matching.
 *
 * @author Christian Trimble
 * @author John Trimble
 */
public class ManifestParser
{
  private static String DIGIT_REGEX = "[0-9]";
  private static String ALPHA_REGEX = "[a-zA-Z]";
  private static String ALPHA_NUM_REGEX = "[a-zA-Z0-9]";
  private static String TOKEN_REGEX = "[a-zA-Z0-9_-]+";
  private static String ZERO_OR_MORE_WHITESPACE_REGEX = "\\s*";
  private static String ONE_OR_MORE_WHITESPACE_REGEX = "\\s+";

  /**
   *   extended ::= ( alphanum | ’_’ | ’-’ | ’.’ )+
   */
  public static final String EXTENDED_REGEX = "[-a-zA-Z0-9_.]+";

  /**
   *   quoted-string::= ’"’ ( [^"\#x0D#x0A#x00] | ’\"’|’\\’)* ’"’
   */
  public static final String QUOTED_STRING_REGEX = "\"[^\"\\\\\\x0D\\x0A\\x00]*\"";
  public static final String COLON_EQUALS_REGEX = ":=";
  public static final String EQUALS_REGEX = "=";

  /**
   *   This has been changed to allow spaces and tabs.
   *   path ::= path-unquoted | (’"’ path-unquoted ’"’)
   *   path-unquoted::= path-sep | path-sep? path-element (path-sep path-element)*
   *   path-element ::= [^/"\#x0D#x0A#x00]+ 
   *   path-sep ::= ’/’
   */
  public static final String QUOTED_PATH_REGEX = "\"((?:/?[^/\"\\\\\\x0D\\x0A\\x00]+(?:/[^/\"\\\\\\x0D\\x0A\\x00]+)*)|/)\"";

  /**
   *   This has been modified to remove ';' and ',' characters.  Without these changes, this could consume too many characters.
   *   path ::= path-unquoted | (’"’ path-unquoted ’"’)
   *   path-unquoted::= path-sep | path-sep? path-element (path-sep path-element)*
   *   path-element ::= [^/"\#x0D#x0A#x00]+ 
   *   path-sep ::= ’/’
   */
  public static final String UNQUOTED_PATH_REGEX = "(?:(?:/?[^/\"\\\\\\x0D\\x0A\\x00;,]+(?:/[^/\"\\\\\\x0D\\x0A\\x00;,]+)*)|/)";

  public static final String START_PARAMETER_REGEX = TOKEN_REGEX+"\\s*:?=";
  public static final String QUOTE_REGEX = "\"";
  public static final String SEMICOLON_REGEX = ";";

  static Pattern TOKEN_PATTERN = null;
  static Pattern EXTENDED_PATTERN = null;
  static Pattern QUOTED_STRING_PATTERN = null;
  static Pattern COLON_EQUALS_PATTERN = null;
  static Pattern EQUALS_PATTERN = null;
  static Pattern ZERO_OR_MORE_WHITESPACE_PATTERN = null;
  static Pattern ONE_OR_MORE_WHITESPACE_PATTERN = null;
  static Pattern QUOTED_PATH_PATTERN = null;
  static Pattern UNQUOTED_PATH_PATTERN = null;
  static Pattern START_PARAMETER_PATTERN = null;
  static Pattern QUOTE_PATTERN = compilePattern(QUOTE_REGEX, "Could not compile quote pattern.");
  static Pattern SEMICOLON_PATTERN = compilePattern(SEMICOLON_REGEX, "Could not compile semicolon pattern.");
  static Pattern COMMA_PATTERN = compilePattern(",", "Could not compile comma pattern.");

  static {
    TOKEN_PATTERN = compilePattern(TOKEN_REGEX, "Could not compile token pattern.");
    EXTENDED_PATTERN = compilePattern(EXTENDED_REGEX, "Could not compile extended pattern.");
    QUOTED_STRING_PATTERN = compilePattern(QUOTED_STRING_REGEX, "Could not compile quoted-string pattern.");
    COLON_EQUALS_PATTERN = compilePattern(COLON_EQUALS_REGEX, "Could not compile colon equals pattern.");
    EQUALS_PATTERN = compilePattern(EQUALS_REGEX, "Could not compile equals pattern.");
    ZERO_OR_MORE_WHITESPACE_PATTERN = compilePattern(ZERO_OR_MORE_WHITESPACE_REGEX, "Could not compile whitespace pattern.");
    ONE_OR_MORE_WHITESPACE_PATTERN = compilePattern(ONE_OR_MORE_WHITESPACE_REGEX, "Could not compile whitespace pattern.");
    QUOTED_PATH_PATTERN = compilePattern(QUOTED_PATH_REGEX, "Could not compile the quoted path pattern.");
    UNQUOTED_PATH_PATTERN = compilePattern(UNQUOTED_PATH_REGEX, "Could not compile the unquoted path pattern.");
    START_PARAMETER_PATTERN = compilePattern(START_PARAMETER_REGEX, "Could not compile the start parameter pattern.");
  }

  /**
   * This method parses the Bundle-ClassPath manifest header.
   *
   * Bundle-ClassPath BNF: (OSGi 4.2 Specification - 3.8.1)
   *   Bundle-ClassPath::=  entry ( ’,’ entry )* 
   *   entry ::= target ( ’;’ target )* ( ’;’ parameter ) * 
   *   target ::= path | ’.’
   *
   * Path BNF: (OSGi 4.2 Specification - 1.3.2)
   *   path ::= path-unquoted | (’"’ path-unquoted ’"’) 
   *   path-unquoted::= path-sep | path-sep? path-element (path-sep path-element)* 
   *   path-element ::= [^/"\#x0D#x0A#x00]+ 
   *   path-sep ::= ’/’
   */
  public static List<ParsedClassPathEntry> parseClassPathEntries( String bundleClassPath )
    throws Exception
  {
    List<ParsedClassPathEntry> entryList = new ArrayList<ParsedClassPathEntry>();

    // create the matcher.
    Matcher matcher = ZERO_OR_MORE_WHITESPACE_PATTERN.matcher(bundleClassPath);

    // remove leading whitespace.
    consumeWhitespace(matcher, false);

    // get the first entry.
    ParsedClassPathEntry entry = parseClassPathEntry(matcher);
    entryList.add(entry);
    consumeWhitespace(matcher, false);

    while( lookingAt(matcher, COMMA_PATTERN) ) {
      advanceRegion(matcher);
      consumeWhitespace(matcher, false);
      entry = parseClassPathEntry(matcher);
      entryList.add(entry);
      consumeWhitespace(matcher, false);
    }

    consumeWhitespace(matcher, false);

    // verify that we are at the end of the matcher.
    // TODO: Make sure that we consumed all of the header.

    return entryList;
  }

  static ParsedClassPathEntry parseClassPathEntry( Matcher matcher )
    throws Exception
  {
    ParsedClassPathEntry entry = new ParsedClassPathEntry();

    Pattern originalPattern = matcher.pattern();

    try {
      entry.getTargetList().add(parseTarget(matcher));
      consumeWhitespace( matcher, false );

      // some lookahead is needed here to properly parse this part of the grammar.
      while( lookingAt(matcher, SEMICOLON_PATTERN) ) {
        int regionStart = matcher.regionStart();
        advanceRegion( matcher );
        consumeWhitespace( matcher, false );

        // if this could not be a parameter, then consume the token.
        if( !lookingAt(matcher, START_PARAMETER_PATTERN) ) {
          entry.getTargetList().add(parseTarget(matcher));
          consumeWhitespace( matcher, false );
        }
        else {
          // reset the region, so that we can parse the parameters.
          matcher.region(regionStart, matcher.regionEnd());
        }
      }

      while( lookingAt(matcher, SEMICOLON_PATTERN) ) {
        advanceRegion( matcher );
        consumeWhitespace( matcher, false );
        entry.getParameterList().add(parseParameter(matcher));
      }
    }
    finally {
      matcher.usePattern(originalPattern);
    }

    return entry;
  }

  /**
   *   target ::= path | ’.’
   *   path ::= path-unquoted | (’"’ path-unquoted ’"’) 
   *   path-unquoted::= path-sep | path-sep? path-element (path-sep path-element)* 
   *   path-element ::= [^/"\#x0D#x0A#x00]+ 
   *   path-sep ::= ’/’
   */
  static String parseTarget( Matcher matcher )
    throws Exception
  {
    Pattern originalPattern = matcher.pattern();

    try {
      if( lookingAt(matcher, QUOTE_PATTERN) ) {
        return parseQuotedPath(matcher);
      }
      else {
        return parseUnquotedPath(matcher);
      }
    }
    finally {
      matcher.usePattern(originalPattern);
    }
  }

  static String parseQuotedPath( Matcher matcher )
    throws Exception
  {
    Pattern originalPattern = matcher.pattern();
    try {
      if( lookingAt( matcher, QUOTED_PATH_PATTERN ) ) {
        String path = matcher.group(1);
        advanceRegion( matcher );
        return path;
      }
      else {
        throw new RuntimeException("Could not parse quoted path.");
      }
    }
    finally {
      matcher.usePattern(originalPattern);
    }
  }

  static String parseUnquotedPath( Matcher matcher )
    throws Exception
  {
    Pattern originalPattern = matcher.pattern();
    try {
      if( lookingAt( matcher, UNQUOTED_PATH_PATTERN ) ) {
        String path = matcher.group().trim();
        advanceRegion( matcher );
        return path;
      }
      else {
        throw new RuntimeException("Could not parse quoted path.");
      }
    }
    finally {
      matcher.usePattern(originalPattern);
    }
  }

  /**
   * Parses a parameter from the specified matcher.  The matcher passed to this method must be looking at the start of a parameter.
   *
   *   parameter ::= directive | attribute 
   *   directive ::= token ’:=’ argument 
   *   attribute ::= token ’=’ argument
   *
   * @param matcher the matcher to parse the parameter from.
   * @return the parsed parameter.
   */
  static ParsedParameter parseParameter( Matcher matcher )
    throws Exception
  {
    ParsedParameter parameter = new ParsedParameter();

    Pattern originalPattern = matcher.pattern();
    try {
      if( lookingAt(matcher, TOKEN_PATTERN) ) {
        parameter.setName(matcher.group());
        advanceRegion(matcher);
      }
      else {
        throw new RuntimeException("Could not find token at start of parameter.");
      }

      consumeWhitespace(matcher, false);

      if( lookingAt( matcher, COLON_EQUALS_PATTERN ) ) {
        parameter.setType(ParameterType.DIRECTIVE);
        advanceRegion(matcher);
      }
      else if( lookingAt( matcher, EQUALS_PATTERN ) ) {
        parameter.setType(ParameterType.ATTRIBUTE);
        advanceRegion(matcher);
      }
      else {
        throw new RuntimeException("Expecting := or =");
      }

      consumeWhitespace(matcher, false);

      parameter.setValue(parseArgument(matcher));
    }
    finally {
      matcher.usePattern(originalPattern);
    }

    return parameter;
  }

  /**
   * @param matcher the matcher that the whitespace will be consumed from.
   * @param mandatory if true, the whitespace in this location mandatory, otherwise
   *                   the whitespace is optional.
   */
  static void consumeWhitespace( Matcher matcher, boolean mandatory )
    throws Exception
  {
    Pattern originalPattern = matcher.pattern();

    try {
      if( lookingAt( matcher, mandatory ? ONE_OR_MORE_WHITESPACE_PATTERN : ZERO_OR_MORE_WHITESPACE_PATTERN ) ) {
        advanceRegion(matcher);
      }
      else {
        throw new RuntimeException("Expected whitespace, but there was none.");
      }
    }
    finally {
      matcher.usePattern(originalPattern);
    }
  }

  /**
   * Perses an argument from the OSGi Core Specification.  Any quoted string are unescaped by this method.
   *
   *   extended ::= ( alphanum | ’_’ | ’-’ | ’.’ )+ 
   *   quoted-string::= ’"’ ( [^"\#x0D#x0A#x00] | ’\"’|’\\’)* ’"’
   *   argument ::= extended  | quoted-string 
   *
   */
  static String parseArgument( Matcher matcher )
    throws Exception
  {
    Pattern originalPattern = matcher.pattern();

    try {
      if( lookingAt( matcher, EXTENDED_PATTERN ) ) {
        return matcher.group();
      }
      else if( lookingAt( matcher, QUOTED_STRING_PATTERN ) ) {
        return unescapeQuotedString(matcher.group());
      }
      // TODO: Better error handling here.
      throw new RuntimeException("Could not find argument.");
    }
    finally {
      matcher.usePattern(originalPattern);
    }
  }

  /**
   * Removed the surrounding quotation marks and unescapes '"' and '\' characters in a quoted string.  This method assumes
   * that the string passed in conforms to the definition of a quoted-string found in the osgi core specification.  If the string
   * is malformed, the results of this method are unspecified.
   *
   * @param quotedString a string that conforms the quoted-string BNF found in the OSGi Core Specification.
   * @return the unescaped string.
   */
  static String unescapeQuotedString(String quotedString)
  {
    return quotedString.replaceAll("\\A\"(.*)\"\\Z", "$1").replaceAll("\\\\([\\\\\"])", "$1");
  }

  /**
   *   digit ::= [0..9] 
   *   alpha ::= [a..zA..Z] 
   *   alphanum ::= alpha | digit 
   *   token ::= ( alphanum | ’_’ | ’-’ )+
   */
  static String parseToken( Matcher matcher )
    throws ParseException
  {
    Pattern originalPattern = matcher.pattern();

    try {
      matcher.usePattern(TOKEN_PATTERN);

      if( matcher.lookingAt() ) {
        return matcher.group();
      }
      else {
        throw new RuntimeException("Could not parse token at "+matcher.regionStart());
      }
    }
    finally {
      matcher.usePattern(originalPattern);
    }
  }

  /**
   * Returns true if the matcher is looking at the specified pattern, false otherwise.  If true is returned, the pattern for the matcher is the pattern specified.
   * If false is returned, then the pattern for the matcher is not changed.
   * NOTE: This should be moved to the RegexUtil.
   */
  static boolean lookingAt( Matcher matcher, Pattern pattern )
    throws Exception
  {
    Pattern originalPattern = matcher.pattern();
    boolean lookingAt = false;

    try {
      matcher.usePattern(pattern);
      return (lookingAt = matcher.lookingAt());
    }
    finally {
      if( !lookingAt ) {
        matcher.usePattern(originalPattern);
      }
    }
  }
}
