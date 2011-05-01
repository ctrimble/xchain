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

import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.ContentHandler;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xchain.framework.lifecycle.XmlFactoryLifecycle;

/**
 * A composite stage is a stage that is made up of many interior stages.  The methods on this class need to be called in the following order.
 * <ol>
 *   <li>addStage(Stage stage) once for each stage in the composite stage.</li>
 *   <li>setResult(Result result) once.</li>
 *   <li>getContentHandler() as many times as needed.</li>
 * </ol>
 * Calling these methods in the wrong order will result in an illegal state exception.
 *
 * Calling setResult() causes the stages to be "wired up" into one stage.  If there are no stages defined and the Result is a SAXResult, then
 * the following call to getContentHandler() will return the results content handler.  If the Result is not a SAXResult, then an identity transform
 * will be inserted to adapt the Result into a content handler.
 *
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Josh Kennedy
 */
public class CompositeStage
  implements Stage
{
  public static Logger log = LoggerFactory.getLogger( CompositeStage.class );

  protected List<Stage> stageList = new ArrayList<Stage>();
  protected ContentHandler contentHandler = null;
  protected boolean isResultSet = false;
  protected Stage lastStage = null;

  public void addStage( Stage stage )
  {
    if( log.isDebugEnabled() ) {
      log.debug("Adding stage to pipeline.");
    }

    if( isResultSet ) {
      throw new IllegalStateException("Stages cannot be added to a composite stage after the result has been set.");
    }
    stageList.add(stage);
  }

  /**
   * Returns the content handler for this stage list.  Once this method is called, no more stages can be added to the 
   * stage list.
   */
  public ContentHandler getContentHandler()
  {
    if( !isResultSet ) {
      throw new IllegalStateException("The content handler for a composite stage cannot be returned before the result has been set.");
    }

    // return the content handler for this stage.
    return contentHandler;
  }

  /**
   * Sets the result for this stage list.  Once this method is called, no more stages can be added to the
   * stage list.
   */
  public void setResult( Result result )
  {
    if( isResultSet ) {
      throw new IllegalStateException("The result of a composite stage cannot be set more than once.");
    }

    if( stageList.isEmpty() && !( result instanceof SAXResult ) ) {
      addIdentityTransformer();
    }

    if( !stageList.isEmpty() ) {
      // set the result on the last stage in the list.
      lastStage = (Stage)stageList.get(stageList.size()-1);
      lastStage.setResult(result);

      // iterate over the stages in reverse order, wiring them up as we go.
      for( int i = stageList.size() - 1; i > 0; i-- ) {
        Stage previousStage = (Stage)stageList.get(i-1);
        Stage nextStage     = (Stage)stageList.get(i);

        // create a result that will feed the next stage.
        SAXResult nextResult = new SAXResult();
        nextResult.setHandler(nextStage.getContentHandler());

        // set next stage result as the result of the previous stage.
        previousStage.setResult(nextResult);
      }

      // set the content handler as the content handler for the first stage.
      Stage firstStage = (Stage)stageList.get(0);
      contentHandler = firstStage.getContentHandler();
    }
    else if( result instanceof SAXResult ) {
      contentHandler = ((SAXResult)result).getHandler();
    }
    else {
      throw new IllegalStateException("Currently, all composite stages must have at least one stage, or they must have a SAXResult.");
    }

    // set the isResultSet flag to true.
    isResultSet = true;
  }

  /**
   * Returns the internet media type for the final stage of the composite
   */
  public String getMediaType()
  {
    if( !isResultSet ) {
      throw new IllegalStateException("The media type for a composite stage cannot be returned before the result has been set.");
    }
    return lastStage.getMediaType();
  }

  /**
   * Returns the character encoding for the final stage of the composite
   */
  public String getEncoding()
  {
    if( !isResultSet ) {
      throw new IllegalStateException("The encoding for a composite stage cannot be returned before the result has been set.");
    }
    return lastStage.getEncoding();
  }

  public void addIdentityTransformer()
  {
    try {
      // Create identity (copy) transformer
      TransformerHandler transformerHandler = XmlFactoryLifecycle.newTransformerFactory(XmlFactoryLifecycle.DEFAULT_TRANSFORMER_FACTORY_NAME).newTransformerHandler();

      // create a transformer handler stage for the identity.
      TransformerHandlerStage stage = new TransformerHandlerStage(transformerHandler);

      // set the transformer handler for the identity.
      addStage(stage);
    }
    catch( Exception e ) {
      throw new RuntimeException("Could not create identity transformer.", e);
    }
  }
}
