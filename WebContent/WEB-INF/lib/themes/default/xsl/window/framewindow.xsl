<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">


<xsl:template match="framewindow">
  <HTML>
    <HEAD>
      <xsl:call-template name="window-head"/>
    </HEAD>
    <xsl:apply-templates select="frameset"/>
  </HTML>
  <BODY>
      <xsl:if test="$dhtml and (./integer[@name='height']/@value &gt; 0) and (./integer[@name='width']/@value &gt; 0)">
        <xsl:attribute name="onload">window.resizeTo(<xsl:value-of select="./integer[@name='width']/@value"/>,<xsl:value-of select="./integer[@name='height']/@value"/>);</xsl:attribute>
      </xsl:if>  
  </BODY>
</xsl:template>

<xsl:template match="frameset">
    <FRAMESET>
    	<xsl:if test="@rows">
    		<xsl:attribute name="ROWS">
    			<xsl:value-of select="@rows"/>
    		</xsl:attribute>
    	</xsl:if>
    	<xsl:if test="@cols">
    		<xsl:attribute name="COLS">
    			<xsl:value-of select="@cols"/>
    		</xsl:attribute>
    	</xsl:if>
        <xsl:apply-templates/>
    </FRAMESET>
</xsl:template>

<xsl:template match="frame">
    <FRAME SRC="{@src}" NAME="{wa:getWindowTargetName(@name)}" />
</xsl:template>

</xsl:stylesheet>

