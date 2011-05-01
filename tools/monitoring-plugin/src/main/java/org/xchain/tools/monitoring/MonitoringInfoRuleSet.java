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

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RuleSetBase;

import org.apache.maven.model.Resource;

/**
 * @author Christian Trimble
 */
public class MonitoringInfoRuleSet
  extends RuleSetBase
{
  public MonitoringInfoRuleSet()
  {
    namespaceURI = "";
  }

  public void addRuleInstances(Digester digester)
  {
    System.out.println("Adding rules.");
    //digester.setNamespaceAware(true);
    //digester.setRuleNamespaceURI(namespaceURI);

    digester.addRule("monitoring-info", new EmptyRule());
    digester.addRule("monitoring-info/resource", new ResourceRule());
    digester.addRule("monitoring-info/resource/directory", new DirectoryRule());
    digester.addRule("monitoring-info/resource/filtering", new FilterRule());
    digester.addRule("monitoring-info/resource/includes", new EmptyRule());
    digester.addRule("monitoring-info/resource/includes/include", new IncludeRule());
    digester.addRule("monitoring-info/resource/excludes", new EmptyRule());
    digester.addRule("monitoring-info/resource/excludes/exclude", new ExcludeRule());
    digester.addRule("monitoring-info/web-resource", new WarResourceRule());
    digester.addRule("monitoring-info/web-resource/directory", new DirectoryRule());
    digester.addRule("monitoring-info/web-resource/filtering", new FilterRule());
    digester.addRule("monitoring-info/web-resource/includes", new EmptyRule());
    digester.addRule("monitoring-info/web-resource/includes/include", new IncludeRule());
    digester.addRule("monitoring-info/web-resource/excludes", new EmptyRule());
    digester.addRule("monitoring-info/web-resource/excludes/exclude", new ExcludeRule());
  }

  public static class EmptyRule
    extends Rule
  {
  }

  public static class ResourceRule
    extends Rule
  {
    public void begin(java.lang.String namespace, java.lang.String name, org.xml.sax.Attributes attributes)
      throws java.lang.Exception
    {
      System.out.println("Found a resource.");
      getDigester().push(new Resource());
    }

    public void end(String namespace, String name)
      throws Exception
    {
      Resource resourceInfo = (Resource)getDigester().pop();
      ((MonitoringInfo)getDigester().peek()).getResourceList().add(resourceInfo);
    }
  }

  public static class WarResourceRule
    extends Rule
  {
    public void begin(java.lang.String namespace, java.lang.String name, org.xml.sax.Attributes attributes)
      throws java.lang.Exception
    {
      getDigester().push(new Resource());
    }

    public void end(String namespace, String name)
      throws Exception
    {
      Resource resourceInfo = (Resource)getDigester().pop();
      ((MonitoringInfo)getDigester().peek()).getWarResourceList().add(resourceInfo);
    }
  }

  public static class DirectoryRule
    extends Rule
  {
    public void body( String namespace, String name, String text )
      throws Exception
    {
      ((Resource)getDigester().peek()).setDirectory(text);
    }
  }

  public static class FilterRule
    extends Rule
  {
    public void body( String namespace, String name, String text )
      throws Exception
    {
      ((Resource)getDigester().peek()).setFiltering(Boolean.valueOf(text));
    }
  }

  public static class IncludeRule
    extends Rule
  {
    public void body( String namespace, String name, String text )
      throws Exception
    {
      ((Resource)getDigester().peek()).getIncludes().add(text);
    }
  }

  public static class ExcludeRule
    extends Rule
  {
    public void boid( String namespace, String name, String text )
      throws Exception
    {
      ((Resource)getDigester().peek()).getExcludes().add(text);
    }
  }
}
