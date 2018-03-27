<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" omit-xml-declaration="no" indent="yes" />

  <xsl:variable name="pattern" select="document('pattern.xml')"/>

  <xsl:template match="/">
    <root>
      <xsl:for-each select="*/copy">
	<xsl:call-template name="substitute-element">
	  <xsl:with-param name="element" select="$pattern"/>
	  <xsl:with-param name="map" select="."/>
	</xsl:call-template>    
      </xsl:for-each>
    </root>
  </xsl:template>

  <xsl:template name="substitute-element">
    <xsl:param name="element"/>
    <xsl:param name="map"/>

    <xsl:for-each select="$element/*">

      <!-- element name -->
      <xsl:variable name="elementName">
	<xsl:call-template name="substitute-value">
	  <xsl:with-param name="value" select="name(.)"/>
	  <xsl:with-param name="map" select="$map"/>
	</xsl:call-template>
      </xsl:variable>

      <xsl:element name="{$elementName}">

	<!-- attributes -->
	<xsl:for-each select="@*">

	  <!-- attribute name -->
	  <xsl:variable name="attrName">
	    <xsl:call-template name="substitute-value">
	      <xsl:with-param name="value" select="name(.)"/>
	      <xsl:with-param name="map" select="$map"/>
	    </xsl:call-template>
	  </xsl:variable>
	  <xsl:attribute name="{$attrName}">

	    <!-- attribute value -->
	    <xsl:call-template name="substitute-value">
	      <xsl:with-param name="value" select="."/>
	      <xsl:with-param name="map" select="$map"/>
	    </xsl:call-template>
	  </xsl:attribute>
	</xsl:for-each> <!-- attributes -->

	
	<!-- element text node -->
	<xsl:call-template name="substitute-value">
	  <xsl:with-param name="value" select="text()"/>
	  <xsl:with-param name="map" select="$map"/>
	</xsl:call-template>
	

	<!-- recurse into child elements -->
	<xsl:for-each select="$element/*">
	  <xsl:call-template name="substitute-element">
	    <xsl:with-param name="element" select="."/>
	    <xsl:with-param name="map" select="$map"/>
	  </xsl:call-template>
	</xsl:for-each>
	
      </xsl:element>

    </xsl:for-each>
  </xsl:template>

  <xsl:template name="substitute-value">
    <xsl:param name="value"/>
    <xsl:param name="map"/>
    <xsl:choose>
      <!-- search for matching key -->
      <xsl:when test="$map/*[name(.)=$value]">
	<xsl:value-of select="$map/*[name(.)=$value]/text()"/>
      </xsl:when>
      <!-- default to incoming value if matching key not found -->
      <xsl:otherwise>
	<xsl:value-of select="$value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  

</xsl:stylesheet>
