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

import CacheWolf.CoordsInput;
import CacheWolf.CoordsPDAInput;
import CacheWolf.MainForm;
import CacheWolf.MainTab;
import CacheWolf.Preferences;
import CacheWolf.controls.GuiImageBroker;
import CacheWolf.controls.InfoBox;
import CacheWolf.database.CWPoint;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheType;
import CacheWolf.utils.MyLocale;
import ewe.fx.Brush;
import ewe.fx.Color;
import ewe.fx.Font;
import ewe.fx.FontMetrics;
import ewe.fx.Graphics;
import ewe.fx.Pen;
import ewe.fx.Point;
import ewe.fx.Rect;
import ewe.graphics.AniImage;
import ewe.sys.Convert;
import ewe.sys.Double;
import ewe.sys.Vm;
import ewe.sys.VmConstants;
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

    final CellPanel buttonPanel;
    final CellPanel coordsPanel;
    final CellPanel rosePanel;

    private mButton btnGPS, btnCenter, btnNewWpt;
    private mButton destination;
    private mButton btnChangeProjection;
    private int currFormatSel;

    mLabel lblDestination, lblGPS, lblPosition;
    Color gpsStatus;

    ImageControl icRose;
    GotoRose compassRose;

    final static Color RED = new Color(255, 0, 0);
    final static Color YELLOW = new Color(255, 255, 0);
    final static Color GREEN = new Color(0, 255, 0);
    final static Color BLUE = new Color(0, 128, 255);

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
    public GotoPanel() {

	// Button
	buttonPanel = new CellPanel();
	btnGPS = GuiImageBroker.getButton(MyLocale.getMsg(1504, "Start"), "gps");
	btnGPS.setToolTip(MyLocale.getMsg(1504, "Start"));

	btnCenter = GuiImageBroker.getButton(MyLocale.getMsg(309, "Centre"), "snap2gps");
	btnCenter.setToolTip(MyLocale.getMsg(646, "Current centre from GPS"));

	btnNewWpt = GuiImageBroker.getButton(MyLocale.getMsg(733, "Addi Wpt"), "newwpt");
	btnNewWpt.setToolTip(MyLocale.getMsg(311, "Create Waypoint"));

	buttonPanel.addNext(btnGPS);
	buttonPanel.addNext(btnCenter);
	buttonPanel.addLast(btnNewWpt);

	// coordsPanel
	coordsPanel = new CellPanel();

	// Format selection for coords (context) menu
	mnuContextFormt = new Menu();
	currFormatSel = 1; // default to d° m.m
	mnuContextFormt.addItem(miCooformat[0] = new MenuItem("d.d°"));
	miCooformat[0].modifiers &= ~MenuItem.Checked;
	mnuContextFormt.addItem(miCooformat[1] = new MenuItem("d°m.m\'"));
	miCooformat[1].modifiers |= MenuItem.Checked; // default
	mnuContextFormt.addItem(miCooformat[2] = new MenuItem("d°m\'s\""));
	miCooformat[2].modifiers &= ~MenuItem.Checked;
	mnuContextFormt.addItems(TransformCoordinates.getProjectedSystemNames());

	CellPanel destinationPanel = new CellPanel();
	lblDestination = new mLabel("DST: ");
	lblDestination.setMenu(mnuContextFormt);
	lblDestination.modifyAll(ControlConstants.WantHoldDown, 0);
	CellPanel labelPanel = new CellPanel();
	labelPanel.backGround = BLUE;
	labelPanel.addLast(lblDestination, VSTRETCH, CENTER);
	destinationPanel.addNext(labelPanel, VSTRETCH, VFILL);
	destination = GuiImageBroker.getButton(MyLocale.getMsg(1500, "DST:"), "goto");
	destination.setToolTip(MyLocale.getMsg(1500, "DST:"));
	destination.setMenu(mnuContextFormt);
	destination.modifyAll(ControlConstants.WantHoldDown, 0);
	destinationPanel.addNext(destination, HSTRETCH, HFILL);

	btnChangeProjection = GuiImageBroker.getButton("...", "projection");
	btnChangeProjection.setMenu(mnuContextFormt);
	btnChangeProjection.modifyAll(ControlConstants.WantHoldDown, 0);
	destinationPanel.addLast(btnChangeProjection, DONTSTRETCH, DONTFILL);
	coordsPanel.addLast(destinationPanel, HSTRETCH, HFILL);

	CellPanel gpsPanel = new CellPanel();
	gpsPanel.addNext(lblGPS = new mLabel("GPS: "), DONTSTRETCH, FILL);
	lblGPS.backGround = RED;
	lblGPS.setMenu(mnuContextFormt);
	lblGPS.modifyAll(ControlConstants.WantHoldDown, 0);

	lblPosition = new mLabel("");
	lblPosition.anchor = CENTER;
	lblPosition.setMenu(mnuContextFormt);
	lblPosition.modifyAll(ControlConstants.WantHoldDown, 0);
	gpsPanel.addLast(lblPosition, HSTRETCH, HFILL);
	coordsPanel.addLast(gpsPanel, HSTRETCH, HFILL);

	// rosePanel for bearing
	rosePanel = new CellPanel();
	compassRose = new GotoRose();
	icRose = new ImageControl(compassRose);
	icRose.modifyAll(ControlConstants.WantHoldDown, 0); // this is necessary in order to make PenHold on a PDA work as right click
	// Create context menu for compass rose
	mnuContextRose = new Menu();
	for (int i = 0; i < SkyOrientation.LUMINARY_NAMES.length; i++) {
	    mnuContextRose.addItem(miLuminary[i] = new MenuItem(SkyOrientation.getLuminaryName(i)));
	}
	icRose.setMenu(mnuContextRose);
	mnuContextRose.addItem(new MenuItem("", MenuItem.Separator, null));
	mnuContextRose.addItem(miNorthCentered = new MenuItem(MyLocale.getMsg(1503, "North Centered")));
	if (compassRose.isNorthCentered())
	    miNorthCentered.modifiers |= MenuItem.Checked;
	else
	    miNorthCentered.modifiers &= MenuItem.Checked;
	rosePanel.addLast(icRose, STRETCH, FILL);

	// add Panels

	if (Preferences.itself().tabsAtTop) {
	    if (Preferences.itself().menuAtTab)
		this.addLast(buttonPanel, HSTRETCH, HFILL);
	} else {
	    if (!Preferences.itself().menuAtTab)
		this.addLast(buttonPanel, HSTRETCH, HFILL);
	}

	this.addLast(coordsPanel, HSTRETCH, HFILL);
	this.addLast(rosePanel);

	if (Preferences.itself().tabsAtTop) {
	    if (!Preferences.itself().menuAtTab)
		this.addLast(buttonPanel, HSTRETCH, HFILL);
	} else {
	    if (Preferences.itself().menuAtTab)
		this.addLast(buttonPanel, HSTRETCH, HFILL);
	}

    }

    public void init() {

	// select luminary for orientation
	for (int i = 0; i < SkyOrientation.LUMINARY_NAMES.length; i++) {
	    if (i == Navigate.luminary)
		miLuminary[i].modifiers |= MenuItem.Checked;
	    else
		miLuminary[i].modifiers &= MenuItem.Checked;
	}

	lblPosition.text = Navigate.gpsPos.toString(CoordsInput.getLocalSystem(currFormatSel));

	setText(destination, getGotoBtnText());

    }

    // Overrides
    public void resizeTo(int pWidth, int pHeight) {
	super.resizeTo(pWidth, pHeight);
	Rect coordsRect = coordsPanel.getRect();
	Rect buttonRect = buttonPanel.getRect();
	int roseHeight = pHeight - coordsRect.y - coordsRect.height;

	if (Preferences.itself().tabsAtTop) {
	    roseHeight = roseHeight - buttonRect.height;
	} else {

	}

	if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
	    // some space for the SIP button
	    if ((Vm.getParameter(VmConstants.VM_FLAGS) & (VmConstants.VM_FLAG_SIP_BUTTON_ON_SCREEN)) == (VmConstants.VM_FLAG_SIP_BUTTON_ON_SCREEN)) {
		Rect screen = (Rect) Window.getGuiInfo(WindowConstants.INFO_SCREEN_RECT, null, new Rect(), 0);
		roseHeight = roseHeight - screen.height / 14;
	    }
	}

	rosePanel.resizeTo(pWidth, roseHeight);
	icRose.resizeTo(pWidth, roseHeight);
	compassRose.resize(pWidth, roseHeight);

	if (Preferences.itself().tabsAtTop) {
	    Rect roseRect = rosePanel.getRect();
	    buttonPanel.setLocation(0, roseRect.y + roseRect.height);
	}
    }

    // called from myNavigate
    public void destChanged(CWPoint d) {
	setText(destination, getGotoBtnText());
	updateDistance();
    }

    /**
     * updates distance and bearing
     * 
     */
    public void updateDistance() {
	// update distance
	float distance = -1.0f;
	if (Navigate.gpsPos.isValid() && Navigate.destination.isValid()) {
	    distance = (float) Navigate.gpsPos.getDistance(Navigate.destination);
	}
	compassRose.setWaypointDirectionDist((float) Navigate.gpsPos.getBearing(Navigate.destination), distance);
    }

    /**
     * method which is called if a timer is set up
     */
    public void updateGps(int fix) {
	Double bearMov = new Double();
	Double speed = new Double();
	Double sunAzimut = new Double();
	compassRose.setGpsStatus(fix, Navigate.gpsPos.getSats(), Navigate.gpsPos.getSatsInView(), Navigate.gpsPos.getHDOP());
	if ((fix > 0) && (Navigate.gpsPos.getSats() >= 0)) {
	    // display values only, if signal good
	    lblPosition.setText(Navigate.gpsPos.toString(CoordsInput.getLocalSystem(currFormatSel)));
	    speed.set(Navigate.gpsPos.getSpeed());
	    sunAzimut.set(MainTab.itself.navigate.skyOrientationDir.lonDec);
	    bearMov.set(Navigate.gpsPos.getBear());
	    updateDistance();
	    compassRose.setSunMoveDirections((float) sunAzimut.value, (float) bearMov.value, (float) speed.value);
	    // Set background to signal quality
	}

	// receiving data, but signal ist not good
	if ((fix == 0) && (Navigate.gpsPos.getSats() >= 0)) {
	    gpsStatus = YELLOW;
	}
	// receiving no data
	if (fix == -1) {
	    if (gpsStatus != RED)
		new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(1510, "No data from GPS.\nConnection to serial port/gpsd closed.")).exec();
	    gpsStatus = RED;
	    MainTab.itself.navigate.stopGps();
	}
	// cannot interpret data
	if (fix == -2) {
	    if (gpsStatus != RED)
		new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(1511,
			"Cannot interpret data from GPS/gpsd!\nPossible reasons:\nWrong port,\nwrong baud rate,\ninvalid protocol (need NMEA/gpsd).\nConnection to serial port closed.\nLast String tried to interpret:\n")
			+ Navigate.gpsPos.lastStrExamined, FormBase.OKB).exec();
	    gpsStatus = RED;
	    MainTab.itself.navigate.stopGps(); // TODO automatic in myNavigate?
	}
    }

    public void gpsStarted() {
	btnGPS.setText(MyLocale.getMsg(1505, "Stop"));
    }

    public void startGps() {
	MainTab.itself.navigate.startGps(Preferences.itself().logGPS, Convert.toInt(Preferences.itself().logGPSTimer));
    }

    public void gpsStoped() {
	btnGPS.setText(MyLocale.getMsg(1504, "Start"));
	gpsStatus = this.backGround;
	this.repaintNow(); // without this the change in the background color will not be displayed
    }

    private String getGotoBtnText() {
	if (Navigate.destination == null)
	    return MyLocale.getMsg(999, "Not set");
	else
	    return Navigate.destination.toString(CoordsInput.getLocalSystem(currFormatSel));
    }

    private void setText(mButton btn, String text) {
	GuiImageBroker.setButtonText(btn, text);
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
		    lblPosition.setText(Navigate.gpsPos.toString(CoordsInput.getLocalSystem(currFormatSel)));
		    setText(destination, getGotoBtnText());
		} // end lat-lon-format context menu
		if (((MenuEvent) ev).menu == mnuContextRose) {
		    MenuItem action = (MenuItem) mnuContextRose.getSelectedItem();
		    if (action != null) {
			for (int i = 0; i < miLuminary.length; i++) {
			    if (action == miLuminary[i]) {
				MainTab.itself.navigate.setLuminary(i);
				miLuminary[i].modifiers |= MenuItem.Checked;
				compassRose.setLuminaryName(SkyOrientation.getLuminaryName(Navigate.luminary));
			    } else
				miLuminary[i].modifiers &= ~MenuItem.Checked;
			}
			if (action == miNorthCentered) {
			    if (compassRose.isNorthCentered()) {
				compassRose.setNorthCentered(false);
				miNorthCentered.modifiers &= ~MenuItem.Checked;
			    } else {
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
		    MainTab.itself.navigate.stopGps();
	    }

	    // set current position as centre and recalculate distance of caches in MainTab
	    if (ev.target == btnCenter) {
		if (Navigate.gpsPos.isValid()) {
		    MainForm.itself.setCurCentrePt(Navigate.gpsPos);
		} else
		    new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(1514, "Cannot recalculate distances, because the GPS position is not set")).wait(FormBase.OKB);
	    }
	    // Start moving map
	    /*
	    if (ev.target == btnMap) {
	    	switchToMovingMap();
	    }
	     */
	    // create new waypoint with current GPS-position
	    if (ev.target == btnNewWpt) {
		CacheHolder ch = new CacheHolder();
		ch.setPos(Navigate.gpsPos);
		ch.setType(CacheType.CW_TYPE_STAGE); // see CacheType.GC_AW_STAGE_OF_MULTI // TODO unfertig
		MainTab.itself.newWaypoint(ch);
	    }
	    // change destination waypoint
	    if (ev.target == destination) {
		if (Vm.isMobile()) {
		    CoordsPDAInput InScr = new CoordsPDAInput(CoordsInput.getLocalSystem(currFormatSel));
		    if (Navigate.destination.isValid())
			InScr.setCoords(Navigate.destination);
		    else
			InScr.setCoords(new CWPoint(0, 0));
		    if (InScr.execute(null, TOP) == FormBase.IDOK)
			Navigate.itself.setDestination(InScr.getCoords());
		} else {
		    CoordsInput cs = new CoordsInput();
		    if (Navigate.destination.isValid())
			cs.setFields(Navigate.destination, CoordsInput.getLocalSystem(currFormatSel));
		    else
			cs.setFields(new CWPoint(0, 0), CoordsInput.getLocalSystem(currFormatSel));
		    if (cs.execute(null, TOP) == FormBase.IDOK)
			Navigate.itself.setDestination(cs.getCoords());
		}

	    }

	    if (ev.target == this.btnChangeProjection) {
		Rect rm = mnuContextFormt.getRect();
		btnChangeProjection.startDropMenu(new Point(rm.x, rm.y));
	    }

	}
	super.onEvent(ev);
    }
}

/**
 * class for displaying the compass rose including goto, sun and moving direction
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

    Font mainFont, bigFont;
    FontMetrics fm, fb;
    int lineHeight, lineHeightBig;

    int roseRadius;

    boolean northCentered = Preferences.itself().northCenteredGoto;

    final static Color RED = new Color(255, 0, 0);
    final static Color YELLOW = new Color(255, 255, 0);
    final static Color GREEN = new Color(0, 255, 0);
    final static Color BLUE = new Color(0, 0, 255);
    final static Color ORANGE = new Color(255, 128, 0);
    final static Color DARKGREEN = new Color(0, 192, 0);
    final static Color CYAN = new Color(0, 255, 255);
    final static Color MAGENTA = new Color(255, 0, 255);
    final static Color GREY = new Color(150, 150, 150);
    final static Color LIGHT_GREY = new Color(200, 200, 200);
    final static Color ROSE_BORDER_COLOR = new Color(150, 150, 150);
    final static Color ROSE_TEXT_COLOR = new Color(75, 75, 75);

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
	int fontSizeBig = location.width / 10;
	mainFont = GetCorrectedFont(g, "Verdana", Font.BOLD, fontSize);
	bigFont = GetCorrectedFont(g, "Verdana", Font.BOLD, fontSizeBig);
	g.setFont(mainFont);
	fm = g.getFontMetrics(mainFont);
	fb = g.getFontMetrics(bigFont);
	lineHeight = fm.getHeight();
	lineHeightBig = fb.getHeight();
	roseRadius = java.lang.Math.min((location.width * 3) / 4, location.height) / 2;

	if (northCentered) {
	    // scale(location.width, location.height, null, 0);
	    // super.doDraw(g, options);
	    drawFullRose(g, 0, Color.White, LIGHT_GREY, Color.White, LIGHT_GREY, ROSE_BORDER_COLOR, ROSE_TEXT_COLOR, 1.0f, true, true);
	} else {
	    int radius = (int) (roseRadius * 0.75f);

	    g.setPen(new Pen(ROSE_BORDER_COLOR, Pen.SOLID, 3));
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
	String strTempVal = "?";
	g.setColor(Color.DarkBlue);
	g.fillRect(0, 0, fm.getTextWidth(strTemp) + 4, lineHeight);
	g.setColor(Color.White);
	g.drawText(strTemp, 2, 0);

	g.setColor(Color.Black);

	int metricSystem = Preferences.itself().metricSystem;
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
	} else {
	    bigUnit = Metrics.KILOMETER;
	    smallUnit = Metrics.METER;
	    threshold = 1.0;
	    newDistance = distance;
	}
	if (newDistance >= 0.0f) {
	    tmp.set(newDistance);
	    if (tmp.value >= threshold) {
		strTempVal = MyLocale.formatDouble(tmp, "0.000");
		strTemp = strTempVal + " " + Metrics.getUnit(bigUnit);
	    } else {
		tmp.set(Metrics.convertUnit(tmp.value, bigUnit, smallUnit));
		strTempVal = tmp.toString(3, 0, 0);
		strTemp = strTempVal + " " + Metrics.getUnit(smallUnit);
	    }
	} else {
	    strTempVal = "---";
	    strTemp = strTempVal + " " + Metrics.getUnit(bigUnit);
	}
	// Draw distance inside the compass rose
	g.setFont(bigFont);
	g.setPen(new Pen(ROSE_BORDER_COLOR, Pen.SOLID, 1));
	g.setBrush(new Brush(Color.White, Brush.SOLID));
	int ellipseWidth = fb.getTextWidth(strTempVal) * 5 / 4;
	int ellipseHeight = lineHeightBig * 5 / 4;
	g.fillEllipse((location.width - ellipseWidth) / 2, (location.height - ellipseHeight) / 2, ellipseWidth, ellipseHeight);
	g.setColor(Color.Black);
	g.drawText(strTempVal, (location.width - fb.getTextWidth(strTempVal)) / 2, (location.height - lineHeightBig) / 2);

	g.setFont(mainFont);
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
	if (Preferences.itself().metricSystem == Metrics.IMPERIAL) {
	    tmp.set(Metrics.convertUnit(m_speed, Metrics.KILOMETER, Metrics.MILES));
	    unit = " mph";
	    strSpeed = "- mph";
	} else {
	    tmp.set(m_speed);
	    unit = " km/h";
	    strSpeed = "- km/h";
	}
	if (tmp.value >= 0) {
	    if (tmp.value >= 100) {
		strSpeed = MyLocale.formatDouble(tmp, "0") + unit;
	    } else {
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
	} else
	// receiving data, but signal ist not good
	if ((m_fix == 0) && (m_sats >= 0)) {
	    g.setColor(YELLOW);
	} else {
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
		} else if (diff <= 22.5f) {
		    moveDirColor = CYAN;
		} else if (diff <= 45.0f) {
		    moveDirColor = ORANGE;
		} else if (diff <= 90.0f) {
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
	    } else {
		// moveDir centered
		if (moveDir < 360 && moveDir > -360) {
		    // drawDoubleArrow(g, 360 - moveDir, BLUE, new Color(175,0,0), 1.0f);
		    // drawRose(g, 360 - moveDir, new Color(100,100,100), new Color(200,200,200), 1.0f);
		    drawFullRose(g, 360 - moveDir, Color.White, LIGHT_GREY, GREY, LIGHT_GREY, ROSE_BORDER_COLOR, ROSE_TEXT_COLOR, 1.0f, false, false);

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
	if (northCentered != Preferences.itself().northCenteredGoto) {
	    Preferences.itself().northCenteredGoto = northCentered;
	    Preferences.itself().savePreferences();
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
