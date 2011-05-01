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

/**
 * @author Christian Trimble
 * @author John Trimble
 */
@LifecycleClass(uri="http://www.xchain.org/lifecycle/test-static")
public class StaticLifecycleClass
{
  @StartStep(localName="step1")
  public static void startStep1( LifecycleContext context )
    throws LifecycleException
  {

  }

  @StartStep(localName="step2")
  public static void startStep2( LifecycleContext context )
    throws LifecycleException
  {

  }

  @StopStep(localName="step2")
  public static void stopStep2( LifecycleContext context )
  {

  }

  @StopStep(localName="step3")
  public static void stopStep3( LifecycleContext context )
  {

  }

  @StartStep(localName="step4", after={"step3"})
  public static void startStep4( LifecycleContext context )
  {

  }

  @StopStep(localName="step4")
  public static void stopStep4()
  {

  }

  @StartStep(localName="step5")
  public static void stopStep5()
  {

  }

  @StopStep(localName="stop5", before={"step4"})
  public static void stopStep5( LifecycleContext context )
  {

  }
}
