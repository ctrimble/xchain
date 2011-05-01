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
package org.xchain.namespaces.core;

import org.xchain.annotations.Component;
import org.xchain.annotations.Function;
import org.xchain.test.component.TestComponent.TestParamClass;

/**
 * A basic component that has a method that will always fail with an exception.
 * @author Christian Trimble
 */
@Component(localName="exception-component")
public class ExceptionComponent {

  public Object throwException()
    throws Exception
  {
    throw new Exception("This exception came from the exception component.");
  }

  @Function(localName="static-throw-exception")
  public static Object staticThrowException()
    throws Exception
  {
    throw new Exception("This exception came from a static function.");
  }
}

