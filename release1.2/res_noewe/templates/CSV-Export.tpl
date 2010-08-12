<#-- CSV -->
<tmpl_par name="charset" value="ASCII">
<tmpl_par name="Out" value="*.csv">
<tmpl_par name="badchars" value=",">
<tmpl_par name="newline" value="CRLF">
<tmpl_loop cache_index>
<tmpl_var name=LON>,<tmpl_var name=LAT>,"<tmpl_var name=WAYPOINT> <tmpl_var name=SHORTTYPE>/<tmpl_var name=SHORTSIZE> <tmpl_var name=NAME> <tmpl_var name=DIFFICULTY>/<tmpl_var name=TERRAIN>",,<tmpl_var name=TYPE><br />
<tmpl_if DECRYPTEDHINTS>
<tmpl_var name=LON>,<tmpl_var name=LAT>,"<tmpl_var name=WAYPOINT> HINT","<tmpl_var name=DECRYPTEDHINTS>",HINT<br />
</tmpl_if>
</tmpl_loop>
