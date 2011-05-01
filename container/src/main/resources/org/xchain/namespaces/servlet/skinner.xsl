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
<?xchain-transformer-factory name="{http://www.xchain.org/core}saxon"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://www.w3.org/1999/xhtml" xmlns:skin="http://www.xchain.org/skin"
  exclude-result-prefixes="skin xs"  version="2.0">

  <!-- Store the original input document. -->
  <xsl:variable name="input-doc" select="/"/>

  <!--
    Matches document nodes that do not have a skin processing instruction and returns them.
    -->
  <xsl:template match="document-node()" priority="-2">
    <xsl:param name="depth" select="0" tunnel="yes" as="xs:integer"/>
    <xsl:choose>
      <xsl:when test="$depth = 0">
        <xsl:variable name="with-skin-namespace">
          <xsl:apply-templates select="." mode="copy"/>
        </xsl:variable>
        <xsl:apply-templates select="$with-skin-namespace" mode="strip-skin"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="." mode="copy"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
    Matches document nodes that have a skin processing instruction.
    -->
  <xsl:template match="document-node()[processing-instruction('skin')]" priority="5">
    <xsl:param name="depth" select="0" tunnel="yes" as="xs:integer"/>
    <xsl:variable name="skin-system-id" select="replace(processing-instruction('skin'), '.*\s*system-id\s*=\s*&quot;([^&quot;]+)&quot;\s*.*', '$1')"/>
    <xsl:variable name="skin-doc" select="document($skin-system-id)"/>
    <xsl:variable name="template-node" select="."/>
    <xsl:choose>
    <xsl:when test="$skin-doc">
      <xsl:variable name="templated-skin-doc">
        <xsl:apply-templates select="$skin-doc">
          <xsl:with-param name="depth" select="$depth + 1" tunnel="yes"/>
        </xsl:apply-templates>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$depth = 0">
          <xsl:variable name="with-skin-namespace">
            <xsl:apply-templates select="$templated-skin-doc/*" mode="template">
              <xsl:with-param name="source-document" select="$template-node" tunnel="yes"/>
              <xsl:with-param name="source-uri" select="$skin-system-id" tunnel="yes"/>
            </xsl:apply-templates>
          </xsl:variable>
          <xsl:apply-templates select="$with-skin-namespace" mode="strip-skin"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="$templated-skin-doc/*" mode="template">
            <xsl:with-param name="source-document" select="$template-node" tunnel="yes"/>
            <xsl:with-param name="source-uri" select="$skin-system-id" tunnel="yes"/>
          </xsl:apply-templates>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <html>
        <head>
          <title>skin not found.</title>
        </head>
        <body>
          <div>Skin <xsl:value-of select="$skin-system-id"/> does not exist.</div>
        </body>
      </html>
    </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <xsl:template match="@*|node()" mode="strip-skin" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="strip-skin"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@skin:*|skin:*" mode="strip-skin" priority="0">
    <xsl:apply-templates select="node()" mode="strip-skin"/>
  </xsl:template>

  <xsl:template match="@*|node()" mode="copy" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="copy"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@skin:*|skin:*" mode="copy" priority="0">
    <xsl:param name="depth" select="0" as="xs:integer" tunnel="yes"/>
    <xsl:choose>
      <xsl:when test="$depth &gt; 0">
        <xsl:copy>
          <xsl:apply-templates select="@*|node()" mode="copy"/>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="node()" mode="copy" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="skin:import[not(@system-id)]" mode="copy">
    <xsl:variable name="source-uri" select="base-uri(.)" />
    <xsl:message>system-id required for skin:import in skin file: <xsl:value-of select="$source-uri"/></xsl:message>
  </xsl:template>

  <xsl:template match="skin:import[@system-id]" mode="copy">
    <xsl:variable name="source-uri" select="base-uri(.)" />
    <xsl:variable name="fragment-uri" select="resolve-uri(@system-id, $source-uri)" />
    <xsl:variable name="fragment-document">
      <xsl:if test="doc-available($fragment-uri)">
        <xsl:copy-of select="document($fragment-uri)" />
      </xsl:if>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$fragment-document = ''">
        <xsl:message>No document found at <xsl:value-of select="$fragment-uri"/></xsl:message>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="$fragment-document/skin:fragment">
            <xsl:apply-templates select="$fragment-document/skin:fragment/node()" mode="template">
              <xsl:with-param name="source-document" select="$fragment-document" tunnel="yes" />
              <xsl:with-param name="source-uri" select="$fragment-uri" tunnel="yes" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:otherwise>
            <xsl:message>skin:fragment node required in file attempting to be imported. <xsl:value-of select="$fragment-uri" /></xsl:message>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
    Matches nodes in the template and copies them.
    -->
  <xsl:template match="@*|node()" mode="template" priority="-3">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="template"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@skin:*|skin:*" mode="template" priority="-2">
    <xsl:param name="depth" select="0" as="xs:integer" tunnel="yes"/>
    <xsl:if test="$depth &gt; 0">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()" mode="template"/>
      </xsl:copy>
    </xsl:if>
  </xsl:template>

  <!--
    Matches nodes in the template that have a mode of 'replace'.
    -->
  <xsl:template match="*[skin:has-id(.) and skin:mode(.) = 'replace']" mode="template" priority="1">
    <xsl:param name="source-document" tunnel="yes"/>
    <xsl:variable name="template-skin-id" select="skin:id(.)"/>
    <xsl:choose>
      <xsl:when test="$source-document//*[skin:id(.) = $template-skin-id]">
        <xsl:apply-templates select="($source-document//*[skin:id(.) = $template-skin-id])[1]" mode="copy"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|node()" mode="template"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
    Matches nodes in the template that have a mode of 'merge'.
    -->
  <xsl:template match="*[skin:has-id(.) and skin:mode(.) = 'merge']" mode="template" priority="1">
    <xsl:param name="source-document" tunnel="yes"/>
    <xsl:param name="depth" select="0" as="xs:integer" tunnel="yes"/>
    <xsl:variable name="template-node" select="."/>
    <xsl:variable name="template-skin-id" select="skin:id(.)"/>
    <xsl:variable name="source-node" select="($source-document//*[skin:id(.)=$template-skin-id])[1]"/>
    <xsl:copy>
      <xsl:apply-templates select="$source-node/@*" mode="template"/>
      <xsl:apply-templates select="$template-node/@*[not(name()=$source-node/@*/name())]" mode="template"/>
      <xsl:apply-templates select="node()[not(self::skin:merge-point or preceding-sibling::skin:merge-point)]" mode="template"/>
      <xsl:apply-templates select="$source-node/node()" mode="copy"/>
      <xsl:if test="$depth &gt; 0 and not($source-node/skin:merge-point)">
        <skin:merge-point/>
      </xsl:if>
      <xsl:apply-templates select="node()[not(self::skin:merge-point) and preceding-sibling::skin:merge-point]" mode="template"/>
    </xsl:copy>
  </xsl:template>

  <!--
    Matches nodes that are of type 'import'
    -->

  <xsl:template match="skin:import[not(@system-id)]" mode="template">
    <xsl:variable name="source-uri" select="base-uri(.)" />
    <xsl:message>system-id required for skin:import in skin file: <xsl:value-of select="$source-uri"/></xsl:message>
  </xsl:template>

  <xsl:template match="skin:import[@system-id]" mode="template">
    <xsl:param name="source-uri" tunnel="yes" />
    <xsl:variable name="fragment-uri" select="resolve-uri(@system-id, $source-uri)" />
    <xsl:variable name="fragment-document">
      <xsl:if test="doc-available($fragment-uri)">
        <xsl:copy-of select="document($fragment-uri)" />
      </xsl:if>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$fragment-document = ''">
        <xsl:message>No document found at <xsl:value-of select="$fragment-uri"/></xsl:message>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="$fragment-document/skin:fragment">
            <xsl:apply-templates select="$fragment-document/skin:fragment/node()" mode="template">
              <xsl:with-param name="source-document" select="$fragment-document" tunnel="yes" />
              <xsl:with-param name="source-uri" select="$fragment-uri" tunnel="yes" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:otherwise>
            <xsl:message>skin:fragment node required in file attempting to be imported. <xsl:value-of select="$fragment-uri" /></xsl:message>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
    Returns true if there is a @skin:id or @id attribute on the element, false otherwise.
    -->
  <xsl:function name="skin:has-id">
    <xsl:param name="element"/>
    <xsl:sequence select="boolean($element/@skin:id) or boolean($element/@id)"/>
  </xsl:function>

  <!--
    Returns the id of the element on the template.
    -->
  <xsl:function name="skin:id">
    <xsl:param name="element"/>
    <xsl:sequence select="if( $element/@skin:id ) then $element/@skin:id else if ($element/@id) then $element/@id else ()"/>
  </xsl:function>

  <!--
    Returns the mode of a template element.
    -->
  <xsl:function name="skin:mode">
    <xsl:param name="element"/>
    <xsl:sequence select="if( $element/@skin:mode ) then $element/@skin:mode else 'replace'"/>
  </xsl:function>

</xsl:stylesheet>
