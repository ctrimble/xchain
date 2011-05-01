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

import org.xchain.framework.util.EngineeringUtil;
import org.xchain.framework.scanner.AbstractScanner;
import org.xchain.framework.scanner.MarkerResourceLocator;
import org.xchain.framework.scanner.ScanException;
import org.xchain.framework.scanner.ScanNode;
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
 * This scanner scans the context class loader for lifecycle steps and resolves their dependencies.  After the scan() method has been invoked, a call to
 * getLifecycleStepList() will return all of the lifecycles steps in their proper order.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 * @author John Trimble
 * @author Josh Kennedy
 */
public class LifecycleStepScanner
  extends AbstractScanner
{
  public static Logger log = LoggerFactory.getLogger(LifecycleStepScanner.class);

  protected static final Pattern NAMESPACE_DEFINITION_MAPPING_PATTERN = Pattern.compile("xmlns[:]([\\w-.]+)=['\"]([^'\"]+)['\"]");
  protected static final String VALID_STEP_METHOD_SIGNATURES = "Lifecycle step methods must have one of the following parameter signatures: (), (LifecycleContext), (ConfigDocumentContext), (LifecycleContext, ConfigDocumentContext).";
  
  protected List<LifecycleStep> lifecycleStepList = null;
  protected Map<QName, LifecycleStep> lifecycleStepMap = null;
  protected LifecycleContext context = null;
  protected ClassPool classPool = null;
  protected CtClass lifecycleStepCtClass = null;
  protected DependencySorter<QName> dependencySorter = null;

  public LifecycleStepScanner( LifecycleContext context )
  {
    super( new MarkerResourceLocator("META-INF/xchain.xml"),  Thread.currentThread().getContextClassLoader() );
    this.context = context;
  }

  /**
   * Scans the context class loader for lifecycle steps and resolves their dependencies.  After this method is called, a call to getLifecycleStepList
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
      lifecycleStepList = new ArrayList<LifecycleStep>();
      lifecycleStepMap = new HashMap<QName, LifecycleStep>();
      classPool = EngineeringUtil.createClassPool(classLoader);
      lifecycleStepCtClass = classPool.get("org.xchain.framework.lifecycle.LifecycleStep");

      super.scan();

      try {
        List<QName> lifecycleStepQNameList = dependencySorter.sort();
        for( QName lifecycleStepQName : lifecycleStepQNameList ) {
          LifecycleStep lifecycleStep = lifecycleStepMap.get(lifecycleStepQName);
          if( lifecycleStep != null ) {
            lifecycleStepList.add(lifecycleStep);
            //try {
              //lifecycleStepList.add(lifecycleStepClass.newInstance());
            //}
            //catch( Exception e ) {
              //throw new ScanException("Could not create instance of lifecycle step '"+lifecycleStepClass.getName()+"'.", e);
            //}
          }
        }
      }
      catch( DependencyCycleException dce ) {
        throw new ScanException("There is at least one cycle in the lifecycle step dependencies.", dce);
      }
    }
    catch( NotFoundException nfe ) {
      throw new ScanException("The definition of a required class could not be found.", nfe);
    }
    finally {
      dependencySorter = null;
      classPool = null;
      lifecycleStepCtClass = null;
    }
  }

  /**
   * @return A list of all loaded LifecycleSteps.  Only valid after scan() has run.
   * 
   * @see #scan()
   */
  public List<LifecycleStep> getLifecycleStepList()
  {
    return lifecycleStepList;
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
            boolean isStartStep = hasAnnotation(method, StartStep.class);
            boolean isStopStep = hasAnnotation(method, StopStep.class);
            boolean isStatic = Modifier.isStatic(method.getModifiers());
            boolean isConfigStep = isDocumentConfigStep(method);

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
              StartStep startStep = method.getAnnotation(StartStep.class);
              localName = startStep.localName();
              qName = new QName(namespaceUri, localName);
              beforeSet = toQNameSet(startStep.before(), namespaceUri);
              afterSet = toQNameSet(startStep.after(), namespaceUri);
            }
            else if( isStopStep ) {
              StopStep stopStep = method.getAnnotation(StopStep.class);
              localName = stopStep.localName();
              qName = new QName(namespaceUri, localName);
              afterSet = toQNameSet(stopStep.before(), namespaceUri);
              beforeSet = toQNameSet(stopStep.after(), namespaceUri);
            }
            
            // add implicit dependency for steps that need configuration information--this will insure those steps
            // run only after the step that creates the configuration information they need.
            if( isStartStep && isConfigStep ) 
              afterSet.add(QName.valueOf("{http://www.xchain.org/framework/lifecycle}create-config-document-context"));

            // ASSERT: the qName, beforeSet, and afterSet have all been set from the perspective of a start step.
            
            Map<String, String> prefixMappings = null;
            if( isStartStep ) {
              // get the start step annotation.
              StartStep startStep = method.getAnnotation(StartStep.class);
              prefixMappings = buildPrefixMappings(startStep.xmlns(), new HashMap<String, String>());
            }
            
            // ASSERT: prefix mappings set for the start step.

            LifecycleStep step = lifecycleStepMap.get(qName);
            if( step == null ) {
              step = new AnnotationLifecycleStep(qName);
            }

            AnnotationLifecycleStep annotationStep = (AnnotationLifecycleStep)step;

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
              annotationStep.setStartMethodPrefixMappings(prefixMappings);
            }
            else if (isStopStep) {
              annotationStep.setStopMethod(method);
            }

            // add the lifecycle step to the map.
            lifecycleStepMap.put(qName, annotationStep);

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
  
  private static Map<String, String> buildPrefixMappings(String[] prefixDefinitions, Map<String, String> prefixUriMap) throws ScanException {
    for( String prefixDefinition : prefixDefinitions ) {
      Matcher matcher = NAMESPACE_DEFINITION_MAPPING_PATTERN.matcher(prefixDefinition); 
      if( !matcher.matches() ) {
        throw new ScanException("Invalid prefix definition: "+prefixDefinition);
      }
      String prefix = matcher.group(1);
      String uri = matcher.group(2);
      prefixUriMap.put(prefix, uri);
    }
    return prefixUriMap;
  }
  
  private static void assertProperStepMethodSignature(Class<?> lifecycleClass, Method method) throws ScanException {
    // check the parameters on the method, to make sure it is a valid method signature.
    Class<?>[] parameterTypes = method.getParameterTypes();
    
    // make sure that the parameter list is valid.  If it isn't, tell the user about the problem.
    if( parameterTypes.length == 1 && (parameterTypes[0] != LifecycleContext.class && parameterTypes[0] != ConfigDocumentContext.class) ) {
      throw new ScanException("The class '"+lifecycleClass+"' has a lifecycle method '"+method.getName()+"' that has an illegal signature."+
                              " "+VALID_STEP_METHOD_SIGNATURES);
    }
    if( parameterTypes.length == 2 && (parameterTypes[0] != LifecycleContext.class || parameterTypes[1] != ConfigDocumentContext.class) ) {
      throw new ScanException("The class '"+lifecycleClass+"' has a lifecycle method '"+method.getName()+"' that has an illegal signature."+
          " "+VALID_STEP_METHOD_SIGNATURES);
    }
    if( parameterTypes.length > 2 ) {
      throw new ScanException("The class '"+lifecycleClass+"' has a lifecycle method '"+method.getName()+"' that has an illegal signature."+
          " "+VALID_STEP_METHOD_SIGNATURES);
    }
  }
  
  private static boolean isDocumentConfigStep(Method stepMethod) {
    for( Class<?> type : stepMethod.getParameterTypes() ) {
      if( type.equals(ConfigDocumentContext.class) )
        return true;
    }
    return false;
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
