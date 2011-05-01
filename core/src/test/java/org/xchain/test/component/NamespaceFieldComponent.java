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
import org.xchain.annotations.PrefixMapping;

/**
 * Component with a namespaced field dependency injection.
 * @author Devon Tackett
 */
@Component(localName="namespace-field-component")
public class NamespaceFieldComponent {
  public static final String TEST_NAMESPACE_URI = "http://www.xchain.org/test/namespace";
  public static final String FINAL_VALUE = "final-result";
  
  @In(select="$namespace:field", prefixMappings={@PrefixMapping(uri=TEST_NAMESPACE_URI, prefix="namespace")})
  public String result = "initial";
  
  public String getResult() {
    return result;
  }
}
