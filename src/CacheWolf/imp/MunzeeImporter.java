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

import CacheWolf.CWPoint;
import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.CacheHolderDetail;
import CacheWolf.CacheSize;
import CacheWolf.CacheTerrDiff;
import CacheWolf.CacheType;
import CacheWolf.Common;
import CacheWolf.DateFormat;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import CacheWolf.STRreplace;
import CacheWolf.SafeXML;
import ewe.io.JavaUtf8Codec;
import ewe.sys.Vm;
import ewe.ui.FormBase;
import ewe.util.mString;

public class MunzeeImporter {
	CacheDB cacheDB;
	Preferences pref;
	Profile profile;
	String file;
	double maxDistance = 0.0;
	CWPoint startPos;

	public MunzeeImporter(Preferences pf, Profile prof, String f) {
		pref = pf;
		profile = prof;
		cacheDB = profile.cacheDB;
		file = f;
	}

	public void doIt() {
		startPos = pref.getCurCentrePt();
		if (startPos == null || !startPos.isValid()) {
			pref.log("Zentrum nicht gesetzt", null);
			return;
		}
		OCXMLImporterScreen options;
		options = new OCXMLImporterScreen("Munzee Import",
				OCXMLImporterScreen.ISGC
				| OCXMLImporterScreen.DIST
				);
		// doing the input
		if (options.execute() == FormBase.IDCANCEL) {
			return;
		}
		if (options.maxDistanceInput != null) {
			final String maxDist = options.maxDistanceInput.getText();
			maxDistance = Common.parseDouble(maxDist);
			if (maxDistance == 0) {
				options.close(0);
				return;
			}
			profile.setDistGC(maxDist);
		}
		options.close(0);
		try {
			Vm.showWait(true);
			ewe.io.TextReader r = null;

			try {
				r = new ewe.io.TextReader(file);
				r.codec=new JavaUtf8Codec();
				// first line -- Heading number of ,
				String s = r.readLine();
				String t = "";
				s = STRreplace.replace(s, "\",\"", "\t");
				String[] l=mString.split(s,'\t');
				int nr_of_elements = l.length;
				//
				do {
					s="";
					do {
						t = r.readLine();
						if (t != null) {
							if (t.length() == 0)
								s = s + "<br>";
							else {
								t=STRreplace.replace(t, "\t", " ");
								t=STRreplace.replace(t, "\",\"", "\t");
								t=STRreplace.replace(t, "\"\"", "\"");
								s = s + t;
							}
							l = mString.split(s,'\t');
						}
					} while (l.length < nr_of_elements);
					if (!l[nr_of_elements - 1].endsWith("\"") && l[l.length - 1].endsWith("\"") ){
						int d = l.length - nr_of_elements;
						for (int i = 5; i < 5 + d; i++) {
							l[4] = l[4] + l[i];
						}
						for (int i = 5; i < l.length - d; i++) {
							l[i] = l[i+d];
						}
					}
					if (t != null) {
						if (!l[10].endsWith("\"") || !l[0].startsWith("\"") ) {
							pref.log("Error MunzeeImporter at: " + s, null);
							// return;
						}
						else parse(l);
					}
				} while (t != null);
			}
			catch (Exception e) {
				pref.log("Abort MunzeeImporter: ", e, true);
				r.close();
				profile.saveIndex(pref, Profile.NO_SHOW_PROGRESS_BAR);
				Vm.showWait(false);
				return;
			}
			r.close();
			// save Index
			profile.saveIndex(pref, Profile.NO_SHOW_PROGRESS_BAR);
			Vm.showWait(false);
		} catch (Exception e) {
		}
		finally {
			Vm.showWait(false);
		}
	}

	private boolean parse(String[] l) {
		final byte LAT=0;
		final byte LON=1;
		final byte CREATED=2;
		final byte DEPLOYED=3;
		final byte NOTES=4;
		final byte FRIENDLYNAME=5;
		final byte LOCATION=6;
		final byte USERNAME=7;
		final byte CODE=8;
		// capture_type_id
		// special_logo
		if (l[DEPLOYED].length() > 0){
			if (l[DEPLOYED].charAt(0) == '0') {
				// return false;
				l[DEPLOYED] = l[CREATED];
			}
		}
		else {
			l[DEPLOYED] = l[CREATED];
		}
		l[LAT] = l[LAT].substring(1); // " weg
		l[CODE] = "0000"+l[CODE];
		l[CODE] = l[CODE].substring(l[CODE].length()-5,l[CODE].length()-1);
		String wayPoint=l[USERNAME].toUpperCase();
		if (wayPoint.length() > 8) {
			wayPoint=wayPoint.substring(0, 8);
		}
		wayPoint="MZ"+wayPoint+l[CODE];
		CWPoint tmpPos = new CWPoint();
		try {
			double lat = Common.parseDoubleException(l[LAT]);
			double lon = Common.parseDoubleException(l[LON]);
			tmpPos.set(lat, lon);
			double tmpDistance = tmpPos.getDistance(startPos);
			if (tmpDistance > maxDistance) {
				// pref.log("MunzeeImporter: not imported " + l[FRIENDLYNAME] + ", Distance = "+ tmpDistance);
				// return false;
			}
		} catch (Exception e) {
			pref.log("Error MunzeeImporter at: " + l[FRIENDLYNAME] + "("+ l[LAT] + " " + l[LON] + ")", e);
			return false;
		}

		CacheHolder ch = cacheDB.get(wayPoint);
		if (ch == null) {
			ch = new CacheHolder(wayPoint);
			cacheDB.add(ch);
		}
		ch.setCacheOwner(l[USERNAME]);
		ch.setCacheName("MZ - " + l[FRIENDLYNAME]);
		ch.setDateHidden(DateFormat.toYYMMDD(l[DEPLOYED].substring(0, 10)));
		ch.setType(CacheType.CW_TYPE_TRADITIONAL);
		ch.setPos(tmpPos);
		CacheHolderDetail chd = ch.getCacheDetails(false);
		chd.setLongDescription(l[NOTES]);
		ch.setCacheSize(CacheSize.CW_SIZE_OTHER);
		ch.setHard(CacheTerrDiff.CW_DT_10);
		ch.setTerrain(CacheTerrDiff.CW_DT_10);
		final String location = l[LOCATION];
		if (location.length() != 0) {
			final int countryStart = location.lastIndexOf(" ");
			if (countryStart > -1) {
				chd.Country = SafeXML.cleanback(location.substring(countryStart + 1).trim());
				chd.State = SafeXML.cleanback(location.substring(0, countryStart).trim());
			} else {
				chd.Country = location.trim();
				chd.State = "";
			}
		} else {
			ch.getCacheDetails(false).Country = "";
			ch.getCacheDetails(false).State = "";
		}
		ch.save();
		return true;
	}

}
