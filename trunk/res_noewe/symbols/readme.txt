In diesem Verzeichnis vorhandene .png-Dateien werden als alternative Symbole in den Cachewolf geladen.
 
Dabei sollte es sich um PNGs der Größe 16x16 handeln oder kleiner.
Auf der Karte werden auch grössere Symbole angezeigt, falls sie existieren und der Dateiname auf _vga endet.
Für folgende Stati kann ein extra Symbol auf der Kartenansicht überlagert werden: 
found.png : habe den Cache schon gefunden.
dnf.png: habe den Cache nicht gefunden:
archived.png : der Cache ist archiviert.
disabled.png: Der Cache ist vorübergehend nicht suchbar.
owned.png: Den Cache habe ich selber platziert.
solved.png: Du hast dem Cache den Status „gelöst“ gegeben, d.h. an den Koordinaten ist der Cache zu finden.
bonus.png: Das ist ein Bonuscache. Die Koordinaten ergeben sich durch Suchen eines anderen Cache.
Das überlagernde Symbol muß kleiner oder gleich groß sein wie das Cachesymbol.
 
Es müssen nur die Dateien erstellt werden, die ersetzt werden sollen.
Ist keine entsprechende Datei vorhanden, wird das Cachewolf-interne Symbol beibehalten.
Für Transparenz (in Win32) muss die png-Datei mit 32 Bit pro Pixel gespeichert werden.
 
.png-files in this directory will be loaded as customized symbols into Cachewolf if checked in preferences tab more.
They're supposed to be 16x16 PNGs or less.
On the map larger symbols are shown, if they exist and the filename ends on _vga.
For the following states, an extra symbol is superimposed on the map:
found.png: the cache had been found.
dnf.png: the cache was not found:
archived.png: the cache is archived.
disabled.png: The cache is temporarily not searchable.
owned.png: the cache I have myself placed.
solved.png: You have the cache status "solved" if i.e. at coordinates cache can be found.
bonus.png: This is a bonus. The Coordinates result from searching another cache.
The superimposed image must be smaller or be the same size as the icon cache.
 
You only have to create images for those symbols you want customized.
For symbols without corresponding file the buildt-in symbol will be maintained.
For transparency (on win32) the png-file must be saved with 32 Bit per pixel.
 
 
Zuordnung der Dateinamen/filename assignment:
---------------------------------------------
Name                       meaning
typeCustom                 Custom
typeApe                    APE ("Project APE Cache")
typeCito                   CITO
typeDrivein                Drive_In
typeEarth                  Earthcache
typeEvent                  Event
typeFinal                  Final
typeLetterbox              Letterbox
typeLocless                Locationless
typeMaze                   Maze ("Adventure Maze Exhibit")
typeMegaevent              Megaevent
typeMulti                  Multi
typeParking                Parking
typeQuestion               Question
typeReference              Reference
typeStage                  Stage
typeTradi                  Traditional
typeTrailhead              Trailhead
typeUnknown                Mysterie / Unknown
typeVirtual                Virtual
typeWebcam                 Webcam
typeWhereigo               Whereigo