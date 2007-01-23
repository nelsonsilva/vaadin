<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="actions" mode="popup">
  <xsl:variable name="actionlistid"><xsl:value-of select="generate-id(.)" /></xsl:variable>
  <xsl:variable name="actionvariableid"><xsl:value-of select="./string[@name='action']/@id"/></xsl:variable>

  <DIV ID="{$actionlistid}_POPUP" 
       CLASS="action-popup" 
       STYLE="display:none">

    <INPUT ID="{$actionvariableid}" NAME="{$actionvariableid}" TYPE="HIDDEN" />
    <INPUT ID="{$actionlistid}_ACTIVE_ITEM" TYPE="HIDDEN" NAME="{$actionlistid}_ACTIVE_ITEM" />
       
    <xsl:for-each select="./action">     
      <DIV ID="{$actionlistid}_{@key}" 
           CLASS="action-item"
           ONMOUSEOVER="this.className = itmill.html.utils.toHighlightClassName(this.className);"
           ONMOUSEOUT="this.className = itmill.html.utils.toUnselectedClassName(this.className);"
           ONCLICK="itmill.html.utils.fireAction('{$actionlistid}','{$actionvariableid}','{@key}');">
        <NOBR><xsl:value-of select="@caption" /></NOBR>
 	  </DIV>
 	</xsl:for-each> 	
  </DIV>
</xsl:template>


<xsl:template match="al" mode="dhtml">
  <xsl:param name="actionlistid" />

  <xsl:variable name="itemid"><xsl:value-of select="../@key"/></xsl:variable>
  <xsl:variable name="activeactions"><xsl:for-each select="./ak"><xsl:value-of select="."/>,</xsl:for-each></xsl:variable>
  
  <xsl:if test="./ak">
    <IMG SRC="{wa:resource('img/popup-button.gif')}" CLASS="action" BORDER="0">
      <xsl:attribute name="onclick">itmill.html.utils.actionPopup(event,'<xsl:value-of select="$actionlistid"/>','<xsl:value-of select="$itemid"/>','<xsl:value-of select="$activeactions"/>')</xsl:attribute>
    </IMG>
  </xsl:if>
</xsl:template>


<!-- Non Javasctipt version -->

<xsl:template match="al" mode="inline">
  <xsl:param name="actionsvar" />

  <xsl:variable name="actionvariableid" select="$actionsvar/string[@name='action']/@id"/>

  <xsl:variable name="itemid" select="../@key"/>
  <xsl:for-each select="./ak">  
    <INPUT TYPE="submit" CLASS="action">
      <xsl:attribute name="NAME">set:<xsl:value-of select="$actionvariableid"/>=<xsl:value-of select="$itemid"/>,<xsl:value-of select="text()"/></xsl:attribute>
      <xsl:variable name="activekey" select="text()"/>
      <xsl:attribute name="VALUE"><xsl:value-of select="$actionsvar/action[@key=$activekey]/@caption" /></xsl:attribute>     
    </INPUT>
  </xsl:for-each>
</xsl:template>

</xsl:stylesheet>

