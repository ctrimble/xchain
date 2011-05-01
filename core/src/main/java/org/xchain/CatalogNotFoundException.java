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
package org.xchain;

/**
 * This exception is thrown if a requested catalog could not be found.
 *
 * @author Christian Trimble
 */
public class CatalogNotFoundException
  extends Exception
{
  /**
   * Constructs a new CatalogNotFoundException.
   */
  public CatalogNotFoundException() {
    super();
  }

  /**
   * Constructs a new CatalogNotFoundException with the specified message.
   *
   * @param message the message describing this exception.
   */
  public CatalogNotFoundException(String message) {
    super(message);
  }

  /**
   * Constructs a new CatalogNotFoundException with the specified message and cause.
   *
   * @param message the message describing this exception.
   * @param cause the throwable that caused the catalog to not be found.
   */
  public CatalogNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new CatalogNotFoundException with the specified cause.
   *
   * @param cause the throwable that caused the catalog to not be found.
   */
  public CatalogNotFoundException(Throwable cause) {
    super(cause);
  }

}
