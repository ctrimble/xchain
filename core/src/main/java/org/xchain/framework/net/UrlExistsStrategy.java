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
package org.xchain.framework.net;

import java.net.URL;

/**
 * Interface for strategies to check if a URL references something that exists.  Each UrlExistsStrategy is tied to a protocol.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 */
public interface UrlExistsStrategy
{
  /**
   * Check if anything exists at the given URL.
   * 
   * @param url The URL to check on.
   * 
   * @return True if something exists at the given URL.
   */
  public boolean exists( URL url )
    throws Exception;
  
  /**
   * @return The protocol this UrlExistsStrategy is for.
   */
  public String getProtocol();
}
