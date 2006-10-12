<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="textfield" mode="core">
  <xsl:choose>
    <xsl:when test="@multiline='true' and not(@secret='true')">
	  <TEXTAREA NAME="{./string[@name='text']/@id}" ID="{./string[@name='text']/@id}"> 
		<xsl:if test="@modified='true'"><xsl:attribute name="CLASS">modified</xsl:attribute></xsl:if>
	    <xsl:if test="@readonly='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
        <xsl:if test="@disabled='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
		<xsl:if test="@immediate='true' and $dhtml"><xsl:attribute name="onchange">Millstone.submit()</xsl:attribute></xsl:if>
		<xsl:if test="not(@immediate='true') and $dhtml"><xsl:attribute name="onchange">this.className='modified'</xsl:attribute></xsl:if>
		<xsl:if test="@cols"><xsl:attribute name="COLS"><xsl:value-of select="@cols"/></xsl:attribute></xsl:if>
		<xsl:if test="@rows"><xsl:attribute name="ROWS"><xsl:value-of select="@rows"/></xsl:attribute></xsl:if>
		<xsl:if test="@wordwrap='false'"><xsl:attribute name="WRAP">off</xsl:attribute></xsl:if>
		<xsl:if test="@maxlength"><xsl:attribute name="MAXLENGTH"><xsl:value-of select="@maxlength"/></xsl:attribute></xsl:if>
    	<xsl:if test="@tabindex"><xsl:attribute name="tabindex"><xsl:value-of select="@tabindex"/></xsl:attribute></xsl:if>
        <xsl:if test="@focusid"><xsl:attribute name="FOCUSID"><xsl:value-of select="@focusid"/></xsl:attribute></xsl:if>
		<xsl:value-of select="./string[@name='text']"/>
      </TEXTAREA>
	</xsl:when>
	<xsl:otherwise>
	  <INPUT NAME="{./string[@name='text']/@id}" ID="{./string[@name='text']/@id}" VALUE="{./string[@name='text']}"> 
		<xsl:if test="@modified='true'"><xsl:attribute name="CLASS">modified</xsl:attribute></xsl:if>
	    <xsl:if test="@readonly='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
        <xsl:if test="@disabled='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
		<xsl:if test="@immediate='true' and $dhtml"><xsl:attribute name="onchange">Millstone.submit()</xsl:attribute></xsl:if>
		<xsl:if test="not(@immediate='true') and $dhtml"><xsl:attribute name="onchange">this.className='modified'</xsl:attribute></xsl:if>
		<xsl:if test="@cols"><xsl:attribute name="SIZE"><xsl:value-of select="@cols"/></xsl:attribute></xsl:if>
		<xsl:if test="@maxlength"><xsl:attribute name="MAXLENGTH"><xsl:value-of select="@maxlength"/></xsl:attribute></xsl:if>
		<xsl:if test="@secret"><xsl:attribute name="TYPE">password</xsl:attribute></xsl:if>
     	<xsl:if test="@tabindex"><xsl:attribute name="tabindex"><xsl:value-of select="@tabindex"/></xsl:attribute></xsl:if>
        <xsl:if test="@focusid"><xsl:attribute name="FOCUSID"><xsl:value-of select="@focusid"/></xsl:attribute></xsl:if>
      </INPUT>
    </xsl:otherwise>
  </xsl:choose>

</xsl:template>

</xsl:stylesheet>

