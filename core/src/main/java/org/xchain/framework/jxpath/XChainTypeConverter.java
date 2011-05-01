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
package org.xchain.framework.jxpath;

import org.apache.commons.jxpath.util.BasicTypeConverter;

/**
 * <p>A type converter for the xchains package.  This type converter adds support for Java 5 Enumerations.  Any other types
 * are converted by JXPaths BasicTypeConverter.</p>
 *
 * @author Christian Trimble
 */
public class XChainTypeConverter
  extends BasicTypeConverter
{
  /**
   * <p>This method corrects the canConvert behavior for Java 5 Enum type.</p>
   *
   * @param expression the expression to evaluate.
   * @param toType the type of object to return.
   * @return true if type type is an enumeration type, otherwise returns super.canConvert(Object, Class)
   */
  public boolean canConvert( Object object, Class toType )
  {
    if( Enum.class.isAssignableFrom(toType) ) {
      return true;
    }
    else {
      return super.canConvert( object, toType );
    }
  }

  /**
   * <p>This method corrects the convert behavior for Java 5 Enum type.</p>
   *
   * @param expression the expression to evaluate.
   * @param toType the type of object to return.
   * @return object as an Enum, if toType is an Enum type, otherwise returns super.convert(Object, Class)
   */
  public Object convert(Object object, final Class toType) 
  {
    if( object != null && Enum.class.isAssignableFrom(toType) ) {
      if( toType.isAssignableFrom(object.getClass()) ) {
        return object;
      }
      else {
        return Enum.valueOf(toType, object.toString());
      }
    }
    else {
      return super.convert(object, toType);
    }
  }
}
