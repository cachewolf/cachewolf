package CacheWolf.imp;

import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.CacheSize;
import CacheWolf.CacheTerrDiff;
import CacheWolf.CacheType;
import CacheWolf.MyLocale;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import HTML.Tmpl.Util;
import HTML.Tmpl.Element.Element;
import ewe.io.BufferedReader;
import ewe.io.FileNotFoundException;
import ewe.io.FileReader;
import ewe.io.IOException;
import ewe.io.JavaUtf8Codec;
import ewe.sys.Vm;
import ewe.sys.Time;
import ewe.util.EmptyStackException;
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
/* TODO Import of files with UNICODE Charset 
	private void read_file(String filename)
			throws FileNotFoundException, 
				IllegalStateException,
				IOException, 
				EmptyStackException
	{
		BufferedReader br=openFile(filename);

		String line;

		Element e = null;
		if(elements.empty())
			e = __template__;
		else
			e = (Element)elements.pop();

		max_includes--;
		while((line=br.readLine()) != null) {
			Util.debug_print("Line: " + line);
			e = parseLine(line+"\n", e);
		}
		max_includes++;

		br.close();
		br=null;

	}
	
	private BufferedReader openFile(String filename)
	throws FileNotFoundException{
		boolean add_path=true;
		if(!elements.empty() && !search_path_on_include)
			add_path=false;

		if(filename.startsWith("/"))
			add_path=false;

		if(this.path == null)
			add_path=false;

		Util.debug_print("open " + filename);
		if(!add_path)
			return new BufferedReader(new FileReader(filename));

		BufferedReader br=null;

		for(int i=0; i<this.path.length; i++) {
			try {
				Util.debug_print("trying " + this.path[i] +	"/" + filename);
				br = new BufferedReader(new FileReader(this.path[i] + "/" + filename));
				break;
			} catch (FileNotFoundException fnfe) {}
		}
		if(br == null) throw new FileNotFoundException(filename);
		return br;
	}
 */
	
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
					String stmp=ch.getCacheStatus();
					ch.setCacheStatus(l1[DATEPOS].replace('T',' ').replace('Z', ' ').trim());
					ch.setFound(true);
				} else {
					String stmp=ch.getCWLogText(l1[LOGTYPPOS]);
					if(stmp.equals("")) 
						ch.setCacheStatus(l1[LOGTYPPOS]); // eingelesener 
					else ch.setCacheStatus(stmp); // Statustext (ohne Datum/Uhrzeit)
					ch.setFound(false);
				}				
				if (!logText.equals("")) ch.getCacheDetails(false).setCacheNotes(logText);
				ch.save();
			}
		}
	}

}
