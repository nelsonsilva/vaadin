<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  
    xmlns:wa="java://com.itmill.toolkit.terminal.web.ThemeFunctionLibrary" 
    xmlns:browser="java://com.itmill.toolkit.terminal.web.WebBrowser">

<xsl:template name="client-probe">

    <script language="javascript1.1">
<xsl:comment>
    	var ver11 = "JavaScript 1.1";
// </xsl:comment>
    </script>
    <script language="javascript1.2">
<xsl:comment>
    	var ver12 = "JavaScript 1.2";
// </xsl:comment>
    </script>
    <script language="javascript1.3">
<xsl:comment>
   		var ver13 = "JavaScript 1.3";
// </xsl:comment>
	</script>
    <script language="javascript1.4">
<xsl:comment>
    	var ver14 = "JavaScript 1.4";
// </xsl:comment>
    </script>
    <script language="javascript1.5">
<xsl:comment>
    	var ver15 = "JavaScript 1.5";
// </xsl:comment>
    </script>

    <script language="JavaScript">
<xsl:comment>
    	var ver10;
    	var ver11;
    	var ver12;
    	var ver13;
    	var ver14;
    	var ver15;
    	var jscript;
    	
		/*@cc_on @*/
		/*@if (@_jscript_version)
  			 jscript = "JScript " + @_jscript_version;  			 
   		  @else @*/
   		     jscript = null;
		/*@end @*/

    	
    	var ver = ver10 ? ver10 : 'JavaScript none';
    	ver = ver11 ? ver11 : ver;
    	ver = ver12 ? ver12 : ver;
    	ver = ver13 ? ver13 : ver;
    	ver = ver14 ? ver14 : ver;
    	ver = ver15 ? ver15 : ver;
    	ver = jscript ? jscript: ver;
// </xsl:comment>
    </script>


    <!-- Form variables -->
	<xsl:variable name="type">hidden</xsl:variable>
   	<input id="wa_clientprobe" name="wa_clientprobe" type="{$type}" value="0" />
   	<input id="wa_jsversion" name="wa_jsversion" type="{$type}" value="" />
   	<input id="wa_screenwidth" name="wa_screenwidth" type="{$type}" value="" />
   	<input id="wa_screenheight" name="wa_screenheight" type="{$type}" value="" />
   	<input id="wa_javaenabled" name="wa_javaenabled" type="{$type}" value="" />

  <script language="JavaScript">
<xsl:comment>

    function setVariables() {
    	var form = document.forms["itmilltoolkit"]; if (typeof form == 'undefined') form = document.forms["mill"+"stone"];
    	form.wa_clientprobe.value = "1";
    	form.wa_jsversion.value = ver;
    	form.wa_screenwidth.value = window.screen.width;
    	form.wa_screenheight.value =  window.screen.height;
    	form.wa_javaenabled.value = navigator.javaEnabled();
    }
    
    setVariables();
// </xsl:comment>
  </script>

</xsl:template>

</xsl:stylesheet>

