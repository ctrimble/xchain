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
package org.xchain.framework.osgi;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Christian Trimble
 * @author John Trimble
 */
public class ParsedClassPathEntry
{
  private List<String> targetList = new ArrayList<String>();
  private List<ParsedParameter> parameterList = new ArrayList<ParsedParameter>();

  public List<String> getTargetList() { return this.targetList; }
  public List<ParsedParameter> getParameterList() { return this.parameterList; }
}
