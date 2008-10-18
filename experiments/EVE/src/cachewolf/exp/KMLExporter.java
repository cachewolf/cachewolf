package cachewolf.exp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import eve.sys.Handle;
import eve.sys.Vm;
import eve.ui.ProgressBarForm;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import cachewolf.*;
import cachewolf.utils.Common;
import cachewolf.utils.SafeXML;


/**
*	Class to export the cache database (index) to an KML-File
*	which can be read by Google Earth   
*   
*/
public class KMLExporter extends Exporter {
	private static final String COLOR_FOUND = "ff98fb98"; 
	private static final String COLOR_OWNED = "ffffaa55"; 
	private static final String COLOR_AVAILABLE = "ffffffff";
	private static final String COLOR_NOT_AVAILABLE = "ff0000ff";


	static final int AVAILABLE = 0;
	static final int FOUND = 1;
	static final int OWNED = 2;
	static final int NOT_AVAILABLE = 3;
	static final int UNKNOWN = 4;
	
	
	String []categoryNames = {"Available","Found", "Owned", "Not Available", "UNKNOWN"};
	Hashtable [] outCacheDB = new Hashtable[categoryNames.length];

	public KMLExporter(){
		super();
		this.setMask("*.kml");
		this.setHowManyParams(LAT_LON);
	}

	
	public KMLExporter(Preferences p, Profile prof){
			super();
			this.setMask("*.kml");
	}

	public void doIt(int variant){
		String outFile;
		String str;
		CacheHolder ch;
		CacheHolder addiWpt;
		CacheHolderDetail chD;
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();
		
		if (variant == ASK_FILE) {
			outFile = getOutputFile();
			if (outFile == null) return;
		} else {
			outFile = tmpFileName;
		}

		pbf.showMainTask = false;
		pbf.setTask(h,"Exporting ...");
		pbf.exec();

		int counter = 0;
		int expCount = 0;
		for(int i = 0; i<cacheDB.size();i++){
			ch = (CacheHolder)cacheDB.get(i);
			if(ch.is_black == false && ch.is_filtered == false) counter++;
		}
		copyIcons(new File(outFile).getParent());
		buildOutDB();
		
		try{
			PrintWriter outp =  new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
			str = this.header();
			if (str != null) outp.print(str);
			for (int cat = 0; cat < categoryNames.length; cat++) {
				// skip over empty categories
				if (outCacheDB[cat]==null) continue;

				Enumeration keys = outCacheDB[cat].keys();
				outp.print(startFolder(categoryNames[cat]));

				Vector tmp;
				String key;
				while (keys.hasMoreElements()){
					key=(String)keys.nextElement();
					tmp = (Vector) outCacheDB[cat].get(key);
					// skip over empty cachetypes
					if (tmp.size() == 0) continue;
					outp.print(startFolder(CacheType.transType(Common.parseInt(key))));

					for(int i = 0; i<tmp.size(); i++){
						ch = (CacheHolder) tmp.get(i);
						if (ch.isAddiWpt()) continue;
						expCount++;
						h.progress = (float)expCount/(float)counter;
						h.changed();
						
						chD=new CacheHolderDetail(ch);
						try {
							chD.readCache(profile.dataDir);
						} catch (IOException e) {
							continue;
						}
						if (chD.pos.isValid()){
							str = record(chD, chD.pos.getLatDeg(CWPoint.DD).replace('.', this.decimalSeparator),
								     chD.pos.getLonDeg(CWPoint.DD).replace('.', this.decimalSeparator));
							if (str != null) outp.print(str);
						}
						if (ch.hasAddiWpt()){
						outp.print(startFolder("Additional Waypoints", false));
							for(int j = 0; j<ch.addiWpts.size(); j++){
								addiWpt = (CacheHolder) ch.addiWpts.get(j);
								chD=new CacheHolderDetail(addiWpt);
								expCount++;
								if (chD.pos.isValid()){
									str = record(chD, chD.pos.getLatDeg(CWPoint.DD).replace('.', this.decimalSeparator),
										     chD.pos.getLonDeg(CWPoint.DD).replace('.', this.decimalSeparator));
									if (str != null) outp.print(str);
								}
								
							}
						outp.print(endFolder());// addi wpts
						}
					}
					outp.print(endFolder());// cachetype
				}
				outp.print(endFolder());// category
			}
			
			str = trailer();
			if (str != null) outp.print(str);
			outp.close();
			pbf.exit(0);
		} catch (IOException ioE){
			Vm.debug("Error opening " + outFile);
		}
		//try

	}
	
	private void buildOutDB(){ // TODO untested since EVE
		CacheHolder ch;
		Vector tmp;
		Enumeration categoryLoop;
		String key;
		boolean foundOne;
		
		// create the roots for the different categories
		for (int i = 0; i < categoryNames.length; i++) {
			outCacheDB[i] = new Hashtable();
			// create the roots for the cachetypes
			for (int j = 0; j < CacheType.wayType.length; j++) {
				outCacheDB[i].put(new Integer(CacheType.wayTypeNo[j]), new Vector());
			}
		}

		// fill structure with data from cacheDB
		for(int i = 0; i<cacheDB.size(); i++){
			ch=(CacheHolder)cacheDB.get(i);
			if(ch.is_black == false && ch.is_filtered == false && !ch.isAddiWpt()){
				if (ch.is_found) { tmp = (Vector) outCacheDB[FOUND].get(new Integer(ch.type));} // this "new" is not very efficient as it is only needed because get needs an object, on which it can call hashcode()
				else if (ch.is_owned) { tmp = (Vector) outCacheDB[OWNED].get(new Integer(ch.type));}
				else if (ch.is_archived || !ch.is_available){ tmp = (Vector) outCacheDB[NOT_AVAILABLE].get(new Integer(ch.type));}
				else if (ch.is_available){ tmp = (Vector) outCacheDB[AVAILABLE].get(new Integer(ch.type));}
				else { tmp = (Vector) outCacheDB[UNKNOWN].get(new Integer(ch.type));}
			
				tmp.add(ch);
			}
		}
		
		//eleminate empty categories
		for (int i = 0; i < categoryNames.length; i++) {
			categoryLoop = outCacheDB[i].keys();
			foundOne = false;
			//look if all vectors for cachetypes are filled
			while (categoryLoop.hasMoreElements()){
				key = (String) categoryLoop.nextElement();
				tmp = (Vector)outCacheDB[i].get(key);
				if (tmp.size()> 0){
					foundOne = true;
					break;
				}
			}
			// set hashtable for that category to null
			if (!foundOne)outCacheDB[i] = null;
		}
		

	}
	
	private String startFolder(String name){
		return startFolder(name, true);
	}
	
	private String startFolder(String name, boolean open){
		StringBuffer strBuf = new StringBuffer(200);
		strBuf.append("<Folder>\r\n");
		strBuf.append("<name>" + name + "</name>\r\n");
		strBuf.append("<open>" + (open?"1":"0") + "</open>\r\n");

		return strBuf.toString();
	}

	private String endFolder() {
		
		return "</Folder>\r\n";
	}

	public void copyIcons(String dir){
		try {
			ZipFile zif = new ZipFile (eve.io.File.getProgramDirectory() + "/POIIcons.zip");
			ZipEntry zipEnt;
			int len;
			String entName, fileName; 

			for (int i = 0; i < CacheType.wayType.length; i++) {
				fileName = CacheType.type2pic(CacheType.wayTypeNo[i]);
				entName = "GoogleEarthIcons/" + fileName;
				zipEnt = zif.getEntry(entName);
				if (zipEnt == null) continue;
			    byte[] buff = new byte[ (int) zipEnt.getSize() ];
			    InputStream  fis = zif.getInputStream(zipEnt);
			    FileOutputStream fos = new FileOutputStream( dir + "/" + fileName);
			    while( 0 < (len = fis.read( buff )) )
			      fos.write( buff, 0, len );
			    fos.flush();
			    fos.close();
			    fis.close();
			}

			} catch (ZipException e) {
				Vm.debug("Problem copying Icon");
				e.printStackTrace();
			} catch (IOException e) {
				Vm.debug("Problem copying Icon");
				e.printStackTrace();
			}
	}

	
	public String header () {
		StringBuffer strBuf = new StringBuffer(200);
				
		strBuf.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n");
		strBuf.append("<kml xmlns=\"http://earth.google.com/kml/2.0\">\r\n");
		strBuf.append("<Folder>\r\n");
		strBuf.append("<name>CacheWolf</name>\r\n");
		strBuf.append("<open>1</open>\r\n");
	
		return strBuf.toString();
	}


	public String record(CacheHolderDetail ch, String lat, String lon){
		StringBuffer strBuf = new StringBuffer(200);
		
		strBuf.append("   <Placemark>\r\n");
		if (ch.URL != null){
			strBuf.append("      <description>"+SafeXML.clean(ch.URL)+"</description>\r\n");
		}
		strBuf.append("      <name>"+ ch.wayPoint + " - " + SafeXML.clean(ch.cacheName) +"</name>\r\n");
		strBuf.append("      <LookAt>\r\n");
		strBuf.append("         <latitude>" + lat + "</latitude>\r\n");
		strBuf.append("         <longitude>" + lon + "</longitude>\r\n");
		strBuf.append("         <range>10000</range><tilt>0</tilt><heading>0</heading>\r\n");
		strBuf.append("      </LookAt>\r\n");
		strBuf.append("      <Point>\r\n");
		strBuf.append("         <coordinates>"  + lon + "," + lat + "</coordinates>\r\n");
		strBuf.append("      </Point>\r\n");
		strBuf.append("      <Style>\r\n");
		strBuf.append("      <IconStyle>\r\n");
		strBuf.append("         <Icon>\r\n");
//		strBuf.append("            <href>"+ File.getProgramDirectory()+ "/" + CacheType.type2pic(Convert.parseInt(ch.type))+ "</href>\r\n");
		strBuf.append("            <href>"+ CacheType.type2pic(ch.type)+ "</href>\r\n");
		strBuf.append("         </Icon>\r\n");
		strBuf.append("      </IconStyle>\r\n");
		strBuf.append("      <LabelStyle>\r\n");
		strBuf.append("         <color>" + getColor(ch) + "</color>\r\n");
		strBuf.append("      </LabelStyle>\r\n");
		strBuf.append("      </Style>\r\n");
		strBuf.append("   </Placemark>\r\n");
	
		return strBuf.toString();
	}
	
	public String trailer(){
		StringBuffer strBuf = new StringBuffer(50);

		strBuf.append("</Folder>\r\n");
		strBuf.append("</kml>\r\n");

		return strBuf.toString();
	}
	
	private String getColor(CacheHolderDetail ch){
		if (ch.is_found) return COLOR_FOUND;
		if (ch.is_owned) return COLOR_OWNED;
		if (ch.is_archived || !ch.is_available) return COLOR_NOT_AVAILABLE;
		
		return COLOR_AVAILABLE;
	}
	
}
