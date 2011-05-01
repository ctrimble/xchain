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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Resource;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

import org.xml.sax.Attributes;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.WithDefaultsRulesWrapper;

import org.xml.sax.SAXException;

import org.codehaus.plexus.util.xml.XMLWriter;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;

/**
 * @goal monitoring-info
 * @phase generate-resources
 * @requiresProject
 * @requiresDependencyResolution runtime
 * @author Christian Trimble
 */
public class MonitoringMojo
    extends AbstractMojo
{
    /**
     * Location of the file.
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * The monitor flag.  If true, then the monitoring information will be generated.
     * @parameter expression="${build.monitor}" default-value="false"
     */
    private boolean monitor;

    /**
     * The packaging type.
     * @parameter expression="${project.packaging}"
     * @required
     */
    private String packaging;

    /**
     * The maven project for this artifact.
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The set of war dependencies that will be included in this project.
     * @parameter
     */
    private Resource[] webResources;

    public void execute()
        throws MojoExecutionException
    {
      getLog().info("Executing the monitoring information plugin.");
      if( monitor ) {
        getLog().info("Building monitoring information.");

        if( !"jar".equals(packaging) && !"war".equals(packaging) ) {
          throw new MojoExecutionException("Mojo information can only be created for projects packaged as 'jar' or 'war'.");
        }

        MonitoringInfo monitoringInfo = new MonitoringInfo();

        // find all of the directories to get resources from.  If they are filtered, then ignore them.
        List<Resource> resourceSet = (List<Resource>)project.getResources();
        for( Resource resource : resourceSet ) {
          monitoringInfo.getResourceList().add(copy(resource));
        }

        // if this is a war, then we need to merge all of the monitoring-info.xml files into this file.
        if( "war".equals(packaging) ) {
          if( webResources != null ) {
            for( Resource resource : webResources ) {
              monitoringInfo.getWarResourceList().add(copy(resource));
            }
          
            // get all of the directories that hold resources, both static and generated.
            Set<Artifact> artifactSet = (Set<Artifact>)project.getArtifacts();
            for( Artifact artifact : artifactSet ) {
              // if this file is a jar, then try to load its monitoring-info.xml file and add it to this class.
              if( "war".equals(artifact.getType()) ) {
                mergeWarMonitoringInfo(monitoringInfo, artifact.getFile());
              }
            }
          }
        }

        File metaInfDir = new File( project.getBuild().getOutputDirectory(), "META-INF");
        metaInfDir.mkdirs();

        getLog().info("Creating '"+metaInfDir.toString()+"'.");

        File monitoringFile = new File(metaInfDir, "monitoring-info.xml");
        PrintWriter writer = null;
        XMLWriter xmlWriter = null;

        // write the artifact out.
        try {
          writer = new PrintWriter(monitoringFile, "UTF-8");
          xmlWriter = new PrettyPrintXMLWriter(writer, "  ");

          xmlWriter.startElement("monitoring-info");

          // write the resource tags.
          for( Resource resource : monitoringInfo.getResourceList() ) {
            xmlWriter.startElement("resource");
            writeResource(xmlWriter, resource);
            xmlWriter.endElement();
          }
          for( Resource warResource : monitoringInfo.getWarResourceList() ) {
            xmlWriter.startElement("web-resource");
            writeResource(xmlWriter, warResource);
            xmlWriter.endElement();
          }
          xmlWriter.endElement();

          writer.flush();
        }
        catch( IOException ioe ) {
          throw new MojoExecutionException("Could not write out META-INF/monitoring-info.xml file.", ioe);
        }
        finally {
          if( writer != null ) {
            try {
              writer.close();
            }
            catch( Exception ioe ) {
              getLog().warn("Could not close writer for META-INF/monitoring-info.xml file.", ioe);
            }
          }
        }
      }
    }

  private void writeResource( XMLWriter xmlWriter, Resource resource )
  {
    xmlWriter.startElement("directory");
    xmlWriter.writeText(resource.getDirectory());
    xmlWriter.endElement();
    xmlWriter.startElement("filtering");
    xmlWriter.writeText(""+resource.isFiltering());
    xmlWriter.endElement();
    if( resource.getIncludes().size() > 0 ) {
      xmlWriter.startElement("includes");
      for( String include : ((List<String>)resource.getIncludes()) ) {
        xmlWriter.startElement("include");
        xmlWriter.writeText(include);
        xmlWriter.endElement();
      }
      xmlWriter.endElement();
    }
    if( resource.getExcludes().size() > 0 ) {
      xmlWriter.startElement("excludes");
      for( String exclude : ((List<String>)resource.getExcludes()) ) {
        xmlWriter.startElement("exclude");
        xmlWriter.writeText(exclude);
        xmlWriter.endElement();
      }
      xmlWriter.endElement();
    }
  }

  private class LoggingRule
    extends Rule
  {
    public void begin( String namespace, String name, Attributes attributes)
      throws Exception
    {
      getLog().warn("Unknown monitoring-info element encountered {"+namespace+"}"+name+".");
    }
  }

  private void mergeWarMonitoringInfo( MonitoringInfo monitoringInfo, File file )
    throws MojoExecutionException
  {
    JarFile artifactJar = null;
    JarEntry monitoringInfoEntry = null;
    InputStream in = null;
    try {
      getLog().info("Getting monitoring info from file "+file.toString());
      artifactJar = new JarFile(file);
      monitoringInfoEntry = artifactJar.getJarEntry("WEB-INF/classes/META-INF/monitoring-info.xml");
      if( monitoringInfoEntry != null ) {
        in = artifactJar.getInputStream( monitoringInfoEntry );

        // digest the xml file and get all of the entries.
        Digester digester = new Digester();
        digester.push(monitoringInfo);
        digester.addRuleSet(new MonitoringInfoRuleSet());
        WithDefaultsRulesWrapper wrapper = new WithDefaultsRulesWrapper(digester.getRules());
        wrapper.addDefault(new LoggingRule());
        digester.setRules(wrapper);
        digester.parse(in);
      }
      else {
        getLog().info("Monitoring info file not found in "+file.toString());
      }
    }
    catch( SAXException se ) {
      throw new MojoExecutionException("Could not parse a monitoring-info.xml file.", se);
    }
    catch( IOException ioe ) {
      throw new MojoExecutionException("Could not open jar file.", ioe);
    }
    finally {
        if( in != null ) {
          try {
            in.close();
          }
          catch( IOException ioe ) {
            getLog().warn("Could not close a jar entry input stream.", ioe);
          }
        }
        try {
          artifactJar.close();
        }
        catch( IOException ioe ) {
          getLog().warn("Could not close a jar.", ioe );
        }
    }
  }

  private Resource copy( Resource resource )
  {
    Resource copy = new Resource();
    copy.setDirectory(resource.getDirectory());
    copy.setTargetPath(resource.getTargetPath());
    copy.setFiltering(resource.isFiltering());
    copy.setModelEncoding(resource.getModelEncoding());
    copy.setIncludes(copy(resource.getIncludes()));
    copy.setExcludes(copy(resource.getExcludes()));
    return copy;
  }

  private List copy( List list )
  {
    List copy = new ArrayList();
    if( list != null ) {
      copy.addAll(list);
    }
    return copy;
  }
}
