<tmpl_par name="charset" value="UTF8">
<tmpl_par name="badchars" value="&">
<tmpl_par name="Out" value="*.gpx">
<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" creator="CacheWolf" version="1.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensions/v3/GpxExtensionsv3.xsd http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
<tmpl_loop cache_index>
<wpt lat="<tmpl_var name = LAT>" lon="<tmpl_var name = LON>">
<name><tmpl_var name = NAME></name>
<desc><tmpl_var name = WAYPOINT> (<tmpl_var name = TYPE>) D=<tmpl_var name = DIFFICULTY>/T=<tmpl_var name = TERRAIN> - <tmpl_var name = SIZE></desc>
<sym>Geocache</sym>
<extensions>
<gpxx:WaypointExtension xmlns:gpxx="http://www.garmin.com/xmlschemas/GpxExtensions/v3"><gpxx:Categories><gpxx:Category>Geocaches</gpxx:Category></gpxx:Categories>
<gpxx:Address><gpxx:StreetAddress><tmpl_var name = WAYPOINT> - D=<tmpl_var name = DIFFICULTY>/T=<tmpl_var name = TERRAIN> - <tmpl_var name = SIZE></gpxx:StreetAddress><gpxx:State><tmpl_var name = TYPE></gpxx:State></gpxx:Address></gpxx:WaypointExtension>
</extensions>
</wpt>
</tmpl_loop>
</gpx>
