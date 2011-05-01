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
package org.xchain.namespaces.hibernate.test;

import org.xchain.framework.lifecycle.LifecycleClass;
import org.xchain.framework.lifecycle.LifecycleContext;
import org.xchain.framework.lifecycle.LifecycleException;
import org.xchain.framework.hibernate.HibernateLifecycle;
import org.xchain.framework.lifecycle.StartStep;
import org.xchain.framework.lifecycle.StopStep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.DefaultComponentSafeNamingStrategy;

import javax.xml.namespace.QName;

/**
 * Lifecycle Step to start and stop the HibernateLifecycle.
 *
 * @author Christian Trimble
 * @author afeldhacker
 * @author John Trimble
 * @author Josh Kennedy
 */
@LifecycleClass(uri="http://www.xchain.org/namespaces/hiberante/test")
public class DefaultSessionFactoryLifecycle
{
  protected static final QName MEMORY1_NAME = new QName("http://www.xchain.org/hibernate", "memory1");
  protected static final QName MEMORY2_NAME = new QName("http://www.xchain.org/hibernate", "memory2");

  public static final Logger log = LoggerFactory.getLogger( DefaultSessionFactoryLifecycle.class );

  @StartStep(localName="session-factory", before="{http://www.xchain.org/hibernate}hibernate-configuration")
  public static void startLifecycle( LifecycleContext context )
    throws LifecycleException
  {
    // get the hibernate lifecycle and create a configuration for 
    AnnotationConfiguration memory1 = new AnnotationConfiguration();
    memory1.setNamingStrategy(new DefaultComponentSafeNamingStrategy());
    memory1.configure();

    HibernateLifecycle.setConfiguration(MEMORY1_NAME, memory1);

    AnnotationConfiguration memory2 = new AnnotationConfiguration();
    memory2.setNamingStrategy(new DefaultComponentSafeNamingStrategy());
    memory2.configure();

    HibernateLifecycle.setConfiguration(MEMORY2_NAME, memory2);
  }

  @StopStep(localName="session-factory")
  public static void stopLifecycle( LifecycleContext context )
  {
  }
}

