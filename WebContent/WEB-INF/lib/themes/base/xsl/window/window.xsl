<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="window">


  <HTML>
    <HEAD>
      <xsl:call-template name="window-head"/>
    </HEAD>    
    <BODY>
    
      <!-- Set class by window style -->
	  <xsl:if test="string-length(@style) &gt; 0"><xsl:attribute name="CLASS"><xsl:value-of select="@style"/></xsl:attribute></xsl:if>
    
      <!-- Special handling of modal windows -->
      <xsl:if test="$dhtml and (@style='modal')"><SCRIPT>Millstone.makeModal(window);</SCRIPT></xsl:if>	 
      
	  <!-- Window resize variable ids -->
      <xsl:variable name="heightid"><xsl:value-of select="./integer[@name='height']/@id"/></xsl:variable>
      <xsl:variable name="widthid"><xsl:value-of select="./integer[@name='width']/@id"/></xsl:variable>

	  <!-- Window scrolling variable ids -->
	  <xsl:variable name="scrolldownid"><xsl:value-of select="./integer[@name='scrolldown']/@id"/></xsl:variable>
	  <xsl:variable name="scrollleftid"><xsl:value-of select="./integer[@name='scrollleft']/@id"/></xsl:variable>

      <!-- Window body attributes initialization -->
	  <xsl:if test="$dhtml">
	  
	    <!-- Run onload-script -->
        <xsl:attribute name="onload">window_onload();</xsl:attribute>

	    <!-- Run onunload-script -->
        <xsl:attribute name="onunload">window_onunload();</xsl:attribute>

        <!-- Capture resize events -->
		<xsl:if test="$dhtml and $heightid and $widthid">
		  <xsl:attribute name="onResize">window_onresize();</xsl:attribute>
		</xsl:if>     
        
        <!-- Capture scroll events -->
		<xsl:if test="$scrolldownid and $scrollleftid">
		  <xsl:attribute name="onscroll">Millstone.setVarById('<xsl:value-of select="$scrolldownid"/>',document.body.scrollTop,false);Millstone.setVarById('<xsl:value-of select="$scrollleftid"/>',document.body.scrollLeft,false)</xsl:attribute>
		</xsl:if>
		
		<!-- Window onload script -->
		<SCRIPT LANGUAGE="JavaScript">

		var chromeX = -1;
		var chromeY = -1;
		  
	    function window_onload() {
			<!-- Initial window size -->
			<xsl:if test="(./integer[@name='height']/@value &gt; 0) and (./integer[@name='width']/@value &gt; 0)">
			    var newWidth = <xsl:value-of select="./integer[@name='width']/@value"/>;
			    var newHeight = <xsl:value-of select="./integer[@name='height']/@value"/>;
			    window.resizeTo(newWidth,newHeight);
				if (window.innerWidth) {
					chromeX = newWidth - window.innerWidth;
					chromeY = newHeight - window.innerHeight;
				} else  {	
					chromeX = newWidth - document.body.clientWidth;
					chromeY = newHeight - document.body.clientHeight;				
				}
			</xsl:if>			
			<!-- Initial scroll position -->
			<xsl:if test="$scrolldownid and $scrollleftid">
			  window.scrollTo(<xsl:value-of select="./integer[@name='scrollleft']/@value"/>,<xsl:value-of select="./integer[@name='scrolldown']/@value"/>);
			</xsl:if>     

			<!-- Open new windows -->
		    <xsl:for-each select="open">
		    	<xsl:variable name="targetName" select="wa:getWindowTargetName('@name')"/>
				Millstone.openWindow('<xsl:value-of select="@src"
		          />','<xsl:value-of select="@targetName"
		          />',<xsl:choose><xsl:when test="@width"><xsl:value-of select="@width"/></xsl:when><xsl:otherwise>-1</xsl:otherwise></xsl:choose
		          >,<xsl:choose><xsl:when test="@height"><xsl:value-of select="@height"/></xsl:when><xsl:otherwise>-1</xsl:otherwise></xsl:choose
		          >,'<xsl:value-of select="@border" />');
		    </xsl:for-each>
		    
		    <!-- Refresh other windows -->
	    	<xsl:value-of select="wa:windowScript()"/>
	    	
			// Invoke all registered listeners
			Millstone.windows.onload();
	    }

	    function window_onunload() {	    
			// Invoke all registered listeners	    
			Millstone.windows.onunload();
	    }
	    
	    function form_onsubmit() {
	    
			// Invoke all registered listeners			
			Millstone.windows.onsubmit();
	    }	    
	    
	    <!-- Resize script -->
	    function window_onresize() {
	    	var w,h;	    	
			if (window.innerWidth) {
				w = window.innerWidth;
				h = window.innerHeight;
			} else {
				w = document.body.clientWidth;
				h = document.body.clientHeight;				
			}
			if(chromeX>=0) Millstone.setVarById('<xsl:value-of select="$widthid"/>',w+chromeX, false);
		  	if(chromeY>=0) Millstone.setVarById('<xsl:value-of select="$heightid"/>',h+chromeY,false);	    
	    }
	    
		</SCRIPT>
	  </xsl:if>     

      
      <!-- Main form -->
      <FORM NAME="millstone" METHOD="POST" ACCEPT-CHARSET="UTF-8" ENCTYPE="multipart/form-data" 
        ACTION="{wa:getFormAction()}">
		
        <xsl:if test="$dhtml">
			<xsl:attribute name="ONSUBMIT">form_onsubmit()</xsl:attribute>
		</xsl:if>
		
  	    <!-- Window size variables -->
        <xsl:if test="$dhtml and $heightid and $widthid">
          <INPUT TYPE="HIDDEN" ID="{$widthid}" NAME="{$widthid}" VALUE="{./integer[@name='width']/@value}"/>
          <INPUT TYPE="HIDDEN" ID="{$heightid}" NAME="{$heightid}" VALUE="{./integer[@name='height']/@value}"/>
        </xsl:if>     

  	    <!-- Window scrolling variables -->
        <xsl:if test="$dhtml and $scrolldownid and $scrollleftid">
          <INPUT TYPE="HIDDEN" ID="{$scrolldownid}" NAME="{$scrolldownid}" VALUE="{./integer[@name='scrolldown']/@value}"/>
          <INPUT TYPE="HIDDEN" ID="{$scrollleftid}" NAME="{$scrollleftid}" VALUE="{./integer[@name='scrollleft']/@value}"/>
        </xsl:if>
             
	    <!-- Focused component variable -->
        <xsl:if test="./string[@name='focused']">
		  <SCRIPT LANGUAGE="JavaScript">
            Millstone.focusable.windowFocusVariableInputId = '<xsl:value-of select="./string[@name='focused']/@id"/>';
		  </SCRIPT>
        </xsl:if>  
               
		<!-- Probe client features -->
  		<xsl:choose>
  		  <xsl:when test="wa:probeClient()">
		    <xsl:call-template name="client-probe" />
		  </xsl:when>
		  <xsl:otherwise>
			<xsl:comment>
			  <xsl:value-of select="browser:toString(wa:browser())"/>
			</xsl:comment>
		  </xsl:otherwise>
		</xsl:choose>
	    
        <!-- Sub component -->
        <xsl:apply-templates/>
        
        <!-- Popup layers -->
        <xsl:if test="$dhtml">
        	<xsl:apply-templates mode="popup"/>
        </xsl:if>

		<!-- Focus -->
        <xsl:if test="./string[@name='focused']">
            <INPUT TYPE="HIDDEN" ID="{./string[@name='focused']/@id}" NAME="{./string[@name='focused']/@id}" VALUE="{./string[@name='focused']}"/>
        </xsl:if> 

      </FORM>
    </BODY>
  </HTML>
</xsl:template>

<xsl:template name="window-head">

  	<xsl:if test="wa:probeClient()">
      <NOSCRIPT>
        <META http-equiv="refresh" content="0; url=?WA_NOSCRIPT=1" />
      </NOSCRIPT>
    </xsl:if>

    <META http-equiv="Content-Type" content="text/html; charset=UTF-8" />    
    <TITLE><xsl:value-of select="@caption" /></TITLE>

	<xsl:value-of select="wa:getCssLinksForHead()" disable-output-escaping="yes"/>

    <xsl:if test="./open[not(@name)] and not($dhtml)">
	    <META http-equiv="Refresh" content="0;{./open/@src}" />
    </xsl:if>

    <xsl:if test="$dhtml">
		<xsl:value-of select="wa:getJavaScriptLinksForHead()" disable-output-escaping="yes"/>
    </xsl:if>

</xsl:template>

<!-- Do not output text by default in popups mode -->
<xsl:template match="text()" mode="popup"></xsl:template>

<!-- Do not output text by default -->
<xsl:template match="window/string"></xsl:template>

</xsl:stylesheet>

