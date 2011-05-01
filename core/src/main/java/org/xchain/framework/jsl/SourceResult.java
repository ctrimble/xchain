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
package org.xchain.framework.jsl;

/**
 * @author Christian Trimble
 */
public class SourceResult
{
  private String source = null;
  private String className = null;
  private String classResourceName = null;
  private String sourceResourceName = null;

  public void setSource( String source ) { this.source = source; }
  public String getSource() { return this.source; }

  public void setClassName( String className ) { this.className = className; }
  public String getClassName() { return this.className; }

  public void setSourceResourceName( String sourceResourceName ) { this.sourceResourceName = sourceResourceName; }
  public String getSourceResourceName() { return this.sourceResourceName; }

  public void setClassResourceName( String classResourceName ) { this.classResourceName = classResourceName; }
  public String getClassResourceName() { return this.classResourceName; }
}
