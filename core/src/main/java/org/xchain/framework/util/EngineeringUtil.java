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
package org.xchain.framework.util;

import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javassist.Modifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Matcher;

import javax.xml.namespace.QName;

import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.PrefixMapping;
import static org.xchain.framework.util.AnnotationUtil.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to build engineered versions of commands and catalogs.
 * 
 * TODO An explanation of why we're engineering classes.
 *
 * @author Christian Trimble
 * @author Mike Moulton
 * @author Devon Tackett
 * @author Josh Kennedy
 */
public class EngineeringUtil
{
  public static Logger log = LoggerFactory.getLogger( EngineeringUtil.class );

  public static Pattern getMethodPattern = null;
  static {
    try {
      getMethodPattern = Pattern.compile("\\A(?:get|iterate)(.+)\\Z");
    }
    catch( PatternSyntaxException pse ) {
      if( log.isErrorEnabled() ) {
        log.error("Could not create the pattern for matching get methods.", pse);
      }
    }
  }

  /**
   * Create a ClassPool which can access all the classes available to the given ClassLoader.
   * 
   * @param classLoader The ClassLoader to build the class path from.
   * 
   * @return A ClassPool which can access classes available to the given ClassLoader.
   */
  public static ClassPool createClassPool( ClassLoader classLoader )
  {
    ClassPool classPool = new ClassPool();
    classPool.appendClassPath(new LoaderClassPath(classLoader));
    return classPool;
  }

  /**
   * Engineer the given command class.
   * 
   * @param classPool The ClassPool to build from.
   * @param ctClass The command class to engineer.
   * @return The engineered command class.
   */
  public static CtClass engineerCommand( ClassPool classPool, CtClass ctClass )
    throws Exception
  {
    CtClass filterClass = getCtClass( classPool, "org.xchain.Filter");
    CtClass locatableClass = getCtClass( classPool, "org.xchain.Locatable");
    CtClass locatorClass = getCtClass( classPool, "org.xml.sax.Locator");
    CtClass engineeredCommandClass = getCtClass( classPool, "org.xchain.EngineeredCommand");
    CtClass qNameClass = getCtClass( classPool, "javax.xml.namespace.QName");
    CtClass stringClass = getCtClass( classPool, "java.lang.String");
    CtClass enumClass = getCtClass( classPool, "java.lang.Enum");
    CtClass iteratorClass = getCtClass( classPool, "java.util.Iterator");

    // create the engineered class
    CtClass engineeredClass = classPool.makeClass(getEngineeredCommandName(ctClass), ctClass);

      // if this class is not marked by the Engineered interface, then add it.
      if( !engineeredClass.subtypeOf(engineeredCommandClass) ) {
        engineeredClass.addInterface(engineeredCommandClass);
      }
      
      // add a list of namespace prefix mappings to the command, so that they can be set during the execute and postProcess methods.
      engineeredClass.addField(CtField.make("protected java.util.Map prefixMap = new java.util.HashMap();", engineeredClass));
      CtMethod prefixMappingGetterMethod = CtNewMethod.make("public java.util.Map getPrefixMap() { return prefixMap; }", engineeredClass);
      engineeredClass.addMethod(prefixMappingGetterMethod);

      engineeredClass.addField(CtField.make("protected java.util.Map attributeMap = new java.util.HashMap();", engineeredClass));
      engineeredClass.addMethod(CtNewMethod.make("public java.util.Map getAttributeMap() { return attributeMap; }", engineeredClass));

      engineeredClass.addField(CtField.make("protected javax.xml.namespace.QName qName = null;", engineeredClass));
      engineeredClass.addMethod(CtNewMethod.make("public void setQName( javax.xml.namespace.QName qName ) { this.qName = qName; }", engineeredClass));
      engineeredClass.addMethod(CtNewMethod.make("public javax.xml.namespace.QName getQName() { return this.qName; }", engineeredClass));

      engineeredClass.addField(CtField.make("protected String systemId = null;", engineeredClass));
      engineeredClass.addMethod(CtNewMethod.make("public void setSystemId( String systemId ) { this.systemId = systemId; }", engineeredClass));
      engineeredClass.addMethod(CtNewMethod.make("public String getSystemId() { return this.systemId; }", engineeredClass));

      engineeredClass.addMethod(CtNewMethod.make("public boolean isRegistered() { return this.qName != null && this.systemId != null; }", engineeredClass));

      // add the locatable interface if it is missing.
      if( !engineeredClass.subtypeOf(locatableClass) ) {
        engineeredClass.addInterface(locatableClass);
      }

      // add or override locatable methods.
      CtMethod setLocatorMethod = getMethod( engineeredClass, "setLocator", new CtClass[] { locatorClass } );
      CtMethod getLocatorMethod = getMethod( engineeredClass, "getLocator", new CtClass[] {} );
      CtField locatorField = getField( engineeredClass, "locator" );

      // make sure the method getLocator() has the correct return type.
      if( getLocatorMethod != null && !getLocatorMethod.getReturnType().subtypeOf(locatorClass) ) {
        throw new Exception("The class '"+engineeredClass.getName()+"' is not compatable with the interface org.xchain.Locatable.  The return type of getLocator() is incorrect.");
      }

      // make sure the method setLocator(Locator) has the correct return type.
      if( setLocatorMethod != null && !setLocatorMethod.getReturnType().subtypeOf(CtClass.voidType) ) {
        throw new Exception("The class '"+engineeredClass.getName()+"' is not compatable with the interface org.xchain.Locatable.  The return type of setLocator( Locator locator ) is incorrect.");
      }

      // if we need to implement either the setLocator(Locator) method or the getLocator() methods, then we need to check the locator fields type.
      if( (setLocatorMethod == null || getLocatorMethod == null) && locatorField != null && !locatorField.getType().subtypeOf(locatorClass) ) {
        throw new Exception("The class '"+engineeredClass.getName()+"' has a locator field that is not of the type org.xml.sax.Locator.");
      }

      if( ( setLocatorMethod == null || getLocatorMethod == null ) && locatorField == null ) {
        engineeredClass.addField(CtField.make("protected org.xml.sax.Locator locator = null;", engineeredClass));
      }

      if( setLocatorMethod == null ) {
        engineeredClass.addMethod(CtNewMethod.make("public void setLocator( org.xml.sax.Locator locator ) { this.locator = locator; }", engineeredClass));
      }
      if( getLocatorMethod == null ) {
        engineeredClass.addMethod(CtNewMethod.make("public org.xml.sax.Locator getLocator() { return this.locator; }", engineeredClass));
      }
      
      // Keep track of all the attributes for this engineered command.
      Set<QName> attributeSet = new HashSet<QName>();
      Map<QName, AttributeType> attributeTypeMap = new HashMap<QName, AttributeType>();
      Map<QName, Boolean> attributeRequiredMap = new HashMap<QName, Boolean>();
      Map<QName, CtClass> attributeJavaTypeMap = new HashMap<QName, CtClass>();

      // add attribute fields to abstract methods.
      for( CtMethod ctMethod : ctClass.getMethods() ) {
        // if the ctMethod has the attribute annotations and is abstract, then add the needed methods.
        if( hasAnnotation( ctMethod, Attribute.class ) ) {
          // variables from the annotation.
          String attributeLocalName = (String)getAnnotationValue( ctMethod, Attribute.class, "localName" );
          String attributeUri = (String)getAnnotationValue( ctMethod, Attribute.class, "namespaceUri" );
          String defaultValue = (String)getAnnotationValue( ctMethod, Attribute.class, "defaultValue" );
          Boolean required = (Boolean)getAnnotationValue( ctMethod, Attribute.class, "required" );

          // variables describing the return type.
          boolean isPrimative = ctMethod.getReturnType().isPrimitive();
          boolean isVoid = ctMethod.getReturnType().equals(CtClass.voidType);
          boolean isBoolean = ctMethod.getReturnType().equals(CtClass.booleanType);
          boolean isNumber = ctMethod.getReturnType().equals(CtClass.byteType) ||
                             ctMethod.getReturnType().equals(CtClass.charType) ||
                             ctMethod.getReturnType().equals(CtClass.doubleType) ||
                             ctMethod.getReturnType().equals(CtClass.floatType) ||
                             ctMethod.getReturnType().equals(CtClass.intType) ||
                             ctMethod.getReturnType().equals(CtClass.longType) ||
                             ctMethod.getReturnType().equals(CtClass.shortType);
          boolean isTypeSpecified = ctMethod.getParameterTypes().length == 2;
          // the type that will be used with ConvertUtils and JXPathContext
          String typeExpression = isTypeSpecified ? "$2" : "$type";
          boolean isIterateMethod = ctMethod.getName().startsWith("iterate") && ctMethod.getReturnType().subtypeOf(iteratorClass);

          // add a default value of primative types.
          if( defaultValue == null && isPrimative ) {
            if( isBoolean ) { defaultValue = "false"; }
            else if( isNumber ) { defaultValue = "("+ctMethod.getReturnType().getSimpleName()+")0;"; }
            else if( isVoid ) { /* void does not get a default value */ }
            else {
              throw new IllegalStateException("The engineering code does not support the primative type '"+ctMethod.getReturnType().getSimpleName());
            }
          }
          //
          // Now we are going to engineer a method following this pattern:
          //
          // {
          //   // The name for this attribute.
          //   javax.xml.namespace.QName attributeName = new javax.xml.namespace.QName(ATTRIBUTE_NAMESPACE_URI, ATTRIBUTE_LOCAL_NAME);
          //
          //   // The attribute value defined on this element.
          //   String attributeValue = (String)getAttributeMap().get(attributeName);
          //
          //   // Do we need use the default?
          //   boolean doDefault = attributeValue == null;
          //
          //   // The map that will hold the original mappings.
          //   java.util.Map originalPrefixMapping = null;
          //
          //   // NOTE: If the attribute is required, but the attribute is not present, we need to throw an exception.
          //
          //   // ASSERT: at this point, we are going to have a value to return, or fail with a type conversion exception.
          //   ATTRIBUTE_TYPE result;
          //   try {
          //     // define the prefix mappings for this commands element.
          //     org.xchain.framework.lifecycle.Execution.definePrefixMappings($1, this);
          //
          //     // if we need a default and we have a default, then do the default mappings.
          //     if( doDefault ) {
          //       originalPrefixMapping = new java.util.HashMap();
          //
          //       // record all of the current mappings for the default prefixes.
          //       originalPrefixMapping.put(DEFAULT_MAPPING_PREFIX, $1.getNamespaceURI(DEFAULT_MAPPING_PREFIX));
          //       ...
          //       // register the default namespace mappings.
          //       $1.registerNamespace(DEFAULT_MAPPING_PREFIX, DEFAULT_MAPPING_URI);
          //       ...
          //       attributeValue = defaultValue;
          //     }
          //       
          //     // Do the AttributeType specific lookup.  This changes per attribute type.
          //   }
          //   catch( org.apache.commons.jxpath.JXPathException jxpe ) {
          //     Throwable cause = jxpe.getCause();
          //     if( cause == null ) {
          //       throw jxpe;
          //     }
          //     else if( cause instanceof RuntimeException ) {
          //       throw (RuntimeException)cause;
          //     }
          //     else if( cause instanceof THROWN_EXCEPTION ) {
          //       throw (THROWN_EXCEPTION)cause;
          //     }
          //     ..
          //   }
          //   finally {
          //     // if there are default mappings, include code to undo the mappings.
          //     if( doDefault && originalPrefixMapping != null ) {
          //       // there is one of there for each mapping.
          //       $1.registerNamespace(DEFAULT_MAPPING_PREFIX, (String)originalPrefixMapping.get(DEFAULT_MAPPING_PREFIX));
          //       ...
          //     }
          //     // undefine the prefix mappings for this commands element.
          //     org.xchain.framework.lifecycle.Execution.undefinePrefixMappings($1, this);  }
          //   }
          //   return value;

          QName attributeQName = new QName(attributeUri, attributeLocalName);
          AttributeType attributeType = (AttributeType)getAnnotationValue( ctMethod, Attribute.class, "type" );
          CtClass attributeJavaType = ctMethod.getReturnType();
          // Add the attribute to the set of known attributes.
          attributeSet.add(attributeQName);
          attributeTypeMap.put(attributeQName, attributeType);
          attributeJavaTypeMap.put(attributeQName, attributeJavaType);
          attributeRequiredMap.put(attributeQName, required);
          PrefixMapping[] defaultPrefixMappings = (PrefixMapping[])getAnnotationValue( ctMethod, Attribute.class, "defaultPrefixMappings" );
          if( Modifier.isAbstract(ctMethod.getModifiers()) ) {
          CtMethod engineeredMethod = CtNewMethod.copy( ctMethod, engineeredClass, null );

          // Start making the method body.
          StringBuffer methodBody = new StringBuffer();
          methodBody.append("{\n");
          methodBody.append("  // The name for this attribute.\n");
          methodBody.append("  javax.xml.namespace.QName attributeName = new javax.xml.namespace.QName(")
                    .append(stringConstant(attributeUri)).append(", ").append(stringConstant(attributeLocalName)).append(");\n");
          methodBody.append("\n");
          methodBody.append("  // The attribute value defined on this element.\n");
          methodBody.append("  String attributeValue = (String)getAttributeMap().get(attributeName);\n");
          methodBody.append("\n");
          methodBody.append("  // Do we need use the default?\n");
          methodBody.append("  boolean doDefault = attributeValue == null;\n");
          methodBody.append("\n");
          methodBody.append("  // The map that will hold the original mappings.\n");
          methodBody.append("  java.util.Map originalPrefixMapping = null;\n");
          methodBody.append("\n");

          // TODO: add code here to handle required.
          // if the attributeValue is null, we are not in a primative, the default is "", and the attribute is not required, return null.
          if( !isPrimative && "".equals(defaultValue)) {
            methodBody.append("  if( attributeValue == null ) {\n");
            methodBody.append("    return null;\n");
            methodBody.append("  }\n");
            methodBody.append("\n");
          }

          methodBody.append("  // ASSERT: at this point, we are going to have a value to return, or fail with a type conversion exception.\n");
          methodBody.append("\n");
          methodBody.append("  // This is the variable to hold the result.\n");
          methodBody.append("  ").append(ctMethod.getReturnType().getName()).append(" result;\n");
          methodBody.append("\n");
          methodBody.append("  try {\n");
          methodBody.append("    // define the prefix mappings for this commands element.\n");
          methodBody.append("    org.xchain.framework.lifecycle.Execution.definePrefixMappings($1, this);\n");
          methodBody.append("\n");
          methodBody.append("    // if we need a default and we have a default, then do the default mappings.\n");
          methodBody.append("    if( doDefault ) {\n");
          methodBody.append("      originalPrefixMapping = new java.util.HashMap();\n");
          methodBody.append("\n");
          methodBody.append("      // record all of the current mappings for the default prefixes.\n");
          for( PrefixMapping mapping : defaultPrefixMappings ) {
            String escapedPrefix = stringConstant(mapping.prefix());
            methodBody.append("      originalPrefixMapping.put(").append(escapedPrefix).append(", $1.getNamespaceURI(").append(escapedPrefix).append("));\n");
          }
          methodBody.append("\n");
          methodBody.append("      // register the default namespace mappings.\n");
          for( PrefixMapping mapping : defaultPrefixMappings ) {
            String escapedPrefix = stringConstant(mapping.prefix());
            String escapedUri = stringConstant(mapping.uri());
            methodBody.append("      $1.registerNamespace(").append(escapedPrefix).append(", ").append(escapedUri).append(");\n");
          }
          methodBody.append("\n");
          if( !"".equals(defaultValue) ) {
            methodBody.append("      attributeValue = ").append(stringConstant(defaultValue)).append(";\n");
          }
          else if( isNumber ) {
            methodBody.append("      attributeValue = \"0\";\n");
          }
          else if( isBoolean ) {
            methodBody.append("      attributeValue = \"false\";\n");
          }
          methodBody.append("    }\n");
          methodBody.append("\n");

          // ASSERT: The top of the method definition is now done.  The value of the attribute has now been looked up, defaulted, or the method would have caused an exception.

          // It is now time to add the logic for the AttriuteType.
          switch( attributeType ) {
            // 
            // AttributeType.JXPATH_VALUE attributes follow one of two patterns... 
            // For methods that start with "iterate" and have a type of java.util.Iterator, the following code is used:
            //   result = ($r)$1.iterate(attributeValue);
            //
            // For all other methods, the following code is used:
            //   if( !TYPE.isAssignableFrom(REQUESTED_TYPE) {
            //     throw new IllegalArgumentException("Can not cast REQUESTED_TYPE into TYPE.");
            //   }
            //   result = ($r)$1.getValue(attributeValue, TYPE);
            case JXPATH_VALUE:
              if( isIterateMethod ) {
                methodBody.append("    result = ($r)$1.iterate(attributeValue);\n");
              }
              else {
                methodBody.append("    result = ($r)$1.getValue(attributeValue, ").append(typeExpression).append(");\n");
              }
              break;
            case JXPATH_SELECT_NODES:
              if( isIterateMethod ) {
                methodBody.append("    result = ($r)$1.selectNodes(attributeValue).iterator();\n");
              }
              else {
                methodBody.append("    return ($r)$1.selectNodes(attributeValue);\n");
              }
              break;
            case JXPATH_SELECT_SINGLE_NODE:
              methodBody.append("    return ($r)$1.selectSingleNode(attributeValue);\n");
              break;
            case JXPATH_POINTER:
              methodBody.append("    result = ($r)$1.getPointer(attributeValue);\n");
              break;
            case JXPATH_ITERATE_POINTERS:
              methodBody.append("    result = ($r)$1.iteratePointers(attributeValue);\n");
              break;
            case QNAME:
              if( engineeredMethod.getReturnType().subtypeOf(qNameClass) ) {
                methodBody.append("    result = org.xchain.framework.util.JXPathContextUtil.stringToQName($1, attributeValue);\n");
              }
              else if( engineeredMethod.getReturnType().subtypeOf(stringClass) ) {
                methodBody.append("    result = org.xchain.framework.util.JXPathContextUtil.stringToQNameString($1, attributeValue);\n");
              }
              else {
                throw new RuntimeException("QName attributes do not support the return type '"+engineeredMethod.getReturnType()+"'.");
              }
              break;
            case LITERAL:
              methodBody.append("    result = ($r)org.xchain.framework.util.JXPathContextUtil.convert((Object)attributeValue, ").append(typeExpression).append(");\n");
              break;
            case ATTRIBUTE_VALUE_TEMPLATE:
              methodBody.append("    result = ($r)org.xchain.framework.util.JXPathContextUtil.convert((Object)org.xchain.framework.util.AttributesUtil.evaluateAttributeValueTemplate($1, attributeValue), ")
                        .append(typeExpression).append(");\n");
              break;
            default:
              throw new RuntimeException("Unknown attribute type encountered.");
          }
          methodBody.append("  }\n");

          // ASSERT: The method body now has a complete try block.

          // JXPath wraps exceptions coming from functions in JXPathException's, we need to unwrap those, so that the caller get the exception that they expect.
          methodBody.append("  catch( org.apache.commons.jxpath.JXPathException jxpe ) {\n");
          methodBody.append("    Throwable cause = jxpe.getCause();\n");
          methodBody.append("    // if there is no cause, then we cannot unwrap the exception.\n");
          methodBody.append("    if( cause == null ) {\n");
          methodBody.append("      throw jxpe;\n");
          methodBody.append("    }\n");
          methodBody.append("    // if this is a runtime exception, then we can throw it as a runtime exception.\n");
          methodBody.append("    else if( cause instanceof RuntimeException ) {\n");
          methodBody.append("      throw (RuntimeException)cause;\n");
          methodBody.append("    }\n");
          // for each exception that is declared to be thrown, we need to test the runnable and try to throw as that type.
          for( CtClass thrownExceptionClass : ctMethod.getExceptionTypes() ) {
            methodBody.append("    else if( cause instanceof ").append(thrownExceptionClass.getName()).append(" ) {\n");
            methodBody.append("      throw (").append(thrownExceptionClass.getName()).append(")cause;\n");
            methodBody.append("    }\n");
          }
          methodBody.append("    // we could not cast the cause, so leave the exception wrapped.\n");
          methodBody.append("    else {\n");
          methodBody.append("      throw jxpe;\n");
          methodBody.append("    }\n");
          methodBody.append("  }\n");

          // ASSERT: Any exceptions thrown while evaluating an attribute have been unwrapped if they could.

          methodBody.append("  finally {\n");
          methodBody.append("    // if there are default mappings, include code to undo the mappings.\n");
          methodBody.append("    if( doDefault && originalPrefixMapping != null ) {\n");
          methodBody.append("      // there is one of there for each mapping.\n");
          for( PrefixMapping mapping : defaultPrefixMappings ) {
            String escapedPrefix = stringConstant(mapping.prefix());
            methodBody.append("      $1.registerNamespace(").append(escapedPrefix).append(", (String)originalPrefixMapping.get(").append(escapedPrefix).append("));\n");
          }
          methodBody.append("    }\n");
          methodBody.append("    // undefine the prefix mappings for this commands element.\n");
          methodBody.append("    org.xchain.framework.lifecycle.Execution.undefinePrefixMappings($1, this);\n");
          methodBody.append("  }\n");
          methodBody.append("  return result;\n");
          methodBody.append("}\n");
            //System.out.println(methodBody.toString());
            //Thread.currentThread().sleep(1000);
            engineeredMethod.setBody(methodBody.toString());
            engineeredMethod.setModifiers( Modifier.clear( ctMethod.getModifiers(), Modifier.ABSTRACT ) );
            engineeredClass.addMethod(engineeredMethod);
          }

          // test to see if the class has a "has" method.
          Matcher matcher = getMethodPattern.matcher(ctMethod.getName());
          if( matcher.find() ) {
            String hasMethodName = "has"+matcher.group(1);
            try {
              CtMethod hasMethod = ctClass.getMethod( hasMethodName, "()Z");
              if( Modifier.isAbstract(hasMethod.getModifiers()) ) {
                CtMethod engineeredHasMethod = CtNewMethod.copy( hasMethod, engineeredClass, null );
                // TODO: should we return true if there is a default value?
                engineeredHasMethod.setBody(
                  "{"+
                    "return getAttributeMap().containsKey(new javax.xml.namespace.QName(\""+attributeUri+"\", \""+attributeLocalName+"\"));"+
                  "}"
                );
                engineeredHasMethod.setModifiers( Modifier.clear( hasMethod.getModifiers(), Modifier.ABSTRACT ) );
                engineeredClass.addMethod(engineeredHasMethod);
              }
            }
            catch( NotFoundException nfe ) {
              // do nothing, we do not need this method.
            }
          }
        }  
      }

      // Add all static fields and value.
      // Add the static attributes.
      engineeredClass.addField(CtField.make("protected static java.util.Set attributeSet = new java.util.HashSet();", engineeredClass));
      engineeredClass.addField(CtField.make("protected static java.util.Map attributeDetailMap = new java.util.HashMap();", engineeredClass));

      // Add the class initializer for the static attributes.
      StringBuilder ib = new StringBuilder();
      ib.append("{");
      ib.append("javax.xml.namespace.QName attributeQName = null;");
      ib.append("org.xchain.AttributeDetail attributeDetail = null;");
      for(QName attributeName : attributeSet) {
        AttributeType attributeType = attributeTypeMap.get(attributeName);
        CtClass attributeJavaType = attributeJavaTypeMap.get(attributeName);
        ib.append("attributeQName = new javax.xml.namespace.QName(\"" + attributeName.getNamespaceURI() + "\", \"" + attributeName.getLocalPart() + "\");");
        ib.append("attributeSet.add(attributeQName);");
        ib.append("attributeDetail = new org.xchain.AttributeDetail(attributeQName, org.xchain.annotations.AttributeType."+attributeType.name()+", "+attributeJavaType.getName()+".class, "+attributeRequiredMap.get(attributeName)+");");
        ib.append("attributeDetailMap.put(attributeQName, attributeDetail);");
      }
      ib.append("attributeSet = java.util.Collections.unmodifiableSet(attributeSet);");
      ib.append("attributeDetailMap = java.util.Collections.unmodifiableMap(attributeDetailMap);");
      ib.append("}");
      
      engineeredClass.makeClassInitializer().insertAfter(ib.toString());

      // Add the instance accessors for the attribute set and the attribute type set.
      engineeredClass.addMethod(CtNewMethod.make("public java.util.Set getAttributeSet() { return attributeSet; }", engineeredClass));
      engineeredClass.addMethod(CtNewMethod.make("public java.util.Map getAttributeDetailMap() { return attributeDetailMap; }", engineeredClass));

      // find the original execute method.

      // create a new method that sets the mappings into the context and the current system id.
      StringBuilder newExecuteMethod = new StringBuilder();
      // code to insert the mappings.
      newExecuteMethod.append("public boolean execute(org.apache.commons.jxpath.JXPathContext context) throws java.lang.Exception {");
      newExecuteMethod.append("  boolean createLocalContext = isRegistered();");
      newExecuteMethod.append("  boolean inThread = org.xchain.framework.lifecycle.ThreadLifecycle.getInstance().inThread();");
      newExecuteMethod.append("  boolean inExecution = org.xchain.framework.lifecycle.Execution.inExecution();");
      newExecuteMethod.append("  boolean result = false;");
      newExecuteMethod.append("  org.xchain.framework.lifecycle.ThreadContext threadContext = null;");
      newExecuteMethod.append("  if( !inThread ) {");
      newExecuteMethod.append("    threadContext = new org.xchain.framework.lifecycle.ThreadContext();");
      newExecuteMethod.append("    org.xchain.framework.lifecycle.ThreadLifecycle.getInstance().startThread(threadContext);");
      newExecuteMethod.append("  }");
      newExecuteMethod.append("  try {");
      newExecuteMethod.append("  if( !inExecution ) {");
      newExecuteMethod.append("    org.xchain.framework.lifecycle.Execution.startExecution(context);");
      newExecuteMethod.append("  }");
      newExecuteMethod.append("  context = org.xchain.framework.lifecycle.Execution.startCommandExecute(this, context);");
      newExecuteMethod.append("  Exception exception = null;");
      newExecuteMethod.append("  try {");
      newExecuteMethod.append("    result = super.execute(context);");
      newExecuteMethod.append("  }");
      newExecuteMethod.append("  catch( Exception e ) {");
      newExecuteMethod.append("    org.xchain.framework.lifecycle.Execution.exceptionThrown(this, e);");
      newExecuteMethod.append("    exception = e;");
      newExecuteMethod.append("    throw e;");
      newExecuteMethod.append("  }");
      newExecuteMethod.append("  finally {");
      newExecuteMethod.append("    context = org.xchain.framework.lifecycle.Execution.endCommandExecute(this, context);");
      // if this is a filter, then after the first execute call, we need to make the post process call that would have been
      // made by our containing chain.
      if( engineeredClass.subtypeOf(filterClass) ) {
        newExecuteMethod.append("    if( !inExecution ) {");
        newExecuteMethod.append("      context = org.xchain.framework.lifecycle.Execution.startCommandPostProcess( this, context );");
        newExecuteMethod.append("      try {");
        newExecuteMethod.append("        boolean handled = super.postProcess(context, exception);");
        newExecuteMethod.append("        if( handled == true ) {");
        newExecuteMethod.append("          org.xchain.framework.lifecycle.Execution.exceptionHandled(this, exception);");
        newExecuteMethod.append("        }");
        newExecuteMethod.append("      }");
        newExecuteMethod.append("      finally {");
        newExecuteMethod.append("        org.xchain.framework.lifecycle.Execution.endCommandPostProcess(this, context);");
        newExecuteMethod.append("        org.xchain.framework.lifecycle.Execution.endExecution();");
        newExecuteMethod.append("      }");
        newExecuteMethod.append("    }");
      }
      else {
        newExecuteMethod.append("    if( !inExecution ) {");
        newExecuteMethod.append("      org.xchain.framework.lifecycle.Execution.endExecution();");
        newExecuteMethod.append("    }");
      }
      newExecuteMethod.append("  }");
      newExecuteMethod.append("  }");
      newExecuteMethod.append("  finally {");
      newExecuteMethod.append("    if( !inThread ) {");
      newExecuteMethod.append("      org.xchain.framework.lifecycle.ThreadLifecycle.getInstance().stopThread(threadContext);");
      newExecuteMethod.append("    }");
      newExecuteMethod.append("  }");
      newExecuteMethod.append("  return result;");
      newExecuteMethod.append("}");
      engineeredClass.addMethod(CtNewMethod.make(newExecuteMethod.toString(), engineeredClass));

      if( engineeredClass.subtypeOf(filterClass) ) {
        StringBuilder newPostProcessMethod = new StringBuilder();

        // code to insert the mappings.
        newPostProcessMethod.append("public boolean postProcess( org.apache.commons.jxpath.JXPathContext context, Exception exception ) {");
        newPostProcessMethod.append("  boolean handled = false;");
        newPostProcessMethod.append("  if( org.xchain.framework.lifecycle.Execution.inExecution() ) {");
        newPostProcessMethod.append("  context = org.xchain.framework.lifecycle.Execution.startCommandPostProcess( this, context );");
        newPostProcessMethod.append("  try {");
        newPostProcessMethod.append("    handled = super.postProcess(context, exception);");
        newPostProcessMethod.append("    if( handled == true ) {");
        newPostProcessMethod.append("      org.xchain.framework.lifecycle.Execution.exceptionHandled(this, exception);");
        newPostProcessMethod.append("    }");
        newPostProcessMethod.append("    return handled;");
        newPostProcessMethod.append("  }");
        newPostProcessMethod.append("  finally {");
        newPostProcessMethod.append("    org.xchain.framework.lifecycle.Execution.endCommandPostProcess(this, context);");
        newPostProcessMethod.append("  }");
        newPostProcessMethod.append("  }");
        newPostProcessMethod.append("  return handled;");
        newPostProcessMethod.append("}");
        engineeredClass.addMethod(CtNewMethod.make(newPostProcessMethod.toString(), engineeredClass));
      }

    return engineeredClass;
  }

  /**
   * Build a unique engineered command name from the given CtClass.
   * @param originalClass The class to build a unique engineered command name from.
   * @return A unique engineered name for the given command CtClass.
   */
  public static String getEngineeredCommandName( CtClass originalClass )
  {
    return originalClass.getName()+"Engineered";
  }

  /**
   * Find the CtMethod on the given class.
   * @param ctClass The class to find the method on.
   * @param name The name of the method to find.
   * @param params The array of parameters that the method will use.
   * @return The indicated method or null if the method could not be found.
   */
  public static CtMethod getMethod( CtClass ctClass, String name, CtClass[] params )
  {
    try {
      return ctClass.getDeclaredMethod(name, params);
    }
    catch( NotFoundException nfe ) {
      return null;
    }
  }

  /**
   * Find the field on the given class.
   * @param ctClass The class on which to find the field.
   * @param name The name of the field.
   * @return The requested field or null if the field could not be found.
   */
  public static CtField getField( CtClass ctClass, String name )
  {
    try {
      return ctClass.getField(name);
    }
    catch( NotFoundException nfe ) {
      return null;
    }
  }

  /**
   * Find the class in the pool for the given name.
   * @param classPool The ClassPool to search through.
   * @param name The name of the class to find.
   * @return The requested class.  If the requested class could not be found a runtime exception will be throw.
   */
  public static CtClass getCtClass( ClassPool classPool, String name )
  {
    try {
      return classPool.get(name);
    }
    catch( NotFoundException nfe ) {
      throw new RuntimeException("The engineering util expected to find the class '"+name+"', but it could not be found.", nfe);
    }
  }

  /**
   * Determine if the given class is a subtype of the given type.
   * @param ctClass The class to check.
   * @param subtype The subtype to check against.
   * @return True if the given class is a subtype of the given subtype.  This is also true if the given class is the same as the given subtype.
   */
  public static boolean subtypeOf( CtClass ctClass, CtClass subtype )
  {
    try {
      return ctClass.subtypeOf(subtype);
    }
    catch( NotFoundException nfe ) {
      throw new RuntimeException("An exception was thrown while testing for a subclass.", nfe);
    }
  }

  /**
   * Engineer the given catalog class.
   * 
   * @param classPool The ClassPool to build from.
   * @param ctClass The catalog class to engineer.
   * @return The engineered catalog class.
   */
  public static CtClass engineerCatalog( ClassPool classPool, CtClass ctClass )
    throws Exception
  {
    CtClass engineeredCatalogClass = getCtClass( classPool, "org.xchain.EngineeredCatalog");

    // create the engineered class
    CtClass engineeredClass = classPool.makeClass(getEngineeredCommandName(ctClass), ctClass);

    // if this class is not marked by the Engineered interface, then add it.
    if( !engineeredClass.subtypeOf(engineeredCatalogClass) ) {
      engineeredClass.addInterface(engineeredCatalogClass);
    }

    // add the class loader.
    engineeredClass.addField(CtField.make("protected java.lang.ClassLoader classLoader = null;", engineeredClass));
    engineeredClass.addMethod(CtNewMethod.make("public java.lang.ClassLoader getClassLoader() { return this.classLoader; }", engineeredClass));
    engineeredClass.addMethod(CtNewMethod.make("public void setClassLoader(java.lang.ClassLoader classLoader) { this.classLoader = classLoader; }", engineeredClass));

    // add the engineered system id.
    engineeredClass.addField(CtField.make("protected java.lang.String systemId = null;", engineeredClass));
    engineeredClass.addMethod(CtNewMethod.make("public java.lang.String getSystemId() { return this.systemId; }", engineeredClass));
    engineeredClass.addMethod(CtNewMethod.make("public void setSystemId(java.lang.String systemId) { this.systemId = systemId; }", engineeredClass));

    return engineeredClass;
  }

  public static String stringConstant( String source )
  {
    // is the source is null, return null.
    if( source == null ) {
      return "((String)null)";
    }

    // the string buffer for the result.
    StringBuilder builder = new StringBuilder();

    // the initial double quote.
    builder.append('\"');

    // escape special characters in the string.
    source = source.replaceAll("\\\\", "\\\\\\\\");
    source = source.replaceAll("\\\"", "\\\\\"");
    source = source.replaceAll("\\\'", "\\\\\'");
    source = source.replaceAll("\r", "\\\\r");
    source = source.replaceAll("\t", "\\\\t");
    source = source.replaceAll("\b", "\\\\b");
    source = source.replaceAll("\n", "\\\\n");
    source = source.replaceAll("\f", "\\\\f");
    builder.append(source);

    // terminating double quote.
    builder.append('\"');

    return builder.toString();
  }
}
