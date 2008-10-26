/*
 * Created on 02.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cachewolf.navi;
import eve.sys.*;
import java.io.*;

import cachewolf.CWPoint;
import cachewolf.Global;
import cachewolf.utils.Common;
import cachewolf.utils.Extractor;




/**
 * @author Kalle
 * Class for decoding NMEA sentences
 */

public class CWGPSPoint extends CWPoint implements TimerProc{
	public static final int LOGNMEA = 0x01;
	public static final int LOGRAW  = 0x02;
	public static final int LOGALL  = LOGNMEA|LOGRAW;

	public double speed; //Speed
	public double bear;	//Bearing
	public String time; //Time
	public String date;
	public int fix; //Fix
	public int numSat; //Satellites in use, -1 indicates no data, -2 that data could not be interpreted
	public int numSatsInView; //Satellites in view
	public double HDOP; // Horizontal dilution of precision
	public double alt; //Altitude

	//Logging
	Timer logTimer = null;
	int logFlag = 0;
	boolean writeLog = false;
	boolean doLogging = false;
	FileWriter logFile;
	String lastStrExamined = new String();


	public CWGPSPoint()
	{
		super();
		this.speed = 0;
		this.bear = 0;
		this.time = "";
		this.date="";
		this.fix = 0;
		this.numSat = 0;
		this.numSatsInView = 0;
		this.alt = 0;
		this.HDOP = 0;
	}


	public double getSpeed(){
		return this.speed;
	}

	public double getBear (){
		return this.bear;
	}
	public String getTime(){
		return this.time;
	}

	/**
	 * @return > 0: fixed <br> 0: not fixed <br> -1: no data from serial port <br> -2 data from serial port could not be interpreted
	 */
	public int getFix(){
		return this.fix;
	}

	/**
	 * this method should be called, if COM-Port is closed
	 */
	public void noData(){
		this.fix = 0;
		this.numSat = 0;
		this.HDOP = 0;
	}

	/**
	 * this method should be called, if not data is coming from COM-Port but is expected to come
	 */
	public void noDataError(){
		this.fix = -1;
		this.numSat = -1;
		this.HDOP = -1;
	}

	/**
	 * this method should be called, if examine returns for several calls that it couldn't interprete the data
	 */
	public void noInterpretableData(){
		this.fix = -2;
		this.numSat = -2;
		this.HDOP = -2;
	}

	public void ticked(Object timerId, long elapsed) {
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
		logTimer = (Timer) eve.sys.Timer.requestTick(this, 1000 * seconds);
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
			if (logTimer !=null) {
				logTimer = null;
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
		return this.alt;
	}

	public double getHDOP(){
		return this.HDOP;
	}

	/**
	 *
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
					} catch (IOException e) {}
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
						case 1: this.time = currToken; break;
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
								this.fix = Convert.toInt(currToken);
								interpreted = true;
								break;
							} else {
								this.fix = 0;
								break;
							}
						case 7: this.numSat = Convert.toInt(currToken); interpreted = true; break;
						case 8: try {this.HDOP = Common.parseDouble(currToken); interpreted = true; } catch (NumberFormatException e) {} break;
						case 9: try {this.alt = Common.parseDouble(currToken); interpreted = true; } catch (NumberFormatException e) {} break;
						} // switch
					} // while
					if (fix > 0) this.set(latNS, latDeg, latMin, "0", lonEW, lonDeg, lonMin, "0", CWPoint.DMM);

				} // if

				if (currToken.equals("$GPVTG")){
					i = 0;
					while(ex.endOfSearch() != true){
						currToken = ex.findNext();
						i++;
						if (currToken.length()==0) continue;
						switch (i){
						case 1: try { this.bear =Common.parseDouble(currToken); interpreted = true; } catch (NumberFormatException e) {}
						if (this.bear > 360) Vm.debug("Error bear VTG");
						break;
						case 7: try { this.speed = Common.parseDouble(currToken); interpreted = true; } catch (NumberFormatException e) {}
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
						if (currToken.length()==0) {
							if (i >= 2 && i <= 6) latlonerror = true; // force non-fix if lat-lon not contained
							continue; // sometimes there are 2 colons directly one after the other like ",," (e.g. loox)
						}
						if (currToken.length()==0) continue;
						//Vm.debug("zz: " + i);
						//Vm.debug(currToken);
						switch (i){
						case 1: this.time = currToken; interpreted = true; break;
						case 2: status = currToken;
						if (status.equals("A")) this.fix = 1;
						else this.fix = 0;
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
						case 5: try {lonDeg = currToken.substring(0,3); interpreted = true;} catch (IndexOutOfBoundsException e) {}
						try {lonMin = currToken.substring(3,currToken.length()); interpreted = true;} catch (IndexOutOfBoundsException e) {}
						break;
						case 6: lonEW = currToken;
						interpreted = true;
						break;
						case 7: if (status.equals("A")){
							try {this.speed = Common.parseDouble(currToken)*1.854;
							interpreted = true; } catch (NumberFormatException e) { }
						}
						break;
						case 8: if (status.equals("A") && currToken.length()> 0){
							try {this.bear = Common.parseDouble(currToken);
							interpreted = true; } catch (NumberFormatException e) { }
						}
						break;
						case 9: if (status.equals("A") && currToken.length()> 0){
							try {this.date = currToken;
							interpreted = true; } catch (NumberFormatException e) { }
						}
						break;
						} // switch
					} // while
					if (latlonerror) this.fix = 0;
					else {
						if (status.equals("A")){
							this.set(latNS, latDeg, latMin, "0",
									lonEW, lonDeg, lonMin, "0", CWPoint.DMM);
						}
					}
				} // if

				if (currToken.equals("$GPGSV")){
					//Vm.debug("In $$GPGSV");
					i = 0;
					while(ex.endOfSearch() != true){
						currToken = ex.findNext();
						i++;
						if (currToken.length()==0) continue; // sometimes there are 2 colons directly one after the other like ",," (e.g. loox)
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
		Vm.debug("Latitude:     " + this.getLatDeg(DD));
		Vm.debug("Longitude:    " + this.getLonDeg(DD));
		Vm.debug("Speed:        " + this.speed);
		Vm.debug("Bearing:      " + this.bear);
		Vm.debug("Time:         " + this.time);
		Vm.debug("Fix:          " + this.fix);
		Vm.debug("Sats:         " + this.numSat);
		Vm.debug("Sats in view: " + this.numSatsInView);
		Vm.debug("HDOP:         " + this.HDOP);
		Vm.debug("Alt:          " + this.alt);
		Vm.debug("----------------");
	}
}


