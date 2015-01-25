<#-- CSV -->
<tmpl_par name="charset" value="UTF8">
<tmpl_par name="Out" value="*.csv">
<tmpl_par name="decsep" value=",">
<tmpl_par name="badchars" value="";">
<tmpl_par name="newline" value="CRLF">
Name;Breitengrad;Längengrad;Typ1;Typ2;Waypoint;Datum;Hyperlink<br />
<tmpl_loop cache_index>
"<tmpl_var name=NAME>";"<tmpl_var name=LAT>";"<tmpl_var name=LON>";"<tmpl_var name=GSTYPE>";"<tmpl_var name=SIZE>";"<tmpl_var name=WAYPOINT>";"<tmpl_var name=DATE>";"<tmpl_var name=URL>";<br />
</tmpl_loop>
