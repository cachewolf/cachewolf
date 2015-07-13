<tmpl_par name="charset" value="ASCII">
<tmpl_par name="newline" value="CRLF">
<tmpl_par name="decsep" value=".">
<tmpl_par name="Out" value="*.gpx">
<tmpl_par name="ShortNameLength" value="15">
<#-- <tmpl_par name="inputSPEED" value="50"> -->
<#-- <tmpl_par name="inputDISTANCE" value="100"> -->
<tmpl_par name="inputShowSpoiler" value="no">
<?xml version="1.0" encoding="Windows-1252"?><br>
<gpx xmlns="http://www.topografix.com/GPX/1/1" creator="CacheWolf" version="1.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd"><br>
<metadata><br>
<link href="http://www.cachewolf.de"><text>CacheWolf</text></link><br>
<tmpl_loop cache_index>
<tmpl_if __FIRST__><time><tmpl_var NOW_DATE>T<tmpl_var NOW_TIME>:00Z</time><br>
</metadata><br>
</tmpl_if>
<wpt lat="<tmpl_var LAT>" lon="<tmpl_var LON>"><br>
<#-- <name><tmpl_var SHORTTYPE><tmpl_var SSHORTWAYPOINT> <tmpl_var SHORTNAME>(<tmpl_var DIFFICULTY>/<tmpl_var TERRAIN>/<tmpl_var SHORTSIZE>)@<tmpl_var SPEED></name><br> -->
<name><tmpl_var SHORTTYPE> <tmpl_var SSHORTWAYPOINT> <tmpl_var SHORTNAME>(<tmpl_var DIFFICULTY>/<tmpl_var TERRAIN>/<tmpl_var SHORTSIZE>)</name><br>
<cmt><tmpl_var DECRYPTEDHINTS> <tmpl_var NOTES> <tmpl_loop ATTRIBUTES>,<tmpl_var INFO></tmpl_loop></cmt><br>
<#-- <desc><tmpl_loop ATTRIBUTES>,<tmpl_var INFO> </tmpl_loop></desc><br> -->
<tmpl_loop cacheImg><link href="<tmpl_var PROFILDIR><tmpl_var FILENAME></link><br>
</tmpl_loop>
<#-- <sym>Scenic Area</sym> -->
<#-- <sym><tmpl_var DECRYPTEDHINTS></sym> -->
<extensions><br>
   <gpxx:WaypointExtension xmlns:gpxx="http://www.garmin.com/xmlschemas/GpxExtensions/v3"><br>
<#-- <gpxx:Proximity><tmpl_var SPEED></gpxx:Proximity><br> -->
      <gpxx:DisplayMode>SymbolAndName</gpxx:DisplayMode><br>
   </gpxx:WaypointExtension><br>
</extensions><br>
</wpt><br>
</tmpl_loop>
</gpx><br>
