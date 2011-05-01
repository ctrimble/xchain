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
package org.xchain.framework.sax;

import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.Templates;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import java.net.URL;
import java.util.Iterator;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import org.xchain.framework.lifecycle.XmlFactoryLifecycle;
import org.xchain.framework.sax.SaxTemplates;
import org.xchain.framework.net.UrlFactory;
import org.xchain.framework.net.UrlFactoryUriResolver;
import org.xchain.framework.util.AttributesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Jason Rose
 * @author Josh Kennedy
 */
public class MultiDocumentResult
  extends SAXResult
{
  public static Logger log = LoggerFactory.getLogger( MultiDocumentResult.class );

  public static String MULTI_DOCUMENT_NAMESPACE_URI = "http://www.xchain.org/sax/multi-document/1.0";
  public static String MULTI_DOCUMENT_ELEMENT_LOCAL_NAME = "multi-document";
  public static String DOCUMENT_ELEMENT_LOCAL_NAME = "document";
  public static String SYSTEM_ID_ATTRIBUTE = "system-id";
  public static String PATH_ATTRIBUTE = "path";
  public static String METHOD_ATTRIBUTE = "method";
  public static String DOCTYPE_SYSTEM_ATTRIBUTE = "doctype-system";
  public static String DOCTYPE_PUBLIC_ATTRIBUTE = "doctype-public";
  public static String INDENT_ATTRIBUTE = "indent";
  public static String INDENT_AMOUNT_ATTRIBUTE = "indent-amount";
  public static String OVERWRITE_ATTRIBUTE = "overwrite";
  public static String TEXT_METHOD = "text";
  public static String XML_METHOD = "xml";
  public static String HTML_METHOD = "html";
  public static String DEFAULT_OVERWRITE_VALUE = "false";
  

  MultiDocumentContentHandler multiDocumentContentHandler;
  Templates identityTemplates;

  public MultiDocumentResult()
  {
    multiDocumentContentHandler = new MultiDocumentContentHandler();
    
    setHandler(multiDocumentContentHandler);
    setLexicalHandler(multiDocumentContentHandler);
  }

  public static class MultiDocumentContentHandler
    extends HandlerWrapper
  {
    public static Logger log = LoggerFactory.getLogger( MultiDocumentContentHandler.class );

    // the number of file elements that we have encountered.
    protected int documentElementCount = 0;
    protected PrefixMappingContext prefixMappingContext = null;

    protected void decrementDocumentElementCount()
    {
      documentElementCount--;

      if( log.isDebugEnabled() ) {
        log.debug("The document element count is now "+documentElementCount+".");
      }
    }

    protected void incrementDocumentElementCount()
    {
      documentElementCount++;

      if( log.isDebugEnabled() ) {
        log.debug("The document element count is now "+documentElementCount+".");
      }
    }

    public void startDocument()
      throws SAXException
    {
      if( log.isDebugEnabled() ) {
        log.debug("Start Document Called.");
      }

      // always intercept the start document event, we will create our own on the target content handler. 
      documentElementCount = 0;
      prefixMappingContext = new PrefixMappingContext();
    }

    public void endDocument()
      throws SAXException
    {
      if( log.isDebugEnabled() ) {
        log.debug("End Document Called.");
      }

      // always intercept the end document event, we will create our own on each target content handler.
      documentElementCount = 0;
      prefixMappingContext = null;
    }

    public void startElement( String namespaceUri, String localName, String qName, Attributes attributes )
      throws SAXException
    {
      if( MULTI_DOCUMENT_NAMESPACE_URI.equals(namespaceUri) && DOCUMENT_ELEMENT_LOCAL_NAME.equals(localName) && documentElementCount == 0 ) {
        try {
          if( log.isDebugEnabled() ) {
            log.debug("Starting creation of new multi-document document.");
          }

          // initialize the wrapped handler.
          initializeWrappedHandler(attributes);

          if( log.isDebugEnabled() ) {
            log.debug("Done initializing the wrapped handler.");
          }

          // send the start document event to the handler.
          super.startDocument();

          // send all of the namespace declarations that were defined on the muti-document tag.
          Iterator<String> prefixIterator = prefixMappingContext.prefixSet().iterator();
          while( prefixIterator.hasNext() ) {
            String prefix = prefixIterator.next();
            super.startPrefixMapping(prefix, prefixMappingContext.lookUpNamespaceUri(prefix));
          }

          // send the file element.
          super.startElement( namespaceUri, localName, qName, attributes );
        }
        finally {
          incrementDocumentElementCount();
        }
      }
      else if( MULTI_DOCUMENT_NAMESPACE_URI.equals(namespaceUri) && DOCUMENT_ELEMENT_LOCAL_NAME.equals(localName) ) {
        try {
          // send the file element.
          super.startElement( namespaceUri, localName, qName, attributes );
        }
        finally {
          incrementDocumentElementCount();
        }
      }
      // if we are in the document, then send output to the wrapped class.
      else if( documentElementCount > 0 ) {
        super.startElement( namespaceUri, localName, qName, attributes );
      }
    }

    public void endElement( String namespaceUri, String localName, String qName )
      throws SAXException
    {
      if( MULTI_DOCUMENT_NAMESPACE_URI.equals(namespaceUri) && DOCUMENT_ELEMENT_LOCAL_NAME.equals(localName) && documentElementCount == 1 ) {
        if( log.isDebugEnabled() ) {
          log.debug("Ending multi-document document.");
        }
        decrementDocumentElementCount();

        // end the file element.
        super.endElement(namespaceUri, localName, qName);

        // remove all of the mappings added to the wrapped document.
        Iterator<String> prefixIterator = prefixMappingContext.prefixSet().iterator();
        while( prefixIterator.hasNext() ) {
          String prefix = prefixIterator.next();
          super.endPrefixMapping(prefix);
        }

        // end the document being sent to the wrapped handler.
        super.endDocument();

        // remove the wrapped handler.
        setWrappedHandler(null);
      }
      else if( MULTI_DOCUMENT_NAMESPACE_URI.equals(namespaceUri) && DOCUMENT_ELEMENT_LOCAL_NAME.equals(localName) ) {
        decrementDocumentElementCount();

        super.endElement( namespaceUri, localName, qName );
      }
      else if( documentElementCount > 0 ) {
        super.endElement( namespaceUri, localName, qName );
      }
    }

    public void startPrefixMapping( String prefix, String namespaceUri )
      throws SAXException
    {
      if( documentElementCount == 0 ) {
        prefixMappingContext.pushPrefixMapping( prefix, namespaceUri );
      }
      else {
        super.startPrefixMapping( prefix, namespaceUri );
      }
    }

    public void endPrefixMapping( String prefix )
      throws SAXException
    {
      if( documentElementCount == 0 ) {
        prefixMappingContext.popPrefixMapping( prefix );
      }
      else {
        super.endPrefixMapping(prefix);
      }
    }

    public void initializeWrappedHandler( Attributes attributes )
      throws SAXException
    {
      if( log.isDebugEnabled() ) {
        log.debug("Initializing the wrapped handler.");
      }
      try {
        String systemId = AttributesUtil.getAttribute( attributes, "", SYSTEM_ID_ATTRIBUTE );
        String path = AttributesUtil.getAttribute( attributes, "", PATH_ATTRIBUTE );
        String method = AttributesUtil.getAttribute( attributes, "", METHOD_ATTRIBUTE, XML_METHOD );
        String doctypeSystem = AttributesUtil.getAttribute( attributes, "", DOCTYPE_SYSTEM_ATTRIBUTE );
        String doctypePublic = AttributesUtil.getAttribute( attributes, "", DOCTYPE_PUBLIC_ATTRIBUTE );
        String indent = AttributesUtil.getAttribute( attributes, "", INDENT_ATTRIBUTE );
        String indentAmount = AttributesUtil.getAttribute( attributes, "", INDENT_AMOUNT_ATTRIBUTE );

        if( shouldOverwrite( attributes ) ) {
        StreamResult streamResult = new StreamResult();
        if( systemId != null ) {
          if( log.isDebugEnabled() ) {
            log.debug("Creating result for system-id '"+systemId+"'.");
          }

          URL url = UrlFactory.getInstance().newUrl( systemId );

          // create an output stream for this url.
          OutputStream out = url.openConnection().getOutputStream();

          // create a stream result for the output stream.
          streamResult.setSystemId(systemId);
          streamResult.setOutputStream(out);
        }
        else if( path != null ) {
          if( log.isDebugEnabled() ) {
            log.debug("Creating result for path '"+path+"'.");
          }

          File file = new File( path );

          File parentFile = file.getParentFile();
          if( !parentFile.exists() ) {
            parentFile.mkdirs();
          }

          file.createNewFile();

          streamResult.setSystemId(file.toURL().toExternalForm());
          streamResult.setOutputStream(new FileOutputStream(file));
        }
        else {
          throw new SAXException("The document element of a multi-document must have the system-id attribute or the path attribute.");
        }

        // create a transformer handler that removes the document element, and sets all of the document information.
        TransformerHandler transformerHandler = createTransformerHandler( method, doctypePublic, doctypeSystem, indent, indentAmount );

        // attach the stream handler to the result.
        transformerHandler.setResult( streamResult );

        if( log.isDebugEnabled() ) {
          log.debug("Setting the transformerHandler as the wrapped handler.");
        }

        // set the fileContentHandler as the current handler strategy.
        setWrappedHandler(transformerHandler);
        }
      }
      catch( Exception e ) {
        if( log.isWarnEnabled() ) {
          log.warn("Could not initialize the wrapped handler", e);
        }
        throw new SAXException("Could not initialize wrapped handler.", e);
      }
    }
  }
  public static boolean shouldOverwrite( Attributes attributes )
    throws Exception
  {
    String systemId = AttributesUtil.getAttribute( attributes, "", SYSTEM_ID_ATTRIBUTE );
    String path = AttributesUtil.getAttribute( attributes, "", PATH_ATTRIBUTE );
    String overwrite = AttributesUtil.getAttribute( attributes, "", OVERWRITE_ATTRIBUTE, DEFAULT_OVERWRITE_VALUE );

    boolean overwriteFlag = Boolean.valueOf(overwrite).booleanValue();

    if( overwriteFlag ) {
      return true;
    }

    if( systemId != null ) {
      if( log.isWarnEnabled() ) {
        log.warn("Did not write document to system id '"+systemId+"'.  System id docurments are currently not overwritten when overwrite is false.");
      }
      return false;
    }

    if( path != null ) {
      File file = new File(path);

      if( !file.exists() ) {
        return true;
      }
      else {
        if( log.isDebugEnabled() ) {
          log.debug("Did not write document to path '"+path+"', it already exists and the overwrite flag is set to '"+overwriteFlag+"'.");
        }
        return false;
      }
    }

    if( log.isWarnEnabled() ) {
      log.warn("The overwrite flag is '"+overwriteFlag+"' and no path or system-id could be found, so no output is being generated.");
    }

    return false;
  }

  public static TransformerHandler createTransformerHandler( String method, String doctypePublicId, String doctypeSystemId, String indent, String indentAmount )
    throws Exception
  {
    if( log.isDebugEnabled() ) {
      log.debug("Creating transformer handlers.");
    }

    String systemId = "resource://context-class-loader/org/xchain/namespaces/sax/mutil-document-format.xsl";

    SaxTemplates templates = XmlFactoryLifecycle.newTemplates(systemId);

    if( log.isDebugEnabled() ) {
      log.debug("Loaded the templates object.");
    }

    // create the transformer handler for the templates object.
    TransformerHandler transformerHandler = templates.newTransformerHandler();

    // get the transformer from the transformer handler  object.
    Transformer transformer = transformerHandler.getTransformer();

    // set the output method.
    transformer.setOutputProperty("method", method);

    // set the output properties for xml and html documents.
    if( XML_METHOD.equals(method) || HTML_METHOD.equals(method) ) {
      if( doctypePublicId != null ) {
        transformer.setOutputProperty("doctype-public", doctypePublicId);
      }
      if( doctypeSystemId != null ) {
        transformer.setOutputProperty("doctype-system", doctypeSystemId);
      }
      if( indent != null ) {
        transformer.setOutputProperty("indent", indent);
      }
      if( indentAmount != null ) {
        transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", indentAmount);
      }
    }

    transformer.setURIResolver(new UrlFactoryUriResolver());

    if( log.isDebugEnabled() ) {
      log.debug("Transformer created.");
    }

    return transformerHandler;
  }
}
