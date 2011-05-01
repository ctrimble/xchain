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
package org.xchain.framework.scanner;

import java.net.URL;
import java.util.Set;

/**
 * <p>Implementations of this interface are used to locate all of the root urls on the classpath that should be included in a scan.</p>
 *
 * @author Christian Trimble
 */
public interface RootUrlLocator
{
  public Set<URL> findRoots( ClassLoader cl )
    throws Exception;
}
