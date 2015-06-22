<#-- TomTom ASC or POI -->
<tmpl_par name="charset" value="ASCII">
<tmpl_par name="badchars" value=",">
<tmpl_par name="newline" value="CRLF">

<tmpl_loop cache_index>
<#-- LON,LAT,"SHORTTYPE-SHORTSIZE-DIFFICULTY-TERRAIN-NAME" -->
<tmpl_var name=LON>,<tmpl_var name=LAT>,"<tmpl_var name=SHORTTYPE>-<tmpl_var name=SHORTSIZE>-<tmpl_var name=DIFFICULTY>-<tmpl_var name=TERRAIN>-<tmpl_var name=NAME>"<br />
</tmpl_loop>
