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

import org.apache.commons.jxpath.JXPathContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Devon Tackett
 * @author Christian Trimble
 */
public class TestJXPathContext 
{
	
	private static final String TEST_VARIABLE_NAME = "test_value";
	
	private JXPathContext parentContext;
	private JXPathContext childContext;
	
	@Before public void setUp() 
	{
		// Create a parent context.
		parentContext = JXPathContext.newContext(new Object());
		
		// Declare a test variable as false.
		parentContext.getVariables().declareVariable(TEST_VARIABLE_NAME, Boolean.FALSE);
		
		// Create a child context from the parent.
		childContext = JXPathContext.newContext(parentContext, new Object());		
	}
	
	@After public void tearDown() 
	{
		parentContext = null;
		childContext = null;
	}
	
	private boolean isDeclaredInCurrentOrParent(JXPathContext pathContext, String variableName) 
	{
		while (pathContext != null) {
			// Check if the variable is declared in the current context.
			if (!pathContext.getVariables().isDeclaredVariable(variableName)) {
				// Variable not declared in this context.  Move to the parent.
				pathContext = pathContext.getParentContext();
			} else {
				// Variable is declared in this context.
				return true;
			}
		}
		
		// Variable not found in any context.
		return false;
	}
	
	@Test public void testParentVariableVisibility() 
	{
		
		//Boolean testVariableVisible = childContext.getVariables().isDeclaredVariable(TEST_VARIABLE_NAME);
		Boolean testVariableVisible = isDeclaredInCurrentOrParent(childContext, TEST_VARIABLE_NAME);
		
		assertEquals("The parent variables are not visible from the child.", 
				testVariableVisible, Boolean.TRUE);
	}
	
	@Test public void testSetParentVariableFromChild() 
		throws Exception 
	{		
		// Set the value in the child context.
		childContext.setValue("$" + TEST_VARIABLE_NAME, Boolean.TRUE);
		
		Boolean testValue = (Boolean)parentContext.getValue("$" + TEST_VARIABLE_NAME);
		assertEquals("The parent variables are not modifiable from the child.", testValue, Boolean.TRUE);
	}
	
	@Test public void testDeclareParentVariableFromChild() 
		throws Exception 
	{
		// Declare the value in the child context.
		childContext.getVariables().declareVariable(TEST_VARIABLE_NAME, Boolean.TRUE);
		
		Boolean parentValue = (Boolean)parentContext.getValue("$" + TEST_VARIABLE_NAME);
		Boolean childValue = (Boolean)childContext.getValue("$" + TEST_VARIABLE_NAME);
		
		assertEquals("Variables in parent context are overridden the child context.", parentValue, Boolean.FALSE);
		assertEquals("Variables declared in a child context are kept in the child context.", childValue, Boolean.TRUE);
	}
}
