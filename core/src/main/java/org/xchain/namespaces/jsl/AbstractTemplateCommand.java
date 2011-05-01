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
package org.xchain.namespaces.jsl;

import java.util.LinkedList;

import org.xchain.Command;
import org.xchain.Filter;
import org.xchain.Locatable;
import org.xchain.Registerable;
import org.xchain.framework.lifecycle.Execution;
import org.xchain.framework.sax.CommandXmlReader;
import org.xchain.framework.sax.CommandHandler;
import org.xchain.impl.ChainImpl;
import static org.xchain.namespaces.jsl.CommandExecutionState.*;
import org.xchain.namespaces.sax.PipelineCommand;
import org.xchain.framework.jxpath.ScopedJXPathContextImpl;

import org.apache.commons.jxpath.JXPathContext;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;

import javax.xml.namespace.QName;

/**
 * The base class for generated jsl template commands.
 * 
 * @author Christian Trimble
 * @author Jason Rose
 */
public abstract class AbstractTemplateCommand
  extends ChainImpl
  implements Locatable, Registerable
{
  /** The thread local stack of command execution state arrays. */
  public static final ThreadLocal<LinkedList<CommandExecutionState[]>> commandExecutionStateStackTL = new ThreadLocal<LinkedList<CommandExecutionState[]>>();

  /** The thread local stack of element output state arrays. */
  public static final ThreadLocal<LinkedList<ElementOutputState[]>> elementOutputStateStackTL = new ThreadLocal<LinkedList<ElementOutputState[]>>();

  public static final ThreadLocal<LinkedList<QName>> dynamicElementStackTL = new ThreadLocal<LinkedList<QName>>();

  public static final ThreadLocal<SAXException> saxExceptionTl = new ThreadLocal<SAXException>();

  public static final ThreadLocal<Integer> depthTl = new ThreadLocal<Integer>();

  /**
   * Returns the command execute state array for the current thread.
   */
  protected static CommandExecutionState[] getCommandExecutionState()
  {
    LinkedList<CommandExecutionState[]> stack = commandExecutionStateStackTL.get();

    if( stack == null || stack.isEmpty() ) {
      throw new IllegalStateException("getCommandExecutionState() called outside of execute method.");
    }

    return stack.getFirst();
  }

  protected static ElementOutputState[] getElementOutputState()
  {
    LinkedList<ElementOutputState[]> stack = elementOutputStateStackTL.get();

    if( stack == null || stack.isEmpty() ) {
      throw new IllegalStateException("getElementOutputState() called outside of execute method.");
    }

    return stack.getFirst();
  }

  protected static LinkedList<QName> getDynamicElementStack()
  {
    LinkedList<QName> stack = dynamicElementStackTL.get();
    if( stack == null ) {
      throw new IllegalStateException("getDynamicElementStack() called outside of execute method.");
    }
    return stack;
  }

  protected Locator locator;
  private int elementCount;
  protected String systemId = null;
  protected QName qName = null;
  protected int templateDepth = 0;

  public AbstractTemplateCommand( int elementCount )
  {
    this.elementCount = elementCount;
  }

  public boolean isRegistered() { return qName != null && systemId != null; }
  public void setQName( QName qName ) { this.qName = qName; }
  public QName getQName() { return this.qName; }
  public void setSystemId( String systemId ) { this.systemId = systemId; }
  public String getSystemId() { return this.systemId; }

  /**
   * Pushes a new command execution state array onto the stack.  The new state array has its values initialized to PRE_EXECUTE.
   */
  private final void pushCommandExecutionState()
  {
    // create the state array.
    int childCount = getCommandList().size();
    CommandExecutionState[] state = new CommandExecutionState[childCount];
    for( int i = 0; i < childCount; i++ ) {
      state[i] = PRE_EXECUTE;
    }

    // get the stack, creating it if it is missing.
    LinkedList<CommandExecutionState[]> stack = commandExecutionStateStackTL.get();
    if( stack == null ) {
      stack = new LinkedList<CommandExecutionState[]>();
      commandExecutionStateStackTL.set(stack);
    }

    // push the state array.
    stack.addFirst(state);
  }

  /**
   * Pops the current command execution state array from the stack.
   */
  private final void popCommandExecutionState()
  {
    // get the stack for the current thread.
    LinkedList<CommandExecutionState[]> stack = commandExecutionStateStackTL.get();

    // if there was not a stack, then we are in an illegal state.
    if( stack == null ) {
      throw new IllegalStateException("popCommandExecutionState() called when there was not a current stack.");
    }

    // remove the state array from the stack.
    stack.removeFirst();

    // if the stack is now empty, clean the thread local up.
    if( stack.isEmpty() ) {
      commandExecutionStateStackTL.remove();
    }
  }

  private final void pushElementOutputState()
  {
    // create the state array.
    ElementOutputState[] state = new ElementOutputState[elementCount];
    for( int i = 0; i < elementCount; i++ ) {
      state[i] = ElementOutputState.PRE_START;
    }

    // get the stack, creating it if it is missing.
    LinkedList<ElementOutputState[]> stack = elementOutputStateStackTL.get();
    if( stack == null ) {
      stack = new LinkedList<ElementOutputState[]>();
      elementOutputStateStackTL.set(stack);
    }

    // push the state array.
    stack.addFirst(state);
  }

  private final void popElementOutputState()
  {
    // get the stack for the current thread.
    LinkedList<ElementOutputState[]> stack = elementOutputStateStackTL.get();

    // if there was not a stack, then we are in an illegal state.
    if( stack == null ) {
      throw new IllegalStateException("popElementOutputState() called when there was not a current stack.");
    }

    // remove the state array from the stack.
    stack.removeFirst();

    // if the stack is now empty, clean the thread local up.
    if( stack.isEmpty() ) {
      elementOutputStateStackTL.remove();
    }

  }

  private final void pushHandlerInfo( JXPathContext context )
  {

  }

  private final void popHandlerInfo()
  {

  }

  /**
   * Uses the specified nameXPath and namespaceXPath to create a dynamic qName.
   */
  protected QName dynamicQName( JXPathContext context, String nameXPath, String namespaceXPath, boolean includeDefaultPrefix )
    throws SAXException
  {
    String name = (String)context.getValue(nameXPath, String.class);
    if( name == null ) {
      throw new SAXException("QNames cannot have null names.");
    }
    String namespace = null;
    if( namespaceXPath != null ) {
      namespace = (String)context.getValue(namespaceXPath, String.class);
      if( namespace == null ) {
        throw new SAXException("Namespace uris cannot be null.");
      }
    }

    String[] parts = name.split(":", 2);
    String prefixPart = parts.length == 2 ? parts[0] : "";
    String localPart = parts.length == 2 ? parts[1] : parts[0];

    // if the prefix is not "", then it must be defined in the context.
    String prefixPartNamespace = (includeDefaultPrefix || !"".equals(prefixPart)) ? context.getNamespaceURI(prefixPart) : "";
    if( !"".equals(prefixPart) && prefixPartNamespace == null ) {
      throw new SAXException("The prefix '"+prefixPart+"' is not defined in the context.");
    }

    // ASSERT: if the prefix is not "", then it is defined in the context.

    // if the namespace is null, then the prefix defines the namespace.
    if( namespace == null ) {
      return new QName( prefixPartNamespace != null ? prefixPartNamespace : "", localPart, prefixPart );
    }

    // ASSERT: the namespace was specified.

    // if the prefix is "", then we can lookup the proper namespace.
    if( "".equals(prefixPart) ) {
      if( !namespace.equals(prefixPartNamespace) ) {
        String namespacePrefix = ((ScopedJXPathContextImpl)context).getPrefix(namespace);
        if( namespacePrefix == null ) {
          throw new SAXException("The namespace '"+namespace+"' is not bound to a prefix.");
        }
        if( !includeDefaultPrefix && "".equals(namespacePrefix) ) {
          throw new SAXException("The namespace '"+namespace+"' cannot be used for the attribute '"+name+"', because it maps to the default namespace.");
        }
        return new QName(namespace, localPart, namespacePrefix);
      }
      else {
        return new QName(namespace, localPart, prefixPart);
      }
    }

    // ASSERT: the prefix is defined and the namespace is defined, if they do not match, we must fail.
    if( !namespace.equals(prefixPartNamespace) ) {
      throw new SAXException("The prefix '"+prefixPart+"' is bound to '"+prefixPartNamespace+"', but the namespace '"+namespace+"' is required.");
    }

    // ASSERT: the namespace and prefix will work together.
    return new QName(namespace, localPart, prefixPart);
  }

  protected void trackStartElement( int elementIndex )
  {
    getElementOutputState()[elementIndex] = ElementOutputState.STARTED;
  }

  protected void trackEndElement( int elementIndex )
  {
    getElementOutputState()[elementIndex] = ElementOutputState.ENDED;
  }

  public boolean isElementStarted( int elementIndex )
  {
    return getElementOutputState()[elementIndex] == ElementOutputState.STARTED;
  }

  public void setLocator( Locator locator ) { this.locator = locator; }
  public Locator getLocator() { return locator; }

  /**
   * Initializes the internal state of this command and then calls executeTemplate( JXPathContext ).
   */
  public final boolean execute( JXPathContext context )
    throws Exception
  {
    boolean inExecution = Execution.inExecution();
    boolean createDynamicElementStack = dynamicElementStackTL.get() == null;
    boolean result = false;

    if( !inExecution ) {
      Execution.startExecution(context);
    }
    context = Execution.startCommandExecute(this, context);
    try {
      if( depthTl.get() == null ) {
        depthTl.set(new Integer(0));
      }
      else {
        depthTl.set(depthTl.get()+1);
      }
      if( createDynamicElementStack ) {
        dynamicElementStackTL.set(new LinkedList<QName>());
      }
      // push a new command execution state array onto the stack.
      pushCommandExecutionState();
      pushElementOutputState();

      // push the information about the current output document.
      pushHandlerInfo( context );

      result = executeTemplate( context );

      // if there was a sax exception registered and this is the outer most template, then we need to pass it up.
      if( depthTl.get().equals(new Integer(0)) && hasSaxExceptionFired() ) {
        SAXException saxException = saxExceptionTl.get();
        saxExceptionTl.remove();
        throw saxException;
      }
    }
    catch( Exception e ) {
      Execution.exceptionThrown(this, e);
      throw e;
    }
    finally {
      // pop the information about the current output document.
      popHandlerInfo();

      popElementOutputState();
      // pop the command execution state array off of the stack.
      popCommandExecutionState();

      if( createDynamicElementStack ) {
        dynamicElementStackTL.remove();
      }

      if( depthTl.get().equals(new Integer(0)) ) {
        depthTl.remove();
      }
      else {
        depthTl.set(depthTl.get()-1);
      }

      context = Execution.endCommandExecute(this, context);

      if( !inExecution ) {
        Execution.endExecution();
      }
    }

    return result;
  }

  /**
   * Generated templates implement this method to provide sax output.
   */ 
  public abstract boolean executeTemplate( JXPathContext context )
    throws Exception;

  /**
   * Calls the execute method of the children specified by childIndecies, passing it the supplied context.
   * If the execute call completes without failing, then the command execution state for that
   * child command is updated to EXECUTED.
   */
  protected final boolean executeChildren( JXPathContext context, int[] childIndecies )
    throws Exception
  {
    boolean result = false;
    for( int i = 0; !result && i < childIndecies.length; i ++ ) {
      getCommandExecutionState()[childIndecies[i]] = EXECUTED;
      result = getCommandList().get(childIndecies[i]).execute(context);
    }
    return result;
  }

  /**
   * The post process command for virtual chains.
   */
  protected final boolean virtualPostProcess( JXPathContext context, Exception exception, boolean result, int[] childIndecies )
    throws Exception
  {
    boolean handled = postProcessChildren( context, exception, childIndecies );

    if( exception != null && !handled ) {
      throw exception;
    }
    else if( hasSaxExceptionFired() ) {
      return true;
    }
    else {
      return result;
    }
  }

  /**
   * Calls post process of the specified indices.
   */
  protected final boolean postProcessChildren( JXPathContext context, Exception exception, int[] childIndecies )
  {
    boolean handled = false;

    for( int i = childIndecies.length - 1; i >= 0; i-- ) {
      if( getCommandExecutionState()[childIndecies[i]] == EXECUTED ) {
        try {
          Command command = getCommandList().get(childIndecies[i]);
          if( command instanceof Filter ) {
            handled = ((Filter)command).postProcess( context, exception ) || handled;
          }
        }
        catch( Exception e ) {
          // ignore this exception.
        }
        getCommandExecutionState()[childIndecies[i]] = POST_PROCESSED;
      }
    }

    return handled;
  }

  /**
   * Turns a QName into its prefixed string representation.
   */
  protected static final String toPrefixedQName( QName qName )
  {
    if( qName.getPrefix() == null || "".equals(qName.getPrefix()) ) {
      return qName.getLocalPart();
    }
    else {
      return qName.getPrefix() + ":" + qName.getLocalPart();
    }
  }

  protected CommandHandler getContentHandler()
  {
    return ((CommandXmlReader)PipelineCommand.getPipelineConfig().getXmlReader()).getCommandHandler();
  }

  protected boolean hasSaxExceptionFired()
  {
    return saxExceptionTl.get() != null;
  }

  protected void registerSaxException( SAXException saxException )
  {
    saxExceptionTl.set(saxException);
  }
}
