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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Port from Google Data
 * See: http://gdata-java-client.googlecode.com/svn/trunk/java/src/com/google/gdata/util/ContentType.java
 *
 * Simple class for parsing and generating Content-Type header values, per
 * RFC 2045 (MIME) and 2616 (HTTP 1.1).
 *
 * @author Mike Moulton
 */
public class ContentType {

  private static String TOKEN =
    "[\\p{ASCII}&&[^\\p{Cntrl} ;/=\\[\\]\\(\\)\\<\\>\\@\\,\\:\\\"\\?\\=]]+";

  // Precisely matches a token
  private static Pattern TOKEN_PATTERN = Pattern.compile(
    "^" + TOKEN + "$");

  // Matches a media type value
  private static Pattern TYPE_PATTERN = Pattern.compile(
    "(" + TOKEN + ")" +         // type  (G1)
    "/" +                       // separator
    "(" + TOKEN + ")" +         // subtype (G2)
    "\\s*(.*)\\s*", Pattern.DOTALL);

  // Matches an attribute value
  private static Pattern ATTR_PATTERN = Pattern.compile(
    "\\s*;\\s*" +
      "(" + TOKEN + ")" +       // attr name  (G1)
      "\\s*=\\s*" +
      "(?:" +
        "\"([^\"]*)\"" +        // value as quoted string (G3)
        "|" +
        "(" + TOKEN + ")?" +    // value as token (G2)
      ")"
    );

  /**
   * Name of the attribute that contains the encoding character set for
   * the content type.
   * @see #getCharset()
   */
  public static final String ATTR_CHARSET = "charset";

  /**
   * Special "*" character to match any type or subtype.
   */
  private static final String STAR = "*";

  /**
   * The UTF-8 charset encoding is used by default for all text and xml
   * based MIME types.
   */
  public static final String DEFAULT_CHARSET = ATTR_CHARSET + "=UTF-8";

  /**
   * A ContentType constant that describes the JSON content type.
   */
  public static final ContentType JSON =
    new ContentType("application/json;" + DEFAULT_CHARSET);

  /**
   * A ContentType constant that describes the Javascript content type.
   */
  public static final ContentType JAVASCRIPT =
    new ContentType("text/javascript;" + DEFAULT_CHARSET);

  /**
   * A ContentType constant that describes the generic text/xml content type.
   */
  public static final ContentType TEXT_XML =
    new ContentType("text/xml;" + DEFAULT_CHARSET);

  /**
   * A ContentType constant that describes the generic text/html content type.
   */
  public static final ContentType TEXT_HTML =
    new ContentType("text/html;" + DEFAULT_CHARSET);

  /**
   * A ContentType constant that describes the generic text/html content type.
   */
  public static final ContentType TEXT_XHTML =
    new ContentType("application/xhtml+xml;" + DEFAULT_CHARSET);

  /**
   * A ContentType constant that describes the generic text/plain content type.
   */
  public static final ContentType TEXT_PLAIN =
    new ContentType("text/plain;" + DEFAULT_CHARSET);

  /**
   * Determines the best "Content-Type" header to use in a servlet response
   * based on the "Accept" header from a servlet request.
   *
   * @param acceptHeader       "Accept" header value from a servlet request (not
   *                           <code>null</code>)
   * @param actualContentTypes actual content types in descending order of
   *                           preference (non-empty, and each entry is of the
   *                           form "type/subtype" without the wildcard char
   *                           '*') or <code>null</code> if no "Accept" header
   *                           was specified
   * @return the best content type to use (or <code>null</code> on no match).
   */
  public static ContentType getBestContentType(String acceptHeader,
      List<ContentType> actualContentTypes) {

    // If not accept header is specified, return the first actual type
    if (acceptHeader == null) {
      return actualContentTypes.get(0);
    }

    // iterate over all of the accepted content types to find the best match
    float bestQ = 0;
    ContentType bestContentType = null;
    String[] acceptedTypes = acceptHeader.split(",");
    for (String acceptedTypeString : acceptedTypes) {

      // create the content type object
      ContentType acceptedContentType;
      try {
        acceptedContentType = new ContentType(acceptedTypeString.trim());
      } catch (IllegalArgumentException ex) {
        // ignore exception
        continue;
      }

      // parse the "q" value (default of 1)
      float curQ = 1;
      try {
        String qAttr = acceptedContentType.getAttribute("q");
        if (qAttr != null) {
          float qValue = Float.valueOf(qAttr);
          if (qValue <= 0 || qValue > 1) {
            continue;
          }
          curQ = qValue;
        }
      } catch (NumberFormatException ex) {
        // ignore exception
        continue;
      }

      // only check it if it's at least as good ("q") as the best one so far
      if (curQ < bestQ) {
        continue;
      }

      /* iterate over the actual content types in order to find the best match
      to the current accepted content type */
      for (ContentType actualContentType : actualContentTypes) {

        /* if the "q" value is the same as the current best, only check for
        better content types */
        if (curQ == bestQ && bestContentType == actualContentType) {
          break;
        }

        /* check if the accepted content type matches the current actual
        content type */
        if (actualContentType.match(acceptedContentType)) {
          bestContentType = actualContentType;
          bestQ = curQ;
          break;
        }
      }
    }

    // if found an acceptable content type, return the best one
    if (bestQ != 0) {
      return bestContentType;
    }

    // Return null if no match
    return null;
  }

  public static boolean acceptsContentType(String acceptHeader, ContentType actualContentType) {
    // If not accept header is specified, return the first actual type
    if (acceptHeader == null || actualContentType == null) {
      return false;
    }

    // iterate over all of the accepted content types to find a match
    String[] acceptedTypes = acceptHeader.split(",");
    for (String acceptedTypeString : acceptedTypes) {

      // create the content type object
      ContentType acceptedContentType;
      try {
        acceptedContentType = new ContentType(acceptedTypeString.trim());
      } catch (IllegalArgumentException ex) {
        // ignore exception
        continue;
      }

      if (actualContentType.mediaTypeMatch(acceptedContentType)) {
        // Accept type found
        return true;
      }
    }

    // Return false if no match
    return false;
  }

 /**
   * Constructs a new instance with default media type
   */
  public ContentType() {
    this(null);
  }

  /**
   * Constructs a new instance from a content-type header value
   * parsing the MIME content type (RFC2045) format.  If the type
   * is {@code null}, then media type and charset will be
   * initialized to default values.
   *
   * @param typeHeader content type value in RFC2045 header format.
   */
  public ContentType(String typeHeader) {

    // If the type header is no provided, then use the HTTP defaults.
    if (typeHeader == null) {
      type = "application";
      subType = "octet-stream";
      attributes.put(ATTR_CHARSET, "iso-8859-1"); // http default
      return;
    }

    // Get type and subtype
    Matcher typeMatch = TYPE_PATTERN.matcher(typeHeader);
    if (!typeMatch.matches()) {
      throw new IllegalArgumentException("Invalid media type:" + typeHeader);
    }

    type = typeMatch.group(1).toLowerCase();
    subType = typeMatch.group(2).toLowerCase();
    if (typeMatch.groupCount() < 3) {
      return;
    }

    // Get attributes (if any)
    Matcher attrMatch = ATTR_PATTERN.matcher(typeMatch.group(3));
    while (attrMatch.find()) {

      String value = attrMatch.group(2);
      if (value == null) {
        value = attrMatch.group(3);
        if (value == null) {
          value = "";
        }
      }

      attributes.put(attrMatch.group(1).toLowerCase(), value);
    }

    // Infer a default charset encoding if unspecified.
    if (!attributes.containsKey(ATTR_CHARSET)) {
      inferredCharset = true;
      if (subType.endsWith("xml")) {
        if (type.equals("application")) {
          // BUGBUG: Actually have need to look at the raw stream here, but
          // if client omitted the charset for "application/xml", they are
          // ignoring the STRONGLY RECOMMEND language in RFC 3023, sec 3.2.
          // I have little sympathy.
          attributes.put(ATTR_CHARSET, "utf-8");    // best guess
        } else {
          attributes.put(ATTR_CHARSET, "us-ascii"); // RFC3023, sec 3.1
        }
      } else if (subType.equals("json")) {
        attributes.put(ATTR_CHARSET, "utf-8");    // RFC4627, sec 3
      } else {
        attributes.put(ATTR_CHARSET, "iso-8859-1"); // http default
      }
    }
  }

  /** {code True} if parsed input didn't contain charset encoding info */
  private boolean inferredCharset = false;

  private String type;
  public String getType() { return type; }
  public void setType(String type) { this.type = type; }


  private String subType;
  public String getSubType() { return subType; }
  public void setSubType(String subType) { this.subType = subType; }

  /** Returns the full media type */
  public String getMediaType() {
    StringBuffer sb = new StringBuffer();
    sb.append(type);
    sb.append("/");
    sb.append(subType);
    return sb.toString();
  }

  private HashMap<String, String> attributes = new HashMap<String, String>();

  /**
   * Returns the additional attributes of the content type.
   */
  public HashMap<String, String> getAttributes() { return attributes; }


  /**
   * Returns the additional attribute by name of the content type.
   *
   * @param name attribute name
   */
  public String getAttribute(String name) {
    return attributes.get(name);
  }

  /*
   * Returns the charset attribute of the content type or null if the
   * attribute has not been set.
   */
  public String getCharset() { return attributes.get(ATTR_CHARSET); }


  /**
   * Returns whether this content type is match by the content type found in the
   * "Accept" header field of an HTTP request.
   *
   * @param acceptedContentType content type found in the "Accept" header field
   *                            of an HTTP request
   */
  public boolean match(ContentType acceptedContentType) {
    String acceptedType = acceptedContentType.getType();
    String acceptedSubType = acceptedContentType.getSubType();
    return STAR.equals(acceptedType) || type.equals(acceptedType) &&
        (STAR.equals(acceptedSubType) || subType.equals(acceptedSubType));
  }

  public boolean mediaTypeMatch(ContentType acceptedContentType) {
    String acceptedType = acceptedContentType.getType();
    String acceptedSubType = acceptedContentType.getSubType();
    return type.equals(acceptedType) && subType.equals(acceptedSubType);
  }

  /**
   * Generates the Content-Type value
   */
  @Override
  public String toString() {

    StringBuffer sb = new StringBuffer();
    sb.append(type);
    sb.append("/");
    sb.append(subType);
    for (String name : attributes.keySet()) {

      // Don't include any inferred charset attribute in output.
      if (inferredCharset && ATTR_CHARSET.equals(name)) {
        continue;
      }
      sb.append(";");
      sb.append(name);
      sb.append("=");
      String value = attributes.get(name);
      Matcher tokenMatcher = TOKEN_PATTERN.matcher(value);
      if (tokenMatcher.matches()) {
        sb.append(value);
      } else {
        sb.append("\"" + value + "\"");
      }
    }
    return sb.toString();
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ContentType that = (ContentType) o;
    return type.equals(that.type) && subType.equals(that.subType) && attributes
        .equals(that.attributes);
  }


  @Override
  public int hashCode() {
    return (type.hashCode() * 31 + subType.hashCode()) * 31 + attributes
        .hashCode();
  }
}
