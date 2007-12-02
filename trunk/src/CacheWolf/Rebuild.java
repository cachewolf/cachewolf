package CacheWolf;

import ewe.io.FileReader;
import ewe.sys.Handle;
import ewe.ui.MessageBox;
import ewe.ui.ProgressBarForm;
import utils.FileBugfix;

public class Rebuild {
	String [] xmlFiles;
	
	public Rebuild() {}
	
	public void rebuild() {	
		int i;
		Profile prof=Global.getProfile();
		
		myProgressBarForm pbf = new myProgressBarForm();
		Handle h = new Handle();
		pbf.setTask(h,MyLocale.getMsg(209,"Rebuilding index"));
		pbf.exec();

		FileBugfix file=new FileBugfix(Global.getProfile().dataDir);
		xmlFiles=file.list("*.xml",0);
		int orphans=0; // xml Files without entry in database
		int nAdded=0;  // caches added to database
		for (i=0; i<xmlFiles.length; i++) {
			int pos=xmlFiles[i].indexOf('.');
			if (pos<0) continue;
			String wayPoint=xmlFiles[i].substring(0,pos);
			if (wayPoint.charAt(0)=='i' || 			// Check for index.xml and index.bak
				prof.getCacheIndex(wayPoint)>=0)		// Check for waypoints already in database 
				xmlFiles[i]=null;   				// Remove existing caches or index.xml
			else {
				//ewe.sys.Vm.debug("Orphan: "+wayPoint);
				orphans++;
			}
		}
		if (orphans>0) { // At least one cache not in database
			int nProcessed=0;
			// Now do the actual work
			for(i = 0; i<xmlFiles.length; i++){
				if (xmlFiles[i]!=null) {
					h.progress = ((float)nProcessed++)/(float)(orphans);
					h.changed();
					String details=getCacheDetails(prof.dataDir+xmlFiles[i]);
					if (details!=null) { // In older Versions of CW the <CACHE... /> line was not stored in the cache.xml
						CacheHolder ch=new CacheHolder(details);
						prof.cacheDB.add(ch);
						nAdded++;
						xmlFiles[i]=null;
					} else Global.getPref().log("File "+xmlFiles[i]+" not in index.xml");
				}
				if (pbf.isClosed) break;
			}
			(new MessageBox(MyLocale.getMsg(327, "Information"), 
					  MyLocale.getMsg(210,"Caches nicht in index.xml: ")+orphans+
					  MyLocale.getMsg(211,"\nDavon hinzugef�gt: ")+nAdded
					, MessageBox.OKB)).execute();
			prof.buildReferences();
			prof.saveIndex(Global.getPref(),true);
		}
		if (orphans!=nAdded && (new MessageBox(MyLocale.getMsg(327, "Information"),
					MyLocale.getMsg(212,"Delete all .xml files not in index.xml and associated pictures"), 
					MessageBox.YESB | MessageBox.NOB)).execute()==MessageBox.YESB) {
			h = new Handle();
			pbf.setTask(h,MyLocale.getMsg(213,"Deleting orphans"));
			DataMover dm=new DataMover();
			int nDeleted=0;
			for (i=0; i<xmlFiles.length; i++) {
				if (xmlFiles[i]!=null){	
					h.progress = ((float)nDeleted++)/(float)(orphans-nAdded);
					h.changed();
					String wayPoint=xmlFiles[i].substring(0,xmlFiles[i].indexOf('.'));
					dm.deleteCacheFiles(wayPoint,prof.dataDir);
				}
			}
		}
		pbf.exit(0);
	}

	private String getCacheDetails(String xmlFile) {
		try {
			FileReader in = new FileReader(xmlFile);
			String text= in.readAll();
			in.close();
			int start,end;
			// Check that we have not accidentally listed another xml file in the directory
			if (text.indexOf("<CACHEDETAILS>")<0 || (start=text.indexOf("<CACHE "))<0) return null;
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
