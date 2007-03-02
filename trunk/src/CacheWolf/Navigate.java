package CacheWolf;

import ewe.fx.Color;
import ewe.io.IOException;
import ewe.io.SerialPort;
import ewe.io.SerialPortOptions;
import ewe.net.Socket;
import ewe.sys.Convert;
import ewe.sys.mThread;
import ewe.ui.MessageBox;
import ewe.util.mString;

public class Navigate {
	public CWPoint destination = new CWPoint();
	public CWGPSPoint gpsPos = new CWGPSPoint();
	public Track curTrack = null;
	Color trackColor = new Color(255,0,0); // red
	public float sunAzimut = -361;

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

	public void startGps() {
		if (serThread != null) if (serThread.isAlive()) return; // TODO use gpsRunning
		try {
			serThread = new SerialThread(pref.mySPO, gpsPos, (pref.forwardGPS ? pref.forwardGpsHost : ""));
			if (pref.forwardGPS && !serThread.tcpForward) {
				(new MessageBox("Warning", "Ignoring error:\n could not forward GPS data to host:\n"+pref.forwardGpsHost+"\n"+serThread.lastError+"\nstop and start GPS to retry",MessageBox.OKB)).exec();
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
			(new MessageBox("Error", "Could not connect to GPS-receiver.\n Error while opening serial Port " + e.getMessage()+"\npossible reasons:\n Another (GPS-)program is blocking the port\nwrong port\nOn Loox: active infra-red port is blocking GPS", MessageBox.OKB)).execute(); 
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
		if (gotoPanel != null) gotoPanel.gpsStoped();
		if (movingMap != null) movingMap.gpsStoped();
	}
	
	public boolean isGpsPosValid() {
		return 	serThread != null && serThread.isAlive() && gpsPos.isValid() ; // && gpsPos.getfiex();

	}


	public void setDestination(String LatLon) { 
		setDestination(new CWPoint(LatLon));
	}


	public void setDestination(CWPoint d) {
		destination = new CWPoint (d);
		if (gotoPanel != null) gotoPanel.destChanged(destination);
		if (movingMap != null) movingMap.destChanged(destination);
	}
	public void ticked() {
		int fix = gpsPos.getFix();
		if (fix > 0 && (gpsPos.getSats()>= 0)) {
			//gpsPosition.printAll();
			//Vm.debug("currTrack.add: voher");
			if (curTrack == null) curTrack = new Track(trackColor);
			try {
				curTrack.add(gpsPos);
			} catch (IndexOutOfBoundsException e) { // track full -> create a new one
				curTrack = new Track(trackColor); 
				curTrack.add(gpsPos);
				if (movingMap != null) movingMap.addTrack(curTrack); // TODO maybe gotoPanel should also hold a list of Tracks, because otherwise they will be destroyed if not saved in mmp before
			}
			try {
				sunAzimut = getSunAzimut(gpsPos.Time, gpsPos.Date, gpsPos.latDec, gpsPos.lonDec);
			} catch (NumberFormatException e) { // irgendeine Info zu Berechnung des Sonnenaziumt fehlt (insbesondere Datum und Uhrzeit sind nicht unbedingt gleichzeitig verfügbar wenn es einen Fix gibt)
				sunAzimut = -361; // any value out of range (bigger than 360) will prevent drawArrows from drawing it 
			}

		} else {
			sunAzimut = -361;
		}
		gotoPanel.updateGps(fix);
		if (movingMap != null) movingMap.updateGps(fix);
	}

	/**
	 * @param utc in the format as it comes from gps DDMMYY
	 * @param datum in the format as it comes from gps HHMMSS
	 * @param lat in degrees in WGS84
	 * @param lon in degrees in WGS84
	 * @return Azimut of the sun in degrees from north
	 * @throws NumberFormatException when utc / datum could not be interpreted
	 */
	public static float getSunAzimut (String utc, String datum, double lat, double lon) {
		//	(new MessageBox("test", "utc:"+utc+" datum: "+datum+", lat: "+lat+", len: "+lon, MessageBox.OKB)).exec();
		try {
			int tag, monat, jahr, stunde, minute, sekunde;
			tag = Convert.parseInt(datum.substring(0, 2));
			monat = Convert.parseInt(datum.substring(2, 4));
			jahr = Convert.parseInt(datum.substring(4, 6)) + 2000;
			stunde=Convert.parseInt(utc.substring(0, 2));
			minute=Convert.parseInt(utc.substring(2, 4));
			sekunde=Convert.parseInt(utc.substring(4, 6)); // Kommastellen werden abgeschnitten
			// julianisches "Datum" jd berechnen (see http://de.wikipedia.org/wiki/Julianisches_Datum )
			if (monat<2) {jahr--; monat+=12;} // verlegung des Jahres Endes auf Feb macht Berechnung von SChaltjahren einfacher
			double a = (int)java.lang.Math.floor((double)jahr/100.); // Alle hundert Jahre kein Schlatjahr (abrunden)
			double b = 2 - a + java.lang.Math.floor((double)a/4.);
			double jd = java.lang.Math.floor(365.25*(jahr + 4716.)) + java.lang.Math.floor(30.6001*((double)monat+1.)) + (double)tag + (double)stunde/24 + (double)minute/1440 + (double)sekunde/86400 + b - 1524.5;
			double jd0 = java.lang.Math.floor(365.25*(jahr + 4716.)) + java.lang.Math.floor(30.6001*((double)monat+1.)) +(double)tag + b - 1524.5;
			// Ekliptikalkoordinaten der Sonne berechnen (see http://de.wikipedia.org/wiki/Sonnenstand )
			double n = jd - 2451545.0;
			double l = 280.46 + 0.9856474 * n;
			double g = 357.528 + 0.9856003 * n;
			double d = l + 1.915*java.lang.Math.sin(g/180*java.lang.Math.PI) + 0.02 * java.lang.Math.sin(2*g/180*java.lang.Math.PI);
			// Rektaszension alpha und Deklination delta der Sonne berechnen
			double e = 23.439 -0.0000004 * n;
			double alphaNenner = java.lang.Math.cos(d/180*java.lang.Math.PI);
			double alpha = 180/java.lang.Math.PI*java.lang.Math.atan(java.lang.Math.cos(e/180*java.lang.Math.PI)*java.lang.Math.sin(d/180*java.lang.Math.PI)/alphaNenner);
			double delta = 180/java.lang.Math.PI*java.lang.Math.asin(java.lang.Math.sin(e/180*java.lang.Math.PI)*java.lang.Math.sin(d/180*java.lang.Math.PI) );
			if (alphaNenner<0) {alpha +=180;}
			// Azimut
			double t0 = (jd0 - 2451545.)/36525.; // schon in t0 bzw jd0 richtig berechnet?
			double thetaHG = 6.697376 + 2400.05134 * t0 + 1.002738 * ((double)stunde + (double)minute/60.);
			double theta = thetaHG * 15. + lon;
			double azimutNenner = java.lang.Math.cos((theta-alpha)/180*java.lang.Math.PI)*java.lang.Math.sin(lat/180*java.lang.Math.PI)-
			java.lang.Math.tan(delta/180*java.lang.Math.PI)*java.lang.Math.cos(lat/180*java.lang.Math.PI);
			float azimut = (float) java.lang.Math.atan(java.lang.Math.sin((theta-alpha)/180*java.lang.Math.PI)/
					azimutNenner);
			azimut = (float) (azimut * 180f / java.lang.Math.PI);
			if (azimutNenner<0) azimut +=180.;
			// null = Sueden auf Null = Norden umrechnen
			azimut +=180.;
			if (azimut >360.) azimut -=360.;
			return azimut;
		} catch (IndexOutOfBoundsException e) {
			// wird von substring geworfen wenn datum / utc nicht genug Ziffern haben
			// NumberFormatException wird außerdem von Convert.ParseInt direkt geworfen wenn
			// nicht in Int konvertiert werden kann
			throw new NumberFormatException();
		}
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
			} catch (InterruptedException e) {}
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
			try { sleep (calldelay);} catch (InterruptedException e) {}
			ticked.ticked();
		}
	}

	public void stop() {
		run = false;
	}
}

