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

import static org.xchain.framework.util.AnnotationUtil.hasAnnotation;

import org.xchain.framework.scanner.AbstractScanner;
import org.xchain.framework.scanner.MarkerResourceLocator;
import org.xchain.framework.scanner.ScanException;
import org.xchain.framework.scanner.ScanNode;
import org.xchain.framework.util.EngineeringUtil;
import org.xchain.framework.util.DependencySorter;
import org.xchain.framework.util.DependencyCycleException;
import org.xchain.framework.util.LexicographicQNameComparator;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 * @author John Trimble
 * @author Josh Kennedy
 */
public class ThreadStepScanner
  extends AbstractScanner
{
  public static Logger log = LoggerFactory.getLogger(ThreadStepScanner.class);

  protected static final Pattern NAMESPACE_DEFINITION_MAPPING_PATTERN = Pattern.compile("xmlns[:]([\\w-.]+)=['\"]([^'\"]+)['\"]");
  protected static final String VALID_STEP_METHOD_SIGNATURES = "Thread step methods must have one of the following parameter signatures: (), (LifecycleContext).";
  
  protected List<ThreadStep> threadStepList = null;
  protected Map<QName, ThreadStep> threadStepMap = null;
  protected LifecycleContext context = null;
  protected ClassPool classPool = null;
  protected CtClass threadStepCtClass = null;
  protected DependencySorter<QName> dependencySorter = null;

  public ThreadStepScanner( LifecycleContext context )
  {
    super( new MarkerResourceLocator("META-INF/xchain.xml"),  Thread.currentThread().getContextClassLoader() );
    this.context = context;
  }

  /**
   * Scans the context class loader for lifecycle steps and resolves their dependencies.  After this method is called, a call to getThreadStepList
   * will return all of the lifecycle steps in their proper order.
   *
   * @throws ScanException this exception is thrown if there is a cyclic dependency among the lifecycle steps or if there is a problem creating an instance
   * of one of the lifecycle steps.
   */
  public void scan()
    throws ScanException
  {
    try {
      dependencySorter = new DependencySorter<QName>(new LexicographicQNameComparator());
      threadStepList = new ArrayList<ThreadStep>();
      threadStepMap = new HashMap<QName, ThreadStep>();
      classPool = EngineeringUtil.createClassPool(classLoader);
      threadStepCtClass = classPool.get("org.xchain.framework.lifecycle.ThreadStep");

      super.scan();

      try {
        List<QName> threadStepQNameList = dependencySorter.sort();
        for( QName threadStepQName : threadStepQNameList ) {
          ThreadStep threadStep = threadStepMap.get(threadStepQName);
          if( threadStep != null ) {
            threadStepList.add(threadStep);
          }
        }
      }
      catch( DependencyCycleException dce ) {
        throw new ScanException("There is at least one cycle in the thread step dependencies.", dce);
      }
    }
    catch( NotFoundException nfe ) {
      throw new ScanException("The definition of a required class could not be found.", nfe);
    }
    catch( Exception e ) {
      throw new ScanException("An unknown exception was thrown from the thread step scanner.", e);
    }
    finally {
      dependencySorter = null;
      classPool = null;
      threadStepCtClass = null;
    }
  }

  /**
   * @return A list of all loaded ThreadSteps.  Only valid after scan() has run.
   * 
   * @see #scan()
   */
  public List<ThreadStep> getThreadStepList()
  {
    return threadStepList;
  }

  public void scanNode( ScanNode node )
    throws ScanException
  {
    try {
      if( isLoadableClassFile(node.getResourceName()) ) {
        String className = toClassName(node);

        CtClass scannedCtClass = null;
        boolean requiresAnnotationScanning = false;

        try {
          scannedCtClass = classPool.get(className);
          requiresAnnotationScanning = hasAnnotation(scannedCtClass, LifecycleClass.class);
        }
        catch( Exception e ) {
          if( log.isDebugEnabled() ) {
            log.debug("Could not scan file '"+node.getResourceName()+"' due to an exception.", e);
          }
          return;
        }
        if( requiresAnnotationScanning ) {
          // get the actual class.
          Class lifecycleClass = context.getClassLoader().loadClass(className);

          // get the lifecycle annotation.
          LifecycleClass lifecycleAnnotation = (LifecycleClass)lifecycleClass.getAnnotation(LifecycleClass.class);
          
          // get the uri for the annotations found in the class.
          String namespaceUri = lifecycleAnnotation.uri();

          // scan the lifecycle class for a lifecycle accessor method.
          Method accessorMethod = null;
          int accessorMethodCount = 0;
          for( Method method : lifecycleClass.getDeclaredMethods() ) {
            if( Modifier.isStatic(method.getModifiers()) && hasAnnotation(method, LifecycleAccessor.class) ) {
              accessorMethod = method;
              accessorMethodCount++;
            }
          }

          if( accessorMethodCount > 1 ) {
            throw new ScanException("The class '"+lifecycleClass+"' has "+accessorMethodCount+" static methods that are annotated with the LifecycleAccessor annotation."+
                     " Lifecycle classes should have at most one accessor method.");
          }

          // scan the class for for start and stop steps.
          for( Method method : lifecycleClass.getMethods() ) {
            boolean isStartStep = hasAnnotation(method, StartThreadStep.class);
            boolean isStopStep = hasAnnotation(method, StopThreadStep.class);
            boolean isStatic = Modifier.isStatic(method.getModifiers());

            if( isStartStep && isStopStep ) {
              throw new ScanException("The class '"+lifecycleClass+"' has a lifecycle method '"+method.getName()+"' that has both a StartStep and a StopStep annotations.");
            }

            // if this is not a start or stop step, then move on.
            if( !isStartStep && !isStopStep ) {
              continue;
            }
            
            // ASSERT: The method has a single step annotation.

            assertProperStepMethodSignature(lifecycleClass, method);

            // ASSERT: The method has the proper signature.

            String localName = null;
            QName qName = null;
            Set<QName> beforeSet = null;
            Set<QName> afterSet = null;

            if( isStartStep ) {
              // get the start step annotation.
              StartThreadStep startStep = method.getAnnotation(StartThreadStep.class);
              localName = startStep.localName();
              qName = new QName(namespaceUri, localName);
              beforeSet = toQNameSet(startStep.before(), namespaceUri);
              afterSet = toQNameSet(startStep.after(), namespaceUri);
            }
            else if( isStopStep ) {
              StopThreadStep stopStep = method.getAnnotation(StopThreadStep.class);
              localName = stopStep.localName();
              qName = new QName(namespaceUri, localName);
              afterSet = toQNameSet(stopStep.before(), namespaceUri);
              beforeSet = toQNameSet(stopStep.after(), namespaceUri);
            }
            
            // ASSERT: the qName, beforeSet, and afterSet have all been set from the perspective of a start step.
            
            if( isStartStep ) {
              // get the start step annotation.
              StartThreadStep startStep = method.getAnnotation(StartThreadStep.class);
            }
            
            // ASSERT: prefix mappings set for the start step.

            ThreadStep step = threadStepMap.get(qName);
            if( step == null ) {
              step = new AnnotationThreadStep(qName);
            }

            AnnotationThreadStep annotationStep = (AnnotationThreadStep)step;

            // set the accessor method on the annotation step.
            annotationStep.setLifecycleAccessor(accessorMethod);

            // make sure that the annotation step does not already have a step defined.
            if( isStartStep && annotationStep.getStartMethod() != null ) {
              throw new ScanException("The class '"+lifecycleClass+"' has more than one start step for QName '"+qName+"'.");
            }
            else if( isStopStep && annotationStep.getStopMethod() != null ) {
              throw new ScanException("The class '"+lifecycleClass+"' has more than one stop step for QName '"+qName+"'.");
            }

            // ASSERT: There is not a duplicate start or stop step for this qName.

            if( isStartStep ) {
              annotationStep.setStartMethod(method);
            }
            else if (isStopStep) {
              annotationStep.setStopMethod(method);
            }

            // add the lifecycle step to the map.
            threadStepMap.put(qName, annotationStep);

            // add information about the step to the sorter.
            dependencySorter.add(qName);
            for( QName before : beforeSet ) {
              dependencySorter.addDependency(qName, before);
            }
            for( QName after : afterSet ) {
              dependencySorter.addDependency(after, qName);
            }
          }
           
        }
      }
    }
    catch( Exception e ) {
      throw new ScanException("Failed to scan file '"+node.getResourceName()+"' due to an exception.", e);
    }
  }
  
  private static void assertProperStepMethodSignature(Class<?> lifecycleClass, Method method) throws ScanException {
    // check the parameters on the method, to make sure it is a valid method signature.
    Class<?>[] parameterTypes = method.getParameterTypes();
    
    // make sure that the parameter list is valid.  If it isn't, tell the user about the problem.
    if( parameterTypes.length == 1 && (LifecycleContext.class.isAssignableFrom(parameterTypes[0])) ) {
      throw new ScanException("The class '"+lifecycleClass+"' has a thread lifecycle method '"+method.getName()+"' that has an illegal signature."+
                              " "+VALID_STEP_METHOD_SIGNATURES);
    }
    if( parameterTypes.length > 1 ) {
      throw new ScanException("The class '"+lifecycleClass+"' has a thread lifecycle method '"+method.getName()+"' that has an illegal signature."+
          " "+VALID_STEP_METHOD_SIGNATURES);
    }
  }
  
  private static Set<QName> toQNameSet( String[] qNameArray, String defaultUri )
    throws ScanException
  {
    Set<QName> qNameSet = new HashSet<QName>();
    for( String qNameString : qNameArray ) {
      QName qName = null;
      if( qNameString.startsWith("{") ) {
        qName = QName.valueOf(qNameString);
      }
      else if( qNameString.matches("[A-Za-z][-A-Za-z0-9._]*") ) {
        qName = new QName( defaultUri, qNameString );
      }
      else {
        throw new ScanException("The qname string '"+qNameString+"' does not appear to be a valid qname or local name.");
      }
      qNameSet.add(qName);
    }
    return qNameSet;
  }
}
