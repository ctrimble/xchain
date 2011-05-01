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
package org.xchain.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies that a field or a setter method is to be injected with a value from the context.
 *
 * @author Devon Tackett
 */
@Target({FIELD, METHOD})
@Retention(RUNTIME)
@Inherited
@Documented
public @interface In
{
  /**
   * The path for the value.
   */
  String select() default "";

  /**
   * Prefix mappings for the field path.
   */
  PrefixMapping[] prefixMappings() default {};
  
  /**
   * Whether the dependency can be null.  If the dependency can not be null and a value for the select
   * can not be found, an exception will be thrown.
   */
  boolean nullable() default false;
}

