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
package org.xchain.framework.net.protocol.resource;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * This url connection is represents a resource that could not be found.  This connection
 * will throw a ResourceNotFoundException if the connect method is called.
 *
 * @author Christian Trimble
 */
public class ResourceNotFoundUrlConnection
  extends URLConnection
{
  public ResourceNotFoundUrlConnection( URL url )
  {
    super( url );
  }

  public void connect()
    throws IOException
  {
    throw new ResourceNotFoundException("The url '"+url+"' does not exist.");
  }
}
