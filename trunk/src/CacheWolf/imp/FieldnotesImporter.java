package CacheWolf.imp;

import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.CacheSize;
import CacheWolf.CacheTerrDiff;
import CacheWolf.CacheType;
import CacheWolf.MyLocale;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import ewe.io.FileReader;
import ewe.io.JavaUtf8Codec;
import ewe.sys.Vm;
import ewe.sys.Time;
import ewe.util.mString;

public class FieldnotesImporter {
	CacheDB cacheDB;
	Preferences pref;
	Profile profile;
	String file;

	public FieldnotesImporter(Preferences pf, Profile prof, String f) {
		pref = pf;
		profile = prof;
		cacheDB = profile.cacheDB;
		file = f;
	}

	public void doIt() {
		try {
			Vm.showWait(true);
			FileReader r = new FileReader(file);
			r.codec=new JavaUtf8Codec();
			parse(r.readAll());
			r.close();
			// save Index
			profile.saveIndex(pref, Profile.NO_SHOW_PROGRESS_BAR);
			Vm.showWait(false);
		} catch (Exception e) {
			// Vm.debug(e.toString());
			Vm.showWait(false);
		}
	}
	
	private void parse(String s) {
		final byte WPPOS=0;
		final byte DATEPOS=1;
		final byte LOGTYPPOS=2;
		String[] l=mString.split(s,'"');
		for (int i = 0; i < l.length; i++) {
			String s1=l[i];
			i++;
			String logText=l[i];
			String[] l1=mString.split(s1,',');
			while (l1[WPPOS].charAt(0)<48) {
				l1[WPPOS]=l1[WPPOS].substring(1);
			}
			while (l1[WPPOS].charAt(0)>122) {
				l1[WPPOS]=l1[WPPOS].substring(1);
			}
			String wayPoint=l1[WPPOS];
			CacheHolder ch = cacheDB.get(wayPoint);
			if (ch!=null) {
				if (l1[LOGTYPPOS].equals(ch.getGCFoundText())) {
					ch.setCacheStatus(l1[DATEPOS].replace('T',' ').replace('Z', ' '));
					ch.setFound(true);
				} else {
					String stmp=ch.getCWLogText(l1[LOGTYPPOS]);
					if(stmp.equals("")) 
						ch.setCacheStatus(l1[LOGTYPPOS]);
					else ch.setCacheStatus(stmp); // Statustext (ohne Datum/Uhrzeit)
					ch.setFound(false);
				}				
				ch.getCacheDetails(false).setCacheNotes(logText);
				ch.save();
			}
		}
	}

}
