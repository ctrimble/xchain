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

import java.io.DataInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ByteMemberValue;
import javassist.bytecode.annotation.CharMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.DoubleMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.FloatMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.LongMemberValue;
import javassist.bytecode.annotation.ShortMemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationUtil
{
  public static Logger log = LoggerFactory.getLogger(AnnotationUtil.class);

   public static boolean hasAnnotation(ClassFile classFile, Class<? extends Annotation> annotationType)
   {
     AnnotationsAttribute visibleAnnotations = (AnnotationsAttribute)classFile.getAttribute(AnnotationsAttribute.visibleTag);
     if( visibleAnnotations == null ) { return false; }
     return visibleAnnotations.getAnnotation(annotationType.getName()) != null;
   }

  public static boolean hasAnnotation( CtClass ctClass, Class<? extends Annotation> annotationType )
    throws ClassNotFoundException
  {
    for( Object annotation: ctClass.getAnnotations() ) {
      if( annotationType.isInstance(annotation) ) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasAnnotation( Method method, Class<? extends Annotation> annotationType )
  {
    return method.getAnnotation(annotationType) != null;
  }


  /**
   * Tests to see if a CtMethod or a CtConstructor has the specified annotation.
   */
  public static boolean hasAnnotation( CtBehavior behavior, Class<? extends Annotation> annotationType )
    throws ClassNotFoundException
  {
    for( Object annotation : behavior.getAnnotations() ) {
      if( annotationType.isInstance(annotation) ) {
        return true;
      }
    }
    return false;
  }

  public static Object getAnnotationValue(ClassFile classFile, Class<? extends Annotation> annotationType, String memberName)
  {
    AnnotationsAttribute visibleAnnotations = (AnnotationsAttribute)classFile.getAttribute(AnnotationsAttribute.visibleTag);
    if( visibleAnnotations == null ) return null;
    return getAnnotationValue( visibleAnnotations.getAnnotation(annotationType.getName()), memberName );
  }

  public static Object getAnnotationValue( CtBehavior behavior, Class<? extends Annotation> annotationType, String memberName )
    throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException
  {
    for( Object annotation : behavior.getAnnotations() ) {
      if( annotationType.isInstance(annotation) ) {
        return annotationType.getMethod(memberName, new Class[] {}).invoke(annotation, new Object[] {});
      }
    }
    return false;
  }


  public static Object getAnnotationValue( javassist.bytecode.annotation.Annotation annotation, String memberName )
  {
	if( annotation == null ) return null;
    return getAnnotationValue(annotation.getMemberValue(memberName));
  }

  public static Object getAnnotationValue( MemberValue memberValue )
  {
    if( memberValue == null ) {
      return null;
    }
    else if( memberValue instanceof AnnotationMemberValue ) {
      return ((AnnotationMemberValue)memberValue).getValue();
    }
    else if( memberValue instanceof ArrayMemberValue ) {
      return ((ArrayMemberValue)memberValue).getValue();
    }
    else if( memberValue instanceof BooleanMemberValue ) {
      return ((BooleanMemberValue)memberValue).getValue();
    }
    else if( memberValue instanceof ByteMemberValue ) {
      return ((ByteMemberValue)memberValue).getValue();
    }
    else if( memberValue instanceof CharMemberValue ) {
      return ((CharMemberValue)memberValue).getValue();
    }
    else if( memberValue instanceof ClassMemberValue ) {
      return ((ClassMemberValue)memberValue).getValue();
    }
    else if( memberValue instanceof DoubleMemberValue ) {
      return ((DoubleMemberValue)memberValue).getValue();
    }
    else if( memberValue instanceof EnumMemberValue ) {
      return ((EnumMemberValue)memberValue).getValue();
    }
    else if( memberValue instanceof FloatMemberValue ) {
      return ((FloatMemberValue)memberValue).getValue();
    }
    else if( memberValue instanceof IntegerMemberValue ) {
      return ((IntegerMemberValue)memberValue).getValue();
    }
    else if( memberValue instanceof LongMemberValue ) {
      return ((LongMemberValue)memberValue).getValue();
    }
    else if( memberValue instanceof ShortMemberValue ) {
      return ((ShortMemberValue)memberValue).getValue();
    }
    else if( memberValue instanceof StringMemberValue ) {
      return ((StringMemberValue)memberValue).getValue();
    }
    else {
      throw new RuntimeException("The value type of this annotation is not yet supported.");
    }
  }

  public static ClassFile getClassFile( String resourceName, ClassLoader classLoader )
  {
    DataInputStream dstream = null;
    ClassFile classFile = null;

    try
    {
      dstream = new DataInputStream(classLoader.getResourceAsStream(resourceName));

      classFile = new ClassFile(dstream);
    }
    catch( Exception e ) {
      if( log.isDebugEnabled() ) {
        log.debug("A javassist ClassFile could not be created for '"+resourceName+"'.", e);
      }
    }
    finally
    {
      if( dstream != null ) {
        try {
          dstream.close();
        }
        catch( Exception e ) {
          if( log.isWarnEnabled() ) {
            log.warn("Could not close DataInputStream for '"+resourceName+"'.", e);
          }
        }
      }
    }
    return classFile;
  }
}
