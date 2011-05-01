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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;

import org.xchain.EngineeredCommand;
import org.xchain.Locatable;
import org.xchain.Registerable;
import org.xchain.Command;
import org.xchain.Filter;
import org.xchain.framework.jxpath.Scope;
import org.xchain.framework.jxpath.ScopedJXPathContextImpl;
import org.xchain.framework.util.ThreadLocalStack;

import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

/**
 * The Execution class manages the context and execution stacks.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
public final class Execution
{
  private static Logger log = LoggerFactory.getLogger(Execution.class);

  /** A thread local stack of the commands being executed. */
  private static ThreadLocalStack<ExecutionContext> executionContextStack = new ThreadLocalStack<ExecutionContext>();

  /** A thread local stack of the commands currently suspended. */
  private static ThreadLocalStack<ExecutionContext> suspendedExecutionContextStack = new ThreadLocalStack<ExecutionContext>();

  /** A thread local of the current execution level context. */
  private static ThreadLocal<JXPathContext> executionContextTl = new ThreadLocal<JXPathContext>();

  /** A thread local stack of execution trace elements. */
  private static ThreadLocalStack<ExecutionTraceElement> executionTraceStack = new ThreadLocalStack<ExecutionTraceElement>();

  /** A thread local stack of suspended execution trace elements. */
  private static ThreadLocalStack<ExecutionTraceElement> suspendedExecutionTraceStack = new ThreadLocalStack<ExecutionTraceElement>();

  /** A thread local stack of chain level contexts. */
  private static ThreadLocalStack<JXPathContext> chainContextStack = new ThreadLocalStack<JXPathContext>();

  /** A thread local stack of suspended chain level contexts. */
  private static ThreadLocalStack<JXPathContext> suspendedChainContextStack = new ThreadLocalStack<JXPathContext>();

  /** A stack of prefixes defined for the life of a given command. */
  private static ThreadLocal<LinkedList<PrefixMappingContext>> prefixMappingStack = new ThreadLocal<LinkedList<PrefixMappingContext>>()
  {
    protected LinkedList<PrefixMappingContext> initialValue() {
      return new LinkedList<PrefixMappingContext>();
    }
  };  

  /**
   * Start an execution stack on the current thread.
   * 
   * @param globalContext The global JXPathContext for this execution.
   */
  public static void startExecution(JXPathContext globalContext)
  {
    // Wrap the incoming context in a global context
    executionContextTl.set(new ScopedJXPathContextImpl(globalContext, globalContext.getContextBean(), Scope.execution));
    executionContextStack.push(new GlobalExecutionContext());
  }

  /**
   * End all execution for this thread.  All contexts will be cleared.
   * 
   * @throws ExecutionException If an exception was thrown during execution it will be wrapped in an ExecutionException.
   */
  public static void endExecution()
    throws ExecutionException
  {
    ExecutionException executionException = null;
    // Check if an exception was encountered during execution.
    if( executionContextStack.peek().exceptionContext != null ) {
      // An exception was found.  Create a new ExecutionException with a trace to the source of the exception.
      executionException = new ExecutionException("An exception was thrown during the execution.", executionContextStack.peek().exceptionContext.trace, executionContextStack.peek().exceptionContext.exception);
    }
    // Clear all stacks.
    executionContextStack.clear();
    suspendedExecutionContextStack.clear();
    executionTraceStack.clear();
    suspendedExecutionTraceStack.clear();
    chainContextStack.clear();
    suspendedChainContextStack.clear();
    
    // End the execution context.
    endContext(executionContextTl.get());
    executionContextTl.set(null);
    if( executionException != null ) {
      // Throw the ExecutionException if one was created.
      throw executionException;
    }
  }

  /**
   * Returns a string representation of the current execution.
   */
  private static String stateString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("Execution Context Stacks:").append(executionContextStack.size()).append("/").append(suspendedExecutionContextStack.size());
    builder.append(" Execution Trace Stacks:").append(executionTraceStack.size()).append("/").append(suspendedExecutionTraceStack.size());
    builder.append(" Local Context Stacks:").append(chainContextStack.size()).append("/").append(suspendedChainContextStack.size());
    builder.append(" Global Context:").append(executionContextTl.get()!=null);
    return builder.toString();
  }

  private static String detailedStateString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("Execution Context Stack:\n");
    for( ExecutionContext executionContext : executionContextStack.toList() ) {
      builder.append("  ").append(executionContext.toString()).append("\n");
    }
    builder.append("Suspended Execution Context Stack:\n");
    for( ExecutionContext executionContext : suspendedExecutionContextStack.toList() ) {
      builder.append("  ").append(executionContext.toString()).append("\n");
    }
    return builder.toString();
  }

  /**
   * Returns true if a command is currently executing.
   */
  public static boolean inExecution()
  {
    return executionContextStack.size() > 0;
  }
  
  /**
   * <p>Signals that the execute method of a command has been called.  If this command is registered with a catalog, then
   * a new local context is created based on the current context that was passed in.</p>
   *
   * <p>NOTE: This command is used by the engineering framework and should not be called directly by
   * command implementations.</p>
   *
   * @param command the command that is being executed.
   * @param context the current local context or the global context.
   * @return the current local context.  If the command is registered with a catalog, then the context returned is
   * the new local context for this command.  Otherwise, the context returned will be the same as the context passed in.
   */
  public static JXPathContext startCommandExecute( Command command, JXPathContext context )
  {
    JXPathContext localContext = null;

    // push this command onto the stack.
    executionContextStack.push(new CommandExecutionContext(command));

    if( isLocalContextBoundary(command) ) {
      // create the new local context.
      localContext = new ScopedJXPathContextImpl( executionContextTl.get(), context.getContextBean(), context.getContextPointer(), Scope.chain );

      // push the local context.
      chainContextStack.push(localContext);

      startExecutionTrace(command);
    }
    else if( !(context instanceof ScopedJXPathContextImpl ) ) {
      throw new IllegalStateException("Initial call to command that is not registered with a catalog.");
    }

    updateExecutionTraceLocation();
    
    // get the local context.
    context = getCurrentContext();
    if (command instanceof EngineeredCommand) {
      // Define prefix mappings for engineered commands.
      definePrefixMappings(context, (EngineeredCommand)command);
    }    

    // return the context.
    return context;
  }

  /**
   * Returns true if the command represents a local context boundary, false otherwise.
   *
   * @return true if the command represents a local context boundary, false otherwise.
   */
  private static boolean isLocalContextBoundary( Command command )
  {
    if( !(command instanceof Registerable ) ) {
      return false;
    }

    return ((Registerable)command).isRegistered();
  }

  /**
   * <p>Signals that we are creating a local context for context bean
   * that has the same variable and function scope as the last context
   * bean.</p>
   * 
   * <p>The user can safely call this method.</p>
   * 
   * @param context the current local context.
   * @param contextPointer A Pointer to the new context bean.
   * 
   * @return the new local context that shares variables and namespace context with the previous local context,
   * but it has a new context bean.
   */
  public static JXPathContext startContextPointer( JXPathContext context, Pointer contextPointer )
  {
    // make sure that this is the current local context.
    testCurrentLocalContext(context);

    // create the relative context.
    JXPathContext localContext = context.getRelativeContext( contextPointer );

    // push the context onto the stack.
    chainContextStack.push(localContext);

    // return the new local context.
    return localContext;
  }

  /**
   * <p>Executed at the end of a command's execute method if it is not a filter or at the end of a
   * command's postProcess method if it is a filter.  This method will change the current local
   * context if the current command is registered with a catalog.</p>
   *
   * <p>NOTE: This command is used by the engineering framework and should not be called directly by
   * command implementations.</p>
   *
   * @param context the current local context.
   * @return the previous local context on the context stack.
   */
  public static JXPathContext endCommandExecute(Command command, JXPathContext context)
  {
    if (command instanceof EngineeredCommand) {
      // Undefine prefix mappings for engineered commands.
      undefinePrefixMappings(context, (EngineeredCommand)command);
    }
    
    // make sure that we are managing the stacks correctly.
    testCurrentLocalContext(context);
    testCurrentCommand(command);

    boolean isFilter = command instanceof Filter;

    // if we are in a filter, the suspend everything.
    if( isFilter ) {
      suspendedExecutionContextStack.push(executionContextStack.pop());

      // if this is a local context boundary, then suspend the context.
      if( isLocalContextBoundary( command ) ) {
        suspendedChainContextStack.push(chainContextStack.pop());
        suspendExecutionTrace();
      }
    }
    // otherwise, this is a just a command, so pop it's state off of the stacks.
    else {
      // remove the command from the stack.
      executionContextStack.pop();

      // if this was a context boundary, then update the execution trace and the local context stack.
      if( isLocalContextBoundary( command ) ) {
        endContext(chainContextStack.pop());
        stopExecutionTrace();
      }
    }

    // update the current location of the execution trace.
    updateExecutionTraceLocation();

    // return the current context.
    return getCurrentContext();
  }

  /**
   * <p>End the current context pointer.</p>
   *
   * @param context the local context to stop.
   * @return the new local context.
   */
  public static JXPathContext stopContextPointer( JXPathContext context )
  {
    testCurrentLocalContext(context);
    endContext(chainContextStack.pop());
    return getCurrentContext();
  }

  /**
   * <p>Marks the end of a commands post process method.</p>
   * <p>NOTE: This command is used by the engineering framework and should not be called directly by
   * command implementations.</p>
   *
   * @param context the current local context.
   * @return the previous local context on the context stack.
   */
  public static JXPathContext endCommandPostProcess(Command command, JXPathContext context)
  {    
    if (command instanceof EngineeredCommand) {
      // Undefine prefix mappings for engineered commands.
      undefinePrefixMappings(context, (EngineeredCommand)command);
    }    
    
    // make sure that we are managing the stacks correctly.
    testCurrentLocalContext(context);
    testCurrentCommand(command);

    // move the command to the top of the suspended command stack.
    executionContextStack.pop();

    // otherwise, this is a just a command, so pop it's state off of the stacks.
    if( isLocalContextBoundary( command ) ) {
      endContext(chainContextStack.pop());
      stopExecutionTrace();
    }

    // update the current location of the execution trace.
    updateExecutionTraceLocation();

    // return the current context.
    return getCurrentContext();
  }

  /**
   * <p>Suspends a local context for a context pointer.</p>
   *
   * @param context the current local context.
   * @return the new local context.
   */
  public static JXPathContext suspendContextPointer(JXPathContext context)
  {
    testCurrentLocalContext(context);
    suspendedChainContextStack.push(chainContextStack.pop());
    return getCurrentContext();
  }

  /**
   * Resumes the top item on the suspended context stack.
   *
   * <p>NOTE: This command is used by the engineering framework and should not be called directly by
   * command implementations.</p>
   *
   * @param context the current local context.
   * @return the context that was resumed.
   */
  public static JXPathContext startCommandPostProcess(Command command, JXPathContext context)
  {
    try {
    // make sure that we are managing the stacks correctly.
    testCurrentLocalContext(context);

    // move the top of the suspended command stack to the top of the command stack.
    executionContextStack.push(suspendedExecutionContextStack.pop());
    testCurrentCommand(command);

    // move the top of the 
    if( isLocalContextBoundary( command ) ) {
      chainContextStack.push(suspendedChainContextStack.pop());
      resumeExecutionTrace();
    }

    // update the current location of the execution trace.
    updateExecutionTraceLocation();
    }
    catch( Exception e ) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    
    // Get the current context.
    context = getCurrentContext();
    if (command instanceof EngineeredCommand) {
      // Define prefix mappings for engineered commands.
      definePrefixMappings(context, (EngineeredCommand)command);
    }

    // return the current context.
    return context;
  }
  
  /**
   * Add all custom prefixes for the given command to the context.  PrefixMappings are stored in a stack.  The last prefix mapping that
   * is defined must be the first one undefined.  Any prefixes that conflict with the command's prefixes will have their original
   * values saved and will be restored once the prefix mapping is undefined for the given command.
   * 
   * @param context The context being used.
   * @param command The command whose custom prefixes are to be used.
   */
  public static void definePrefixMappings( JXPathContext context, EngineeredCommand command )
  {
    PrefixMappingContext prefixContext = null;
    // get the first prefix mapping.
    if( prefixMappingStack.get().size() > 0 && prefixMappingStack.get().getFirst().isContextFor( context, command ) ) {
      prefixContext = prefixMappingStack.get().getFirst();
      prefixContext.setCallCount(prefixContext.getCallCount()+1);
    }
    else {
      // create a new prefix context.
      prefixContext = new PrefixMappingContext(context, command);

      // place the prefix on the top of the prefix context stack.
      prefixMappingStack.get().addFirst(prefixContext);

      // iterate over the mappings defined in the command, storing the old values.
      for( Map.Entry<String, String> entry : command.getPrefixMap().entrySet() ) {
        prefixContext.getOriginalPrefixMap().put(entry.getKey(), context.getNamespaceURI(entry.getKey()));
      }

      // set the new values into the context.
      for( Map.Entry<String, String>entry : command.getPrefixMap().entrySet() ) {
        context.registerNamespace( entry.getKey(), entry.getValue() );
      }
    }
  }

  /**
   * Remove all custom prefixes for the given command from the context.  Prefix mappings are stored in a stack.  The
   * latest prefix mapping that has been defined must be the first one undefined.  When a prefix mapping is undefined
   * any prefixes that conflicted with the command's prefixes will have their original values restored.
   * 
   * @param context The context being used.
   * @param command The command whose custom prefixes are to be undefined.
   */
  public static void undefinePrefixMappings( JXPathContext context,  EngineeredCommand command )
  {
    // get the first item from the stack.
    PrefixMappingContext prefixContext = prefixMappingStack.get().getFirst();

    if( !prefixContext.isContextFor( context, command ) ) {
      throw new RuntimeException("CommandUtil.undefinePrefixMapping called on wrong context and command.");
    }

    // if this is a duplicate mapping call, then just decrement the call count.
    if( prefixContext.getCallCount() > 0 ) {
      prefixContext.setCallCount(prefixContext.getCallCount() - 1);
    }

    // otherwise, we need to remove the mappings and remove the context.
    else {

      // set the original values into the context.
      for( Map.Entry<String, String> entry : prefixContext.getOriginalPrefixMap().entrySet() ) {
        context.registerNamespace( entry.getKey(), entry.getValue() );
      }

      // remove the prefix mapping from the context stack.
      prefixMappingStack.get().removeFirst();

      // if the stack is empty, then remove the stack for this thread.
      if( prefixMappingStack.get().size() == 0 ) {
        prefixMappingStack.remove();
      }
    }
  }  

  /**
   * <p>This method is used to resume a suspended context pointer.</p>
   *
   * @param context the current context.
   * @return the context that was resumed.
   */
  public static JXPathContext resumeContextPointer(JXPathContext context)
  {
    testCurrentLocalContext(context);
    chainContextStack.push(suspendedChainContextStack.pop());
    return chainContextStack.peek();
  }

  /**
   * <p>Pushes an execution trace onto the top of the execution trace stack.</p>
   *
   * @param command the command to start an execution trace for.
   */
  private static void startExecutionTrace( Command command )
  {
    String systemId = null;
    QName qName = null;

    if( command instanceof Registerable ) {
      Registerable registerable = (Registerable)command;
      if( registerable.isRegistered() ) {
        systemId = registerable.getSystemId();
        qName = registerable.getQName();
      }
    }

    // add a new locator for this systemId and qName to the top of the stack.
    executionTraceStack.push(new ExecutionTraceElement(systemId, qName, null));
  }

  /**
   * <p>Removes the top execution trace from the execution trace stack.</p>
   */
  private static void stopExecutionTrace()
  {
    executionTraceStack.pop();
  }

  /**
   * <p>Moves the execution trace on the top of the execution trace stack to the top of the suspended execution
   * trace stack.</p>
   */
  private static void suspendExecutionTrace()
  {
    suspendedExecutionTraceStack.push(executionTraceStack.pop());
  }

  /**
   * <p>Moves the execution trace on the top of the suspended exection trace stack to the top of the execution trace stack.</p>
   */
  private static void resumeExecutionTrace()
  {
    executionTraceStack.push(suspendedExecutionTraceStack.pop());
  }

  /**
   * Updates the current location of the top entry of the execution trace stack.
   */
  private static void updateExecutionTraceLocation()
  {
    if( !executionTraceStack.isEmpty() ) {
    Command command = ((CommandExecutionContext)executionContextStack.peek()).command;
    Locator locator = null;

    if( command instanceof Locatable ) {
      locator = ((Locatable)command).getLocator();
    }
    else {
      LocatorImpl locatorImpl = new LocatorImpl();
      locatorImpl.setLineNumber(0);
      locatorImpl.setColumnNumber(0);
      locatorImpl.setSystemId("UNKNOWN_LOCATION");
      locator = locatorImpl;
    }

    executionTraceStack.peek().setLocator(locator);
    }
  }

  private static JXPathContext getCurrentContext()
  {
    JXPathContext currentContext = null;
    if( chainContextStack.isEmpty() ) {
      currentContext = executionContextTl.get();
    }
    else {
      currentContext = chainContextStack.peek();
    }
    
    return currentContext;
  }

  /**
   * Called by engineered commands to notify that an exception is propagating from an execute(JXPathContext) method.
   */
  public static void exceptionThrown( Command command, Exception exception )
  {
    // make sure that we are in the current command.
    testCurrentCommand(command);

    // get the parent context.
    ExecutionContext parentExecutionContext = executionContextStack.peek(1);
    ExecutionContext executionContext = executionContextStack.peek();
    ExceptionContext exceptionContext = executionContext.exceptionContext;

    // if the exception thrown is the same exception on the current node, then move it to the parent.
    if( exceptionContext != null && exceptionContext.exception == exception ) {
      parentExecutionContext.exceptionContext = exceptionContext;
    }

    // if the exception is different, but one of the filters claimed to handle it, then we need to create a new context with no cause.
    else if( exceptionContext == null || exceptionContext.handled ) {
      parentExecutionContext.exceptionContext = new ExceptionContext(exception, getExecutionTrace(), null);
    }

    // if the exception thrown is different and it was not handled, then it must be a cause.
    else {
      parentExecutionContext.exceptionContext = new ExceptionContext(exception, getExecutionTrace(), exceptionContext);
    }
  }

  public static void exceptionHandled( Command command, Exception exception )
  {
    // make sure that we are in the current command.
    testCurrentCommand(command);

    // mark the exception as handled.
    executionContextStack.peek(1).exceptionContext.handled = true;
  }

  /**
   * Returns the current local context.
   */
  public static JXPathContext getLocalContext()
  {
    return chainContextStack.peek();
  }

  /**
   * Returns the global context.
   */
  public static JXPathContext getGlobalContext()
  {
    return executionContextTl.get();
  }

  /**
   * Returns the system id for the currently executing command.
   */
  public static String getSystemId()
  {
    if( executionTraceStack.isEmpty() ) {
      throw new IllegalStateException("The getCurrentSystemId() function must only be called during an execution.");
    }
    return executionTraceStack.peek().getSystemId();
  }

  /**
   * Returns a copy of the current execution trace stack.  This list contains all of the currently executing
   * xchains starting with the current chain and anding with the first chain that was called.
   */
  public static List<ExecutionTraceElement> getExecutionTrace()
  {
    // we need clone the entries in this list, since the locations are changing.
    // we will reuse the list, since toList() creates a new list.
    List<ExecutionTraceElement> executionTrace = executionTraceStack.toList();
    for( int i = 0; i < executionTrace.size(); i++ ) {
      executionTrace.set(i, new ExecutionTraceElement(executionTrace.get(i)));
    }
    return executionTrace;
  }

  /**
   * Tests that the context passed in is the current local context.
   */
  private static void testCurrentLocalContext( JXPathContext context )
  {
    if( (chainContextStack.isEmpty() && executionContextTl.get() != context) ) {
      throw new IllegalStateException("The global context should have been passed to Execution.startCommandPostProcess().");
    }
    else if (!chainContextStack.isEmpty() && chainContextStack.peek() != context ) {
      throw new IllegalStateException("The local context passed to Execution.XXXLocalContext() was not the current local context.");
    }
  }

  /**
   * Tests that the command passed in is the current command.
   */
  private static void testCurrentCommand( Command command )
  {
    if( ((CommandExecutionContext)executionContextStack.peek()).command != command ) {
      throw new IllegalStateException("The command passed to Execution.XXXCommand() was not the current command.");
    }
  }

  /**
   * Returns true if this context represents the start of a local scope.  Returns false if the context represents a change of
   * context node inside of a local scope.
   */
  private static boolean representsLocalScopeStart( JXPathContext context )
  {
    return context.getParentContext() != null && context.getParentContext() == executionContextTl.get();
  }

  /**
   * Base ExcecutionContext.  Contains an ExceptionContext if an exception was encountered during execution.
   */
  private static abstract class ExecutionContext
  {
    public ExceptionContext exceptionContext = null;
  }

  /**
   * Execution context for commands.
   */
  private static class CommandExecutionContext
    extends ExecutionContext
  {
    /** The command being executed. */
    public Command command = null;

    public CommandExecutionContext( Command command )
    {
      this.command = command;
    }

    public String toString() { return "Command:"+command.getClass().getName()+"["+command.toString()+"]"; }
  }

  /**
   * Global execution context for any type of execution.
   */
  private static class GlobalExecutionContext
    extends ExecutionContext
  {
    public String toString() { return "Global"; }
  }

  /**
   * The ExceptionContext contains information about where an exception was encountered in an XChain.
   */
  public static class ExceptionContext
  {
    public Exception exception = null;
    public List<ExecutionTraceElement> trace = null;
    public ExceptionContext cause = null;
    public boolean handled = false;

    public ExceptionContext( Exception exception, List<ExecutionTraceElement> trace, ExceptionContext cause )
    {
      this.exception = exception;
      this.trace = trace;
      this.cause = cause;
    }
  }
  
  /**
   * This stores the command and context the prefixes were defined fore.
   */
  private static class PrefixMappingContext
  {
    // The command the custom prefixes belong to.
    private EngineeredCommand command;
    // The context the commands are executing with.
    private JXPathContext context;
    // If a command defines a prefix that already exists, the previous value will be stored in this map.
    private Map<String, String> originalPrefixMap = new HashMap<String, String>();
    private int callCount = 0;

    public PrefixMappingContext( JXPathContext context, EngineeredCommand command )
    {
      this.context = context;
      this.command = command;
    }

    public void setCommand( EngineeredCommand command ) { this.command = command; }
    public EngineeredCommand getCommand() { return this.command; }
    public void setContext( JXPathContext context ) { this.context = context; }
    public JXPathContext getContext() { return this.context; }
    public Map<String, String> getOriginalPrefixMap() { return originalPrefixMap; }
    public void setCallCount( int callCount ) { this.callCount = callCount; }
    public int getCallCount() { return callCount; }

    /**
     * Test if this PrefixMappingContext is for the given command and context.
     */
    public boolean isContextFor( JXPathContext testContext, EngineeredCommand testCommand ) {
      return this.context == testContext && this.command == testCommand;
    }
  }  
  
  private static void endContext(JXPathContext context) {
    if (context instanceof ScopedJXPathContextImpl) {
      ((ScopedJXPathContextImpl)context).releaseComponents();
    }
  }
}
