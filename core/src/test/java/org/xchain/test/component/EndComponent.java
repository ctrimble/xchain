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
import org.xchain.annotations.End;
import org.xchain.test.component.TestComponent.TestParamClass;

/**
 * Component with an end method.
 * @author Devon Tackett
 */
@Component(localName="end-component")
public class EndComponent {
  public static final String END_RESULT = "end-result";

  private TestParamClass testParam = null;
  
  public TestParamClass testParam(TestParamClass test) {
    testParam = test;
    return test;
  }
  
  @End
  public void end() {
    testParam.setValue(END_RESULT);
  }
}
