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
 * <p>The annotation for marking methods as StopSteps in the XChain Lifecycle.  This annotation should be
 * used on methods of classes annotated with LifecycleClass.  The name of the step will be the namespace defined
 * for the lifecycle class combined with the local name defined on the stop step.  If a StartStep and a StopStep
 * in the same Lifecycle Class share the same local name, then they are linked as the same LifecycleStep.</p>
 *
 * <p>If this StopStep needs to execute after another StopStep, then you can add that StopStep's QName to the after array.  Each step in
 * this array will execute before this StopStep.  If a StopStep is linked to a StartStep, then an entry in the after array of the StopStep could
 * also be represented as an entry in the before array of the StartStep.</p>
 *
 * <p>If this StopStep needs to execute before another StopStep, then you can add that StopStep's QName to the before array.  Each step in
 * this array will execte after this StopStep.  If a StopStep is linked to a StartStep, then an entry in the before array of the StopStep could
 * also be represented as an entry in the after array of the StartStep.</p>
 *
 * <p>If this StopStep is linked to a StartStep, then it is garenteed to execute if the StartStep executes, even if the StartStep throws an exception.  By including an
 * argument of Throwable, or a subclass thereof, your stop step can be notified of the exception that caused the StartStep to fail.</p>
 * 
 * @author Christian Trimble
 * @author John Trimble
 */
@Target(METHOD)
@Retention(RUNTIME)
@Inherited
@Documented
public @interface StopStep
{
  /**
   * @return the local name for this lifecycle step.
   */
  String localName();

  /**
   * <p>The array of QNames for StopSteps that will execute before this StopStep.  The strings in this array should be of the form
   * "{namespace}localName".  If a QName does not define a namespace, then it will be assumed that it is in the namespace of the LifecycleClass
   * in which it is defined.</p>
   * @return the array of StopStep qNames that will execute before this StopStep.
   */
  String[] after() default {};

  /**
   * <p>The array of QNames for StopSteps that will execute after this StopStep.  The strings in this array should be of the form "{namespace}localName".  If
   * a QName does not define a namespace, then it will be assumed that it is in the namespace of the LifecycleClass in which it is defined.</p>
   *
   * @return the array of StopStep qNames that will execute after this StopStep.
   */
  String[] before() default {};
}
