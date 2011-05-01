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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Matcher;
import org.xchain.framework.jxpath.JXPathValidator;
import javax.xml.namespace.NamespaceContext;

/**
 * Utility class for Attribute related methods.
 * 
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 *
 * @see org.xml.sax.Attributes
 */
public class AttributesUtil
{
  private static Logger log = LoggerFactory.getLogger(AttributesUtil.class);

  /** A regex that matches the fixed part of an attribute value template. */
  public static final String FIXED_PART_REGEX = "((?:[^{}]+|\\{\\{|\\}\\})+)";
  /** A regex that matches the dynamic part of an attribute value template. */
  public static final String DYNAMIC_PART_REGEX = "(?:\\{((?:[^\\}\'\"]*|\"[^\"]*\"|\'[^\']*\')*)\\})";

  /**
   * The pattern for attribute value templates.  The first group of this pattern is a fixed part, the second group is a dynamic part.  This
   * pattern will match one part at a time.
   */
  private static Pattern attributeValueTemplatePattern = null;

  static {
    try {
      attributeValueTemplatePattern = Pattern.compile(FIXED_PART_REGEX+"|"+DYNAMIC_PART_REGEX);
    }
    catch( PatternSyntaxException pse ) {
      log.error("Could not compile attribute value template pattern.", pse);
    }
  }

  /**
   * Get the attribute value.
   * 
   * @param attributes The list of attributes to check.
   * @param namespaceUri The namespace of the attribute.
   * @param localName The local name of the attribute.
   * 
   * @return The value of the attribute if found.  Null if not found.
   */
  public static String getAttribute( Attributes attributes, String namespaceUri, String localName )
  {
    return getAttribute( attributes, namespaceUri, localName, null );
  }

  
  /**
   * Get the attribute value.
   * 
   * @param attributes The list of attributes to check.
   * @param namespaceUri The namespace of the attribute.
   * @param localName The local name of the attribute.
   * @param defaultValue The default value to use if the attribute could not be found.
   * 
   * @return The value of the attribute if found.  The given defaultValue if not found.
   */
  public static String getAttribute( Attributes attributes, String namespaceUri, String localName, String defaultValue )
  {
    String value = defaultValue;

    // get the index of the target attribute.
    int index = attributes.getIndex( namespaceUri, localName );

    // if the index is 0 or greater, then return the value.
    if( index >= 0 ) {
      value = attributes.getValue( index );
    }

    return value;
  }

  /**
   * Parses an attribute value template into fixed and dynamic parts.  This list will always start with a fixed part and
   * then include alternating dynamic and fixed parts.
   */
  public static List<String> parseAttributeValueTemplate( String attributeValueTemplate )
    throws SAXException
  {
    // the result.
    ArrayList<String> result = new ArrayList<String>();

    // create the matcher.
    Matcher matcher = attributeValueTemplatePattern.matcher(attributeValueTemplate);

    while( matcher.lookingAt() ) {
      String fixedPart = matcher.group(1);
      String dynamicPart = matcher.group(2);

      if( result.isEmpty() && fixedPart == null ) {
        result.add("");
      }

      if( fixedPart != null ) {
        result.add(fixedPart.replaceAll("\\{\\{", "{").replaceAll("\\}\\}", "}"));
      }
      if( dynamicPart != null ) {
        result.add(dynamicPart);
      }
      matcher.region(matcher.regionStart()+matcher.group().length(), matcher.regionEnd());
    }

    if( !matcher.hitEnd() ) {
      throw new SAXException("The attribute value template '"+attributeValueTemplate+"' has an error between characters "+matcher.regionStart()+" and "+matcher.regionEnd()+".");
    }

    return result;
  }

  /**
   * Evaluates an attribute value template using the provided JXPathContext object.
   */
  public static String evaluateAttributeValueTemplate( JXPathContext context, String attributeValueTemplate )
    throws SAXException, Exception
  {
    List<String> parsedAvt = parseAttributeValueTemplate(attributeValueTemplate);

    // if there is just one part, then return the string.
    if( parsedAvt.size() == 1 ) {
      return parsedAvt.get(0);
    }

    // build a string buffer and start building the result in it.
    StringBuilder sb = new StringBuilder();
    for( int i = 0; i < parsedAvt.size(); i+=2 ) {
      sb.append(parsedAvt.get(i));
      if( (i+1) < parsedAvt.size() ) {
        sb.append((String)context.getValue(parsedAvt.get(i+1), String.class));
      }
    }
    return sb.toString();
  }

  public static void validateAttributeValueTemplate( String attributeValueTemplate, NamespaceContext namespaceContext )
    throws JXPathException
  {
    List<String> parsedAvt = null;
    try {
      parsedAvt = parseAttributeValueTemplate(attributeValueTemplate);
    }
    catch( SAXException saxe ) {
      throw new JXPathException(saxe.getMessage());
    }

    // validate the xpaths nested in the attribute value template.
    for( int i = 1; i < parsedAvt.size(); i+=2 ) {
      JXPathValidator.validate( parsedAvt.get(i), namespaceContext );
    }
  }
}
