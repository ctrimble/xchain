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
  xmlns:servlet="http://www.xchain.org/servlet/1.0"
  xmlns:sax="http://www.xchain.org/sax/1.0"
  xmlns:jsl="http://www.xchain.org/jsl/1.0"
  version="2.0">

  <xsl:template match="@*|node()" mode="copy">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="copy"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/">
    <xchain:catalog>
      <xchain:chain xchain:name="servlet:get">
        <sax:pipeline>
          <sax:command-source>
            <jsl:template exclude-result-prefixes="xchain servlet sax jsl">
              <xsl:apply-templates select="*" mode="copy"/>
            </jsl:template>
          </sax:command-source>
          <sax:serializer method="'xhtml'"/>
          <servlet:result/>
        </sax:pipeline>
      </xchain:chain>
    </xchain:catalog>
  </xsl:template>

</xsl:stylesheet>

