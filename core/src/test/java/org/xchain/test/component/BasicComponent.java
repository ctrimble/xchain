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
package org.xchain.test.component;

import org.xchain.annotations.Component;
import org.xchain.test.component.TestComponent.TestParamClass;

/**
 * Basic component.  No begin or end methods.  No dependency injection.
 * @author Devon Tackett
 */
@Component(localName="basic-component") 
public class BasicComponent {
  public static String BASIC_RESULT = "basic-result";
  public static String PARAM_RESULT = "param-result";
  public static String OBJECT_PARAM_TEST_RESULT = "object-param-result";
  
  public String getResult() {
    return BASIC_RESULT;
  }
  
  public String getResult(String incoming) {
    return incoming + PARAM_RESULT;
  }
  
  public TestParamClass javaObjectTest(TestParamClass test) {
    test.setValue(OBJECT_PARAM_TEST_RESULT);
    return test;
  }
}
