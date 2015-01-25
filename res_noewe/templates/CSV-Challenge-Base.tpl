<#-- CSV -->
<tmpl_par name="charset" value="UTF8">
<tmpl_par name="Out" value="*.csv">
<tmpl_par name="decsep" value=",">
<tmpl_par name="badchars" value="\";">
<tmpl_par name="newline" value="CRLF">
"Name";"GC Code";"Type";"Difficulty";"Terrain";"DISTANCE";"DATE FOUND";"OWNER"<br />
<tmpl_loop cache_index>
"<tmpl_var name=NAME>";"<tmpl_var name=WAYPOINT>";"<tmpl_var name=GSTYPE>";<tmpl_var name=DIFFICULTY>;<tmpl_var name=TERRAIN>;<tmpl_var name=DISTANCE>;<tmpl_var name=STATUS_DATE>;"<tmpl_var name=OWNER>"<br />
</tmpl_loop>
