<#-- My Field Notes -->
<tmpl_par name="Out" value="*.txt">
<tmpl_par name="charset" value="UTF8">
<tmpl_par name="newline" value="CRLF">
<tmpl_loop cache_index>
<tmpl_var name=WAYPOINT>,<tmpl_var name=STATUS_DATE>T<tmpl_var name=STATUS_TIME>Z,Found it,"<tmpl_var name=NOTES>"<br>
</tmpl_loop>
________________________________
Logged with Cachewolf