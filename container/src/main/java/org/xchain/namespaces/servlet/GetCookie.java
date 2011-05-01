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
import org.xchain.framework.jxpath.Scope;
import org.xchain.framework.jxpath.ScopedQNameVariables;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Base class to provide simple string value existence and email string validation.
 *
 * @author Nicholas Bolton
 */

@Element(localName = "get-cookie")
public abstract class GetCookie implements Command {

  public GetCookie() {
  }

  @Attribute(localName = "name", type = AttributeType.JXPATH_VALUE)
  public abstract String getName(JXPathContext context);

  @Attribute(localName = "request", type = AttributeType.JXPATH_VALUE)
  public abstract HttpServletRequest getRequest(JXPathContext context);

  @Attribute(localName = "variable", type = AttributeType.JXPATH_VALUE)
  public abstract String getVariable(JXPathContext context);

  public boolean execute(JXPathContext context) throws Exception {
    String name = getName(context);
    Cookie[] cookies = getRequest(context).getCookies();
    for (int i = 0; cookies != null && i < cookies.length; i++) {
      Cookie cookie = cookies[i];
      if (name.equals(cookie.getName())) {
        ((ScopedQNameVariables) context.getVariables()).declareVariable(
            getVariable(context), cookie.getValue(), Scope.execution);
      }
    }
    return false;
  }
}
