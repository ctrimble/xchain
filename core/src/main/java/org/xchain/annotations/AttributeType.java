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

import javax.xml.namespace.NamespaceContext;
import org.apache.commons.jxpath.JXPathException;
import org.xchain.framework.jxpath.JXPathValidator;
import org.xchain.framework.util.AttributesUtil;
import org.xchain.framework.util.JXPathContextUtil;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 */
public enum AttributeType {
  /** The attribute type for jxpath attributes that get their value with JXPathContext.getValue(String jxpath) */
  JXPATH_VALUE() {
    public void validate( String value, NamespaceContext namespaceContext ) {
      JXPathValidator.validate( value, namespaceContext );
    }
  },
  /** The attribute type for jxpath attributes that get their value with JXPathContext.selectNodes(String jxpath) */
  JXPATH_SELECT_NODES() {
    public void validate( String value, NamespaceContext namespaceContext ) {
      JXPathValidator.validate( value, namespaceContext );
    }
  },
  /** The attribute type for jxpath attributes that get their value with JXPathContext.selectSingleNode(String jxpath) */
  JXPATH_SELECT_SINGLE_NODE() {
    public void validate( String value, NamespaceContext namespaceContext ) {
      JXPathValidator.validate( value, namespaceContext );
    }
  },
  /** The attribute type for jxpath attributes that get their value with JXPathContext.getPointer(String jxpath) */
  JXPATH_POINTER() {
    public void validate( String value, NamespaceContext namespaceContext ) {
      JXPathValidator.validate( value, namespaceContext );
    }
  },
  /** The attribute type for jxpath attributes that get their value with JXPathContext.iteratePointers(String jxpath) */
  JXPATH_ITERATE_POINTERS() {
    public void validate( String value, NamespaceContext namespaceContext ) {
      JXPathValidator.validate( value, namespaceContext );
    }
  },
  /** The attribute type for qname attributes. */
  QNAME() {
    public void validate( String value, NamespaceContext namespaceContext ) {
      JXPathContextUtil.validate( value, namespaceContext );
    }
  },
  /** The attribute type for literal attributes.  Currently, literal attributes can only be of type String or enum.  This will be expanded in the future. */
  LITERAL() {
    public void validate( String value, NamespaceContext namespaceContext ) {
      // TODO: Literal Validator.
    }
  },
  /** The attribute type for attribute value template attributes.  Currently, literal attributes can only be of type String or enum.  This will be expanded in the future. */
  ATTRIBUTE_VALUE_TEMPLATE() {
    public void validate( String value, NamespaceContext namespaceContext ) {
      // TODO: Attribute Value Validator.
      AttributesUtil.validateAttributeValueTemplate(value, namespaceContext);
    }
  };

  public abstract void validate( String value, NamespaceContext namespaceContext )
    throws JXPathException;
}
