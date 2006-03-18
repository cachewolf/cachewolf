<html>
<head>
<title></title>
<meta name="generator" content="CacheWolf">
<meta http-equiv="content-type" content="text/html; charset=utf-8">
</head>
<body>
<table border = "2px" align = "center">
 <tr style = "background-color:lightgrey;font-variant:small-caps; font-family:sans-serif;font-size:16;">
    <td>Art</td>
    <td>Size</td>
    <td>Wegpunkt</td>
    <td>Name</td>
    <td>Besitzer</td>
    <td>Schwierigkeit</td>
    <td>Gel&auml;nde</td>
    <td>Entfernung</td>
    <td>Richtung</td>
    <td>Koordinaten</td>
 </tr>
 <tmpl_loop cache_index>
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