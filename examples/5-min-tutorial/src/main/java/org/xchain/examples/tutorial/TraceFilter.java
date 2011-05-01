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
package org.xchain.examples.tutorial;

import static org.xchain.examples.tutorial.TraceUtil.*;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.impl.FilterChainImpl;
import org.xchain.framework.sax.CommandHandler;
import org.xchain.framework.sax.CommandXmlReader;
import org.xchain.namespaces.sax.PipelineCommand;
import org.apache.commons.jxpath.JXPathContext;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Christian Trimble
 */
@Element(localName="trace-filter")
public abstract class TraceFilter
  extends FilterChainImpl
{
  private static String XHTML_NAMESPACE_URI = "http://www.w3.org/1999/xhtml";
  private static String DIV_ELEMENT = "div";

  @Attribute(localName="name", type=AttributeType.ATTRIBUTE_VALUE_TEMPLATE)
  public abstract String getName( JXPathContext context );

  public boolean execute( JXPathContext context )
    throws Exception
  {
    // variables for flow control.
    boolean result = false;
    Exception exception = null;

    // get the sax content handler.
    CommandHandler handler =((CommandXmlReader)PipelineCommand.getPipelineConfig().getXmlReader()).getCommandHandler();
    String xhtmlPrefix = xhtmlPrefix(context);
    String name = getName(context);

    startExecuteBlock(handler, xhtmlPrefix, name);
    
    try {
      result = super.execute(context);
    }
    catch( Exception e ) {
      exception = e;
    }
    finally {
      // if we are not passing a sax exception, then send the output.
      if( exception != null && !(exception instanceof SAXException ) ) {
        endExecuteBlock(handler, xhtmlPrefix, name, exception);
      }
    }

    if( exception != null ) {
      throw exception;
    }

    endExecuteBlock(handler, xhtmlPrefix, name, result);

    return result;
  }

  public boolean postProcess( JXPathContext context, Exception exception )
  {
    boolean handled = false;

    try {
      // get the sax content handler.
      CommandHandler handler =((CommandXmlReader)PipelineCommand.getPipelineConfig().getXmlReader()).getCommandHandler();

      String name = getName(context);
      String xhtmlPrefix = xhtmlPrefix(context);

      startPostProcessBlock(handler, xhtmlPrefix, name, exception);
   
      handled = super.postProcess( context, exception );

      endPostProcessBlock(handler, xhtmlPrefix, name, handled);
    }
    catch( Exception e ) {
      // we cannot pass this exception up, it should be logged.
    }

    return handled;
  }
}
