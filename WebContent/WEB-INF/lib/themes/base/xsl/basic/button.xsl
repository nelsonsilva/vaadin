<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="button" mode="core">
  <xsl:choose>

    <!-- Link Style -->
    <xsl:when test="$dhtml and (@style='link')">
      <A CLASS="button-link">
        <xsl:if test="@focusid"><xsl:attribute name="FOCUSID"><xsl:value-of select="@focusid"/></xsl:attribute></xsl:if>
        <xsl:if test="not(@disabled)"><xsl:attribute name="HREF">javascript:Millstone.setVarById('<xsl:value-of select="./boolean/@id"/>','true',true)</xsl:attribute></xsl:if>
        <xsl:if test="@disabled"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
        <xsl:if test="@icon"><IMG class="icon" SRC="{@icon}" /></xsl:if>
        <INPUT TYPE="HIDDEN" ID="{./boolean/@id}" NAME="{./boolean/@id}" VALUE="{./boolean/@value}" />
        <xsl:value-of select="@caption" />
      </A>
    </xsl:when>
    
    <!-- Normal Style -->
    <xsl:otherwise>
      <xsl:if test="@icon"><INPUT TYPE="image" src="{@icon}" NAME="set:{./boolean/@id}=true" VALUE=" {@caption} ">
        <xsl:attribute name="CLASS">button<xsl:if test="string-length(./@style) &gt; 0">-<xsl:value-of select="./@style"/></xsl:if></xsl:attribute>    
   	    <xsl:if test="@disabled='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
        <xsl:if test="@readonly='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
        <xsl:if test="not(string-length(@caption) &gt; 0)"><xsl:attribute name="ID"><xsl:value-of select="./boolean/@id"/></xsl:attribute></xsl:if>
      </INPUT></xsl:if>
      <xsl:if test="string-length(@caption) &gt; 0">
      <INPUT CLASS="button" TYPE="SUBMIT" ID="{./boolean/@id}" NAME="set:{./boolean/@id}=true" VALUE=" {@caption} ">
        <xsl:attribute name="ONCLICK">Millstone.showHourglassCursor()</xsl:attribute>    
        <xsl:attribute name="CLASS">button<xsl:if test="string-length(./@style) &gt; 0">-<xsl:value-of select="./@style"/></xsl:if></xsl:attribute>    
   	    <xsl:if test="@disabled='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
        <xsl:if test="@readonly='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
     	<xsl:if test="@tabindex"><xsl:attribute name="tabindex"><xsl:value-of select="@tabindex"/></xsl:attribute></xsl:if>
        <xsl:if test="@focusid"><xsl:attribute name="FOCUSID"><xsl:value-of select="@focusid"/></xsl:attribute></xsl:if>
      </INPUT>
      </xsl:if>
   
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="button">

  <xsl:choose>

    <!-- Link Style -->
    <xsl:when test="$dhtml and ((@icon) or @style='link')">
      <DIV CLASS="button-link">
        <xsl:apply-templates select="." mode="core"/>
        <xsl:for-each select="./error"><xsl:apply-templates select="." mode="dhtml"/></xsl:for-each>
        <xsl:for-each select="./description"><xsl:apply-templates select="." mode="dhtml"/></xsl:for-each>
      </DIV>
    </xsl:when>
    
    <!-- Normal Style -->
    <xsl:otherwise>

      <!-- Core button -->
      <xsl:apply-templates select="." mode="core"/>
    
      <!-- descriptions and errors -->  
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
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<!-- Switch -->

<xsl:template match="button[@type='switch']">
  <DIV>
  	<xsl:attribute name="CLASS"><xsl:value-of select="local-name()"/><xsl:if test="(./@style) and (string-length(./@style) &gt; 0)">-<xsl:value-of select="./@style"/></xsl:if></xsl:attribute>
  <NOBR>		
    <xsl:apply-templates select="." mode="core"/>
    <SPAN class="caption">
      <xsl:if test="not(@disabled) and $dhtml"><xsl:attribute name="ONCLICK">Millstone.toggleCheckbox('<xsl:value-of select="./boolean/@id"/>',<xsl:value-of select="@immediate or false"/>)</xsl:attribute></xsl:if>
      <xsl:if test="@disabled"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
      <xsl:if test="@icon"><IMG class="icon" SRC="{@icon}" /></xsl:if>
      <xsl:value-of select="@caption"/>
	</SPAN>
  </NOBR>
  <xsl:choose>
    <xsl:when test="$dhtml">
      <xsl:for-each select="./error"><xsl:apply-templates select="." mode="dhtml"/></xsl:for-each>
      <xsl:for-each select="./description"><xsl:apply-templates select="." mode="dhtml"/></xsl:for-each>
    </xsl:when>
    <xsl:otherwise>
      <xsl:if test="@immediate='true'">      
         <INPUT STYLE="margin:4px; vertical-align: bottom;" TYPE="image" src="{wa:resource('img/immediate.gif')}" />
      </xsl:if>    
      <xsl:if test="./error"><BR /><xsl:apply-templates select="./error" mode="inline"/></xsl:if>
      <xsl:if test="./description"><BR /><xsl:apply-templates select="./description" mode="inline"/></xsl:if>
    </xsl:otherwise>
  </xsl:choose>
  </DIV>
</xsl:template>

<xsl:template match="button[@type='switch']" mode="core">
  <INPUT TYPE="HIDDEN" NAME="declare:{./boolean/@id}" VALUE="" />
  <INPUT TYPE="CHECKBOX" ID="{./boolean/@id}" NAME="{./boolean/@id}">
    <xsl:if test="@disabled='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
    <xsl:if test="@immediate='true' and $dhtml"><xsl:attribute name="onclick">Millstone.submit()</xsl:attribute></xsl:if>
    <xsl:if test="./boolean/@value='true'"><xsl:attribute name="CHECKED">true</xsl:attribute></xsl:if>
    <xsl:if test="@readonly='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
    <xsl:if test="@focusid"><xsl:attribute name="FOCUSID"><xsl:value-of select="@focusid"/></xsl:attribute></xsl:if>
   </INPUT>
      
</xsl:template>

</xsl:stylesheet>

