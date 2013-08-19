/*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
	See http://www.cachewolf.de/ for more information.
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

import CacheWolf.CWPoint;
import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.CacheType;
import CacheWolf.CoordsScreen;
import CacheWolf.DetailsPanel;
import CacheWolf.Global;
import CacheWolf.InputScreen;
import CacheWolf.MainTab;
import CacheWolf.MyLocale;
import ewe.fx.Brush;
import ewe.fx.Color;
import ewe.fx.Dimension;
import ewe.fx.Font;
import ewe.fx.FontMetrics;
import ewe.fx.Graphics;
import ewe.fx.Pen;
import ewe.fx.Rect;
import ewe.graphics.AniImage;
import ewe.sys.Convert;
import ewe.sys.Double;
import ewe.sys.Vm;
import ewe.sys.VmConstants;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.FormBase;
import ewe.ui.Gui;
import ewe.ui.ImageControl;
import ewe.ui.Menu;
import ewe.ui.MenuEvent;
import ewe.ui.MenuItem;
import ewe.ui.MessageBox;
import ewe.ui.Window;
import ewe.ui.WindowConstants;
import ewe.ui.mButton;
import ewe.ui.mLabel;

/**
 * Class to create the panel which handles the connection to the GPS-device<br>
 * Displays: current position,speed and bearing; relation to destination waypoint<br>
 * Class ID: 1500
 */

public final class GotoPanel extends CellPanel {

	// public CWGPSPoint gpsPosition = new CWGPSPoint();
	// public CWPoint toPoint = new CWPoint();
	public Navigate myNavigation;
	mButton btnGPS, btnCenter, btnSave;
	mButton btnGoto, btnMap;
	int currFormatSel;

	mLabel lblGPS, lblPosition, lblDST;
	Color gpsStatus;

	MainTab mainT;
	CacheDB cacheDB;
	DetailsPanel detP;

	// different panels to avoid spanning
	CellPanel HeadP = new CellPanel();
	CellPanel ButtonP = new CellPanel();
	CellPanel CoordsP = new CellPanel();
	CellPanel roseP = new CellPanel();

	ImageControl icRose;
	GotoRose compassRose;

	final static Color RED = new Color(255, 0, 0);
	final static Color YELLOW = new Color(255, 255, 0);
	final static Color GREEN = new Color(0, 255, 0);
	final static Color BLUE = new Color(0, 0, 255);

	Menu mnuContextFormt;
	MenuItem miCooformat[] = new MenuItem[TransformCoordinates.localSystems.length + 3]; // miDMM, miDMS, miDD, miUTM, miGK;

	Menu mnuContextRose;
	MenuItem miLuminary[] = new MenuItem[SkyOrientation.LUMINARY_NAMES.length];
	MenuItem miNorthCentered;

	/**
	 * Create GotoPanel
	 * 
	 * @param Preferences
	 *            global preferences
	 * @param MainTab
	 *            reference to MainTable
	 * @param DetailsPanel
	 *            reference to DetailsPanel
	 * @param Vector
	 *            cacheDB
	 */
	public GotoPanel(Navigate nav) {
		myNavigation = nav;
		mainT = Global.mainTab;
		detP = mainT.detP;
		cacheDB = Global.profile.cacheDB;

		// Button
		ButtonP.addNext(btnGPS = new mButton(MyLocale.getMsg(1504, "Start")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		ButtonP.addNext(btnCenter = new mButton(MyLocale.getMsg(309, "Centre")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		ButtonP.addLast(btnSave = new mButton(MyLocale.getMsg(311, "Create Waypoint")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		// ButtonP.addLast(btnMap = new mButton(MyLocale.getMsg(1506,"Map")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));

		// Format selection for coords
		// context menu
		mnuContextFormt = new Menu();
		currFormatSel = 1; // default to d° m.m
		mnuContextFormt.addItem(miCooformat[0] = new MenuItem("d.d°"));
		miCooformat[0].modifiers &= ~MenuItem.Checked;
		mnuContextFormt.addItem(miCooformat[1] = new MenuItem("d°m.m\'"));
		miCooformat[1].modifiers |= MenuItem.Checked; // default
		mnuContextFormt.addItem(miCooformat[2] = new MenuItem("d°m\'s\""));
		miCooformat[2].modifiers &= ~MenuItem.Checked;
		mnuContextFormt.addItems(TransformCoordinates.getProjectedSystemNames());

		// Create context menu for compass rose: select luminary for orientation
		mnuContextRose = new Menu();
		for (int i = 0; i < SkyOrientation.LUMINARY_NAMES.length; i++) {
			mnuContextRose.addItem(miLuminary[i] = new MenuItem(SkyOrientation.getLuminaryName(i)));
			if (i == myNavigation.luminary)
				miLuminary[i].modifiers |= MenuItem.Checked;
			else
				miLuminary[i].modifiers &= MenuItem.Checked;
		}

		// Coords
		CoordsP.addNext(lblGPS = new mLabel("GPS: "), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		lblGPS.backGround = RED;
		lblGPS.setMenu(mnuContextFormt);
		lblGPS.modifyAll(ControlConstants.WantHoldDown, 0);

		lblPosition = new mLabel(myNavigation.gpsPos.toString(CoordsScreen.getLocalSystem(currFormatSel)));
		lblPosition.anchor = CellConstants.CENTER;
		lblPosition.setMenu(mnuContextFormt);
		lblPosition.modifyAll(ControlConstants.WantHoldDown, 0);
		CoordsP.addLast(lblPosition, CellConstants.HSTRETCH, (CellConstants.HFILL | CellConstants.WEST));

		CoordsP.addNext(lblDST = new mLabel(MyLocale.getMsg(1500, "DST:")), CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.WEST));
		lblDST.backGround = new Color(0, 0, 255);
		lblDST.setMenu(mnuContextFormt);
		lblDST.modifyAll(ControlConstants.WantHoldDown, 0);

		CoordsP.addLast(btnGoto = new mButton(getGotoBtnText()), CellConstants.HSTRETCH, (CellConstants.HFILL | CellConstants.WEST));

		// Rose for bearing
		// compassRose = new GotoRose("rose.png");
		compassRose = new GotoRose();
		icRose = new ImageControl(compassRose);
		icRose.setMenu(mnuContextRose);
		icRose.modifyAll(ControlConstants.WantHoldDown, 0); // this is necessary in order to make PenHold on a PDA work as right click
		roseP.addLast(icRose, CellConstants.DONTSTRETCH, (CellConstants.DONTFILL | CellConstants.NORTH));

		mnuContextRose.addItem(new MenuItem("", MenuItem.Separator, null));
		mnuContextRose.addItem(miNorthCentered = new MenuItem(MyLocale.getMsg(1503, "North Centered")));
		if (compassRose.isNorthCentered())
			miNorthCentered.modifiers |= MenuItem.Checked;
		else
			miNorthCentered.modifiers &= MenuItem.Checked;

		// add Panels
		HeadP.addLast(ButtonP, CellConstants.HSTRETCH, CellConstants.DONTFILL | CellConstants.WEST).setTag(SPAN, new Dimension(2, 1));
		HeadP.addLast(CoordsP, CellConstants.HSTRETCH, CellConstants.HFILL | CellConstants.NORTH).setTag(SPAN, new Dimension(2, 1));
		this.addNext(HeadP, CellConstants.HSTRETCH, CellConstants.WEST).setTag(SPAN, new Dimension(2, 1));
		this.addLast(btnMap = new mButton(MyLocale.getMsg(1506, "Map") + " "), CellConstants.HSTRETCH, CellConstants.VFILL | CellConstants.RIGHT).setTag(SPAN, new Dimension(2, 1));
		this.addLast(roseP, CellConstants.DONTSTRETCH, CellConstants.DONTFILL | CellConstants.WEST).setTag(SPAN, new Dimension(2, 1));
		btnMap.backGround = GREEN;
	}

	public void resizeTo(int pWidth, int pHeight) {
		super.resizeTo(pWidth, pHeight);
		Rect coordsRect = CoordsP.getRect();
		int roseHeight = pHeight - coordsRect.y - coordsRect.height;
		if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
			// some space for the SIP button
			if ((Vm.getParameter(VmConstants.VM_FLAGS) & (VmConstants.VM_FLAG_SIP_BUTTON_ON_SCREEN)) == (VmConstants.VM_FLAG_SIP_BUTTON_ON_SCREEN)) {
				Rect screen = (Rect) Window.getGuiInfo(WindowConstants.INFO_SCREEN_RECT, null, new Rect(), 0);
				roseHeight -= screen.height / 14;
			}
		}
		roseP.resizeTo(pWidth, roseHeight);
		icRose.resizeTo(pWidth, roseHeight);
		compassRose.resize(pWidth, roseHeight);
	}

	/**
	 * set the coords of the destination
	 * 
	 * @param dest
	 *            destination
	 */
	public void setDestination(CWPoint dest) {
		myNavigation.setDestination(dest);
		if (!myNavigation.destination.isValid())
			(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1507, "Coordinates are out of range:") + "\n" + MyLocale.getMsg(1508, "latitude") + ": " + myNavigation.destination.latDec + "\n " + MyLocale.getMsg(1509, "longditue") + ": "
					+ myNavigation.destination.lonDec, FormBase.OKB)).execute();

	}

	public void destChanged(CWPoint d) { // called from myNavigate
		btnGoto.setText(getGotoBtnText());
		updateDistance();
	}

	/**
	 * set the coords of the destination and switch to gotoPanel
	 * 
	 * @param LatLon
	 *            destination
	 */
	public void setDestinationAndSwitch(CWPoint where) {
		myNavigation.setDestination(where);
		mainT.select(this);
	}

	public void setDestinationAndSwitch(CacheHolder ch) {
		myNavigation.setDestination(ch);
		mainT.select(this);
	}

	/**
	 * updates distance and bearing
	 * 
	 */

	public void updateDistance() {
		// update distance
		float distance = -1.0f;
		if (myNavigation.gpsPos.isValid() && myNavigation.destination.isValid()) {
			distance = (float) myNavigation.gpsPos.getDistance(myNavigation.destination);
		}
		compassRose.setWaypointDirectionDist((float) myNavigation.gpsPos.getBearing(myNavigation.destination), distance);
	}

	/**
	 * method which is called if a timer is set up
	 */
	public void updateGps(int fix) {
		Double bearMov = new Double();
		Double speed = new Double();
		Double sunAzimut = new Double();
		compassRose.setGpsStatus(fix, myNavigation.gpsPos.getSats(), myNavigation.gpsPos.getSatsInView(), myNavigation.gpsPos.getHDOP());
		if ((fix > 0) && (myNavigation.gpsPos.getSats() >= 0)) {
			// display values only, if signal good
			lblPosition.setText(myNavigation.gpsPos.toString(CoordsScreen.getLocalSystem(currFormatSel)));
			speed.set(myNavigation.gpsPos.getSpeed());
			sunAzimut.set(myNavigation.skyOrientationDir.lonDec);
			bearMov.set(myNavigation.gpsPos.getBear());
			updateDistance();
			compassRose.setSunMoveDirections((float) sunAzimut.value, (float) bearMov.value, (float) speed.value);
			// Set background to signal quality
		}

		// receiving data, but signal ist not good
		if ((fix == 0) && (myNavigation.gpsPos.getSats() >= 0)) {
			gpsStatus = YELLOW;
		}
		// receiving no data
		if (fix == -1) {
			if (gpsStatus != RED)
				(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1510, "No data from GPS.\nConnection to serial port/gpsd closed."), FormBase.OKB)).exec();
			gpsStatus = RED;
			myNavigation.stopGps();
		}
		// cannot interpret data
		if (fix == -2) {
			if (gpsStatus != RED)
				(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1511,
						"Cannot interpret data from GPS/gpsd!\nPossible reasons:\nWrong port,\nwrong baud rate,\ninvalid protocol (need NMEA/gpsd).\nConnection to serial port closed.\nLast String tried to interpret:\n")
						+ myNavigation.gpsPos.lastStrExamined, FormBase.OKB)).exec();
			gpsStatus = RED;
			myNavigation.stopGps(); // TODO automatic in myNavigate?
		}
	}

	public void gpsStarted() {
		btnGPS.setText(MyLocale.getMsg(1505, "Stop"));
	}

	public void startGps() {
		myNavigation.startGps(Global.pref.logGPS, Convert.toInt(Global.pref.logGPSTimer));
	}

	public void gpsStoped() {
		btnGPS.setText(MyLocale.getMsg(1504, "Start"));
		gpsStatus = this.backGround;
		this.repaintNow(); // without this the change in the background color will not be displayed
	}

	private String getGotoBtnText() {
		if (myNavigation.destination == null)
			return MyLocale.getMsg(999, "Not set");
		else
			return myNavigation.destination.toString(CoordsScreen.getLocalSystem(currFormatSel));
	}

	public void switchToMovingMap() {
		CWPoint centerTo = null;
		if (myNavigation.isGpsPosValid())
			centerTo = new CWPoint(myNavigation.gpsPos); // set gps-pos if gps is on
		else {
			// setze Zielpunkt als Ausgangspunkt, wenn GPS aus ist und lade entsprechende Karte
			// centerTo = new CWPoint(myNavigation.destination);
			if (myNavigation.destination.isValid())
				centerTo = new CWPoint(myNavigation.destination);
			else {
				if (mainT.ch != null && mainT.ch.getPos().isValid())
					centerTo = new CWPoint(mainT.ch.getPos());
				else {
					if (Global.pref.getCurCentrePt().isValid())
						centerTo = new CWPoint(Global.pref.getCurCentrePt());
				}
			}
		}
		if (centerTo != null && centerTo.isValid())
			mainT.SwitchToMovingMap(centerTo, false);
		else
			(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(1513, "Cannot start moving map without valid coordinates. Please enter coordinates as destination, as center, in selected cache or start GPS"), FormBase.OKB)).execute();
	}

	/**
	 * Eventhandler
	 */
	public void onEvent(Event ev) {
		if (ev instanceof MenuEvent) {
			if (ev.type == MenuEvent.SELECTED) {
				if (((MenuEvent) ev).menu == mnuContextFormt) {
					mnuContextFormt.close();
					mnuContextFormt.getItemAt(currFormatSel).modifiers &= ~MenuItem.Checked;
					currFormatSel = mnuContextFormt.getInt();
					mnuContextFormt.getItemAt(currFormatSel).modifiers |= MenuItem.Checked;
					lblPosition.setText(myNavigation.gpsPos.toString(CoordsScreen.getLocalSystem(currFormatSel)));
					btnGoto.setText(getGotoBtnText());
				} // end lat-lon-format context menu
				if (((MenuEvent) ev).menu == mnuContextRose) {
					MenuItem action = (MenuItem) mnuContextRose.getSelectedItem();
					if (action != null) {
						for (int i = 0; i < miLuminary.length; i++) {
							if (action == miLuminary[i]) {
								myNavigation.setLuminary(i);
								miLuminary[i].modifiers |= MenuItem.Checked;
								compassRose.setLuminaryName(SkyOrientation.getLuminaryName(myNavigation.luminary));
							}
							else
								miLuminary[i].modifiers &= ~MenuItem.Checked;
						}
						if (action == miNorthCentered) {
							if (compassRose.isNorthCentered()) {
								compassRose.setNorthCentered(false);
								miNorthCentered.modifiers &= ~MenuItem.Checked;
							}
							else {
								compassRose.setNorthCentered(true);
								miNorthCentered.modifiers |= MenuItem.Checked;
							}
						}
					}
				}
			}
		}

		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
			// start/stop GPS connection
			if (ev.target == btnGPS) {
				if (btnGPS.getText().equals(MyLocale.getMsg(1504, "Start")))
					startGps();
				else
					myNavigation.stopGps();
			}

			// set current position as centre and recalculate distance of caches in MainTab
			if (ev.target == btnCenter) {
				if (myNavigation.gpsPos.isValid()) {
					Global.pref.setCurCentrePt(myNavigation.gpsPos);
				}
				else
					(new MessageBox(MyLocale.getMsg(312, "Error"), MyLocale.getMsg(1514, "Cannot recalculate distances, because the GPS position is not set"), FormBase.OKB)).execute();
			}
			// Start moving map
			if (ev.target == btnMap) {
				switchToMovingMap();
			}
			// create new waypoint with current GPS-position
			if (ev.target == btnSave) {
				CacheHolder ch = new CacheHolder();
				ch.setPos(myNavigation.gpsPos);
				ch.setType(CacheType.CW_TYPE_STAGE); // see CacheType.GC_AW_STAGE_OF_MULTI // TODO unfertig
				mainT.newWaypoint(ch);
			}
			// change destination waypoint
			if (ev.target == btnGoto) {
				if (Vm.isMobile()) {
					InputScreen InScr = new InputScreen(CoordsScreen.getLocalSystem(currFormatSel));
					if (myNavigation.destination.isValid())
						InScr.setCoords(myNavigation.destination);
					else
						InScr.setCoords(new CWPoint(0, 0));
					if (InScr.execute(null, CellConstants.TOP) == FormBase.IDOK)
						setDestination(InScr.getCoords());
				}
				else {
					CoordsScreen cs = new CoordsScreen();
					if (myNavigation.destination.isValid())
						cs.setFields(myNavigation.destination, CoordsScreen.getLocalSystem(currFormatSel));
					else
						cs.setFields(new CWPoint(0, 0), CoordsScreen.getLocalSystem(currFormatSel));
					if (cs.execute(null, CellConstants.TOP) == FormBase.IDOK)
						setDestination(cs.getCoords());
				}

			}
		}
		super.onEvent(ev);
	}
}

/**
 * class for displaying the compass rose
 * including goto, sun and moving direction
 */
class GotoRose extends AniImage {
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

	boolean northCentered = Global.pref.northCenteredGoto;

	final static Color RED = new Color(255, 0, 0);
	final static Color YELLOW = new Color(255, 255, 0);
	final static Color GREEN = new Color(0, 255, 0);
	final static Color BLUE = new Color(0, 0, 255);
	final static Color ORANGE = new Color(255, 128, 0);
	final static Color DARKGREEN = new Color(0, 192, 0);
	final static Color CYAN = new Color(0, 255, 255);
	final static Color MAGENTA = new Color(255, 0, 255);

	/**
	 * @param gd
	 *            goto direction
	 * @param sd
	 *            sun direction
	 * @param md
	 *            moving direction
	 */
	public GotoRose(String fn) {
		super(fn);
	}

	public GotoRose() {
		super();
	}

	public void setWaypointDirectionDist(float wd, float dist) {
		gotoDir = wd;
		distance = dist;
	}

	public void setSunMoveDirections(float sd, float md, float speed) {
		sunDir = sd;
		moveDir = md;
		m_speed = speed;
		refresh();
	}

	public void setGpsStatus(int fix, int sats, int satsInView, double hdop) {
		m_fix = fix;
		m_sats = sats;
		m_satsInView = satsInView;
		m_hdop = hdop;
		refresh();
	}

	public void setLuminaryName(String Luminary) {
		m_Luminary = Luminary;
		refresh();
	}

	/**
	 * draw arrows for the directions of movement and destination waypoint
	 * 
	 * @param ctrl
	 *            the control to paint on
	 * @param moveDir
	 *            degrees of movement
	 * @param destDir
	 *            degrees of destination waypoint
	 */

	public void doDraw(Graphics g, int options) {
		g.setColor(Color.White);
		g.fillRect(0, 0, location.width, location.height);

		int fontSize = location.width / 17;
		mainFont = GetCorrectedFont(g, "Verdana", Font.BOLD, fontSize);
		g.setFont(mainFont);
		fm = g.getFontMetrics(mainFont);
		lineHeight = fm.getHeight() + 1;
		roseRadius = java.lang.Math.min((location.width * 3) / 4, location.height) / 2;

		if (northCentered) {
			// scale(location.width, location.height, null, 0);
			// super.doDraw(g, options);
			drawFullRose(g, 0, new Color(255, 255, 255), new Color(200, 200, 200), new Color(255, 255, 255), new Color(200, 200, 200), new Color(150, 150, 150), new Color(75, 75, 75), 1.0f, true, true);
		}
		else {
			int radius = (int) (roseRadius * 0.75f);

			g.setPen(new Pen(new Color(150, 150, 150), Pen.SOLID, 3));
			g.drawEllipse(location.width / 2 - radius, location.height / 2 - radius, 2 * radius, 2 * radius);
		}

		drawArrows(g);
		drawWayPointData(g);
		drawGpsData(g);
		drawLuminaryData(g);
		drawGpsStatus(g);
	}

	private void drawWayPointData(Graphics g) {
		String strTemp = MyLocale.getMsg(1512, "Waypoint");
		g.setColor(Color.DarkBlue);
		g.fillRect(0, 0, fm.getTextWidth(strTemp) + 4, lineHeight);
		g.setColor(Color.White);
		g.drawText(strTemp, 2, 0);

		g.setColor(Color.Black);

		int metricSystem = Global.pref.metricSystem;
		Double tmp = new Double();
		strTemp = "";
		double newDistance = 0;
		int bigUnit = -1;
		int smallUnit = -1;
		double threshold = -1;
		// Allow for different metric systems
		if (metricSystem == Metrics.IMPERIAL) {
			// Why these values? See: http://tinyurl.com/b4nn9m
			bigUnit = Metrics.MILES;
			smallUnit = Metrics.FEET;
			threshold = 0.1;
			newDistance = Metrics.convertUnit(distance, Metrics.KILOMETER, Metrics.MILES);
		}
		else {
			bigUnit = Metrics.KILOMETER;
			smallUnit = Metrics.METER;
			threshold = 1.0;
			newDistance = distance;
		}
		if (newDistance >= 0.0f) {
			tmp.set(newDistance);
			if (tmp.value >= threshold) {
				strTemp = MyLocale.formatDouble(tmp, "0.000") + " " + Metrics.getUnit(bigUnit);
			}
			else {
				tmp.set(Metrics.convertUnit(tmp.value, bigUnit, smallUnit));
				strTemp = tmp.toString(3, 0, 0) + " " + Metrics.getUnit(smallUnit);
			}
		}
		else
			strTemp = "--- " + Metrics.getUnit(bigUnit);
		g.drawText(strTemp, 2, lineHeight);

		tmp.set(gotoDir);
		if ((tmp.value <= 360) && (tmp.value >= -360))
			strTemp = tmp.toString(0, 0, 0) + " " + MyLocale.getMsg(1502, "deg");
		else
			strTemp = "---" + " " + MyLocale.getMsg(1502, "deg");
		g.drawText(strTemp, 2, 2 * lineHeight);
	}

	private void drawGpsData(Graphics g) {
		g.setColor(RED);

		String strHeadline = MyLocale.getMsg(1501, "Current");

		Double tmp = new Double();

		String strSpeed = null;
		String unit = null;

		// Allow for different metric systems
		if (Global.pref.metricSystem == Metrics.IMPERIAL) {
			tmp.set(Metrics.convertUnit(m_speed, Metrics.KILOMETER, Metrics.MILES));
			unit = " mph";
			strSpeed = "- mph";
		}
		else {
			tmp.set(m_speed);
			unit = " km/h";
			strSpeed = "- km/h";
		}
		if (tmp.value >= 0) {
			if (tmp.value >= 100) {
				strSpeed = MyLocale.formatDouble(tmp, "0") + unit;
			}
			else {
				strSpeed = MyLocale.formatDouble(tmp, "0.0") + unit;
			}
		}

		tmp.set(moveDir);
		String strMoveDir = "---" + " " + MyLocale.getMsg(1502, "deg");
		if ((tmp.value <= 360) && (tmp.value >= -360))
			strMoveDir = tmp.toString(0, 0, 0) + " " + MyLocale.getMsg(1502, "deg");

		int textWidth = java.lang.Math.max(fm.getTextWidth(strSpeed), fm.getTextWidth(strMoveDir));
		textWidth = java.lang.Math.max(textWidth, fm.getTextWidth(strHeadline));

		int startX = location.width - (textWidth + 4);
		g.fillRect(startX, 0, location.width - startX, lineHeight);

		g.setColor(Color.Black);
		g.drawText(strHeadline, startX + 2, 0);
		g.drawText(strSpeed, startX + 2, lineHeight);
		g.drawText(strMoveDir, startX + 2, 2 * lineHeight);
	}

	private void drawLuminaryData(Graphics g) {
		g.setColor(YELLOW);

		String strSunDir = "---" + " " + MyLocale.getMsg(1502, "deg");
		if (sunDir < 360 && sunDir > -360) {
			Double tmp = new Double();
			tmp.set(sunDir);
			strSunDir = tmp.toString(0, 0, 0) + " " + MyLocale.getMsg(1502, "deg");
		}

		int textWidth = java.lang.Math.max(fm.getTextWidth(m_Luminary), fm.getTextWidth(strSunDir));
		int startY = location.height - 2 * lineHeight;
		g.fillRect(0, startY, textWidth + 4, location.height - startY);

		g.setColor(Color.Black);
		g.drawText(m_Luminary, 2, startY);
		g.drawText(strSunDir, 2, startY + lineHeight);
	}

	private void drawGpsStatus(Graphics g) {
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
		if (m_hdop >= 0)
			strHdop = "HDOP: " + Convert.toString(m_hdop);

		int textWidth = java.lang.Math.max(fm.getTextWidth(strSats), fm.getTextWidth(strHdop));
		int startX = location.width - (textWidth + 4);
		int startY = location.height - 2 * lineHeight;
		g.fillRect(startX, startY, location.width - startX, location.height - startY);

		g.setColor(Color.Black);
		g.drawText(strSats, startX + 2, startY);
		g.drawText(strHdop, startX + 2, startY + lineHeight);
	}

	private void drawArrows(Graphics g) {
		if (g != null) {
			// select moveDirColor according to difference to gotoDir
			Color moveDirColor = RED;

			if (gotoDir < 360 && gotoDir > -360 && moveDir < 360 && moveDir > -360) {
				float diff = java.lang.Math.abs(moveDir - gotoDir);
				while (diff > 360) {
					diff -= 360.0f;
				}
				if (diff > 180.0f) {
					diff = 360.0f - diff;
				}

				if (diff <= 12.25f) {
					moveDirColor = GREEN;
				}
				else if (diff <= 22.5f) {
					moveDirColor = CYAN;
				}
				else if (diff <= 45.0f) {
					moveDirColor = ORANGE;
				}
				else if (diff <= 90.0f) {
					moveDirColor = MAGENTA;
				}
			}

			// draw only valid arrows
			if (northCentered) {
				if (gotoDir < 360 && gotoDir > -360)
					drawThickArrow(g, gotoDir, Color.DarkBlue, 1.0f);
				if (moveDir < 360 && moveDir > -360)
					drawThinArrow(g, moveDir, RED, moveDirColor, 1.0f);
				if (sunDir < 360 && sunDir > -360)
					drawSunArrow(g, sunDir, YELLOW, 0.75f);
			}
			else {
				// moveDir centered
				if (moveDir < 360 && moveDir > -360) {
					// drawDoubleArrow(g, 360 - moveDir, BLUE, new Color(175,0,0), 1.0f);
					// drawRose(g, 360 - moveDir, new Color(100,100,100), new Color(200,200,200), 1.0f);
					drawFullRose(g, 360 - moveDir, new Color(255, 255, 255), new Color(200, 200, 200), new Color(150, 150, 150), new Color(200, 200, 200), new Color(200, 200, 200), new Color(75, 75, 75), 1.0f, false, false);

					int radius = (int) (roseRadius * 0.75f);
					g.setPen(new Pen(RED, Pen.SOLID, 3));
					g.drawLine(location.width / 2, location.height / 2 - radius, location.width / 2, location.height / 2 + radius);

					if (gotoDir < 360 && gotoDir > -360)
						drawThinArrow(g, gotoDir - moveDir, Color.DarkBlue, moveDirColor, 1.0f);
					if (sunDir < 360 && sunDir > -360)
						drawSunArrow(g, sunDir - moveDir, YELLOW, 0.75f);
				}
			}
		}
	}

	private void drawSunArrow(Graphics g, float angle, Color col, float scale) {
		float angleRad = (angle) * (float) java.lang.Math.PI / 180;
		int centerX = location.width / 2, centerY = location.height / 2;
		float arrowLength = roseRadius * scale;
		float halfArrowWidth = arrowLength * 0.08f;
		float circlePos = arrowLength * 0.7f;
		int circleRadius = (int) (arrowLength * 0.1f);

		int circleX = centerX + new Float(circlePos * java.lang.Math.sin(angleRad)).intValue();
		int circleY = centerY - new Float(circlePos * java.lang.Math.cos(angleRad)).intValue();

		int[] pointsX = new int[4];
		int[] pointsY = new int[4];

		pointsX[0] = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad)).intValue();
		pointsY[0] = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad)).intValue();
		pointsX[1] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad + java.lang.Math.PI / 2.0)).intValue();
		pointsY[1] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad + java.lang.Math.PI / 2.0)).intValue();
		pointsX[2] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad + java.lang.Math.PI)).intValue();
		pointsY[2] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad + java.lang.Math.PI)).intValue();
		pointsX[3] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad - java.lang.Math.PI / 2.0)).intValue();
		pointsY[3] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad - java.lang.Math.PI / 2.0)).intValue();

		// g.setPen(new Pen(col,Pen.SOLID,3));
		// g.drawLine(centerX,centerY,pointX,pointY);

		g.setPen(new Pen(Color.Black, Pen.SOLID, 1));
		g.setBrush(new Brush(col, Brush.SOLID));
		g.fillPolygon(pointsX, pointsY, 4);
		g.fillEllipse(circleX - circleRadius, circleY - circleRadius, 2 * circleRadius, 2 * circleRadius);
	}

	private void drawThinArrow(Graphics g, float angle, Color col, Color colPoint, float scale) {
		float angleRad = (angle) * (float) java.lang.Math.PI / 180;
		int centerX = location.width / 2, centerY = location.height / 2;
		float arrowLength = roseRadius * scale;
		float halfOpeningAngle = (float) (java.lang.Math.PI * 0.03);
		float sideLineLength = arrowLength * 0.75f;

		int[] pointsX = new int[4];
		int[] pointsY = new int[4];

		pointsX[0] = centerX + new Float(sideLineLength * java.lang.Math.sin(angleRad - halfOpeningAngle)).intValue();
		pointsY[0] = centerY - new Float(sideLineLength * java.lang.Math.cos(angleRad - halfOpeningAngle)).intValue();
		pointsX[1] = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad)).intValue();
		pointsY[1] = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad)).intValue();
		pointsX[2] = centerX + new Float(sideLineLength * java.lang.Math.sin(angleRad + halfOpeningAngle)).intValue();
		pointsY[2] = centerY - new Float(sideLineLength * java.lang.Math.cos(angleRad + halfOpeningAngle)).intValue();
		pointsX[3] = centerX;
		pointsY[3] = centerY;

		g.setPen(new Pen(Color.Black, Pen.SOLID, 1));
		g.setBrush(new Brush(col, Brush.SOLID));
		g.fillPolygon(pointsX, pointsY, 4);
		if (colPoint != null) {
			g.setBrush(new Brush(colPoint, Brush.SOLID));
			g.fillPolygon(pointsX, pointsY, 3);
		}
	}

	private void drawFullRose(Graphics g, float angle, Color colLeft, Color colRight, Color colNorthLeft, Color colNorthRight, Color colBorder, Color colText, float scale, boolean bDrawText, boolean bDrawEightArrows) {
		float subScale1 = 1.0f;
		float subScale2 = 0.9f;
		float innerScale = 0.15f;
		if (bDrawEightArrows) {
			innerScale = 0.12f;
			drawRosePart(g, 45 + angle, colLeft, colRight, colBorder, colText, scale * subScale2, innerScale, "NE", bDrawText);
			drawRosePart(g, 135 + angle, colLeft, colRight, colBorder, colText, scale * subScale2, innerScale, "SE", bDrawText);
			drawRosePart(g, 225 + angle, colLeft, colRight, colBorder, colText, scale * subScale2, innerScale, "SW", bDrawText);
			drawRosePart(g, 315 + angle, colLeft, colRight, colBorder, colText, scale * subScale2, innerScale, "NW", bDrawText);
		}

		drawRosePart(g, 0 + angle, colNorthLeft, colNorthRight, colBorder, colText, scale * subScale1, innerScale, "N", bDrawText);
		drawRosePart(g, 90 + angle, colLeft, colRight, colBorder, colText, scale * subScale1, innerScale, "E", bDrawText);
		drawRosePart(g, 180 + angle, colLeft, colRight, colBorder, colText, scale * subScale1, innerScale, "S", bDrawText);
		drawRosePart(g, 270 + angle, colLeft, colRight, colBorder, colText, scale * subScale1, innerScale, "W", bDrawText);
	}

	private void drawRosePart(Graphics g, float angle, Color colLeft, Color colRight, Color colBorder, Color colText, float scale, float innerScale, String strDir, boolean bDrawText) {
		float angleRad = angle * (float) java.lang.Math.PI / 180;
		float angleRadText = (angle + 7.5f) * (float) java.lang.Math.PI / 180;
		int centerX = location.width / 2, centerY = location.height / 2;

		float arrowLength = roseRadius * scale;
		float halfArrowWidth = arrowLength * innerScale;

		int[] pointsX = new int[3];
		int[] pointsY = new int[3];

		pointsX[0] = centerX;
		pointsY[0] = centerY;
		pointsX[1] = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad)).intValue();
		pointsY[1] = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad)).intValue();
		pointsX[2] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad - java.lang.Math.PI / 4.0)).intValue();
		pointsY[2] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad - java.lang.Math.PI / 4.0)).intValue();

		g.setPen(new Pen(colBorder, Pen.SOLID, 1));
		g.setBrush(new Brush(colLeft, Brush.SOLID));
		g.fillPolygon(pointsX, pointsY, 3);

		pointsX[2] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad + java.lang.Math.PI / 4.0)).intValue();
		pointsY[2] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad + java.lang.Math.PI / 4.0)).intValue();

		g.setBrush(new Brush(colRight, Brush.SOLID));
		g.fillPolygon(pointsX, pointsY, 3);

		if (bDrawText) {
			int tempFontSize = new Float(scale * mainFont.getSize()).intValue();
			Font tempFont = new Font(mainFont.getName(), Font.BOLD, tempFontSize);
			g.setFont(tempFont);
			FontMetrics tempFm = g.getFontMetrics(tempFont);
			float stringHeight = tempFm.getHeight();
			float stringWidth = tempFm.getTextWidth(strDir);
			float stringGap = (float) java.lang.Math.sqrt(stringHeight * stringHeight + stringWidth * stringWidth);

			float stringPosition = arrowLength - stringGap / 2.0f;
			g.setColor(colText);
			g.drawText(strDir, centerX + new Float(stringPosition * java.lang.Math.sin(angleRadText) - stringWidth / 2.0f).intValue(), centerY - new Float(stringPosition * java.lang.Math.cos(angleRadText) + stringHeight / 2.0f).intValue());

			g.setFont(mainFont);
		}
	}

	private void drawThickArrow(Graphics g, float angle, Color col, float scale) {
		float angleRad = (angle) * (float) java.lang.Math.PI / 180;
		int centerX = location.width / 2, centerY = location.height / 2;
		float arrowLength = roseRadius * scale;
		float halfArrowWidth = arrowLength * 0.1f;

		int[] pointsX = new int[4];
		int[] pointsY = new int[4];

		pointsX[0] = centerX + new Float(arrowLength * java.lang.Math.sin(angleRad)).intValue();
		pointsY[0] = centerY - new Float(arrowLength * java.lang.Math.cos(angleRad)).intValue();
		pointsX[1] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad + java.lang.Math.PI / 2.0)).intValue();
		pointsY[1] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad + java.lang.Math.PI / 2.0)).intValue();
		pointsX[2] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad + java.lang.Math.PI)).intValue();
		pointsY[2] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad + java.lang.Math.PI)).intValue();
		pointsX[3] = centerX + new Float(halfArrowWidth * java.lang.Math.sin(angleRad - java.lang.Math.PI / 2.0)).intValue();
		pointsY[3] = centerY - new Float(halfArrowWidth * java.lang.Math.cos(angleRad - java.lang.Math.PI / 2.0)).intValue();

		g.setPen(new Pen(Color.Black, Pen.SOLID, 1));
		g.setBrush(new Brush(col, Brush.SOLID));
		g.fillPolygon(pointsX, pointsY, 4);
	}

	public void setNorthCentered(boolean nc) {
		northCentered = nc;
		if (northCentered != Global.pref.northCenteredGoto) {
			Global.pref.northCenteredGoto = northCentered;
			Global.pref.savePreferences();
		}
		refresh();
	}

	public boolean isNorthCentered() {
		return northCentered;
	}

	public static Font GetCorrectedFont(Graphics g, String name, int style, int size) {
		Font newFont = new Font(name, style, size);
		FontMetrics metrics = g.getFontMetrics(newFont);
		int fontHeight = metrics.getHeight();

		float ratio = (float) fontHeight / (float) size;
		if (ratio < 0.9 || ratio > 1.1) {
			size = (int) (size / ratio + 0.5);
			if (size < 5)
				size = 5;
			newFont = new Font(name, style, size);
		}

		return newFont;
	}
}
