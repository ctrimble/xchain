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
 * Component with a simple field dependency injection.
 * @author Devon Tackett
 */
@Component(localName="field-component")
public class FieldComponent {
  public static final String FINAL_PRIVATE_VALUE = "private-field-result";
  public static final String FINAL_PROTECTED_VALUE = "protected-field-result";
  public static final String FINAL_PUBLIC_VALUE = "public-field-result";
  
  @In(select="$private-field")
  private String privateResult = "initial";
  
  @In(select="$protected-field")
  private String protectedResult = "initial";  
  
  @In(select="$public-field")
  private String publicResult = "initial";  
  
  public String getPrivateResult() {
    return privateResult;
  }
  
  public String getProtectedResult() {
    return protectedResult;
  }
  
  public String getPublicResult() {
    return publicResult;
  }
}
