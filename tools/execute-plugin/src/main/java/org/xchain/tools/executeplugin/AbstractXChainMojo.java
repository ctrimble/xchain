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
package org.xchain.tools.executeplugin;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.maven.surefire.booter.IsolatedClassLoader;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.codehaus.classworlds.ClassRealm;

/**
 * @author John Trimble
 */
public abstract class AbstractXChainMojo extends org.apache.maven.plugin.AbstractMojo {

	/**
	 * The maven project.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject mavenProject;

	/**
	 * The class realm.
	 * 
	 * @parameter expression="${dummyExpression}"
	 */
	protected ClassRealm realm;
	
    /**
     * Additional elements to be appended to the classpath.
     * 
     * @parameter
     */
    protected List additionalClasspathElements;

	/**
	 * The base dir.
	 * 
	 * @parameter expression="${basedir}"
	 * @required
	 * @readonly
	 */
	protected String baseDir;

	/**
	 * @parameter expression="${build.monitoring}" default-value="true"
	 * @required
	 */
	protected String monitoring;

	/**
	 * @parameter expression="${build.mode}" default-value="DEV"
	 * @required
	 */
	protected String buildMode;

	/**
	 * 
	 * @parameter expression="${basedir}/src/main"
	 * @required
	 */
	protected String sourceDirectory;

	/**
	 * @parameter expression="${basedir}/src/main/java"
	 * @required
	 */
	protected String sourceJavaDirectory;

	/**
	 * @parameter expression="${basedir}/src/main/resources"
	 * @required
	 */
	protected String sourceResourcesDirectory;

	/**
	 * @parameter expression="${basedir}/src/main/webapp"
	 * @required
	 */
	protected String sourceWebappDirectory;

	/**
	 * @parameter expression="${basedir}/target/classes"
	 * @required
	 */
	protected String outputDirectory;

	/**
	 * The top level directory where sources are generated. The default is
	 * '${project.build.directory}/generated'.
	 * 
	 * @parameter expression="${project.build.directory}/generated"
	 * @required
	 */
	protected String generatedDirectory;

	/**
	 * The source generation directory.
	 * 
	 * @parameter expression="${project.build.directory}/generated/main/java"
	 * @required
	 */
	protected String generatedJavaDirectory;

	/**
	 * The directory where generated resources are placed.
	 * 
	 * @parameter expression="${project.build.directory}/generated/main/resources"
	 * @required
	 */
	protected String generatedResourcesDirectory;

	/**
	 * The directory where generated webapp files are placed.
	 * 
	 * @parameter expression="${project.build.directory}/generated/main/webapp"
	 * @required
	 */
	protected String generatedWebappDirectory;

	/**
	 * @parameter expression="${project.artifactId}"
	 * @required
	 * @readonly
	 */
	protected String artifactId;

	/**
	 * @parameter expression="${project.groupId}"
	 * @required
	 * @readonly
	 */
	protected String groupId;

	/**
	 * @parameter expression="${project.version}"
	 * @required
	 * @readonly
	 */
	protected String version;

	/**
	 * The cache directory for dependency information.
	 * 
	 * @parameter expression="${project.build.directory}/cache/meltmojo"
	 * @required
	 */
	protected String cacheDirectory;

	/**
	 * The name of the resource that plugins use to be configured.
	 * 
	 * @parameter expression="META-INF/meltmedia/melt-mojo-component-config.xml"
	 * @required
	 */
	protected String componentResourceName;

	/**
	 * @component
	 */
	protected ArtifactResolver artifactResolver;

	/**
	 * @component
	 */
	protected ArtifactFactory artifactFactory;

	/**
	 * @component
	 */
	protected ArtifactMetadataSource metadataSource;

	/**
	 * @component
	 */
	private MavenProjectBuilder projectBuilder;

	/**
	 * @parameter expression="${localRepository}"
	 */
	protected ArtifactRepository localRepository;

	/**
	 * @parameter expression="${project.remoteArtifactRepositories}"
	 */
	protected List remoteRepositories;

	/**
	 * @parameter
	 */
	protected List dependencies;
	
    /**
     * The classpath elements of the project being tested.
     * 
     * @parameter expression="${project.testClasspathElements}"
     * @required
     * @readonly
     */
    protected List<String> classpathElements;

}
