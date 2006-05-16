<#-- TomTom ASC or POI -->
<#-- Codecs: ASCII, UTF8 -->
<tmpl_par name="charset" value="ASCII">
<#-- somme chars should not appear in the cachename -->
<tmpl_par name="badchars" value=",">
<#-- newline: CR, LF, CRLF -->
<tmpl_par name="newline" value="CRLF">
<tmpl_loop cache_index>
<tmpl_var name=LON>,<tmpl_var name=LAT>,"<tmpl_var name=SHORTTYPE>-<tmpl_var name=SHORTSIZE>-<tmpl_var name=DIFFICULTY>-<tmpl_var name=TERRAIN>-<tmpl_var name=NAME>"<br />
</tmpl_loop>
