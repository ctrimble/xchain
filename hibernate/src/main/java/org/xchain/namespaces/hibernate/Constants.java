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
package org.xchain.namespaces.hibernate;

/**
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Jason Rose
 */
public class Constants
{
  public static final String URI = "http://www.xchain.org/hibernate/1.0";
  public static final String DEFAULT_PREFIX = "hibernate";
  @Deprecated
  public static final String SESSION = "session";
  public static final String CURRENT_SESSION_FUNCTION = "current-session()";
  public static final String QUERY = "query";
}
