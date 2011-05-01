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
import java.net.JarURLConnection;

import org.xchain.framework.net.UrlExistsStrategy;

/**
 * A UrlExistsStrategy implementation to check if a URL references a file inside a jar.
 *
 * @author Christian Trimble
 * @author Jason Rose
 * @author Devon Tackett
 */
public class JarUrlExistsStrategy
  implements UrlExistsStrategy
{
  private static final String PROTOCOL = "jar";
  
  public boolean exists( URL url )
    throws Exception
  {
    // get a connection to the url.
    JarURLConnection connection = (JarURLConnection)url.openConnection();

    // return true if there is an entry for this jar file.
    return connection.getJarEntry() != null;
  }

  public String getProtocol() {
    return PROTOCOL;
  }
}
