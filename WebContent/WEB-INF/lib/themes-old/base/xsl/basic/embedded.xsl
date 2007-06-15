<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">


<xsl:template match="embedded[@type='image']" mode="core">
  <IMG>
    <xsl:if test="@src"><xsl:attribute name="SRC"><xsl:value-of select="@src"/></xsl:attribute></xsl:if>
    <xsl:if test="@width"><xsl:attribute name="WIDTH"><xsl:value-of select="@width"/></xsl:attribute></xsl:if>
    <xsl:if test="@height"><xsl:attribute name="HEIGHT"><xsl:value-of select="@height"/></xsl:attribute></xsl:if>
    <xsl:if test="./description"><xsl:attribute name="ALT"><xsl:value-of select="./description"/></xsl:attribute></xsl:if>
  </IMG>
</xsl:template>

<xsl:template match="embedded" mode="core">
  <OBJECT>
    <xsl:if test="@src"><xsl:attribute name="DATA"><xsl:value-of select="@src"/></xsl:attribute></xsl:if>
    <xsl:if test="@mimetype"><xsl:attribute name="TYPE"><xsl:value-of select="@mimetype"/></xsl:attribute></xsl:if>
    <xsl:if test="@width"><xsl:attribute name="WIDTH"><xsl:value-of select="@width"/></xsl:attribute></xsl:if>
    <xsl:if test="@height"><xsl:attribute name="HEIGHT"><xsl:value-of select="@height"/></xsl:attribute></xsl:if>
    <xsl:if test="@classid"><xsl:attribute name="CLASSID"><xsl:value-of select="@classid"/></xsl:attribute></xsl:if>
    <xsl:if test="@codebase"><xsl:attribute name="CODEBASE"><xsl:value-of select="@codebase"/></xsl:attribute></xsl:if>
    <xsl:if test="@codetype"><xsl:attribute name="CODETYPE"><xsl:value-of select="@codetype"/></xsl:attribute></xsl:if>
    <xsl:if test="@standby"><xsl:attribute name="STANDBY"><xsl:value-of select="@standby"/></xsl:attribute></xsl:if>
    <xsl:for-each select="./embeddedparam">
        <PARAM NAME="{@name}" VALUE="{@value}" />
    </xsl:for-each>
  </OBJECT>
</xsl:template>

</xsl:stylesheet>