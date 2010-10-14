<tmpl_par name="charset" value="UTF8">
<tmpl_par name="decsep" value=".">
<tmpl_par name="newline" value="LF">
<tmpl_par name="Out" value="index_alpha.html">
<tmpl_par name="singleFile" value="yes">
<tmpl_par name="formatModifier" value="2">
<tmpl_par name="takeOnlyWp" value="main">
<tmpl_par name="sortedBy" value="5">
<html>
<head>
<title></title>
<meta name="generator" content="CacheWolf">
<meta http-equiv="content-type" content="text/html; charset=utf-8">
</head>
<body>
<table border = "2px" align = "center">
 <tr style = "background-color:lightgrey;font-variant:small-caps; font-family:sans-serif;font-size:16;">
    <td><a href = "index_type.html">Art</a></td>
    <td><a href = "index_size.html">Size</a></td>
    <td><a href = "index.html">Wegpunkt</td>
    <td><a href = "index_alpha.html">Name</a></td>
    <td>Besitzer</td>
    <td>Schwierigkeit</td>
    <td>Gel&auml;nde</td>
    <td><a href = "index_dist.html">Entfernung</a></td>
    <td>Richtung</td>
    <td>Koordinaten</td>
 </tr>
 <tmpl_loop cache_index>
 <tmpl_if ANCHORNAME>
  <tr style = "font-family:sans-serif;font-size:12;">
 		<td colspan="2" style = "background-color:lightgrey;font-variant:small-caps; font-family:sans-serif;font-size:16;"><a name = "<tmpl_var ANCHORNAME>"><tmpl_var ANCHORTEXT></a></td>
 		<td><a href="#top">Back</a></td>
  </tr>
 </tmpl_if>
 <tr style = "font-family:sans-serif;font-size:12;">
    <td><tmpl_var TYPE></td>
    <td><tmpl_var SIZE></td>
    <td><a href = "<tmpl_var WAYPOINT>.html"><tmpl_var WAYPOINT></a></td>
    <td><tmpl_var NAME></td>
    <td><tmpl_var OWNER></td>
    <td><tmpl_var DIFFICULTY></td>
    <td><tmpl_var TERRAIN></td>
    <td><tmpl_var DISTANCE></td>
    <td><tmpl_var BEARING></td>
    <td><tmpl_var LATLON></td>
 </tr>
 </tmpl_loop>
</table>
</body>
</html>