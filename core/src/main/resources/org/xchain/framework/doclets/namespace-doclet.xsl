<?xml version="1.0"?>
<!--

       Copyright 2011 meltmedia

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

           http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.

-->
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="2.0">

  <!--
    Copy any nodes that we find.
    -->
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>

  <!--
    If we find a source node, then replace it with a source tag.
    -->
  <xsl:template match="code[tokenize(@class, '/s+') = ('source')]">
    <source>
      <xsl:apply-templates select="node()"/>
    </source>
  </xsl:template>

  <xsl:template match="subsection">
    <xsl:variable name="depth" select="count(ancestor::subsection)"/>
    <xsl:choose>
      <xsl:when test="$depth=0">
        <xsl:copy>
          <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <div class="section"><a name="{@name}"></a>
          <xsl:element name="{concat('h', $depth+3)}"><xsl:value-of select="@name"/></xsl:element>
          <xsl:apply-templates select="node()"/>
        </div>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
