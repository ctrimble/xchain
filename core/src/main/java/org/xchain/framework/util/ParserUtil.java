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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.xchain.framework.util.RegExUtil.compilePattern;

/**
 * Utility class for performing parsing operations.
 *
 * @author Devon Tackett
 * @author Christian Trimble
 * @author Jason Rose
 * @author Josh Kennedy
 */
public class ParserUtil
{
  private static Logger log = LoggerFactory.getLogger(ParserUtil.class);

  static Pattern attributesBoundaryPattern;
  static Pattern featuresBoundaryPattern;
  static Pattern parametersBoundaryPattern;
  static Pattern outputPropertiesBoundaryPattern; 
  static Pattern attributePattern;
  static Pattern whitespacePattern;
  static Pattern remainderOfString;
  static Pattern featurePattern;
  
  private static final Map<String, String> xmlEntityMap = new HashMap<String, String>();
  private static Pattern xmlEscapePattern;
  
  static {
    attributePattern = compilePattern("\\s*([^\\s=]+)\\s*=\\s*(\"|\')((?:(?!\\2).)*)\\2", log, "Could not compile attribute pattern.");
    featurePattern = compilePattern("\\s*([^\\s=]+)\\s*=\\s*(\"|\')\\s*(true|false)\\s*\\2", log, "Could not compile feature pattern.");
    attributesBoundaryPattern = compilePattern("\\s*attributes(?=\\s|\\Z)", log, "The features boundary pattern did not compile.");
    featuresBoundaryPattern = compilePattern("\\s*features(?=\\s|\\Z)", log, "The features boundary pattern did not compile.");
    parametersBoundaryPattern = compilePattern("\\s*parameters(?=\\s|\\Z)", log, "The parameters boundary pattern did not compile.");
    outputPropertiesBoundaryPattern = compilePattern("\\s*output\\s+properties(?=\\s|\\Z)", log, "The output properties boundary pattern did not compile.");
    whitespacePattern = compilePattern("\\G(\\s+|\\Z)", log, "Could not compile whitespace pattern");
    remainderOfString = compilePattern(".*", log, "Could not compile start pattern.");
    xmlEscapePattern = compilePattern("&(?:([a-zA-Z|_|:]\\w*)|#(x?)(\\d*))(;?)", log, "Could not compilet xml escape pattern.");
    
    // Build the XML Entity map
    xmlEntityMap.put("quot", "\"");
    xmlEntityMap.put("apos", "\'");
    xmlEntityMap.put("amp", "&");
    xmlEntityMap.put("lt", "<");
    xmlEntityMap.put("gt", ">");
  }

  /**
   * Unescape XML escape characters.
   * 
   * @param xmlData The XML escaped string.
   * 
   * @return The unescaped string.
   */
  public static String unescapeXML(String xmlData)
    throws ParseException
  {
    StringBuffer escapedData = new StringBuffer();
    Matcher match = xmlEscapePattern.matcher(xmlData);
    
    while (match.find()) {
      String entity = match.group(1);
      String hex = match.group(2);
      String charCode = match.group(3);
      String terminator = match.group(4);
      
      if (terminator != null && terminator.trim().length() != 0) {
        if (entity != null) {
          if (xmlEntityMap.containsKey(entity))
              match.appendReplacement(escapedData, xmlEntityMap.get(entity));
          else 
            throw new ParseException("Unknown escape entity.", entity, match.regionEnd());
        } else if (charCode != null) {
          int code;
          if (hex != null && hex.trim().length() != 0)
            // Hex (base 16)
            code = Integer.parseInt(charCode, 16);
          else
            // Decimal (base 10, default)
            code = Integer.parseInt(charCode);
          
          match.appendReplacement(escapedData, Character.toString((char)code));
        } else {
          throw new ParseException("No escape data found.", match.regionEnd());
        }
      } else {
        // Throw exception
        throw new ParseException("Improper data. & found but not terminated with ;", match.regionEnd());
      }
    }
    match.appendTail(escapedData);

    return escapedData.toString();
  }  
  
  /**
   * Unescape parameter characters.
   * 
   * \\ to \
   * \' to '
   * \" to "
   * 
   * @param parameterValue The escaped string.
   * 
   * @return The unescaped string.
   */
  public static String unescapeParameter(String parameterValue)
  {
    return parameterValue.replaceAll("\\\\(\"|\'|\\\\)", "$1");
  }   
 
  /**
   * Parse a transformer processing instruction.
   * 
   * @param data The data to parse.
   * @return A ParsedTransformer object containing the properties from the given data string.
   * 
   * @throws ParseException If the given input data could not be properly parsed.
   */
  public static ParsedTransformer parseTransformer( String data )
    throws ParseException
  {
    ParsedTransformer parsedTransformer = null;

    // create a matcher to pass to the other functions.
    Matcher matcher = whitespacePattern.matcher( data );

    // consume any leading whitespace.
    if( matcher.lookingAt() ) {
      advanceRegion(matcher);
    }

    // parse the rest of the data.
    parsedTransformer = parseTransformer( matcher );

    // consume any tailing whitespace.
    if( matcher.lookingAt()) {
      advanceRegion(matcher);
    }

    // Make sure the entire string has been consumed.
    if( matcher.regionStart() != matcher.regionEnd() ) {
      throw new ParseException("Extra characters found.", data, matcher.regionStart());
    }

    return parsedTransformer;
  }

  /**
   * Parse a transformer processing instruction.  The given matcher will have the attributes,
   * parameters and output properties consumed.  If properly parsed only whitespace should be
   * left in the matcher.
   * 
   * @param matcher A matcher containing the parameter data with no leading whitespace.
   * @return A ParsedTransform object containing the properties from the matcher.
   * 
   * @throws ParseException If the given input could not be properly parsed.
   */
  private static ParsedTransformer parseTransformer( Matcher matcher )
    throws ParseException
  {
    ParsedTransformer parsedTransformer = new ParsedTransformer();

    // Retrieve the original pattern.
    Pattern originalPattern = matcher.pattern();
    try {
      // Parse the attributes.
      parsedTransformer.setAttributes( parseAttributeMap( matcher ) );

      if( matcher.usePattern(parametersBoundaryPattern).lookingAt() ) {
        // Matched on the parameter boundary pattern.
        // Advance past the boundary pattern.
        advanceRegion(matcher);
        // Parse the parameters.
        parsedTransformer.setParameters(parseAttributeMap( matcher ));
      }
      else {
        // Did not find the parameter boundary pattern.  Assume no parameters.
        parsedTransformer.setParameters(new HashMap<String, String>());
      }

      if( matcher.usePattern(outputPropertiesBoundaryPattern).lookingAt() ) {
        // Matched on the output properties boundary pattern.
        // Advance past the boundary pattern.
        advanceRegion(matcher);
        // Parse the output properties.
        parsedTransformer.setOutputProperties(parseAttributeMap( matcher ));
      }
      else {
        // No match on the output properties boundary pattern.  Assume no output properties.
        parsedTransformer.setOutputProperties(new HashMap<String, String>());
      }
    }
    finally {
      // Restore the original pattern.
      matcher.usePattern(originalPattern);
    }

    return parsedTransformer;
  }

  /**
   * Parsed the data for an xchain-transformer-factory processing instruction.
   */
  public static ParsedTransformerFactory parseTransformerFactory( String data )
    throws ParseException
  {
    ParsedTransformerFactory parsedTransformerFactory = null;

    // create a matcher to pass to the other functions.
    Matcher matcher = whitespacePattern.matcher( data );

    // consume any leading whitespace.
    if( matcher.lookingAt() ) {
      advanceRegion(matcher);
    }

    // parse the rest of the data.
    parsedTransformerFactory = parseTransformerFactory( matcher );

    // consume any tailing whitespace.
    if( matcher.lookingAt()) {
      advanceRegion(matcher);
    }

    // Make sure the entire string has been consumed.
    if( matcher.regionStart() != matcher.regionEnd() ) {
      throw new ParseException("Extra characters found.", data, matcher.regionStart());
    }

    return parsedTransformerFactory;
  }

  private static ParsedTransformerFactory parseTransformerFactory( Matcher matcher )
    throws ParseException
  {
    ParsedTransformerFactory parsedTransformerFactory = new ParsedTransformerFactory();

    // Retrieve the original pattern.
    Pattern originalPattern = matcher.pattern();
    try {
      // Parse the attributes.
      parsedTransformerFactory.setFields( parseAttributeMap( matcher ) );

      if( matcher.usePattern(featuresBoundaryPattern).lookingAt() ) {
        // Matched on the features boundary pattern.
        // Advance past the boundary pattern.
        advanceRegion(matcher);
        // Parse the features.
        parsedTransformerFactory.setFeatures(parseFeatureMap( matcher ));
      }
      else {
        // No match on the features boundary pattern.  Assume no features.
        parsedTransformerFactory.setFeatures(new HashMap<String, Boolean>());
      }

      if( matcher.usePattern(attributesBoundaryPattern).lookingAt() ) {
        // Matched on the parameter boundary pattern.
        // Advance past the boundary pattern.
        advanceRegion(matcher);
        // Parse the parameters.
        parsedTransformerFactory.setAttributes(parseAttributeMap( matcher ));
      }
      else {
        // Did not find the parameter boundary pattern.  Assume no parameters.
        parsedTransformerFactory.setAttributes(new HashMap<String, String>());
      }
    }
    finally {
      // Restore the original pattern.
      matcher.usePattern(originalPattern);
    }

    return parsedTransformerFactory;
  }
  /**
   * Parse the given matcher into a map of attribute name to attribute value.
   * 
   * @param matcher The matcher to parse through.
   * @return A mapping of attribute name to attribute value.
   * 
   * @throws ParseException If an error was encountered parsing the given data.
   */
  public static Map<String, String> parseAttributeMap( Matcher matcher )
    throws ParseException
  {
    Map<String, String> attributeMap = new HashMap<String, String>();

    Pattern originalPattern = matcher.pattern();
    try {

      // set the pattern for attributes.
      matcher.usePattern(attributePattern);

      // consume as many attributes as possible.
      while(matcher.lookingAt()) {
        // xml unescape both groups.
        attributeMap.put(unescapeXML(matcher.group(1)), unescapeXML(matcher.group(3)));
        advanceRegion(matcher);
      }
    }
    finally {
      matcher.usePattern(originalPattern);
    }

    return attributeMap;
  }

  public static Map<String, Boolean> parseFeatureMap( Matcher matcher )
    throws ParseException
  {
    Map<String, Boolean> featureMap = new HashMap<String, Boolean>();

    Pattern originalPattern = matcher.pattern();
    try {
      matcher.usePattern(featurePattern);

      // consume as many geatures as possible.
      featureMap.put(unescapeXML(matcher.group(1)), Boolean.valueOf(matcher.group(3)));
     
    }
    finally {
      matcher.usePattern(originalPattern);
    }

    return featureMap;
  }

  /**
   * Advances the region past the last match.
   * 
   * @param matcher The matcher to advance.
   * @return The matcher advanced to the next region.
   */
  public static Matcher advanceRegion( Matcher matcher )
  {
    return matcher.region(matcher.regionStart()+matcher.group().length(), matcher.regionEnd());
  }
}
