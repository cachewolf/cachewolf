<tmpl_par name="charset" value="ASCII">
<tmpl_par name="newline" value="CRLF">
<tmpl_par name="decsep" value=".">
<tmpl_par name="Out" value="*.loc">
<tmpl_par name="ShortNameLength" value="25">
<?xml version="1.0"?><loc version="1.0" src=EasyGPS"><br/>
<tmpl_loop cache_index>
<waypoint><br/>
   <name id="<tmpl_var WAYPOINT> <tmpl_var SHORTSIZE><tmpl_var SHORTDIFFICULTY>/<tmpl_var SHORTTERAIN>"><![CDATA[<tmpl_var SHORTNAME> TB:<tmpl_var TRAVELBUG>]]></name><br/>
   <coord lat="<tmpl_var LAT>" lon="<tmpl_var LON>"/><br/>
   <type><tmpl_var GMTYPE></type><br/>
</waypoint><br/>
</tmpl_loop>
</loc><br/>

