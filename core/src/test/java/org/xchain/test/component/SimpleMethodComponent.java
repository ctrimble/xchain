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
import org.xchain.annotations.In;

/**
 * Component with a simple method dependency injection.
 * @author Devon Tackett
 */
@Component(localName="simple-method-component")
public class SimpleMethodComponent {
  public static final String FINAL_VALUE = "field-result";
  
  private String result = "initial";
  
  @In(select="$method")
  public void setResult(String result) {
    this.result = result;
  }
  
  public String getResult() {
    return result;
  }
}
