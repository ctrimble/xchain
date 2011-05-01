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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.surefire.booter.IsolatedClassLoader;
import org.xchain.tools.executeplugin.AbstractXChainMojo;

import org.hibernate.cfg.Configuration;

/**
 * Configure Hibernate.
 * 
 * Run an XChain.
 * 
 * @author John Trimble
 * @author Jason Rose
 * @requiresDependencyResolution runtime
 * @goal execute-xchain
 */
public class ExecuteMojo extends AbstractXChainMojo {
	
	private static final String LIFE_CYCLE_CLASS_NAME = "org.xchain.framework.lifecycle.Lifecycle";
	private static final String HIBERNATE_LIFE_CYCLE_CLASS_NAME = "org.xchain.framework.hibernate.HibernateLifecycle";
	private static final String CONFIGURATION_CLASS_NAME = Configuration.class.getName();
	private static final String CONFIGURATION_CONFIGURE_CONFIGURATION_METHOD = "configure";
	private static final Class<?>[] CONFIGURATION_CONFIGURE_CONFIGURATION_ARGUMENTS = new Class<?>[] {File.class};
	private static final String CONFIGURATION_SET_CONFIGURATION_METHOD = "setConfiguration";
	private static final String CATALOG_FACTORY_CLASS_NAME = "org.xchain.framework.factory.CatalogFactory";
	private static final String CATALOG_CLASS_NAME = "org.xchain.Catalog";
	private static final String JXPATH_CONTEXT_CLASS_NAME = "org.apache.commons.jxpath.JXPathContext";
	private static final String LIFE_CYCLE_START_LIFE_CYCLE_METHOD = "startLifecycle";
	private static final String LIFE_CYCLE_STOP_LIFE_CYCLE_METHOD = "stopLifecycle";
	private static final String COMMAND_CLASS_NAME = "org.xchain.Command";
	private static final String CATALOG_FACTORY_INSTANCE_METHOD_NAME = "getInstance";
	private static final String CATALOG_FACTORY_CATALOG_METHOD_NAME = "getCatalog";
	private static final Class<?>[] CATALOG_FACTORY_CATALOG_ARGUMENT_TYPES = new Class<?>[] {String.class};
	private static final String CATALOG_COMMAND_METHOD_NAME = "getCommand";
	private static final Class<?>[] CATALOG_COMMAND_ARGUMENT_TYPES = new Class<?>[] {String.class};
	private static final String COMMAND_EXECUTE_METHOD_NAME = "execute";
	
	/**
     * @parameter expression="${catalog}"
     * @required
     */
    private String catalog;
    
    /**
     * Configuration has to be a File because we don't have the url resolver available before the xchains lifecycle has started, so we can't resolve "resource://context-class-loader" urls.
     * @parameter expression="${configuration}"
     */
    private File configuration;
    
    /**
     * @parameter expression="${dispatcherCommand}" default-value="dispatcher"
     * @required
     */
    private String dispatcherCommand;
    
    /**
     * @parameter expression="${command}" default-value="update-to-latest-build"
     */
    private String command;
    
    /**
     * @parameter default-value=false
     */
    private boolean exportSchema;
    
    /**
     * @parameter default-value=true
     */
    private boolean runXChainLifeCycle;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		ClassLoader chainClassLoader = createClassLoader(Thread.currentThread().getContextClassLoader(), this.classpathElements);
		
		try {
			Class<?> lifeCycleClass = chainClassLoader.loadClass(LIFE_CYCLE_CLASS_NAME);
			Class<?> hibernateLifeCycleClass = chainClassLoader.loadClass(HIBERNATE_LIFE_CYCLE_CLASS_NAME);
			Class<?> catalogFactoryClass = chainClassLoader.loadClass(CATALOG_FACTORY_CLASS_NAME);
			Class<?> catalogClass = chainClassLoader.loadClass(CATALOG_CLASS_NAME);
			Class<?> jxpathContextClass = chainClassLoader.loadClass(JXPATH_CONTEXT_CLASS_NAME);
			Class<?> scopedJXPathContextClass = chainClassLoader.loadClass("org.xchain.framework.jxpath.ScopedJXPathContextImpl");
			Class<?> contextFactoryClass = chainClassLoader.loadClass("org.xchain.framework.jxpath.JXPathContextFactoryImpl");
			Class<?> scopedQNameVariablesClass = chainClassLoader.loadClass("org.xchain.framework.jxpath.ScopedQNameVariables");
			Class<?> commandClass = chainClassLoader.loadClass(COMMAND_CLASS_NAME);
			Class<?> configurationClass = chainClassLoader.loadClass(CONFIGURATION_CLASS_NAME);
			
			// LifeCycle methods
			Method startLifeCycleMethod = lifeCycleClass.getMethod(LIFE_CYCLE_START_LIFE_CYCLE_METHOD, new Class<?>[]{});
			Method stopLifeCycleMethod = lifeCycleClass.getMethod(LIFE_CYCLE_STOP_LIFE_CYCLE_METHOD, new Class<?>[]{});
			
			// CatalogFactory methods
			Method getInstanceMethod = catalogFactoryClass.getMethod(CATALOG_FACTORY_INSTANCE_METHOD_NAME, new Class<?>[]{});
			Method getCatalogMethod = catalogFactoryClass.getMethod(CATALOG_FACTORY_CATALOG_METHOD_NAME, CATALOG_FACTORY_CATALOG_ARGUMENT_TYPES);
			
			// Catalog methods
			Method getCommandMethod = catalogClass.getMethod(CATALOG_COMMAND_METHOD_NAME, CATALOG_COMMAND_ARGUMENT_TYPES);
			
			// Command methods
			Method executeMethod = commandClass.getMethod(COMMAND_EXECUTE_METHOD_NAME, jxpathContextClass);
			
			// JXPathContext methods
			Method newContextMethod = contextFactoryClass.getMethod("newContext", jxpathContextClass, Object.class);
			Method declareVariableMethod = scopedQNameVariablesClass.getMethod("declareVariable", String.class, Object.class);
			Method getVariablesMethod = scopedJXPathContextClass.getMethod("getVariables", new Class[] {});
			
			// Configuration methods
			Method configureMethod = configurationClass.getMethod(CONFIGURATION_CONFIGURE_CONFIGURATION_METHOD, CONFIGURATION_CONFIGURE_CONFIGURATION_ARGUMENTS);
			Method setConfigurationMethod = hibernateLifeCycleClass.getMethod(CONFIGURATION_SET_CONFIGURATION_METHOD, configurationClass);
			
			// Step 1: Switch Class loader
			ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(chainClassLoader);
			
			// Step 2: Configure Lifecycle
			Object configurationObject = null;
			if( configuration != null && configuration.exists() ) {
  			configurationObject = configurationClass.newInstance();
  			configurationObject = configureMethod.invoke(configurationObject, this.configuration);
  			setConfigurationMethod.invoke(null, new Object[]{configurationObject});
			}
			
			// Step 3: Start life cycle
			if( this.runXChainLifeCycle ) startLifeCycleMethod.invoke(null, new Object[]{});
			
			// Step 4: Get the catalog
			Object catalogFactoryObject = getInstanceMethod.invoke(null, new Object[] {});
			Object catalogObject = getCatalogMethod.invoke(catalogFactoryObject, new Object[] {this.catalog});
			
			// Step 5: Get the command
			Object commandObject = getCommandMethod.invoke(catalogObject, new Object[] {this.dispatcherCommand});
			
			// Step 6: Configure the context
			Object jxpathContextFactoryObject = contextFactoryClass.newInstance();
			Object jxpathContextObject = newContextMethod.invoke(jxpathContextFactoryObject, null, new Object());
			Object variablesObject = getVariablesMethod.invoke(jxpathContextObject, new Object[] {});
			declareVariableMethod.invoke(variablesObject, "command", this.command);
			declareVariableMethod.invoke(variablesObject, "exportSchema", this.exportSchema);
			declareVariableMethod.invoke(variablesObject, "configuration", configurationObject);

			// Step 7: Execute the command
			executeMethod.invoke(commandObject, jxpathContextObject);
			
			// Step 8: Stop life cycle
			if( this.runXChainLifeCycle ) stopLifeCycleMethod.invoke(null, new Object[]{});
			
			// Step 9: Switch Class loader back
			Thread.currentThread().setContextClassLoader(originalContextClassLoader);
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
		  e.printStackTrace();
		}
	}
	
	private ClassLoader createClassLoader(ClassLoader parent, Collection<String> classPathElementFilePaths) {
		IsolatedClassLoader isolatedClassLoader = new IsolatedClassLoader(parent);
		if( classPathElementFilePaths != null && classPathElementFilePaths.size() > 0 ) {
			for( String classElementString : classPathElementFilePaths ) {
				File classElementFile = new File(classElementString);
				try {
					URL url = classElementFile.toURL();
					isolatedClassLoader.addURL(url);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return isolatedClassLoader;
	}
}
