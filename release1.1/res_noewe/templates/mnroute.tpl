<#-- Mobile Navigator Route -->
<#-- newline: CR, LF, CRLF -->
<tmpl_par name="newline" value="CRLF">
<#-- somme chars should not appear in the cachename -->
<tmpl_par name="badchars" value="|">
<tmpl_loop cache_index>
<tmpl_var name=NAME>|-|-|-|-|-|-|-|-|-|<tmpl_var name=LON>|<tmpl_var name=LAT>|-|<br/>
</tmpl_loop>