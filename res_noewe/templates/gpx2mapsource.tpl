<#-- Export-Template V0.9 for Export via .gpx to 'MapSource'-->

<tmpl_par name="charset" value="UTF8">
<tmpl_par name="newline" value="CRLF">
<tmpl_par name="decsep" value=".">
<tmpl_par name="Out" value="*.gpx">
<tmpl_par name="takeOnlyWp" value="main">

<?xml version="1.0" encoding="utf-8"?><br>
<gpx xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" version="1.0" creator="Groundspeak Pocket Query" xsi:schemaLocation="http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd http://www.groundspeak.com/cache/1/0/1 http://www.groundspeak.com/cache/1/0/1/cache.xsd" xmlns="http://www.topografix.com/GPX/1/0"><br>

<tmpl_loop cache_index>
   <wpt lat="<tmpl_var LAT>" lon="<tmpl_var LON>"><br>
      <name><tmpl_var WAYPOINT></name><br>
      <desc><tmpl_var name="NAME" escape="html"> (D=<tmpl_var DIFFICULTY>/T=<tmpl_var TERRAIN>/<tmpl_var SIZE>
<tmpl_if ADDIS>/Multi)</desc><br>
<tmpl_else>/Single)</desc><br>
</tmpl_if>
      <url><tmpl_var URL></url><br>
      <urlname><tmpl_var name="NAME" escape="html"></urlname><br>
      <sym><tmpl_var SYM></sym><br>
      <type><tmpl_var TYPE></type><br>
   </wpt>

   <tmpl_if ADDIS>
      <tmpl_loop ADDIS>
         <tmpl_if LAT><br>
         <wpt lat="<tmpl_var LAT>" lon="<tmpl_var LON>">
         <tmpl_else><br>
         <wpt lat="00.000001" lon="00.000001">
         </tmpl_if><br>
            <name><tmpl_var WAYPOINT></name><br>
            <desc><tmpl_var name="NAME" escape="html"></desc><br>
            <sym><tmpl_var SYM></sym><br>
            <type><tmpl_var TYPE></type><br>
         </wpt>
      </tmpl_loop>
   </tmpl_if>
   <br>
   <br>
</tmpl_loop>

</gpx><br>

