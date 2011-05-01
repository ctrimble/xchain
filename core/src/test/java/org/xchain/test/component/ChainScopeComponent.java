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
import org.xchain.annotations.In;
import org.xchain.framework.jxpath.Scope;
import org.xchain.test.component.TestComponent.TestParamClass;

/**
 * A component which will only exist at the chain level.
 * @author Devon Tackett
 */
@Component(localName="chain-scope-component", scope=Scope.chain)
public class ChainScopeComponent {
  private String result = "initial";
  private TestParamClass testParam = null;

  public String getResult() {
    return result;
  }

  @In(select="$value")
  public void setResult(String result) {
    this.result = result;
  }
  
  public void setTestParam(TestParamClass testParam) {
    this.testParam = testParam;
  }
  
  @End
  public void end() {
    if (testParam != null) {
      testParam.setValue("clean");
    }
  }
}
