<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- GridLayout compoenent XSL -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Grid layout component -->
<xsl:template match="gridlayout" mode="core">
  <table border="0">
    <!-- Table rows -->
    <xsl:for-each select="gr">
      <tr>
      <xsl:for-each select="gc">
        <xsl:variable name="colspan">
          <xsl:choose>
            <xsl:when test="@w">
              <xsl:value-of select="@w"/>
            </xsl:when>
            <xsl:otherwise>1</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="rowspan">
          <xsl:choose>
            <xsl:when test="@h">
              <xsl:value-of select="@h"/>
            </xsl:when>
            <xsl:otherwise>1</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <!-- Grid cells -->
            <td valign="top" align="left"><xsl:if test="$colspan &gt; 1">
                  <xsl:attribute name="colspan"><xsl:value-of select="$colspan"/></xsl:attribute>
                </xsl:if>
              <xsl:if test="$rowspan &gt; 1">
                <xsl:attribute name="rowspan"><xsl:value-of select="$rowspan"/></xsl:attribute>
              </xsl:if>
              <!-- apply component -->
              <xsl:apply-templates/>
             </td>
      </xsl:for-each>      
      </tr>
    </xsl:for-each>
  </table>
</xsl:template>

</xsl:stylesheet>

