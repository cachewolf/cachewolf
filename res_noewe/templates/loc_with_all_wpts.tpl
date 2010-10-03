<tmpl_par name="charset" value="ASCII">
<tmpl_par name="newline" value="CRLF">
<tmpl_par name="decsep" value=".">
<tmpl_par name="Out" value="*.loc">
<tmpl_par name="ShortNameLength" value="25">
<tmpl_par name="formatModifier" value="1">
<?xml version="1.0"?><loc version="1.0" src=EasyGPS"><tmpl_loop cache_index>
<waypoint>
   <name id="<tmpl_var WAYPOINT> <tmpl_var SHORTSIZE><tmpl_var SHORTDIFFICULTY>/<tmpl_var SHORTTERRAIN>"><![CDATA[<tmpl_var SHORTNAME> TB:<tmpl_var TRAVELBUG>]]></name>
   <tmpl_if LAT><coord lat="<tmpl_var LAT>" lon="<tmpl_var LON>"/><tmpl_else><coord lat="70.00000" lon="000.00000"/></tmpl_if LAT>
   <type><tmpl_var GMTYPE></type>
</waypoint></tmpl_loop></loc>