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
package org.xchain.namespaces.sax;

import org.apache.commons.jxpath.JXPathContext;
import org.xchain.Command;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>&lt;sax:output-property/&gt; elements are placed inside &lt;sax:transfromer/&gt; elements to set output properties on a transformer.</p>
 *
 * <code class="source">
 * &lt;sax:pipeline xmlns:sax="http://www.xchain.org/sax/1.0"&gt;
 *   &lt;sax:source .../&gt;
 *   ...
 *   &lt;sax:transformer system-id="'relative-uri-of-template'"&gt;
 *     &lt;sax:output-property name="'name'" value="'value'"/&gt;
 *   &lt;/sax:transformer&gt;
 *   ...
 *   &lt;sax:result .../&gt;
 * &lt;/sax:pipeline&gt;
 * </code>
 *
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
@Element(localName="output-property")
public abstract class OutputPropertyCommand
  implements Command
{
  /** The log for this class. */
  private static Logger log = LoggerFactory.getLogger(OutputPropertyCommand.class);
  
  /**
   * <p>The name of the output property to set.</p>
   * @param context the JXPathContext to evaluate this attribute against.
   */
  @Attribute(localName="name", type=AttributeType.JXPATH_VALUE)
  public abstract String getName( JXPathContext context )
    throws Exception;

  /**
   * <p>The value of the output property to set.</p>
   * @param context the JXPathContext to evaluate this attribute against.
   */
  @Attribute(localName="value", type=AttributeType.JXPATH_VALUE)
  public abstract String getValue( JXPathContext context )
    throws Exception;  

  public OutputPropertyCommand()
  {
    if( log.isDebugEnabled() ) {
      log.debug("Parameter command created.");
    }
  }

  /**
   * <p>Gets the current transformer and sets an output property on it.</p>
   * @param context the JXPathContext to evaluate this attribute against.
   */
  public boolean execute( JXPathContext context )
    throws Exception
  {
    String name = getName( context );
    String value = getValue( context );

    if( log.isDebugEnabled() ) {
      log.debug("Name: "+name+" Value:"+value);
    }

    TransformerCommand.getCurrentTransformer().setOutputProperty(name, value);

    return false;
  }
}
