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
package org.xchain.namespaces.core;

import org.apache.commons.jxpath.JXPathContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xchain.Command;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.impl.ChainImpl;

import org.xchain.framework.jxpath.Scope;
import org.xchain.framework.jxpath.ScopedQNameVariables;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * The <code>iterate</code> command will execute its child commands for each element in the object selected by the <code>select</code> attribute.
 *
 * <code class="source">
 * &lt;xchain:iterate xmlns:xchain="http://www.xchain.org/core/1.0" select="/some/xpath" variable="name"&gt;
 *   ...
 * &lt;/xchain:iterate&gt;
 * </code>
 *
 * @author Christian Trimble
 * @author Josh Kennedy
 */
@Element(localName="iterate")
public abstract class IterateCommand
  extends ChainImpl
{
  private static Logger log = LoggerFactory.getLogger(IterateCommand.class);

  /**
   * The QName of the variable.
   */
  @Attribute(localName="variable", type=AttributeType.QNAME)
  public abstract QName getName( JXPathContext context );
  public abstract boolean hasName();

  /**
   * The JXPath of the java.util.Iterator object.  This method is provided for backwards compatiblity, the
   * select attribute should be used instead.
   */
  @Attribute(localName="iterator", type=AttributeType.JXPATH_VALUE)
  public abstract Iterator getIterator( JXPathContext context );
  public abstract boolean hasIterator();

  /**
   * The JXPath of the object to iterate.  The object can be of type java.util.Iterator, java.util.Enumeration,
   * java.util.Collection, java.util.Map, or an array.
   */
  @Attribute(localName="select", type=AttributeType.JXPATH_VALUE)
  public abstract Object getSelect( JXPathContext context );
  public abstract boolean hasSelect();

  /**
   * The scope of the variable.  Can either be the literal global or local. 
   */
  @Attribute(localName="scope", type=AttributeType.LITERAL, defaultValue="request")
  public abstract Scope getScope(JXPathContext context);

  public boolean execute( JXPathContext context )
    throws Exception
  {
    boolean result = false;
    try {
    if( log.isDebugEnabled() ) {
      log.debug("Iterate command called. Has iterator:"+hasIterator()+" Has select:"+hasSelect());
    }

    Iterator iterator = null;
    Enumeration enumeration = null;
    Object[] objectArray = null;
    int index = -1;
    QName variableName = getName(context);

    if( log.isDebugEnabled() ) {
      log.debug("Iterate variable:"+variableName);
    }

    // get the scope.
    Scope scope = getScope(context);

    ScopedQNameVariables variables = (ScopedQNameVariables)context.getVariables();

    if( log.isDebugEnabled() ) {
      log.debug("Starting iteration process.");
    }

    if( hasIterator() ) {
      iterator = getIterator(context);
    }
    else if( hasSelect() ) {
      Object object = getSelect(context);
      if( object == null ) {
        if( log.isDebugEnabled() ) {
          log.debug("The selected iteration object is null.");
        }
      }
      else if( object instanceof Iterator ) {
        if( log.isDebugEnabled() ) {
          log.debug("Iterator selected.");
        }
        iterator = (Iterator)object;
      }
      else if( object instanceof Enumeration ) {
        if( log.isDebugEnabled() ) {
          log.debug("Enumeration selected.");
        }
        enumeration = (Enumeration)object;
      }
      else if( object instanceof Collection ) {
        if( log.isDebugEnabled() ) {
          log.debug("Collection selected.");
        }
        iterator = ((Collection)object).iterator();
      }
      else if( object instanceof Map ) {
        if( log.isDebugEnabled() ) {
          log.debug("Map selected.");
        }
        iterator = ((Map)object).entrySet().iterator();
      }
      else if( object instanceof Object[] ) {
        objectArray = (Object[])object;
        index = 0;
      }
      else {
        if( log.isDebugEnabled() ) {
          log.debug("The selected iteration object is not a supported type: "+object.getClass().getName());
        }
      }
    }

    if( iterator != null || enumeration != null || objectArray != null ) {
    // if the variable has a value, then get it.
    while( ((iterator != null && iterator.hasNext()) || (enumeration != null && enumeration.hasMoreElements()) || (objectArray != null && index < objectArray.length)) && !result ) {
      Object value = null;
      if( iterator != null ) {
        value = iterator.next();
      }
      else if( enumeration != null ) {
        value = enumeration.nextElement();
      }
      else {
        value = objectArray[index++];
      }

      if( log.isDebugEnabled() ) {
        log.debug("Setting variable name '"+variableName+"' to value '"+value+"' in scope '"+scope+"'.");
      }

      variables.declareVariable( variableName, value, scope );
      result = super.execute(context);
    }
    }
    else {
      if( log.isDebugEnabled() ) {
        log.debug("Skipping iteration due to a null iterator.");
      }
    }

    if( log.isDebugEnabled() ) {
      log.debug("Iteration complete.");
    }

    }
    catch( Exception e ) {
      if( log.isDebugEnabled() ) {
        log.debug("Could not iterate due to exception.", e);
      }
      throw e;
    }
    catch( Error err ) {
      if( log.isDebugEnabled() ) {
        log.debug("Could not iterate due to exception.", err);
      }
      throw err;
    }

    // return false and allow other chains to execute.
    return result;
  }
}
