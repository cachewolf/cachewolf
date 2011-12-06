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
import CacheWolf.Common;
import CacheWolf.MyLocale;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import ewe.io.FileReader;
import ewe.io.IOException;
import ewe.io.JavaUtf8Codec;
import ewe.io.TextCodec;
import ewe.sys.Vm;
import ewe.ui.FormBase;
import ewe.util.ByteArray;
import ewe.util.CharArray;
import ewe.util.mString;
import CacheWolf.STRreplace;
import CacheWolf.CacheType;
import CacheWolf.navi.TrackPoint;
import CacheWolf.CacheHolderDetail;

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
								s = s + t;
								s=STRreplace.replace(s, "\",\"", "\t");
								s=STRreplace.replace(s, "\"\"", "\"");
							}
							l = mString.split(s,'\t');
						}
					} while (l.length < nr_of_elements);
					if (t != null) {
						if (!l[8].endsWith("\"") || !l[0].startsWith("\"") ) {
							pref.log("error MunzeeImporter at: " + s, null);
							// return;
						}
						else parse(l);
					}
				} while (t != null);
			}
			catch (Error e) {
				r.close();
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
		l[LAT] = l[LAT].substring(1); // " weg
		l[CODE] = l[CODE].substring(0, l[CODE].length()-1);
		String wayPoint="MZ"+l[USERNAME].toUpperCase()+l[CODE];
		CWPoint tmpPos = new CWPoint();
		try {
			double lat = Double.parseDouble(l[LAT]);
			double lon = Double.parseDouble(l[LON]);
			tmpPos.set(lat, lon);
			final double tmpDistance = tmpPos.getDistance(startPos);
			if (tmpDistance > maxDistance)
				return false;
		} catch (Exception e) {
			return false;
		}

		CacheHolder ch = cacheDB.get(wayPoint);
		if (ch == null) {
			ch = new CacheHolder(wayPoint);
			cacheDB.add(ch);
		}
		ch.setCacheOwner(l[USERNAME]);
		ch.setCacheName(l[FRIENDLYNAME] + " " + l[LOCATION]);
		ch.setDateHidden(l[CREATED]);
		ch.setType(CacheType.CW_TYPE_CUSTOM);
		ch.setPos(tmpPos);
		CacheHolderDetail chd = ch.getCacheDetails(false);
		chd.setLongDescription(l[NOTES]);
		ch.save();
		return true;
	}

}
