package CacheWolf.exp;
import CacheWolf.*;
import CacheWolf.navi.TransformCoordinates;
import ewe.sys.*;

/**
*	Class to export the cache database (index) to an ascii overlay file for
*	the TOP50 map products (mainly available in german speaking countries).
*/
public class OVLExporter extends Exporter{

	public OVLExporter(Preferences p, Profile prof){
		super();
		this.setMask("*.ovl");
		this.setHowManyParams(LAT_LON|COUNT);
	}

	public String record(CacheHolder ch, String lat, String lon, int counter){
		StringBuffer str = new StringBuffer(200);
		double tmp;
		str.append("[Symbol "+Convert.toString(2*counter + 1)+"]\r\n");
		str.append("Typ=6\r\n");
		str.append("Width=15\r\n");
		str.append("Height=15\r\n");
		str.append("Col=1\r\n");
		str.append("Zoom=1\r\n");
		str.append("Size=2\r\n");
		str.append("Area=2\r\n");
		str.append("XKoord="+lon+"\r\n");
		str.append("YKoord="+lat+"\r\n");
		//the text
		str.append("[Symbol "+Convert.toString(2*counter + 2)+"]\r\n");
		str.append("Typ=2\r\n");
		str.append("Col=1\r\n");
		str.append("Zoom=1\r\n");
		str.append("Size=2\r\n");
		str.append("Area=2\r\n");
		str.append("Font=3\r\n");
		str.append("Dir=1\r\n");
		tmp = Common.parseDouble(lon);
		tmp += 0.002;
		str.append("XKoord="+Convert.toString(tmp).replace(',', '.')+"\r\n");
		tmp = Common.parseDouble(lat);
		tmp += 0.002;
		str.append("YKoord="+Convert.toString(tmp).replace(',', '.')+"\r\n");
		str.append("Text="+ch.getWayPoint()+"\r\n");

		return str.toString();
	}

	public String trailer(int counter){
		StringBuffer str = new StringBuffer(200);

		str.append("[Overlay]\r\n");
		str.append("Symbols="+Convert.toString(counter*2)+"\r\n");
		// maplage section
		str.append("[MapLage]\r\n");
		str.append("MapName=Gesamtes Bundesgebiet (D1000)\r\n");
		str.append("DimmFc=100\r\n");
		str.append("ZoomFc=100\r\n");
		str.append("CenterLat="+pref.getCurCentrePt().getLatDeg(TransformCoordinates.CW)+".00\r\n");
		str.append("CenterLong="+pref.getCurCentrePt().getLonDeg(TransformCoordinates.CW)+".00\r\n");
		str.append("RefColor=255\r\n");
		str.append("RefRad=58\r\n");
		str.append("RefLine=6\r\n");
		str.append("RefOn=0\r\n");
		str.append("\r\n");
		return str.toString();
	}


/*	public void doIt(){
		CacheHolder holder;
		ParseLatLon pll;
		int symCounter = 1;
		FileChooser fc = new FileChooser(FileChooser.SAVE, profile.dataDir);
		fc.setTitle("Select target file:");
		if(fc.execute() != FileChooser.IDCANCEL){
			File saveTo = fc.getChosenFile();
			try{
				PrintWriter outp =  new PrintWriter(new BufferedWriter(new FileWriter(saveTo)));
				//symbols section, loop through database
				//a circle and text per cache is created
				for(int i = 0; i<cacheDB.size(); i++){
					holder=(CacheHolder)cacheDB.get(i);
					if(holder.is_black == false && holder.is_filtered == false){
						pll = new ParseLatLon(holder.LatLon,".");
						pll.parse();
						//the circle!
						outp.print("[Symbol "+Convert.toString(symCounter)+"]\r\n");
						outp.print("Typ=6\r\n");
						outp.print("Width=15\r\n");
						outp.print("Height=15\r\n");
						outp.print("Col=1\r\n");
						outp.print("Zoom=1\r\n");
						outp.print("Size=2\r\n");
						outp.print("Area=2\r\n");
						outp.print("XKoord="+pll.getLonDeg()+"\r\n");
						outp.print("YKoord="+pll.getLatDeg()+"\r\n");
						symCounter++;
						//the text
						outp.print("[Symbol "+Convert.toString(symCounter)+"]\r\n");
						outp.print("Typ=2\r\n");
						outp.print("Col=1\r\n");
						outp.print("Zoom=1\r\n");
						outp.print("Size=2\r\n");
						outp.print("Area=2\r\n");
						outp.print("Font=3\r\n");
						outp.print("Dir=1\r\n");
						outp.print("XKoord="+Convert.toString(pll.lon2+0.002).replace(',', '.')+"\r\n");
						outp.print("YKoord="+Convert.toString(pll.lat2+0.001).replace(',', '.')+"\r\n");
						outp.print("Text="+holder.wayPoint+"\r\n");
						symCounter++;
					}//if holder...
				}//for ... i < cacheDB ...
				// overlay section
				outp.print("[Overlay]\r\n");
				outp.print("Symbols="+Convert.toString(symCounter-1)+"\r\n");
				// maplage section
				outp.print("[MapLage]\r\n");
				outp.print("MapName=Gesamtes Bundesgebiet (D1000)\r\n");
				outp.print("DimmFc=100\r\n");
				outp.print("ZoomFc=100\r\n");
				outp.print("CenterLat="+myPreferences.curCentrePt.getLatDeg(CWPoint.CW)+".00\r\n");
				outp.print("CenterLong="+myPreferences.curCentrePt.getLonDeg(CWPoint.CW)+".00\r\n");
				outp.print("RefColor=255\r\n");
				outp.print("RefRad=58\r\n");
				outp.print("RefLine=6\r\n");
				outp.print("RefOn=0\r\n");
				outp.print("\r\n");

				outp.close();
			}catch(Exception e){
				Vm.debug("Error writing to OVL file! "+e.toString());
			}
		} // if execute
	}
*/}
