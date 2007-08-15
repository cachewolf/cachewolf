package CacheWolf;

import ewe.ui.*;
import ewe.fx.*;

////////////////////////////////////////////////////////////////////////////////
//*  Feature: Uhrzeit / Datum Button zum einfügen in dem Feld "Notizen"
//*  Bugfix:  (canceled!) Bilder von externen Quellen (nicht gc.com) herunterladen 
//*  Feature: (cancelled!) Umkreissuche auf Anzahl Caches oder Entfernung erweitern
//*  Feature: (cancelled!) Bei der Umkreissuche (optional) werden gefundene nicht geladen
//j   Update: Filterverhalten ändern --> es wird nur das angezeigt, was ausgewählt wurde
//j   Update:  Vollständige Dokumentation auf Deutsch vorhanden
//j   Feature: (naja) CacheWolf hat nun ein eigenes Icon
//j  Bugfix:  Archivierte werden nicht mehr mit "An error..." angezeigt sondern sind tief dunkelrot unterlegt
//j  Feature: Bildnamen bleiben erhalten und werden in der Thumbnail - Übersicht angezeigt
//j  Feature: Uhrzeit anzeigen
//j  Feature: Blacklist einführen
//j  Feature: Native Destinator export
//j  Feature: Markieren in der Listenansicht und anwenden von "irgendeiner" Aktion
//j  Update:  Filter nach "Archiviert"
//j  Update:  Filtern nach zuletzt gefunden
//j  Update:  HTML export enthält Bilder
//k  Feature: Hinterlegen von Karten im Radar
//k  Feature: Georeferenzieren von Karten
//k  Feature: Beliebiges zoomen im Radar
//k  Feature: Navigation zum Cache
//   Feature: Log eines Caches mit den Informationen im Notizen-Feld
//   Feature: Statistiken:
//            *) aktivste Cacher im Umkreis,
//	      *) aktivste letzten x Tage
//   Bugfix:  Sortierung sollte nach Filter aufheben gleich bleiben
//   Feature: use GPS Babel to further enhance export capabilities
//   Feature: Wetterprognose von wetter.com holen
//   Feature: Wolflanguage UTM konversionen und projektionen
//l  Feature: Korridorsuche
//l  Feature: CacheAttributes werden angezeigt
//l  Feature: Man kann nach CacheAttributes filtern
//   Feature: CacheIcons werden angezeigt
//   Feature: Man kann nach CacheIcons filtern
//   Feature: Statistikseite
//l  Feature: Filter über Fläche (Vielvieleck!)
//   Update:  Dokumentation auf Englisch vorhanden
//   Feature: Ad hoc erstellen einer HTML Seite und im Pocketexplorer anzeigen
//            lassen
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// 0.9i
// * Feature: System Infoscreen vorhanden
// * Feature: CW per Kommandozeile starten lassen:
//            Beispiel: cachewolf spider n --> spider= aktualisiere und hole neue
//						      daten
//					      n = umkreis (eine Zahl) von den 
//						  Koordinaten in den
//					          Präferenzen
// * Feature: Spidern abbrechen können
// * Feature: Wenn Cache gefunden und temp. offline: grüner Hintergrund mit 
//	      roter Schrift
// * Feature: Travelbugs mitsamt Beschreibung werden gespidert
// * Feature: Cachedaten in ein anderes Verzeichnis verschieben. Existierende
//	      index.xml Datei wird verwendet. Alle Daten werden im 
//	      Ursprungsverzeichnis werden gelöscht.
// * Feature: GPX import nun vorhanden
// * Kosmetisch: Zeilenhöhe in der Listenansicht verringert
// * Kosmetisch:  Punktgrafiken in der Zeilenansicht verkleinert
// * Bugfix:  Profile speichern nun die richtigen Koordinaten
// * Bugfix:  Selektierter Cache bleibt nach sortierung selektiert
// * Bugfix:  Beim HTML export werden (nur) die richtigen Logs zum Cache 
//            exportiert
// * Bugfix:  Spaltenbreite bleibt nun erhalten, wenn man zwischen
//	      Ansichten hin und her springt
// * Bugfix:  Laden von locationless caches geht wieder
// * Bugfix:  Kleiner Bugfix im GPX Exporter
// * Bugfix:  Absturz beim Laden von Seiten, die Bilder mit PHP anzeigen, ist
//	      behoben
// * Bugfix:  Cache wird als found angezeigt obwohl es ein DNF Eintrag ist
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// 0.9h
// * Feature: Nur logbilder der neuesten N logs laden!
//	      N ist durch den Anwender einstellbar
// * Feature: Karten zu einzelnen Caches laden
// * Update:  Existierende Cacheinformationen werden nicht aktuallisiert, wenn
//	      neue Caches per LOC oder LIST(E) geladen werden
// * Update:  Thumbnails werden nicht verzerrt dargestellt
// * Update:  Log- und Cachebilder lassen sich klein und groß anschauen
// * Update:  Speichern ohne beenden
// * Bugfix:  Multipunkt Umkreissuche (CW hatte sich vollkommen abgeschossen!)
// * Update:  Erkennen von neuen Logs ersteinmal deaktiviert!
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// 0.9g
// * Bugfix:  Löser funktioniert wieder
// * Bugfix:  Profile speichern auch die aktuellen Koordinaten
// * Feature: Export direkt in das TomTom Format
// * Feature: Anzeige von neuen Caches (gelber stern)
// * Feature: Anzeige von updates zur Cachebeschreibung (roter stern),
//	      bzw. Logs wird angezeigt (blauer stern)
// * Bugfix:  CW hängt nicht mehr bei der Multipunkt Umkreissuche, wenn der letzte
//            Eintrag nicht mit ";" abgeschlossen wird
// * Feature: relative Pfadnamen zu den Cacheverzeichnissen; 
//            hiermit leichter transportierbar, z.B. USB Stick
// * Update:  Das Statusfenster beim spidern der Caches nervt nicht mehr
// * Update:  Sanduhr wird beim filtern angezeigt
// * Update:  Filter ignoriert Groß- und Kleinschreibung
// * Update:  CacheRadar nun zentriert
// * Update:  Reiter nun ebenfalls "eingedeutscht"
// * Feature: Es können "profile" angelegt werden
// * Feature: Export zu CacheMate PDB Datei (mit cmconvert)
// * Feature: Export direkt zum Garmin (mit gpsbabel)
// * Bugfix:  Löschen einzelner Caches wieder möglich
////////////////////////////////////////////////////////////////////////////////

////////////////////
// 0.9f B2
// * Update: Hints and Logs: Tastatur unterdrücken
// * Bugifx: Koordinaten unter Preferenzen: Eingabe von Komma und Punkt bei Minuten möglich
// * Update: Ein und ausschalten von Spalten ist möglich ohne Neustart
// * Bugfix: "Found" nicht als Standard, wenn kein Alias eingetragen ist
// * Bugfix: Status läßt sich auf "Leer" zurücksetzen
////////////////////

////////////////////
// 0.9f
// * Feature: Multipunkt Umkreissuche
// * Bugfix:  Einige Bilder werden nicht richtig heruntergeladen
// * Update:  Spidern sollte nun spürbar schneller funktionieren
// * Update:  Sanduhr wird während Sortierung und an anderen Stellen angezeigt
// * Update:  Status nun mit vordefinierten Werten belegt
// * Update:  Status wird mit dem Laden des Caches gesetzt
// * Bugfix:  Sortieren mit eingeschaltetem Filter erzeugt kein Müll mehr
// * Bugfix:  '"', ''', '´' in Cachenamen werden richtig erkannt
// * Bugfix:  Weitere Kleinigkeiten mit Umlauten bereinigt
// * Bugfix:  Bilderanzeig hängt nicht mehr, wenn bestimmte Bilder nicht geladen
//	      werden können
// * Feature: Alle Angaben in den Details sind veränderbar
// * Feature: Deutsch / Englisch Programmversion; theoretisch mehr möglich.
// * Feature: Ändermöglichkeit für alle Felder in den Details
// * Update:  Suche filtert alle nicht zutreffenden Caches aus der Liste. Besser
//            als nur gelb anzeigen
////////////////////

/////////////////
// 0.9e
// * Bugfix:  Umkreissuche funktioniert nun auch ohne Proxy
// * Feature: Filterfunktionalität um Filterumkehr erweitert
// * Update:  HTML export templates schöner gemacht
// * Update:  In den Logs erscheinen die Bildbeschreibungen 
//            (allerdings noch nicht bei den Bildern selbst)
// * Update:  Es werden immer nur 5 logs gezeigt. Performancegewinn bei der 
//            Anzeige auf PocketPC!
// * Update:  SIP nervt nicht mehr "die dritte", bzw. auf PocketPC wird die
//			  SIP Bar für die Menüs genutzt.
// * Update:  Formatierung bei PCX export angepasst
// * Update:  Es werden nun die Originalbilder (Logs, originalgröße) gespidert
// * Feature: Noch ein klick auf ein Bild zeigt es in Originalgrösse
// * Bugfix:  Cachegröße small wird erkannt
// * Bugfix:  TOP50 (OVL) export richtiggestellt
// * Bugfix:  Fehlermeldung, bei Umkreissuche, wenn keine Koordinaten
//            in den Preferenzen gesetzt sind
// * Bugfix:  PCX5 Export sollte nun gefundene mit offener Cachebox versehen
// * Feature: Export zu TomTom ASC
// * Feature: Export zu MS AutoRoute CSV
// * Bugfix:  Einige Korrekturen in den Entfernungs- und Kursberechnungen.
/////////////////

/////////////////
// 0.9d
// * Bugfix:  Erkennen von Änderungen in den Preferenzen, ohne Neustart
// * Bugfix:  Darstellung Cachenamen im Radar auf PPC entgültig richtiggestellt.
// * Update:  Sanduhr wird dargestellt, wenn der Wolf Bilder, Beschreibung und Hints & Logs aufbaut.
// * Bugfix:  SIP nervt nicht mehr 2ter Versuch
// * Bugfix:  "&" wird nun richtig verarbeitet
// * Bugfix:  CITO wird nun erkannt
// * Update:  Icons im Radarpanel werden genauer positioniert
/////////////////

/////////////////
// 0.9c
//	* Bugfix: OVL export enthält text (GC - Name)
//	* Update: Erkennen von Earthcaches
//	* Bugfix: Nicht erkannte Caches sollten nicht mehr zum Absturz führen.
//	* Bugfix: Zu viele <SPAN> tags werden erkannt
//	* Bugfix: Darstellung in Radar auf dem PPC
//	* Bugfix: SIP nervt nicht mehr auf PDA
//	* Bugfix: CacheWolf hängt sich nicht mehr auf, wenn man ein letzten Cache löscht!
//	* Update: Export zu GPX nun möglich
//	* Update: Member Only Caches werden erkannt und nicht geladen
/////////////////


/////////////////
//	0.9b 
//	* Update: Spider LOCs
//	* Bugfix: Ä,Ö,Ü werden erkannt
//  * Sortieren nach Status bugfix
//	* Tooltips bei den Reitern
//	* Filter ok für "found by" (Namen müßen durch "," getrennt werden)
//	* Update: Übernahme von "Status"!
//	* Bugfix: Nach einem filter funktionierte das sortieren nicht mehr
//	* Update: Spider Liste (Caches müßen mit ',' getrennt werden)
//	* Bugfix: Solver und Zahlenformate
/////////////////

/**
*	This is the application starter class.
*	It startes the Ewe VM and creates the main form that displays
*	the user interface.
*	@param 	null	no parameters required.
*	@return null	does not return any return codes
*	@see			MainForm
*	@author	 Marc Schnitzler
*	@version version of this class, Date: date of release of the version of the class 
*/

import ewe.sys.*;

public class CacheWolf extends Editor{
	
	
	public static void main(String args[])
	{
		//start with parameters:
		//args[0]: spider
		//args[1]: distance
		ewe.sys.Vm.startEwe(args);
/*		Gui.screenIs(Gui.PDA_SCREEN);
		Rect s = (Rect)Window.getGuiInfo(Window.INFO_SCREEN_RECT,null,new Rect(),0);
		//Gui.screenIs(Gui.PDA_SCREEN)
		if (Vm.isMobile() && s.height >= 400) {
			Font defaultGuiFont = mApp.findFont("gui");
			int sz = (int)(defaultGuiFont.getSize());
			Font newGuiFont = new Font(defaultGuiFont.getName(), defaultGuiFont.getStyle(), sz); 
			mApp.addFont(newGuiFont, "gui"); 
			mApp.fontsChanged();
			mApp.mainApp.font = newGuiFont;
		}
*/		
		if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
			Vm.setSIP(Vm.SIP_LEAVE_BUTTON);
		}
		
		if(args.length > 0){
			if(args[0].equals("test")){
				Test t=new Test(); 
				t.testAll();
			}
		}
		Editor mainF = new MainForm();
		Device.preventIdleState(true);
		mainF.execute();
		Device.preventIdleState(false);
		ewe.sys.Vm.exit(0);
	}
	
}

// for javadoc see: http://java.sun.com/j2se/javadoc/writingdoccomments/index.html#exampleresult
// or the local files "JavaDoc" directory
// Javadoc Main Page: http://java.sun.com/j2se/javadoc/index.jsp
// javadoc -classpath ewe.jar -d "cachewolf doc" cachewolf/*.java
