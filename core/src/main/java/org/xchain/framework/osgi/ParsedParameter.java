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

/**
 * @author Christian Trimble
 * @author John Trimble
 */
public class ParsedParameter
{
  private String name;
  private String value;
  private ParameterType type;

  public void setName( String name ) { this.name = name; } 
  public String getName() { return this.name; } 
  public void setValue( String value ) { this.value = value; }
  public String getValue() { return this.value; }
  public void setType( ParameterType type ) { this.type = type; }
  public ParameterType getType() { return this.type; }
}
