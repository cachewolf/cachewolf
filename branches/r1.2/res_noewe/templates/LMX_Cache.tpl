<#-- Template für Nokia .LMX Format v1.0  -->
<#-- Dieses Template exportiert           -->
<#-- AUSSCHLIEßLICH Caches (keine extra   -->
<#-- Wegpunkte) von Cachewolf nach LMX.   -->
<#-- Zusätzliche Wegpunkte können mit dem -->
<#-- Template für die Waypoints separat   -->
<#-- exportiert werden. (LMX_WP.tpl)      -->
<#-- Durch die Integration der Webseite   -->
<#-- stehen alle Informationen zum Cache  -->
<#-- zur Verfügung--->



<#-- WEGPUNKTE                            -->
<#-- Zur Verwendung mit Nokia-Programmen  -->
<#--   - Orientierungspunkte              -->
<#--   - Karten                           -->
<#-- Import in die Handy-Datenbank:       -->
<#--   - .LMX Datei auf Handy kopieren    -->
<#--   - Mit Dateimanager öffnen          -->
<#--   - Optionen / Alle speichern        -->

<#-- INTERNETSEITE                        -->
<#-- Caches mit HTML-Exporter exportieren -->
<#-- Exportierte Dateien in den Ordner    -->
<#-- e:/cache/CACHEORT/                   -->
<#-- kopieren.                            -->
<#-- CACHEORT entspricht hierbei dem beim -->
<#-- Export abgefragten Ort.              -->
<#-- Dir URL der Orientierungspunkte      -->
<#-- verweist dann auf die passende Datei -->
<#-- im Handy und ruft diese mit dem      -->
<#-- internen Browser auf. Eine Online    -->
<#-- Verbindung ist nicht erforderlich.   -->



<#-- CONFIG-KRAM-->
<tmpl_par name="charset" value="UTF8">
<tmpl_par name="badchars" value=";">
<tmpl_par name="Out" value="000_ORT_Cache.lmx">
<tmpl_par name="newline" value="CRLF">
<tmpl_par name="decsep" value=".">
<#-- CONFIG-KRAM Ende-->

<#-- VARIABLE FUER ORT DEFINIEREN UND ABFRAGEN-->
<tmpl_par name="inputCACHEORT" value="CACHEORT">



<#-- Nokia Header-->
<?xml version="1.0" encoding="UTF-8"?><br />
<lm:lmx xmlns:lm="http://www.nokia.com/schemas/location/landmarks/1/0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.nokia.com/schemas/location/landmarks/1/0/ lmx.xsd"><br />
<#-- Nokia Header Ende-->

	<lm:landmarkCollection><br />

<#-- Loop-Anfang -->
<tmpl_loop cache_index>
<tmpl_unless MAINWPNAME>
		<lm:landmark><br />

<#-- Waypoints-->
<tmpl_if MAINWPNAME>
			<lm:name>C WP <tmpl_var name=MAINWPNAME escape=html><tmpl_var name=__COUNTER__ escape=html>/<tmpl_var name=NAME escape=html>_<tmpl_var name=MAINWP escape=html>/<tmpl_var name=WAYPOINT escape=html> </lm:name><br />
<#-- Waypoints Ende-->


<#-- Caches-->
<tmpl_else>
			<lm:name>C <tmpl_var name=SHORTTYPE escape=html><tmpl_var name=SHORTSIZE escape=html> <tmpl_var name=NAME escape=html><tmpl_if SHORTTYPE></tmpl_if>:<tmpl_var name=DIFFICULTY escape=html>/<tmpl_var name=TERRAIN escape=html><tmpl_var name=TRAVELBUG>_<tmpl_var name=WAYPOINT escape=html> </lm:name><br />
</tmpl_if>
<#-- Caches Ende-->

<#-- Beschreibung, Koordinaten, URL, Kategorie-->
			<lm:description><tmpl_var name=DECRYPTEDHINTS escape=html></lm:description><br />
			<lm:coordinates><br />
				<lm:latitude><tmpl_var name=LAT></lm:latitude><br />
				<lm:longitude><tmpl_var name=LON></lm:longitude><br />
			</lm:coordinates><br />
			<lm:mediaLink><br />
				<lm:url>file:///e:/cache/<tmpl_var name=CACHEORT>/<tmpl_if MAINWP><tmpl_var name=MAINWP escape=html>.html</lm:url><br />
				<tmpl_else><tmpl_var name=WAYPOINT escape=html>.html</lm:url><br />
				</tmpl_if></lm:mediaLink><br />
			<lm:category><br />
				<lm:name>C <tmpl_var name=CACHEORT></lm:name><br />
			</lm:category><br />
		</lm:landmark><br />
</tmpl_unless>
</tmpl_loop>
<#-- Loop-Ende -->


	</lm:landmarkCollection><br />
</lm:lmx><br />