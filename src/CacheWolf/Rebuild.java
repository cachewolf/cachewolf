package CacheWolf;

import CacheWolf.utils.FileBugfix;
import ewe.io.FileBase;
import ewe.io.FileReader;
import ewe.sys.Handle;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.ui.ProgressBarForm;

public class Rebuild {
	private int cacheXmlVersion;
	private int startOfCacheDetails;

	public Rebuild() { // Public constructor
	}

	public void rebuild() {
		int i;
		Profile prof=Global.getProfile();

		myProgressBarForm pbf = new myProgressBarForm();
		Handle h = new Handle();

		int orphans=0; // xml Files without entry in database : Changed to all entries!!!
		int nAdded=0;  // caches added to database
		boolean checkOlder;
		MessageBox mBox = new MessageBox("", MyLocale.getMsg(5522,"Check for older Versions xml-Files ( without <CACHE... /> )") , FormBase.IDYES |FormBase.IDNO);
		if (mBox.execute() == FormBase.IDOK){checkOlder=true;}
		else {checkOlder=false;}
		// still time consuming the list files
		pbf.setTask(h, "Reading Directory, be patient");
		pbf.exec();
		h.progress=(float) 0.5;
		h.changed();
		String[] CacheFiles = new FileBugfix(prof.dataDir).list("*.xml", FileBase.LIST_FILES_ONLY|FileBase.LIST_DONT_SORT);
		pbf.setTask(h,"checking XML-files");
		for (i=0; i<CacheFiles.length; i++) {
				int pos=CacheFiles[i].lastIndexOf('.');
				if (pos<0) continue;
				String wayPoint=CacheFiles[i].substring(0,pos).toUpperCase();
				if (wayPoint.equalsIgnoreCase("index"))	// Check for index.xml and index.bak
					CacheFiles[i]=null;   				// Remove (existing caches) or index.xml
				else {
					//ewe.sys.Vm.debug("Orphan: "+wayPoint);
					orphans++;
					h.progress = ((float)(orphans)/(float)CacheFiles.length);
					h.changed();
					if (checkOlder) {
						int cacheIndex=prof.getCacheIndex(wayPoint);
						if (cacheIndex > -1) {
							// In older Versions of CW the <CACHE... /> line was not stored in the cache.xml
							// therefore get it from the index.xml (prof.cacheDB) and put it into the cache.xml
							getCacheDetails(prof.dataDir+CacheFiles[i]);
							if (startOfCacheDetails < 0) {
								CacheHolder ch=prof.cacheDB.get(cacheIndex);
								ch.save();
							}
							// prof.cacheDB.removeElementAt(cacheIndex);
						}
					}
				}
		}
		prof.cacheDB.clear(); //easier than removeElementAt
		pbf.setTask(h,MyLocale.getMsg(209,"Rebuilding index"));
		if (orphans>0) { // At least one cache not in database
			int nProcessed=0;
			// Now do the actual work
			for(i = 0; i<CacheFiles.length; i++){
				if (CacheFiles[i]!=null) {
					h.progress = ((float)nProcessed++)/(float)(orphans);
					h.changed();
					String details=getCacheDetails(prof.dataDir+CacheFiles[i]);
					if (details!=null) {
						CacheHolder ch=new CacheHolder(details, cacheXmlVersion);
						prof.cacheDB.add(ch);
						nAdded++;
						CacheFiles[i]=null;
					} else Global.getPref().log("File "+CacheFiles[i]+" not in index.xml");
				}
				if (pbf.isClosed) break;
			}
			prof.buildReferences();
			prof.saveIndex(Global.getPref(),true);
		}
		pbf.exit(0);
	}

	private String getCacheDetails(String xmlFile) {
		try {
			FileReader in = new FileReader(xmlFile);
			String text= in.readAll();
			in.close();
			int start,end;
			int vstart;
			cacheXmlVersion = 1; // Initial guess
			// Check that we have not accidentally listed another xml file in the directory
			startOfCacheDetails=text.indexOf("<CACHE ");
			if ((start=startOfCacheDetails)<0) return null;
			if (text.indexOf("<CACHEDETAILS>")<0) return null; // startOfCacheDetails must be set in advance
			if ((vstart = text.indexOf("<VERSION value = \"")) >= 0) {
				cacheXmlVersion = Integer.valueOf(text.substring(vstart+18, text.indexOf("\"", vstart+18))).intValue();
			}
			end=text.indexOf("/>",start);
			return text.substring(start,end+2);
		} catch (Exception ex) {
			return null;
		}
	}

	class myProgressBarForm extends ProgressBarForm {
		 boolean isClosed=false;
		 protected boolean canExit(int exitCode) {
			isClosed=true;
			return true;
		 }
	 }


}
