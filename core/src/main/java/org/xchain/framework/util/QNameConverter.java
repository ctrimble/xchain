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

import javax.xml.namespace.QName;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.jxpath.JXPathContext;
import org.xchain.framework.lifecycle.Execution;

/**
 * @author Christian Trimble
 */
public class QNameConverter
  implements Converter
{
  public Object convert( Class type, Object value )
  {
    if( value == null ) {
      return null;
    }

    if( QName.class.isAssignableFrom(value.getClass()) ) {
      return value;
    }

    // if we are currently in an execution, then look up any prefixes based on the currently executing context.
    if( Execution.inExecution() && value instanceof String ) {
      JXPathContext context = Execution.getLocalContext();
      try {
        return JXPathContextUtil.stringToQName(context, (String)value);
      }
      catch( Exception e ) {
        throw new ConversionException(e.getMessage(), e);
      }
    }
    else if( value instanceof String ) {
      try {
        return QName.valueOf((String)value);
      }
      catch( Exception e ) {
        throw new ConversionException("The string '"+value+"' could not be converted into a QName.");
      }
    }

    throw new ConversionException("The object of type '"+value.getClass()+"' could not be converted to '"+QName.class+"'.");
  }
}
