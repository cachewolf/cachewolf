<#-- My Field Notes for uploading by http://www.geocaching.com/my/uploadfieldnotes.aspx -->
<tmpl_par name="Out" value="*.txt">
<tmpl_par name="charset" value="UTF8">
<tmpl_par name="newline" value="CRLF">
<tmpl_par name="badchars" value=",">
<tmpl_loop cache_index>
<tmpl_unless MAINWP>
<tmpl_if STATUS>
<tmpl_if STATUS_DATE>
<tmpl_var name=WAYPOINT>,<tmpl_var name=STATUS_DATE>T<tmpl_var name=STATUS_TIME>Z,<tmpl_var name=GC_LOGTYPE>,"#<tmpl_var __COUNTER__> <tmpl_var name=STATUS><br>
<tmpl_var name=NOTES>
DfdC sagt hierSetzeDeinenAlias
"<br>
</tmpl_else>
<tmpl_var name=WAYPOINT>,<tmpl_var name=NOW_DATE>T<tmpl_var name=NOW_TIME>Z,<tmpl_var name=GC_LOGTYPE>,"#<tmpl_var __COUNTER__> <tmpl_var name=NOW_DATE> <tmpl_var name=NOW_TIME> <tmpl_var name=STATUS><br>
<tmpl_var name=NOTES>
DfdC sagt hierSetzeDeinenAlias
"<br>
</tmpl_if>
</tmpl_if>
</tmpl_unless>
</tmpl_loop>
________________________________
Logged with Cachewolf