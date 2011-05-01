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
package org.xchain.framework.jxpath;

import org.apache.commons.jxpath.PackageFunctions;
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.Functions;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.JXPathException;
import org.xchain.framework.jxpath.GenericsWisePackageFunctions;
import org.xchain.framework.lifecycle.Lifecycle;

/**
 * An implementation of JXPathContext that installs an instance of scoped QName variables into the
 * context.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 * @author John Trimble
 */
public class ScopedJXPathContextImpl
  extends JXPathContextReferenceImpl
{
  private static final PackageFunctions GENERIC_FUNCTIONS = new GenericsWisePackageFunctions("", null);
  private Scope scope;
  
  public ScopedJXPathContextImpl(JXPathContext parentContext, Object contextBean, Scope scope)
  {
    super(parentContext, contextBean);
    this.scope = scope;
    // Create the variables with a reference to a possible parent context.
    setVariables(createQNameVariables(parentContext));
    
    // If present, use the namespaceResolver from the parentContext.
    if (parentContext != null)
      namespaceResolver = ((JXPathContextReferenceImpl)parentContext).getNamespaceResolver();

    setFunctions(createFunctions(parentContext));
    setLenient(true);
  }

  /**
   * Creates a new ScopedJXPathContextImpl.
   */
  public ScopedJXPathContextImpl( JXPathContext parentContext, Object contextBean, Pointer contextPointer, Scope scope)
  {
    super( parentContext, contextBean, contextPointer );
    this.scope = scope;
    // Create the variables with a reference to a possible parent context.
    setVariables(createQNameVariables(parentContext));
    
    // If present, use the namespaceResolver from the parentContext.
    if (parentContext != null)
      namespaceResolver = ((JXPathContextReferenceImpl)parentContext).getNamespaceResolver();

    setFunctions(createFunctions(parentContext));
    setLenient(true);
  }

  public JXPathContext getRelativeContext(Pointer pointer)
  {
    Object contextBean = pointer.getNode();
    if (contextBean == null) {
      throw new JXPathException("Cannot create a relative context for a non-existent node: "+pointer);
    }
    return new ScopedJXPathContextImpl(this, contextBean, pointer, scope);
  }

  /**
   * Creates the proper qName variables for this parent context.  If the given parentContext is
   * a LocalJXPathContext then the QNameVariables will be shared with the parentContext.  If the
   * given parentContext is not a LocalJXPathContext then the QNameVariables will be able to reference
   * variables in the parent context but values in the create QNameVariables will not be available to
   * the parent context.
   * 
   * @param parentContext The parent context to build from.
   * 
   * @return The ScopedQNameVariables for this JXPathContext.
   */
  private ScopedQNameVariables createQNameVariables( JXPathContext parentContext )
  {
    ScopedQNameVariables variables = null;

    if (parentContext != null) {
      // Create a new instance of the ScopedQNameVariables with a reference to the parent context's variables.
      variables = new ScopedQNameVariablesImpl(this, ((ScopedQNameVariables)parentContext.getVariables()), scope);
    } else {
      // Create a new instance of the ScopedQNameVariables with no reference to the parent context.
      variables = new ScopedQNameVariablesImpl(this, null, scope);
    }
    return variables;
  }

  private Functions createFunctions( JXPathContext parent )
  {
    if( parent == null && Lifecycle.getLifecycleContext() != null ) {
      FunctionLibrary library = new NamespaceResolvingFunctionLibrary(namespaceResolver);
      library.addFunctions(Lifecycle.getLifecycleContext().getFunctionLibrary());
      return library;
    }
    else if( parent != null ) {
      return parent.getFunctions();
    }
    else {
      return getFunctions();
    }
  }

  /**
   * <p>This method corrects casting behavior for the Java 5 Enum type.</p>
   *
   * @param expression the expression to evaluate.
   * @param type the type of object to return.
   * @return the value returned after evaluating the expression.
   */
  /*
  public Object getValue( String jxpath, Class type )
  {
    if( Enum.class.isAssignableFrom(type) ) {
      Object value = super.getValue( jxpath, Object.class );
      if( value == null ) {
        return null;
      }
      else if( type.isAssignableFrom(value.getClass()) ) {
        return value;
      }
      else {
        return Enum.valueOf( type, value.toString() );
      }
    }
    else {
      return super.getValue( jxpath, type );
    }
  }
  */
  
  /**
   * Release all components generated in this context.
   */
  public void releaseComponents() {
    ScopedQNameVariablesImpl vars = (ScopedQNameVariablesImpl)getVariables();
    
    vars.releaseComponents();
  }  
  
  public Scope getScope() {
    return scope;
  }
  
  /** 
   * Overridden to insure an instance of <code>GenericsWisePackageFunctions</code> is returned by
   * default, when a Functions instance hasn't been set explicitly, instead of a
   * <code>PackageFunctions</code> instance. 
   */
  @Override
  public Functions getFunctions() {
    if( this.functions != null )
      return this.functions;
    return ScopedJXPathContextImpl.GENERIC_FUNCTIONS;
  }
}
