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
	 * this is prgrammed acording to the algorithmus described in http://de.wikipedia.org/wiki/Sonnenstand
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
			double thetaHG = 6.697376 + 2400.05134 * t0 + 1.002738 * ((double)stunde + (double)minute/60. + (double)sekunde/3600.);
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
		/*	ewe.sys.Vm.debug("sunAzimut1: " + azimut);
			ewe.sys.Vm.debug("sun Elevation: " +getSunAzimut2 (utc, datum, lat, lon).latDec);
			CWPoint MoonDir = getMoonAzimut(jd, new CWPoint(lat, lon));
			ewe.sys.Vm.debug("Moon Elevation: " + MoonDir.latDec + "Moon Azimut: " + MoonDir.lonDec);
			CWPoint OrionDir = getOrionAzimut(jd, new CWPoint(lat, lon));
			ewe.sys.Vm.debug("Alnilam (Orion) Elevation: " + OrionDir.latDec + "Alnilam (Orion) Azimut: " + OrionDir.lonDec );
			*/
			return azimut;
		} catch (IndexOutOfBoundsException e) {
			// wird von substring geworfen wenn datum / utc nicht genug Ziffern haben
			// NumberFormatException wird außerdem von Convert.ParseInt direkt geworfen wenn
			// nicht in Int konvertiert werden kann
			throw new NumberFormatException();
		}
	}

	public static CWPoint getSunAzimut2 (String utc, String datum, double lat, double lon) {
		double jd = utc2juliandate(utc, datum);
		CWPoint eclCoos = getSunEclipticCoos(jd);
		// calculate ecliptic coos
		// convert coos
		return ecliptic2AzimutCoos(new CWPoint(lat, lon), jd, eclCoos);
	}
	
	public static CWPoint getMoonAzimut(double jd, CWPoint onEarth) {
		CWPoint eclCoo = getMoonEclipticCoos(jd);
		return ecliptic2AzimutCoos(onEarth, jd, eclCoo);
	}
	
	public static CWPoint getOrionAzimut(double jd, CWPoint onEarth) {
		// Koordinaten Alnilam (mittlerer Gürtelstern des Orion), Rektaszension 5h36m13s; Deklination -1°12'7 TODO Äquinoktium 2000
		// Source: wikipedia
		return equatorial2AzimutCoos(onEarth, jd, new CWPoint(-1. -12./60. -7./3600., (5. + 36./60. + 13./3600.)*15.) ); // (-1. -12./60. -7./3600., (5. + 36./60. + 13./3600.)*15.) <- wikipedia // -1.19748, 5.60978 * 15.) <- www.... // (-1. -11./60. -52./3600., (5. + 36./60. + 35./3600.)*15.)  <- Stellarium
	}
	
	/**
	 * get the ecliptic coordinates of the sun
	 * @param juliandate
	 * @return
	 */
	public static CWPoint getSunEclipticCoos(double juliandate) {
		double n = juliandate - 2451545.0;
		double l = 280.46 + 0.9856474 * n;
		double g = 357.528 + 0.9856003 * n;
		double lambda = l + 1.915*java.lang.Math.sin(g/180*java.lang.Math.PI) + 0.02 * java.lang.Math.sin(2*g/180*java.lang.Math.PI);
		return new CWPoint(0, lambda);
	}
	
	
	// the following code is adopted from http://lexikon.astronomie.info/java/sunmoon/sunmoon.html
	// ignores the time difference between juliandate and TDT, which is something like 1 minute
	public static CWPoint getMoonEclipticCoos(double julianDate) {
		final double DEG = Math.PI / 180;  
		final double RAD = 1/DEG;
		double sunAnomalyMean = 360*DEG/365.242191*(julianDate - 2447891.5) + 279.403303*DEG - 282.768422*DEG;
		double D = julianDate-2447891.5;

		// Mean Moon orbit elements as of 1990.0
		double l0 = 318.351648*DEG;
		double P0 =  36.340410*DEG;
		double N0 = 318.510107*DEG;
		double i  = 5.145396*DEG;

		double l = 13.1763966*DEG*D+l0;
		double MMoon = l-0.1114041*DEG*D-P0; // Moon's mean anomaly M
		double N = N0-0.0529539*DEG*D;       // Moon's mean ascending node longitude
		
		double sunlon = getSunEclipticCoos(julianDate).lonDec; 
		double C = l-sunlon;
		double Ev = 1.2739*DEG*Math.sin(2*C-MMoon);
		double Ae = 0.1858*DEG*Math.sin(sunAnomalyMean);
		double A3 = 0.37*DEG*Math.sin(sunAnomalyMean);

		double MMoon2 = MMoon+Ev-Ae-A3;  // corrected Moon anomaly
		double Ec = 6.2886*DEG*Math.sin(MMoon2);  // equation of centre
		double A4 = 0.214*DEG*Math.sin(2*MMoon2);
		double l2 = l+Ev+Ec-Ae+A4; // corrected Moon's longitude
		double V = 0.6583*DEG*Math.sin(2*(l2-sunlon));

		double l3 = l2+V; // true orbital longitude;
		double N2 = N-0.16*DEG*Math.sin(sunAnomalyMean);

		CWPoint moonCoor = new CWPoint();  
		moonCoor.lonDec = (( N2 + Math.atan2( Math.sin(l3-N2)*Math.cos(i), Math.cos(l3-N2) ) ) * RAD)% 360;
		moonCoor.latDec = Math.asin( Math.sin(l3-N2)*Math.sin(i) ) * RAD;
		//moonCoor.orbitLon = l3;
		return moonCoor;
		
		/*
		double e  = 0.054900;
		double a  = 384401; // km
		double diameter0 = 0.5181*DEG; // angular diameter of Moon at a distance
		double parallax0 = 0.9507*DEG; // parallax at distance a

		  // relative distance to semi mayor axis of lunar oribt
		  moonCoor.distance = (1-sqr(e)) / (1+e*Math.cos(MMoon2+Ec) );
		  moonCoor.diameter = diameter0/moonCoor.distance; // angular diameter in radians
		  moonCoor.parallax = parallax0/moonCoor.distance; // horizontal parallax in radians
		  moonCoor.distance *= a;	// distance in km

		  // Age of Moon in radians since New Moon (0) - Full Moon (pi)
		  moonCoor.moonAge = Mod2Pi(l3-sunCoor.lon);   
		  moonCoor.phase   = 0.5*(1-Math.cos(moonCoor.moonAge)); // Moon phase, 0-1

		  var phases = new Array("Neumond", "Zunehmende Sichel", "Erstes Viertel", "Zunnehmender Mond", 
		  	"Vollmond", "Abnehmender Mond", "Letztes Viertel", "Abnehmende Sichel", "Neumond");
		  var mainPhase = 1./29.53*360*DEG; // show 'Newmoon, 'Quarter' for +/-1 day arond the actual event
		  var p = Mod(moonCoor.moonAge, 90.*DEG);
		  if (p < mainPhase || p > 90*DEG-mainPhase) p = 2*Math.round(moonCoor.moonAge / (90.*DEG));
		  else p = 2*Math.floor(moonCoor.moonAge / (90.*DEG))+1;
		  moonCoor.moonPhase = phases[p];

		  moonCoor.sign = Sign(moonCoor.lon);
		  return (float) moonCoor.lonDec;
		return 0;
	}
		 */
	}

		public static CWPoint ecliptic2AzimutCoos(CWPoint onEarth, double julianDate, CWPoint ecliptic) {
			CWPoint equat = ecliptic2Equatorial(ecliptic, julianDate);
			return equatorial2AzimutCoos(onEarth, julianDate, equat);
		}
		/**
		 * convert rektaszension alpha and deklination delta to azimut
		 * @param onEarth pos. on earth for which the azimut is wanted
		 * @param julianDate
		 * @param equatorial: lonDec = rektaszension (alpha), latDec = Deklination (delta)
		 * @return lonDec: azimut in degrees from north, lat: elevation in degrees from horizont
		 * alogithism from wikipedia sonnenbahn
		 */
		public static CWPoint equatorial2AzimutCoos(CWPoint onEarth, double julianDate, CWPoint equatorial) {
			double stunde = ((julianDate + 0.5) % 1) * 24;
			double jd0 = julianDate - stunde /24; // julian date at UTC 0:00
			double t0 = (jd0 - 2451545.)/36525.; // schon in t0 bzw jd0 richtig berechnet?
			double thetaHG = 6.697376 + 2400.05134 * t0 + 1.002738 * stunde; // + (double)minute/60.);
			double theta = thetaHG * 15. + onEarth.lonDec;
			double tau = (theta - equatorial.lonDec ) /180*Math.PI;
			double phi = onEarth.latDec/180*Math.PI;
			double azimutNenner = Math.cos(tau)*Math.sin(phi)-
				Math.tan(equatorial.latDec/180*Math.PI)*Math.cos(onEarth.latDec/180*java.lang.Math.PI);
			float azimut = (float) java.lang.Math.atan(java.lang.Math.sin((theta-equatorial.lonDec)/180*Math.PI)/
					azimutNenner);
			azimut = (float) (azimut * 180f / java.lang.Math.PI);
			if (azimutNenner<0) azimut +=180.;
			// null = Sueden auf Null = Norden umrechnen
			azimut +=180.;
			if (azimut >360.) azimut -=360.;
			double h = 180 / Math.PI * Math.asin(Math.cos(equatorial.latDec/180*Math.PI) * Math.cos(tau)*Math.cos(phi) + Math.sin(equatorial.latDec/180 *Math.PI) * Math.sin(phi));
			return new CWPoint(h, azimut);
		}

		/**
		 * convert from eliptical to equatorial coordinates
		 * @param juliandate
		 * @param eklipCoo ecliptic coos in degrees  
		 * @return lon: Deklination (delta), lat: Rektaszension (alpha) in degree
		 * this is adopted from http://lexikon.astronomie.info/java/sunmoon/sunmoon.html 
		 */
		public static CWPoint ecliptic2Equatorial(CWPoint eklipCoo, double juliandate) {
			double T = (juliandate - 2451545.0)/36525.; // Epoch 2000 January 1.5
			double eps = (23.+(26+21.45/60)/60 + T*(-46.815 +T*(-0.0006 + T*0.00181) )/3600 ) / 180 * java.lang.Math.PI; // schiefe der Ekliptik
			double coseps = Math.cos(eps);
			double sineps = Math.sin(eps);

			double sinlon = Math.sin(eklipCoo.lonDec / 180 * Math.PI);
			CWPoint equatorial = new CWPoint();
			equatorial.lonDec = (180 / Math.PI * Math.atan2( (sinlon*coseps-Math.tan(eklipCoo.latDec /180 * Math.PI)*sineps), Math.cos(eklipCoo.lonDec/180 * Math.PI) ) ) % 360; // rektaszension (alpha)
			equatorial.latDec = 180 / Math.PI * Math.asin( Math.sin(eklipCoo.latDec/180 * Math.PI)*coseps + Math.cos(eklipCoo.latDec/180 * Math.PI)*sineps*sinlon ); // deklination (delta)

			return equatorial;
		}
		
		/**
		 * @param utc in the format as it comes from gps DDMMYY
		 * @param datum in the format as it comes from gps HHMMSS
		 * @return juliandate
		 * @throws NumberFormatException if utc / datum could not be parsed successfully
		 */
		public static double utc2juliandate(String utc, String datum) {
			try {
				int tag, monat, jahr, stunde, minute, sekunde;
				tag     = Convert.parseInt(datum.substring(0, 2));
				monat   = Convert.parseInt(datum.substring(2, 4));
				jahr    = Convert.parseInt(datum.substring(4, 6)) + 2000;
				stunde  = Convert.parseInt(utc.substring(0, 2));
				minute  = Convert.parseInt(utc.substring(2, 4));
				sekunde = Convert.parseInt(utc.substring(4, 6)); // Kommastellen werden abgeschnitten
				// julianisches "Datum" jd berechnen (see http://de.wikipedia.org/wiki/Julianisches_Datum )
				if (monat<2) {jahr--; monat+=12;} // verlegung des Jahres Endes auf Feb macht Berechnung von SChaltjahren einfacher
				double a = (int)java.lang.Math.floor((double)jahr/100.); // Alle hundert Jahre kein Schlatjahr (abrunden)
				double b = 2 - a + java.lang.Math.floor((double)a/4.);
				double jd = java.lang.Math.floor(365.25*(jahr + 4716.)) + java.lang.Math.floor(30.6001*((double)monat+1.)) + (double)tag + (double)stunde/24 + (double)minute/1440 + (double)sekunde/86400 + b - 1524.5;
				return jd;
				//double jd0 = java.lang.Math.floor(365.25*(jahr + 4716.)) + java.lang.Math.floor(30.6001*((double)monat+1.)) +(double)tag + b - 1524.5;
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

