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

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import org.apache.commons.jxpath.JXPathContext;
import org.xchain.Filter;
import org.xchain.impl.ChainImpl;
import org.xchain.annotations.Element;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.PrefixMapping;
import org.xchain.framework.jxpath.Scope;
import org.xchain.framework.jxpath.ScopedQNameVariables;
import org.xchain.framework.sax.CommandXmlReader;
import org.xchain.framework.sax.CommandHandler;
import org.xchain.namespaces.sax.PipelineCommand;
import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Christian Trimble
 */
@Element(localName="select")
public abstract class SelectCommand
  extends ChainImpl
  implements Filter
{
  public static final QName SELECTED_VARIABLE_NAME = QName.valueOf("{http://www.xchain.org/guide/xhtml}selected");
  public static final String SELECT_LOCAL_NAME = "select";
  public static final String ID_LOCAL_NAME = "id";
  public static final String NAME_LOCAL_NAME = "name";
  public static final String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";

  @Attribute(
    localName = "id",
    type = AttributeType.ATTRIBUTE_VALUE_TEMPLATE
  ) 
  public abstract String getId(JXPathContext context);
  public abstract boolean hasId();

  @Attribute(
    localName = "name",
    type = AttributeType.ATTRIBUTE_VALUE_TEMPLATE
  )
  public abstract String getName(JXPathContext context);

  @Attribute(
    localName = "request",
    type = AttributeType.JXPATH_VALUE,
    defaultValue = "$servlet:request",
    defaultPrefixMappings = {@PrefixMapping(uri="http://www.xchain.org/servlet/1.0", prefix="servlet")}
  )
  public abstract HttpServletRequest getRequest( JXPathContext context );

  public boolean execute(JXPathContext context)
    throws Exception
  {
    // get the request and the name from the context.
    HttpServletRequest request = getRequest(context);
    String name = getName(context);

    // get the original value of the name.
    String currentValue = request.getParameter(name);

    // get the xhtml prefix and make sure that there is a mapping for it.
    String xhtmlPrefix = context.getPrefix(XHTML_NAMESPACE);
    if( xhtmlPrefix == null ) {
      throw new IllegalStateException("There is no mapping defined for "+XHTML_NAMESPACE);
    }

    // create the attributes for the element we are going to output.
    AttributesImpl attributes = new AttributesImpl();
    if( hasId() ) {
      attributes.addAttribute("", ID_LOCAL_NAME, ID_LOCAL_NAME, "CDATA", getId(context));
    }
    attributes.addAttribute("", NAME_LOCAL_NAME, NAME_LOCAL_NAME, "CDATA", getName(context));

    ((ScopedQNameVariables)context.getVariables()).declareVariable(SELECTED_VARIABLE_NAME, currentValue, Scope.execution);
    ContentHandler handler = getContentHandler();
    handler.startElement(XHTML_NAMESPACE, SELECT_LOCAL_NAME, qNameString(xhtmlPrefix, SELECT_LOCAL_NAME), attributes);

    boolean result = false;
    Exception exception = null;

    // execute the children and catch any exceptions.
    try {
      result = super.execute(context);
    }
    catch (Exception e) {
      exception = e;
    }

    // if there was not an exception, or the exception is not a sax exception, then we need to finish the output.
    if( exception == null || !(exception instanceof SAXException) ) {
      handler.endElement(XHTML_NAMESPACE, SELECT_LOCAL_NAME, qNameString(xhtmlPrefix, SELECT_LOCAL_NAME));
    }

    // rethrow the exception.
    if( exception != null ) {
      throw exception;
    }
    return result;
  }

  public boolean postProcess( JXPathContext context, Exception e )
  {
    ((ScopedQNameVariables)context).undeclareVariable(SELECTED_VARIABLE_NAME);
    return false;
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
