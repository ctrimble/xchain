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
package org.xchain.namespaces.jsl;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.junit.Ignore;
import org.xchain.Command;
import org.xchain.EngineeredCatalog;
import org.xchain.framework.sax.SaxEventRecorder;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Christian Trimble
 */
public class TestCommentCommand
  extends BaseTestSaxEvents
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/jsl/comment.xchain";
  public static String JSL_NAMESPACE_URI = "http://www.xchain.org/jsl/1.0";
  public static QName SIMPLE_COMMENT_TEMPLATE = new QName(JSL_NAMESPACE_URI, "simple-comment-pipeline");
  public static QName RAW_EXECUTED_COMMENT_TEMPLATE = new QName(JSL_NAMESPACE_URI, "raw-executed-comment-pipeline");
  public static QName TEXT_EXECUTED_COMMENT_TEMPLATE = new QName(JSL_NAMESPACE_URI, "text-executed-comment-pipeline");
  public static QName VALUE_OF_EXECUTED_COMMENT_TEMPLATE = new QName(JSL_NAMESPACE_URI, "value-of-executed-comment-pipeline");
  public static QName MIXED_EXECUTED_COMMENT_TEMPLATE = new QName(JSL_NAMESPACE_URI, "mixed-executed-comment-pipeline");
  public static QName WHEN_EXECUTED_COMMENT_TEMPLATE = new QName(JSL_NAMESPACE_URI, "when-executed-comment-pipeline");
  public static QName DEEP_EXECUTED_COMMENT_TEMPLATE = new QName(JSL_NAMESPACE_URI, "deep-executed-comment-pipeline");

  protected Command command = null;

  public TestCommentCommand()
  {
    catalogUri = CATALOG_URI;
  }

  @Test public void testSimpleComment()
    throws Exception
  {
    executeAndTestComment( SIMPLE_COMMENT_TEMPLATE, "simple comment" );
  }

  @Test public void testRawExecutedComment()
    throws Exception
  {
    executeAndTestComment( RAW_EXECUTED_COMMENT_TEMPLATE, "raw executed comment" );
  }

  @Test public void testTextExecutedComment()
    throws Exception
  {
    executeAndTestComment( TEXT_EXECUTED_COMMENT_TEMPLATE, "jsl:text executed comment" );
  }

  @Test public void testValueOfExecutedComment()
    throws Exception
  {
    executeAndTestComment( VALUE_OF_EXECUTED_COMMENT_TEMPLATE, "jsl:value-of executed comment" );
  }

  @Test public void testMixedExecutedComment()
    throws Exception
  {
    executeAndTestComment( MIXED_EXECUTED_COMMENT_TEMPLATE, "mixed executed comment" );
  }

  @Test public void testWhenExecutedComment()
    throws Exception
  {
    executeAndTestComment( WHEN_EXECUTED_COMMENT_TEMPLATE, "xchain:when executed comment" );
  }

  @Test public void testDeepExecutedComment()
    throws Exception
  {
    executeAndTestComment( DEEP_EXECUTED_COMMENT_TEMPLATE, "raw executed comment, jsl:text executed comment, jsl:value-of executed comment, xchain:when executed comment" );
  }

  private void executeAndTestComment( QName commandName, String commentText )
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(commandName);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackCharactersEvents(true);
    recorder.setTrackCommentEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "element", null);
    assertComment(eventIterator, commentText);
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }
}
