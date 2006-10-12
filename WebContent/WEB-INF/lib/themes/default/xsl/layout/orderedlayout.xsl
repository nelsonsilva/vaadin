<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">


<!-- STYLE: default -->

<xsl:template match="orderedlayout[@orientation='horizontal']" mode="core">
  <xsl:if test="child::*">
    <TABLE BORDER="0" CELLPADDING="0" CELLSPACING="0">
      <TR>
        <xsl:for-each select="*">
          <TD><xsl:apply-templates select="."/></TD>
        </xsl:for-each>
      </TR>
    </TABLE>
  </xsl:if>
</xsl:template>

<xsl:template match="orderedlayout" mode="core">
  <xsl:if test="child::*">
    <TABLE BORDER="0" CELLPADDING="0" CELLSPACING="0">
      <xsl:for-each select="*">
        <TR><TD><xsl:apply-templates select="."/></TD></TR>
      </xsl:for-each>
    </TABLE>
  </xsl:if>
</xsl:template>


<!-- STYLE: form -->

<xsl:template match="orderedlayout[@style='form']" mode="core">
  <xsl:if test="./child::*">
    <TABLE BORDER="0">
      <xsl:for-each select="*">
        <TR>
          <TD CLASS="form-caption">
		    <xsl:if test="not(local-name()!='button' and @type='switch') and local-name()!='link' and @caption">
		      <NOBR CLASS="caption">
  		        <xsl:if test="@icon"><IMG SRC="{@icon}" /></xsl:if>
  	            <xsl:value-of select="@caption"/>
		      </NOBR>
		    </xsl:if>
		    <xsl:if test="not(@caption) and @icon"><IMG SRC="{@icon}" /></xsl:if>
          </TD>
          <TD><xsl:apply-templates select="." mode="core"/></TD>
          <TD>      
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
          </TD>
        </TR>
      </xsl:for-each>
    </TABLE>
  </xsl:if>
</xsl:template>


<xsl:template match="orderedlayout[(@orientation='horizontal') and (@style='form')]" mode="core">
  <xsl:if test="./child::*">
    <TABLE BORDER="0">
      <TR CLASS="form-caption">
        <xsl:for-each select="*">
          <TD>
		    <xsl:if test="not(local-name()!='button' and @type='switch') and local-name()!='link' and @caption">
		      <NOBR CLASS="caption">
  		        <xsl:if test="@icon"><IMG SRC="{@icon}" /></xsl:if>
		        <xsl:value-of select="@caption"/>
		      </NOBR>
		    </xsl:if>
		    <xsl:if test="not(@caption) and @icon"><IMG SRC="{@icon}" /></xsl:if>
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
          </TD>
        </xsl:for-each>
      </TR>
      <TR>
        <xsl:for-each select="*">
	        <TD><xsl:apply-templates select="." mode="core"/></TD>
        </xsl:for-each>
      </TR>
    </TABLE>
  </xsl:if>
</xsl:template>


</xsl:stylesheet>