<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">        


<xsl:template match="tree[@style='menu']" mode="core">

  <xsl:variable name="class">tree<xsl:if test="(./@style) and (string-length(./@style) &gt; 0)">-<xsl:value-of select="./@style"/></xsl:if></xsl:variable>

  <DIV CLASS="{$class}-body">

    <!-- Create dummy images for expanded/collapsed state -->
    <!-- These are used in javascript to change the images -->
    <xsl:if test="$dhtml">
      <IMG HEIGHT="0" WIDTH="0" ID="{array[@name='collapse']/@id}_IMG" STYLE="display:none;" SRC="{wa:resource('img/tree/menu/expanded.gif')}" ALT=""/>  
      <IMG HEIGHT="0" WIDTH="0" ID="{array[@name='expand']/@id}_IMG" STYLE="display:none;" SRC="{wa:resource('img/tree/menu/collapsed.gif')}" ALT=""/>  
    </xsl:if>
    
    <!-- Actions -->
    <xsl:variable name="actionlistid"><xsl:value-of select="generate-id(./actions)"/></xsl:variable>

    <xsl:for-each select="node|leaf">
      <xsl:apply-templates select="." mode="menu">
        <xsl:with-param name="level">1</xsl:with-param>
        <xsl:with-param name="expandid"><xsl:value-of select="../array[@name='expand']/@id"/></xsl:with-param>
        <xsl:with-param name="collapseid"><xsl:value-of select="../array[@name='collapse']/@id"/></xsl:with-param>
        <xsl:with-param name="class"><xsl:value-of select="$class"/></xsl:with-param>
        <xsl:with-param name="actionlistid"><xsl:value-of select="$actionlistid"/></xsl:with-param>
        <xsl:with-param name="selectedvar" select="../array[@name='selected']"/>
        <xsl:with-param name="root" select=".."/>
      </xsl:apply-templates>
    </xsl:for-each>
  </DIV>

  <!-- Output variables -->
  <xsl:if test="$dhtml">
  	<xsl:call-template name="tree-variables" />
  </xsl:if>
</xsl:template>

<xsl:template match="tree" mode="core">

  <xsl:variable name="class">tree<xsl:if test="(./@style) and (string-length(./@style) &gt; 0)">-<xsl:value-of select="./@style"/></xsl:if></xsl:variable>

  <!-- Create dummy images for expanded/collapsed state -->
  <!-- These are used in javascript to change the images -->
    <xsl:if test="$dhtml">
      <IMG HEIGHT="0" WIDTH="0" ID="{array[@name='collapse']/@id}_IMG" STYLE="display:none;" SRC="{wa:resource('img/tree/expanded.gif')}" ALT=""/>  
      <IMG HEIGHT="0" WIDTH="0" ID="{array[@name='expand']/@id}_IMG" STYLE="display:none;" SRC="{wa:resource('img/tree/collapsed.gif')}" ALT=""/>  
    </xsl:if>

  <!-- Actions -->
  <xsl:variable name="actionlistid"><xsl:value-of select="generate-id(./actions)"/></xsl:variable>
  
  <xsl:for-each select="node|leaf">
    <xsl:apply-templates select="." mode="tree">
      <xsl:with-param name="expandid"><xsl:value-of select="../array[@name='expand']/@id"/></xsl:with-param>
      <xsl:with-param name="collapseid"><xsl:value-of select="../array[@name='collapse']/@id"/></xsl:with-param>
      <xsl:with-param name="class"><xsl:value-of select="$class"/></xsl:with-param>
      <xsl:with-param name="actionlistid"><xsl:value-of select="$actionlistid"/></xsl:with-param>
      <xsl:with-param name="selectedvar" select="../array[@name='selected']"/>
      <xsl:with-param name="root" select=".."/>
    </xsl:apply-templates>
  </xsl:for-each>


  <!-- Output variables -->  
  <xsl:if test="$dhtml">
  	<xsl:call-template name="tree-variables" />
  </xsl:if>
	
</xsl:template>


<!-- Tree node template -->

<xsl:template match="node" mode="tree">
<xsl:param name="expandid"/>
<xsl:param name="collapseid"/>
<xsl:param name="class"/>
<xsl:param name="actionlistid"/>
<xsl:param name="selectedvar"/>
<xsl:param name="root"/>

<xsl:variable name="isLastNode" select="not(following-sibling::node | following-sibling::leaf)" />
<xsl:variable name="isLeafNode" select="local-name()='leaf'" />
<xsl:variable name="childid"><xsl:value-of select="$expandid"/>_<xsl:value-of select="@key"/></xsl:variable>

<TABLE BORDER="0" CELLPADDING="0" CELLSPACING="0">
  <TR>
    <TD>     
      <!-- Check if node has following siblings -->
      <xsl:if test="not($isLastNode)">
		<xsl:attribute name="BACKGROUND"><xsl:value-of select="wa:resource('img/tree/dots.gif')"/> 
		</xsl:attribute>
	  </xsl:if>

            <xsl:choose>
              <xsl:when test="$dhtml">           
            	<IMG HEIGHT="13" WIDTH="13" ID="img{$childid}" BORDER="0" CLASS="{$class}">
            	<xsl:attribute name="ONCLICK">Millstone.treeExpClick('<xsl:value-of select="$expandid"/>','<xsl:value-of select="$collapseid"/>','<xsl:value-of select="@key"/>','<xsl:value-of select="$root/@immediate"/>')</xsl:attribute>
            	<xsl:choose>
              	  <xsl:when test="@expanded">
                	<xsl:attribute name="SRC"><xsl:value-of select="wa:resource('img/tree/expanded.gif')"/></xsl:attribute>
                	<xsl:attribute name="ALT">-</xsl:attribute>
              	  </xsl:when>
              	  <xsl:otherwise>
                	<xsl:attribute name="SRC"><xsl:value-of select="wa:resource('img/tree/collapsed.gif')"/></xsl:attribute>
                	<xsl:attribute name="ALT">+</xsl:attribute>
              	  </xsl:otherwise>
            	</xsl:choose>
                </IMG>
          	  </xsl:when>

          	  <!-- Non-js version using image inputs -->
          	  <xsl:otherwise>
            	<INPUT TYPE="image" ID="img{$childid}" BORDER="0" CLASS="{$class}">
            	<xsl:choose>
              	  <xsl:when test="@expanded">
            		<xsl:attribute name="NAME">set:<xsl:value-of select="$collapseid"/>=<xsl:value-of select="@key"/></xsl:attribute>
                	<xsl:attribute name="SRC"><xsl:value-of select="wa:resource('img/tree/expanded.gif')"/></xsl:attribute>
                	<xsl:attribute name="ALT">-</xsl:attribute>
              	  </xsl:when>
              	  <xsl:otherwise>
            		<xsl:attribute name="NAME">set:<xsl:value-of select="$expandid"/>=<xsl:value-of select="@key"/></xsl:attribute>
                	<xsl:attribute name="SRC"><xsl:value-of select="wa:resource('img/tree/collapsed.gif')"/></xsl:attribute>
                	<xsl:attribute name="ALT">+</xsl:attribute>
              	  </xsl:otherwise>
            	</xsl:choose>
                </INPUT>
          	  </xsl:otherwise>
          	</xsl:choose>

    </TD>
    
    <!-- Icon and caption cell -->
    <TD>
      <xsl:call-template name="tree-caption">
  		<xsl:with-param name="class"><xsl:value-of select="$class"/>-node<xsl:if test="@selected">-selected</xsl:if></xsl:with-param>
  		<xsl:with-param name="actionlistid"><xsl:value-of select="$actionlistid"/></xsl:with-param>
        <xsl:with-param name="selectedvar" select="$selectedvar"/>
        <xsl:with-param name="root" select="$root"/>
      </xsl:call-template>    
	</TD>	
  </TR>
  
  <!-- Subnodes -->
  <xsl:if test="node|leaf">
    <TR>
      <xsl:attribute name="ID"><xsl:value-of select="$childid"/></xsl:attribute>    
      <TD>
        <xsl:if test="not(@expanded='true')">
          <xsl:attribute name="STYLE">display:none;</xsl:attribute>
        </xsl:if>
        <!-- Following siblings -->
        <xsl:if test="not($isLastNode)">
          <xsl:attribute name="BACKGROUND"><xsl:value-of select="wa:resource('img/tree/dots.gif')"/></xsl:attribute>
        </xsl:if>
      </TD>
      <TD>
		<xsl:apply-templates select="leaf|node" mode="tree">
			<xsl:with-param name="expandid"><xsl:value-of select="$expandid"/></xsl:with-param>
			<xsl:with-param name="collapseid"><xsl:value-of select="$collapseid"/></xsl:with-param>
			<xsl:with-param name="class"><xsl:value-of select="$class"/></xsl:with-param>
			<xsl:with-param name="actionlistid"><xsl:value-of select="$actionlistid"/></xsl:with-param>
            <xsl:with-param name="selectedvar" select="$selectedvar"/>
            <xsl:with-param name="root" select="$root"/>
		</xsl:apply-templates>
      </TD>
    </TR>
  </xsl:if>
</TABLE>
</xsl:template>


<!-- Tree leaf template -->

<xsl:template match="leaf" mode="tree">
<xsl:param name="expandid"/>
<xsl:param name="collapseid"/>
<xsl:param name="class"/>
<xsl:param name="actionlistid"/>
<xsl:param name="selectedvar"/>
<xsl:param name="root"/>

<xsl:variable name="isLastNode" select="not(following-sibling::node | following-sibling::leaf)" />
<xsl:variable name="childid"><xsl:value-of select="$expandid"/>_<xsl:value-of select="@key"/></xsl:variable>

<TABLE BORDER="0" CELLPADDING="0" CELLSPACING="0">
  <TR>
    <TD>     
      <!-- Check if node has following siblings -->
	  <xsl:if test="not($isLastNode)">
		<xsl:attribute name="BACKGROUND"><xsl:value-of select="wa:resource('img/tree/dots.gif')"/></xsl:attribute>
	  </xsl:if>

      <IMG HEIGHT="13" WIDTH="13" BORDER="0" CLASS="{$class}" ALT="">
        <xsl:attribute name="SRC"><xsl:value-of select="wa:resource('img/tree/leaf.gif')"/></xsl:attribute>
      </IMG>
    </TD>

    <!-- Icon and caption cell -->
    <TD>          
      <xsl:call-template name="tree-caption">
  		<xsl:with-param name="class"><xsl:value-of select="$class"/>-node<xsl:if test="@selected">-selected</xsl:if></xsl:with-param>
  		<xsl:with-param name="actionlistid"><xsl:value-of select="$actionlistid"/></xsl:with-param>
        <xsl:with-param name="selectedvar" select="$selectedvar"/>
        <xsl:with-param name="root" select="$root"/>
      </xsl:call-template>
	</TD>	
  </TR>
</TABLE>

</xsl:template>



<!-- Tree node/leaf Caption Template -->

<xsl:template name="tree-caption">
  
  <xsl:param name="class"/>
  <xsl:param name="selectedvar"/>
  <xsl:param name="actionlistid"/>
  <xsl:param name="root"/>

  <NOBR>
  <!-- Action lists in javascript mode-->
  <xsl:if test="$dhtml">
    <xsl:apply-templates select="./al" mode="dhtml">
      <xsl:with-param name="actionlistid"><xsl:value-of select="$actionlistid" /></xsl:with-param>
    </xsl:apply-templates>   
  </xsl:if>
  
  <!-- Selection -->
  <xsl:choose>
    <xsl:when test="($root/@selectmode='multi') or ($root/@selectmode='single')">
      <xsl:choose>
      
        <!-- DHTML/Javascript selection -->
        <xsl:when test="$dhtml">
  		  <A>      
    		<!-- Current selection state -->
    		<xsl:attribute name="CLASS"><xsl:value-of select="$class" /></xsl:attribute>    
      	  	<xsl:variable name="selid"><xsl:value-of select="$selectedvar/@id"/>_<xsl:value-of select="@key"/></xsl:variable>  
			<xsl:attribute name="ID"><xsl:value-of select="$selid"/></xsl:attribute>
            <xsl:if test="not($root/@readonly)">
      	  	  <xsl:attribute name="HREF">javascript:Millstone.treeSelClick('<xsl:value-of select="$selectedvar/@id"/>','<xsl:value-of select="@key"/>','<xsl:value-of select="$root/@immediate"/>','<xsl:value-of select="$root/@selectmode"/>');</xsl:attribute>
      	  	</xsl:if>
      		<xsl:if test="@icon"><IMG class="icon" SRC="{@icon}" BORDER="0" /></xsl:if>
      		<xsl:value-of select="@caption" />
  		  </A>              
      	</xsl:when>
      	
      	<!-- Non-DHTML/Javascript version uses submit for selection -->
      	<xsl:otherwise>
   		    <xsl:if test="@icon"><IMG class="icon" SRC="{@icon}" BORDER="0" /></xsl:if>
            <!-- Radiobutton or button for selection -->
            <INPUT>
                <xsl:choose>
                
                  <!-- Non-JS Immediate as submit buttons -->
                  <xsl:when test="$root/@immediate='true'">
                    <xsl:attribute name="TYPE">SUBMIT</xsl:attribute>
                	<xsl:choose>                	  
                  	  <xsl:when test="$root/@selectmode='multi'">
                  	    <xsl:variable name="selected" select="@selected"/>
                  	    <xsl:variable name="key" select="@key"/>
                        <xsl:attribute name="NAME">set-array:<xsl:value-of select="$selectedvar/@id"
                        />=<xsl:for-each select="$selectedvar/ai"
                              ><xsl:if test="not($selected) or ($key!=text())"><xsl:value-of select="text()"
                              />,</xsl:if></xsl:for-each
                              ><xsl:if test="not($selected)"><xsl:value-of select="$key"/></xsl:if>
					    </xsl:attribute>
                  	   </xsl:when>                  	   
                       <xsl:otherwise>                   
                         <xsl:attribute name="NAME">set:<xsl:value-of select="$selectedvar/@id"/>=<xsl:value-of select="@key"/></xsl:attribute>
                  	   </xsl:otherwise>
                	 </xsl:choose>
                     <xsl:attribute name="VALUE"><xsl:value-of select="@caption"/></xsl:attribute>
    				 <xsl:attribute name="CLASS"><xsl:value-of select="$class" /></xsl:attribute>    
                  </xsl:when>
                  
                  <!-- non-JS and not immeadiate as check-/radioboxes -->
                  <xsl:otherwise>                   
                    <xsl:attribute name="TYPE">
                      <xsl:choose>
                        <xsl:when test="$root/@selectmode='multi'">CHECKBOX</xsl:when>
                        <xsl:otherwise>RADIO</xsl:otherwise>
                      </xsl:choose>  
                    </xsl:attribute>
                    <xsl:attribute name="NAME"><xsl:value-of select="$selectedvar/@id"/></xsl:attribute>
                    <xsl:attribute name="VALUE"><xsl:value-of select="@key"/></xsl:attribute>
             		<xsl:if test="@selected='true'">
                	  <xsl:attribute name="CHECKED">CHECKED</xsl:attribute>
              		</xsl:if>
                  </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="$root/@readonly">
                  <xsl:attribute name="DISABLED">true</xsl:attribute>
                </xsl:if>
              </INPUT>   		    
             <xsl:if test="not($root/@immediate='true')">
               <xsl:value-of select="@caption" />
             </xsl:if>
      	</xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    
    <!-- Non-selectable tree caption -->
    <xsl:otherwise>
      <NOBR>
        <xsl:if test="@icon"><IMG class="icon" SRC="{@icon}" BORDER="0" /></xsl:if>
        <xsl:value-of select="@caption" />
      </NOBR>
    </xsl:otherwise>
  </xsl:choose>

  <!-- Action lists in non-javascript mode-->
  <xsl:if test="not($dhtml)">
    <xsl:apply-templates select="./al" mode="inline">
      <xsl:with-param name="actionsvar" select="$root/actions" />
    </xsl:apply-templates>   
  </xsl:if>
  
  </NOBR>
  
</xsl:template>



<!-- Menu node template -->

<xsl:template match="node" mode="menu">
<xsl:param name="level"/>
<xsl:param name="expandid"/>
<xsl:param name="collapseid"/>
<xsl:param name="class"/>
<xsl:param name="actionlistid"/>
<xsl:param name="selectedvar"/>
<xsl:param name="root"/>

<xsl:variable name="isLastNode" select="not(following-sibling::node | following-sibling::leaf)" />
<xsl:variable name="isSelectable" select="not($root/@readonly)  and (($root/@selectmode='multi') or ($root/@selectmode='single'))" />	
<xsl:variable name="childid"><xsl:value-of select="$expandid"/>_<xsl:value-of select="@key"/></xsl:variable>
  <DIV>
    <!-- Current selection state -->
  	<xsl:attribute name="CLASS"><xsl:value-of select="$class" />-<xsl:value-of select="$level" /><xsl:if test="@selected and ($dhtml or $root/@immediate='true')">-selected</xsl:if></xsl:attribute>

    <!-- ID for style change functions -->
    <xsl:if test="$isSelectable and $dhtml">
      <xsl:variable name="selid"><xsl:value-of select="$selectedvar/@id"/>_<xsl:value-of select="@key"/></xsl:variable>  
      <xsl:attribute name="ID"><xsl:value-of select="$selid"/></xsl:attribute>
    </xsl:if>
    
    <TABLE border="0" cellpadding="0" cellspacing="0"><TR><TD>
            <xsl:choose>
              <xsl:when test="$dhtml">           
               <A>
             	<xsl:attribute name="HREF">javascript:Millstone.treeExpClick('<xsl:value-of select="$expandid"/>','<xsl:value-of select="$collapseid"/>','<xsl:value-of select="@key"/>','<xsl:value-of select="$root/@immediate"/>')</xsl:attribute>
            	<IMG HEIGHT="13" WIDTH="13" ID="img{$childid}" BORDER="0" CLASS="tree-menu-exp">
            	<xsl:choose>
              	  <xsl:when test="@expanded">
                	<xsl:attribute name="SRC"><xsl:value-of select="wa:resource('img/tree/menu/expanded.gif')"/></xsl:attribute>
                	<xsl:attribute name="ALT">-</xsl:attribute>
              	  </xsl:when>
              	  <xsl:otherwise>
                	<xsl:attribute name="SRC"><xsl:value-of select="wa:resource('img/tree/menu/collapsed.gif')"/></xsl:attribute>
                	<xsl:attribute name="ALT">+</xsl:attribute>
              	  </xsl:otherwise>
            	</xsl:choose>
                </IMG>
               </A>
          	  </xsl:when>

          	  <!-- Non-js expand/collapse using image inputs -->
          	  <xsl:otherwise>
            	<INPUT TYPE="image" ID="img{$childid}" BORDER="0" CLASS="{$class}">
            	<xsl:choose>
              	  <xsl:when test="@expanded">
            		<xsl:attribute name="NAME">set:<xsl:value-of select="$collapseid"/>=<xsl:value-of select="@key"/></xsl:attribute>
                	<xsl:attribute name="SRC"><xsl:value-of select="wa:resource('img/tree/menu/expanded.gif')"/></xsl:attribute>
                	<xsl:attribute name="ALT">-</xsl:attribute>
              	  </xsl:when>
              	  <xsl:otherwise>
            		<xsl:attribute name="NAME">set:<xsl:value-of select="$expandid"/>=<xsl:value-of select="@key"/></xsl:attribute>
                	<xsl:attribute name="SRC"><xsl:value-of select="wa:resource('img/tree/menu/collapsed.gif')"/></xsl:attribute>
                	<xsl:attribute name="ALT">+</xsl:attribute>
              	  </xsl:otherwise>
            	</xsl:choose>
                </INPUT>
          	  </xsl:otherwise>
          	</xsl:choose>
      
      </TD><TD>   	   
      <!-- Icon and caption -->
      <xsl:call-template name="tree-caption">
  		<xsl:with-param name="class"><xsl:value-of select="$class"/>-<xsl:value-of select="$level" /><xsl:if test="@selected">-selected</xsl:if></xsl:with-param>
  		<xsl:with-param name="actionlistid"><xsl:value-of select="$actionlistid"/></xsl:with-param>
        <xsl:with-param name="selectedvar" select="$selectedvar"/>
        <xsl:with-param name="root" select="$root"/>
      </xsl:call-template>    
    </TD></TR></TABLE>
  </DIV>
  <xsl:if test="node|leaf">
    <DIV CLASS="{$class}-{$level}-child">
        <xsl:attribute name="ID"><xsl:value-of select="$childid"/></xsl:attribute>    
        <xsl:apply-templates select="leaf|node" mode="menu">
          <xsl:with-param name="level"><xsl:value-of select="$level + 1"/></xsl:with-param>
          <xsl:with-param name="expandid"><xsl:value-of select="$expandid"/></xsl:with-param>
          <xsl:with-param name="collapseid"><xsl:value-of select="$collapseid"/></xsl:with-param>
          <xsl:with-param name="class"><xsl:value-of select="$class"/></xsl:with-param>
          <xsl:with-param name="actionlistid"><xsl:value-of select="$actionlistid"/></xsl:with-param>
          <xsl:with-param name="selectedvar" select="$selectedvar"/>
          <xsl:with-param name="root" select="$root"/>
        </xsl:apply-templates>
    </DIV>
  </xsl:if>
</xsl:template>



<!-- Menu leaf template -->

<xsl:template match="leaf" mode="menu">
<xsl:param name="level"/>
<xsl:param name="expandid"/>
<xsl:param name="collapseid"/>
<xsl:param name="class"/>
<xsl:param name="actionlistid"/>
<xsl:param name="selectedvar"/>
<xsl:param name="root"/>

<xsl:variable name="isLastNode" select="not(following-sibling::node | following-sibling::leaf)" />
<xsl:variable name="isSelectable" select="not($root/@readonly)  and (($root/@selectmode='multi') or ($root/@selectmode='single'))" />	
<xsl:variable name="childid"><xsl:value-of select="$expandid"/>_<xsl:value-of select="@key"/></xsl:variable>
  <DIV>
    <!-- Current selection state -->
  	<xsl:attribute name="CLASS"><xsl:value-of select="$class" />-<xsl:value-of select="$level" /><xsl:if test="@selected and ($dhtml or $root/@immediate='true')">-selected</xsl:if></xsl:attribute>

    <!-- ID for style change functions -->
    <xsl:if test="$isSelectable and $dhtml">
      <xsl:variable name="selid"><xsl:value-of select="$selectedvar/@id"/>_<xsl:value-of select="@key"/></xsl:variable>  
      <xsl:attribute name="ID"><xsl:value-of select="$selid"/></xsl:attribute>
    </xsl:if>
    
    <TABLE border="0" cellpadding="0" cellspacing="0"><TR><TD> <!-- This fixes the rendering in ie -->
      <IMG HEIGHT="13" WIDTH="13" ALT="" SRC="{wa:resource('img/tree/menu/leaf.gif')}" ID="img{$childid}" BORDER="0" CLASS="tree-menu-exp"/>
      </TD><TD>
      <!-- Actions Icon and caption -->
      <xsl:call-template name="tree-caption">
  		<xsl:with-param name="class"><xsl:value-of select="$class"/>-<xsl:value-of select="$level" /><xsl:if test="@selected">-selected</xsl:if></xsl:with-param>
  		<xsl:with-param name="actionlistid"><xsl:value-of select="$actionlistid"/></xsl:with-param>
        <xsl:with-param name="selectedvar" select="$selectedvar"/>
        <xsl:with-param name="root" select="$root"/>
      </xsl:call-template>
    </TD></TR></TABLE>
  </DIV>
</xsl:template>



<!-- Tree variables -->

<xsl:template name="tree-variables">	
  <!-- Selection variable -->
  <xsl:if test="(@selectmode='single' or @selectmode='multi')">
    <xsl:for-each select="./array[@name='selected']">
      <INPUT TYPE="HIDDEN" NAME="declare:{@id}"/>
      <INPUT TYPE="HIDDEN" ID="{@id}" NAME="array:{@id}">
        <xsl:attribute name="VALUE">
          <xsl:for-each select="ai">
            <xsl:value-of select="text()"/>
            <xsl:if test="following-sibling::*">,</xsl:if>  
          </xsl:for-each>
        </xsl:attribute>
      </INPUT>
    </xsl:for-each>
  </xsl:if>	
  
  <!-- Expand variable -->
  <xsl:for-each select="./array[@name='expand']">    
    <INPUT TYPE="HIDDEN" ID="{@id}" NAME="array:{@id}" />
  </xsl:for-each>  

  <!-- Collapse variable -->
  <xsl:for-each select="./array[@name='collapse']">
    <INPUT TYPE="HIDDEN" ID="{@id}" NAME="array:{@id}" />
  </xsl:for-each>  

</xsl:template>

</xsl:stylesheet>

