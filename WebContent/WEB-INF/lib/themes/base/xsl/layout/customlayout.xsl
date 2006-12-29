<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- This layout must be overriden with higher priority template -->

<xsl:template match="customlayout" mode="core">
    <xsl:for-each select="location"><xsl:apply-templates select="."/></xsl:for-each>    
</xsl:template>

</xsl:stylesheet>

