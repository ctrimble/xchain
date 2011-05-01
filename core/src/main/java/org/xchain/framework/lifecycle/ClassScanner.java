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

import org.xchain.framework.util.EngineeringUtil;
import org.xchain.annotations.Component;
import org.xchain.annotations.Element;
import org.xchain.annotations.Function;
import org.xchain.annotations.Namespace;
import org.xchain.annotations.Optional;
import org.xchain.framework.util.AnnotationUtil;
import org.xchain.framework.util.ComponentUtil;
import org.xchain.framework.scanner.AbstractScanner;
import org.xchain.framework.scanner.MarkerResourceLocator;
import org.xchain.framework.scanner.ScanException;
import org.xchain.framework.scanner.ScanNode;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.regex.Pattern;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.bytecode.ClassFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A scanner that searches the lifecycle context looking for commands and catalogs, creates engineered versions of those classes, and loads them into the context.
 *
 * @author Christian Trimble
 * @author Mike Moulton
 * @author Devon Tackett
 * @author John Trimble
 * @author Josh Kennedy
 */
public class ClassScanner
  extends AbstractScanner
{
  public static Logger log = LoggerFactory.getLogger(ClassScanner.class);

  protected LifecycleContext context = null;
  protected ClassPool classPool = null;
  protected CtClass catalogCtClass = null;
  protected CtClass commandCtClass = null;

  public ClassScanner( LifecycleContext context )
  {
    super( new MarkerResourceLocator("META-INF/xchain.xml"),  context.getClassLoader() );
    this.context = context;
  }

  /**
   * Starts the scanning of the lifecycle context.
   */
  public void scan()
  {
    try {
      classPool = EngineeringUtil.createClassPool(classLoader);
      catalogCtClass = classPool.get("org.xchain.Catalog");
      commandCtClass = classPool.get("org.xchain.Command");

      super.scan();
    }
    catch( Exception e ) {
      e.printStackTrace();
      log.warn("Unexpected Exception", e);
    }
    finally {
      classPool = null;
    }
  }

  @Override
  public void scanNode(ScanNode node)
    throws ScanException
  {
    try {
      // Handle all class files.
      if( isLoadableClassFile(node.getResourceName()) ) {
        // Get the name of the class.
        String className = toClassName(node);

        // Get the class from teh class pool 
        CtClass scannedCtClass = classPool.get(className);

        // Check whether the class has an Element annotation.
        if( AnnotationUtil.hasAnnotation(scannedCtClass, Element.class) || AnnotationUtil.hasAnnotation(scannedCtClass, Component.class) || hasFunctionsAnnotation(scannedCtClass) ) {

          String namespaceUri = null;

          if( AnnotationUtil.hasAnnotation(scannedCtClass, LifecycleClass.class) ) {
            namespaceUri = (String)AnnotationUtil.getAnnotationValue(scannedCtClass.getClassFile(), LifecycleClass.class, "uri");
          }
          else {
            try {
              // figure out what namespace we are in.
              String packageName = scannedCtClass.getPackageName();

              // Get the package level information for the class.
              CtClass packageCtClass = classPool.get(packageName+".package-info");
              namespaceUri = (String)AnnotationUtil.getAnnotationValue(packageCtClass.getClassFile(), Namespace.class, "uri");
            }
            catch( Exception e ) {
              if( log.isDebugEnabled() ) {
                log.debug("Could not load package-info.class for "+scannedCtClass.getName()+".");
              }
            }
          }
          if( namespaceUri != null ) {
            // Try to find the namespace context in the current context.
            NamespaceContext namespaceContext = context.getNamespaceContextMap().get(namespaceUri);

            if( namespaceUri != null && namespaceContext == null ) {
              // Namespace not found, create a new namespace context.
              namespaceContext = new NamespaceContext(namespaceUri);
              context.getNamespaceContextMap().put(namespaceUri, namespaceContext);

              if( log.isInfoEnabled() ) {
                log.info("Found xchain uri '"+namespaceUri+"'.");
              }
            }

            if( namespaceContext != null ) {
              // Check if the scanned class is optional.
              boolean isOptional = AnnotationUtil.hasAnnotation(scannedCtClass, Optional.class);
              
              try {
                if( scannedCtClass.subtypeOf(catalogCtClass) ) {
                  // The class is a catalog, engineer as such.
                  CtClass engineeredCtClass = EngineeringUtil.engineerCatalog(classPool, scannedCtClass);
                  // scan the class for static methods with Function annotations.
                  namespaceContext.getCatalogList().add(classPool.toClass(engineeredCtClass, context.getClassLoader()));
                  if( hasFunctionsAnnotation(scannedCtClass) ) {
                    Class scannedClass = Thread.currentThread().getContextClassLoader().loadClass(className);
                    scanStaticFunctions(namespaceContext, scannedClass);
                  }
                  for( CtClass nestedCtClass : engineeredCtClass.getNestedClasses() ) {
                    classPool.toClass( nestedCtClass, context.getClassLoader() );
                    // TODO: What are the class loading implications of this?
                    // scan the class for static methods with Function annotations.
                    //scanStaticFunctions(namespaceContext, nestedCtClass);
                    //scanInstanceFunctions(namespaceContext, nestedCtClass);
                  }
                }
                else if( scannedCtClass.subtypeOf(commandCtClass) ) {
                  // The class is a command, engineer as such.                
                  CtClass engineeredCtClass = EngineeringUtil.engineerCommand(classPool, scannedCtClass);
                  namespaceContext.getCommandList().add(classPool.toClass(engineeredCtClass, context.getClassLoader()));
                  // scan the class for static methods with Function annotations.
                  if( hasFunctionsAnnotation(scannedCtClass) ) {
                    Class scannedClass = Thread.currentThread().getContextClassLoader().loadClass(className);
                    scanStaticFunctions(namespaceContext, scannedClass);
                  }
                  for( CtClass nestedCtClass : engineeredCtClass.getNestedClasses() ) {
                    if( log.isDebugEnabled() ) log.debug("Adding class "+nestedCtClass.getName());
                    classPool.toClass( nestedCtClass, context.getClassLoader() );
                    // TODO: what are the class loading implications of this?
                    //scanStaticFunctions(namespaceContext, nestedCtClass);
                    //scanInstanceFunctions(namespaceContext, nestedCtClass);
                  }
                }
                else if ( AnnotationUtil.hasAnnotation(scannedCtClass, Component.class)) {
                  // The class is an component.
                  // TODO
                  namespaceContext.getComponentMap().put(AnnotationUtil.getAnnotationValue(scannedCtClass.getClassFile(), Component.class, "localName").toString(),
                      ComponentUtil.createAnalysis(Thread.currentThread().getContextClassLoader().loadClass(className)));
                  // scan the class for static and instance methods with Function annotations.
                  if( hasFunctionsAnnotation(scannedCtClass) ) {
                    Class scannedClass = Thread.currentThread().getContextClassLoader().loadClass(className);
                    scanStaticFunctions(namespaceContext, scannedClass);
                    scanInstanceFunctions(namespaceContext, scannedClass);
                  }
                }
                else {
                  // scan the class for static and instance methods with Function annotations/
                  if( hasFunctionsAnnotation(scannedCtClass) ) {
                    Class scannedClass = Thread.currentThread().getContextClassLoader().loadClass(className);
                    scanStaticFunctions(namespaceContext, scannedClass);
                    scanInstanceFunctions(namespaceContext, scannedClass);
                    scanConstructorFunctions(namespaceContext, scannedClass);
                  }
                }
              } catch (NoClassDefFoundError error) {
                if (isOptional)
                  log.info("Class {} has missing dependencies but is marked optional.", scannedCtClass.getName());
                else
                  throw error;
              }
              // load any inner classes from the source file.
            }
          }
          else {
            if( log.isWarnEnabled() ) {
              log.warn("Skipping class '"+scannedCtClass.getName()+"' because its package does not specify a namespace uri.");
            }
          }          
        }
      }
    }
    catch( Exception e ) {
      e.printStackTrace();
      log.error("Unhandled Exception", e);
    }
  }
  
  public boolean hasFunctionsAnnotation( CtClass ctClass )
    throws Exception
  {
    for( CtMethod ctMethod : ctClass.getMethods() ) {
      if( AnnotationUtil.hasAnnotation(ctMethod, org.xchain.annotations.Function.class) ) {
        return true;
      }
    }
    for( CtConstructor ctConstructor : ctClass.getConstructors() ) {
      if( AnnotationUtil.hasAnnotation(ctConstructor, org.xchain.annotations.Function.class) ) {
        return true;
      }
    }
    return false;
  }

  public Method getSingletonAccessor( Class scannedClass )
  {
    for( Method method : scannedClass.getDeclaredMethods() ) {
      //try {
        if( java.lang.reflect.Modifier.isStatic(method.getModifiers()) && AnnotationUtil.hasAnnotation(method, org.xchain.framework.lifecycle.LifecycleAccessor.class) ) {
          return method;
        }
      //}
      //catch( Exception e ) {
        //if( log.isDebugEnabled() ) {
          //log.debug("Could not scan method '"+method.toGenericString()+" in class "+scannedClass.getName()+".", e);
        //}
      //}
    }
    return null;
  }

  public void scanStaticFunctions( NamespaceContext namespaceContext, Class scannedClass )
  {
    NamespaceFunctionLibrary functionLibrary = namespaceContext.getFunctionLibrary();
    // iterate all of the static methods in this class.
    for( Method method : scannedClass.getDeclaredMethods() ) {
      org.xchain.annotations.Function functionAnnotation = method.getAnnotation(org.xchain.annotations.Function.class);
      if( functionAnnotation != null && java.lang.reflect.Modifier.isStatic(method.getModifiers()) ) {

        // get the value of the local name.
        String localName = functionAnnotation.localName();

        functionLibrary.addStaticFunction(localName, scannedClass, method.getName());
      }
      else if( java.lang.reflect.Modifier.isStatic(method.getModifiers()) ) {
        if( log.isDebugEnabled() ) {
          log.debug("Skipping method '"+method.toString()+"' because it does not have a Function annotation.");
        }
      }
    }
  }

  public void scanInstanceFunctions( NamespaceContext namespaceContext, Class scannedClass )
  {
    NamespaceFunctionLibrary functionLibrary = namespaceContext.getFunctionLibrary();

    // get the singleton accessor if it is defined.
    Method singletonAccessor = getSingletonAccessor(scannedClass);

    for( Method method : scannedClass.getDeclaredMethods() ) {
      org.xchain.annotations.Function functionAnnotation = method.getAnnotation(org.xchain.annotations.Function.class);
      if( functionAnnotation != null && !java.lang.reflect.Modifier.isStatic(method.getModifiers()) ) {

        // get the value of the local name.
        String localName = functionAnnotation.localName();

        if( singletonAccessor != null ) {
          functionLibrary.addSingletonInstanceFunction(localName, scannedClass, method.getName(), singletonAccessor);
        }
        else {
          functionLibrary.addInstanceFunction(localName, scannedClass, method.getName());
        }
      }
      else if( !java.lang.reflect.Modifier.isStatic(method.getModifiers()) ) {
        if( log.isDebugEnabled() ) {
          log.debug("Skipping method '"+method.toString()+"' because it does not have a Function annotation.");
        }
      }
    }
  }

  public void scanConstructorFunctions( NamespaceContext namespaceContext, Class scannedClass )
  {
    NamespaceFunctionLibrary functionLibrary = namespaceContext.getFunctionLibrary();

    for( Constructor constructor : scannedClass.getConstructors() ) {
      // Note: This cast should not be necessary, but Eclipse complains without it.
      org.xchain.annotations.Function functionAnnotation = (Function) constructor.getAnnotation(org.xchain.annotations.Function.class);
      if( functionAnnotation != null ) {

        // get the value of the local name.
        String localName = functionAnnotation.localName();

        functionLibrary.addConstructorFunction(localName, scannedClass);
      }
      else {
        if( log.isDebugEnabled() ) {
          log.debug("Skipping a constructor for '"+scannedClass.getName()+"' because it does not have a Function annotation.");
        }
      }
    }
  }
}

