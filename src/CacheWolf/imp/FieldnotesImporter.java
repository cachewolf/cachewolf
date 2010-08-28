package CacheWolf.imp;

import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import ewe.io.FileReader;
import ewe.io.JavaUtf8Codec;
import ewe.sys.Vm;
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
		String[] l=mString.split(s,'"');
		for (int i = 0; i < l.length; i++) {
			String s1=l[i];
			i++;
			String logText=l[i];
			String[] l1=mString.split(s1,',');
			if (l1[0].charAt(0)=='\r') {
				l1[0]=l1[0].substring(2);
			}
			String wayPoint=l1[0];
			CacheHolder ch = cacheDB.get(wayPoint);
			if (ch!=null) {
				ch.setCacheStatus(l[1]+l[2]);
				ch.getCacheDetails(false).setCacheNotes(logText);
				ch.save();
				// ch.getCacheDetails(false).saveCacheDetails();
			}
		}
	}

}
