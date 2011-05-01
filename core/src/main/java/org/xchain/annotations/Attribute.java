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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Inherited;
import javax.xml.XMLConstants;

/**
 * <p>Binds an abstract method to an xml attribute for a Command.  When a command has an abstract
 * method marked with the annotation, XChains will engineer all of the code needed to access the value described
 * by the attribute from a JXPathContext.</p>
 *
 * <p>Methods marked with this annotation must meet the following requirements:</p>
 * <ul>
 *   <li>The method must be abstract.</li>
 *   <li>The method must take a JXPathContext as its first parameter.</li>
 *   <li>If the method has two parameters, the second parameter must be of type java.lang.Class and the annotation's type attribute must be AttributeType.JXPATH_VALUE.</li>
 *   <li>The method must not have more than two parameters.</li>
 * </ul>
 *
 * <h3>Basic usage:</h3>
 * <p>To bind a method to an attribute, two things must be specified.  First the name of the attribute must be specified.  Secondly, the way to treat the attributes value must be specified.
 * This can be accomplished with the localName and type attributes of this annotation.  Here is an example:</ p>
 * <pre>
 *   &#064;Attribute(localName="attr", type=AttributeType.JXPATH_VALUE)
 *   public abstract Object getAttr(JXPathContext context);
 * </pre>
 * <p>This method binds the attribute "attr" in the default namespace and defines its value to be an XPath.  The engineered method will include a call to
 * JXPathContext.getValue(String, Class) where the xpath is the value of the attribute "attr" and the class is java.lang.Object.</p>
 *
 * <h3>Defining a default value for an attribute:</h3>
 * <p>When defining a default value for an attribute, the defaultValue attribute of the annotation must be defined.  If the default value uses any prefixes, then the
 * defaultPrefixMappings attribute must also be included on the annotation and it must include the prefixes required for the default value.  For example:</p>
 * <pre>
 *   &#064;Attribute(localName="attr", type=AttributeType.JXPATH_VALUE, defaultValue="example:function()", defaultPrefixMappings={"xmlns:example='http://www.xchain.org/example'"}
 *   public abstract Object getAttr(JXPathContext context);
 * </pre>
 * <p>If the attribute "attr" is not specified, then the default prefix mappings will be registered with the context and the attribute will be evaluated as though it had
 * the value "example:function()".  Once the evaluation is done, then all of the default prefix mappings will be unregistered.</p>
 *
 * <h3>Marking an attribute as required:</h3>
 * <p>When defining that an attribute is required, the required attribute of the annotation must be defined to be true.  If this part of the annotation is defined, then an XChain will
 * not load if the attribute is missing.  For example:</p>
 * <pre>
 *   &#064;Attribute(localName="attr", type=AttributeType.JXPATH_VALUE, required=true}
 *   public abstract Object getAttr(JXPathContext context);
 * </pre>
 * <p>If the attribute "attr" is not specified, then any attempt to load a catalog containing that command will fail with an exception.</p>
 * 
 * @author Mike Moulton
 * @author Christian Trimble
 */
@Target(METHOD)
@Retention(RUNTIME)
@Inherited
@Documented
public @interface Attribute
{
  /**
   * <p>Defines the local name component of this attribute's QName.</p>
   *
   * @return the local name component of this attribute's QName.</p>
   */
  String localName();

  /**
   * <p>Defines the namespace uri component of this attributes QName.</p>
   *
   * @return the namespace uri component of this attribute's QName. 
   */
  String namespaceUri() default XMLConstants.NULL_NS_URI;

  /**
   * <p>Defines the type of this attributes value.</p>
   *
   * @return the type of this attributes value.
   */
  AttributeType type() default AttributeType.JXPATH_VALUE;

  /**
   * <p>Defines the default value for this attribute.</p>
   *
   * @return the default value for this attribute.
   */
  String defaultValue() default "";

  /**
   * <p>Defines the prefix mapping context for the default value.  The values of this array are defined like namespace prefixes in
   * an xml document.  Here is an example that binds the prefixes "one" and "two" to the namespaces "http://www.xchain.org/one" and "http://www.xchain.org/two", respectively.</p>
   * <pre>
   *   defaultPrefixMapping={"xmlns:one='http://www.xchain.org/one'", "xmlns:two='http://www.xchain.org/two'"}
   * </pre>
   *
   * @return the prefix mapping context for the default value.
   */
  PrefixMapping[] defaultPrefixMappings() default {};

  /**
   * <p>Defines if this attribute is required.  A value of true means that this attribute is required.  A value of false means that this attribute is not required.  The default
   * value if false.</p>
   *
   * @return true if this attribute is required, false otherwise.
   */
  boolean required() default false;
}
