/*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
	See http://www.cachewolf.de/ for more information.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package CacheWolf.imp;

import CacheWolf.MainForm;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import CacheWolf.database.CWPoint;
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheHolderDetail;
import CacheWolf.database.CacheTerrDiff;
import CacheWolf.database.CacheType;
import CacheWolf.utils.Common;
import CacheWolf.utils.DateFormat;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.STRreplace;
import CacheWolf.utils.SafeXML;
import ewe.io.AsciiCodec;
import ewe.io.JavaUtf8Codec;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.FormBase;
import ewe.util.mString;

public class CSVImporter {
    CacheDB cacheDB;
    String file;
    double maxDistance = 0.0;
    CWPoint startPos;

    String[] header;

    public CSVImporter(String f) {
	cacheDB = MainForm.profile.cacheDB;
	file = f;
    }

    public void doIt() {
	startPos = Preferences.itself().curCentrePt;
	if (startPos == null || !startPos.isValid()) {
	    Preferences.itself().log("Zentrum nicht gesetzt", null);
	    return;
	}
	ImportGui importGui;
	importGui = new ImportGui("CSV Import", ImportGui.ISGC | ImportGui.DIST, 0);
	// doing the input
	if (importGui.execute() == FormBase.IDCANCEL) {
	    return;
	}
	maxDistance = importGui.getDoubleFromInput(importGui.maxDistanceInput, 0);
	if (maxDistance == 0) {
	    importGui.close(0);
	    return;
	}
	MainForm.profile.setDistGC(Common.DoubleToString(maxDistance, 0, 2));
	importGui.close(0);
	try {
	    Vm.showWait(true);
	    ewe.io.TextReader r = null;

	    try {
		String s;
		r = new ewe.io.TextReader(file);
		r.codec = new AsciiCodec();
		// first line -- Heading number of ,
		s = r.readLine();
		if (s.startsWith("ï»¿")) {
		    r.close();
		    r = new ewe.io.TextReader(file);
		    r.codec = new JavaUtf8Codec();
		    s = r.readLine();
		}
		String t = "";
		byte csvTyp = 0; // 0 Tab getrennt, 1 Komma getrennt, 2 Semikolon getrennt
		if (s.indexOf("\t") < 0) {
		    // in der Headerzeile darf kein , oder ; innerhalb eines Textfeldes sein
		    // Wenn Textkennzeichnung ", dann alle in der Headerzeile
		    t = STRreplace.replace(s, "\",\"", "\t");
		    if (t.length() != s.length()) {
			csvTyp = 1; // +Textkennzeichnung "
		    } else {
			t = STRreplace.replace(s, "\";\"", "\t");
			if (t.length() != s.length())
			    csvTyp = 2; // +Textkennzeichnung "
		    }
		    if (csvTyp == 0) {
			if (s.indexOf(",") > 0) {
			    csvTyp = 3;
			    t = STRreplace.replace(s, ",", "\t");
			}
			if (s.indexOf(";") > 0) {
			    csvTyp = 4;
			    t = STRreplace.replace(s, ";", "\t");
			}
		    }
		    header = mString.split(t, '\t');
		} else {
		    header = mString.split(s, '\t');
		}
		int nr_of_elements = header.length;

		String[] l = null;
		// Berücksichtige so gut es geht mehrzeiligen Text
		do {
		    s = "";
		    do {
			t = r.readLine();
			if (t != null) {
			    if (t.length() == 0) {
				if (s.length() > 0)
				    s = s + "<br>";
			    } else {
				switch (csvTyp) {
				case 1:
				    t = STRreplace.replace(t, "\t", " ");
				    t = STRreplace.replace(t, "\",\"", "\t");
				    t = STRreplace.replace(t, "\"\"", "\"");
				    break;
				case 2:
				    t = STRreplace.replace(t, "\t", " ");
				    t = STRreplace.replace(t, "\";\"", "\t");
				    t = STRreplace.replace(t, "\"\"", "\"");
				    break;
				case 3:
				    t = STRreplace.replace(t, "\t", " ");
				    t = STRreplace.replace(t, ",", "\t");
				    break;
				case 4:
				    t = STRreplace.replace(t, "\t", " ");
				    t = STRreplace.replace(t, ";", "\t");
				    break;
				default:
				    // 0 hat schon Tabs
				    break;
				}
				s = s + t;
			    }
			    l = mString.split(s, '\t');
			    if (l.length > nr_of_elements) {
				Preferences.itself().log(s);
				s = "";
			    }
			}
		    } while (l.length < nr_of_elements && t != null);

		    if (l != null) {
			if (l.length == nr_of_elements) {
			    if (!parse(l)) {
				Preferences.itself().log(s);
			    }
			} else {
			    Preferences.itself().log("bug" + s);
			}
		    }

		} while (t != null);

	    } catch (Exception e) {
		Preferences.itself().log("Abort CSVImporter: ", e, true);
		r.close();
		MainForm.profile.saveIndex(Profile.NO_SHOW_PROGRESS_BAR);
		Vm.showWait(false);
		return;
	    }
	    r.close();
	    // save Index
	    MainForm.profile.saveIndex(Profile.NO_SHOW_PROGRESS_BAR);
	    Vm.showWait(false);
	} catch (Exception e) {
	} finally {
	    Vm.showWait(false);
	}
    }

    final byte LAT = 0;
    final byte LON = 1;
    final byte WAYPOINT = 2;
    final byte OWNER = 3;
    final byte CACHENAME = 4;
    final byte STATUS = 5;
    final byte DATEHIDDEN = 6;
    final byte HINT = 7;
    final byte NOTES = 8;
    final byte CREATED = 9;
    final byte LOCATION = 10;
    final byte CODE = 11;
    final byte SIZE = 12;
    final byte DIFFICULTY = 13;
    final byte TERRAIN = 14;
    final byte TYPE = 15;
    final byte LAST = 16;

    String l[] = new String[LAST];

    private boolean parse(String[] ds) {

	for (int i = 0; i < l.length; i++) {
	    l[i] = "";
	}
	l[SIZE] = "1";
	l[DIFFICULTY] = "1";
	l[TERRAIN] = "1";
	boolean munzee = false;
	boolean hyper = false;
	boolean gefunden = false;

	for (int i = 0; i < header.length; i++) {
	    if (header[i].equalsIgnoreCase("LAT") || header[i].toUpperCase().indexOf("NORD") > -1) {
		l[LAT] = STRreplace.replace(ds[i], "\"", ""); // 0.
	    } else if (header[i].equalsIgnoreCase("LON") || header[i].toUpperCase().indexOf("OST") > -1) {
		l[LON] = STRreplace.replace(ds[i], "\"", ""); // 1.
	    } else if (header[i].equalsIgnoreCase("LATLON") || header[i].toUpperCase().indexOf("OORD") > -1) {
		CWPoint coord = new CWPoint(ds[i]); // 0. 1.
		l[LAT] = "" + coord.latDec;
		l[LON] = "" + coord.lonDec;
	    } else if (header[i].equalsIgnoreCase("GO TO") || header[i].equalsIgnoreCase("GOTO")) {
		// 0. 1.
		if (ds[i].length() < 15)
		    return false;
		hyper = true;
		String s = "N48 " + ds[i].substring(1, 7) + "E9 " + ds[i].substring(9);
		CWPoint coord = new CWPoint(s);
		l[LAT] = "" + coord.latDec;
		l[LON] = "" + coord.lonDec;
	    } else if (header[i].equalsIgnoreCase("WAYPOINT") || header[i].equalsIgnoreCase("WP") || header[i].equalsIgnoreCase("WEGPUNKT") || (header[i].toUpperCase().indexOf("GC") > -1 && header[i].toUpperCase().indexOf("CODE") > -1)) {
		l[WAYPOINT] = ds[i]; // 2.
	    } else if (header[i].toUpperCase().indexOf("USER") > -1 || header[i].equalsIgnoreCase("OWNER") || header[i].equalsIgnoreCase("BESITZER")) {
		l[OWNER] = ds[i]; // 3.
	    } else if (header[i].toUpperCase().indexOf("TIT") > -1 || header[i].equalsIgnoreCase("CACHENAME") || header[i].equalsIgnoreCase("CACHE NAME")) {
		l[CACHENAME] = ds[i]; // 4.
	    } else if (header[i].equalsIgnoreCase("FRIENDLYNAME")) {
		l[CACHENAME] = ds[i]; // 4.
		munzee = true;
	    } else if (header[i].toUpperCase().indexOf("FUND") > -1 || header[i].toUpperCase().indexOf("FOUND") > -1 || header[i].toUpperCase().indexOf("FIND") > -1 || header[i].equalsIgnoreCase("STATUS")) {
		l[STATUS] = ds[i]; // 5.
	    } else if (header[i].equalsIgnoreCase("DEPLOYED") || header[i].equalsIgnoreCase("DATEHIDDEN")) {
		l[DATEHIDDEN] = ds[i]; // 6.
	    } else if (header[i].toUpperCase().indexOf("HINT") > -1 || header[i].toUpperCase().indexOf("HINW") > -1) {
		l[HINT] = ds[i]; // 7.
	    } else if (header[i].toUpperCase().indexOf("OMMENT") > -1 || header[i].toUpperCase().indexOf("NOT") > -1) {
		l[NOTES] = ds[i]; // 8.
	    } else if (header[i].equalsIgnoreCase("CREATED")) {
		l[CREATED] = ds[i]; // 9.
	    } else if (header[i].equalsIgnoreCase("LOCATION")) {
		l[LOCATION] = ds[i]; // 10.
	    } else if (header[i].equalsIgnoreCase("CODE")) {
		l[CODE] = ds[i]; // 11.
	    } else if (header[i].equalsIgnoreCase("SIZE") || header[i].toUpperCase().indexOf("GROE") > -1 || header[i].toUpperCase().indexOf("GRÖ") > -1) {
		l[SIZE] = ds[i]; // 12.
		if (l[SIZE].length() == 0)
		    l[SIZE] = "1";
	    } else if (header[i].equalsIgnoreCase("DIFFICULTY") || header[i].toUpperCase().indexOf("SCHWI") > -1) {
		if (ds[i].startsWith("0"))
		    l[DIFFICULTY] = ds[i].substring(1); // 13.
		else
		    l[DIFFICULTY] = ds[i]; // 13.
		l[DIFFICULTY] = STRreplace.replace(l[DIFFICULTY], " Mai", "5");
		if (l[DIFFICULTY].length() == 0)
		    l[DIFFICULTY] = "1";
	    } else if (header[i].equalsIgnoreCase("TERRAIN")) {
		if (ds[i].startsWith("0"))
		    l[TERRAIN] = ds[i].substring(1); // 14.
		else
		    l[TERRAIN] = ds[i]; // 14.
		l[TERRAIN] = STRreplace.replace(l[TERRAIN], " Mai", "5");
		if (l[TERRAIN].length() == 0)
		    l[TERRAIN] = "1";
	    } else if (header[i].toUpperCase().indexOf("TYP") > -1) {
		l[TYPE] = ds[i]; // 15.
	    }
	}
	// 0. + 1. Koordinaten
	CWPoint tmpPos = new CWPoint(Preferences.itself().curCentrePt);
	try {
	    try {
		double lat = Common.parseDoubleException(l[LAT]);
		double lon = Common.parseDoubleException(l[LON]);
		tmpPos.set(lat, lon);
	    } catch (Exception e) {
		if (!(l[LAT].startsWith("N") || l[LAT].startsWith("S")))
		    l[LAT] = "N" + l[LAT];
		if (!(l[LON].startsWith("E") || l[LON].startsWith("W")))
		    l[LON] = "E" + l[LON];
		tmpPos = new CWPoint(l[LAT] + "," + l[LON]); // 0. 1.
	    }
	    double tmpDistance = tmpPos.getDistance(startPos);
	    if (tmpDistance > maxDistance) {
		// Preferences.itself().log("CSVImporter: not imported " + l[CACHENAME] + ", Distance = "+ tmpDistance);
		return false;
	    }
	    if (!tmpPos.isValid()) {
		//return false;
	    }
	} catch (Exception e) {
	    // Use default coordinates (for only import GCCode)
	    // Preferences.itself().log("Error CSVImporter at: " + l[CACHENAME] + "(" + l[LAT] + " " + l[LON] + ")", e);
	    // return false;	    
	}
	// 2. Wegpunkt
	String wayPoint;
	if (munzee) {
	    // generate the Waypoint
	    l[CODE] = "0000" + l[CODE];
	    l[CODE] = l[CODE].substring(l[CODE].length() - 5, l[CODE].length() - 1);
	    wayPoint = l[OWNER].toUpperCase();
	    if (wayPoint.length() > 8) {
		wayPoint = wayPoint.substring(0, 8);
	    }
	    wayPoint = "MZ" + wayPoint + l[CODE];
	} else {
	    if (l[WAYPOINT].length() > 0) {
		// aus Datei
		wayPoint = l[WAYPOINT];
	    } else {
		// ? schon einer an der gleichen Koordinate
		wayPoint = getWPofCoordinates(tmpPos);
		if (wayPoint.length() == 0) {
		    // nein --> neuer
		    wayPoint = MainForm.profile.getNewWayPointName(extractFileName(file).substring(0, 2).toUpperCase());
		} else {
		}
	    }
	}
	// neu oder ändern
	CacheHolder ch = cacheDB.get(wayPoint);
	if (ch == null) {
	    ch = new CacheHolder(wayPoint);
	    cacheDB.add(ch);
	}
	if (l[TYPE].length() > 0) {
	    try {
		ch.setType(CacheType.gcSpider2CwType(l[TYPE]));
	    } catch (Exception e) {
		ch.setType(CacheType.CW_TYPE_TRADITIONAL);
	    }
	} else
	    ch.setType(CacheType.CW_TYPE_TRADITIONAL);
	// 3. OWNER
	if (l[OWNER].length() > 0) {
	    ch.setOwner(l[OWNER]);
	} else {
	    ch.setOwner("Unknown");
	}
	// 4. CacheName - Titel
	if (munzee) {
	    ch.setName("MZ - " + l[CACHENAME]);
	} else if (hyper) {
	    String s = extractFileName(file).substring(0, 2).toUpperCase();
	    if (wayPoint.startsWith(s)) {
		ch.setName(wayPoint + " " + l[STATUS] + " " + l[HINT]);
	    } else {
		String st = STRreplace.replace(l[NOTES], "# ", "#");
		int i1 = st.indexOf("#");
		if (i1 > -1) {
		    int i2 = st.indexOf(" ", i1);
		    String std;
		    if (i2 < 0) {
			std = st.substring(i1);
		    } else {
			std = st.substring(i1, i2);
		    }
		    std = (std + "    ").substring(0, 4);
		    ch.setName(s + std + " " + l[STATUS] + " " + l[HINT]);
		} else {
		    if (ch.getName().length() == 0) {

		    }
		}
	    }
	} else {
	    ch.setName(l[CACHENAME]);
	}
	if (ch.getName().length() == 0)
	    ch.setName(ch.getCode());
	// 5. Status gefunden
	String statusText = "";
	if (l[STATUS].length() > 0) {
	    gefunden = true;
	    statusText = CacheType.getFoundText(ch.getType());
	}
	if (hyper) {
	    boolean dnf = false;
	    if (l[NOTES].toUpperCase().indexOf("DNF") >= 0) {
		dnf = true;
		statusText = MyLocale.getMsg(319, "Didn't find it");
		ch.setName(ch.getName() + " DNF");
	    }
	    if (wayPoint.startsWith("GC")) {
		if (dnf) {
		    ch.setIsSolved(true);
		    gefunden = false;
		} else {
		    if (l[STATUS].length() == 0) {
			gefunden = false;
			ch.setIsSolved(true);
		    }
		}
	    }
	}
	ch.setFound(gefunden);
	ch.setStatus(statusText);
	// 6. Datum versteckt
	if (l[DATEHIDDEN].length() > 0) {
	    if (l[DATEHIDDEN].charAt(0) == '0') {
		// return false;
		l[DATEHIDDEN] = l[CREATED];
	    }
	} else {
	    l[DATEHIDDEN] = l[CREATED];
	}
	String dateHidden;
	try {
	    dateHidden = DateFormat.toYYMMDD(l[DATEHIDDEN].substring(0, 10));
	} catch (Exception e) {
	    dateHidden = DateFormat.toYYMMDD(new Time());
	}
	ch.setHidden(dateHidden);
	ch.setSize((byte) (Common.parseInt(l[SIZE]) + 1));
	ch.setDifficulty(CacheTerrDiff.v1Converter(l[DIFFICULTY]));
	ch.setTerrain(CacheTerrDiff.v1Converter(l[TERRAIN]));
	// Koordinaten
	ch.setWpt(tmpPos);
	// Details
	CacheHolderDetail chd = ch.getDetails();
	// Description
	if (munzee) {
	    chd.setLongDescription(l[NOTES]);
	} else {
	    if (hyper) {
		chd.setLongDescription("gef:" + l[STATUS] + "<br>von: " + l[CODE] + "<br>" + l[NOTES] + "<br>" + l[HINT]);
	    } else {
		chd.setCacheNotes(l[HINT] + l[NOTES]);
	    }
	}
	chd.setHints(l[HINT]);
	// Country , State
	final String location = l[LOCATION];
	if (location.length() != 0) {
	    final int countryStart = location.lastIndexOf(" ");
	    if (countryStart > -1) {
		chd.setCountry(SafeXML.html2iso8859s1(location.substring(countryStart + 1).trim()));
		chd.setState(SafeXML.html2iso8859s1(location.substring(0, countryStart).trim()));
	    } else {
		chd.setCountry(location.trim());
		chd.setState("");
	    }
	} else {
	    ch.getDetails().setCountry("");
	    ch.getDetails().setState("");
	}
	ch.saveCacheDetails();
	return true;
    }

    private String getWPofCoordinates(CWPoint coords) {
	int s = cacheDB.size();
	if (s > 0) {
	    for (int i = 0; i < cacheDB.size(); i++) {
		CacheHolder ch = cacheDB.get(i);
		if (ch.getWpt().equals(coords)) {
		    return ch.getCode();
		}
	    }
	}
	return "";
    }

    private String extractFileName(String filePathName) {
	if (filePathName == null)
	    return null;

	int dotPos = filePathName.lastIndexOf('.');
	int slashPos = filePathName.lastIndexOf('\\');
	if (slashPos == -1)
	    slashPos = filePathName.lastIndexOf('/');

	if (dotPos > slashPos) {
	    return filePathName.substring(slashPos > 0 ? slashPos + 1 : 0, dotPos);
	}

	return filePathName.substring(slashPos > 0 ? slashPos + 1 : 0);
    }

}
