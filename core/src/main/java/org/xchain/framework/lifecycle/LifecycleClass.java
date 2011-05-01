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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Inherited;

/**
 * <p>The annotation that identifies a class that participates in the XChain Lifecycle.  Classes that specify this annotation will be scanned for lifecycle methods that have the StartStep and StopStep
 * annotations.  If any of the methods found are instance methods, then classes annotated with LifecycelClass must also provide a static method annotated with LifecycleAccessor.</p>
 * 
 * @author Christian Trimble
 * @author John Trimble
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface LifecycleClass
{
  /**
   * @return the qName for this lifecycle step.  The qName is in the form {uri}localName.
   */
  String uri();
}
