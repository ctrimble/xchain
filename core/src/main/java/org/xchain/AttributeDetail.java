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

import javax.xml.namespace.QName;
import org.xchain.annotations.AttributeType;

/**
 * Detailed information for a command attribute.  The attribute details for an instance of a command are available from the
 * EngineeredCommand interface.
 *
 * @see org.xchain.EngineeredCommand#getAttributeDetailMap()
 * @author Christian Trimble
 */
public class AttributeDetail {

  /**
   * The QName for this attribute.
   */
  private QName qName = null;

  /**
   * The type for this attrubute, after it is evaluted against a JXPathContext.
   */
  private Class javaType = null;

  /**
   * The type of expression that this attribute represents.
   */
  private AttributeType type = null;

  /**
   * A flag that specifies if this attribute is required.
   */
  private boolean required = false;

  /**
   * Creates a new attribute detail object with the specified values.
   *
   * @param qName the name of the attribute.
   * @param type the type of expression that this attribute's value represents.
   * @param javaType the java type that results from evaluating the attribute's value.
   * @param required true if the attribute is required, false otherwise.
   */
  public AttributeDetail( QName qName, AttributeType type, Class javaType, boolean required ) {
    this.qName = qName;
    this.type = type;
    this.javaType = javaType;
    this.required = required;
  }

  /**
   * Returns the QName of the attribute.
   *
   * @return the QName of this attribute.
   */
  public QName getQName() { return this.qName; }

  /**
   * Returns the java type that results from evaluating this attribute's value.
   *
   * @return the java type that results from evaluating this attribute's value.
   */
  public Class getJavaType() { return this.javaType; }

  /**
   * The type of expression that this attribute's value represents.
   *
   * @return the type of expression that this attribute's value represents.
   */
  public AttributeType getType() { return this.type; }

  /**
   * Returns true, if this attribute is required, false otherwise.
   *
   * @return true if this attribute is required, false otherwise.
   */
  public boolean getRequired() { return this.required; }
}
