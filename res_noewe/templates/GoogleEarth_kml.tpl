<tmpl_par name="newline" value="CRLF">
<tmpl_par name="Out" value="*.kml">
<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://earth.google.com/kml/2.1">

<Document>
  <name>Geocaching</name>
  <description><![CDATA[Geocaching mit CacheWolf]]></description>
<tmpl_loop cache_index>
 <Placemark><br/>
   <description><tmpl_var name=NAME></description><br/>
   <name><tmpl_var name=NAME></name><br/>
   <Point><br/>
     <coordinates><tmpl_var name=LON>,<tmpl_var name=LAT>,0</coordinates><br/>
   </Point><br/>
 </Placemark><br/>
</tmpl_loop>
</Document>
</kml>

