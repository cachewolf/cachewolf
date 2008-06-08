<#-- AMAP Ascii Overlay -->
<tmpl_par name="newline" value="CRLF">
<tmpl_par name="decsep" value=".">
<tmpl_loop cache_index>
[Symbol <tmpl_var name=__COUNTER__>]<br />
Typ=6<br />
Group=1<br />
Width=40<br />
Height=40<br />
Dir=100<br />
Col=1<br />
Zoom=1<br />
Size=102<br />
Area=1<br />
XKoord=<tmpl_var name=LON><br />
YKoord=<tmpl_var name=LAT><br />
<tmpl_if __LAST__>
[Overlay]<br />
Symbols=<tmpl_var name=__COUNTER__><br />
</tmpl_if>
</tmpl_loop>


