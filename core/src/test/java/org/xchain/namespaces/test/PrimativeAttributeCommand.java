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
package org.xchain.namespaces.test;

import org.xchain.Command;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.apache.commons.jxpath.JXPathContext;

/**
 * @author Christian Trimble
 */
@Element(localName="primative")
public abstract class PrimativeAttributeCommand
  implements Command
{
  @Attribute(localName="boolean", type=AttributeType.JXPATH_VALUE)
  public abstract boolean getBoolean( JXPathContext context );

  @Attribute(localName="int", type=AttributeType.JXPATH_VALUE)
  public abstract int getInt( JXPathContext context );

  @Attribute(localName="long", type=AttributeType.JXPATH_VALUE)
  public abstract long getLong( JXPathContext context );

  @Attribute(localName="double", type=AttributeType.JXPATH_VALUE)
  public abstract double getDouble( JXPathContext context );

  public boolean execute( JXPathContext context )
    throws Exception
  {
    try {
    System.out.println("\n-------------- Execute Called ---------------\n");
    boolean booleanValue = getBoolean(context);
    System.out.println("Boolean Value: "+booleanValue);
    int intValue         =  getInt(context);
    System.out.println("Int Value: "+intValue);
    long longValue       = getLong(context);
    System.out.println("Long Value: "+longValue);
    double doubleValue   = getDouble(context);
    System.out.println("Double Value: "+doubleValue);
    }
    catch( Throwable t ) {
      t.printStackTrace();
    }

    return false;
  }
}
