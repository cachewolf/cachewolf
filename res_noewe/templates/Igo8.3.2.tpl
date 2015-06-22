<#------------------------------------------->
<#-- *** GPX -> IGO8 @ MtBo 16.03.2009 *** -->
<#--    Einfach gleich speichern unter     -->
<#--    IGO8/save/user.upoi                -->
<#------------------------------------------->

<#-- Platzhalter -->
<#-- TYPE: Typ des Caches, z.B. Regular, Multi  -->
<#-- SHORTTYPE: Erster Buchstabe von Typ  -->
<#-- SIZE: Größe des Caches, z.B. Regular, Micro  -->
<#-- SHORTSIZE: Erster Buchstabe von Größe  -->
<#-- WAYPOINT: GCXXXX, OCXXXX  -->
<#-- NAME: Name des Caches  -->
<#-- OWNER  -->
<#-- DIFFICULTY  -->
<#-- TERRAIN  -->
<#-- DISTANCE: Die Entfernung aus der Cacheliste  -->
<#-- BEARING: Die Richtung aus der Cacheliste  -->
<#-- LATLON: Koordinaten in langem Format: N 50° 31.234 E 008° 45.267  -->
<#-- LAT: Breite in Dezimalformat, z.B. 50.20147. Trenner kann konfiguriert werden.  -->
<#-- LON: Länge in Dezimalformat, z.B. 008.58132. Trenner kann konfiguriert werden.  -->
<#-- STATUS  -->
<#-- DATE: Datum, wann versteckt  -->
<#-- NOTES: Notizen  -->
<#-- DESCRIPTION: Beschreibung  -->
<#-- DECRYPTEDHINTS  -->
<#-- URL  -->

<#-- -----------------------Template-------------------------------------- -->
<tmpl_par name="charset" value="UTF-16 LE">
<tmpl_par name="badchars" value="|">
<tmpl_par name="newline" value="CRLF">
<tmpl_par name="Out" value="user.upoi">

@My POI<br />

<tmpl_loop cache_index>

<#-- Feld 1 : Zähler -->
<tmpl_var name=__COUNTER__>|

<#-- Feld 2 : Ordner -->
<#-- Dieser eintrag erzeugt einen Ordner in der Poi-Verwaltung -->
<#-- Dabei ergibt sich folgende Struktur: Ordner.Unterordner -->
Geocaching.<tmpl_var name=GSTYPE>|

<#-- Feld 3 : Poiname -->
<tmpl_var name=NAME>
|

<#-- Feld 4 : kein Textfeld -->
|

<#-- Feld 5 : Nordkoordinate -->
<tmpl_var name=LAT>
|

<#-- Feld 6 : Ostkoordinate -->
<tmpl_var name=LON>
|

<#-- Feld 7 : Landkarten-Name -->
|

<#-- Feld 8 : kein Textfeld -->
|
<#-- Feld 9 : kein Textfeld -->
|
<#-- Feld 10 : kein Textfeld -->
|
<#-- Feld 11 : Postleitzahl -->
|

<#-- Feld 12 : Ort -->
Diff:<tmpl_var name=DIFFICULTY>
 /
 Terr:<tmpl_var name=TERRAIN>
|

<#-- Feld 13 : Straße -->
<tmpl_var name=SIZE>
|

<#-- Feld 14 : Hausnummer -->
|

<#-- Feld 15 : Infofeld -->
<tmpl_var name=DECRYPTEDHINTS>
|

<#-- Feld 16 : Telefonnummer -->
<tmpl_var name=WAYPOINT>
 /
 <tmpl_var name=OWNER>
|
<br />
</tmpl_loop>