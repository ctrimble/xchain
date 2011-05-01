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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.namespace.QName;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * Utility class for JXPathContext related methods.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
public class JXPathContextUtil
{
  public static Logger log = LoggerFactory.getLogger( JXPathContextUtil.class );

  /** A regex pattern the matches qNames of the form '{uri}local-name' */
  public static Pattern uriLocalNamePattern;
  /** A regex pattern that matches qNames of the form 'prefix:local-name' */
  public static Pattern prefixLocalNamePattern;
  /** A regex pattern that matches qNames of the form 'local-name' */
  public static Pattern localNamePattern;

  static {
    try {
      uriLocalNamePattern = Pattern.compile("\\{([^{]*)\\}(.+)");
      prefixLocalNamePattern = Pattern.compile("([^:]*):([^:]+)");
      localNamePattern = Pattern.compile("([^:]+)");
    }
    catch( PatternSyntaxException pse ) {
      if( log.isErrorEnabled() ) {
        log.error("Could not compile a qName pattern.", pse);
      }
    }
  }

  /**
   * Converts a string to a qName based on the mappings in the context object.
   */
  public static QName stringToQName( JXPathContext context, String value )
  {
    Matcher matcher = uriLocalNamePattern.matcher(value);
    if( matcher.matches() ) {
      String uri = matcher.group(1);
      String localName = matcher.group(2);
      return new QName(uri, localName);
    }

    matcher = prefixLocalNamePattern.matcher(value);
    if( matcher.matches() ) {
      String prefix = matcher.group(1);
      String localName = matcher.group(2);

      // get the uri for the prefix.
      String uri = context.getNamespaceURI(prefix);

      if( uri != null ) {
        return new QName(uri, localName);
      }
      else {
        throw new RuntimeException("Could not find uri for prefix '"+prefix+"'.");
      }
    }

    matcher = localNamePattern.matcher(value);
    if( matcher.matches() ) {
      return new QName( matcher.group(1) );
    }

    throw new RuntimeException("Could not parse the qName '"+value+"'.");
  }

  public static String stringToQNameString( JXPathContext context, String value )
  {
    return stringToQName(context, value).toString();
  }

  /**
   * <p>A conversion utility that will properly convert enumeration types.  All other conversion are done with
   * org.apache.commons.beanutils.ConvertUtils.convert( Object, Class ).</p>
   *
   * @param value the value to convert.
   * @param type the target type for the value.
   * @return the converted type.
   */
  public static Object convert( Object value, Class type )
  {
    if( Enum.class.isAssignableFrom(type) ) {
      //System.out.println("The type "+type+" is assignable to Enum.");
      Object result = org.apache.commons.beanutils.ConvertUtils.convert( value, type );
      if( result == null ) {
        return null;
      }
      else if( type.isAssignableFrom(result.getClass()) ) {
        return result;
      }
      else {
        return Enum.valueOf( type, result.toString() );
      }
    }
    else {
      //System.out.println("The type "+type+" is not assignable to Enum.");
      return org.apache.commons.beanutils.ConvertUtils.convert( value, type );
    }
  }

  public static void validate( String value, NamespaceContext namespaceContext )
  {
    Matcher matcher = uriLocalNamePattern.matcher(value);
    if( matcher.matches() ) {
      // this is valid, so return.
      return;
    }

    matcher = prefixLocalNamePattern.matcher(value);
    if( matcher.matches() ) {
      String prefix = matcher.group(1);

      if( prefix != null && !XMLConstants.DEFAULT_NS_PREFIX.equals(prefix) && XMLConstants.NULL_NS_URI.equals(namespaceContext.getNamespaceURI(prefix)) ) {
        throw new JXPathException("There is not namespace mapping for the prefix of qname '"+value+"'.");
      }
      return;
    }

    matcher = localNamePattern.matcher(value);
    if( matcher.matches() ) {
      return;
    }

    throw new JXPathException("There is a syntax error in the qname '"+value+"'.");
  }
}
