package cachewolf.navi;

import eve.ui.*;
import eve.fx.*;
import eve.sys.*;
import java.lang.Double;

import cachewolf.CWPoint;
import cachewolf.CacheHolder;
import cachewolf.CoordsScreen;
import cachewolf.Global;
import cachewolf.MainTab;
import cachewolf.MyLocale;
import cachewolf.Preferences;

import eve.ui.event.MenuEvent;
import eve.ui.event.ControlEvent;

/**
 *	Class to create the panel which handles the connection to the GPS-device<br>
 *	Displays: current position,speed and bearing; relation to destination waypoint<br>
 *	Class ID: 1500
 */


public class GotoPanel extends CellPanel {

	//public CWGPSPoint gpsPosition = new CWGPSPoint();
	//public CWPoint toPoint = new CWPoint();
	public Navigate myNavigation;
	Button btnGPS, btnCenter,btnSave;
	Button btnGoto, btnMap;
	int currFormat;

	Label lblGPS, lblPosition, lblDST;
	Color gpsStatus;

	MainTab mainT;
	//Vector cacheDB;
	//DetailsPanel detP;

	Preferences pref;
	//Profile profile;
	// different panels to avoid spanning
	CellPanel ButtonP = new CellPanel();
	CellPanel CoordsP = new CellPanel();
	CellPanel roseP = new CellPanel();

	//ImageControl icRose;
	GotoRose compassRose;

	final static Color RED = new Color(255,0,0);
	final static Color YELLOW = new Color(255,255,0);
	final static Color GREEN = new Color(0,255,0);
	final static Color BLUE = new Color(0,0,255);

	final static Font BOLD = new Font("Arial", Font.BOLD, 14);

	//int ticker = 0;

	Menu mnuContextFormt;
	MenuItem miDMM, miDMS, miDD, miUTM, miGK;

	Menu mnuContextRose;
	MenuItem miLuminary[] = new MenuItem[SkyOrientation.LUMINARY_NAMES.length];
	MenuItem miNorthCentered;

	/**
	 * Create GotoPanel
	 * @param Preferences 	global preferences
	 * @param MainTab		reference to MainTable
	 * @param DetailsPanel 	reference to DetailsPanel
	 * @param Vector		cacheDB
	 */
	public GotoPanel(Navigate nav) {
		myNavigation = nav;
		pref = Global.getPref();
		mainT = Global.mainTab;
		//detP = mainT.detP;
		//cacheDB = profile.cacheDB;

		// Button
		ButtonP.addNext(btnGPS = new Button(MyLocale.getMsg(1504,"Start")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		ButtonP.addNext(btnCenter = new Button(MyLocale.getMsg(309,"Centre")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		ButtonP.addNext(btnSave = new Button(MyLocale.getMsg(311,"Create Waypoint")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		ButtonP.addLast(btnMap = new Button(MyLocale.getMsg(1506,"Map")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));

		//Format selection for coords
		//context menu
		mnuContextFormt = new Menu();
		mnuContextFormt.addItem(miDD = new MenuItem("d.d°"));
		miDD.modifiers &= ~MenuItem.Checked;
		mnuContextFormt.addItem(miDMM = new MenuItem("d°m.m\'"));
		miDMM.modifiers |= MenuItem.Checked;
		mnuContextFormt.addItem(miDMS = new MenuItem("d°m\'s\""));
		miDMS.modifiers &= ~MenuItem.Checked;
		mnuContextFormt.addItem(miUTM = new MenuItem("UTM"));
		miUTM.modifiers &= ~MenuItem.Checked;
		mnuContextFormt.addItem(miGK = new MenuItem("GK"));
		miGK.modifiers &= ~MenuItem.Checked;
		currFormat = CWPoint.DMM;

		// Create context menu for compass rose: select luminary for orientation
		mnuContextRose = new Menu();
		for (int i=0; i<SkyOrientation.LUMINARY_NAMES.length; i++) {
			mnuContextRose.addItem(miLuminary[i] = new MenuItem(SkyOrientation.getLuminaryName(i)));
			if (i == myNavigation.luminary) miLuminary[i].modifiers |= MenuItem.Checked;
			else miLuminary[i].modifiers &= MenuItem.Checked;
		}

		//Coords
		CoordsP.addNext(lblGPS = new Label("GPS: "),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		lblGPS.backGround = RED;
		lblGPS.setMenu(mnuContextFormt);
		lblGPS.modifyAll(Control.WantHoldDown, 0);

		CoordsP.addLast(lblPosition = new Label(myNavigation.gpsPos.toString(currFormat)),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		lblPosition.setMenu(mnuContextFormt);
		lblPosition.anchor = Label.CENTER;
		lblPosition.modifyAll(Control.WantHoldDown, 0);

		CoordsP.addNext(lblDST = new Label(MyLocale.getMsg(1500,"DST:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		lblDST.backGround = new Color(0,0,255);
		lblDST.setMenu(mnuContextFormt);
		lblDST.modifyAll(Control.WantHoldDown, 0);

		CoordsP.addLast(btnGoto = new Button(getGotoBtnText()),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));

		//Rose for bearing
		//compassRose = new GotoRose("rose.png");
		compassRose = new GotoRose();
		compassRose.setMenu(mnuContextRose);
		compassRose.modifyAll(Control.WantHoldDown, 0); // this is necessary in order to make PenHold on a PDA work as right click
		roseP.addLast(compassRose,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.NORTH|CellConstants.WEST));

		mnuContextRose.addItem(new MenuItem("", MenuItem.Separator, null));
		mnuContextRose.addItem(miNorthCentered = new MenuItem(MyLocale.getMsg(1503,"North Centered")));
		if (compassRose.isNorthCentered()) miNorthCentered.modifiers |= MenuItem.Checked;
		else miNorthCentered.modifiers &= MenuItem.Checked;

		//add Panels
		this.addLast(ButtonP,CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.WEST); //.setTag(TAG_SPAN,new Dimension(2,1));
		this.addLast(CoordsP,CellConstants.HSTRETCH, CellConstants.HFILL|CellConstants.NORTH); //.setTag(TAG_SPAN,new Dimension(2,1));
		this.addLast(roseP,CellConstants.DONTSTRETCH, CellConstants.DONTFILL|CellConstants.NORTH|CellConstants.WEST); //.setTag(TAG_SPAN,new Dimension(2,1));

		// for debuging
		/*		CWGPSPoint myGPS;
		myGPS = new CWGPSPoint();
		String ex = new String();
		boolean test = false;
		try { FileReader f = new FileReader("c:\\kw\\20060928_1051.log");
		ex = f.readAll();
		test = myGPS.examine(ex);
		f.close();
		} catch (IOException e ) {
			Vm.debug(e.toString());
		}
		test = myGPS.examine("$GPGLL,5226.8935,N,01338.5327,E,084635.00,A,D*6E");
		test = myGPS.examine("$GPGSA,A,3,10,28,26,29,09,,,,,,,,04.1,02.6,03.2*0F");
		test = myGPS.examine("$GPGSV,4,1,13,08,40,072,28,10,29,201,47,27,15,079,29,28,62,102,44*7E");
		test = myGPS.examine("$GPGSV,4,2,13,29,72,289,38,26,63,296,41,09,12,259,35,18,14,324,*79");
		test = myGPS.examine("$GPGSV,4,3,13,19,09,025,,17,06,138,,21,06,300,,37,29,171,40*7A");
		test = myGPS.examine("$GPGSV,4,4,13,39,29,166,38*40");
		 */

		//while (true){
//		int notinterpreted = 0;
//		if (myGPS.examine("@ööH @ööHö@ÖÖHHÜÄÜÖÄÄÄH")) { notinterpreted = 0;} else notinterpreted++;
//		if (notinterpreted > 5) myGPS.noInterpretableData();
//		// myGPS.noInterpretableData();


//		}
	}

	public void resizeTo(int width, int height){
		super.resizeTo(width, height);
		Rect coordsRect = CoordsP.getRect();
		int roseHeight = height - coordsRect.y - coordsRect.height;
		if (Gui.screenIs(Gui.PDA_SCREEN) && Device.isMobile()) {
			//some space for the SIP button
			if ( (Vm.getParameter(Vm.VM_FLAGS) & (Vm.VM_FLAG_SIP_BUTTON_ON_SCREEN)) == (Vm.VM_FLAG_SIP_BUTTON_ON_SCREEN) ){
				roseHeight -= MyLocale.getScreenHeight() / 14;
			}
		}
		roseP.resizeTo(width, roseHeight);
		compassRose.resizeTo(width, roseHeight);
	}


	/**
	 * set the coords of the destination
	 * @param dest destination
	 */
	public void setDestination(CWPoint dest){
		myNavigation.setDestination(dest);
		if (!myNavigation.destination.isValid()) (new MessageBox(MyLocale.getMsg(321,"Error"), MyLocale.getMsg(1507,"Coordinates are out of range:") +"\n"+MyLocale.getMsg(1508,"latitude")+": "+myNavigation.destination.latDec+"\n "+MyLocale.getMsg(1509,"longditue")+": "+myNavigation.destination.lonDec, MessageBox.OKB)).execute();

	}

	public void destChanged(CWPoint d) { // called from myNavigate
		btnGoto.setText(getGotoBtnText());
		updateDistance();
	}


	/**
	 * set the coords of the destination and switch to gotoPanel
	 * @param latLon destination
	 */
	public void setDestinationAndSwitch(CWPoint where) {
		myNavigation.setDestination(where);
		mainT.select(this);
	}

	/**
	 * updates distance and bearing
	 *
	 */

	public void updateDistance() {
		//update distance
		float distance = -1.0f;
		if (myNavigation.gpsPos.isValid() && myNavigation.destination.isValid() ) {
			distance = (float)myNavigation.gpsPos.getDistance(myNavigation.destination);
		}
		compassRose.setWaypointDirectionDist((float)myNavigation.gpsPos.getBearing(myNavigation.destination), distance);
	}

	/**
	 * method which is called if a timer is set up
	 */
	public void updateGps(int fix) {
		//Vm.debug("ticked: before");
		compassRose.setGpsStatus(fix, myNavigation.gpsPos.getSats(), myNavigation.gpsPos.getSatsInView(), myNavigation.gpsPos.getHDOP());
		if ((fix > 0) && (myNavigation.gpsPos.getSats()>= 0)) {
			Double bearMov;
			Double speed;
			Double sunAzimut;
			// display values only, if signal good
			//Vm.debug("currTrack.add: nachher");
			lblPosition.setText(myNavigation.gpsPos.toString(currFormat));
			speed=new Double(myNavigation.gpsPos.getSpeed());
			sunAzimut=new Double(myNavigation.skyOrientationDir.lonDec);
			bearMov=new Double(myNavigation.gpsPos.getBear());
			updateDistance();
			compassRose.setSunMoveDirections((float)sunAzimut.doubleValue(), (float)bearMov.doubleValue(), (float)speed.doubleValue());
			// Set background to signal quality
		}

		// receiving data, but signal ist not good
		if ((fix == 0) && (myNavigation.gpsPos.getSats()>= 0)) {
			gpsStatus = YELLOW;
		}
		// receiving no data
		if (fix == -1) {
			if (gpsStatus != RED) (new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1510, "No data from GPS\nConnection to serial port closed"),MessageBox.OKB)).exec();
			gpsStatus = RED;
			myNavigation.stopGps();
		}
		// cannot interprete data
		if (fix == -2) {
			if (gpsStatus != RED) (new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1511, "Cannot interpret data from GPS\n possible reasons:\n wrong Port,\n wrong Baudrate,\n not NMEA-Protocol\nConnection to serial port closed\nLast String tried to interprete:\n")+myNavigation.gpsPos.lastStrExamined, MessageBox.OKB)).exec();
			gpsStatus = RED;
			myNavigation.stopGps(); // TODO automatic in myNavigate?
		}
	}

	public void gpsStarted() {
		btnGPS.setText(MyLocale.getMsg(1505,"Stop"));
	}

	public void startGps() {
		myNavigation.startGps(pref.logGPS, Convert.toInt(pref.logGPSTimer));
	}

	public void gpsStopped() {
		btnGPS.setText(MyLocale.getMsg(1504,"Start"));
		gpsStatus = this.backGround;
		this.repaintNow(); // without this the change in the background color will not be displayed
	}


	private String getGotoBtnText() {
		if (myNavigation.destination == null)
			return MyLocale.getMsg(999,"Not set");
		return myNavigation.destination.toString(currFormat);
	}

	public void switchToMovingMap() {
		CWPoint centerTo=null;
		if (myNavigation.isGpsPosValid()) centerTo = new CWPoint(myNavigation.gpsPos); // set gps-pos if gps is on
		else {
			// setze Zielpunkt als Ausgangspunkt, wenn GPS aus ist und lade entsprechende Karte
			//centerTo = new CWPoint(myNavigation.destination);
			if (myNavigation.destination.isValid())	centerTo = new CWPoint(myNavigation.destination);
			else {
				if (mainT.ch != null && mainT.ch.pos.isValid()) centerTo = new CWPoint(mainT.ch.pos);
				else {
					if (pref.curCentrePt.isValid()) centerTo = new CWPoint(pref.curCentrePt);
					else {
					}
				}
			}
		}
		if (centerTo != null && centerTo.isValid())
			mainT.switchToMovingMap(centerTo, false);
		else
			(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1513, "Cannot start moving map without valid coordinates. Please enter coordinates as destination, as center, in selected cache or start GPS"), MessageBox.OKB)).execute();
	}

	/**
	 * Eventhandler
	 */

	public void onEvent(Event ev){
		if (ev instanceof MenuEvent) {
			if (ev.type == MenuEvent.SELECTED ) {
				MenuItem action = (MenuItem) mnuContextFormt.getSelectedItem();
				if (action != null) {
					if (action == miDD) {
						mnuContextFormt.close();
						currFormat = CWPoint.DD;
					}
					if (action == miDMM) {
						mnuContextFormt.close();
						currFormat = CWPoint.DMM;
					}
					if (action == miDMS) {
						mnuContextFormt.close();
						currFormat = CWPoint.DMS;
					}
					if (action == miUTM) {
						mnuContextFormt.close();
						currFormat = CWPoint.UTM;
					}
					if (action == miGK) {
						mnuContextFormt.close();
						currFormat = CWPoint.GK;
					}
					miDD.modifiers &= ~MenuItem.Checked;
					miDMM.modifiers &= ~MenuItem.Checked;
					miDMS.modifiers &= ~MenuItem.Checked;
					miUTM.modifiers &= ~MenuItem.Checked;
					miGK.modifiers &= ~MenuItem.Checked;
					switch (currFormat) {
					case CWPoint.DD: miDD.modifiers |= MenuItem.Checked; break;
					case CWPoint.DMM: miDMM.modifiers |= MenuItem.Checked; break;
					case CWPoint.DMS: miDMS.modifiers |= MenuItem.Checked; break;
					case CWPoint.UTM: miUTM.modifiers |= MenuItem.Checked; break;
					case CWPoint.GK: miGK.modifiers |= MenuItem.Checked; break;
					}

					lblPosition.setText(myNavigation.gpsPos.toString(currFormat));
					btnGoto.setText(getGotoBtnText());
				} // end lat-lon-format context menu
				action = (MenuItem) mnuContextRose.getSelectedItem();
				if (action != null) {
					for (int i=0; i<miLuminary.length; i++) {
						if (action == miLuminary[i]) {
							myNavigation.setLuminary(i);
							miLuminary[i].modifiers |= MenuItem.Checked;
							compassRose.setLuminaryName(SkyOrientation.getLuminaryName(myNavigation.luminary));
						} else miLuminary[i].modifiers &= ~MenuItem.Checked;
					}
					if (action == miNorthCentered) {
						if (compassRose.isNorthCentered()) {
							compassRose.setNorthCentered(false);
							miNorthCentered.modifiers &= ~MenuItem.Checked;
						}
						else
						{
							compassRose.setNorthCentered(true);
							miNorthCentered.modifiers |= MenuItem.Checked;
						}
					}
				}
			}
		}

		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			// start/stop GPS connection
			if (ev.target == btnGPS){
				if (btnGPS.getText().equals(MyLocale.getMsg(1504, "Start"))) startGps();
				else myNavigation.stopGps();
			}

			// set current position as centre and recalculate distance of caches in MainTab
			if (ev.target == btnCenter){
				if (myNavigation.gpsPos.isValid()) {
					Form.showWait();
					pref.curCentrePt.set(myNavigation.gpsPos);
					mainT.updateBearDist();
					Form.cancelWait();
				} else (new MessageBox(MyLocale.getMsg(312, "Error"), MyLocale.getMsg(1514, "Cannot recalculate distances, because the GPS position is not set"), MessageBox.OKB)).execute();
			}
			//Start moving map
			if (ev.target == btnMap){
				switchToMovingMap();
			}
			// create new waypoint with current GPS-position
			if (ev.target == btnSave){
				CacheHolder ch = new CacheHolder();
				ch.latLon = myNavigation.gpsPos.toString();
				ch.pos = new CWPoint(myNavigation.gpsPos);
				ch.type = 51; // see CacheType.GC_AW_STAGE_OF_MULTI // TODO unfertig
				mainT.newWaypoint(ch);
			}
			// change destination waypoint
			if (ev.target == btnGoto){
				CoordsScreen cs = new CoordsScreen();
				if (myNavigation.destination.isValid())	cs.setFields(myNavigation.destination, currFormat);
				else cs.setFields(new CWPoint(0,0), currFormat);
				if (cs.execute(null, Gui.TOP) == CoordsScreen.IDOK)
					setDestination(cs.getCoords());
			}
		}
		super.onEvent(ev);
	}
}

/** class for displaying the compass rose
 * including goto, sun and moving direction
 */
class GotoRose extends Panel {
	//TODO Too many objects are created
	//TOTO Store the sine and cosine in a variable
	float gotoDir = -361;
	float sunDir = -361;
	float moveDir = -361;
	float distance = -1;

	int m_fix = -1;
	int m_sats = -1;
	int m_satsInView = 0;
	double m_hdop = -1;
	float m_speed = -1;

	String m_Luminary = MyLocale.getMsg(6100, "Sun");

	Font mainFont;
	FontMetrics fm;
	int lineHeight;

	int roseRadius;

	boolean northCentered = Global.getPref().northCenteredGoto;

	final static Color RED = new Color(255,0,0);
	final static Color YELLOW = new Color(255,255,0);
	final static Color GREEN = new Color(0,255,0);
	final static Color BLUE = new Color(0,0,255);
	final static Color ORANGE = new Color(255,128,0);
	final static Color DARKGREEN = new Color(0,192,0);
	final static Color CYAN = new Color(0,255,255);
	final static Color MAGENTA = new Color(255,0,255);

	/**
	 * @param gd goto direction
	 * @param sd sun direction
	 * @param md moving direction
	 */
	//public GotoRose(String fn){
	//}

	public GotoRose(){
		super();
	}

	public void setWaypointDirectionDist(float wd, float dist) {
		gotoDir = wd;
		distance = dist;
	}

	public void setSunMoveDirections(float sd, float md, float speed ) {
		sunDir = sd;
		moveDir = md;
		m_speed = speed;
		repaint();
	}

	public void setGpsStatus(int fix, int sats, int satsInView, double hdop) {
		m_fix = fix;
		m_sats = sats;
		m_satsInView = satsInView;
		m_hdop = hdop;
		repaint();
	}

	public void setLuminaryName(String Luminary) {
		m_Luminary = Luminary;
		repaint();
	}


	/**
	 * draw arrows for the directions of movement and destination waypoint
	 * @param ctrl the control to paint on
	 * @param moveDir degrees of movement
	 * @param destDir degrees of destination waypoint
	 */

	public void doPaint(Graphics g, Rect area)  {
		g.setColor(Color.White);
		g.fillRect(0, 0, this.width, this.height);

		int fontSize = this.width/17;
		mainFont = new Font("Verdana", Font.BOLD, fontSize);
		g.setFont(mainFont);
		fm = g.getFontMetrics(mainFont);
		lineHeight = fm.getHeight() + 1;
		roseRadius = java.lang.Math.min((this.width * 3) / 4, this.height) / 2;

		if (northCentered) {
			drawFullRose(g, 0, 				// angle
					Color.White, 			// colLeft
					new Color(200,200,200), // colRight
					Color.White, 			// colNorthLeft
					new Color(200,200,200), // colNorthRight
					new Color(150,150,150), // colBorder
					new Color(75,75,75),    // colText
					1.0f,					// scale
					true, 					// bDrawText
					true);					// bDrawEightArrows
		}
		else {
			int radius = (int)(roseRadius * 0.75f);
			Pen oldPen = g.getPen(Pen.getCached());
			g.changePen(new Color(150,150,150),Pen.SOLID,3);
			//g.setPen(new Pen(new Color(150,150,150),Pen.SOLID,3));
			g.drawEllipse(this.width/2 - radius, this.height/2 - radius, 2 * radius, 2 * radius );
			g.set(oldPen);
			oldPen.cache();
		}

		drawArrows(g);
		drawWayPointData(g);
		drawGpsData(g);
		drawLuminaryData(g);
		drawGpsStatus(g);
	}

	private void drawWayPointData(Graphics g){
		String strTemp = MyLocale.getMsg(1512, "Waypoint");
		g.setColor(Color.DarkBlue);
		g.fillRect(0, 0, fm.getTextWidth(strTemp) + 4, lineHeight);
		g.setColor(Color.White);
		g.drawText(strTemp, 2, 0);

		g.setColor(Color.Black);

		if ( distance >= 0.0f ) {
			if (distance >= 1){
				strTemp = MyLocale.formatDouble(distance,"0.000")+ " km";
			} else {
				strTemp = MyLocale.formatDouble(distance*1000.0,"0") + " m";
			}
		}
		else strTemp = "--- km";
		g.drawText(strTemp, 2, lineHeight);

		if ((gotoDir <= 360) && (gotoDir >= -360))
			strTemp = MyLocale.formatDouble(gotoDir,"0") + " " + MyLocale.getMsg(1502,"deg");
		else
			strTemp = "---" + " " + MyLocale.getMsg(1502,"deg");
		g.drawText(strTemp, 2, 2*lineHeight);
	}

	private void drawGpsData(Graphics g){
		g.setColor(RED);

		String strHeadline = MyLocale.getMsg(1501,"Current");

		Double tmp = new Double(m_speed);
		String strSpeed = "- km/h";
		if (m_speed >= 0) {
			if (m_speed >= 100) {
				strSpeed = MyLocale.formatDouble(tmp,"0") + " km/h";
			}
			else {
				strSpeed = MyLocale.formatDouble(tmp,"0.0") + " km/h";
			}
		}

		tmp = new Double(moveDir);
		String strMoveDir = "---" + " " + MyLocale.getMsg(1502,"deg");
		if ((tmp.doubleValue() <= 360) && (tmp.doubleValue() >= -360))
			strMoveDir = tmp.toString() + " " + MyLocale.getMsg(1502,"deg");

		int textWidth = java.lang.Math.max(fm.getTextWidth(strSpeed), fm.getTextWidth(strMoveDir));
		textWidth = java.lang.Math.max(textWidth, fm.getTextWidth(strHeadline));

		int startX = this.width - (textWidth + 4);
		g.fillRect(startX, 0, this.width - startX, lineHeight);

		g.setColor(Color.Black);
		g.drawText(strHeadline, startX + 2, 0);
		g.drawText(strSpeed, startX + 2, lineHeight);
		g.drawText(strMoveDir, startX + 2, 2*lineHeight);
	}

	private void drawLuminaryData(Graphics g){
		g.setColor(YELLOW);

		String strSunDir = "---" + " " + MyLocale.getMsg(1502,"deg");
		if (sunDir < 360 && sunDir > -360) {
			strSunDir = MyLocale.formatDouble(sunDir, "0") + " " + MyLocale.getMsg(1502,"deg");
		}

		int textWidth = java.lang.Math.max(fm.getTextWidth(m_Luminary), fm.getTextWidth(strSunDir));
		int startY = this.height - 2*lineHeight;
		g.fillRect(0, startY, textWidth + 4, this.height - startY);

		g.setColor(Color.Black);
		g.drawText(m_Luminary, 2, startY);
		g.drawText(strSunDir, 2, startY + lineHeight);
	}

	private void drawGpsStatus(Graphics g){
		if ((m_fix > 0) && (m_sats >= 0)) {
			// Set background to signal quality
			g.setColor(GREEN);
		}
		else
		// receiving data, but signal ist not good
		if ((m_fix == 0) && (m_sats >= 0)) {
			g.setColor(YELLOW);
		}
		else {
			g.setColor(RED);
		}

		String strSats = "Sats: -";
		if (m_sats >= 0) {
			strSats = "Sats: " + Convert.toString(m_sats) + "/" + Convert.toString(m_satsInView);
		}
		String strHdop = "HDOP: -";
		if (m_hdop >= 0) strHdop = "HDOP: " + Convert.toString(m_hdop);

		int textWidth = java.lang.Math.max(fm.getTextWidth(strSats), fm.getTextWidth(strHdop));
		int startX = this.width - (textWidth + 4);
		int startY = this.height - 2*lineHeight;
		g.fillRect(startX, startY, this.width - startX ,this.height - startY);

		g.setColor(Color.Black);
		g.drawText(strSats, startX + 2, startY);
		g.drawText(strHdop, startX + 2, startY + lineHeight);
	}

	private void drawArrows(Graphics g){
		if (g != null)
		{
			// select moveDirColor according to difference to gotoDir
			Color moveDirColor = RED;

			if (gotoDir < 360 && gotoDir > -360 && moveDir < 360 && moveDir > -360)
			{
				float diff = java.lang.Math.abs(moveDir - gotoDir);
				while (diff > 360)
				{
					diff -= 360.0f;
				}
				if (diff > 180.0f)
				{
					diff = 360.0f - diff;
				}

				if (diff <= 12.25f)
				{
					moveDirColor = GREEN;
				}
				else if (diff <= 22.5f)
				{
					moveDirColor = CYAN;
				}
				else if (diff <= 45.0f)
				{
					moveDirColor = ORANGE;
				}
				else if (diff <= 90.0f)
				{
					moveDirColor = MAGENTA;
				}
			}

			// draw only valid arrows
			if (northCentered) {
				if (gotoDir < 360 && gotoDir > -360) drawThickArrow(g, gotoDir, Color.DarkBlue, 1.0f);
				if (moveDir < 360 && moveDir > -360) drawThinArrow(g, moveDir, RED, moveDirColor, 1.0f);
				if (sunDir < 360 && sunDir > -360) drawSunArrow(g, sunDir, YELLOW, 0.75f);
			}
			else {
				//moveDir centered
				if (moveDir < 360 && moveDir > -360) {
					//drawDoubleArrow(g, 360 - moveDir, BLUE, new Color(175,0,0), 1.0f);
					//drawRose(g, 360 - moveDir, new Color(100,100,100), new Color(200,200,200), 1.0f);
					drawFullRose(g, 360 - moveDir, new Color(255,255,255), new Color(200,200,200), new Color(150,150,150), new Color(200,200,200), new Color(200,200,200), new Color(75,75,75), 1.0f, false, false);

					int radius = (int)(roseRadius * 0.75f);
					g.changePen(RED,Pen.SOLID,3);
					g.drawLine(this.width/2, this.height/2 - radius, this.width/2, this.height/2 + radius);

					if (gotoDir < 360 && gotoDir > -360) drawThinArrow(g, gotoDir - moveDir, Color.DarkBlue, moveDirColor, 1.0f);
					if (sunDir < 360 && sunDir > -360) drawSunArrow(g, sunDir - moveDir, YELLOW, 0.75f);
				}
			}
		}
	}

	/**
	 * draw single arrow
	 * @param g handle for drawing
	 * @param angle angle of arrow
	 * @param col color of arrow
	 */
/*	private void drawSimpleArrow(Graphics g, float angle, Color col, float scale) {
		float angleRad;
		int x, y, centerX = this.width/2, centerY = this.height/2;
		int arrowLength = roseRadius;

		angleRad = (angle) * (float)java.lang.Math.PI / 180;
		x = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad) * scale).intValue();
		y = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad) * scale).intValue();
		g.setPen(new Pen(col,Pen.SOLID,3));
		g.drawLine(centerX,centerY,x,y);
	}
*/
	private void drawSunArrow(Graphics g, float angle, Color col, float scale) {
		float angleRad = (angle) * (float)java.lang.Math.PI / 180;
		int centerX = this.width/2, centerY = this.height/2;
		float arrowLength = roseRadius * scale;
		float halfArrowWidth = arrowLength * 0.08f;
		float circlePos = arrowLength * 0.7f;
		int circleRadius = (int)(arrowLength * 0.1f);

		//int pointX = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad)).intValue();
		//int pointY = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad)).intValue();
		int circleX = centerX + (int) (circlePos * java.lang.Math.sin(angleRad));
		int circleY = centerY - (int) (circlePos * java.lang.Math.cos(angleRad));

		int[] pointsX = new int[4];
		int[] pointsY = new int[4];

		pointsX[0] = centerX + (int)(arrowLength * java.lang.Math.sin(angleRad));
		pointsY[0] = centerY - (int)(arrowLength * java.lang.Math.cos(angleRad));
		pointsX[1] = centerX + (int)(halfArrowWidth * java.lang.Math.sin(angleRad + java.lang.Math.PI / 2.0));
		pointsY[1] = centerY - (int)(halfArrowWidth * java.lang.Math.cos(angleRad + java.lang.Math.PI / 2.0));
		pointsX[2] = centerX + (int)(halfArrowWidth * java.lang.Math.sin(angleRad + java.lang.Math.PI));
		pointsY[2] = centerY - (int)(halfArrowWidth * java.lang.Math.cos(angleRad + java.lang.Math.PI));
		pointsX[3] = centerX + (int)(halfArrowWidth * java.lang.Math.sin(angleRad - java.lang.Math.PI / 2.0));
		pointsY[3] = centerY - (int)(halfArrowWidth * java.lang.Math.cos(angleRad - java.lang.Math.PI / 2.0));

//		g.setPen(new Pen(col,Pen.SOLID,3));
//		g.drawLine(centerX,centerY,pointX,pointY);

		g.changePen(Color.Black,Pen.SOLID,1);
		g.changeBrush(col, Brush.SOLID);
		g.fillPolygon(pointsX, pointsY, 4);
		g.fillEllipse(circleX - circleRadius, circleY - circleRadius, 2 * circleRadius, 2 * circleRadius);
	}

	private void drawThinArrow(Graphics g, float angle, Color col, Color colPoint, float scale) {
		float angleRad = (angle) * (float)java.lang.Math.PI / 180;
		int centerX = this.width/2, centerY = this.height/2;
		float arrowLength = roseRadius * scale;
		float halfOpeningAngle = (float)(java.lang.Math.PI * 0.03);
		float sideLineLength = arrowLength * 0.75f;

		int[] pointsX = new int[4];
		int[] pointsY = new int[4];

		pointsX[0] = centerX + (int)(sideLineLength * java.lang.Math.sin(angleRad - halfOpeningAngle));
		pointsY[0] = centerY - (int)(sideLineLength * java.lang.Math.cos(angleRad - halfOpeningAngle));
		pointsX[1] = centerX + (int)(arrowLength * java.lang.Math.sin(angleRad));
		pointsY[1] = centerY - (int)(arrowLength * java.lang.Math.cos(angleRad));
		pointsX[2] = centerX + (int)(sideLineLength * java.lang.Math.sin(angleRad + halfOpeningAngle));
		pointsY[2] = centerY - (int)(sideLineLength * java.lang.Math.cos(angleRad + halfOpeningAngle));
		pointsX[3] = centerX;
		pointsY[3] = centerY;

		g.changePen(Color.Black,Pen.SOLID,1);
		g.changeBrush(col, Brush.SOLID);
		g.fillPolygon(pointsX, pointsY, 4);
		if (colPoint != null) {
			g.changeBrush(colPoint, Brush.SOLID);
			g.fillPolygon(pointsX, pointsY, 3);
		}
	}

/*	private void drawDoubleArrow(Graphics g, float angle, Color colFront, Color colRear, float scale) {
		float angleRad = (angle) * (float)java.lang.Math.PI / 180;
		int centerX = this.width/2, centerY = this.height/2;
		float arrowLength = (float)roseRadius * scale;
		float halfArrowWidth = arrowLength * 0.1f;

		int[] pointsX = new int[3];
		int[] pointsY = new int[3];

		pointsX[0] = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad)).intValue();
		pointsY[0] = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad)).intValue();
		pointsX[1] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad + java.lang.Math.PI / 2.0)).intValue();
		pointsY[1] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad + java.lang.Math.PI / 2.0)).intValue();
		pointsX[2] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad - java.lang.Math.PI / 2.0)).intValue();
		pointsY[2] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad - java.lang.Math.PI / 2.0)).intValue();

		g.setPen(new Pen(Color.Black,Pen.SOLID,1));
		g.setBrush(new Brush(colFront, Brush.SOLID));
		g.fillPolygon(pointsX, pointsY, 3);

		pointsX[0] = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad + java.lang.Math.PI)).intValue();
		pointsY[0] = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad + java.lang.Math.PI)).intValue();

		g.setBrush(new Brush(colRear, Brush.SOLID));
		g.fillPolygon(pointsX, pointsY, 3);
	}
*/
/*	private void drawRose(Graphics g, float angle, Color colFront, Color colRear, float scale) {
		float angleRad = (angle) * (float)java.lang.Math.PI / 180;
		int centerX = this.width/2, centerY = this.height/2;
		float arrowLength = (float)roseRadius * scale;
		float halfArrowWidth = arrowLength * 0.12f;

		int[] pointsX = new int[8];
		int[] pointsY = new int[8];

		pointsX[0] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad - java.lang.Math.PI / 4.0)).intValue();
		pointsY[0] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad - java.lang.Math.PI / 4.0)).intValue();
		pointsX[1] = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad)).intValue();
		pointsY[1] = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad)).intValue();
		pointsX[2] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad + java.lang.Math.PI / 4.0)).intValue();
		pointsY[2] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad + java.lang.Math.PI / 4.0)).intValue();
		pointsX[3] = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad + java.lang.Math.PI / 2.0)).intValue();
		pointsY[3] = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad + java.lang.Math.PI / 2.0)).intValue();
		pointsX[4] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad + 3.0 * java.lang.Math.PI / 4.0)).intValue();
		pointsY[4] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad + 3.0 * java.lang.Math.PI / 4.0)).intValue();
		pointsX[5] = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad + java.lang.Math.PI)).intValue();
		pointsY[5] = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad + java.lang.Math.PI)).intValue();
		pointsX[6] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad - 3.0 * java.lang.Math.PI / 4.0)).intValue();
		pointsY[6] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad - 3.0 * java.lang.Math.PI / 4.0)).intValue();
		pointsX[7] = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad - java.lang.Math.PI / 2.0)).intValue();
		pointsY[7] = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad - java.lang.Math.PI / 2.0)).intValue();

		g.setPen(new Pen(colRear,Pen.SOLID,1));
		g.setBrush(new Brush(colRear, Brush.SOLID));
		g.fillPolygon(pointsX, pointsY, 8);

		g.setBrush(new Brush(colFront, Brush.SOLID));
		g.fillPolygon(pointsX, pointsY, 3);
	}
*/
	private void drawFullRose(Graphics g, float angle, Color colLeft, Color colRight, Color colNorthLeft, Color colNorthRight,
			Color colBorder, Color colText, float scale, boolean bDrawText, boolean bDrawEightArrows) {
		float subScale1 = 1.0f;
		float subScale2 = 0.9f;
		float innerScale = 0.15f;
		if(bDrawEightArrows){
			innerScale = 0.12f;
			drawRosePart(g,  45 + angle, colLeft, colRight, colBorder, colText, scale * subScale2, innerScale, "NE", bDrawText);
			drawRosePart(g, 135 + angle, colLeft, colRight, colBorder, colText, scale * subScale2, innerScale, "SE", bDrawText);
			drawRosePart(g, 225 + angle, colLeft, colRight, colBorder, colText, scale * subScale2, innerScale, "SW", bDrawText);
			drawRosePart(g, 315 + angle, colLeft, colRight, colBorder, colText, scale * subScale2, innerScale, "NW", bDrawText);
		}

		drawRosePart(g,   0 + angle, colNorthLeft, colNorthRight, colBorder, colText, scale * subScale1, innerScale, "N", bDrawText);
		drawRosePart(g,  90 + angle, colLeft, colRight, colBorder, colText, scale * subScale1, innerScale, "E", bDrawText);
		drawRosePart(g, 180 + angle, colLeft, colRight, colBorder, colText, scale * subScale1, innerScale, "S", bDrawText);
		drawRosePart(g, 270 + angle, colLeft, colRight, colBorder, colText, scale * subScale1, innerScale, "W", bDrawText);
	}

	private void drawRosePart(Graphics g, float angle, Color colLeft, Color colRight, Color colBorder, Color colText, float scale, float innerScale, String strDir, boolean bDrawText) {
		float angleRad = angle * (float)java.lang.Math.PI / 180;
		float angleRadText = (angle + 7.5f) * (float)java.lang.Math.PI / 180;
		int centerX = this.width/2, centerY = this.height/2;

		float arrowLength = roseRadius * scale;
		float halfArrowWidth = arrowLength * innerScale;

		int[] pointsX = new int[3];
		int[] pointsY = new int[3];

		pointsX[0] = centerX;
		pointsY[0] = centerY;
		pointsX[1] = centerX + (int)(arrowLength * java.lang.Math.sin(angleRad));
		pointsY[1] = centerY - (int)(arrowLength * java.lang.Math.cos(angleRad));
		pointsX[2] = centerX + (int)(halfArrowWidth * java.lang.Math.sin(angleRad - java.lang.Math.PI / 4.0));
		pointsY[2] = centerY - (int)(halfArrowWidth * java.lang.Math.cos(angleRad - java.lang.Math.PI / 4.0));

		g.changePen(colBorder,Pen.SOLID,1);
		g.changeBrush(colLeft, Brush.SOLID);
		g.paintPolygon(pointsX, pointsY, 3);

		pointsX[2] = centerX + (int)(halfArrowWidth * java.lang.Math.sin(angleRad + java.lang.Math.PI / 4.0));
		pointsY[2] = centerY - (int)(halfArrowWidth * java.lang.Math.cos(angleRad + java.lang.Math.PI / 4.0));

		g.changeBrush(colRight, Brush.SOLID);
		g.paintPolygon(pointsX, pointsY, 3);

		if (bDrawText){
			int tempFontSize = (int)(scale * mainFont.getSize());
			Font tempFont = new Font(mainFont.getName(), Font.BOLD, tempFontSize);
			g.setFont(tempFont);
			FontMetrics tempFm = g.getFontMetrics(tempFont);
			float stringHeight = tempFm.getHeight();
			float stringWidth = tempFm.getTextWidth( strDir );
			float stringGap = (float)java.lang.Math.sqrt(stringHeight*stringHeight + stringWidth*stringWidth);

			float stringPosition = arrowLength - stringGap / 2.0f;
			g.setColor(colText);
			g.drawText(strDir, centerX + (int)(stringPosition * java.lang.Math.sin(angleRadText) - stringWidth / 2.0f),
					           centerY - (int)(stringPosition * java.lang.Math.cos(angleRadText) + stringHeight / 2.0f));

			g.setFont(mainFont);
		}
	}

	private void drawThickArrow(Graphics g, float angle, Color col, float scale) {
		float angleRad = (angle) * (float)java.lang.Math.PI / 180;
		int centerX = this.width/2, centerY = this.height/2;
		float arrowLength = roseRadius * scale;
		float halfArrowWidth = arrowLength * 0.1f;

		int[] pointsX = new int[4];
		int[] pointsY = new int[4];

		pointsX[0] = centerX + (int)(arrowLength * java.lang.Math.sin(angleRad));
		pointsY[0] = centerY - (int)(arrowLength * java.lang.Math.cos(angleRad));
		pointsX[1] = centerX + (int)(halfArrowWidth * java.lang.Math.sin(angleRad + java.lang.Math.PI / 2.0));
		pointsY[1] = centerY - (int)(halfArrowWidth * java.lang.Math.cos(angleRad + java.lang.Math.PI / 2.0));
		pointsX[2] = centerX + (int)(halfArrowWidth * java.lang.Math.sin(angleRad + java.lang.Math.PI));
		pointsY[2] = centerY - (int)(halfArrowWidth * java.lang.Math.cos(angleRad + java.lang.Math.PI));
		pointsX[3] = centerX + (int)(halfArrowWidth * java.lang.Math.sin(angleRad - java.lang.Math.PI / 2.0));
		pointsY[3] = centerY - (int)(halfArrowWidth * java.lang.Math.cos(angleRad - java.lang.Math.PI / 2.0));

		g.changePen(Color.Black,Pen.SOLID,1);
		g.changeBrush(col, Brush.SOLID);
		g.fillPolygon(pointsX, pointsY, 4);
	}

	public void setNorthCentered(boolean nc) {
		northCentered = nc;
		if (northCentered != Global.getPref().northCenteredGoto) {
			Global.getPref().northCenteredGoto = northCentered;
			Global.getPref().savePreferences();
		}
		repaint();
	}

	public boolean isNorthCentered() {
		return northCentered;
	}
}
