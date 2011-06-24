<tmpl_par name="decsep" value=".">
<tmpl_par name="Out" value="*.loc">
<tmpl_par name="charset" value="UTF8">
<tmpl_par name="newline" value="CRLF">
<tmpl_par name="takeOnlyWp" value="parking">
<?xml version="1.0"?><loc version="1.0" src="EasyGPS"><br/>
<tmpl_loop cache_index>
<waypoint><br/>
   <name id="<tmpl_var WAYPOINT>"><![CDATA[<tmpl_var NAME>]]></name><br/>
   <coord lat="<tmpl_var LAT>" lon="<tmpl_var LON>"/><br/>
   <type><tmpl_var GMTYPE></type><br/>
</waypoint><br/>
</tmpl_loop>
</loc><br/>
