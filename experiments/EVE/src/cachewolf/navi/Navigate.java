package cachewolf.navi;

import eve.fx.Color;
import java.io.IOException;
import eve.io.SerialPort;
import eve.ui.data.SerialPortOptions;
import java.net.Socket;

import cachewolf.CWPoint;
import cachewolf.Global;
import cachewolf.MyLocale;
import cachewolf.Preferences;

import eve.ui.MessageBox;
import eve.util.mString;
import eve.sys.Handle;

/**
 * Non-Gui Class to handle all things regarding navigation
 * (GPS, Sun direction etc.)
 * start offset in localisation file: 4400
 * @author Pfeffer
 *
 */
public class Navigate {
	public CWPoint destination = new CWPoint();
	public CWGPSPoint gpsPos = new CWGPSPoint();
	public Track curTrack = null;
	Color trackColor = new Color(255,0,0); // red
	public CWPoint skyOrientationDir = new CWPoint();
	public int luminary = SkyOrientation.SUN;

	public GotoPanel gotoPanel = null;
	public MovingMap movingMap = null;
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
		setRawLogging(loggingOn, loggingIntervall);
		if (serThread != null) if ((serThread.check() & Handle.Running)!=0) return; // TODO use gpsRunning
		try {
			serThread = new SerialThread(pref.mySPO, gpsPos, (pref.forwardGPS ? pref.forwardGpsHost : ""));
			if (pref.forwardGPS && !serThread.tcpForward) {
				(new MessageBox(MyLocale.getMsg(4400, "Warning"),
						MyLocale.getMsg(4401, "Ignoring error:\n could not forward GPS data to host:\n")
						+ pref.forwardGpsHost+"\n" + serThread.lastError
						+ MyLocale.getMsg(4402, "\nstop and start GPS to retry"), MessageBox.OKB)).exec();
			}
			if (gpsPos.latDec == 0 && gpsPos.lonDec == 0) { // TODO use isValid() // TODO raus damit?
				gpsPos.latDec = destination.latDec; // setze Zielpunkt als Ausgangspunkt
				gpsPos.lonDec = destination.lonDec;
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
					MessageBox.OKB)).execute();
		} catch (UnsatisfiedLinkError e) {
			(new MessageBox(MyLocale.getMsg(4403, "Error"),
					MyLocale.getMsg(4404, "Could not connect to GPS-receiver.\n Error while opening serial Port ")
					+ MyLocale.getMsg(4406, "Please copy jave_eve.dll into the directory of the cachewolf program"),
					MessageBox.OKB)).execute();
		}
	}

	public void setRawLogging(boolean on, int intervall) {
		lograw = on;
		logIntervall = intervall; // TODO switch on and off during serthread running
	}
	public void startDisplayTimer() {
		tickerThread = new UpdateThread(this, 1000);
		tickerThread.start();
	}

	public void stopDisplayTimer(){
		if (tickerThread != null) tickerThread.stop();
	}

	public void stopGps() {
		serThread.stop();
		stopDisplayTimer();
		gpsPos.stopLog();
		gpsRunning = false;
		if (gotoPanel != null) gotoPanel.gpsStopped();
		if (movingMap != null) movingMap.gpsStoped();
	}

	public boolean isGpsPosValid() {
		return 	serThread != null && ((serThread.check()&Handle.Running)!=0) && gpsPos.isValid() ; // && gpsPos.getfiex();

	}


	public void setDestination(String LatLon) {
		setDestination(new CWPoint(LatLon));
	}


	public void setDestination(CWPoint d) {
		destination = new CWPoint (d);
		if (gotoPanel != null) gotoPanel.destChanged(destination);
		if (movingMap != null) movingMap.destChanged(destination);
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
			//gpsPosition.printAll();
			//Vm.debug("currTrack.add: before");
			if (curTrack == null) curTrack = new Track(trackColor);
			try {
				curTrack.add(gpsPos);
			} catch (IndexOutOfBoundsException e) { // track full -> create a new one
				curTrack = new Track(trackColor);
				curTrack.add(gpsPos);
				if (movingMap != null) movingMap.addTrack(curTrack); // TODO maybe gotoPanel should also hold a list of Tracks, because otherwise they will be destroyed if not saved in mmp before
			}
			try {
				SkyOrientation.getSunAzimut(gpsPos.time, gpsPos.date, gpsPos.latDec, gpsPos.lonDec);
				double jd = SkyOrientation.utc2juliandate(gpsPos.time, gpsPos.date);
				skyOrientationDir = SkyOrientation.getLuminaryDir(luminary, jd, gpsPos);
				// eve.sys.Vm.debug("neu: "+ skyOrientationDir.lonDec+ "jd: " + jd);
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
 * Thread for reading data from COM-port
 *
 */
class SerialThread extends eve.sys.Task{
	SerialPort comSp;
	byte[] comBuff = new byte[1024*10]; // when some action takes a long time (eg. loading or zooming a map), a lot of data can be in the buffer, read that at once
	int comLength = 0;
	CWGPSPoint myGPS;
	boolean run, tcpForward;
	Socket tcpConn;
	String lastError = "";

	public SerialThread(SerialPortOptions spo, CWGPSPoint GPSPoint, String forwardIP) throws IOException {
		try{
			spo.portName = cachewolf.utils.Common.fixSerialPortName(spo.portName);
			comSp = new SerialPort(spo);
		} catch (IOException e) {
			throw new IOException(spo.portName);
		} // catch (UnsatisfiedLinkError e) {} // TODO in original java-vm
		if (forwardIP.length()>0) {
			try {
				tcpConn = new Socket(forwardIP, 23);
				tcpForward = true;
			} catch (java.net.UnknownHostException e) { tcpForward = false; lastError = e.getMessage();
			} catch (IOException e) { tcpForward = false; lastError = e.getMessage();
			}
		}
		myGPS = GPSPoint;
	}

	public void doRun() {
		int noData = 0;
		int notinterpreted = 0;
		run = true;
		while (run){
				sleep(1000);
				//Vm.debug("Loop? " + noData);
				noData++;
				if (noData > 5) { myGPS.noDataError(); }
			if (comSp != null)	{
//TODO Activate				comLength = comSp.nonBlockingRead(comBuff, 0 ,comBuff.length);
				//Vm.debug("Length: " + comBuff.length);
				if (comLength > 0)	{
					noData = 0;
					String str = mString.fromAscii(comBuff, 0, comLength);
					if (tcpForward) {
						try {
							tcpConn.getOutputStream().write(comBuff, 0, comLength);
						} catch (IOException e) { tcpForward = false; }
					}
					//Vm.debug(str);
					if (myGPS.examine(str)) notinterpreted = 0; else notinterpreted++;
					if (notinterpreted > 22) myGPS.noInterpretableData();
				}
			}
		} // while
		myGPS.noData();
		try {
			tcpConn.close();
		} catch (Exception ex) {};
	}

	public void stop() {
		try {
			run = false;
			if (comSp != null) comSp.close();
		} catch(IOException ex) {}
	}
}

/**
 * Class for creating a new mThread to create timer ticks to be able to do form.close in the ticked-thread.
 * Using the Vm.requestTimer-Method causes "eve.sys.EventDirectionException: This task cannot be done within
 * a Timer Tick." in the eve-vm when form.close is called.
 */

class UpdateThread extends eve.sys.Task {
	public boolean run;
	public int calldelay;
	public Navigate ticked;

	public UpdateThread (Navigate gp, int cd) {
		ticked = gp;
		calldelay = cd;
	}

	public void doRun () {
		run = true;
		while (run) {
			sleep (calldelay);
			ticked.ticked();
		}
	}

	public void stop() {
		run = false;
	}
}

