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
package org.xchain.namespaces.sax;

import org.apache.commons.jxpath.JXPathContext;

import org.xchain.Command;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.framework.net.UrlFactory;

import java.net.URL;

import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Writer;

import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Result;

import org.xml.sax.ContentHandler;

import org.w3c.dom.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>The &lt;sax:result/&gt; command adds result objects to a pipeline.  This command can add several types of results, based on the attributes
 * defined on the command.  To write output to a system id, add the system-id attribute to the command:</p>
 *
 * <code class="source">
 * &lt;sax:pipeline xmlns:sax="http://www.xchain.org/sax/1.0"&gt;
 *   ...
 *   &lt;sax:result system-id="'file:/some/path'"&gt;
 * &lt;/sax:pipeline&gt;
 * </code>
 *
 * <p>To write output to a file path, add the path attribute:</p>
 *
 * <code class="source">
 * &lt;sax:pipeline xmlns:sax="http://www.xchain.org/sax/1.0"&gt;
 *   ...
 *   &lt;sax:result path="'/some/path'"&gt;
 * &lt;/sax:pipeline&gt;
 * </code>
 *
 * <p>To write output to an object in the context, add the select attribute:</p>
 *
 * <code class="source">
 * &lt;sax:pipeline xmlns:sax="http://www.xchain.org/sax/1.0"&gt;
 *   ...
 *   &lt;sax:result select="$result"&gt;
 * &lt;/sax:pipeline&gt;
 * </code>
 *
 * <p>The value of the select attribute must resovle to one of:</p>
 * <ul>
 *   <li>javax.xml.transform.Result</li>
 *   <li>org.xml.sax.ContentHandler</li>
 *   <li>org.w3c.dom.Node</li>
 *   <li>java.io.OutputStream</li>
 *   <li>java.io.Writer</li>
 *   <li>java.io.File</li>
 * </ul>
 *
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Jason Rose
 * @author Josh Kennedy
 */
@Element(localName = "result")
public abstract class ResultCommand implements Command {
  public static Logger log = LoggerFactory.getLogger(ResultCommand.class);

  /**
   * <p>DEPRICATED: Use system-id instead.  The system id to send the output to.</p>
   * @param context the JXPathContext to evaluate against.
   */
  @Attribute(localName = "systemId", type = AttributeType.JXPATH_VALUE)
  public abstract String getSystemIdDepricated(JXPathContext context)
    throws Exception;

  /**
   * <p>Returns true if the systemId attribute has been set.</p>
   * @return true if the systemId attribute has been set, false otherwise.
   */
  public abstract boolean hasSystemIdDepricated();

  /**
   * <p>The system id to send output to.</p>
   * @param context the JXPathContext to evaluate against.
   */
  @Attribute(localName="system-id", type = AttributeType.JXPATH_VALUE)
  public abstract String getSystemId(JXPathContext context)
    throws Exception;

  /**
   * <p>Returns true if the system-id attribute has been set.</p>
   * @return true if the system-id attribute has been set, false otherwise.
   */
  public abstract boolean hasSystemId();

  /**
   * <p>The file path to send output to.</p>
   * @param context the JXPathContext to evaluate against.
   */
  @Attribute(localName = "path", type = AttributeType.JXPATH_VALUE)
  public abstract String getPath(JXPathContext context)
    throws Exception;

  /**
   * <p>Returns true if the path attribute has been set.</p>
   * @return true if the path attribute has been set, false otherwise.
   */
  public abstract boolean hasPath();

  /**
   * <p>The object to send output to.</p>
   * @param context the JXPathContext to evaluate against.
   */
  @Attribute(localName = "select", type = AttributeType.JXPATH_SELECT_SINGLE_NODE)
  public abstract Object getSelect(JXPathContext context)
    throws Exception;

  /**
   * <p>Returns true if the select attribute has been set.</p>
   * @return true if the select attribute has been set, false otherwise.
   */
  public abstract boolean hasSelect();

  /**
   * <p>Returns the result for the select attribute.</p>
   * @param context the JXPathContext to evaluate against.
   * @return the correct result object for the type of object selected from the context.
   */
  public Result createResultForSelect(JXPathContext context)
    throws Exception
  {
    Object object = getSelect(context);

    if( object == null ) {
      throw new IllegalArgumentException("The selected object cannot be null.");
    }
    // if the object is a result, then use it.
    else if( object instanceof Result ) {
      return (Result) object;
    }

    // if the object is a stream, then create a stream source.
    else if( object instanceof OutputStream ) {
      return new StreamResult((OutputStream) object);
    } else if( object instanceof Writer ) {
      return new StreamResult((Writer) object);
    } else if( object instanceof File ) {
      return new StreamResult((File) object);
    }

    // if the object is a content handler, then create a sax result.
    else if( object instanceof ContentHandler ) {
      return new SAXResult((ContentHandler) object);
    }

    // if the result is a node, then create a dom result.
    else if( object instanceof Node ) {
      return new DOMResult((Node) object);
    }

    // we do not how to make a result for this object, so bail out.
    else {
      throw new IllegalArgumentException("The selected result object (" + object.getClass().getName() + ") is not a result object nor is it an output stream.");
    }
  }

  /**
   * <p>Returns the Result object for the system-id attribute.</p>
   * @param context the JXPathContext to evaluate against.
   * @return a stream result for the system id.
   */
  public Result createResultForSystemId(JXPathContext context)
    throws Exception
  {
    // set the system id.
    //String systemId = getSystemId(context);
    String systemId = null;
    if( hasSystemId() ) {
      systemId = getSystemId(context);
    }
    else {
      systemId = getSystemIdDepricated(context);
    }

    // create a result object for the system id.
    URL url = UrlFactory.getInstance().newUrl(systemId);

    // create an output stream for this url.
    OutputStream out = url.openConnection().getOutputStream();

    // create a stream result for the output stream.
    StreamResult streamResult = new StreamResult();
    streamResult.setSystemId(systemId);
    streamResult.setOutputStream(out);

    return streamResult;
  }

  /**
   * <p>Returns the Result object for the path attribute.</p>
   * @param context the JXPathContext to evaluate against.
   * @return a stream result for the path specified.
   */
  public Result createResultForPath(JXPathContext context)
    throws Exception
  {
    // set the system id.
    String path = getPath(context);

    // get the file object for the path.
    File file = new File(path);

    // create the directories leading up to the path.
    File parentFile = file.getParentFile();
    if( !parentFile.exists() ) {
      parentFile.mkdirs();
    }

    // make sure that the file also exists.
    file.createNewFile();

    // create an output stream for this url.
    OutputStream out = new FileOutputStream(file);

    // create a stream result for the output stream.
    StreamResult streamResult = new StreamResult();
    streamResult.setSystemId(file.toURL().toExternalForm());
    streamResult.setOutputStream(out);

    return streamResult;
  }

  /**
   * <p>Builds the result object for this element and sets it on the current sax pipeline configuration.</p>
   */
  public boolean execute(JXPathContext context)
    throws Exception
  {
    Result result = null;

    if( hasSelect() ) {
      result = createResultForSelect(context);
    } else if( hasSystemId() || hasSystemIdDepricated() ) {
      result = createResultForSystemId(context);
    } else if( hasPath() ) {
      result = createResultForPath(context);
    } else {
      throw new IllegalStateException("The system-id, path or select attribute must be set for the result tag.");
    }

    // set the result in the pipeline config's composite stage.
    PipelineCommand.getPipelineConfig().getCompositeStage().setResult(result);

    return false;
  }
}
