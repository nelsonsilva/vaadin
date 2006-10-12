<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="link">
  <DIV>
    <xsl:if test="@disabled='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
    <xsl:attribute name="CLASS">link<xsl:if test="./@style">-<xsl:value-of select="./@style"/></xsl:if></xsl:attribute>    
      <xsl:apply-templates select="." mode="core"/>
      <xsl:choose>
        <xsl:when test="$dhtml">
          <xsl:for-each select="./error"><xsl:apply-templates select="." mode="dhtml"/></xsl:for-each>
          <xsl:for-each select="./description"><xsl:apply-templates select="." mode="dhtml"/></xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="./error"><xsl:apply-templates select="." mode="inline"/></xsl:for-each>
          <xsl:for-each select="./description"><xsl:apply-templates select="." mode="inline"/></xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
  </DIV>
</xsl:template>

<xsl:template match="link" mode="core">
  <SPAN>
    <xsl:attribute name="CLASS">link-body<xsl:if test="./@style">-<xsl:value-of select="./@style"/></xsl:if></xsl:attribute>    
	<A>
	  <xsl:if test="not(@disabled='true')">
        <xsl:choose> 
          <xsl:when test="$dhtml and (string-length(@name) &gt; 0)">
            <xsl:attribute name="HREF">javascript:Millstone.openWindow('<xsl:value-of select="@src"
            />','<xsl:value-of select="@name"
            />',<xsl:choose><xsl:when test="@width"><xsl:value-of select="@width"/></xsl:when><xsl:otherwise>-1</xsl:otherwise></xsl:choose
            >,<xsl:choose><xsl:when test="@height"><xsl:value-of select="@height"/></xsl:when><xsl:otherwise>-1</xsl:otherwise></xsl:choose
            >,'<xsl:value-of select="@border" />');
             </xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="HREF"><xsl:value-of select="@src" /></xsl:attribute>
            <xsl:attribute name="TARGET"><xsl:value-of select="@name" /></xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      <xsl:if test="@icon"><IMG class="icon" SRC="{@icon}" /></xsl:if>
      <xsl:value-of select="@caption" />
    </A>
    </SPAN>	
</xsl:template>

</xsl:stylesheet>

