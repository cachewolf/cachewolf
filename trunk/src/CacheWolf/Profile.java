package CacheWolf;

import ewe.io.BufferedWriter;
import ewe.io.FileNotFoundException;
import ewe.io.FileReader;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.PrintWriter;
import ewe.sys.Convert;
import ewe.sys.Vm;
import ewe.util.Vector;

/**
 * This class holds a profile, i.e. a group of caches with a centre location
 * 
 * @author salzkammergut
 *
 */
public class Profile {
	
	/** The list of caches (CacheHolder objects). A pointer to this object exists in many classes in parallel to
	 *  this object, i.e. the respective class contains both a {@link Profile} object and a cacheDB Vector. 
	 */
	public Vector cacheDB=new Vector();
	/** The centre point of this group of caches. Read from ans stored to index.xml file */
	public CWPoint centre=new CWPoint();
	/** The name of the profile. The baseDir in preferences is appended this name to give the dataDir where
	 *  the index.xml and cache files live. (Excuse the English spelling of centre)     */
	public String name=new String();
	/** This is the directory for the profile. It contains a closing /.   	 */
	public String dataDir=new String();  
	/** Last sync date for opencaching caches */
	public String last_sync_opencaching = new String();
	/** Distance for opencaching caches */
	public String distOC = new String();
	
	public String filterType = new String("11111111110");
	public String filterRose = new String("1111111111111111");
	//filter settings for archived ... owner (section) in filterscreen
	public String filterVar = new String("0000");
	//TODO Add the current filter settings here so that they are restored when the profile is reloaded
	//TODO Add other settings, such as max. number of logs to spider
	//TODO Add settings for the preferred mapper to allow for maps other than expedia and other resolutions
	
	/**
	 * Constructor for a profile
	 *
	 */
	public Profile(){
	}
	
	public void clearProfile() {
		cacheDB.clear();
		centre.set(0,0);
		name="";
		dataDir="";  
		last_sync_opencaching = "";
		distOC = "";
	}
	
	
	/**
	*	Method to save the index.xml file that holds the total information
	*	on available caches in the database. The database is nothing else
	*	than the collection of caches in a directory.
	*   
	*   Not sure whether we need to keep 'pref' in method signature. May eventually remove it. 
	*/
	public void saveIndex(Preferences pref){
		PrintWriter detfile;
		CacheHolder ch;
		try{
		  detfile = new PrintWriter(new BufferedWriter(new FileWriter(dataDir + "index.xml")));
		} catch (Exception e) {
		  Vm.debug("Problem creating index file "+e.toString()+"\nFilename="+dataDir + "index.xml");
			return;
		}
		CWPoint savedCentre=centre;
		if (centre==null || !centre.isValid() || (savedCentre.latDec==0.0 && savedCentre.lonDec==0.0)) savedCentre=pref.curCentrePt;
		
		try{
			detfile.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			detfile.print("<CACHELIST format=\"decimal\">\n");
			if (savedCentre.isValid())
//				detfile.print("    <CENTRE lat=\""+savedCentre.getNSLetter() + " " + savedCentre.getLatDeg(CWPoint.CW) + "&deg; " + savedCentre.getLatMin(CWPoint.CW)+ "\" "+
//				                        "long=\""+savedCentre.getEWLetter() + " " + savedCentre.getLonDeg(CWPoint.CW) + "&deg; " + savedCentre.getLonMin(CWPoint.CW)+"\"/>\n");
				detfile.print("    <CENTRE lat=\""+savedCentre.latDec+"\" lon=\""+savedCentre.lonDec+"\"/>\n");
			if(last_sync_opencaching == null || last_sync_opencaching.endsWith("null") || last_sync_opencaching.equals("")){
				last_sync_opencaching = "20050801000000";
			}
			if(distOC == null || distOC.endsWith("null") || distOC.equals("")){
				distOC = "0";
			}
			
			detfile.print("    <FILTER rose = \""+filterRose+"\" type = \""+filterType+"\"/>\n");
			detfile.print("    <SYNCOC date = \""+last_sync_opencaching+"\" dist = \""+distOC+"\"/>\n");
			for(int i = 0; i<cacheDB.size();i++){
				ch = (CacheHolder)cacheDB.get(i);
				////Vm.debug("Saving: " + ch.CacheName);
				if(ch.wayPoint.length()>0 && ch.LongDescription.equals("An Error Has Occured") == false){
					if (ch.pos==null) {
						ParseLatLon pl=new ParseLatLon(ch.LatLon);
						pl.parse();
						ch.pos=new CWPoint(pl.lat2,pl.lon2);
					}
					detfile.print("    <CACHE name = \""+SafeXML.clean(ch.CacheName)+"\" owner = \""+SafeXML.clean(ch.CacheOwner)+
							//"\" lat = \""+ SafeXML.clean(ch.LatLon) +
							"\" lat = \""+ ch.pos.latDec + "\" lon = \""+ch.pos.lonDec+
							"\" hidden = \""+ch.DateHidden+"\" wayp = \""+SafeXML.clean(ch.wayPoint)+"\" status = \""+ch.CacheStatus+"\" type = \""+ch.type+"\" dif = \""+ch.hard+"\" terrain = \"" + ch.terrain + "\" dirty = \"" + ch.dirty + "\" size = \""+ch.CacheSize+"\" online = \"" + Convert.toString(ch.is_available) + "\" archived = \"" + Convert.toString(ch.is_archived) + "\" has_bug = \"" + Convert.toString(ch.has_bug) + "\" black = \"" + Convert.toString(ch.is_black) + "\" owned = \"" + Convert.toString(ch.is_owned) + "\" found = \"" + Convert.toString(ch.is_found) + "\" is_new = \"" + Convert.toString(ch.is_new) +"\" is_log_update = \"" + Convert.toString(ch.is_log_update) + "\" is_update = \"" + Convert.toString(ch.is_update) + "\" is_HTML = \"" + Convert.toString(ch.is_HTML) + "\" DNFLOGS = \"" + ch.noFindLogs + "\" ocCacheID = \"" + ch.ocCacheID + "\" />\n");
				}
			}
			detfile.print("</CACHELIST>\n");
			detfile.close();
			CacheHolder.buildReferences(cacheDB);
		}catch(Exception e){
			Vm.debug("Problem writing to index file "+e.toString());
		}
	}
	
	
	
	/**
	*	Method to read the index.xml file that holds the total information
	*	on available caches in the database. The database in nothing else
	*	than the collection of caches in a directory.
	*/
	public void readIndex(){
		try {
			boolean fmtDec=false;
			char decSep=MyLocale.getDigSeparator().charAt(0);
			char notDecSep=decSep=='.'?',':'.';
			FileReader in = new FileReader(dataDir + "index.xml");
			in.readLine(); // <?xml version= ...
			String text=in.readLine(); // <CACHELIST>
			if (text!=null && text.indexOf("decimal")>0) fmtDec=true;
			Extractor ex = new Extractor(null, " = \"", "\" ", 0, true);
			while ((text = in.readLine()) != null){
				// Check for Line with cache data
				if (text.indexOf("<CACHE ")>=0){
					ex.setSource(text);
					CacheHolder ch = new CacheHolder();
					ch.CacheName = SafeXML.cleanback(ex.findNext());
					ch.CacheOwner = SafeXML.cleanback(ex.findNext());
					if (fmtDec) {
						double lat=Convert.parseDouble(ex.findNext().replace(notDecSep,decSep));
						double lon=Convert.parseDouble(ex.findNext().replace(notDecSep,decSep));
						ch.pos=new CWPoint(lat,lon);
						ch.LatLon=ch.pos.toString();
					} else
						ch.LatLon = SafeXML.cleanback(ex.findNext());
					ch.DateHidden = ex.findNext();
					ch.wayPoint = SafeXML.cleanback(ex.findNext());
					ch.CacheStatus = ex.findNext();
					ch.type = ex.findNext();
					ch.hard = ex.findNext();
					ch.terrain = ex.findNext();
					ch.dirty = ex.findNext();
					ch.CacheSize = ex.findNext();
					ch.is_available = ex.findNext().equals("true") ? true : false;
					ch.is_archived = ex.findNext().equals("true") ? true : false;
					ch.has_bug = ex.findNext().equals("true") ? true : false;
					ch.is_black = ex.findNext().equals("true") ? true : false;
					ch.is_owned = ex.findNext().equals("true") ? true : false;
					ch.is_found = ex.findNext().equals("true") ? true : false;
					ch.is_new = ex.findNext().equals("true") ? true : false;
					ch.is_log_update = ex.findNext().equals("true") ? true : false;
					ch.is_update = ex.findNext().equals("true") ? true : false;
					  // for backwards compatibility set value to true, if it is not in the file
					ch.is_HTML = ex.findNext().equals("false") ? false : true;
					ch.noFindLogs = Convert.toInt(ex.findNext());
					ch.ocCacheID = ex.findNext();
					// remove "/>
					ch.ocCacheID = STRreplace.replace(ch.ocCacheID,"\"/>", null);
					// remove additional " if present
					ch.ocCacheID = STRreplace.replace(ch.ocCacheID,"\"", null);
					cacheDB.add(ch);
				} else if (text.indexOf("<CENTRE")>=0) { // lat=  lon=
					if (fmtDec) {
						int start=text.indexOf("lat=\"")+5;
						String lat=text.substring(start,text.indexOf("\"",start)).replace(notDecSep,decSep);
						start=text.indexOf("lon=\"")+5;
						String lon=text.substring(start,text.indexOf("\"",start)).replace(notDecSep,decSep);
						centre.set(Convert.parseDouble(lat),Convert.parseDouble(lon));
					} else {	
						int start=text.indexOf("lat=\"")+5;
						String lat=SafeXML.cleanback(text.substring(start,text.indexOf("\"",start)));
						start=text.indexOf("long=\"")+6;
						String lon=SafeXML.cleanback(text.substring(start,text.indexOf("\"",start)));
						centre.set(lat+" "+lon,CWPoint.CW); // Fast parse
					}	
				} else if (text.indexOf("<SYNCOC")>=0) {
					int start=text.indexOf("date = \"")+8;
					last_sync_opencaching=text.substring(start,text.indexOf("\"",start));
					start=text.indexOf("dist = \"")+8;
					distOC=text.substring(start,text.indexOf("\"",start));
				} else if (text.indexOf("<FILTER")>=0){
					ex.setSource(text);
					filterRose = ex.findNext();
					filterType = ex.findNext();
//					 Bilbowolf: Pattern for storing filter <FILTER type="01001101" rose = "010010101" var = "0101" dist = "<12" diff = ">13" terr = "<1"/>
				}
			}
			in.close();
			// Build references between caches and addi wpts
			CacheHolder.buildReferences(cacheDB);
			
		} catch (FileNotFoundException e) {
			Vm.debug("index.xml not found"); // Normal when profile is opened for first time
			//e.printStackTrace();
		} catch (IOException e){
			Vm.debug("Problem reading index.xml "+e.toString()); 
			e.printStackTrace();
		}
	}
	
	public int getCacheIndex(String wp){
		int retval = -1;
		CacheHolder ch;
		for(int i = 0; i<cacheDB.size();i++){
			ch = (CacheHolder)cacheDB.get(i);
			if(ch.wayPoint.equals(wp)){
				return i;
			}
		}
		return retval;
	}

	public String toString() {
		return "Profile: Name="+name+"\nCentre="+centre.toString()+"\ndataDir="+dataDir+"\nlastSyncOC="+
		     last_sync_opencaching+"\ndistOC="+distOC;
	}
}
