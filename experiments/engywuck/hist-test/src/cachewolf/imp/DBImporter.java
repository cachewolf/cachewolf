package cachewolf.imp;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cachewolf.CWPoint;
import cachewolf.CacheHolder;
import cachewolf.CacheHolderDetail;
import cachewolf.CacheType;
import cachewolf.Filter;
import cachewolf.InfoBox;
import cachewolf.MyLocale;
import cachewolf.Preferences;
import cachewolf.Profile;
import cachewolf.utils.Common;
import cachewolf.utils.SafeXML;
import eve.sys.Handle;
import eve.ui.Form;
import eve.ui.ProgressBarForm;


public class DBImporter {
	int i,j,ln;
	Vector cacheDB;
	int zaehlerGel = 0;
	Hashtable DBindex = new Hashtable();
	
	public DBImporter(){
		cacheDB = cachewolf.Global.getProfile().cacheDB;
		//index db for faster search
		CacheHolder ch;
		for(i = cacheDB.size()-1;i>=0; i--){
			ch = (CacheHolder)cacheDB.get(i);
			DBindex.put(ch.wayPoint, new Integer(i));
		}//for
	}

	public void doIt(String file){
		Filter flt = new Filter();
		boolean wasFiltered = (cachewolf.Global.getProfile().filterActive==Filter.FILTER_ACTIVE);		
		flt.clearFilter();
		try {
			java.io.BufferedReader r;
			
			if (file.indexOf(".zip") > 0){
				ZipFile zif = new ZipFile (file);
				ZipEntry zipEnt;
				Enumeration zipEnum = zif.entries();
				// there could be more than one file in the archive
				while (zipEnum.hasMoreElements()) {	
					zipEnt = (ZipEntry) zipEnum.nextElement();
					if (zipEnt.getName().endsWith("db")){
						r = new java.io.BufferedReader(new java.io.InputStreamReader(zif.getInputStream(zipEnt)));
						
						parse(r,zipEnt.getSize());
						r.close();
					}
				}
			} else { // Already unpacked
				r = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(file)));
				parse(r,new java.io.File(file).length());
				r.close();
			}
			// save Index 
			cachewolf.Global.getProfile().saveIndex(Profile.NO_SHOW_PROGRESS_BAR);
			Form.cancelWait();
		}catch(Exception e){
			e.printStackTrace();
			Form.cancelWait();
		}
		if(wasFiltered){
			flt.setFilter();
			flt.doFilter();
		}
	}

	private final void parse(java.io.BufferedReader r,long len) {
// One typical line in DB file
//734139 "CQ DE HB0 - HAM-Cache in Liechtenstein by DunaX" 2007 11 12 47.1388 9.53848333333333 
//"Multi-cache" "Regular" GC17DPV 7a5585fd-af71-4ab7-ab42-27da2e461ef2 1.5 1.5
		final int LINESIZE=150;
		float size=len/LINESIZE;
		boolean showprogress=true; // Keep this so that we can later decide whether to make it switchable
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();
		if(showprogress){
			pbf.showMainTask = false;
			pbf.setTask(h,"Importing DB");
			pbf.exec();
		}
		// Each line is about 150 chars
		int progressInt=(int) len/LINESIZE/100;
		int nextProgress=0;
		int idx;
		String line;
		ln=0; // Line No
		try {
			while((line=r.readLine())!=null) {
				ln++;
				CacheHolder ch=new CacheHolder();
				// "cachename by owner"
				i=line.indexOf("\"");
				j=line.indexOf("\"",i+1);
				String name=line.substring(i+1,j);
				i=name.indexOf(" by ");
				if (i>0) {
					ch.cacheName=SafeXML.cleanback(name.substring(0,i));
					ch.cacheOwner=SafeXML.cleanback(name.substring(i+4));
				} else {
					ch.cacheName=SafeXML.cleanback(name);
				}
				// date hidden: Y M D
				StringBuffer sb=new StringBuffer(12);
				sb.append(line.substring(j+2,j+6)).append("-");
				i=line.indexOf(" ",j+7);
				if (j+8==i) sb.append("0");
				sb.append(line.substring(j+7,i)).append("-");
				j=line.indexOf(" ",i+1);
				if (i+2==j) sb.append("0");
				sb.append(line.substring(i+1,j));
				ch.dateHidden=sb.toString();
				// lat lon
				i=line.indexOf(" ",j+1);
				double lat=Common.parseDouble(line.substring(j+1,i));
				j=line.indexOf(" ",i+1);
				ch.pos=new CWPoint(lat,Common.parseDouble(line.substring(i+1,j)));
				ch.latLon=ch.pos.toString();
				// Type
				i=line.indexOf("\"",i+1);
				j=line.indexOf("\"",i+1);
				ch.type=CacheType.typeText2Number(line.substring(i+1,j));
				// Size
				i=line.indexOf("\"",j+1);
				j=line.indexOf("\"",i+1);
				ch.setCacheSize(line.substring(i+1,j));
				//waypoint
				i=line.indexOf(" ",j+1);
				j=line.indexOf(" ",i+1);
				ch.wayPoint=line.substring(i+1,j);
				// skip ID
				i=line.indexOf(" ",j+1);
				// diff terrain
				j=line.indexOf(" ",i+1);
				ch.hard=line.substring(i+1,j);
				ch.terrain=line.substring(j+1);
				idx=searchWpt(ch.wayPoint);
				if (idx<0)
					cacheDB.add(ch);
				else
					((CacheHolder)cacheDB.get(idx)).update(ch);
				ch=null;
				if(showprogress && ln>=nextProgress){
					h.progress = (float)ln/size;
					h.changed();
					nextProgress+=progressInt;
				}
			}
		} catch(Exception ex) {}
		if(showprogress) pbf.exit(0);
	}
	
	private int searchWpt(String wpt){
		Integer INTR = (Integer)DBindex.get(wpt);
		if(INTR != null){
			return INTR.intValue();
		} 
		return -1;
	}
}
