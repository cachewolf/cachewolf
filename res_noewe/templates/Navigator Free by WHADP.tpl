<#-- Export-Template v1.0a for Cache-Export via .gpx to 'Navigator Free' -->
<#-- -->
<#-- Mit Hilfe dieses Templates können Cache Daten für die Darstellung -->
<#-- in Navigator11Free von MapFactor exportiert werden. Das .gpx file -->
<#-- wird dann mit diggerQT.exe eingelesen und ein .mca file erzeugt. -->
<#-- -->
<#-- Mit dem Feld symbol_name lassen sich die Icons für die verschie- -->
<#-- denen Caches leicht zuordnen. -->
<#-- -->
<#-- Die Caches werden nun als POIs in Navigator11Free dargestellt. -->

<tmpl_par name="charset" value="UTF8">
<tmpl_par name="newline" value="CRLF">
<tmpl_par name="decsep" value=".">
<tmpl_par name="Out" value="*.gpx">
<tmpl_par name="takeOnlyWp" value="main">
<tmpl_par name="badchars" value="&<>">

<?xml version="1.0" encoding="utf-8"?><br>
<gpx xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" version="1.0" creator="Groundspeak Pocket Query" xsi:schemaLocation="http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd http://www.groundspeak.com/cache/1/0/1 http://www.groundspeak.com/cache/1/0/1/cache.xsd" xmlns="http://www.topografix.com/GPX/1/0"><br>

<tmpl_loop cache_index>
<wpt lat="<tmpl_var LAT>" lon="<tmpl_var LON>"><br>
<name><tmpl_var WAYPOINT></name><br><#-- Cachenummer GCxxxx -->
<cmt><tmpl_var name="NAME"></cmt><br><#-- Name des Caches -->
<time><tmpl_var STATUS_DATE></time><br><#-- Cache gefunden am Datum -->
<ele><tmpl_var DIFFICULTY></ele><br><#-- Schwierigkeitswertung des Caches -->
<desc>(D=<tmpl_var DIFFICULTY>/T=<tmpl_var TERRAIN>/<tmpl_var SIZE>
<tmpl_if ADDIS>/Multi)</desc><br>
<tmpl_else>/Single)</desc><br>
</tmpl_if>

<tmpl_if STATUS><#-- gefundene Caches kennzeichnen -->
<sym>99 Found it</sym><br><#-- 99 = gefunden -->
<tmpl_else>
<sym><tmpl_var TYPENO> <tmpl_var GSTYPE></sym><br><#-- Art des Caches 02=tradi, ... und als Text --> 
</tmpl_if>
<type>Hint: <tmpl_var DECRYPTEDHINTS></type><br><#-- Hint -->
</wpt>

<tmpl_if ADDIS>
<tmpl_loop ADDIS>
<tmpl_if LAT><br>
<wpt lat="<tmpl_var LAT>" lon="<tmpl_var LON>">
<tmpl_else><br>
<wpt lat="00.000001" lon="00.000001">
</tmpl_if><br>
<name><tmpl_var WAYPOINT></name><br><#-- Cachenummer GCxxxx -->
<desc><tmpl_var name="NAME" escape="html"></desc><br>
<sym><tmpl_var ICON> <tmpl_var GSTYPE></sym><br><#-- Art des Caches 02=tradi, ... und als Text -->
<type><tmpl_var DECRYPTEDHINTS></type><br><#-- Hint -->
</wpt>
</tmpl_loop>
</tmpl_if>
<br>
<br>
</tmpl_loop>

</gpx><br>
