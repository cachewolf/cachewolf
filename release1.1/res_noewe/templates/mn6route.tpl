<#-- Mobile Navigator 6 Route -->
<tmpl_par name="charset" value="ASCII">
<#-- somme chars should not appear in the cachename -->
<tmpl_par name="badchars" value="|">
<#-- newline: CR, LF, CRLF -->
<tmpl_par name="newline" value="CRLF">
<tmpl_par name="Out" value="*.rte">
<tmpl_loop cache_index>
[<tmpl_var name=NAME>|][0][10]|<tmpl_var name=LON>,<tmpl_var name=LAT>||<tmpl_var name=LON>|<tmpl_var name=LAT>[7]||[6]|||<tmpl_var name=LON>|<tmpl_var name=LAT>[3]||[2]|||[0]|||<br/>
</tmpl_loop>
