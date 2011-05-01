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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.commons.jxpath.JXPathContext;
import org.hibernate.cfg.Configuration;
import org.xchain.framework.hibernate.HibernateLifecycle;
import org.xchain.framework.util.IoUtil;

/**
 * <p>Bootstraps the environment for executing an XChain.  It used an XML configuration file for
 * determining what catalog to use and which commands to execute.  It will also try and load in a Hibernate
 * Configuration file and add it to the XChain Context.</p>
 * 
 * @author Josh Kennedy
 * @author Mike Moulton
 */
public class StandAloneHibernateExecutor extends StandAloneExecutor {
  // XChain Configs
  public static final String XCHAIN_EXECUTE = "{http://www.xchain.org/hibernate/}xchainEnabled";
  public static final String XCHAIN_EXECUTE_ENV = "xchain_enabled";

  // Deploy Data Source Configs
	public static final String DEPLOY_DATASOURCE_CONFIGURATION = "{http://www.xchain.org/hibernate/}deploy_datasource";
	public static final String DEPLOY_DATASOURCE_CONFIGURATION_ENV = "deploy_datasource";

	public static final String DATASOURCE_CONFIGURATION = "{http://www.xchain.org/hibernate/}datasource";
	public static final String DATABASE_CONFIGURATION = "{http://www.xchain.org/hibernate/}database";

	public static final String DEPLOY_LOCATION = "{http://www.xchain.org/hibernate/}deploylocation";
	public static final String DEPLOY_LOCATION_ENV = "DEPLOY_LOCATION";
	
  // Shell Execution Config
	public static final String COMMAND_EXECUTE_ENABLED = "{http://www.xchain.org/hibernate/}command_enabled";
	public static final String COMMAND_EXECUTE_ENABLED_VAR = "command_enabled";

	public static final String COMMAND_EXECUTE_SHELL = "{http://www.xchain.org/hibernate/}command_shell";
	public static final String COMMAND_EXECUTE_SCRIPT = "{http://www.xchain.org/hibernate/}command_script";
	
	public static final String COMMAND_EXECUTE_SHELL_DEFAULT = "bash";
	
	public static final String CONFIGURATION_URI = "hibernate.configuration";
	
	Configuration hibernateConfig;
	
	/**
	 * Initalize the Hibernate specific Configurations
	 */
	public void initLifeCycle() {
		if (this.getProperties().getProperty(CONFIGURATION_URI, null) != null && this.getProperties().getProperty(CONFIGURATION_URI).length() > 0) {
			Configuration hibernateConfig = new Configuration();
			hibernateConfig.configure(this.getProperties().getProperty(CONFIGURATION_URI));
	
			HibernateLifecycle.setConfiguration(hibernateConfig);
		}
	}
	
	/**
	 * Add the Hibernate config as a variable
	 */
	public void configureContext(JXPathContext context, Map<QName, Object> variables) {
		super.configureContext(context, variables);
		
		context.getVariables().declareVariable("configuration", hibernateConfig);
	}
	
	/**
	 * Used to copy datasource jndi configurations to the Application Server
	 * @param deployLocation
	 * @param dataSource
	 * @return int status 0 == good, > 0 == error
	 */
	protected int deployDatasource(String deployLocation, String dataSource) {
		if (deployLocation == null || dataSource == null) {
			log.warn("Both deploy location '{}' and datasource '{}' must be specified.", deployLocation, dataSource);
			return DataSourceDeploy.CONFIGURATION_ERROR;
		}
		
		// Check if Deploy Directory exists
		File deployDirectory = new File(deployLocation);
		if (deployDirectory == null || !deployDirectory.exists() || !deployDirectory.isDirectory() || !deployDirectory.canWrite()) {
			log.warn("Could not access Deploy directory '{}'.", deployDirectory.getAbsolutePath());
			return DataSourceDeploy.DEPLOY_DESTINATION_NONEXISTENT;
		}
		
		InputStream datasourceConfiguration = null;
		try {
			datasourceConfiguration = IoUtil.getFileStream(dataSource, log);
		} catch (FileNotFoundException e) {
			log.warn("Could not access configuration file '{}'.", datasourceConfiguration);
			return DataSourceDeploy.DATASOURCE_CONFIGURATION_NONEXISTENT;
		}
		
		
		File deployDatasourceFile = new File(deployDirectory, new File(dataSource).getName());
		log.info("Destination file is '{}'.", deployDatasourceFile.getAbsolutePath());
		
		if (IoUtil.fileCopy(datasourceConfiguration, deployDatasourceFile, log)) {
			return DataSourceDeploy.SUCCESS;
		}
		else {
			return DataSourceDeploy.DATASOURCE_COPY_FAILED;
		}
	}
	
	protected int runCommands() {
	  // Find all of the Commands in the Executor XML
	  // For each one, copy the script to the local file system
	  // then execute it
	  
	  // For now just enable the ability to run one command
	  // In order to run multiples the best approach would be a
	  // seperate xml config that contains the commands to run
	  try {
      String shell = this.getProperties().getProperty(COMMAND_EXECUTE_SHELL, COMMAND_EXECUTE_SHELL_DEFAULT);
      String command = this.getProperties().getProperty(COMMAND_EXECUTE_SCRIPT, null);
      
      if (command != null && command.length() > 0) {
        File dstFile = new File("script-" + UUID.randomUUID().toString());
        
        IoUtil.fileCopy(IoUtil.getFileStream(command, log), dstFile, log);
        
        String[] arguments = new String[]{shell, dstFile.getAbsolutePath()};
        
        log.trace("Executing: {} {}", arguments);
        Process process = Runtime.getRuntime().exec(arguments);

        int exitCode = process.waitFor();
        dstFile.delete();
        log.debug("Finished executing command {} ({}).", command, exitCode);
      }
    } catch (Exception e) {
      log.warn("Exception occured while trying to execute scripts", e);
      return 9;
    }
	  
	  return 0;
	}
	
	/**
	 * Add Hibernate specific defaults to the base defaults
	 * 
	 * @return
	 */
	protected static Properties getDefaultProperties() {
		Properties properties = StandAloneExecutor.getDefaultProperties();

		properties.setProperty(DEPLOY_DATASOURCE_CONFIGURATION, "true");
		properties.setProperty("command", "update-to-latest-build");
		properties.setProperty("exportSchema", "false");

		return properties;
	}

  private static boolean getCommandExecuteEnabled(String envVarName, String propertyName, Properties props, boolean def) {
    // Check JVM Command
    String sysvar = System.getProperty(envVarName, null);
    String prop = props.getProperty(propertyName, null);
    String envvar = null;
    try {
      envvar = System.getenv(envVarName);
    } catch (SecurityException e) {
      log.warn("Error checking the System Environment", e);
    }

    log.debug("System => {}, Env => {}, Prop => {}", new String[]{ sysvar, envvar, prop });

    if (sysvar != null && sysvar.length() > 0)
      return Boolean.valueOf(sysvar);

    // Check ENV Var
    if (envvar != null && envvar.length() > 0)
      return Boolean.valueOf(envvar);

    // Check Config
    if (prop != null && prop.length() > 0)
      return Boolean.valueOf(prop);

    return def;
  }
	
	public static class DataSourceDeploy {
		// Even numbers are 'success'
	  public static final int SUCCESS = 0;
		public static final int DATASOURCE_COPY_FAILED = 2;
		public static final int DATASOURCE_CONFIGURATION_NONEXISTENT = 4;

		// Odd Numbers are 'failures'
		public static final int CONFIGURATION_ERROR = 1;
		public static final int DEPLOY_DESTINATION_NONEXISTENT = 3;
		public static final int UNCAUGHT_EXCEPTION = 5;
	}

	public static void main(String[] args) {
		int status = 0;
		Properties properties = getDefaultProperties();

		if (!loadProperties(properties, args)) {
			StandAloneExecutor.log.warn("Unable to find and load a configuration, proceeding with defaults.  Please see the usage documentaion.");
		}
		
		StandAloneHibernateExecutor executor = new StandAloneHibernateExecutor();
    executor.setProperties(properties);

		try {
      if (getCommandExecuteEnabled(XCHAIN_EXECUTE_ENV, XCHAIN_EXECUTE, executor.getProperties(), true)) {
        log.debug("EXECUTING XCHAIN");
        executor.execute();
      }
      else {
        log.info("SKIPPING XCHAIN");
      }
		} catch (Exception e) {
			StandAloneExecutor.log.error("There was an unexpected exception while executing the xchain", e);
			status = 7;
		}
		
		// Deploy the Data source
		try {
      if (getCommandExecuteEnabled(DEPLOY_DATASOURCE_CONFIGURATION_ENV, DEPLOY_DATASOURCE_CONFIGURATION, executor.getProperties(), true)) {
        log.debug("DEPLOYING DATASOURCE");
				status = executor.deployDatasource(
					executor.getProperties().getProperty(DEPLOY_LOCATION, System.getenv(DEPLOY_LOCATION_ENV)), 
					executor.getProperties().getProperty(DATASOURCE_CONFIGURATION, null)
				);
			}
      else {
        log.info("SKIPPING DATASOURCE");
      }
		} catch (Exception e) {
		  StandAloneExecutor.log.error("There was an unexpected exception while deploying the datasource", e);
			status = 7;
		}
		
		try {
      if (getCommandExecuteEnabled(COMMAND_EXECUTE_ENABLED_VAR, COMMAND_EXECUTE_ENABLED, executor.getProperties(), false)) {
        log.debug("EXECUTING COMMANDS");
		    status = executor.runCommands();
		  }
      else {
        log.info("SKIPPING COMMANDS");
      }
		} catch (Exception e) {
		  StandAloneExecutor.log.error("There was an unexpected exception while executing scripts", e);
			status = 7;
		}
		
		log.debug("Exiting with status: {}", status);
		System.exit(status);
	}
}
