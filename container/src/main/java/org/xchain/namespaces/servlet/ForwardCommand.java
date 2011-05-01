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
package org.xchain.namespaces.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.RequestDispatcher;

import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.Command;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.annotations.PrefixMapping;

/**
 * @author Mike Moulton
 * @author Jason Rose
 * @author Josh Kennedy
 */
@Element(localName="forward")
public abstract class ForwardCommand
  implements Command
{

  public static Logger log = LoggerFactory.getLogger(ForwardCommand.class);
  
  @Attribute(
      localName="path",
      type=AttributeType.JXPATH_VALUE
     )
  public abstract String getPath(JXPathContext context);

  @Attribute(
      localName="servlet-context",
      type=AttributeType.JXPATH_VALUE,
      defaultValue="$" + Constants.DEFAULT_PREFIX + ":" + Constants.CONTEXT,
      defaultPrefixMappings={@PrefixMapping(uri=Constants.URI, prefix=Constants.DEFAULT_PREFIX)}
     )
  public abstract ServletContext getServletContext(JXPathContext context);

  @Attribute(
      localName="response",
      type=AttributeType.JXPATH_VALUE,
      defaultValue="$" + Constants.DEFAULT_PREFIX + ":" + Constants.RESPONSE,
      defaultPrefixMappings={@PrefixMapping(uri=Constants.URI, prefix=Constants.DEFAULT_PREFIX)}
     )
  public abstract ServletResponse getResponse(JXPathContext context);

  @Attribute(
      localName="request",
      type=AttributeType.JXPATH_VALUE,
      defaultValue="$" + Constants.DEFAULT_PREFIX + ":" + Constants.REQUEST,
      defaultPrefixMappings={@PrefixMapping(uri=Constants.URI, prefix=Constants.DEFAULT_PREFIX)}
     )
  public abstract ServletRequest getRequest(JXPathContext context);

  public boolean execute( JXPathContext context )
    throws Exception
  {
    ServletContext servletContext = getServletContext(context);
    RequestDispatcher dispatcher = servletContext.getRequestDispatcher(getPath(context));
    dispatcher.forward(getRequest(context), getResponse(context));

    return false;
  }
}
