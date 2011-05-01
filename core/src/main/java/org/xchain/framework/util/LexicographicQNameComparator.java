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

import java.util.Comparator;
import javax.xml.namespace.QName;

/**
 * This comparator compares QName objects using the string.compareTo function.  QNames are
 * compared first by namespace and then by local name.  Null QNames, namespaces and local names are
 * considered equal by this comparitor. If the nullFirst flag is set to true, then null values are sorted before
 * non-null values.
 *
 * @author Christian Trimble
 */
public class LexicographicQNameComparator
  implements Comparator<QName>
{
  /** If this flag is true, then null values come before non-null values. */
  private boolean nullFirst;

  /**
   * Creates a new comparator that sorts null values first.
   */
  public LexicographicQNameComparator()
  {
    this(true);
  }

  public LexicographicQNameComparator( final boolean nullFirst )
  {
    this.nullFirst = nullFirst;
  }

  /**
   * Returns true if this comparator sorts null values first, false otherwise.
   */
  public boolean getNullFirst()
  {
    return this.nullFirst;
  }

  /**
   * Compares QName objects lexicographically, first by namespace and then by local name.
   */
  public int compare( QName qName1, QName qName2 )
  {
    if( qName1 == null && qName2 == null ) {
      return 0;
    }

    if( qName1 == null || qName2 == null ) {
      nullCompare(qName1, qName2);
    }

    int namespaceCompare = lexicographicCompare(qName1.getNamespaceURI(), qName2.getNamespaceURI());

    if( namespaceCompare != 0 ) {
      return namespaceCompare;
    }

    return lexicographicCompare(qName1.getLocalPart(), qName2.getLocalPart());
  }

  /**
   * Returns true of o is a lexicographicQNameComparator and its nullFirst flag is the
   * same as this object.
   */
  public boolean equals( Object o )
  {
    if( !(o instanceof LexicographicQNameComparator) ) {
      return false;
    }
    return nullFirst = ((LexicographicQNameComparator)o).getNullFirst();
  }

  /**
   * Compares to Strings in lexicograhically.  If both arguments are null, then they are considered equals.  If
   * only one argument is null, then the nullFirst flag is used to determine order.
   */
  private int lexicographicCompare( String s1, String s2 )
  {
    if( s1 == null && s2 == null ) {
      return 0;
    }

    if( s1 == null || s2 == null) {
      return nullCompare(s1, s2);
    }

    return s1.compareTo(s2);
  }

  /**
   * Sorts two values using the nullFirst flag.  One and only one of the arguments to this method must be null.
   */
  private int nullCompare( final Object o1, final Object o2 )
  {
    return (nullFirst && o1 == null) || (!nullFirst && o1 != null) ? 1 : -1;
  }
}
