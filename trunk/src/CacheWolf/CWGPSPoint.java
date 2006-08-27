/*
 * Created on 02.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package CacheWolf;
import ewe.sys.*;
import ewe.util.*;
import ewe.io.*;



/**
 * @author Kalle
 * Class for decoding NMEA sentences
 */

public class CWGPSPoint extends CWPoint implements TimerProc{
	static protected final int LOGNMEA = 0x01;
	static protected final int LOGRAW  = 0x02;
	static protected final int LOGALL  = LOGNMEA|LOGRAW;
	
	double Speed; //Speed
	double Bear;	//Bearing
	String Time; //Time
	int Fix; //Fix
	int numSat; //Satellites in use, -1 indicates no data
	double HDOP; // Horizontal dilution of precision
	double Alt; //Altitude
	
	//Logging
	int logTimer = 0;
	int logFlag = 0;
	boolean writeLog = false;
	boolean doLogging = false;
	FileWriter logFile;
	
	
	public CWGPSPoint()
	{
		super();
		this.Speed = 0;
		this.Bear = 0;
		this.Time = "";
		this.Fix = 0;
		this.numSat = 0;
		this.Alt = 0;
		this.HDOP = 0;
	}
	
	
	public double getSpeed(){
		return this.Speed;
	}
	
	public double getBear (){
		return this.Bear;
	}
	public String getTime(){
		return this.Time;
	}
	
	public int getFix(){
		return this.Fix;
	}
	
	/**
	 * this method should be called, if no more data is received
	 *
	 */
	public void noData(){
		this.Fix = -1;
		this.numSat = 0;
	}
	
	public void ticked(int timerId, int elapsed){
		if (timerId == logTimer) {
			writeLog = true;
		}
		
	}
	
	/**
	 * 
	 * @param logFileDir directory for logfile
	 * @param seconds	 intervall for writing to logfile
	 * @param flag		 level of logging
	 * @return 0 success, -1 failure
	 */
	public int startLog(String logFileDir, int seconds, int flag){

		Time currTime = new Time();
		currTime.getTime();
		currTime.setFormat("yyyyMMdd'_'HHmm");
		String logFileName = new String(logFileDir + currTime.toString()+ ".log");
		// create Logfile
		try {
			logFile = new FileWriter(logFileName);
		} catch (IOException e) {
			Vm.debug("Error creating LogFile " + logFileName);
			return -1;
		} 
		// start timer
		logTimer = Vm.requestTimer(this, 1000 * seconds);
		logFlag = flag;
		doLogging = true;
		return 0;
	}
	
	public void stopLog() {
		writeLog = false;

		if (doLogging){
			try {
				logFile.close();
			} catch (IOException e) {}
			if (logTimer > 0) {
				Vm.cancelTimer(logTimer);
				logTimer = 0;
			}
		}
		doLogging = false;
	}

	
	public int getSats(){
		return this.numSat;
	}
	
	public double getAlt(){
		return this.Alt;
	}
	
	public double getHDOP(){
		return this.HDOP;
	}
	
	/**
	 * 
	 * @param NMEA	string with data to examine
	 */
	public void examine(String NMEA){
		int i, start, end;
		String latDeg="0", latMin="0", latNS="N"; 
		String lonDeg="0", lonMin="0", lonEW="E";
		String currToken;
		end = 0;
		//Vm.debug(NMEA);
		if (writeLog && (logFlag & LOGRAW) > 0){
			try {
				logFile.write(NMEA);
				writeLog = false;
			} catch (IOException e) {}
		}
		while(true){
			start = NMEA.indexOf("$GP", end);  
			if (start == -1) return;  
			end = NMEA.indexOf("*", start);  
			if ((end == -1)||(end+3 >= NMEA.length())) return;  

			
			//Vm.debug(NMEA.substring(start,end+3));
			if ((end - start) < 15 || !checkSumOK(NMEA.substring(start,end+3))){
				//Vm.debug("checksum wrong");
				continue;
			}
			Extractor ex = new Extractor ("," + NMEA.substring(start,end), ",",",",0,true);
			currToken = ex.findNext();
			if (currToken.equals("$GPGGA")){
				//Vm.debug("In $GPGGA");
				i = 0;
				while(ex.endOfSearch() != true){
					currToken = ex.findNext();
					i++;
					if (currToken.length()==0) continue; // sometimes there are 2 colons directly one after the other like ",," (e.g. loox)
					switch (i){
						case 1: this.Time = currToken; break;
						case 2: try {latDeg = currToken.substring(0,2); } catch (IndexOutOfBoundsException e) {}
								try {latMin = currToken.substring(2,currToken.length()); } catch (IndexOutOfBoundsException e) {}
								break;
						case 3: latNS = currToken;
								break;
								
						case 4: try {lonDeg = currToken.substring(0,3); } catch (IndexOutOfBoundsException e) {}
								try {lonMin = currToken.substring(3,currToken.length()); } catch (IndexOutOfBoundsException e) {}
								break;
						case 5: lonEW = currToken;
								break;
						case 6: this.Fix = Convert.toInt(currToken); break;
						case 7: this.numSat = Convert.toInt(currToken); break;
						case 8: try {this.HDOP = Common.parseDouble(currToken); } catch (NumberFormatException e) {} break;
						case 9: try {this.Alt = Common.parseDouble(currToken);  } catch (NumberFormatException e) {} break;
					} // switch
				} // while
				this.set(latNS, latDeg, latMin, "0",
						 lonEW, lonDeg, lonMin, "0", CWPoint.DMM);
			} // if
		
			if (currToken.equals("$GPVTG")){
				i = 0;
				while(ex.endOfSearch() != true){
					currToken = ex.findNext();
					i++;
					if (currToken.length()==0) continue;
					switch (i){
						case 1: try { this.Bear =Common.parseDouble(currToken); } catch (NumberFormatException e) {}
								if (this.Bear > 360) Vm.debug("Error bear VTG");
								break;
						case 7: try { this.Speed = Common.parseDouble(currToken); } catch (NumberFormatException e) {} 
								break;
					} // switch
				} // while
			} // if
			
			if (currToken.equals("$GPRMC")){
				//Vm.debug("In $GPRMC");
				i = 0;
				String status = "V";
				while(ex.endOfSearch() != true){
					currToken = ex.findNext();
					i++;
					if (currToken.length()==0) continue;
					//Vm.debug("zz: " + i);
					//Vm.debug(currToken);
					switch (i){
						case 1: this.Time = currToken; break;
						case 2: status = currToken;
								if (status.equals("A")) this.Fix = 1;
								else this.Fix = 0;
								break;
						case 3: 	//Vm.debug("Here--->");
								try {latDeg = currToken.substring(0,2); } catch (IndexOutOfBoundsException e) {}
								//Vm.debug(":" + latDeg);
								try {latMin = currToken.substring(2,currToken.length()); } catch (IndexOutOfBoundsException e) {}
								//Vm.debug(":" + latMin);
								break;
						case 4: latNS = currToken;
								break;
						case 5: try {lonDeg = currToken.substring(0,3); } catch (IndexOutOfBoundsException e) {}
								try {lonMin = currToken.substring(3,currToken.length()); } catch (IndexOutOfBoundsException e) {}
								break;
						case 6: lonEW = currToken;
								break;
						case 7: if (status.equals("A")){
									this.Speed = Common.parseDouble(currToken)*1.854;
								}
								break;
						case 8: if (status.equals("A") && currToken.length()> 0){
									this.Bear = Common.parseDouble(currToken);
								}
								break;
					} // switch
				} // while
				if (status.equals("A")){
					this.set(latNS, latDeg, latMin, "0",
							 lonEW, lonDeg, lonMin, "0", CWPoint.DMM);				
				}
			} // if
		//Vm.debug("End of examine");
		} //while
	}
	
	private boolean checkSumOK(String nmea){
		int startPos = 1; // begin after $
		int endPos = nmea.length() - 3;// without * an two checksum chars
		byte checkSum = 0;
		
		for (int i= startPos; i<endPos;i++){
			checkSum ^= nmea.charAt(i);
		}
		//Vm.debug(nmea.substring(3,6)+" Checksum: " + nmea.substring(endPos+1) + " Calculated: " + Convert.intToHexString(checkSum));
		return (checkSum == Byte.parseByte(nmea.substring(endPos+1),16));
		
	}
	
	  
	
	public void printAll(){
		Vm.debug("Latitude:  " + this.getLatDeg(DD));
		Vm.debug("Longitude: " + this.getLonDeg(DD));
		Vm.debug("Speed:     " + this.Speed);
		Vm.debug("Bearing:   " + this.Bear);
		Vm.debug("Time:      " + this.Time);
		Vm.debug("Fix:       " + this.Fix);
		Vm.debug("Sats:      " + this.numSat);
		Vm.debug("HDOP:      " + this.HDOP);
		Vm.debug("Alt:       " + this.Alt);
		Vm.debug("----------------");
	}
}
	
