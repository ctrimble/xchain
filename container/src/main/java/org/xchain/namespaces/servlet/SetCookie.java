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

import org.apache.commons.jxpath.JXPathContext;
import org.xchain.Command;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Base class to provide simple string value existence and email string validation.
 *
 * @author Nicholas Bolton
 */

@Element(localName = "set-cookie")
public abstract class SetCookie implements Command {

  public SetCookie() {
  }

  @Attribute(localName = "name", type = AttributeType.JXPATH_VALUE)
  public abstract String getName(JXPathContext context);

  @Attribute(localName = "value", type = AttributeType.JXPATH_VALUE)
  public abstract String getValue(JXPathContext context);

  @Attribute(localName = "path", type = AttributeType.JXPATH_VALUE)
  public abstract String getPath(JXPathContext context);
  public abstract boolean hasPath();

  @Attribute(localName = "domain", type = AttributeType.JXPATH_VALUE)
  public abstract String getDomain(JXPathContext context);
  public abstract boolean hasDomain();

  @Attribute(localName = "maxAge", type = AttributeType.JXPATH_VALUE)
  public abstract int getMaxAge(JXPathContext context);
  public abstract boolean hasMaxAge();

  @Attribute(localName = "response", type = AttributeType.JXPATH_VALUE)
  public abstract HttpServletResponse getResponse(JXPathContext context);

  public boolean execute(JXPathContext context) throws Exception {
    Cookie cookie = new Cookie(getName(context), getValue(context));
    if (hasPath()) {
      cookie.setPath(getPath(context));
    }
    if (hasDomain()) {
      cookie.setDomain(getDomain(context));
    }
    if (hasMaxAge()) {
      cookie.setMaxAge(getMaxAge(context));
    }
    getResponse(context).addCookie(cookie);
    return false;
  }
}
