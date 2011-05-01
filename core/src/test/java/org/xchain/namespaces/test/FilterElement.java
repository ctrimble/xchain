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

import javax.xml.namespace.QName;
import org.apache.commons.jxpath.JXPathContext;
import org.xchain.Filter;
import org.xchain.annotations.Element;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.impl.FilterChainImpl;
import org.xchain.framework.sax.CommandXmlReader;
import org.xchain.namespaces.sax.PipelineCommand;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Christian Trimble
 */
@Element(localName="filter")
public abstract class FilterElement
  extends FilterChainImpl
{
  @Attribute(localName="name", type=AttributeType.QNAME)
  public abstract QName getName( JXPathContext context );

  @Attribute(localName="execute-chars", type=AttributeType.ATTRIBUTE_VALUE_TEMPLATE)
  public abstract String getExecuteChars( JXPathContext context );

  @Attribute(localName="post-process-chars", type=AttributeType.ATTRIBUTE_VALUE_TEMPLATE)
  public abstract String getPostProcessChars( JXPathContext context );

  public boolean execute( JXPathContext context )
    throws Exception
  {
    boolean result = false;

    QName name = getName(context);
    String executeChars = getExecuteChars(context);

    ContentHandler handler = getContentHandler();

    if( name != null ) {
      handler.startElement(name.getNamespaceURI(), name.getLocalPart(), name.getPrefix(), new AttributesImpl());
    }
    if( executeChars != null ) {
      char[] characters = executeChars.toCharArray();
      handler.characters(characters, 0, characters.length);
    }

    result = super.execute(context);

    return result;
  }

  public boolean postProcess( JXPathContext context, Exception exception )
  {
    boolean handled = false;
    try {
      handled = super.postProcess( context, exception );

      QName name = getName(context);
      String postProcessChars = getPostProcessChars(context);

      ContentHandler handler = getContentHandler();

      if( postProcessChars != null ) {
        char[] characters = postProcessChars.toCharArray();
        handler.characters(characters, 0, characters.length);
      }
      if( name != null ) {
        handler.endElement(name.getNamespaceURI(), name.getLocalPart(), name.getPrefix());
      }
    }
    catch( Exception e ) {
      // this exception should propigate.
    }

    return handled;
  }

  private static ContentHandler getContentHandler()
  {
    return ((CommandXmlReader)PipelineCommand.getPipelineConfig().getXmlReader()).getCommandHandler();
  }
}
