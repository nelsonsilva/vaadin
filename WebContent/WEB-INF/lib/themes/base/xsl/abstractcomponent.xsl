<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="label|datefield|embedded|textfield|upload|select|table|tree|customlayout|gridlayout|orderedlayout|tabsheet">
  <DIV>
  	<xsl:attribute name="CLASS"><xsl:value-of select="local-name()"/><xsl:if test="(./@style) and (string-length(./@style) &gt; 0)">-<xsl:value-of select="./@style"/></xsl:if></xsl:attribute>
    <xsl:if test="(@caption)|(@icon)|(./description)|(./error)">
      <xsl:if test="@disabled='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
      <NOBR CLASS="caption">
        <xsl:if test="@icon"><IMG SRC="{@icon}"/></xsl:if>
        <xsl:value-of select="@caption"/>
      </NOBR>
      <xsl:choose>
        <xsl:when test="$dhtml">
          <xsl:for-each select="./error"><xsl:apply-templates select="." mode="dhtml"/></xsl:for-each>
          <xsl:for-each select="./description"><xsl:apply-templates select="." mode="dhtml"/></xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:if test="./error"><BR /><xsl:apply-templates select="./error" mode="inline"/></xsl:if>
          <xsl:if test="./description"><BR /><xsl:apply-templates select="./description" mode="inline"/></xsl:if>
        </xsl:otherwise>
      </xsl:choose>
      <BR />
    </xsl:if>    
    <xsl:apply-templates select="." mode="core"/>    
    <!-- Immediate buttons -->
    <xsl:if test="@immediate='true' and (not($dhtml) or (local-name()='upload'))">
      <xsl:if test="local-name()='textfield' or local-name()='datefield' or local-name()='upload' or local-name()='select'">      
         <INPUT STYLE="margin:4px; vertical-align: bottom;" TYPE="image" src="{wa:resource('img/immediate.gif')}" />
      </xsl:if>    
    </xsl:if>    
  </DIV>
</xsl:template>

<!-- Description popup -->

<xsl:template match="description"/>

<xsl:template match="description" mode="dhtml">
  <xsl:variable name="descid" select="generate-id(.)"/>
  <A ONCLICK="itmill.html.utils.showPopupById('{$descid}',event.clientX,event.clientY);">	
    <IMG> 
      <xsl:attribute name="SRC"><xsl:value-of select="wa:resource('icon/error/info.gif')"/></xsl:attribute>
    </IMG>
  </A>
</xsl:template>

<xsl:template match="description" mode="popup">
  <xsl:variable name="descid" select="generate-id(.)"/>
  <DIV ID="{$descid}" CLASS="popup">
    <xsl:if test="$dhtml">
      <xsl:attribute name="STYLE">display:none;</xsl:attribute>
      <xsl:attribute name="ONCLICK">itmill.html.utils.hidePopupById('<xsl:value-of select="$descid"/>');</xsl:attribute>
    </xsl:if>
    <xsl:apply-templates/>		 
  </DIV>
</xsl:template>

<xsl:template match="description" mode="inline">
  <xsl:variable name="descid" select="generate-id(.)"/>
  <DIV ID="{$descid}" CLASS="popup-inline">
    <xsl:apply-templates/>		 
  </DIV>
</xsl:template>


<!-- Error popup -->

<xsl:template match="error"/>

<xsl:template match="error" mode="dhtml">
  <xsl:variable name="errid" select="generate-id(.)"/>
  <A ONCLICK="itmill.html.utils.showPopupById('{$errid}',event.clientX-4,event.clientY-4);">	
    <xsl:call-template name="error-icon">
	  <xsl:with-param name="level" select="@level"/>
	</xsl:call-template>
  </A>
</xsl:template>

<xsl:template match="error" mode="popup">
  <xsl:variable name="errid" select="generate-id(.)"/>
  <DIV ID="{$errid}" CLASS="popup">
      <xsl:attribute name="STYLE">display:none;</xsl:attribute>
      <xsl:attribute name="ONCLICK">itmill.html.utils.hidePopupById('<xsl:value-of select="$errid"/>');</xsl:attribute>
    <xsl:apply-templates select="." mode="errordesc"/>		 
  </DIV>
</xsl:template>

<xsl:template match="error" mode="inline">
  <xsl:variable name="errid" select="generate-id(.)"/>
  <DIV ID="{$errid}" CLASS="popup-inline">
    <xsl:apply-templates select="." mode="errordesc"/>		 
  </DIV>
</xsl:template>

<xsl:template match="error" mode="errordesc">
  <TABLE BORDER="0"><TR><TD VALIGN="TOP">
    <xsl:call-template name="error-icon">
      <xsl:with-param name="level" select="@level"/>
    </xsl:call-template></TD>
  <TD><xsl:apply-templates/><xsl:apply-templates select="error" mode="errordesc"/></TD></TR></TABLE>
</xsl:template>

<xsl:template name="error-icon">
  <xsl:param name="level"/>
  <xsl:choose>
	<xsl:when test="$level='info'">
	  <IMG> 
        <xsl:attribute name="SRC"><xsl:value-of select="wa:resource('icon/error/info.gif')"/></xsl:attribute>
	  </IMG>
	</xsl:when>
	<xsl:when test="$level='error'">
	  <IMG>
        <xsl:attribute name="SRC"><xsl:value-of select="wa:resource('icon/error/error.gif')"/></xsl:attribute>
	  </IMG>
	</xsl:when>
	<xsl:when test="$level='warning'">
	  <IMG>
        <xsl:attribute name="SRC"><xsl:value-of select="wa:resource('icon/error/warning.gif')"/></xsl:attribute>
	  </IMG>
	</xsl:when>
	<xsl:when test="$level='critical'">
	  <IMG>
        <xsl:attribute name="SRC"><xsl:value-of select="wa:resource('icon/error/critical.gif')"/></xsl:attribute>
	  </IMG>
	</xsl:when>
	<xsl:otherwise>
	  <IMG>
        <xsl:attribute name="SRC"><xsl:value-of select="wa:resource('icon/error/system.gif')"/></xsl:attribute>
	  </IMG>
	</xsl:otherwise>
	</xsl:choose>
</xsl:template>

</xsl:stylesheet>

