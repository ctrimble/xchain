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
<?xchain-transformer-factory name="{http://www.xchain.org/core}saxon"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xchain="http://www.xchain.org/core/1.0"
  xmlns:fn="http://www.w3.org/2005/02/xpath-functions"
  version="2.0">

  <xsl:template match="@*|node()" mode="copy">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="copy"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/xchain:chain">
    <xchain:catalog>
      <xchain:chain xchain:name="xchain:generated">
        <xchain:execute system-id="'{resolve-uri('./xchain-stylesheet-decl.xchain',static-base-uri())}'" name="'xchain:provided'"/>
      </xchain:chain>

      <xchain:chain xchain:name="xchain:relative-execute">
        <xchain:execute name="'xchain:provided'"/>
      </xchain:chain>
      <xsl:apply-templates select="." mode="copy"/>
    </xchain:catalog>
  </xsl:template>

</xsl:stylesheet>
