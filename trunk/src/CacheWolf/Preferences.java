package CacheWolf;

import ewe.io.*;
import ewe.sys.*;
import ewe.ui.*;
import ewesoft.xml.*;
import ewesoft.xml.sax.*;

/**
*	A class to hold the preferences that were loaded upon start up of CacheWolf.
*	This class is also capable of parsing the prefs.xml file as well as
*	saving the current settings of preferences.
*/
public class Preferences extends MinML{
	
	public int tablePrefs[] = {1,1,1,1,1,1,1,1,1,1,1};
	public int tableWidth[] = {20,20,20,65,135,135,100,60,50,50,50};
	     
	//Longitude
	public String mylgNS = new String();
	public String mylgDeg = new String();
	public String mylgMin = new String();
	//Latitude
	public String mybrWE = new String();
	public String mybrDeg = new String(); 
	public String mybrMin = new String();

	public String mydatadir = new String();  
	public String myproxy = new String();    
	public String myproxyport = new String();
	
	public String myAlias = new String();
	public String browser = new String();
		
	public int myAppHeight = 0;
	public int myAppWidth = 0;
	//public int nLogs = 5;
	public boolean dirty = false;
	
	public int currProfile = 0;
	public String profiles[] = new String[4];
	public String profdirs[] = new String[4];
	public String lats[] = new String[4];
	public String longs[] = new String[4];
	public String lastSyncOC[] = new String[4];
	public String lastDistOC[] = new String[4];
		
	public String last_sync_opencaching = new String();
	public String distOC = new String();
	
	public String digSeparator = new String();
	public boolean debug = false;
	public SerialPortOptions mySPO = new SerialPortOptions();
	
	public Preferences(){
		double testA = Convert.toDouble("1,50") + Convert.toDouble("3,00");
		if(testA == 4.5) digSeparator = ","; else digSeparator = ".";
		//Vm.debug("Separ: " + digSeparator);
		mySPO.bits = 8;
		mySPO.parity = SerialPort.NOPARITY;
		mySPO.stopBits = 1;
	}
	
	/**
	* Returns true if coordinates have been set.
	* Does not validate! if coordinates are real.
	*/
	public boolean existCenter(){
		boolean test = false;
		String t1 = new String();
		String t2 = new String();
		t1 = mylgDeg+mylgMin;
		t2 = mybrDeg+mybrMin;
		if(t1.length() > 0 && t2.length() > 0) test = true;
		return test;
	}
	
	/**
	* Method to open and parse the pref.xml file. Results are stored in the
	* public variables of this class.
	* mode == 0 --> do not display profile selector
	* mode == 1 --> display profile selector
	*/
	public void doIt(int mode){
		try{
			String datei = File.getProgramDirectory() + "/" + "pref.xml";
			datei = datei.replace('\\', '/');
			ewe.io.Reader r = new ewe.io.InputStreamReader(new ewe.io.FileInputStream(datei));
			parse(r);
			r.close();
			//Check if there are "profiles" entries. If yes display a form
			//so the user may choose a profile.
			if(mode == 1){
				if(profiles[0].length()>0 ||
				   profiles[1].length()>0 ||
				   profiles[2].length()>0 ||
				   profiles[3].length()>0){
					   Vm.showWait(false);
					   Form f = new ProfilesForm(profiles);
					   int code = f.execute();
					   currProfile = code;
					   Vm.showWait(true);
					   if(code > 0){
						   if(profiles[code-1].length()>0){
							mydatadir = profdirs[code-1];
							if(lastSyncOC[code-1] == null || lastSyncOC[code-1].endsWith("null")){
								last_sync_opencaching = "20050801000000";
							}
							else {
								last_sync_opencaching = lastSyncOC[code-1];
							}
							if(lastDistOC[code-1] == null || lastDistOC[code-1].endsWith("null")){
								distOC = "0";
							}
							else {
								distOC = lastDistOC[code-1];
							}
							Extractor ex = new Extractor(" " + longs[code-1], " ", " ", 0,true);
							mybrWE = ex.findNext();
							mybrDeg = ex.findNext();
							mybrMin = ex.findNext();
							ex = new Extractor(" " + lats[code-1], " ", " ", 0,true);
							mylgNS = ex.findNext();
							mylgDeg = ex.findNext();
							mylgMin = ex.findNext();
						   }
					   }
					   if(mydatadir.indexOf('.') > 0){
						String cwd = File.getProgramDirectory();
						mydatadir = cwd + "/" + mydatadir.substring(1, mydatadir.length()-2);
						//Vm.debug("Datadir? " + mydatadir);
					   }
				   }
			}
		}catch(Exception e){
			//Vm.debug(e.toString());
		}
	}
	
	/**
	* Method that gets called when a new element has been identified in pref.xml
	*/
	public void startElement(String name, AttributeList atts){
		String tmp;
		if(name.equals("browser")) browser = atts.getValue("name");
		if(name.equals("syncOC")) {
			if (atts.getValue("date") == null || atts.getValue("date").endsWith("null")){
				last_sync_opencaching = "20050801000000";
			}
			else {
				last_sync_opencaching = atts.getValue("date");
			}
			if (atts.getValue("dist") == null || atts.getValue("dist").endsWith("null")){
				distOC = "0";
			}
			else {
				distOC =  atts.getValue("dist");
			}
		}
		if(name.equals("alias")) myAlias = atts.getValue("name");
		if(name.equals("location")){
			Extractor ex = new Extractor(" " + atts.getValue("long"), " ", " ", 0,true);
			mybrWE = ex.findNext();
			mybrDeg = ex.findNext();
			mybrMin = ex.findNext();
			ex = new Extractor(" " + atts.getValue("lat"), " ", " ", 0,true);
			mylgNS = ex.findNext();
			mylgDeg = ex.findNext();
			mylgMin = ex.findNext();
		}
		if(name.equals("port")){
			mySPO.portName = atts.getValue("portname");
			mySPO.baudRate = Convert.toInt(atts.getValue("baud"));
		}
		//if(name.equals("logs")){
		//	nLogs = Convert.parseInt(atts.getValue("number"));
		//}
		if(name.equals("profile1")){
			profiles[0] = atts.getValue("name");
			profdirs[0] = atts.getValue("dir");
			lats[0] = atts.getValue("lat");
			longs[0] = atts.getValue("lon");
			lastSyncOC[0] = atts.getValue("lastsyncoc");
			lastDistOC[0] = atts.getValue("lastdistoc");

		}
		if(name.equals("profile2")){
			profiles[1] = atts.getValue("name");
			profdirs[1] = atts.getValue("dir");
			lats[1] = atts.getValue("lat");
			longs[1] = atts.getValue("lon");
			lastSyncOC[1] = atts.getValue("lastsyncoc");
			lastDistOC[1] = atts.getValue("lastdistoc");


		}
		if(name.equals("profile3")){
			profiles[2] = atts.getValue("name");
			profdirs[2] = atts.getValue("dir");
			lats[2] = atts.getValue("lat");
			longs[2] = atts.getValue("lon");
			lastSyncOC[2] = atts.getValue("lastsyncoc");
			lastDistOC[2] = atts.getValue("lastdistoc");


		}
		if(name.equals("profile4")){
			profiles[3] = atts.getValue("name");
			profdirs[3] = atts.getValue("dir");
			lats[3] = atts.getValue("lat");
			longs[3] = atts.getValue("lon");
			lastSyncOC[3] = atts.getValue("lastsyncoc");
			lastDistOC[3] = atts.getValue("lastdistoc");


		}
		if(name.equals("datadir")) {
			mydatadir = atts.getValue("dir");
		}
		if(name.equals("proxy")) {
			myproxy = atts.getValue("prx");
			myproxyport = atts.getValue("prt");
		}
		if(name.equals("tableType")){ 
			tablePrefs[0] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[0] = Convert.parseInt(tmp);
		}
		if(name.equals("tableD")){
			tablePrefs[1] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[1] = Convert.parseInt(tmp);
		}
		if(name.equals("tableT")){
			tablePrefs[2] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[2] = Convert.parseInt(tmp);
		}
		if(name.equals("tableWay")) {
			tablePrefs[3] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[3] = Convert.parseInt(tmp);
		}
		if(name.equals("tableName")){
			tablePrefs[4] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[4] = Convert.parseInt(tmp);
		}
		if(name.equals("tableLoc")){
			tablePrefs[5] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[5] = Convert.parseInt(tmp);
		}
		if(name.equals("tableOwn")){
			tablePrefs[6] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[6] = Convert.parseInt(tmp);
		}
		if(name.equals("tableHide")){
			tablePrefs[7] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[7] = Convert.parseInt(tmp);
		}
		if(name.equals("tableStat")){
			tablePrefs[8] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[8] = Convert.parseInt(tmp);
		}
		if(name.equals("tableDist")){
			tablePrefs[9] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[9] = Convert.parseInt(tmp);
		}
		if(name.equals("tableBear")){
			tablePrefs[10] = Convert.parseInt(atts.getValue("active"));
			tmp = atts.getValue("width");
			if (tmp != null) tableWidth[10] = Convert.parseInt(tmp);
		}
	}
	
	/**
	* Method to save current preferences in the pref.xml file
	*/
	public void savePreferences(){
		String lat = new String();
		String lon = new String();
		lat = mylgNS+" "+ mylgDeg+ " " + mylgMin;
		lon = mybrWE+" "+ mybrDeg + " " + mybrMin;
		lat = STRreplace.replace(lat, ",", ".");
		lon = STRreplace.replace(lon, ",", ".");
		String datei = File.getProgramDirectory() + "/" + "pref.xml";
		datei = datei.replace('\\', '/');
		last_sync_opencaching = last_sync_opencaching==null?"20050801000000":last_sync_opencaching;
		distOC = distOC==null?"0":distOC;
		if (currProfile > 0) {
			lastSyncOC[currProfile -1] = last_sync_opencaching;
			lastDistOC[currProfile - 1] = distOC;
		}

		try{
			PrintWriter outp =  new PrintWriter(new BufferedWriter(new FileWriter(datei)));
			outp.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			outp.print("<preferences>\n");
			outp.print("	<alias name =\""+ myAlias +"\"/>\n");
			outp.print("	<location lat = \""+lat+"\" long = \""+lon+"\"/>\n");
			outp.print("	<datadir dir = \""+ mydatadir +"\"/>\n");
			outp.print("	<proxy prx = \""+ myproxy+"\" prt = \""+ myproxyport + "\"/>\n");
			outp.print("	<port portname = \""+ mySPO.portName +"\" baud = \""+ mySPO.baudRate+"\"/>\n");
			outp.print("	<tableType active = \"1\" width = \""+Convert.toString(tableWidth[0])+"\"/>\n");
			//outp.print("    <logs number = \""+Convert.toString(nLogs)+"\"/>\n");
			outp.print("	<tableD active = \""+Convert.toString(tablePrefs[1])+ "\"" +
					               " width = \""+Convert.toString(tableWidth[1])+"\"/>\n");
			outp.print("	<tableT active = \""+Convert.toString(tablePrefs[2])+ "\"" +
								   " width = \""+Convert.toString(tableWidth[2])+"\"/>\n");
			outp.print("	<tableWay active = \"1\" width = \""+Convert.toString(tableWidth[3])+"\"/>\n");
			outp.print("	<tableName active = \"1\" width = \""+Convert.toString(tableWidth[4])+"\"/>\n");
			outp.print("	<tableLoc active = \""+Convert.toString(tablePrefs[5])+ "\"" +
					   				 " width = \""+Convert.toString(tableWidth[5])+"\"/>\n");
			outp.print("	<tableOwn active = \""+Convert.toString(tablePrefs[6])+ "\"" +
					   				 " width = \""+Convert.toString(tableWidth[6])+"\"/>\n");
			outp.print("	<tableHide active = \""+Convert.toString(tablePrefs[7])+ "\"" +
					   				  " width = \""+Convert.toString(tableWidth[7])+"\"/>\n");
			outp.print("	<tableStat active = \""+Convert.toString(tablePrefs[8])+ "\"" +
					   				  " width = \""+Convert.toString(tableWidth[8])+"\"/>\n");
			outp.print("	<tableDist active = \""+Convert.toString(tablePrefs[9])+ "\"" +
					   				  " width = \""+Convert.toString(tableWidth[9])+"\"/>\n");
			outp.print("	<tableBear active = \""+Convert.toString(tablePrefs[10])+ "\"" +
					   				  " width = \""+Convert.toString(tableWidth[10])+"\"/>\n");
			outp.print("	<profile1 name = \""+profiles[0]+"\" lat = \""+ lats[0] +"\" lon = \""+ longs[0] +"\" dir = \""+ profdirs[0] +"\" lastsyncoc= \"" + lastSyncOC[0] + "\" lastdistoc= \"" + lastDistOC[0] + "\" />\n");
			outp.print("	<profile2 name = \""+profiles[1]+"\" lat = \""+ lats[1] +"\" lon = \""+ longs[1] +"\" dir = \""+ profdirs[1] +"\" lastsyncoc= \"" + lastSyncOC[1] + "\" lastdistoc= \"" + lastDistOC[1] + "\" />\n");
			outp.print("	<profile3 name = \""+profiles[2]+"\" lat = \""+ lats[2] +"\" lon = \""+ longs[2] +"\" dir = \""+ profdirs[2] +"\" lastsyncoc= \"" + lastSyncOC[2] + "\" lastdistoc= \"" + lastDistOC[2] + "\" />\n");
			outp.print("	<profile4 name = \""+profiles[3]+"\" lat = \""+ lats[3] +"\" lon = \""+ longs[3] +"\" dir = \""+ profdirs[3] +"\" lastsyncoc= \"" + lastSyncOC[3] + "\" lastdistoc= \"" + lastDistOC[3] + "\" />\n");
			outp.print("<browser name = \""+browser+"\"/>\n");
			outp.print("<syncOC date = \"" + last_sync_opencaching + "\" dist = \"" + distOC +  "\"/>\n");
			outp.print("</preferences>");
			outp.close();
		} catch (Exception e) {
			Vm.debug("Problem saving: " +datei);
    		}
	}
}
