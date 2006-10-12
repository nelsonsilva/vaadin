<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  
    xmlns:wa="millstone://org.millstone.webadapter.ThemeFunctionLibrary" 
    xmlns:browser="millstone://org.millstone.webadapter.WebBrowser">

<xsl:output method="html" doctype-public="-//W3C//DTD HTML 4.0 Transitional//EN"/>

<!-- Calculate some browser dependant variables for use in templates -->
<xsl:variable name="dhtml" select="browser:supports(wa:browser(),browser:parseJavaScriptVersion('ECMA-262'))"/>

<xsl:variable name="maxtablewidth">
  <xsl:choose>
    <xsl:when test="browser:supports(wa:browser(),'MSIE ')">98%</xsl:when>
    <xsl:otherwise>100%</xsl:otherwise>
  </xsl:choose>
</xsl:variable>

<xsl:template match="uidl">
  <xsl:apply-templates/>
</xsl:template>

<!-- Bold formatting -->
<xsl:template match="b"><b><xsl:apply-templates /></b></xsl:template>

<!-- Italic formatting -->
<xsl:template match="i"><i><xsl:apply-templates /></i></xsl:template>

<!-- Underline formatting -->
<xsl:template match="u"><u><xsl:apply-templates /></u></xsl:template>

<!-- Linebreak  -->
<xsl:template match="br"><br /></xsl:template>

<!-- Unordered list -->
<xsl:template match="ul"><ul><xsl:apply-templates /></ul></xsl:template>

<!-- List item -->
<xsl:template match="li"><li><xsl:apply-templates /></li></xsl:template>

<!-- Headers -->
<xsl:template match="h1"><h1><xsl:apply-templates /></h1></xsl:template>
<xsl:template match="h2"><h2><xsl:apply-templates /></h2></xsl:template>
<xsl:template match="h3"><h3><xsl:apply-templates /></h3></xsl:template>
<xsl:template match="h4"><h4><xsl:apply-templates /></h4></xsl:template>
<xsl:template match="h5"><h5><xsl:apply-templates /></h5></xsl:template>
<xsl:template match="h6"><h6><xsl:apply-templates /></h6></xsl:template>

<!-- Preformatted data -->
<xsl:template match="pre">
   <PRE><xsl:copy-of select="text()|*"/></PRE>
</xsl:template>

<!-- XML raw data (should be in some other namespace) -->
<xsl:template match="data">
	   <xsl:copy-of select="text()|*"/>
</xsl:template>

<!-- XML raw data with escape="false" -->
<xsl:template match="data[@escape='false']">
	   <xsl:value-of select="text()|*" disable-output-escaping="yes" />
</xsl:template>

</xsl:stylesheet>

