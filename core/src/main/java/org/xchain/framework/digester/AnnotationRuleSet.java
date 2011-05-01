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
package org.xchain.framework.digester;

import org.apache.commons.digester.RuleSetBase;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.WithDefaultsRulesWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xchain.AttributeDetail;
import org.xchain.Catalog;
import org.xchain.Chain;
import org.xchain.Command;
import org.xchain.EngineeredCatalog;
import org.xchain.EngineeredCommand;
import org.xchain.Locatable;
import org.xchain.Registerable;
import org.xchain.annotations.Element;
import org.xchain.annotations.ParentElement;
import javax.xml.namespace.QName;
import javax.xml.XMLConstants;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static org.xchain.framework.util.AnnotationUtil.*;
import org.xchain.framework.lifecycle.Lifecycle;
import org.xchain.framework.lifecycle.LifecycleContext;
import org.xchain.framework.lifecycle.LifecycleClassLoader;
import org.xchain.framework.lifecycle.NamespaceContext;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;
import org.xml.sax.ext.Locator2;
import org.xml.sax.ext.Locator2Impl;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
public class AnnotationRuleSet
  extends RuleSetBase
{
  /** The log for this class. */
  public static final Logger log = LoggerFactory.getLogger( AnnotationRuleSet.class );

  /** The namespace for XChain command and catalog elements - {@value}.*/
  public static final String NAMESPACE_URI    = "http://www.xchain.org/core/1.0";

  /** The local name of the name attribute - {@value}. */
  public static final String NAME_ATTRIBUTE       = "name";
  
  public static final QName NAME_QNAME = new QName(NAMESPACE_URI, NAME_ATTRIBUTE);

  public static String getAttribute( Attributes attributes, String namespace, String localName, String defaultValue )
  {
    String value = defaultValue;

    int index = attributes.getIndex(namespace, localName);
    if( index != -1 ) {
      value = attributes.getValue(index);
    }

    return value;
  }

  private String systemId = null;

  public AnnotationRuleSet(String systemId)
  {
    this.systemId = systemId;
  }

  public void addRuleInstances(Digester digester)
  {
    // log what we are attempting to do.

    // set up the namespace in the digester.
    digester.setNamespaceAware(true);

    // add the place holder rule used to pass mappings when there is not an element to pass them on...
    // TODO: add code for the place holder command.

    LifecycleContext lifecycleContext = Lifecycle.getLifecycleContext();

    for( NamespaceContext namespaceContext : lifecycleContext.getNamespaceContextMap().values() ) {
      digester.setRuleNamespaceURI(namespaceContext.getNamespaceUri());

      for( Class classObject : namespaceContext.getCatalogList() ) {
        // for catalogs that we find, we need to add a create rule, a properties rule, a set next rule, and a registration rule.
        addRulesForCatalog(digester, classObject, lifecycleContext);
      }

      for( Class classObject : namespaceContext.getCommandList() ) {
        // for commands that we find, we need to add a create rule, a properties rule, a set next rule, and a registration rule.
        addRulesForCommand(digester, lifecycleContext, systemId, classObject);
      }
    }

    WithDefaultsRulesWrapper defaults = new WithDefaultsRulesWrapper(digester.getRules());
    defaults.addDefault(new UnknownElementRule());
    digester.setRules(defaults);
  }

  public static void addRulesForCatalog( Digester digester, Class classObject, LifecycleContext lifecycleContext )
  {
    String localName = ((Element)classObject.getAnnotation(Element.class)).localName();
    digester.addRule( localName, new ClassObjectCreateRule( classObject ) );
    // set the class loader.
    digester.addRule( localName, new SetCatalogClassLoader( lifecycleContext ) );
  }

  public static void addRulesForCommand( Digester digester, LifecycleContext lifecycleContext, String systemId, Class classObject )
  {
    Element element = (Element)classObject.getAnnotation(Element.class);
    List<String> commandPathList = null;

    try {
    // build a list of all the possible mappings for this class.
      commandPathList = getCommandPathList(lifecycleContext, classObject);
    }
    catch( Throwable t ) {
      t.printStackTrace();
    }

    Rule createRule = new ClassObjectCreateRule( classObject );
    Rule prefixMappingRule = new CommandPrefixMappingRule();
    Rule setAttributeRule = new SetCommandAttributeRule();
    Rule nextCommandRule = new CommandSetNextRule();
    Rule commandRegistrationRule = new CommandRegistrationRule(systemId);

    for( String commandPath : commandPathList ) {
      digester.addRule( "*"+commandPath, createRule );
      digester.addRule( "*"+commandPath, prefixMappingRule );
      digester.addRule( "*"+commandPath, setAttributeRule );
      digester.addRule( "*"+commandPath, nextCommandRule );
      if( element.parentElements().length == 0 ) {
        digester.addRule( "*"+commandPath, commandRegistrationRule );
      }
    }
  }

  public static List<String> getCommandPathList( LifecycleContext lifecycleContext, Class classObject )
  {
    // get the current element.
    Element element = (Element)classObject.getAnnotation(Element.class);

    List<String> parentPaths = new ArrayList<String>();

    // if the element has parent elements, then create a path for each parent element.
    for( ParentElement parentElement : element.parentElements() ) {
      // get the element namespace for the parent element.
      NamespaceContext namespaceContext = lifecycleContext.getNamespaceContextMap().get(parentElement.namespaceUri());

      if( namespaceContext == null ) {
        break;
      }

      // get the class for the command.
      for( Class commandClass : namespaceContext.getCommandList() ) {
        Element namespaceCommandElement = (Element)commandClass.getAnnotation(Element.class);
        if( namespaceCommandElement.localName().equals(parentElement.localName()) ) {
          parentPaths.addAll(getCommandPathList( lifecycleContext, commandClass ));
          break;
        }
      }
    }

    // get this elements local name.
    String localName = element.localName();

    List<String> pathList = new ArrayList<String>();

    if( parentPaths.isEmpty() ) {
      pathList.add("/"+localName);
    }

    // if the element has parent elements, then create a path for each parent element.
    for( String parentPath : parentPaths ) {
      pathList.add(parentPath+"/"+localName);
    }

    return pathList;
  }

  public static class UnknownElementRule
    extends Rule
  {
    public void begin( String namespaceURI, String name, Attributes attributes )
      throws Exception
    {
      QName elementQName = new QName(namespaceURI, name);

      StringBuffer sb = new StringBuffer();
      sb.append("Could not find command for ").append(elementQName).append(".\n");

      // if this is a namespace that is defined, then display a message about the namespace.
      NamespaceContext namespaceContext = Lifecycle.getLifecycleContext().getNamespaceContextMap().get(namespaceURI);

      if( namespaceContext != null ) {
        sb.append("The following commands are defined for this namespace '"+namespaceContext.getNamespaceUri()+"'.\n");
        for( Class<?> commandClass : namespaceContext.getCommandList() ) {
          QName commandQName = new QName(namespaceContext.getNamespaceUri(), commandClass.getAnnotation(org.xchain.annotations.Element.class).localName());
          sb.append("  ").append(commandQName).append("\n");
        }
      }
      else {
        sb.append("There are no commands defined in the namespace '").append(namespaceURI).append("'.  If you are tring to create an output element, ");
        sb.append("make sure it is wrapped in an '{http://www.xchain.org/jsl/1.0}template' element.\n");
      }
      throw new XChainParseException(getDigester().getDocumentLocator(), sb.toString());
    }
  }

  /**
   * A create rule that takes the class of the object to create.  This class was selected over ObjectCreateRule, since it allows the
   * Class object to come from a class loader other than the digesters class loader.
   */
  public static class ClassObjectCreateRule
    extends Rule
  {
    protected Class classObject;

    public ClassObjectCreateRule( Class classObject )
    {
      this.classObject = classObject;
    }

    /**
     * Creates a new instance of the class and places it on the top of the stack.
     */
    public void begin( String namespaceURI, String name, Attributes attributes )
      throws Exception
    {
      // get the class from the class loader.
      Object instance = classObject.newInstance();

      if( instance instanceof Locatable ) {
        Locatable locatable = (Locatable)instance;
        Locator locator = digester.getDocumentLocator();

        if( locator == null ) {
          locatable.setLocator(new LocatorImpl());
        } 
        else if( locator instanceof Locator2 ) {
          locatable.setLocator(new Locator2Impl((Locator2)locator));
        }
        else {
          locatable.setLocator(new LocatorImpl(locator));
        }
      }

      // push the class onto the stack.
      digester.push( instance );
      
    }

    /**
     * Removes the instance of the class from the top of the stack.
     */
    public void end()
      throws Exception
    {
      digester.pop();
    }
  }

  public static class CommandPrefixMappingRule
    extends Rule
  {

    public void begin( String namespaceURI, String name, Attributes attributes )
      throws Exception
    {
      Object top = digester.peek();
      if( top instanceof EngineeredCommand ) {
        ((EngineeredCommand)top).getPrefixMap().putAll(digester.getCurrentNamespaces());
      }
    }
  }

  public static class SetCommandAttributeRule
    extends Rule
  {
    public void begin( String namespaceUri, String name, Attributes attributes )
      throws Exception
    {
      Object top = digester.peek();
      if( top instanceof EngineeredCommand ) {
        Map<QName, AttributeDetail> attributeDetailMap = ((EngineeredCommand)top).getAttributeDetailMap();
        Map<QName, String> attributeMap = ((EngineeredCommand)top).getAttributeMap();
        QName elementQName = new QName(namespaceUri, name);
        for( int i = 0; i < attributes.getLength(); i++ ) {
          QName attribute = new QName(attributes.getURI(i), attributes.getLocalName(i));

          // if the attribute is not defined, then we have an error.
          if ( !attributeDetailMap.containsKey(attribute) && !attribute.equals(NAME_QNAME)) {
            StringBuffer sb = new StringBuffer();
            sb.append("Unknown attribute '").append(attribute).append("' found on element '").append(elementQName).append("'.\n");
            sb.append("Valid attributes are:\n");
            for( QName validAttribute : attributeDetailMap.keySet() ) {
              sb.append("  ").append(validAttribute).append("\n");
            }
            throw new XChainParseException(getDigester().getDocumentLocator(), sb.toString());
          }
          else if (attribute.equals(NAME_QNAME)) {
            // SyntaxUtil.validateQName(attributes.getValue(), getDigester().getDocumentLocator() );
            attributeMap.put(attribute, attributes.getValue(i));
          }
          else {
            // we need to validate the attribute here.
            // we need to know the attrubute type and there needs to be a generic way to pass that attribute name and type into a
            // a utility method that does the validating, or the attribute type needs to know how to do the validating.
            try {
              attributeDetailMap.get(attribute).getType().validate(attributes.getValue(i), new DigesterNamespaceContext(getDigester()));
            }
            catch( Exception e ) {
              throw new XChainParseException(getDigester().getDocumentLocator(), "Invalid attribute value for "+attribute+":"+e.getMessage());
            }
            attributeMap.put(attribute, attributes.getValue(i));
          }
        }
        // check for required attributes that are not defined.
        for( Map.Entry<QName, AttributeDetail> entry : attributeDetailMap.entrySet() ) {
          if( entry.getValue().getRequired() && !attributeMap.containsKey(entry.getKey()) ) {
            throw new XChainParseException(getDigester().getDocumentLocator(), "The attribute "+entry.getKey()+" is required for element "+elementQName+".");
          }
        }
      }
    }
  }

  /**
   * If the object at the top of the stack is an instance of Command and the object above it is an instance of Chain, then this rule adds the command
   * to the chain.
   */
  public static class CommandSetNextRule
    extends Rule
  {
    public void end( String namespace, String name )
    {
      Object top       = digester.peek();
      Object nextToTop = digester.peek(1);

      if( top != null && top instanceof Command && nextToTop != null && nextToTop instanceof Chain ) {
        ((Chain)nextToTop).addCommand((Command)top);
      }
    }
  }

  /**
   * Registers a command with the inner most catalog tag in the input document.
   */
  public static class CommandRegistrationRule
    extends Rule
  {
    protected int depth = 0;
    protected QName qName = null;
    protected String systemId = null;

    public CommandRegistrationRule( String systemId )
    {
      this.systemId = systemId;
    }

    public void begin( String namespaceURI, String name, Attributes attributes )
      throws Exception
    {
      // record the name of the command.
      String commandName = null;

      int commandNameIndex = attributes.getIndex(NAMESPACE_URI, NAME_ATTRIBUTE);
      if( commandNameIndex != -1 ) {
        commandName = attributes.getValue(commandNameIndex);
      }

      if( commandName != null && depth == 0 ) {
        if( commandName.matches(".*:.*") ) {
          String[] splitName = commandName.split(":", 2);
          String namespaceUri = (String)getDigester().getCurrentNamespaces().get(splitName[0]);
          if( namespaceUri == null ) {
            throw new XChainParseException(getDigester().getDocumentLocator(), "Could not find a namespace for the prefix '"+splitName[0]+"'.");
          }
          qName = new QName(namespaceUri, splitName[1]);
        }
        else {
          qName = new QName(XMLConstants.NULL_NS_URI, commandName);
        }
      }
      else if( commandName != null ) {
        throw new XChainParseException(getDigester().getDocumentLocator(), "Named commands may not be nested in other commands.");
      }

      depth++;
    }

    public void end( String namespaceURI, String name )
      throws Exception
    {
      depth--;
      if( qName != null && depth == 0 ) {
        // get the command that is being constructed.
        Catalog catalog = findCatalogInStack();

        if( catalog != null ) {
          // get the command on the top of the stack.
          Command command = (Command)digester.peek();

          if( command instanceof Registerable ) {
            Registerable registerable = (Registerable)command;
            registerable.setQName(qName);
            registerable.setSystemId(systemId);
          }

          // register the command with the catalog.
          catalog.addCommand(qName, command);
        }
        else {
          throw new XChainParseException(getDigester().getDocumentLocator(), "Command name '"+qName+"' defined outside the scope of a catalog.");
        }
      }

      if( depth == 0 ) {
        qName = null;
      }
    }

    /**
     * Finds the catalog that is closest to the top of the digesters stack.
     */
    protected Catalog findCatalogInStack()
    {
      Catalog catalog = null;

      if( log.isDebugEnabled() ) {
        log.debug("digester.getCount() = "+digester.getCount());
      }

      for( int i = 0; i < digester.getCount() && catalog == null; i++ ) {
        Object peeked = digester.peek(i);

        if( log.isDebugEnabled() ) {
          log.debug("Looking for catalog in digester stack at index "+i+".  Found object of type '"+peeked.getClass().getName()+"'.");
        }

        if( peeked instanceof Catalog ) {
          catalog = (Catalog)peeked;
        }
      }

      return catalog;
    }
  }

  /**
   */
  public static class SetCatalogClassLoader
    extends Rule
  {
    protected LifecycleContext lifecycleContext = null;
    public SetCatalogClassLoader( LifecycleContext lifecycleContext )
    {
      this.lifecycleContext = lifecycleContext;
    }

    public void begin( String namespaceURI, String name, Attributes attributes )
      throws Exception
    {
      Catalog catalog = findCatalogInStack();
      if( catalog instanceof EngineeredCatalog ) {
        ((EngineeredCatalog)catalog).setClassLoader(new LifecycleClassLoader(lifecycleContext.getClassLoader()));
      }
    }
    /**
     * Finds the catalog that is closest to the top of the digesters stack.
     */
    protected Catalog findCatalogInStack()
    {
      Catalog catalog = null;

      if( log.isDebugEnabled() ) {
        log.debug("digester.getCount() = "+digester.getCount());
      }

      for( int i = 0; i < digester.getCount() && catalog == null; i++ ) {
        Object peeked = digester.peek(i);

        if( log.isDebugEnabled() ) {
          log.debug("Looking for catalog in digester stack at index "+i+".  Found object of type '"+peeked.getClass().getName()+"'.");
        }

        if( peeked instanceof Catalog ) {
          catalog = (Catalog)peeked;
        }
      }

      return catalog;
    }
  }
}
