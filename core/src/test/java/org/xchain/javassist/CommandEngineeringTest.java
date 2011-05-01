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
package org.xchain.javassist;

import static org.junit.Assert.assertEquals;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.Test;
import org.xchain.Command;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class CommandEngineeringTest
{
  @Test public void testExtendingExecute()
    throws Exception
  {
    Command command = getExtendingCommand("org.xchain.javassist.SimpleCommand");

    // create a context.
    JXPathContext context = JXPathContext.newContext(new Object());

    // execute the command
    command.execute(context);

    // assert that the namespace was defined while the command executed.
    assertEquals("The namespace returned was not correct.", (Object)"http://www.xchain.org/test", context.getValue("$namespace"));

    // assert that the namespace is no longer defined on the context.
    assertEquals("The namespace was still defined on the context.", (String)null, context.getNamespaceURI("test"));
  }

/*
  @Test public void testExtendingInnerExecute()
    throws Exception
  {
    Command command = getExtendingInnerCommand("org.xchain.javassist.InnerClassCommand");

    // create a context.
    JXPathContext context = JXPathContext.newContext(new Object());

    // execute the command
    CommandUtil.execute(command, context);

    // assert that the namespace was defined while the command executed.
    assertEquals("The namespace returned was not correct.", (Object)"http://www.xchain.org/test", context.getValue("$namespace"));
    assertEquals("The namespace returned was not correct.", (Object)"http://www.xchain.org/test-inner", context.getValue("$inner-namespace"));
    assertEquals("The namespace returned was not correct.", (Object)"http://www.xchain.org/test-static-inner", context.getValue("$static-inner-namespace"));

    // assert that the namespace is no longer defined on the context.
    assertEquals("The namespace was still defined on the context.", (String)null, context.getNamespaceURI("test"));
  }
*/

  @Test public void testWrappingExecute()
    throws Exception
  {
    Command command = getWrappingCommand("org.xchain.javassist.SimpleCommand");

    // create a context.
    JXPathContext context = JXPathContext.newContext(new Object());

    // execute the command
    command.execute(context);

    // assert that the namespace was defined while the command executed.
    assertEquals("The namespace returned was not correct.", (Object)"http://www.xchain.org/test", context.getValue("$namespace"));

    // assert that the namespace is no longer defined on the context.
    assertEquals("The namespace was still defined on the context.", (String)null, context.getNamespaceURI("test"));
  }

  @Test public void testWrappingInnerExecute()
    throws Exception
  {
    Command command = getWrappingInnerCommand("org.xchain.javassist.InnerClassCommand");

    // create a context.
    JXPathContext context = JXPathContext.newContext(new Object());

    // execute the command
    command.execute(context);

    // assert that the namespace was defined while the command executed.
    assertEquals("The namespace returned was not correct.", (Object)"http://www.xchain.org/test", context.getValue("$namespace"));
    assertEquals("The namespace returned was not correct.", (Object)"http://www.xchain.org/test-inner", context.getValue("$inner-namespace"));
    assertEquals("The namespace returned was not correct.", (Object)"http://www.xchain.org/test-static-inner", context.getValue("$static-inner-namespace"));

    // assert that the namespace is no longer defined on the context.
    assertEquals("The namespace was still defined on the context.", (String)null, context.getNamespaceURI("test"));
  }

  public static Command getExtendingCommand( String commandClassName )
    throws Exception
  {
    ClassPool classPool = ClassPool.getDefault();
    classPool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));

    // create a ctclass for the command.
    CtClass originalClass = classPool.get(commandClassName);
    CtClass engineeredClass = engineerExtendingCommand(classPool, originalClass, "http://www.xchain.org/test");

    // instanciate the new class and return it.
    return (Command)classPool.toClass(engineeredClass, new MyClassLoader()).newInstance();
  }

  /*
  This may not work well, since classes that are created with new will break.
    */
  public static Command getExtendingInnerCommand( String commandClassName )
    throws Exception
  {
    ClassPool classPool = ClassPool.getDefault();
    classPool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));

    // create a ctclass for the command.
    CtClass originalClass = classPool.get(commandClassName);
    CtClass originalInnerClass = classPool.get(commandClassName+"$InnerCommand");
    CtClass originalStaticInnerClass = classPool.get(commandClassName+"$StaticInnerCommand");
    CtClass engineeredClass = engineerExtendingCommand(classPool, originalClass, "http://www.xchain.org/test");
    CtClass engineeredInnerClass = engineerExtendingCommand(classPool, originalInnerClass, "http://www.xchain.org/test-inner");
    CtClass engineeredStaticInnerClass = engineerExtendingCommand(classPool, originalStaticInnerClass, "http://www.xchain.org/test-static-inner");

    ClassLoader classLoader = new MyClassLoader();

    classPool.toClass(engineeredInnerClass, classLoader);
    classPool.toClass(engineeredStaticInnerClass, classLoader);

    // instanciate the new class and return it.
    return (Command)classPool.toClass(engineeredClass, classLoader).newInstance();
  }

  public static Command getWrappingInnerCommand( String commandClassName )
    throws Exception
  {
    ClassPool classPool = ClassPool.getDefault();
    classPool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));

    // create a ctclass for the command.
    CtClass originalClass = classPool.get(commandClassName);
    CtClass originalInnerClass = classPool.get(commandClassName+"$InnerCommand");
    CtClass originalStaticInnerClass = classPool.get(commandClassName+"$StaticInnerCommand");
    CtClass engineeredClass = engineerWrappingCommand(classPool, originalClass, "http://www.xchain.org/test");
    CtClass engineeredInnerClass = engineerWrappingCommand(classPool, originalInnerClass, "http://www.xchain.org/test-inner");
    CtClass engineeredStaticInnerClass = engineerWrappingCommand(classPool, originalStaticInnerClass, "http://www.xchain.org/test-static-inner");

    ClassLoader classLoader = new MyClassLoader();

    classPool.toClass(engineeredInnerClass, classLoader);
    classPool.toClass(engineeredStaticInnerClass, classLoader);

    // instanciate the new class and return it.
    return (Command)classPool.toClass(engineeredClass, classLoader).newInstance();
  }

  public static CtClass engineerExtendingCommand( ClassPool classPool, CtClass originalClass, String namespace )
    throws Exception
  {
    CtClass engineeredClass = classPool.makeClass(originalClass.getName()+"_Engineered", originalClass);

    // modify the command, adding code to modify the namespaces.
    CtMethod newExecute = CtNewMethod.make(
      "public boolean execute( org.apache.commons.jxpath.JXPathContext context ) "+
        "throws Exception "+
      "{"+
        "String oldNamespace = context.getNamespaceURI(\"test\");"+
        "context.registerNamespace(\"test\", \""+namespace+"\");"+
        "try {"+
          "return super.execute(context);"+
        "} finally {"+
          "context.registerNamespace(\"test\", oldNamespace);"+
        "}"+
      "}", engineeredClass);
    engineeredClass.addMethod(newExecute);

    return engineeredClass;
  }

  public static Command getWrappingCommand( String commandClassName )
    throws Exception
  {
    ClassPool classPool = ClassPool.getDefault();
    classPool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
    
    // create a ctclass for the command.
    CtClass originalClass = classPool.get(commandClassName);

    CtClass engineeredClass = engineerWrappingCommand( classPool, originalClass, "http://www.xchain.org/test" );

    // instanciate the new class and return it.
    return (Command)classPool.toClass(engineeredClass, new MyClassLoader()).newInstance();
  }

  public static CtClass engineerWrappingCommand( ClassPool classPool, CtClass originalClass, String namespace )
    throws Exception
  {
    CtClass contextClass = classPool.get("org.apache.commons.jxpath.JXPathContext");
    CtMethod executeMethod = null;
    try {
      executeMethod = originalClass.getDeclaredMethod("execute", new CtClass[] { contextClass });
      executeMethod.setName("executeOriginal");
      CtMethod engineeredMethod = CtNewMethod.make(
        "public boolean execute(org.apache.commons.jxpath.JXPathContext context) throws Exception {"+
          "String oldNamespace = context.getNamespaceURI(\"test\");"+
          "context.registerNamespace(\"test\", \""+namespace+"\");"+
          "try {"+
            "return this.executeOriginal(context);"+
          "}"+
          "finally {"+
            "context.registerNamespace(\"test\", oldNamespace);"+
          "}"+
        "}", originalClass);
       originalClass.addMethod(engineeredMethod);
    }
    catch( Exception e ) {
      CtMethod engineeredMethod = CtNewMethod.make(
        "public boolean execute(org.apache.commons.jxpath.JXPathContext context) throws Exception {"+
          "String oldNamespace = context.getNamespaceURI(\"test\");"+
          "context.registerNamespace(\"test\", \"http://www.xchain.org/test\");"+
          "try {"+
            "return super.execute(context);"+
          "}"+
          "finally {"+
            "context.registerNamespace(\"test\", oldNamespace);"+
          "}"+
        "}", originalClass);
       originalClass.addMethod(engineeredMethod);
    }

    return originalClass;
  }

  public static class MyClassLoader 
    extends ClassLoader
  {
    public MyClassLoader()
    {
      super(Thread.currentThread().getContextClassLoader());
    }

    public MyClassLoader(ClassLoader parent)
    {
      super(parent);
    }
  }
}
