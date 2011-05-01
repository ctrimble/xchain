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
package org.xchain.framework.doclets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xchain.Catalog;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.Element;
import org.xchain.annotations.Namespace;
import org.xchain.framework.util.HtmlUtil;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;

/**
 * Doclet for producing documentation on catalogs and commands available in namespaces.
 *
 * @author Devon Tackett
 * @author Christian Trimble
 */
public class NamespaceDoclet {
    private static Pattern localNamePattern;
    private static ClassDoc catalogDoc;
    
    private static final String XDOC_PATH = "./xdoc/namespaces/";
    private static final String APT_PATH = "./apt/namespaces/";
  
    public static boolean start(RootDoc root) {
      
      StringBuffer namespaceData;

      if (!makeDirectories(XDOC_PATH))
        return false;
      
      if (!makeDirectories(APT_PATH))
        return false;
      
      try {
        copyAttributeTypeDoc();
      } catch (Exception e) {
        System.out.println("Could not load attribute type documentation.");
        e.printStackTrace();
        return false;
      }

      // create the templates.
      Templates templates = null;
      try {
        templates = loadTemplates();
      }
      catch( Exception e ) {
        System.out.println("Could not load stylesheet for xchain namespace documentation.");
        e.printStackTrace();
        return false;
      }
      
      // Compile the localName regex pattern.
      localNamePattern = Pattern.compile(".*localName=\"([^\"]*)\".*");
      // Find the Catalog classDoc
      catalogDoc = root.classNamed(Catalog.class.getName());
      
      // Process all the packages.
      for (PackageDoc packageDoc : root.specifiedPackages()) {
        if (isNamespacePackage(packageDoc)) {
          try {
            namespaceData = new StringBuffer();
            
            // Hard coded hack to ignore the jsl namespace.  It'll need to be built by hand.
            if (getUnqualifiedName(packageDoc.name()).equalsIgnoreCase("jsl"))
                continue;
            
            File xdocFile = new File(XDOC_PATH + getUnqualifiedName(packageDoc.name())+ ".xml");
            System.out.println("Generating " + xdocFile.getAbsolutePath() + "...");
            
            // Build the xdoc
            // Add the XML header
            namespaceData.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            namespaceData.append("<document>\n");
            namespaceData.append(createIndent(1)).append("<properties>\n");
            // Set the title to the package name.
            namespaceData.append(createIndent(2)).append("<title>").append(getUnqualifiedName(packageDoc.name())).append(" namespace</title>\n");
            namespaceData.append(createIndent(1)).append("</properties>\n");
            namespaceData.append(createIndent(1)).append("<body>\n");
            
            namespaceData.append(createIndent(1)).append("<section name=\"").append(getUnqualifiedName(packageDoc.name())).append(" Namespace\">\n");
            
            namespaceData.append(createIndent(1)).append("<p>Namespace URI: ").append(getNamespaceUri(packageDoc)).append("</p>");
            
            // Namespace comments.            
            namespaceData.append(createIndent(2)).append(HtmlUtil.htmlFragmentToXmlFragment(packageDoc.commentText()));
            
            Map<String, Entry> commandMap = new HashMap<String, Entry>();
            Map<String, Entry> catalogMap = new HashMap<String, Entry>();
            
            // Build the command map for all the catalogs and commands in the namespace.
            for (ClassDoc classDoc : packageDoc.allClasses()) {
              if (isElement(classDoc)) {
                createEntries(commandMap, catalogMap, classDoc);
              }
            }            
            
            namespaceData.append(createIndent(1)).append("</section>\n");
            
            // Catalogs.
            if (!catalogMap.isEmpty()) {
              namespaceData.append(createIndent(2)).append("<section name=\"Available Catalogs\">\n");            
              writeMap(catalogMap, namespaceData);            
              namespaceData.append(createIndent(2)).append("</section>\n");
            }
            
            // Commands.
            if (!commandMap.isEmpty()) {
              namespaceData.append(createIndent(2)).append("<section name=\"Available Commands\">\n");            
              writeMap(commandMap, namespaceData);            
              namespaceData.append(createIndent(2)).append("</section>\n");
            }
            
            namespaceData.append(createIndent(1)).append("</body>\n");
            namespaceData.append("</document>\n");

            xdocFile.createNewFile();
            writeData( templates, xdocFile, namespaceData.toString() );
          } catch (Exception ex) {
            System.out.println("Exception found.");
            ex.printStackTrace();
            return false;
          }
        }
      }

      return true;
    }
    
    /**
     * Get the namespace uri for the given packageDoc.
     * 
     * @param packageDoc The packageDoc to check.
     * 
     * @return The namespace uri for the packageDoc.
     */
    private static String getNamespaceUri(PackageDoc packageDoc) {
      AnnotationDesc annoDesc = getAnnotation(packageDoc.annotations(), Namespace.class);
      
      return getNamespaceAttribute(annoDesc);
    }
    
    /**
     * Append entry information to the given StringBuffer.
     * 
     * @param map The map of entries to append.
     * @param doc The StringBuffer to build upon.
     */
    private static void writeMap(Map<String, Entry> map, StringBuffer doc) {
      // Write out the entries in a sorted order.
      TreeSet<String> nameSet = new TreeSet<String>(map.keySet());
      
      for(String name : nameSet) {
        appendEntity(doc, map.get(name), 3);
      }         
    }    
    
    /**
     * Append the given entry to the given StringBuffer.
     * 
     * @param doc The StringBuffer to append to.
     * @param command The entry to append.
     * @param currentDepth The tab depth for the entry.  Used for document cosmetic purposes.
     */
    private static void appendEntity(StringBuffer doc, Entry command, int currentDepth) {
      doc.append("<subsection name=\"").append(command.getName()).append("\">\n");
      
      doc.append(command.getContent()).append("\n");
      
      TreeSet<String> subCommands = new TreeSet<String>(command.getSubEntries().keySet());
      
      for(String subCommandName : subCommands) {
        appendEntity(doc, command.getSubEntries().get(subCommandName), currentDepth + 1);
      }
      
      doc.append("</subsection>");
    }
    
    /**
     * Get an unqualified name from a fully qualified name.
     * 
     * @param name The fully qualified name.
     * 
     * @return The unqualified name.
     */
    private static String getUnqualifiedName(String name) {
      int lastIndex = name.lastIndexOf(".");
      if (lastIndex != -1)
        name = name.substring(lastIndex+1);

      return name;
    }
    
    /**
     * Add a catalog or command to the given maps.
     * 
     * @param commandMap The currently known command map.
     * @param catalogMap The currently known catalog map.
     * @param entityDoc The entity to parse.
     */
    private static void createEntries(Map<String, Entry> commandMap, Map<String, Entry> catalogMap, ClassDoc entityDoc)
      throws Exception
    {
      // Create a new entity.
      Entry entity = new Entry();
      // Set the name.
      entity.setName(getElementName(entityDoc));
      // Create the content.
      StringBuffer entityContent = new StringBuffer();
      
      if (hasAttributes(entityDoc)) {
        // Set the attributes.
        entityContent.append(createIndent(4)).append("Attributes\n");
        entityContent.append(createIndent(4)).append("<table>\n");
        entityContent.append(createIndent(5)).append("<tr><th>Name</th><th>Description</th><th>Type</th><th>Default Value</th><th>Java Return Type</th></tr>\n");
        
        appendAttributes(entityContent, entityDoc);
        
        entityContent.append(createIndent(4)).append("</table>\n");
      }
      
      // Grab the javadoc comments.
      entityContent.append(HtmlUtil.htmlFragmentToXmlFragment(entityDoc.commentText()));
      
      // Set the content on the command.
      entity.setContent(entityContent);
      
      Map<String, Entry> entityMap;
      
      if (isCatalog(entityDoc))
        entityMap = catalogMap;
      else
        entityMap = commandMap;
      
      String parentEntityName = getParentEntity(entityDoc);
      // Check if the entity is a sub entity.
      if (parentEntityName != null && parentEntityName.trim().length() != 0) {
        Entry parentCommand = entityMap.get(parentEntityName);
        parentCommand.addSubEntry(entity);
      } else {        
        // Add the entity to the entity map.
        entityMap.put(entity.getName(), entity);
      }
    }
    
    /**
     * Get the name of the parent element.  Null if there is no parent element.
     */
    private static String getParentEntity(ClassDoc commandDoc) {
      AnnotationDesc annoDesc = getAnnotation(commandDoc.annotations(), Element.class);      
      
      for (ElementValuePair elementValue : annoDesc.elementValues()) {
        if (elementValue.element().name().equalsIgnoreCase("parentElements")) {
          Matcher matcher = localNamePattern.matcher(elementValue.value().toString());
          
          if (matcher.matches())
              return matcher.group(1);
        }
      }
      
      return null;     
    }
    
    /**
     * Determine if the given ClassDoc is a Catalog.
     */
    private static boolean isCatalog(ClassDoc entityDoc) {
      return entityDoc.subclassOf(catalogDoc);
    }
    
    /**
     * Add all the attributes for the given class to the given StringBuffer.
     * 
     * @param doc The StringBuffer to append to.
     * @param command The command to inspect.
     */
    private static void appendAttributes(StringBuffer doc, ClassDoc command) {
      // Get attributes from interfaces.
      for (ClassDoc parentClassDoc : command.interfaces()) {
        appendAttributes(doc, parentClassDoc);
      }
      
      // Get attributes from super class.
      if (command.superclass() != null) {
        appendAttributes(doc, command.superclass());
      }
      
      for(MethodDoc methodDoc : command.methods()) {
        if (isAttribute(methodDoc)) {
          AnnotationDesc annoDesc = getAnnotation(methodDoc.annotations(), Attribute.class);            
          doc.append(createIndent(5)).append("<tr>");
          doc.append("<td>").append(getAttributeName(annoDesc)).append("</td>");
          doc.append("<td>").append(methodDoc.commentText()).append("</td>");
          String attributeType = getAttributeType(annoDesc);
          doc.append("<td><a href=\"./attributetypes.html#").append(attributeType).append("\">").append(attributeType).append("</a></td>");
          doc.append("<td>").append(getAttributeDefaultValue(annoDesc)).append("</td>");
          doc.append("<td>").append(methodDoc.returnType().typeName()).append("</td>");
          doc.append("</tr>\n");
        }
      }        
    }
    
    /**
     * Get the name of the Attribute annotation.
     */
    private static String getAttributeName(AnnotationDesc annoDesc) {
      for (ElementValuePair elementValue : annoDesc.elementValues()) {
        if (elementValue.element().name().equalsIgnoreCase("localName"))
          return elementValue.value().toString().replaceAll("\"", "");
      }
      
      return "unknown";
    }
    
    /**
     * Get the Attribute annotation type. 
     */
    private static String getAttributeType(AnnotationDesc annoDesc) {
      for (ElementValuePair elementValue : annoDesc.elementValues()) {
        if (elementValue.element().name().equalsIgnoreCase("type"))
          return elementValue.value().toString().replaceAll("\"", "");
      }
      
      return "unknown";      
    }
    
    /**
     * Get the Attribute annotation default value. 
     */
    private static String getAttributeDefaultValue(AnnotationDesc annoDesc) {
      for (ElementValuePair elementValue : annoDesc.elementValues()) {
        if (elementValue.element().name().equalsIgnoreCase("defaultValue"))
          return elementValue.value().toString().replaceAll("\"", "").replace("\'", "");
      }
      
      return "N/A";
    }
    
    /**
     * Get the Namespace annotation uri.
     */
    private static String getNamespaceAttribute(AnnotationDesc annoDesc) {
      for (ElementValuePair elementValue : annoDesc.elementValues()) {
        if (elementValue.element().name().equalsIgnoreCase("uri"))
          return elementValue.value().toString().replaceAll("\"", "");
      }
      
      return "unspecified";
    }
    
    /**
     * Get the command name for the given class.
     * 
     * @param command The class command.
     * 
     * @return The name of the command.
     */
    private static String getElementName(ClassDoc command) {
      for (AnnotationDesc annotationDesc : command.annotations()) {
        if (annotationDesc.annotationType().qualifiedName().equals(Element.class.getName())) {
          for (ElementValuePair elementValue : annotationDesc.elementValues()) {
            return elementValue.value().toString().replaceAll("\"", "");
          }
        }
      }   
      
      return "unknown";
    }
    
    /**
     * Determine if the given class has any Attributes.
     * 
     * @param classDoc The class to check.
     * 
     * @return True if the class has any attributes.
     */
    private static boolean hasAttributes(ClassDoc classDoc) {
      for (MethodDoc methodDoc : classDoc.methods()) {
        if (isAttribute(methodDoc))
          return true;
      }
      
      for (ClassDoc parentClassDoc : classDoc.interfaces()) {
        if (hasAttributes(parentClassDoc))
          return true;
      }
      
      if (classDoc.superclass() != null) {
        return hasAttributes(classDoc.superclass());
      }
      
      return false;
    }
    
    /**
     * Determine if any of the annotations in the given array are of the given class.
     * 
     * @param annotations The array of annotations to check.
     * @param annotationClass The class to check for.
     * 
     * @return True if any of the annotations in the given array are of the given class.  False otherwise.
     */
    private static boolean hasAnnotation(AnnotationDesc[] annotations, Class annotationClass) {
      for (AnnotationDesc annotationDesc : annotations) {
        if (annotationDesc.annotationType().qualifiedName().equals(annotationClass.getName())) {
          return true;
        }
      }
      
      return false;
    }
    
    /**
     * Get the annotation type from the array of annotations.
     * 
     * @param annotations The array of annotations to search.
     * @param annotationClass The class to check for.
     * 
     * @return The annotation desc for the annotation or null if not found.
     */
    public static AnnotationDesc getAnnotation(AnnotationDesc[] annotations, Class annotationClass) {
      for (AnnotationDesc annotationDesc : annotations) {
        if (annotationDesc.annotationType().qualifiedName().equals(annotationClass.getName())) {
          return annotationDesc;
        }
      }
      
      return null;
    }
    
    /**
     * Determine if the given packages is a Namespace.
     * 
     * @param packageDoc The package to check.
     * 
     * @return True if the given package is a Namespace.
     */
    private static boolean isNamespacePackage(PackageDoc packageDoc) {
      return hasAnnotation(packageDoc.annotations(), Namespace.class);
    }
    
    /**
     * Determine if the given class is an Element.
     * 
     * @param classDoc The class to check.
     * 
     * @return True if the given class is an Element.
     */
    private static boolean isElement(ClassDoc classDoc) {
      return hasAnnotation(classDoc.annotations(), Element.class);
    }

    
    /**
     * Determine if the given method is an Attribute.
     * 
     * @param methodDoc The method to check.
     * 
     * @return True if the given method is an Attribute.
     */
    private static boolean isAttribute(MethodDoc methodDoc) {
      return hasAnnotation(methodDoc.annotations(), Attribute.class);
    }
    
    /**
     * Make sure the give path exists.
     */
    private static boolean makeDirectories(String path) {
      // make sure that the base directory exists.
      File rootDirectory = new File(path);

      try {
        System.out.println("Generated namespace document to '"+rootDirectory.getAbsoluteFile()+"'.");
        if( rootDirectory.mkdirs() ) {
          System.out.println("Directory '"+rootDirectory.getAbsoluteFile()+"' was created for the namespace documentation.");
        }
      }
      catch( SecurityException se ) {
        System.out.println("Could not create the namespace documentation root directory.");
        se.printStackTrace();
        return false;
      }
      
      return true;
    }
    
    /**
     * Create a string of '\t' for the given indent count.
     */
    private static String createIndent(int indent) {
      StringBuffer indentString = new StringBuffer();
      while (indent-- > 0) {
        indentString.append("\t");
      }
      
      return indentString.toString();
    }

    /**
     * Load the namespace doclet template.
     */
    private static Templates loadTemplates()
      throws TransformerConfigurationException, IOException, InstantiationException, IllegalAccessException
    {
      TransformerFactory factory = net.sf.saxon.TransformerFactoryImpl.class.newInstance();

      // get the template as a resource.
      URLConnection urlConnection = Thread.currentThread().getContextClassLoader().getResource("org/xchain/framework/doclets/namespace-doclet.xsl").openConnection();
      
      try {
        StreamSource streamSource = new StreamSource();
        streamSource.setInputStream(urlConnection.getInputStream());
        Templates templates = factory.newTemplates(streamSource);
        return templates;
      }
      finally {
        close(urlConnection.getInputStream());
      }
    }

    private static void writeData( Templates templates, File xdocFile, String data )
      throws TransformerException, IOException
    {
      // create the source.
      StreamSource source = new StreamSource();
      source.setReader(new StringReader(data));

      // create the result.
      StreamResult result = new StreamResult();
      result.setWriter(new FileWriter(xdocFile));

      // create the transformer.
      Transformer transformer = templates.newTransformer();

      // do the transformation.
      transformer.transform( source, result );
    }
    
    private static void copyAttributeTypeDoc()
      throws IOException
    {
      URLConnection urlConnection = Thread.currentThread().getContextClassLoader().getResource("org/xchain/framework/doclets/attributetypes.apt").openConnection();
      
      try {               
        File attributeTypeFile = new File(APT_PATH + "attributetypes.apt");
        
        attributeTypeFile.createNewFile();
        
        FileOutputStream fileOutput = new FileOutputStream(attributeTypeFile);
        
        InputStream input = urlConnection.getInputStream();
        
        int readBytes = -1;
        byte[] byteBuffer = new byte[1024];
        
        do {
          readBytes = input.read(byteBuffer);
          
          if (readBytes != -1)
            fileOutput.write(byteBuffer, 0, readBytes);
          
        } while (readBytes != -1);

        fileOutput.close();
      } finally {
        close(urlConnection.getInputStream());
      }
    }

    private static void close( InputStream inputStream )
    {
      try {
        inputStream.close();
      }
      catch( IOException ioe ) {
        // TODO: log this.
      }
    }
    
    /**
     * Inner class to manage catalogs and commands.
     */
    private static class Entry {
      private String name;
      private StringBuffer content;
      private Map<String, Entry> subEntries = new HashMap<String, Entry>();
      
      public String getName() {
        return name;
      }
      
      public void setName(String name) {
        this.name = name;
      }
      
      public StringBuffer getContent() {
        return content;
      }
      
      public void setContent(StringBuffer content) {
        this.content = content;
      }
      
      public void addSubEntry(Entry entry) {
        subEntries.put(entry.getName(), entry);
      }
      
      public Map<String,Entry> getSubEntries() {
        return subEntries;
      }
    }
}

