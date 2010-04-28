package CacheWolf.navi;
 
import CacheWolf.CWPoint;
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
import CacheWolf.CacheHolder;
import CacheWolf.navi.MovingMap;


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
		if(Global.getPref().useGPSD){
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
					MyLocale.getMsg(4408, "Could not connect to GPSD:")
					+ e.getMessage()
					+ MyLocale.getMsg(4409, "\npossible reasons:\nGPSD is not running or GPSD host is not reachable"),
					FormBase.OKB)).execute();
			}
		}else{
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
		stopDisplayTimer();
		gpsPos.stopLog();
		gpsRunning = false;
		if (gotoPanel != null) gotoPanel.gpsStoped();
		if (movingMap != null) movingMap.gpsStoped();
	}

	public boolean isGpsPosValid() {
		return (serThread != null && serThread.isAlive() || gpsdThread != null && gpsdThread.isAlive() ) && gpsPos.isValid() ; // && gpsPos.getfiex();
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
			//Vm.debug("currTrack.add: before");
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
				// ewe.sys.Vm.debug("neu: "+ skyOrientationDir.lonDec+ "jd: " + jd);
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


class GpsdThread extends mThread{
	Socket gpsdSocket;
	CWGPSPoint myGPS;
	boolean run, tcpForward;
	Socket tcpConn;
	String lastError = new String();

	public GpsdThread(CWGPSPoint GPSPoint) throws IOException {
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
					//Vm.debug("P -> " + gpsResult);
					noData = 0;
					if (myGPS.examineGpsd(gpsResult))
						notinterpreted = 0;
					else
						notinterpreted++;
					if (notinterpreted > 22) myGPS.noInterpretableData();
				}
			}
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
				//Vm.debug("Loop? " + noData);
				noData++;
				if (noData > 5) { myGPS.noDataError(); }
			} catch (InterruptedException e) {
				Global.getPref().log("Ignored Exception", e, true);
			}
			if (comSp != null)	{
				comLength = comSp.nonBlockingRead(comBuff, 0 ,comBuff.length);
				//Vm.debug("Length: " + comBuff.length);
				if (comLength > 0)	{
					noData = 0;
					String str = mString.fromAscii(comBuff, 0, comLength);
					if (tcpForward) {
						try {
							tcpConn.write(comBuff, 0, comLength);
						} catch (IOException e) { tcpForward = false; }
					}
					//Vm.debug(str);
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



