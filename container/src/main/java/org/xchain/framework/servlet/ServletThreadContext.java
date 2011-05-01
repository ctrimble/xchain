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
package org.xchain.framework.servlet;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.xchain.framework.lifecycle.ThreadContext;

/**
 * @author Christian Trimble
 */
public class ServletThreadContext
  extends ThreadContext
{
  protected ServletRequest request;
  protected ServletResponse response;

  public ServletThreadContext( ServletRequest request, ServletResponse response )
  {
    this.request = request;
    this.response = response;
  }

  public ServletRequest getRequest()
  {
    return request;
  }

  public ServletResponse getResponse()
  {
    return response;
  }
}
