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
package org.xchain.framework.net.strategy;

import java.net.URL;
import java.net.URLConnection;
import org.xchain.framework.net.protocol.resource.ResourceUrlConnection;
import org.xchain.framework.net.protocol.resource.ResourceUrlStreamHandlerFactory;
import org.xchain.framework.net.UrlUtil;
import org.xchain.framework.net.UrlExistsStrategy;

/**
 * A UrlExistsStrategy implementation to check if a url references an existing resource.  Refer to {@link ResourceUrlConnection} for more
 * information on ResourceUrls.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class ResourceUrlExistsStrategy
  implements UrlExistsStrategy
{ 
  public boolean exists( URL url )
    throws Exception
  {
    // get the connection.
    URLConnection connection = url.openConnection();

    // test if the connection exists.
    return exists( (ResourceUrlConnection)connection );
  }

  public boolean exists( ResourceUrlConnection connection )
    throws Exception
  {
    // test if the wrapped connection exists.
    return UrlUtil.getInstance().exists(connection.getWrapped().getURL());
  }

  public String getProtocol() {
    return ResourceUrlStreamHandlerFactory.RESOURCE_PROTOCOL;
  }
}
