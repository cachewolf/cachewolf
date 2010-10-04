<tmpl_par name="charset" value="UTF8">
<tmpl_par name="newline" value="CRLF">
<tmpl_par name="decsep" value=".">
<tmpl_par name="Out" value=".html">
<tmpl_par name="NrLogs " value="-1">
<tmpl_par name="singleFile" value="no">
<tmpl_par name="formatModifier" value="2">
<tmpl_par name="takeOnlyWp" value="all">
<tmpl_par name="CopyCacheImages" value="yes">
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<!-- von ~Idefix~ modifiziert v1.0-->
<!-- saved from url=(0014)http://localhost/ -->
<!-- Note: The line above is needed to avoid the warning due to the javascript contained in this page.
   Using javascript, various sections of the Cache listing can be hidden/unhidden.
   Try clicking on anything that is underlined.
-->
<html>
<head>
<title></title>
<meta name="generator" content="CacheWolf">
<meta http-equiv="content-type" content="text/html; charset=utf-8">
<script type="text/javascript">
function toggleDivOL( elemID ) { // Hide or show a DIV
	var elem = document.getElementById( elemID );
	if (!elem.style.display || elem.style.display=='block') {
		elem.style.display='none';
	} else {
		elem.style.display='block';
	}
}
</script>
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


<tr style = "font-family:sans-serif;font-size:0;">
<tmpl_loop ADDIS>
<tr style = "font-family:sans-serif;font-size:12;">
</tmpl_loop><td>
<a href="javascript:toggleDivOL('STAGES');">STAGES:</a><BR/>
<div style ="display:none;" id="STAGES">
<tmpl_loop ADDIS>
<tmpl_var NAME> / <tmpl_var WAYPOINT><br>
<tmpl_var LONGDESC><br>
<tmpl_var LATLON><br>
-------------------------------------<br>
</tmpl_loop>
</div>
</td></tr>


<tmpl_if NOTES>
<tr style = "font-family:sans-serif;font-size:12;"><td>
<a href="javascript:toggleDivOL('NOTES');">Notes:</a><BR/>
<div style ="display:none;" id="NOTES">
<tmpl_var NOTES>
</div>
</td></tr>
</tmpl_if>




<tr style = "font-family:sans-serif;font-size:0;">
<tmpl_loop cacheImg>
<tr style = "font-family:sans-serif;font-size:12;">
</tmpl_loop><td>
<a href="javascript:toggleDivOL('BILDER');">Bilder (kann Spoiler enthalten):</a><BR/>
<div style ="display:none;" id="BILDER">
	<tmpl_loop cacheImg>
		<tmpl_var TEXT><br>
		<img src = "<tmpl_var FILENAME>" border="0"><br>
	</tmpl_loop>
</div>
</td></tr>



<tr style = "font-family:sans-serif;font-size:12;"><td>
<tmpl_if DECRYPTEDHINTS>
<a href="javascript:toggleDivOL('Decrypted_Hints');">Decrypted Hints:</a><BR/>
<div style ="display:none;" id="Decrypted_Hints">
<tmpl_var DECRYPTEDHINTS>
</div>
<tmpl_else>
No Hints
</tmpl_if>
</td></tr>


<tr style = "font-family:sans-serif;font-size:12;"><td>
<a href="javascript:toggleDivOL('LOGS_');">Logs:</a><BR/>
<div style ="display:none;" id="LOGS_">
<tmpl_loop LOGS>
#logNr=<tmpl_var __COUNTER__><br>
<img src="<tmpl_var ICON>" border="0">&nbsp;<tmpl_var DATE>&nbsp;by&nbsp;<tmpl_var LOGGER><br><tmpl_var MESSAGE>
</tmpl_loop>
</div>
</td></tr>


</table>
</body>
</html>


