/*
 * Created on 02.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package CacheWolf.navi;
import com.stevesoft.ewe_pat.Regex;

import CacheWolf.CWPoint;
import CacheWolf.Common;
import CacheWolf.Extractor;
import CacheWolf.Global;
import ewe.sys.*;
import ewe.io.*;



/**
 * @author Kalle
 * Class for decoding NMEA sentences
 */

public class CWGPSPoint extends CWPoint implements TimerProc{
	public static final int LOGNMEA = 0x01;
	public static final int LOGRAW  = 0x02;
	public static final int LOGALL  = LOGNMEA|LOGRAW;

	public double Speed; //Speed
	public double Bear;	//Bearing
	public String Time; //Time
	public String Date;
	public int Fix; //Fix
	public int numSat; //Satellites in use, -1 indicates no data, -2 that data could not be interpreted
	public int numSatsInView; //Satellites in view
	public double HDOP; // Horizontal dilution of precision
	public double Alt; //Altitude

	//Logging
	int logTimer = 0;
	int logFlag = 0;
	boolean writeLog = false;
	boolean doLogging = false;
	FileWriter logFile;
	String lastStrExamined = new String();

	Regex numberMatcher = new Regex("\\-?\\d+");

	public CWGPSPoint()
	{
		super();
		this.Speed = 0;
		this.Bear = 0;
		this.Time = "";
		this.Date="";
		this.Fix = 0;
		this.numSat = 0;
		this.numSatsInView = 0;
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

	/**
	 * @return > 0: fixed <br> 0: not fixed <br> -1: no data from serial port <br> -2 data from serial port could not be interpreted
	 */
	public int getFix(){
		return this.Fix;
	}

	/**
	 * this method should be called, if COM-Port is closed
	 */
	public void noData(){
		this.Fix = 0;
		this.numSat = 0;
		this.HDOP = 0;
	}

	/**
	 * this method should be called, if not data is coming from COM-Port but is expected to come
	 */
	public void noDataError(){
		this.Fix = -1;
		this.numSat = -1;
		this.HDOP = -1;
	}

	/**
	 * this method should be called, if examine returns for several calls that it couldn't interprete the data
	 */
	public void noInterpretableData(){
		this.Fix = -2;
		this.numSat = -2;
		this.HDOP = -2;
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
			} catch (IOException e) {/*Too lazy to do something */}
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

	public int getSatsInView(){
		return this.numSatsInView;
	}

	public double getAlt(){
		return this.Alt;
	}

	public double getHDOP(){
		return this.HDOP;
	}

	/**
	 * Sets the attributes from a NMEA String
	 * @param NMEA	string with data to examine
	 * @return true if some data could be interpreted false otherwise
	 */
	public boolean examine(String NMEA){ 
		boolean interpreted = false;
		boolean logWritten = false;
		try {
			int i, start, end;
			String latDeg="0", latMin="0", latNS="N"; 
			String lonDeg="0", lonMin="0", lonEW="E";
			String currToken;
			end = 0;
			lastStrExamined = NMEA;
			//Vm.debug(NMEA);
/*			if (writeLog && (logFlag & LOGRAW) > 0){ 
				try {
					logFile.write(NMEA);
					writeLog = false;
				} catch (IOException e) {}
			}
*/			while(true){
				start = NMEA.indexOf("$GP", end);  
				if (start == -1) break;
				end = NMEA.indexOf("*", start);  
				if ((end == -1)||(end+3 > NMEA.length())) break;  


				//Vm.debug(NMEA.substring(start,end+3));
				if ((end - start) < 15 || !checkSumOK(NMEA.substring(start,end+3))){
					//Vm.debug("checksum wrong");
					continue;
				}
				// Write log after finding valid NMEA sequence 
				if (writeLog && (logFlag & LOGRAW) > 0){ 
					try {
						logFile.write(NMEA.substring(start,end+3)+"\n");
						logWritten = true;
					} catch (IOException e) {
						Global.getPref().log("Ignored Exception", e, true);
					}
				}

				Extractor ex = new Extractor ("," + NMEA.substring(start,end), ",",",",0,true);
				currToken = ex.findNext();
				if (currToken.equals("$GPGGA")){
					//Vm.debug("In $GPGGA");
					i = 0;
					while(ex.endOfSearch() != true){
						boolean latlonerror = false; // indicate that some error occured in the data -> in this case frace fix to non-fixed in order to avoid invalid coordinates when a fix is indicated to the higher level API
						currToken = ex.findNext();
						i++;
						if (currToken.length()==0) {
							if (i >= 2 && i <= 5) latlonerror = true; // force non-fix if lat-lon not contained
							continue; // sometimes there are 2 colons directly one after the other like ",," (e.g. loox)
						}
						switch (i){
						case 1: this.Time = currToken; break;
						case 2: try {latDeg = currToken.substring(0,2); interpreted = true;} catch (IndexOutOfBoundsException e) {latlonerror = true;}
						try {latMin = currToken.substring(2,currToken.length()); interpreted = true;} catch (IndexOutOfBoundsException e) {latlonerror = true;}
						break;
						case 3: latNS = currToken;
						break;

						case 4: try {lonDeg = currToken.substring(0,3); interpreted = true;} catch (IndexOutOfBoundsException e) {latlonerror = true;}
						try {lonMin = currToken.substring(3,currToken.length()); interpreted = true; } catch (IndexOutOfBoundsException e) {latlonerror = true;}
						break;
						case 5: lonEW = currToken;
						break;
						case 6: 
							if (!latlonerror) {
								this.Fix = Convert.toInt(currToken); 
								interpreted = true; 
								break;
							} else {
								this.Fix = 0; 
								break;
							}
						case 7: this.numSat = Convert.toInt(currToken); interpreted = true; break;
						case 8: try {this.HDOP = Common.parseDouble(currToken); interpreted = true; } catch (NumberFormatException e) {
							Global.getPref().log("Ignored Exception", e, true);
						} break;
						case 9: try {this.Alt = Common.parseDouble(currToken); interpreted = true; } catch (NumberFormatException e) {
							Global.getPref().log("Ignored Exception", e, true);
						} break;
						} // switch
					} // while
					if (Fix > 0) this.set(latNS, latDeg, latMin, "0", lonEW, lonDeg, lonMin, "0", TransformCoordinates.DMM);

				} // if

				if (currToken.equals("$GPVTG")){
					i = 0;
					while(ex.endOfSearch() != true){
						currToken = ex.findNext();
						i++;
						if (currToken.length()==0) continue;
						switch (i){
						case 1: try { this.Bear =Common.parseDouble(currToken); interpreted = true; } catch (NumberFormatException e) {
							Global.getPref().log("Ignored Exception", e, true);
						}
						if (this.Bear > 360) Vm.debug("Error bear VTG");
						break;
						case 7: try { this.Speed = Common.parseDouble(currToken); interpreted = true; } catch (NumberFormatException e) {
							Global.getPref().log("Ignored Exception", e, true);
						} 
						break;
						} // switch
					} // while
				} // if

				if (currToken.equals("$GPRMC")){
					//Vm.debug("In $GPRMC");
					i = 0;
					String status = "V";
					boolean latlonerror = false;
					while(ex.endOfSearch() != true){
						currToken = ex.findNext();
						i++;
						if (currToken.length() == 0) {
							if (i >= 2 && i <= 6) latlonerror = true; // force non-fix if lat-lon not contained
							continue; // sometimes there are 2 colons directly one after the other like ",," (e.g. loox)
						}
						if (currToken.length() == 0) continue;
						//Vm.debug("zz: " + i);
						//Vm.debug(currToken);
						switch (i){
						case 1: this.Time = currToken; interpreted = true; break;
						case 2: status = currToken; 
						if (status.equals("A")) this.Fix = 1;
						else this.Fix = 0;
						interpreted = true;
						break;
						case 3: 	//Vm.debug("Here--->");
							try {latDeg = currToken.substring(0,2); interpreted = true;} catch (IndexOutOfBoundsException e) {latlonerror = true;}
							//Vm.debug(":" + latDeg);
							try {latMin = currToken.substring(2,currToken.length()); interpreted = true;} catch (IndexOutOfBoundsException e) {latlonerror = true;}
							//Vm.debug(":" + latMin);
							break;
						case 4: latNS = currToken; interpreted = true;
						break;
						case 5: try {lonDeg = currToken.substring(0,3); interpreted = true;} catch (IndexOutOfBoundsException e) {
							Global.getPref().log("Ignored Exception", e, true);
						}
						try {lonMin = currToken.substring(3,currToken.length()); interpreted = true;} catch (IndexOutOfBoundsException e) {
							Global.getPref().log("Ignored Exception", e, true);
						}
						break;
						case 6: lonEW = currToken;
						interpreted = true;
						break;
						case 7: if (status.equals("A")){
							try {this.Speed = Common.parseDouble(currToken)*1.854;
							interpreted = true; } catch (NumberFormatException e) { 
								Global.getPref().log("Ignored Exception", e, true);
							}
						}
						break;
						case 8: if (status.equals("A") && currToken.length()> 0){
							try {this.Bear = Common.parseDouble(currToken);
							interpreted = true; } catch (NumberFormatException e) {
								Global.getPref().log("Ignored Exception", e, true);
							}
						}
						break;
						case 9: if (status.equals("A") && currToken.length()> 0){
							try {this.Date = currToken;
							interpreted = true; } catch (NumberFormatException e) { 
								Global.getPref().log("Ignored Exception", e, true);
							}
						}
						break;
						} // switch
					} // while
					if (latlonerror) this.Fix = 0;
					else {
						if (status.equals("A")){
							this.set(latNS, latDeg, latMin, "0",
									lonEW, lonDeg, lonMin, "0", TransformCoordinates.DMM);				
						}
					}
				} // if

				if (currToken.equals("$GPGSV")){
					//Vm.debug("In $$GPGSV");
					i = 0;
					while(ex.endOfSearch() != true){
						currToken = ex.findNext();
						i++;
						if (currToken.length() == 0) continue; // sometimes there are 2 colons directly one after the other like ",," (e.g. loox)
						switch (i){
						case 3: this.numSatsInView = Convert.toInt(currToken); interpreted = true; break;
						} // switch
					} // while
				} // if
				
				//Vm.debug("End of examine");
			} //while
		} catch (Exception e) {
			Global.getPref().log("Exception in examine in CWGPSPoint", e, true);
			e.printStackTrace();
		}

		if	(logWritten)
			writeLog = false;

		return interpreted;
	}

	
	/**
	 * Sets the attributes from a GPSD string
	 * @param gps	GPSD string with data to examine
	 *              Format: GPSD,key=value,... 
	 * @return true if some data could be interpreted false otherwise
	 */
	public boolean examineGpsd(String gps){
		boolean valid = false;
		if(!gps.startsWith("GPSD,"))
			return false;
		Extractor ex = new Extractor (gps, ",",",",4,true);
		while(!ex.endOfSearch()){
			String part = ex.findNext();
			if(part.startsWith("A=") && part.indexOf('?')<0){
				// The current altitude as "A=%f", meters above mean sea level. 
				this.Alt=Common.parseDouble(part.substring(2));
				valid = true;
			}else if(part.startsWith("D=") && part.indexOf('?')<0){
				// Returns the UTC time in the ISO 8601 format, "D=yyyy-mm-ddThh:mm:ss.ssZ"
				//                                               0000000000111111111122 
				//                                               0123456789012345678901
				String year = part.substring(2,6);
				String month = part.substring(7,9);
				String day = part.substring(10,12);
				String hour = part.substring(13,15);
				String min = part.substring(16,18);
				String sec = part.substring(19,21);
				this.Date=year+month+day;
				this.Time=hour+min+sec;
				valid = true;
			}else if(part.startsWith("P=")){
				// Returns the current position in the form "P=%f %f"; numbers are in degrees, latitude first. 
				if(part.indexOf('?')<0){
					this.Fix = 1;
					int spacepos=part.indexOf(' ');
					if(spacepos>=3){
						String lat=part.substring(2,spacepos);
						String lon=part.substring(spacepos+1);
						this.latDec=Common.parseDouble(lat);
						this.lonDec=Common.parseDouble(lon);
					}else
						this.set(part.substring(2));
				}else{
					this.Fix = 0;
				}
				valid = true;
			}else if(part.startsWith("Q=")){
				// Returns "Q=%d %f %f %f %f %f": a count of satellites used in the last fix,
				// and five dimensionless dilution-of-precision (DOP) numbers -- 
				// spherical, horizontal, vertical, time, and total geometric.
				int spacepos=part.indexOf(' ');
				if(part.indexOf('?')<0 && spacepos>=3){
					this.numSat = Common.parseInt(part.substring(2, spacepos));
					valid = true;
				}else{
					this.numSat = 0;
				}
				this.numSatsInView = 0;			// Not supported by GPSD
				//TODO parse DOP values
			}else if(part.startsWith("T=") && part.indexOf('?')<0){
				// Track made good; course "T=%f" in degrees from true north.
				this.Bear = Common.parseDouble(part.substring(2));
				valid = true;
			}else if(part.startsWith("V=") && part.indexOf('?')<0){
				// The current speed over ground as "V=%f" in knots.
				this.Speed = Common.parseDouble(part.substring(2));
				valid = true;
			}
		}
		return valid;
	}
		
	private boolean checkSumOK(String nmea){
		int startPos = 1; // begin after $
		int endPos = nmea.length() - 3;// without * an two checksum chars
		byte checkSum = 0;

		for (int i= startPos; i<endPos;i++){
			checkSum ^= nmea.charAt(i);
		}
		//Vm.debug(nmea.substring(3,6)+" Checksum: " + nmea.substring(endPos+1) + " Calculated: " + Convert.intToHexString(checkSum));
		try { return (checkSum == Byte.parseByte(nmea.substring(endPos+1),16));
		} catch (IndexOutOfBoundsException e) {
			return false;
		} catch (NumberFormatException e) {
			return false;
		}
	}



	public void printAll(){
		Vm.debug("Latitude:     " + this.getLatDeg(TransformCoordinates.DD));
		Vm.debug("Longitude:    " + this.getLonDeg(TransformCoordinates.DD));
		Vm.debug("Speed:        " + this.Speed);
		Vm.debug("Bearing:      " + this.Bear);
		Vm.debug("Time:         " + this.Time);
		Vm.debug("Fix:          " + this.Fix);
		Vm.debug("Sats:         " + this.numSat);
		Vm.debug("Sats in view: " + this.numSatsInView);
		Vm.debug("HDOP:         " + this.HDOP);
		Vm.debug("Alt:          " + this.Alt);
		Vm.debug("----------------");
	}
}


