    /*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
    See http://developer.berlios.de/projects/cachewolf/
    for more information.
    Contact: 	bilbowolf@users.berlios.de
    			kalli@users.berlios.de

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    */
package CacheWolf.navi;
 
import net.ax86.GPS;
import net.ax86.GPSException;

import org.json.JSONException;
import org.json.JSONObject;

import CacheWolf.CWPoint;
import CacheWolf.CacheHolder;
import CacheWolf.Global;
import CacheWolf.MyLocale;
import CacheWolf.Preferences;
import ewe.fx.Color;
import ewe.io.IOException;
import ewe.io.SerialPort;
import ewe.io.SerialPortOptions;
import ewe.net.Socket;
import ewe.sys.mThread;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.util.mString;


/**
 * Non-Gui Class to handle all things regarding navigation
 * (GPS, Sun direction etc.)
 * start offset in localisation file: 4400
 * @author Pfeffer
 *
 */
public class Navigate {
	public CWPoint destination = new CWPoint();
	public CacheHolder destinationCache;
	public boolean destinationIsCache = false;
	public CWGPSPoint gpsPos = new CWGPSPoint();
	public Track curTrack = null;
	Color trackColor = new Color(255,0,0); // red
	public CWPoint skyOrientationDir = new CWPoint();
	public int luminary = SkyOrientation.SUN;

	public GotoPanel gotoPanel = null;
	public MovingMap movingMap = null;
	public GpsdThread gpsdThread = null;
	public OldGpsdThread oldGpsdThread = null;
	public SerialThread serThread = null;
	public Preferences pref = Global.getPref();
	public UpdateThread tickerThread;
	public boolean gpsRunning = false;
	boolean lograw = false;
	int logIntervall = 10;

	public void setGotoPanel(GotoPanel gp) {
		gotoPanel = gp;
	}
	public void setMovingMap (MovingMap mm) {
		movingMap = mm;
		if (gpsRunning) mm.gpsStarted();
	}

	public void startGps(boolean loggingOn, int loggingIntervall) {
		lograw = loggingOn;
		logIntervall = loggingIntervall; // TODO switch on and off during serthread running
		
		switch(Global.getPref().useGPSD) {
			// Tblue> TODO: NEW vs. OLD: This is ugly! The only line that's
			//        different is the one where the object is created!
			case Preferences.GPSD_FORMAT_NEW:
				try {
					gpsdThread = new GpsdThread(gpsPos);
					gpsdThread.start();
					startDisplayTimer();
					gpsRunning = true;
					curTrack = new Track(trackColor); // TODO addTrack here to MovingMap? see MovingMapPanel.snapToGps
					if (lograw)	gpsPos.startLog(Global.getProfile().dataDir, logIntervall, CWGPSPoint.LOGALL);
					if (gotoPanel != null) gotoPanel.gpsStarted();
					if (movingMap != null) movingMap.gpsStarted();
				} catch (IOException e) {
					(new MessageBox(MyLocale.getMsg(4403, "Error"),
						MyLocale.getMsg(4408, "Could not connect to GPSD: ")
						+ e.getMessage()
						+ MyLocale.getMsg(4409, "\nPossible reasons:\nGPSD is not running or GPSD host is not reachable"),
						FormBase.OKB)).execute();
				} catch( Exception e ) {
					// Other error (JSON/GPS).
					(new MessageBox(MyLocale.getMsg(4403, "Error"),
						MyLocale.getMsg(99999, "Could not initialize GPSD connection: ") 
						+ e.getMessage(),
						FormBase.OKB)).execute();
				}
				break;

			case Preferences.GPSD_FORMAT_OLD:
				try {
					oldGpsdThread = new OldGpsdThread(gpsPos);
					oldGpsdThread.start();
					startDisplayTimer();
					gpsRunning = true;
					curTrack = new Track(trackColor); // TODO addTrack here to MovingMap? see MovingMapPanel.snapToGps
					if (lograw)	gpsPos.startLog(Global.getProfile().dataDir, logIntervall, CWGPSPoint.LOGALL);
					if (gotoPanel != null) gotoPanel.gpsStarted();
					if (movingMap != null) movingMap.gpsStarted();
				} catch (IOException e) {
					(new MessageBox(MyLocale.getMsg(4403, "Error"),
						MyLocale.getMsg(4408, "Could not connect to GPSD: ")
						+ e.getMessage()
						+ MyLocale.getMsg(4409, "\nPossible reasons:\nGPSD is not running or GPSD host is not reachable"),
						FormBase.OKB)).execute();
				}
				break;

			case Preferences.GPSD_DISABLED:
			default:
				if (serThread != null) if (serThread.isAlive()) return; // TODO use gpsRunning
				try {
					serThread = new SerialThread(pref.mySPO, gpsPos, (pref.forwardGPS ? pref.forwardGpsHost : ""));
					if (pref.forwardGPS && !serThread.tcpForward) {
						(new MessageBox(MyLocale.getMsg(4400, "Warning"),
								MyLocale.getMsg(4401, "Ignoring error:\n could not forward GPS data to host:\n")
								+ pref.forwardGpsHost+"\n" + serThread.lastError
								+ MyLocale.getMsg(4402, "\nstop and start GPS to retry"), FormBase.OKB)).exec();
					}
					serThread.start();
					startDisplayTimer();
					gpsRunning = true;
					curTrack = new Track(trackColor); // TODO addTrack here to MovingMap? see MovingMapPanel.snapToGps
					if (lograw)	gpsPos.startLog(Global.getProfile().dataDir, logIntervall, CWGPSPoint.LOGALL);
					if (gotoPanel != null) gotoPanel.gpsStarted();
					if (movingMap != null) movingMap.gpsStarted();
				} catch (IOException e) {
					(new MessageBox(MyLocale.getMsg(4403, "Error"),
							MyLocale.getMsg(4404, "Could not connect to GPS-receiver.\n Error while opening serial Port ")
							+ e.getMessage()
							+ MyLocale.getMsg(4405, "\npossible reasons:\n Another (GPS-)program is blocking the port\nwrong port\nOn Loox: active infra-red port is blocking GPS"),
							FormBase.OKB)).execute();
				} catch (UnsatisfiedLinkError e) {
					(new MessageBox(MyLocale.getMsg(4403, "Error"),
							MyLocale.getMsg(4404, "Could not connect to GPS-receiver.\n Error while opening serial Port ")
							+ MyLocale.getMsg(4406, "Please copy jave_ewe.dll into the directory of the cachewolf program"),
							FormBase.OKB)).execute();
				}
				break;
		}
	}
	public void startDisplayTimer() {
		tickerThread = new UpdateThread(this, 1000);
		tickerThread.start();
	}

	public void stopDisplayTimer(){
		if (tickerThread != null) tickerThread.stop();
	}

	public void stopGps() {
		if(serThread!=null)	serThread.stop();
		if(gpsdThread!=null) gpsdThread.stop();
		if(oldGpsdThread!=null) oldGpsdThread.stop();
		stopDisplayTimer();
		gpsPos.stopLog();
		gpsRunning = false;
		if (gotoPanel != null) gotoPanel.gpsStoped();
		if (movingMap != null) movingMap.gpsStoped();
	}

	public boolean isGpsPosValid() {
		return ((serThread != null && serThread.isAlive()) ||
		       (gpsdThread != null && gpsdThread.isAlive()) ||
		       (oldGpsdThread != null && oldGpsdThread.isAlive()))
		        && gpsPos.isValid() ; // && gpsPos.getfiex();
	}


	public void setDestination(String LatLon) {
		setDestination(new CWPoint(LatLon));
	}


	public void setDestination(CWPoint d) {
		destinationIsCache = false;
		destination = new CWPoint (d);
		if (gotoPanel != null) gotoPanel.destChanged(destination);
		if (movingMap != null) movingMap.destChanged(destination);
	}

	public void setDestination(CacheHolder ch) {
		destinationIsCache = true;
		destinationCache=ch;
		destination = new CWPoint (ch.pos);
		if (gotoPanel != null) gotoPanel.destChanged(destination);
		if (movingMap != null) movingMap.destChanged(ch);
	}

	/**
	 * use the constants SkyOrientation.SUN, SkyOrientation.MOON etc.
	 * @param lu
	 */
	public void setLuminary(int lu) {
		luminary = lu;
	}
	public void ticked() {
		int fix = gpsPos.getFix();
		if (fix > 0 && (gpsPos.getSats()>= 0)) {
			if (curTrack == null) curTrack = new Track(trackColor);
			try {
				curTrack.add(gpsPos);
			} catch (IndexOutOfBoundsException e) { // track full -> create a new one
				curTrack = new Track(trackColor);
				curTrack.add(gpsPos);
				if (movingMap != null) movingMap.addTrack(curTrack);
			}
			try {
				SkyOrientation.getSunAzimut(gpsPos.Time, gpsPos.Date, gpsPos.latDec, gpsPos.lonDec);
				double jd = SkyOrientation.utc2juliandate(gpsPos.Time, gpsPos.Date);
				skyOrientationDir = SkyOrientation.getLuminaryDir(luminary, jd, gpsPos);
			} catch (NumberFormatException e) { // irgendeine Info zu Berechnung des Sonnenaziumt fehlt (insbesondere Datum und Uhrzeit sind nicht unbedingt gleichzeitig verfügbar wenn es einen Fix gibt)
				skyOrientationDir.set(-361, -361); // any value out of range (bigger than 360) will prevent drawArrows from drawing it
			}

		} else {
			skyOrientationDir.set(-361, -361); // any value out of range (bigger than 360) will prevent drawArrows from drawing it
		}
		gotoPanel.updateGps(fix);
		if (movingMap != null) movingMap.updateGps(fix);
	}
}


/**
 * Thread for reading data from gpsd.
 *
 * @author Tilman Blumenbach
 */
class GpsdThread extends mThread {
	GPS gpsObj;
	CWGPSPoint myGPS;
	boolean run;


	public GpsdThread(CWGPSPoint GPSPoint) throws IOException, JSONException, GPSException {
		JSONObject response;
		int proto_major;

		myGPS = GPSPoint;
		gpsObj = new GPS(Global.getPref().gpsdHost, Global.getPref().gpsdPort);
		gpsObj.stream( GPS.WATCH_ENABLE );

		// Check major protocol version:
		response = gpsObj.read();

		if( ! response.getString( "class" ).equals( "VERSION" ) ) {
			throw new GPSException( "Expected VERSION object at connect." );
		} else if( ( proto_major = response.getInt( "proto_major" ) ) != 3 ) {
			throw new GPSException( "Invalid protocol API version; got " +
					proto_major + ", want 3." );
		}
	}


	public void run() {
		JSONObject response;
		String respClass;
		int noData = 0;
		int notInterpreted = 0;
		boolean gotValidData = false; // redundant, but compiler complains.

		run = true;
		while (run) {
			if( gpsObj != null ) {
				gotValidData = false;

				try {
					/* Tblue> This is ugly, but BufferedReader::ready() seems to
					 *        be broken in Ewe, so instead of only polling when
					 *        there is no data from gpsd (by checking the return
					 *        value of GPS::waiting(), we poll on every iteration.
					 *        Not ideal, but works for now.
					 */
					gpsObj.poll();

					/* Tblue> TODO: I think this call should not block, but
					 *              my GPS class does not yet support non-blocking
					 *              reads...
					 */
					response  = gpsObj.read();
					
					// If we get here we have got some data:
					noData = 0;

					respClass = response.getString( "class" );
					if( respClass.equals( "DEVICE" ) && response.has( "activated" ) &&
						response.getDouble( "activated" ) != 0 )
					{	// This is a new device, we need to tell gpsd we want to watch it:
						Global.getPref().log( "New GPS device, sending WATCH command." );
						gpsObj.stream( GPS.WATCH_ENABLE );
					} else if( respClass.equals( "POLL" ) ) {
						gotValidData = myGPS.examineGpsd(response);
					} else if( respClass.equals( "ERROR" ) ) {
						Global.getPref().log( "Ignored gpsd error: " + response.getString( "message" ) );
					}
				} catch( Exception e ) {
					// Something bad happened, will just ignore this JSON
					// object:
					Global.getPref().log("Ignored Exception", e, true);
					gotValidData = false;
				}

				if( gotValidData ) {
					notInterpreted = 0;
				} else {
					notInterpreted++;
				}
				if (notInterpreted > 22) {
					myGPS.noInterpretableData();
				}
			}

			try {
				sleep(1000);
			} catch (InterruptedException e) {
				Global.getPref().log("Ignored Exception", e, true);
			}

			noData++;
			if (noData > 5) {
				myGPS.noDataError();
			}
		} // while

		myGPS.noData();
	}


	public void stop() {
		run = false;

		if( gpsObj != null ) {
			gpsObj.cleanup();
		}
	}
}


class OldGpsdThread extends mThread{
	Socket gpsdSocket;
	CWGPSPoint myGPS;
	boolean run, tcpForward;
	Socket tcpConn;
	String lastError = new String();

	public OldGpsdThread(CWGPSPoint GPSPoint) throws IOException {
		try{
			gpsdSocket = new Socket(Global.getPref().gpsdHost, Global.getPref().gpsdPort);
		} catch (IOException e) {
			throw new IOException(Global.getPref().gpsdHost);
		} // catch (UnsatisfiedLinkError e) {} // TODO in original java-vm
		myGPS = GPSPoint;
	}

	public void run() {
		String gpsResult;
		int noData = 0;
		int notinterpreted = 0;
		run = true;
		while (run){
			try {
				sleep(900);
				noData++;
				if (noData > 5) { myGPS.noDataError(); }
			} catch (InterruptedException e) {
				Global.getPref().log("Ignored Exception", e, true);
			}
			if (gpsdSocket != null)	{
				gpsResult = getGpsdData("ADPQTV\r\n");
				if (gpsResult!=null) {
					noData = 0;
					if (myGPS.examineOldGpsd(gpsResult))
						notinterpreted = 0;
					else
						notinterpreted++;
					if (notinterpreted > 22) myGPS.noInterpretableData();
				}
			}

			//myGPS.printAll();
		} // while
		myGPS.noData();
	}

	private String getGpsdData(String command) {
		byte[] rcvBuff = new byte[1024*10]; // when some action takes a long time (eg. loading or zooming a map), a lot of data can be in the buffer, read that at once
		int rcvLength = 0;
		try {
			gpsdSocket.write(command.getBytes());
		} catch (IOException e) {
			Global.getPref().log("Socket exception", e, true);
		}
		try {
			sleep(100);
		} catch (InterruptedException e) {
			Global.getPref().log("Ignored exception", e, true);
		}
		try {
			rcvLength = gpsdSocket.read(rcvBuff);
		} catch (IOException e) {
			Global.getPref().log("Socket exception", e, true);
		}
		String str = null;
		if (rcvLength > 0)	{
			str = mString.fromAscii(rcvBuff, 0, rcvLength);
		}
		return str;
	}

	public void stop() {
		run = false;
		if (gpsdSocket != null) gpsdSocket.close();
	}
}


/**
 * Thread for reading data from COM-port
 *
 */
class SerialThread extends mThread{
	SerialPort comSp;
	byte[] comBuff = new byte[1024*10]; // when some action takes a long time (eg. loading or zooming a map), a lot of data can be in the buffer, read that at once
	int comLength = 0;
	CWGPSPoint myGPS;
	boolean run, tcpForward;
	Socket tcpConn;
	String lastError = new String();

	public SerialThread(SerialPortOptions spo, CWGPSPoint GPSPoint, String forwardIP) throws IOException {
		try{
			spo.portName = CacheWolf.Common.fixSerialPortName(spo.portName);
			comSp = new SerialPort(spo);
		} catch (IOException e) {
			throw new IOException(spo.portName);
		} // catch (UnsatisfiedLinkError e) {} // TODO in original java-vm
		if (forwardIP.length()>0) {
			try {
				tcpConn = new Socket(forwardIP, 23);
				tcpForward = true;
			} catch (ewe.net.UnknownHostException e) { tcpForward = false; lastError = e.getMessage();
			} catch (IOException e) { tcpForward = false; lastError = e.getMessage();
			}
		}
		myGPS = GPSPoint;
	}

	public void run() {
		int noData = 0;
		int notinterpreted = 0;
		run = true;
		while (run){
			try {
				sleep(1000);
				noData++;
				if (noData > 5) { myGPS.noDataError(); }
			} catch (InterruptedException e) {
				Global.getPref().log("Ignored Exception", e, true);
			}
			if (comSp != null)	{
				comLength = comSp.nonBlockingRead(comBuff, 0 ,comBuff.length);
				if (comLength > 0)	{
					noData = 0;
					String str = mString.fromAscii(comBuff, 0, comLength);
					if (tcpForward) {
						try {
							tcpConn.write(comBuff, 0, comLength);
						} catch (IOException e) { tcpForward = false; }
					}
					if (myGPS.examine(str)) notinterpreted = 0; else notinterpreted++;
					if (notinterpreted > 22) myGPS.noInterpretableData();
				}
			}
		} // while
		myGPS.noData();
		tcpConn.close();
	}

	public void stop() {
		run = false;
		if (comSp != null) comSp.close();
	}
}

/**
 * Class for creating a new mThread to create timer ticks to be able to do form.close in the ticked-thread.
 * Using the Vm.requestTimer-Method causes "ewe.sys.EventDirectionException: This task cannot be done within
 * a Timer Tick." in the ewe-vm when form.close is called.
 */

class UpdateThread extends mThread {
	public boolean run;
	public int calldelay;
	public Navigate ticked;

	public UpdateThread (Navigate gp, int cd) {
		ticked = gp;
		calldelay = cd;
	}

	public void run () {
		run = true;
		while (run) {
			try { sleep (calldelay);} catch (InterruptedException e) {	}
			try { ticked.ticked();} catch (Exception e) {
				Global.getPref().log("Navigate.UpdateThread.run(): Ignored Exception. There should not be an Exception, so please report it in the cachewolf forum at www.geoclub.de", e, true);
			}
		}
	}

	public void stop() {
		run = false;
	}
}



