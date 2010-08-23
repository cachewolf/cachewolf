<html>
<head>
<title></title>
<meta name="generator" content="CacheWolf">
<meta http-equiv="content-type" content="text/html; charset=utf-8">
</head>
<body>
<table border = "2px" align = "center">
<tr style = "background-color:lightgrey;font-family:sans-serif;font-size:14;"><td>
<tmpl_var TYPE> <tmpl_var WAYPOINT> <tmpl_var NAME> by <tmpl_var OWNER><br>
Difficulty: <tmpl_var DIFFICULTY>&nbsp;&nbsp;Terrain: <tmpl_var TERRAIN>&nbsp;&nbsp;Size: <tmpl_var SIZE><br>
Coordinates: <tmpl_var LATLON>
</td></tr>
<tr style = "font-family:sans-serif;font-size:12;"><td>
Description:<br>
<tmpl_var DESCRIPTION>
</td></tr>
<tr style = "font-family:sans-serif;font-size:12;"><td>
Hints:<br>
<tmpl_var HINTS>
</td></tr>
<tr style = "font-family:sans-serif;font-size:12;"><td>
Decrypted Hints:<br>
<tmpl_var DECRYPTEDHINTS>
</td></tr>
<tr style = "font-family:sans-serif;font-size:12;"><td>
Logs:<br>
<tmpl_loop LOGS>
#logNr=<tmpl_var __COUNTER__><br>
<img src="<tmpl_var ICON>" border="0">&nbsp;<tmpl_var DATE>&nbsp;by&nbsp;<tmpl_var LOGGER><br><tmpl_var MESSAGE>
</tmpl_loop>
</td></tr>
<tr style = "font-family:sans-serif;font-size:12;"><td>
<br>
Notes:<br>
<tmpl_var NOTES>
</td></tr>
<tr style = "font-family:sans-serif;font-size:12;"><td>
Pictures:<br>
Cache:<br>
<tmpl_loop cacheImg>
<a href = "<tmpl_var FILENAME>"><tmpl_var TEXT></a><br>
</tmpl_loop>
</td></tr>
<tr style = "font-family:sans-serif;font-size:12;"><td>
</table>
</body>
</html>
