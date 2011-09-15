    /*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
    See http://developer.berlios.de/projects/cachewolf/
    for more information.
    Contact: 	bilbowolf@users.berlios.de
    			kalli@users.berlios.de

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
package CacheWolf;

import CacheWolf.utils.FileBugfix;
import ewe.io.FileBase;
import ewe.io.FileReader;
import ewe.io.IOException;
import ewe.sys.Handle;
import ewe.ui.ProgressBarForm;

public class Rebuild {
	private int cacheXmlVersion;

	public Rebuild() { // Public constructor
	}

	public void rebuild() {
		int i;
		Profile prof = Global.getProfile();
		CacheDB cacheDB=prof.cacheDB;

		myProgressBarForm pbf = new myProgressBarForm();
		Handle h = new Handle();

		int count = 0;
		int nAdded = 0;
		pbf.setTask(h, "Reading Directory, be patient");
		pbf.exec();
		h.progress = (float) 0.5;
		h.changed();
		String[] CacheFiles = new FileBugfix(prof.dataDir).list("*.xml",FileBase.LIST_FILES_ONLY | FileBase.LIST_DONT_SORT);
		pbf.setTask(h, "preparing XML-files");
		for (i = 0; i < CacheFiles.length; i++) {
			int pos = CacheFiles[i].lastIndexOf('.');
			if (pos < 0)
				continue;
			String wayPoint = CacheFiles[i].substring(0, pos).toUpperCase();
			if (wayPoint.equalsIgnoreCase("index")) CacheFiles[i] = null;
			else {
				count++;
			}
		}
		cacheDB.clear();
		pbf.setTask(h, MyLocale.getMsg(209, "Rebuilding index"));
		if (count > 0) {
			int nProcessed = 0;
			// Now do the actual work
			String details="";
			for (i = 0; i < CacheFiles.length; i++) {
				if (CacheFiles[i] != null) {
					
					nProcessed++;
					if (nProcessed%100==0) {
						h.progress = ((float) nProcessed) / (float) (count);
						h.changed();
					}
					int start=0;
					boolean doit=true;
					try {
						FileReader in = new FileReader(prof.dataDir + CacheFiles[i]);
						details=in.readAll();
						in.close();
						start = details.indexOf("<CACHE ");
						if (start < 0) doit=false;
						else if (details.indexOf("<CACHEDETAILS>") < 0) doit=false;					
					}
					catch (IOException e) {
						doit=false;
					};
					
					if (doit) {
						int end;
						int vstart=details.indexOf("<VERSION value = \"");						
						if (vstart >= 0) {
							cacheXmlVersion = Integer.valueOf(details.substring(vstart + 18, details.indexOf("\"",vstart + 18))).intValue();
						}
						else {
							cacheXmlVersion = 1;
						}
						end = details.indexOf("/>", start);
						CacheHolder ch = new CacheHolder(details.substring(start, end + 2),cacheXmlVersion);
						cacheDB.add(ch);
						nAdded++;
						Global.getPref().log(ch.getWayPoint() + " added. (" + nAdded + ")");
						// CacheFiles[i] = null;
					} else
						Global.getPref().log("File " + CacheFiles[i] + " not entered to index.xml");
						;
				}
				if (pbf.isClosed)
					break;
			}
			prof.buildReferences();
			prof.saveIndex(Global.getPref(), true);
		}
		pbf.exit(0);
	}

	class myProgressBarForm extends ProgressBarForm {
		boolean isClosed = false;

		protected boolean canExit(int exitCode) {
			isClosed = true;
			return true;
		}
	}

}
