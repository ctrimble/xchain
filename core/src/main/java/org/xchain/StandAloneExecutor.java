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
package org.xchain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.Catalog;
import org.xchain.Command;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.framework.jxpath.ScopedQNameVariables;
import org.xchain.framework.lifecycle.Execution;
import org.xchain.framework.lifecycle.ExecutionException;
import org.xchain.framework.lifecycle.ExecutionTraceElement;
import org.xchain.framework.lifecycle.Lifecycle;
import org.xchain.framework.lifecycle.LifecycleException;
import org.xml.sax.Locator;

/**
 * <p>Bootstraps the environment for executing an XChain.  It used an XML configuration file for
 * determining what catalog to use and which commands to execute.</p>
 * 
 * @author Josh Kennedy
 */
public class StandAloneExecutor implements Executor {
	public static final String CONFIGURATION_SYSTEM_PROPERTY = "org.xchain.executor.configuration";
	public static final String DATASETLOADER_CONFIGURATION_DEFAULT = "executor.xml";
	
	public static final String CATALOG_URI = "{http://www.xchain.org/}catalog";
	public static final String EXECUTE_COMMAND = "{http://www.xchain.org/}command";
	
	public static final Logger log = LoggerFactory.getLogger(StandAloneExecutor.class);
	
	private Properties properties;
	
	/**
	 * Complete any configuration needed for starting the Life Cycle
	 */
	public void initLifeCycle() {
	}
	
	/**
	 * Start the Life Cycle
	 */
	public void startLifeCycle() throws LifecycleException {
		Lifecycle.startLifecycle();
	}
	
	/**
	 * Get the given Catalog based on the property CATALOG_URI
	 */
	public Catalog getCatalog(String uri) throws CatalogNotFoundException, CatalogLoadException {
		return CatalogFactory.getInstance().getCatalog(uri);
	}
	
	/**
	 * Get the command off of the Catalog
	 */
	public Command getCommand(Catalog catalog, String cmd) throws CommandNotFoundException {
		return catalog.getCommand(cmd);
	}
	
	/**
	 * Create the JXPathContext that is going to be used
	 */
	public JXPathContext getContext() {
		return JXPathContext.newContext(new HashMap<Object, Object>());
	}
	
	/**
	 * Calls configure context passing in the properties configuration
	 */
	public void configureContext(JXPathContext context, Map<QName, Object> variables) {
		ScopedQNameVariables contextVariables = (ScopedQNameVariables) context.getVariables();
		
		for (Entry<QName, Object> entry : variables.entrySet()) {
			contextVariables.declareVariable(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * Load the properties configuration into the Context, while maintaining proper scoping
	 * if it's present in the variable name
	 * 
	 * @param context
	 * @param properties
	 */
	protected void configureContext(JXPathContext context, Properties properties) {
		HashMap<QName, Object> variables = new HashMap<QName, Object>();
		
		for (Object key : properties.keySet()) {
			variables.put(new QName((String) key), properties.get(key));
		}
		
		configureContext(context, variables);
	}
	
	/**
	 * Stop the Life Cycle
	 */
	public void stopLifeCycle() throws LifecycleException {
		Lifecycle.stopLifecycle();
	}
	
	/**
	 * Step through the process of using an XChain in the needed order
	 * 
	 * @throws Exception
	 */
	public void execute() throws Exception {
		// Step 1: Configure Lifecycle
		initLifeCycle();
		
		// Step 2: Start life cycle
		startLifeCycle();
		
		try {
			// Step 3: Get the catalog
			Catalog catalog = getCatalog(properties.getProperty(CATALOG_URI));
			
			// Step 4: Get the command
			Command command = getCommand(catalog, properties.getProperty(EXECUTE_COMMAND));
			
			// Step 5: Get the context
			JXPathContext context = getContext();
			
			// Step 5.5: Configure Context
			configureContext(context, properties);
			
			// Step 6: Execute the command
			command.execute(context);
		}
		catch (ExecutionException e) {
			printStack(e.getMessage(), "error", e.getExecutionTrace());
			throw e;
		}
		catch (Exception e) {
			printStack(e.getMessage(), "error");
			throw e;
		}
		finally {
			// Step 8: Stop life cycle
			Lifecycle.stopLifecycle();
		}
	}
	
	/**
	 * Retrieve the properties configuration that the Executor is using
	 * @return
	 */
	public Properties getProperties() {
		return properties;
	}
	
	/**
	 * Set the properties configuration that the Executor will use
	 * @param properties
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	public void printStack(String message) {
		printStack(message, "warn");
	}
	
	public void printStack(String message, String level) {
		printStack(message, level, Execution.getExecutionTrace());
	}
	
	public void printStack(String message, String level, List<ExecutionTraceElement> stack) {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(message);
		buffer.append("\n");
		for (ExecutionTraceElement element : stack) {
			Locator locator = element.getLocator();
			buffer.append("\t running ");
			buffer.append(element.getQName().toString());
			buffer.append(" in ");
			buffer.append(element.getSystemId());
			buffer.append(" at ");
			buffer.append(locator.getLineNumber());
			buffer.append(":");
			buffer.append(locator.getColumnNumber());
			buffer.append("\n");
		}
		
		if (level.toLowerCase().equals("trace")) {
			log.trace(buffer.toString());
		}
		if (level.toLowerCase().equals("debug")) {
			log.debug(buffer.toString());
		}
		if (level.toLowerCase().equals("info")) {
			log.info(buffer.toString());
		}
		if (level.toLowerCase().equals("warn")) {
			log.warn(buffer.toString());
		}
		if (level.toLowerCase().equals("error")) {
			log.error(buffer.toString());
		}
	}
	
	/**
	 * This method is used to populate the defaults for the properties that will be used to execute 
	 * the XChain.
	 * 
	 * @return
	 */
	protected static Properties getDefaultProperties() {
		return new Properties(){
			private static final long serialVersionUID = -3730355766429570858L;

			{
				this.setProperty(EXECUTE_COMMAND, "dispatcher");
				this.setProperty("command", "latest");
			}
		};
	}
	
	/**
	 * Attempts to load properties based off of the three methods to define where a
	 * configuration file is.  First it will try the "standard" configuration placement
	 * "./executor.xml", then it will look for a jvm property CONFIGURATION_SYSTEM_PROPERTY,
	 * and finally it will check to see if a file name was passed in
	 * 
	 * @param properties Properties Object to use
	 * @param args Arguments passed in from the command line
	 * @return
	 */
	protected static boolean loadProperties(Properties properties, String[] args) {
		boolean defaultProp = false;
		boolean jvmProp = false;
		boolean cmdProp = false;
		// This follows the order of default, JVM Param, and finally command line
		// As they are loaded, each new one will overwrite any properties that were
		// loaded prior, but leave any that don't exist in the current file
		defaultProp = loadPropertiesFromFile(DATASETLOADER_CONFIGURATION_DEFAULT, properties);
		
		if (System.getProperty(CONFIGURATION_SYSTEM_PROPERTY) != null) {
			jvmProp = loadPropertiesFromFile(System.getProperty(CONFIGURATION_SYSTEM_PROPERTY), properties);
		}
		
		if (args.length > 0) {
			cmdProp = loadPropertiesFromFile(args[0], properties);
		}
		
		if (defaultProp || jvmProp || cmdProp) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Attempts to load the properties from both the regular file system, as well as
	 * from the contents of the class loader.  If there are files in both locations
	 * they will both be loaded into the properties, the file system takes precedence.
	 * 
	 * @param name file name
	 * @param properties properties object to load file into
	 * @return
	 */
	protected static boolean loadPropertiesFromFile(String name, Properties properties) {
		boolean status = false;
		
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		File propertiesFile = new File(name);
		
		if (stream != null) {
			try {
				properties.loadFromXML(stream);
				status = true;
			} catch (IOException e) {
				log.debug("Unable to read configuration file '" + name + "'", e);
			}
			finally {
				try {
					stream.close();
				} catch (NullPointerException e) {
				} catch (IOException e) {
				}
			}
		}
		
		if (propertiesFile != null && propertiesFile.exists()) {
			try {
				stream = new FileInputStream(propertiesFile); 
				properties.loadFromXML(stream);
				status = true;
			} catch (FileNotFoundException e) {
				log.debug("Unable to find configuration file '" + name + "'", e);
			} catch (IOException e) {
				log.debug("Unable to load configuration file '" + name + "'", e);
			}
			finally {
				try {
					stream.close();
				} catch (NullPointerException e) {
				} catch (IOException e) {
				}
			}
		}

		return status;
	}
	
	public static void main(String[] args) {
		// Set some sane defaults . . .
		Properties properties = getDefaultProperties();
		
		if (!loadProperties(properties, args)) {
			log.warn("Unable to find and load a configuration, proceeding with defaults.  Please see the usage documentaion.");
		}

		try {
			StandAloneExecutor executor = new StandAloneExecutor();
			
			executor.setProperties(properties);
			
			executor.execute();
		} catch (Exception e) {
			log.error("There was an unexpected exception while executing the xchain", e);
		}
	}
}
