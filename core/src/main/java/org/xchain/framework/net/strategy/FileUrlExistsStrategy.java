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
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xchain.framework.net.UrlExistsStrategy;

/**
 * A UrlExistsStrategy implementation to check if a URL references an existing file.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
public class FileUrlExistsStrategy
  implements UrlExistsStrategy
{
  private static final Logger log = LoggerFactory.getLogger( FileUrlExistsStrategy.class );
  private static final String PROTOCOL = "file";
  
  public boolean exists( URL url )
    throws Exception
  {
    if( log.isDebugEnabled() ) {
      log.debug("Testing if file '"+url.toExternalForm()+"' exists.");
    }

    // create a file object for the url.
    File file = new File(url.getPath());

    // return if the file exists.
    return file.exists();
  }
  
  public String getProtocol() {
    return PROTOCOL;
  }
}
