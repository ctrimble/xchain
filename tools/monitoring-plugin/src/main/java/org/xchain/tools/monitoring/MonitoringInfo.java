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
package org.xchain.tools.monitoring;

import java.util.ArrayList;
import java.util.List;
import org.apache.maven.model.Resource;

/**
 * A simple data object that stores information about the resource paths and webapp paths to be added to a
 * monitoring info file.
 *
 * @author Christian Trimble
 */
public class MonitoringInfo
{
  private List<Resource> resourceList = new ArrayList<Resource>();
  private List<Resource> webResourceList = new ArrayList<Resource>();

  public MonitoringInfo()
  {

  }

  public List<Resource> getResourceList()
  {
    return resourceList;
  }

  public List<Resource> getWarResourceList()
  {
    return webResourceList;
  }
}
