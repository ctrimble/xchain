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
package org.xchain.framework.security;

/**
 * This class just exists so we can have more fine-grained control over our exception handling.
 *
 * @author Jason Rose
 */
public class AuthenticationException extends SecurityException {
  
  private static final long serialVersionUID = -7386437653612323322L;

  public AuthenticationException() {
    super();
  }

  public AuthenticationException(String cause) {
    super(cause);
  }

  public AuthenticationException(Throwable cause) {
    super(cause);
  }

  public AuthenticationException(String str, Throwable cause) {
    super(str, cause);
  }
}
