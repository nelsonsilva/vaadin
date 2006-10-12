<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
<xsl:template match="upload" mode="core">
  <INPUT TYPE="file" ID="{./uploadstream/@id}" NAME="{./uploadstream/@id}" VALUE="{./uploadstream}"> 
    <xsl:if test="@modified='true'"><xsl:attribute name="CLASS">modified</xsl:attribute></xsl:if>
    <xsl:if test="@readonly='true'"><xsl:attribute name="READONLY">true</xsl:attribute></xsl:if>
    <xsl:if test="not(@immediate='true') and $dhtml"><xsl:attribute name="onchange">this.CLASS='modified'</xsl:attribute></xsl:if>
    <xsl:if test="@tabindex"><xsl:attribute name="tabindex"><xsl:value-of select="@tabindex"/></xsl:attribute></xsl:if>
    <xsl:if test="@focusid"><xsl:attribute name="FOCUSID"><xsl:value-of select="@focusid"/></xsl:attribute></xsl:if>
  </INPUT>  
</xsl:template>

</xsl:stylesheet>
