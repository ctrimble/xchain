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
package org.xchain.framework.lifecycle;

import static org.junit.Assert.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
public class LifecycleTest
{
  public static Logger log = LoggerFactory.getLogger(LifecycleTest.class);

  @Before public void setUp()
  {
  }

  @After public void tearDown()
  {
  }

  @Test public void testLifecycleRunning()
    throws Exception
  {
    Lifecycle.startLifecycle();

    assertTrue("The lifecycle claims it is not running when it is.", Lifecycle.isRunning());

    Lifecycle.stopLifecycle();
  }

}
