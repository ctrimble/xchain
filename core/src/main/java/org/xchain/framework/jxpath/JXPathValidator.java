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
package org.xchain.framework.jxpath;

import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.XMLConstants;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.*;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.Parser;
import org.apache.commons.jxpath.JXPathException;

/**
 * A static utility for validating the syntax of a JXPath Expression.
 *
 * @author Christian Trimble
 */
public class JXPathValidator
{
  public static Compiler compiler = new TreeCompiler();

  /**
   * Parses the specified xpath and throws a JXPathException if there is a syntax exception.
   */
  public static void validate( String xpath )
  {
    Parser.parseExpression(xpath, compiler);
  }

  public static void validate( String xpath, NamespaceContext xmlns )
  {
    Expression expression = (Expression)Parser.parseExpression(xpath, compiler);
    validateExpression(expression, xmlns);
  }

  static void validateExpression( Expression expression, NamespaceContext xmlns )
  {
    if( expression instanceof Path ) {
      validatePath( (Path)expression, xmlns );
    }
    else if( expression instanceof VariableReference ) {
      validateVariableReference( (VariableReference)expression, xmlns );
    }
    else if( expression instanceof Operation ) {
      validateOperation((Operation)expression, xmlns);
    }
  }

  static void validateExtensionFunction( ExtensionFunction ef, NamespaceContext xmlns )
  {
    QName name = ef.getFunctionName();
    String prefix = name.getPrefix();

    // validate that the prefix is mapped.
    validatePrefix( prefix, xmlns );

    // if the prefix is mapped to an xchain namespace uri, then validate that a function is bound that might match the function.
    // TODO: add hooks for lifecycle based validation.
  }

  static void validatePath( Path path, NamespaceContext xmlns )
  {
    for( Step step : path.getSteps() ) {
      validateStep( step, xmlns );
    }

    if( path instanceof ExpressionPath ) {
      validateExpressionPath( (ExpressionPath)path, xmlns );
    }
  }

  static void validateVariableReference( VariableReference variableReference, NamespaceContext xmlns )
  {
    QName name = variableReference.getVariableName();
    validatePrefix(name.getPrefix(), xmlns);
  }

  static void validateOperation( Operation operation, NamespaceContext xmlns )
  {
    if ( operation.getArguments() != null ) for ( Expression argument : operation.getArguments() ) {
      validateExpression(argument, xmlns);
    }

    if( operation instanceof ExtensionFunction ) {
      validateExtensionFunction((ExtensionFunction)operation, xmlns);
    }
    // ASSERT: all other types of operations do not need validation.
  }

  static void validateStep( Step step, NamespaceContext xmlns )
  {
    validateNodeTest(step.getNodeTest(), xmlns);

    for( Expression predicate : step.getPredicates() ) {
      validateExpression( predicate, xmlns );
    }
  }

  static void validateNodeTest( NodeTest nodeTest, NamespaceContext xmlns )
  {
    if( nodeTest instanceof NodeNameTest ) {
      validateNodeNameTest( (NodeNameTest)nodeTest, xmlns);
    }
    // ASSERT: all other node test types are valid if they parsed.
  }

  static void validateNodeNameTest( NodeNameTest nodeNameTest, NamespaceContext xmlns )
  {
    if( nodeNameTest.isWildcard() ) {
      // TODO: we need to investigate wildcards with namespaces.
    }
    else {
      QName name = nodeNameTest.getNodeName();
      validatePrefix(name.getPrefix(), xmlns);
    }
  }

  static void validateExpressionPath( ExpressionPath ep, NamespaceContext xmlns )
  {
    validateExpression(ep.getExpression(), xmlns);
    for( Expression predicate : ep.getPredicates() ) {
      validateExpression(predicate, xmlns);
    }
  }

  static void validatePrefix( String prefix, NamespaceContext xmlns )
  {
    if( (prefix != null) && (!XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) && XMLConstants.NULL_NS_URI.equals(xmlns.getNamespaceURI(prefix)) ) {
      throw new JXPathException("The prefix '"+prefix+"' is not defined in the current context.");
    }
  }
}
