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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.xchain.framework.util.AnnotationUtil.getAnnotationValue;
import static org.xchain.framework.util.AnnotationUtil.getClassFile;
import static org.xchain.framework.util.AnnotationUtil.hasAnnotation;
import javassist.bytecode.ClassFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xchain.annotations.Element;
import org.xchain.annotations.Namespace;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
public class AnnotationTest
{
  public static Logger log = LoggerFactory.getLogger(AnnotationTest.class);

  public static String CORE_RESOURCE_BASE      = "org/xchain/namespaces/core/";
  public static String PACKAGE_INFO_RESOURCE   = CORE_RESOURCE_BASE+"package-info.class";
  public static String XCHAIN_CATALOG_RESOURCE = CORE_RESOURCE_BASE+"XChainCatalog.class";
  public static String CHAIN_RESOURCE          = CORE_RESOURCE_BASE+"ChainCommand.class";
  public static String IF_RESOURCE             = CORE_RESOURCE_BASE+"IfCommand.class";
  public static String CHOOSE_RESOURCE         = CORE_RESOURCE_BASE+"ChooseCommand.class";

  public static String URI_MEMBER_NAME = "uri";
  public static String LOCAL_NAME_MEMBER_NAME = "localName";

  @Before public void setUp()
  {
  }

  @After public void tearDown()
  {
  }

  @Test public void testPackageAnnotation()
    throws Exception
  {
    ClassFile classFile = getClassFile("org/xchain/namespaces/core/package-info.class", Thread.currentThread().getContextClassLoader());

    // does the xchain package have the annotation?
    assertTrue("The core package does not have the namespace attribute.", hasAnnotation(classFile, Namespace.class));

    // does the xchain package annotation have the correct uri.
    assertEquals("The core package does not have the correct namespace.", "http://www.xchain.org/core/1.0", getAnnotationValue(classFile, Namespace.class, URI_MEMBER_NAME));
  }

  @Test public void testXChainCatalogAnnotation()
  {
    ClassFile classFile = getClassFile(XCHAIN_CATALOG_RESOURCE, Thread.currentThread().getContextClassLoader());

    // does the xchain package have the annotation?
    assertTrue("The core package does not have the namespace attribute.", hasAnnotation(classFile, Element.class));

    // does the xchain package annotation have the correct uri.
    assertEquals("The core package does not have the correct namespace.", "catalog", getAnnotationValue(classFile, Element.class, LOCAL_NAME_MEMBER_NAME));
  }

  @Test public void testChainAnnotation()
  {
    ClassFile classFile = getClassFile(CHAIN_RESOURCE, Thread.currentThread().getContextClassLoader());

    // does the xchain package have the annotation?
    assertTrue("The core package does not have the namespace attribute.", hasAnnotation(classFile, Element.class));

    // does the xchain package annotation have the correct uri.
    assertEquals("The core package does not have the correct namespace.", "chain", getAnnotationValue(classFile, Element.class, LOCAL_NAME_MEMBER_NAME));

  }

  @Test public void testIfAnnotation()
  {
    ClassFile classFile = getClassFile(IF_RESOURCE, Thread.currentThread().getContextClassLoader());

    // does the xchain package have the annotation?
    assertTrue("The core package does not have the namespace attribute.", hasAnnotation(classFile, Element.class));

    // does the xchain package annotation have the correct uri.
    assertEquals("The core package does not have the correct namespace.", "if", getAnnotationValue(classFile, Element.class, LOCAL_NAME_MEMBER_NAME));

  }

  @Test public void testChooseAnnotation()
  {
    ClassFile classFile = getClassFile(CHOOSE_RESOURCE, Thread.currentThread().getContextClassLoader());

    // does the xchain package have the annotation?
    assertTrue("The core package does not have the namespace attribute.", hasAnnotation(classFile, Element.class));

    // does the xchain package annotation have the correct uri.
    assertEquals("The core package does not have the correct namespace.", "choose", getAnnotationValue(classFile, Element.class, LOCAL_NAME_MEMBER_NAME));
  }

}
