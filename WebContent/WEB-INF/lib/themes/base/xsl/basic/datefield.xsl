<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">


<!-- Default style date field -->

<xsl:template match="datefield" mode="core">

  <xsl:choose>
    <xsl:when test="$dhtml and @style='calendar'">
      <xsl:apply-templates select="." mode="calendar" />
    </xsl:when>
    <xsl:otherwise>	
      <SPAN CLASS="datefield-date">
        <xsl:apply-templates mode="datefield" select="integer[@name='year']"/>
        <xsl:apply-templates mode="datefield-dropdown" select="integer[@name='month']"/>
	    <xsl:apply-templates mode="datefield-dropdown" select="integer[@name='day']"/>
      </SPAN>
      <SPAN CLASS="datefield-time">
	   <NOBR>
        <xsl:apply-templates mode="datefield" select="integer[@name='hour']"/>
        <xsl:apply-templates mode="datefield" select="integer[@name='min']"/>
        <xsl:apply-templates mode="datefield" select="integer[@name='sec']"/>
        <xsl:apply-templates mode="datefield" select="integer[@name='msec']"/>
	   </NOBR>
      </SPAN>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<!-- Text style date field -->

<xsl:template match="datefield[@style='text']" mode="core">
	<SPAN CLASS="datefield-date">
	  <xsl:apply-templates mode="datefield" select="integer[@name='year']"/>
	  <xsl:apply-templates mode="datefield" select="integer[@name='month']"/>
	  <xsl:apply-templates mode="datefield" select="integer[@name='day']"/>
	</SPAN>
	<SPAN CLASS="datefield-time">
     <NOBR>
	  <xsl:apply-templates mode="datefield" select="integer[@name='hour']"/>
	  <xsl:apply-templates mode="datefield" select="integer[@name='min']"/>
	  <xsl:apply-templates mode="datefield" select="integer[@name='sec']"/>
	  <xsl:apply-templates mode="datefield" select="integer[@name='msec']"/>
	 </NOBR>
	</SPAN>
</xsl:template>


<!-- Calendar style -->

<xsl:template match="datefield[@style='calendar']" mode="calendar">

  <xsl:variable name="calendarid"><xsl:value-of select="generate-id()"/></xsl:variable>
  <xsl:variable name="weekbegin"><xsl:value-of select="wa:getFirstDayOfWeek()"/></xsl:variable>

  <TABLE>
    <TR>
      <TD CLASS="datefield-date">

        <!-- Year selector -->
        <xsl:apply-templates select="integer[@name='year']" mode="datefield">
          <xsl:with-param name="calendarid"><xsl:value-of select="$calendarid"/></xsl:with-param>
          <xsl:with-param name="weekbegin"><xsl:value-of select="$weekbegin"/></xsl:with-param>
        </xsl:apply-templates>  
  
        <!-- Month selector -->
        <xsl:apply-templates select="integer[@name='month']" mode="datefield-dropdown">
          <xsl:with-param name="calendarid"><xsl:value-of select="$calendarid"/></xsl:with-param>
          <xsl:with-param name="weekbegin"><xsl:value-of select="$weekbegin"/></xsl:with-param>
        </xsl:apply-templates>  
    
        <!-- Day selector --> 
        <xsl:apply-templates select="integer[@name='day']" mode="datefield-calendar">
          <xsl:with-param name="calendarid"><xsl:value-of select="$calendarid"/></xsl:with-param>
          <xsl:with-param name="weekbegin"><xsl:value-of select="$weekbegin"/></xsl:with-param>
        </xsl:apply-templates>  

        <!-- Select the current day -->
        <xsl:variable name="yearid"><xsl:value-of select="./integer[@name='year']/@id"/></xsl:variable>
        <xsl:variable name="monthid"><xsl:value-of select="./integer[@name='month']/@id"/></xsl:variable>
        <xsl:variable name="dayid"><xsl:value-of select="./integer[@name='day']/@id"/></xsl:variable>
        
        <xsl:if test="$dhtml">
          <SCRIPT LANGUAGE="JavaScript">
            Millstone.updateCalendar('<xsl:value-of select="$calendarid"/>','<xsl:value-of select="$yearid"/>','<xsl:value-of select="$monthid"/>','<xsl:value-of select="$dayid"/>','<xsl:value-of select="$weekbegin"/>');
            <xsl:if test="./integer[@name='day']">
                Millstone.calendarDaySelect('<xsl:value-of select="$calendarid"/>',<xsl:value-of select="./integer[@name='day']/@value"/>);
             </xsl:if>
          </SCRIPT>
        </xsl:if>
      </TD>

      <!-- Time -->
      <TD CLASS="datefield-time">
        <SPAN CLASS="datefield-time">
	     <NOBR>        
          <xsl:apply-templates mode="datefield" select="integer[@name='hour']"/>
          <xsl:apply-templates mode="datefield" select="integer[@name='min']"/>
          <xsl:apply-templates mode="datefield" select="integer[@name='sec']"/>
          <xsl:apply-templates mode="datefield" select="integer[@name='msec']"/>
	     </NOBR>
        </SPAN>
      </TD>
    </TR>
  </TABLE>
</xsl:template>







<!-- Year selectors for different styles -->

<xsl:template match="integer[@name='year']" mode="datefield">
  <xsl:param name="calendarid"/>
  <xsl:param name="weekbegin"/>
  <xsl:variable name="class">datefield<xsl:if test="string-length(../@style) &gt; 0">-<xsl:value-of select="../@style"/></xsl:if></xsl:variable>

  <!-- Format value -->
  <xsl:variable name="value">
    <xsl:choose>
      <xsl:when test="@value &gt;= 0"><xsl:value-of select="format-number(@value,'0000')"/></xsl:when>
      <xsl:otherwise></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:choose>
  
    <!-- Text only -->
    <xsl:when test="(../@readonly) and (../@style='text')"><xsl:value-of select="$value" /></xsl:when>


    <!-- Editor for year -->
    <xsl:otherwise>
      <INPUT TYPE="text" SIZE="4" MAXLENGTH="4" ID="{@id}" NAME="{@id}" VALUE="{$value}" CLASS="{$class}-y">
       <xsl:if test="../@disabled='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
       <xsl:if test="../@readonly='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
       <xsl:if test="../@tabindex"><xsl:attribute name="tabindex"><xsl:value-of select="../@tabindex"/></xsl:attribute></xsl:if>
       <xsl:if test="../@focusid"><xsl:attribute name="FOCUSID"><xsl:value-of select="../@focusid"/></xsl:attribute></xsl:if>
       
       <!-- calendar is updated on change -->
       <xsl:choose>       
         <xsl:when test="$dhtml and (../@style='calendar')">
           <xsl:variable name="monthid"><xsl:value-of select="../integer[@name='month']/@id"/></xsl:variable>  
           <xsl:variable name="dayid"><xsl:value-of select="../integer[@name='day']/@id"/></xsl:variable>  
           <xsl:attribute name="ONCHANGE">Millstone.updateCalendar('<xsl:value-of select="$calendarid"/>','<xsl:value-of select="@id"/>','<xsl:value-of select="$monthid"/>','<xsl:value-of select="$dayid"/>','<xsl:value-of select="$weekbegin"/>','<xsl:value-of select="../@immediate"/>');</xsl:attribute>
         </xsl:when>
         <xsl:when test="$dhtml and (../@immediate)">
           <xsl:attribute name="ONCHANGE">Millstone.submit();</xsl:attribute>
         </xsl:when>        
       </xsl:choose>
      </INPUT>

    </xsl:otherwise>
  </xsl:choose>

  
</xsl:template>


<!-- Month selector styles -->

<xsl:template match="integer[@name='month']" mode="datefield">

  <!-- Format value -->
  <xsl:variable name="value">
    <xsl:choose>
      <xsl:when test="@value &gt;= 0"><xsl:value-of select="format-number(@value,'00')"/></xsl:when>
      <xsl:otherwise></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:choose>
  
    <!-- Text only -->
    <xsl:when test="(../@readonly) and (../@style='text')">-<xsl:value-of select="$value" /></xsl:when>
    
    <!-- Editor -->
    <xsl:otherwise>
      <xsl:variable name="class">datefield<xsl:if test="string-length(../@style) &gt; 0">-<xsl:value-of select="../@style"/></xsl:if></xsl:variable>
      - <INPUT TYPE="text" SIZE="2" MAXLENGTH="2" ID="{@id}" NAME="{@id}" VALUE="{$value}" CLASS="{$class}-m">
        <xsl:if test="../@disabled='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
        <xsl:if test="$dhtml and (../@immediate)">
            <xsl:attribute name="ONCHANGE">Millstone.submit();</xsl:attribute>
        </xsl:if>
      </INPUT>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<xsl:template match="integer[@name='month']" mode="datefield-dropdown">
  <xsl:param name="calendarid"/>
  <xsl:param name="weekbegin"/>
  
  <xsl:variable name="class">datefield<xsl:if test="string-length(../@style) &gt; 0">-<xsl:value-of select="../@style"/></xsl:if></xsl:variable>
  
      - <SELECT ID="{@id}" 
              NAME="{@id}" 
              CLASS="{$class}-m">

        <xsl:if test="../@disabled='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
        <xsl:if test="../@readonly='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>

        <!-- calendar is updated on change -->
        <xsl:if test="$dhtml">
            <xsl:variable name="yearid"><xsl:value-of select="../integer[@name='year']/@id"/></xsl:variable>
            <xsl:variable name="dayid"><xsl:value-of select="../integer[@name='day']/@id"/></xsl:variable>  
            <xsl:attribute name="ONCHANGE">Millstone.updateCalendar('<xsl:value-of select="$calendarid"/>','<xsl:value-of select="$yearid"/>','<xsl:value-of select="@id"/>','<xsl:value-of select="$dayid"/>','<xsl:value-of select="$weekbegin"/>','<xsl:value-of select="../@immediate"/>');</xsl:attribute>
        </xsl:if>

    	<!-- Generate months for selector -->    
        <xsl:call-template name="calendar-month">
          <xsl:with-param name="month">1</xsl:with-param>
          <xsl:with-param name="selectedmonth"><xsl:value-of select="@value"/></xsl:with-param>
        </xsl:call-template>             
      </SELECT>

</xsl:template>


<!-- Day selectors -->

<xsl:template match="integer[@name='day']" mode="datefield">

  <!-- Format value -->
  <xsl:variable name="value">
    <xsl:choose>
      <xsl:when test="@value &gt;= 0"><xsl:value-of select="format-number(@value,'00')"/></xsl:when>
      <xsl:otherwise></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:choose>

    <!-- Text only -->    
    <xsl:when test="(../@readonly) and (../@style='text')">-<xsl:value-of select="$value" /></xsl:when>

    <!-- Day Editor -->
    <xsl:otherwise>
      <xsl:variable name="class">datefield<xsl:if test="string-length(../@style) &gt; 0">-<xsl:value-of select="../@style"/></xsl:if></xsl:variable>
      - <INPUT TYPE="text" SIZE="2" MAXLENGTH="2" NAME="{@id}" VALUE="{$value}" CLASS="{$class}-d">
        <xsl:if test="../@disabled='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>

        <xsl:if test="$dhtml and (../@immediate)">
            <xsl:attribute name="ONCHANGE">Millstone.submit();</xsl:attribute>
        </xsl:if>
      </INPUT>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<xsl:template match="integer[@name='day']" mode="datefield-dropdown">
  <xsl:param name="calendarid"/>
  <xsl:param name="weekbegin"/>

  <xsl:variable name="class">datefield<xsl:if test="string-length(../@style) &gt; 0">-<xsl:value-of select="../@style"/></xsl:if></xsl:variable>

      - <SELECT ID="{@id}" 
                NAME="{@id}" 
                CLASS="{$class}-d">

        <xsl:if test="../@disabled='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
        <xsl:if test="../@readonly='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>

        <!-- calendar is updated on change -->
        <xsl:if test="$dhtml and (../@immediate)">
          <xsl:attribute name="ONCHANGE">Millstone.submit();</xsl:attribute>
        </xsl:if>
        
    	<!-- Generate days for selector -->          
        <xsl:call-template name="calendar-day">
          <xsl:with-param name="day">1</xsl:with-param>
          <xsl:with-param name="selectedday"><xsl:value-of select="@value"/></xsl:with-param>
        </xsl:call-template>      
      </SELECT>
</xsl:template>


<xsl:template match="integer[@name='day']" mode="datefield-calendar">
  <xsl:param name="calendarid"/>
  <xsl:param name="weekbegin"/>

  <xsl:variable name="yearid"><xsl:value-of select="../integer[@name='year']/@id"/></xsl:variable>
  <xsl:variable name="monthid"><xsl:value-of select="../integer[@name='month']/@id"/></xsl:variable>  
  <xsl:variable name="class">datefield<xsl:if test="string-length(../@style) &gt; 0">-<xsl:value-of select="../@style"/></xsl:if></xsl:variable>

  <INPUT TYPE="hidden" SIZE="2" MAXLENGTH="2" ID="{@id}" NAME="{@id}" VALUE="{@value}" CLASS="{$class}-d">
    <xsl:if test="../@disabled='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
    <xsl:if test="(../@disabled='true')"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
  </INPUT>  

  <TABLE CLASS="{$class}-d">  
    <!-- Weekday titles -->
    <TR>
      <xsl:call-template name="calendar-weekdaytitle">
        <xsl:with-param name="weekday">0</xsl:with-param>
        <xsl:with-param name="weekbegin"><xsl:value-of select="$weekbegin"/></xsl:with-param>
      </xsl:call-template>
	</TR>
	
	<!-- Weeks in month -->  
    <xsl:call-template name="calendar-weeks">
      <xsl:with-param name="calendarid"><xsl:value-of select="$calendarid"/></xsl:with-param>
  	  <xsl:with-param name="week">0</xsl:with-param>
      <xsl:with-param name="dayid"><xsl:value-of select="@id"/></xsl:with-param>
      <xsl:with-param name="immediate"><xsl:value-of select="../@immediate"/></xsl:with-param>
    </xsl:call-template>
  </TABLE>
</xsl:template>
    

<!-- Hour template -->

<xsl:template match="integer[@name='hour']" mode="datefield">
  <xsl:choose>
    <xsl:when test="../@readonly"><xsl:value-of select="format-number(@value,'00')" /> </xsl:when>
    <xsl:otherwise>

       <!-- Format value -->
       <xsl:variable name="value">
         <xsl:choose>
           <xsl:when test="@value &gt;= 0"><xsl:value-of select="format-number(@value,'00')"/></xsl:when>
           <xsl:otherwise></xsl:otherwise>
         </xsl:choose>
       </xsl:variable>

      <xsl:variable name="class">datefield<xsl:if test="string-length(../@style) &gt; 0">-<xsl:value-of select="../@style"/></xsl:if></xsl:variable>
      <INPUT TYPE="text" SIZE="2" MAXLENGTH="2" ID="{@id}" NAME="{@id}" VALUE="{$value}" CLASS="{$class}-h">
        <xsl:if test="../@disabled='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
        <xsl:if test="$dhtml and (../@immediate)">
            <xsl:attribute name="ONCHANGE">Millstone.submit();</xsl:attribute>
        </xsl:if>        
      </INPUT>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<!-- Minute template -->

<xsl:template match="integer[@name='min']" mode="datefield">
   <xsl:choose>
     <xsl:when test="../@readonly">:<xsl:value-of select="format-number(@value,'00')" /> </xsl:when>
     <xsl:otherwise>
     
       <!-- Format value -->
       <xsl:variable name="value">
         <xsl:choose>
           <xsl:when test="@value &gt;= 0"><xsl:value-of select="format-number(@value,'00')"/></xsl:when>
           <xsl:otherwise></xsl:otherwise>
         </xsl:choose>
       </xsl:variable>
       
       <xsl:variable name="class">datefield<xsl:if test="string-length(../@style) &gt; 0">-<xsl:value-of select="../@style"/></xsl:if></xsl:variable>
       : <INPUT TYPE="text" SIZE="2" MAXLENGTH="2" ID="{@id}" NAME="{@id}" VALUE="{$value}" CLASS="{$class}-min">
         <xsl:if test="../@disabled='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>
         <xsl:if test="$dhtml and (../@immediate)">
            <xsl:attribute name="ONCHANGE">Millstone.submit();</xsl:attribute>
         </xsl:if>
      </INPUT>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<!-- Seconds template -->

<xsl:template match="integer[@name='sec']" mode="datefield">
   <xsl:choose>
     <xsl:when test="../@readonly">:<xsl:value-of select="format-number(@value,'00')" /> </xsl:when>
     <xsl:otherwise>

       <!-- Format value -->
       <xsl:variable name="value">
         <xsl:choose>
           <xsl:when test="@value &gt;= 0"><xsl:value-of select="format-number(@value,'00')"/></xsl:when>
           <xsl:otherwise></xsl:otherwise>
         </xsl:choose>
       </xsl:variable>

       <xsl:variable name="class">datefield<xsl:if test="string-length(../@style) &gt; 0">-<xsl:value-of select="../@style"/></xsl:if></xsl:variable>
       : <INPUT TYPE="text" SIZE="2" MAXLENGTH="2" ID="{@id}" NAME="{@id}" VALUE="{$value}" CLASS="{$class}-s">
         <xsl:if test="../@disabled='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>

        <xsl:if test="$dhtml and (../@immediate)">
            <xsl:attribute name="ONCHANGE">Millstone.submit();</xsl:attribute>
        </xsl:if>
      </INPUT>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<!-- Milllisecond template -->

<xsl:template match="integer[@name='msec']" mode="datefield">
   <xsl:choose>
     <xsl:when test="../@readonly">:<xsl:value-of select="format-number(@value,'000')" /></xsl:when>
     <xsl:otherwise>

       <!-- Format value -->
       <xsl:variable name="value">
         <xsl:choose>
           <xsl:when test="@value &gt;= 0"><xsl:value-of select="format-number(@value,'000')"/></xsl:when>
           <xsl:otherwise></xsl:otherwise>
         </xsl:choose>
       </xsl:variable>

       <xsl:variable name="class">datefield<xsl:if test="string-length(../@style) &gt; 0">-<xsl:value-of select="../@style"/></xsl:if></xsl:variable>
       : <INPUT TYPE="text" SIZE="3" MAXLENGTH="3" ID="{@id}" NAME="{@id}" VALUE="{$value}" CLASS="{$class}-ms">
         <xsl:if test="../@disabled='true'"><xsl:attribute name="DISABLED">true</xsl:attribute></xsl:if>

        <xsl:if test="$dhtml and (../@immediate)">
            <xsl:attribute name="ONCHANGE">Millstone.submit();</xsl:attribute>
        </xsl:if>
      </INPUT>
    </xsl:otherwise>
  </xsl:choose>     
</xsl:template>







<!-- Generate day selections -->

<xsl:template name="calendar-day">
  <xsl:param name="day"/>
  <xsl:param name="selectedday"/>
  
    <OPTION VALUE="{$day}">
      <xsl:if test="$selectedday=$day">
        <xsl:attribute name="SELECTED">selected</xsl:attribute>
      </xsl:if>
      <xsl:value-of select="$day"/>
    </OPTION>
  	
  	<xsl:if test="$day &lt; 31">
      <xsl:call-template name="calendar-day">
        <xsl:with-param name="day"><xsl:value-of select="$day + 1"/></xsl:with-param>
        <xsl:with-param name="selectedday"><xsl:value-of select="$selectedday"/></xsl:with-param>
      </xsl:call-template>	
  	</xsl:if>
</xsl:template>


<!-- Generate month selections -->

<xsl:template name="calendar-month">
  <xsl:param name="month"/>
  <xsl:param name="selectedmonth"/>
  
    <OPTION VALUE="{$month}">
      <xsl:if test="$selectedmonth=$month">
        <xsl:attribute name="SELECTED">selected</xsl:attribute>
      </xsl:if>
      <xsl:value-of select="wa:getMonth(number($month - 1))"/>
    </OPTION>
  	
  	<xsl:if test="$month &lt; 12">
      <xsl:call-template name="calendar-month">
        <xsl:with-param name="month"><xsl:value-of select="$month + 1"/></xsl:with-param>
        <xsl:with-param name="selectedmonth"><xsl:value-of select="$selectedmonth"/></xsl:with-param>
      </xsl:call-template>	
  	</xsl:if>
</xsl:template>



<!-- Generate weeks for calendar -->

<xsl:template name="calendar-weeks">
  <xsl:param name="calendarid"/>
  <xsl:param name="dayid"/>
  <xsl:param name="week"/>
  <xsl:param name="immediate"/>
  
  <TR>
    <xsl:call-template name="calendar-weekdays">
      <xsl:with-param name="day">0</xsl:with-param>
      <xsl:with-param name="week"><xsl:value-of select="$week"/></xsl:with-param>
      <xsl:with-param name="calendarid"><xsl:value-of select="$calendarid"/></xsl:with-param>
      <xsl:with-param name="dayid"><xsl:value-of select="$dayid"/></xsl:with-param>
      <xsl:with-param name="immediate"><xsl:value-of select="$immediate"/></xsl:with-param>
    </xsl:call-template>
  </TR>

  <xsl:if test="$week &lt; 5">
    <xsl:call-template name="calendar-weeks">
      <xsl:with-param name="calendarid"><xsl:value-of select="$calendarid"/></xsl:with-param>
      <xsl:with-param name="week"><xsl:value-of select="$week + 1"/></xsl:with-param>
      <xsl:with-param name="dayid"><xsl:value-of select="$dayid"/></xsl:with-param>
      <xsl:with-param name="immediate"><xsl:value-of select="$immediate"/></xsl:with-param>
    </xsl:call-template>	
  </xsl:if>
  
</xsl:template>

<!-- Generate titles for weekdays -->

<xsl:template name="calendar-weekdaytitle">
  <xsl:param name="weekday"/>
  <xsl:param name="weekbegin"/>
  
  	<TD CLASS="cal-title-{($weekday + $weekbegin) mod 7}"><xsl:value-of select="wa:getShortWeekday(number(($weekday + $weekbegin) mod 7))"/></TD>
  	
  	<xsl:if test="$weekday &lt; 6">
      <xsl:call-template name="calendar-weekdaytitle">
        <xsl:with-param name="weekday"><xsl:value-of select="$weekday + 1"/></xsl:with-param>
        <xsl:with-param name="weekbegin"><xsl:value-of select="$weekbegin"/></xsl:with-param>
      </xsl:call-template>	
  	</xsl:if>
</xsl:template>


<!-- Generate days for a week -->

<xsl:template name="calendar-weekdays">
  <xsl:param name="day"/>
  <xsl:param name="week"/>
  <xsl:param name="calendarid"/>
  <xsl:param name="dayid"/>
  <xsl:param name="immediate"/>
  
  <TD>
    <xsl:if test="not(../@readonly) and not(../@disabled)">
      <xsl:attribute name="ONCLICK">Millstone.calSel('<xsl:value-of select="$calendarid"/>','<xsl:value-of select="$dayid"/>',this.id,'<xsl:value-of select="$immediate"/>')</xsl:attribute>
    </xsl:if>
    <xsl:attribute name="ID"><xsl:value-of select="$calendarid"/>_<xsl:value-of select="$week"/>_<xsl:value-of select="$day"/></xsl:attribute>
    <xsl:attribute name="CLASS">cal-day</xsl:attribute>
  </TD>

  <xsl:if test="$day &lt; 6">
    <xsl:call-template name="calendar-weekdays">
      <xsl:with-param name="day"><xsl:value-of select="$day + 1"/></xsl:with-param>
      <xsl:with-param name="week"><xsl:value-of select="$week"/></xsl:with-param>
      <xsl:with-param name="calendarid"><xsl:value-of select="$calendarid"/></xsl:with-param>
      <xsl:with-param name="dayid"><xsl:value-of select="$dayid"/></xsl:with-param>
      <xsl:with-param name="immediate"><xsl:value-of select="$immediate"/></xsl:with-param>
    </xsl:call-template>
  </xsl:if>
</xsl:template>
     
</xsl:stylesheet>