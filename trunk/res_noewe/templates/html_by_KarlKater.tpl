<tmpl_par name="charset" value="UTF8">
<tmpl_par name="decsep" value=".">
<tmpl_par name="newline" value="LF">
<tmpl_par name="Out" value=".html">
<tmpl_par name="NrLogs " value="-1">
<tmpl_par name="singleFile" value="no">
<tmpl_par name="formatModifier" value="1">
<tmpl_loop cache_index>
<html>

<head>
<title></title>
<meta name="generator" content="CacheWolf HTML-Export edit by KarlKater">
<meta http-equiv="content-type" content="text/html; charset=utf-8">
</head>

<body>
<table border = "2px" align = "center">

<tr style = "background-color:lightgrey;font-family:sans-serif;font-size:14;">
<td>
<tmpl_var TYPE> <tmpl_var WAYPOINT> <tmpl_var NAME> by <tmpl_var OWNER><br>
Difficulty: <tmpl_var DIFFICULTY>&nbsp;&nbsp;Terrain: <tmpl_var TERRAIN>&nbsp;&nbsp;Size: <tmpl_var SIZE><br>
Coordinates: <tmpl_var LATLON>
</td>
</tr>

<tr style = "font-family:sans-serif;font-size:12;">
<td>
<tmpl_loop ATTRIBUTES>
<img src="../attributes/<tmpl_var IMAGE>" border=0 alt="<tmpl_var INFO>">
<tmpl_var BR>
<!--<tmpl_var IMAGE>-->
<!--<tmpl_var INFO>-->
</tmpl_loop>
<br>
<br>
<tmpl_var DESCRIPTION>
</td>
</tr>

<tmpl_if ADDIS>
<TABLE cellSpacing=1 cellPadding=1 bgColor=#448e35 border=0>
<TBODY>
<TR bgColor=#c6e3c0>
<TH align=left>Waypoint</TH>
<TH align=left>Name</TH>
<TH align=left>Coordinate</TH>
<tmpl_loop ADDIS>
<TR bgColor=#ffffff>
<TD vAlign=top align=left><tmpl_var WAYPOINT></TD>
<TD vAlign=top align=left><tmpl_var NAME></TD>
<TD vAlign=top align=left><tmpl_var LATLON></TD></TR>
<TR bgColor=#ffffff>
<TD vAlign=top align=left ColSpan=3><tmpl_var LONGDESC></TD>
</tmpl_loop>
</TBODY>
</TABLE><BR/></div>
</tmpl_if>

<table cellSpacing=1 cellPadding=1 bgColor=#448e35 border=0>
<tr bgColor=#c6e3c0>
       <td align=right>Hints:&nbsp;&nbsp;</td>
       <td><tmpl_var HINTS></td>
</tr>
<tr bgColor=#ffffff>
       <td>Decrypted Hints:&nbsp;&nbsp;</td>
       <td><tmpl_var DECRYPTEDHINTS></td>
</tr>
</table>

<br>

<tr style = "font-family:sans-serif;font-size:12;"><td>
Logs:<br>
<tmpl_loop LOGS>
<img src="<tmpl_var ICON>" border="0">&nbsp;<tmpl_var DATE>&nbsp;by&nbsp;<tmpl_var LOGGER><br><tmpl_var MESSAGE>
<br>
</tmpl_loop>
</td>
</tr>

<tr style="font-family:sans-serif;font-size:12;">
<td>
Notes:<br>
<tmpl_var NOTES>
</td>
</tr>

<tr style = "font-family:sans-serif;font-size:12;"><td>
Pictures:<br>
Cache:<br>
<tmpl_loop cacheImg>
<a href = "<tmpl_var FILE>"><tmpl_var TEXT></a><br>
</tmpl_loop>
User:<br>
<tmpl_loop userImg>
<a href = "<tmpl_var FILE>"><tmpl_var TEXT></a><br>
</tmpl_loop>
Logs:<br>
<tmpl_loop logImg>
<a href = "<tmpl_var FILE>"><tmpl_var TEXT></a><br>
</tmpl_loop>
Maps:<br>
<tmpl_loop mapImg>
<a href = "<tmpl_var FILE>"><tmpl_var TEXT></a><br>
</tmpl_loop>
</td>
</tr>

<tr style = "font-family:sans-serif;font-size:12;"><td>
</table>
</body>
</html>
</tmpl_loop>