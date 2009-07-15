<#-- OziExplorer Namesearch -->
<#-- Codecs: ASCII, UTF8 -->
<tmpl_par name="charset" value="ASCII">
<#-- somme chars should not appear in the cachename -->
<tmpl_par name="badchars" value=",">
<#-- newline: CR, LF, CRLF -->
<tmpl_par name="newline" value="CRLF">
;nst FORMAT :  FULL NAME,CODE,UTM ZONE,LAT,LONG,IGNORE ANYTHING ELSE<br />
#1,,<br />
#2,WGS 84<br />
<tmpl_loop cache_index>
"<tmpl_var name=SHORTTYPE>-<tmpl_var name=SHORTSIZE>-<tmpl_var name=DIFFICULTY>-<tmpl_var name=TERRAIN>-<tmpl_var name=NAME>",<tmpl_var name=TYPE>,,<tmpl_var name=LAT>,<tmpl_var name=LON>,<br />
</tmpl_loop>
