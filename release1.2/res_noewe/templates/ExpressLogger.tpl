<#-- Express Logger GPX -->
<#-- Codecs: ASCII, UTF8 -->
<tmpl_par name="charset" value="UTF8">
<#-- newline: CR, LF, CRLF -->
<tmpl_par name="newline" value="CRLF">
<?xml version="1.0" encoding="utf-8"?><br/>
<gpx><br/>
<tmpl_loop cache_index>
   <wpt><br/>
       <name><tmpl_var name=WAYPOINT></name><br/>
       <cmlog:log version="1.0" found="true"><br/>
           <cmlog:notes><tmpl_var name=NOTES></cmlog:notes><br/>
       </cmlog:log><br/>
   </wpt><br/>
</tmpl_loop>
</gpx><br/>