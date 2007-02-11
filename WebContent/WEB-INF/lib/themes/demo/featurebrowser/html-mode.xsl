<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">

	<!--
		<xsl:template match="customlayout" mode="core">
		<xsl:for-each select="location"><xsl:apply-templates select="."/></xsl:for-each>    
		</xsl:template>
	-->

	<xsl:template
		match="orderedlayout[orderedlayout[@style='featurebrowser-mainlayout']]">
		<table border="0" cellpadding="0" cellspacing="0" width="100%"
			height="100%">
			<tr height="62px">
				<td colspan="3">
					<table border="0" width="100%" cellpadding="0"
						cellspacing="0" height="62px">
						<tr>
							<td>
								<img
									src="{wa:resource('featurebrowser/img/header.png')}" />
							</td>
							<td align="right" valign="bottom">
						<xsl:apply-templates
							select="./orderedlayout/orderedlayout[position()=3]/button" />
							</td>
						</tr>
					</table>
				</td>
			</tr>
			<tr>
				<td width="200px">
					<table cellpadding="0" cellspacing="0" border="0"
						width="200px" height="100%">
						<tr>
							<td valign="top" colspan="2">
									<xsl:apply-templates
										select="./orderedlayout/orderedlayout[position()=1]/tree" />

							</td>
						</tr>
						<tr align="center" valign="middle"
							height="60px;">
							<td>

								<xsl:apply-templates
									select="./orderedlayout/orderedlayout[position()=1]/select" />
							</td>
							<td>
								<xsl:apply-templates
									select="./orderedlayout/orderedlayout[position()=1]/button" />
							</td>
						</tr>
					</table>
				</td>
				<td>
					<table border="0" width="100%" height="100%">
						<tr height="50%">
							<td>
								<table border="0" width="100%"
									height="100%">
									<tr valign="middle">
										<td align="center">
											<xsl:apply-templates
												select="./orderedlayout/orderedlayout[position()=2]/child::*[position()=1]" />
										</td>
									</tr>
								</table>
							</td>
						</tr>
						<tr height="50%">
							<td valign="top">
								<xsl:apply-templates
									select="./orderedlayout/orderedlayout[position()=2]/tabsheet" />
							</td>
						</tr>
					</table>
				</td>
				<xsl:if test="./orderedlayout/orderedlayout[position()=3]/panel">
				<td width="260px">
						<xsl:apply-templates
							select="./orderedlayout/orderedlayout[position()=3]/panel" />
				</td>
				</xsl:if>
				<xsl:if test="not(./orderedlayout/orderedlayout[position()=3]/panel)">
				<td width="0px">
				</td>
				</xsl:if>
			</tr>
		</table>
		<!-- 
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
		-->
	</xsl:template>

</xsl:stylesheet>
