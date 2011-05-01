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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Inherited;

/**
 * <p>The annotation for marking methods as StartSteps in the XChain Lifecycle.  This annotation should be
 * used on methods of classes annotated with LifecycleClass.  The name of the step will be the namespace defined
 * for the lifecycle class combined with the local name defined on the StartStep.  If a StartStep and a StopStep
 * in the same Lifecycle Class share the same local name, then they are linked as the same LifecycleStep.</p>
 *
 * <p>If this StartStep needs to execute after another StartStep, then you can add that StartStep's QName to the after array.  Each step in
 * this array will execute before this StartStep.  If a StartStep is linked to a StopStep, then an entry in the after array of the StartStep could
 * also be represented as an entry in the before array of the StopStep.</p>
 *
 * <p>If this StartStep needs to execute before another StartStep, then you can add that StartStep's QName to the before array.  Each step in
 * this array will execte after this StartStep.  If a StartStep is linked to a StopStep, then an entry in the before array of the StartStep could
 * also be represented as an entry in the after array of the StopStep.</p>
 *
 * @author Christian Trimble
 * @author John Trimble
 */
@Target(METHOD)
@Retention(RUNTIME)
@Inherited
@Documented
public @interface StartStep
{
  /**
   * @return the local name for this StartStep.
   */
  String localName();

  /**
   * <p>The array of QNames for StartSteps that will execute before this StartStep.  The strings in this array should be of the form
   * "{namespace}localName".  If a QName does not define a namespace, then it will be assumed that it is in the namespace of the LifecycleClass
   * in which it is defined.</p>
   * @return the array of StartStep qNames that will execute before this StartStep.
   */
  String[] after() default {};

  /**
   * <p>The array of QNames for StartSteps that will execute after this StartStep.  The strings in this array should be of the form "{namespace}localName".  If
   * a QName does not define a namespace, then it will be assumed that it is in the namespace of the LifecycleClass in which it is defined.</p>
   *
   * @return the array of StartStep qNames that will execute after this StartStep.
   */
  String[] before() default {};
  
  String[] xmlns() default {};
}
