# XChain Container

The XChain Container package provides commands/chains for working with servlets.  Here is a quick example of an xchain that produces output to a servlet:

    <xchain:catalog
       xmlns:sax="http://www.xchain.org/sax/1.0"
       xmlns:jsl="http://www.xchain.org/jsl/1.0"
       xmlns:servlet="http://www.xchain.org/servlet/1.0"
       xmlns:xchain="http://www.xchain.org/core/1.0">
    
      <xchain:chain xchain:name="my-chain-name">
        <sax:pipeline>
          <sax:command-source>
            <jsl:template>
              <html xmlns="http://www.w3.org/1999/xhtml">
                <head>
                  <title>Testing</title>
                </head>
                <body>
                  <h1>This is a test</h1>
                </body>
              </html>
            </jsl:template>
          </sax:command-source>
    
          <servlet:result/>
    
        </sax:pipeline>
      </xchain:chain>
    
    </xchain:catalog>
