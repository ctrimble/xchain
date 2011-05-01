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
package org.xchain.example.namespaces.xhtml;

import org.apache.commons.jxpath.JXPathContext;
import org.xchain.impl.ChainImpl;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.framework.jxpath.Scope;
import org.xchain.framework.jxpath.ScopedQNameVariables;
import org.xchain.framework.sax.CommandXmlReader;
import org.xchain.framework.sax.CommandHandler;
import org.xchain.namespaces.sax.PipelineCommand;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Christian Trimble
 */
@Element(localName="option")
public abstract class OptionCommand
  extends ChainImpl
{
  public static final String OPTION_LOCAL_NAME = "option";
  public static final String SELECTED_LOCAL_NAME = "selected";
  public static final String SELECTED_VALUE = "selected";
  public static final String VALUE_LOCAL_NAME = "value";
  public static final String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";


  @Attribute(localName = "value", type = AttributeType.ATTRIBUTE_VALUE_TEMPLATE)
  public abstract String getValue(JXPathContext context);

  public boolean execute(JXPathContext context)
    throws Exception
  {
    String value = getValue(context);
    String currentValue = (String)((ScopedQNameVariables) context.getVariables()).getVariable(SelectCommand.SELECTED_VARIABLE_NAME, Scope.execution);

    // get the xhtml prefix and make sure that there is a mapping for it.
    String xhtmlPrefix = context.getPrefix(XHTML_NAMESPACE);
    if( xhtmlPrefix == null ) {
      throw new IllegalStateException("There is no mapping defined for "+XHTML_NAMESPACE);
    }

    AttributesImpl attributes = new AttributesImpl();
    if( value != null ) {
      attributes.addAttribute("", VALUE_LOCAL_NAME, VALUE_LOCAL_NAME, "CDATA", value);
    }
    if( value != null && value.equals(currentValue) ) {
      attributes.addAttribute("", SELECTED_LOCAL_NAME, SELECTED_LOCAL_NAME, "CDATA", SELECTED_VALUE);
    }

    ContentHandler handler = getContentHandler();
    handler.startElement(XHTML_NAMESPACE, OPTION_LOCAL_NAME, qNameString(xhtmlPrefix, OPTION_LOCAL_NAME), attributes);

    boolean result = false;
    Exception exception = null;

    try {
      result = super.execute(context);
    } catch (Exception e) {
      exception = e;
    }
    if( exception == null || !(exception instanceof SAXException) ) {
      handler.endElement(XHTML_NAMESPACE, OPTION_LOCAL_NAME, qNameString(xhtmlPrefix, OPTION_LOCAL_NAME));
    }

    if( exception != null ) {
      throw exception;
    }
    return result;
  }

  protected CommandHandler getContentHandler()
  {
    return ((CommandXmlReader) PipelineCommand.getPipelineConfig().getXmlReader()).getCommandHandler();
  }

  protected String qNameString( String prefix, String localName )
  {
    return (prefix == null || prefix.equals("")) ? localName : prefix+":"+localName;
  }
}
