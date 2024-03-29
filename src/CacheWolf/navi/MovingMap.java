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

import CacheWolf.MainForm;
import CacheWolf.MainTab;
import CacheWolf.Preferences;
import CacheWolf.controls.ExecutePanel;
import CacheWolf.controls.InfoBox;
import CacheWolf.controls.MyScrollBarPanel;
import CacheWolf.database.*;
import CacheWolf.navi.touchControls.ICommandListener;
import CacheWolf.navi.touchControls.MovingMapControls;
import CacheWolf.utils.Common;
import CacheWolf.utils.Metrics;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.STRreplace;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.fx.*;
import ewe.graphics.AniImage;
import ewe.graphics.ImageDragContext;
import ewe.graphics.ImageList;
import ewe.graphics.InteractivePanel;
import ewe.sys.Convert;
import ewe.sys.SystemResourceException;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.*;
import ewe.util.Vector;

/**
 * Class to handle a moving map.
 * <p>
 * additional classes defined in this file :
 * <p>
 * class MovingMapPanel extends InteractivePanel implements EventListener Class to display the map bitmap and to select another bitmap to display.
 * <p>
 * class ListBox extends Form Class to display maps to manually choose from
 * <p>
 * class ArrowsOnMap extends AniImage
 */
public final class MovingMap extends Form implements ICommandListener {
    public final static int gotFix = 4; // green
    public final static int lostFix = 3; // yellow
    public final static int noGPSData = 2; // red
    public final static int noGPS = 1; // no GPS-Position marker, manually disconnected
    public final static int ignoreGPS = -1; // ignore even changes in GPS-signal (eg. from lost fix to gotFix) this is wanted when the map is moved manually

    private final static Image imgSelectedCache = new Image("mark_cache.png");
    private final static Image imgGoto = new Image("goto_map.png");

    private final static int smallTileWidth = 256; //100;
    private final static int smallTileHeight = 256; //100;
    // keeps the choosen resolution as long as a map is available
    // that overlaps with the screen and with the PosCircle -
    // it changes the resolution if no such map is available.
    // It will change back to the wanted scale as soon as a map becomes available (through movement of the GPS-receiver)
    private final static int NORMAL_KEEP_RESOLUTION = 1;
    private final static int HIGHEST_RESOLUTION = 2;
    private final static int HIGHEST_RESOLUTION_GPS_DEST = 3;

    // the layer for the buttons
    private final MovingMapControls controlsLayer;
    // Holds areas not filled by currentMap and/or used tiles
    private final Vector whiteAreas = new Vector();
    FontMetrics fm;
    boolean reflectResourceException = true;
    private CacheDB cacheDB;
    private MovingMapPanel mmp;
    private MapsList maps;
    private Vector symbols;
    private TrackOverlay[] TrackOverlays;
    private CWPoint TrackOverlaySetCenterTopLeft;
    private Vector tracks;
    private MapInfoObject currentMap = null;
    private boolean running = false;
    private MapSymbol gotoPos = null;
    private MapImage mapImage1to1;
    private ArrowsOnMap directionArrows = new ArrowsOnMap();
    private MapSymbol posCircle;
    private int posCircleX = 0, posCircleY = 0;
    private AniImage posCircleImageHaveSignal;
    private AniImage posCircleImageNoSignal;
    private AniImage posCircleImageNoGps;
    // local access
    private int GpsStatus;
    // ignores updateGps-calls if true
    private boolean ignoreGps = false;
    private boolean autoSelectMap = true;
    // only needed to force updateposition to try to load the best map again after OutOfMemoryError after an repeated click on snap-to-gps
    private boolean forceMapLoad = true;
    private boolean mapHidden = false;
    // to avoid multi-threading problems
    private boolean inUpdatePosition;
    private boolean inLoadMaps = false;
    private boolean dontUpdatePos = false;
    private boolean zoomingMode = false;
    private boolean mapsloaded = false;
    private boolean doPaintPosDestLine = true;
    private boolean mobileVGA = false;
    private double lastDistance = -1;
    private float lastHighestResolutionGPSDestScale = -1;
    // Needed by updatePosition to decide if a recalculation of map-tiles is needed:
    private int lastXPos;
    private int lastYPos;
    private int lastWidth;
    private int lastHeight;
    private int lastCompareX = Integer.MAX_VALUE;
    private int lastCompareY = Integer.MAX_VALUE;
    private boolean eventOccurred; // not yet implemented, don't know how (check for abort filling white areas)
    private int mapChangeModus = HIGHEST_RESOLUTION_GPS_DEST;
    private float scaleWanted;
    private boolean inSetBestMap = false; // to avoid multi-threading problems

    /**
     * Creating the moving map.
     */
    public MovingMap() {
        symbols = new Vector();
        this.cacheDB = MainForm.profile.cacheDB;
        if (Preferences.itself().getScreenHeight() <= 640 && Preferences.itself().getScreenWidth() <= 640) {
            this.windowFlagsToSet = WindowConstants.FLAG_FULL_SCREEN;
        } else {
            Preferences.itself().setBigWindowSize(this);
        }
        // The following line is commented out,
        // because this caused trouble under ewe-vm v1.49 on win-xp
        // when MovingMap was started with maximized CacheWolf-Window
        // this.windowFlagsToClear = WindowConstants.FLAG_HAS_TITLE | UIConstants.BDR_NOBORDER;
        this.hasTopBar = false;
        this.noBorder = true;
        this.title = "Moving Map";
        // background must not be black because black is interpreted as transparent
        // and transparent images above (eg trackoverlay) want be drawn in windows-VM,
        // so be care, don|t use white either
        this.backGround = new Color(254, 254, 254);

        mmp = new MovingMapPanel(this);
        this.addLast(mmp);

        if (Vm.isMobile() && Preferences.itself().getScreenWidth() >= 400)
            mobileVGA = true;
        String imagesize = "";
        if (mobileVGA)
            imagesize = "_vga";

        // Symbol Position
        posCircle = new MapSymbol("GPS-Position", null, new CWPoint());
        posCircle.properties = mImage.AlwaysOnTop;
        mmp.addImage(posCircle);
        posCircleImageHaveSignal = new AniImage("position_green" + imagesize + ".png");
        posCircleImageNoSignal = new AniImage("position_yellow" + imagesize + ".png");
        posCircleImageNoGps = new AniImage("position_red" + imagesize + ".png");

        // Symbol directionArrows
        directionArrows.properties = mImage.AlwaysOnTop;
        mmp.addImage(directionArrows);

        final int fontSize = (3 * Preferences.itself().fontSize) / 2;
        final Font imageFont = new Font("Helvetica", Font.PLAIN, fontSize);
        fm = getFontMetrics(imageFont);

        GpsStatus = noGPS;
        dontUpdatePos = false;
        ignoreGps = true;

        mmp.startDragResolution = 5;
        mapsloaded = false;
        scaleWanted = 1;
        mapChangeModus = HIGHEST_RESOLUTION_GPS_DEST;
        lastHighestResolutionGPSDestScale = -1;

        controlsLayer = new MovingMapControls(this);

    }

    /**
     * Showing the moving map.
     */
    public void display(CWPoint centerTo) {
        exec(); // displays the Form modal
        running = true;
        ignoreGps = true; // else overlay symbols are removed on started gps

        rebuildOverlaySet(); // show tracks , even if reentering map

        scaleWanted = Preferences.itself().lastScale;
        mapChangeModus = NORMAL_KEEP_RESOLUTION;

        // a cache could have been deleted (previously shown)
        this.removeAllMapSymbols();
        initMaps(centerTo);
        if (symbols.isEmpty()) {
            this.showCachesOnMap();
        }

        if (getControlsLayer() != null) {
            getControlsLayer().changeRoleState(MovingMapControls.ROLE_MENU, false);
        }

        // if this is not running the destChanged called by navigate did nothing,
        if (MainTab.itself.navigate.destinationIsCache) {
            destChanged(MainTab.itself.navigate.destinationCache);
        } else {
            if (Navigate.destination.isValid())
                destChanged(Navigate.destination);
        }

        ignoreGps = false;

    }

    /**
     * moving the map by MainTab.itself.navigate.gpsPos.
     */
    public void updatePositionFromGps(int fix) {
        if (!running || ignoreGps)
            return;
        // runMovingMap neccessary in case of multi-threaded Java-VM:
        // ticked could be called during load of mmp
        if ((fix > 0) && (Navigate.gpsPos.getSats() >= 0)) {
            // TODO is getSats really necessary?
            directionArrows.setDirections((float) Navigate.gpsPos.getBearing(Navigate.destination), (float) MainTab.itself.navigate.skyOrientationDir.lonDec, (float) Navigate.gpsPos.getBear());
            setGpsStatus(MovingMap.gotFix);
            updatePosition(Navigate.gpsPos);
            ShowLastAddedPoint(MainTab.itself.navigate.curTrack);
        }
        if (fix == 0 && Navigate.gpsPos.getSats() == 0)
            setGpsStatus(MovingMap.lostFix);
        if (fix < 0)
            setGpsStatus(MovingMap.noGPSData);
        controlsLayer.updateContent("hdop", Convert.toString(Navigate.gpsPos.getHDOP()));
        controlsLayer.updateContent("sats", Convert.toString(Navigate.gpsPos.getSats()) + "/" + Convert.toString(Navigate.gpsPos.getSatsInView()));
    }

    public void destChanged(CWPoint d) {
        if (!running || gotoPos != null && gotoPos.where.equals(d))
            return;
        removeMapSymbol("goto");
        gotoPos = addSymbol("goto", "goto_map.png", d);
        repaint();
    }

    public void destChanged(CacheHolder ch) {
        final CWPoint d = new CWPoint(ch.getWpt()); // d is never null
        if (!running || !d.isValid() || gotoPos != null && gotoPos.where.equals(d))
            return;
        removeMapSymbol("goto");
        gotoPos = addSymbol("goto", ch, "goto_map.png", d);
        repaint();
    }

    /**
     * Method to load the best map for lat/lon and move the map so that the posCircle is at lat/lon
     */
    private void updatePosition(CWPoint where) {
        if (inUpdatePosition || dontUpdatePos || (where.latDec == 0 && where.lonDec == 0) || !where.isValid())
            return; // avoid multi-threading problems
        inUpdatePosition = true;

        Point mapPos = updatePositionOfMainMapImage(where);

        forceMapLoad = forceMapLoad || lastWidth != this.width || lastHeight != this.height;
        lastWidth = width;
        lastHeight = height;

        // if more then 1/10 of screen moved since last time or forceMapLoad:
        if (forceMapLoad || (java.lang.Math.abs(lastCompareX - mapPos.x) > this.width / 10 || java.lang.Math.abs(lastCompareY - mapPos.y) > this.height / 10)) {
            // we try to find a better map
            if (autoSelectMap) {
                setBestMap(where, !mmp.ScreenCompletlyCoveredByMainMap(this.width, this.height));
                mapPos = getMapPositionOnScreen();
                forceMapLoad = false;
            }
            if (isFillWhiteArea() && !mmp.ScreenCompletlyCoveredByMainMap(this.width, this.height))
                fillWhiteArea();
            lastCompareX = mapPos.x;
            lastCompareY = mapPos.y;
        } else {
            mmp.updatePositionOfMapTiles(mapPos.x - lastXPos, mapPos.y - lastYPos);
        }

        updatePositionOfMapElements();
        lastXPos = mapPos.x;
        lastYPos = mapPos.y;

        inUpdatePosition = false;
        repaint();
    }

    public MovingMapControls getControlsLayer() {
        return controlsLayer;
    }

    public void resizeTo(int w, int h) {
        super.resizeTo(w, h);
        updateFormSize(w, h);
    }

    public void updateFormSize(int w, int h) {
        MapImage.setScreenSize(w, h);
        MapImage mainMap = mmp.getMainMap();
        directionArrows.setLocation(w / 2 - directionArrows.getWidth() / 2, 10);
        if (mainMap != null)
            mainMap.screenDimChanged();
        if (posCircle != null)
            posCircle.screenDimChanged();
        if (tracks != null)
            rebuildOverlaySet();
        // TODO: see if the rest of the code works with symbols = null
        for (int i = symbols.size() - 1; i >= 0; i--) {
            ((MapSymbol) symbols.get(i)).screenDimChanged();
        }
        if (controlsLayer != null) {
            controlsLayer.updateFormSize(w, h);
        }
    }

    /**
     * loads the list of maps
     *
     * @param lat used to create empty maps with correct conversion from lon to meters the latitude must be known
     */
    private void loadMaps(double lat) {
        if (inLoadMaps)
            return;

        if (this.maps != null) {
            if (!(MainForm.profile.getMapsDir().equals(this.maps.getDirOfMapsList()))) {
                mapsloaded = false;
            }
        }
        if (mapsloaded)
            return;

        inLoadMaps = true;

        final InfoBox inf = new InfoBox(MyLocale.getMsg(4201, "Info"), MyLocale.getMsg(4203, "Loading list of maps..."));
        Vm.showWait(this, true);
        inf.exec();
        inf.waitUntilPainted(100);

        boolean remember = dontUpdatePos;
        dontUpdatePos = true;
        maps = new MapsList(lat);
        resetCenterOfMap();
        dontUpdatePos = remember;

        inf.close(0);
        Vm.showWait(this, false);

        mapsloaded = true;
        inLoadMaps = false;
    }

    public void updateScale() {

        if (currentMap != null) {
            double lineLengthMeters = 40 * currentMap.scale;

            final int metricSystem = Preferences.itself().metricSystem;
            double localizedLineLength = 0;
            int bigUnit = -1;
            int smallUnit = -1;
            double threshold = -1;
            // Allow for different metric systems
            if (metricSystem == Metrics.IMPERIAL) {
                bigUnit = Metrics.MILES;
                smallUnit = Metrics.FEET;
                threshold = 501;

                localizedLineLength = Metrics.convertUnit(lineLengthMeters, Metrics.METER, smallUnit);
            } else {
                bigUnit = Metrics.KILOMETER;
                smallUnit = Metrics.METER;
                threshold = 1000;

                localizedLineLength = lineLengthMeters;
            }

            int currentUnit = smallUnit;

            float digits = (float) java.lang.Math.floor(java.lang.Math.log(localizedLineLength) / java.lang.Math.log(10.0));
            localizedLineLength = (float) java.lang.Math.ceil(localizedLineLength / (float) java.lang.Math.pow(10, digits)) * (float) java.lang.Math.pow(10, digits);

            if (localizedLineLength >= threshold) {
                currentUnit = bigUnit;
                localizedLineLength = Metrics.convertUnit(lineLengthMeters, Metrics.METER, currentUnit);

                digits = (float) java.lang.Math.floor(java.lang.Math.log(localizedLineLength) / java.lang.Math.log(10.0));
                localizedLineLength = (float) java.lang.Math.ceil(localizedLineLength / (float) java.lang.Math.pow(10, digits)) * (float) java.lang.Math.pow(10, digits);
            }

            String lineLengthString = Convert.toString((int) localizedLineLength) + Metrics.getUnit(currentUnit);

            if (digits < 0) {
                final int decimals = (int) (-1 * digits);
                lineLengthString = Common.DoubleToString(localizedLineLength, decimals + 2, decimals) + Metrics.getUnit(currentUnit);
            }

            lineLengthMeters = Metrics.convertUnit(localizedLineLength, currentUnit, Metrics.METER);

            final int lineLengthPixels = (int) java.lang.Math.round(lineLengthMeters / currentMap.scale);

            controlsLayer.updateContent("scale", lineLengthString, lineLengthPixels);
        } else {

            controlsLayer.updateContent("scale", "no map", 20);
        }
    }

    private void updateDistance() {
        if (gotoPos != null && posCircle.where.isValid()) {
            final double currentDistance = gotoPos.where.getDistance(posCircle.where);
            if (currentDistance != lastDistance) {
                lastDistance = currentDistance;
                String d;

                final int metricSystem = Preferences.itself().metricSystem;
                double localizedDistance = 0;
                int bigUnit = -1;
                int smallUnit = -1;
                double threshold = -1;
                // Allow for different metric systems
                if (metricSystem == Metrics.IMPERIAL) {
                    // Why these values? See: http://tinyurl.com/b4nn9m
                    bigUnit = Metrics.MILES;
                    smallUnit = Metrics.FEET;
                    threshold = 0.1;
                    localizedDistance = Metrics.convertUnit(currentDistance, Metrics.KILOMETER, Metrics.MILES);
                } else {
                    bigUnit = Metrics.KILOMETER;
                    smallUnit = Metrics.METER;
                    threshold = 1.0;
                    localizedDistance = currentDistance;
                }
                if (localizedDistance >= threshold) {
                    d = Common.formatDouble(localizedDistance, "0.000") + Metrics.getUnit(bigUnit);
                } else {
                    d = Common.DoubleToString(Metrics.convertUnit(localizedDistance, bigUnit, smallUnit), 3, 0) + Metrics.getUnit(smallUnit);
                }
                controlsLayer.updateContent("distance", d);
            }
        } else {
            controlsLayer.updateContent("distance", "");
        }
    }

    public void addTrack(Track tr) {
        if (tr == null)
            return;
        if (tracks == null)
            tracks = new Vector();
        if (tracks.find(tr) >= 0)
            return; // track already in list
        tracks.add(tr);
        rebuildOverlaySet();
    }

    public void addTracks(Track[] trs) {
        if (trs == null || trs.length == 0)
            return;
        for (int i = 0; i < trs.length; i++) {
            addTrack(trs[i]);
        }
        rebuildOverlaySet();
    }

    /**
     * adds an 3x3 set of overlays to the map-window which contain the track
     * <p>
     * add tracks with addtrack(track) before
     */

    public void addOverlaySet() {
        if (tracks == null){
            return; // no tracks
	}
	
        try {
            TrackOverlaySetCenterTopLeft = ScreenXY2LatLon(100, 100);
            addMissingOverlays();
        } catch (final NullPointerException e) {
            // hapens if currentMap == null or PosCircle not valid
        } catch (final IllegalArgumentException e) {
            // happens if screensize is still not known ---> in both cases
            // creation of Overlayset will be done in updateOverlayPos if
            // tracks != null
        }
    }

    public void destroyOverlaySet() {
        if (TrackOverlays != null) {
            for (int i = 0; i < TrackOverlays.length; i++) {
                destroyOverlay(i);
            }
        }
        Vm.getUsedMemory(true); // call garbage collection
        Vm.gc();
    }

    public void rebuildOverlaySet() {
        destroyOverlaySet();
        addOverlaySet();
    }

    public void addMissingOverlays() {
	// height == 0 happens if this is called before the form is displayed on the screen
        if (currentMap == null || (!posCircle.where.isValid()) || width == 0 || height == 0){
            return;
	}
	
	if (TrackOverlays == null) {
            TrackOverlays = new TrackOverlay[9];
            TrackOverlaySetCenterTopLeft = ScreenXY2LatLon(100, 100);
        }
        // avoid multi-threading problems
        final boolean remember = dontUpdatePos;
        dontUpdatePos = true;
        final Point upperleftOf4 = getXYonScreen(TrackOverlaySetCenterTopLeft);
        int i;
        for (int yi = 0; yi < 3; yi++) {
            for (int xi = 0; xi < 3; xi++) {
                i = yi * 3 + xi;
                if (TrackOverlays[i] == null) {
                    Preferences.itself().log("addMissingOverlays: width: " + width + ", height: " + height);
                    TrackOverlays[i] = new TrackOverlay(ScreenXY2LatLon(upperleftOf4.x + (xi - 1) * width, upperleftOf4.y + (yi - 1) * height), width, height, currentMap);
                    TrackOverlays[i].setLocation(width + 1, height + 1);
                    // outside of the screen will hide it automatically
                    // it will get the correct position in upadteOverlayposition
                    TrackOverlays[i].tracks = this.tracks;
                    TrackOverlays[i].paintTracks();
                    mmp.addImage(TrackOverlays[i]);
                }
            }
        }
        updateOverlayOnlyPos();
        MapImage mainMap = mmp.getMainMap();
        if (mainMap != null)
            mmp.images.moveToBack(mainMap);
        dontUpdatePos = remember;
    }

    private void destroyOverlay(int ov) {
        if (TrackOverlays[ov] == null)
            return;
        mmp.removeImage(TrackOverlays[ov]);
        TrackOverlays[ov].free();
        TrackOverlays[ov] = null;
    }

    public void rearangeOverlays() {
        final Point oldp = getXYonScreen(TrackOverlaySetCenterTopLeft);
        if (TrackOverlays[1].isOnScreen()) { // oben raus
            TrackOverlaySetCenterTopLeft.set(ScreenXY2LatLon(oldp.x, oldp.y - 2 * height));
            destroyOverlay(6);
            destroyOverlay(7);
            destroyOverlay(8);
            mmp.removeImage(TrackOverlays[0]);
            mmp.removeImage(TrackOverlays[1]);
            mmp.removeImage(TrackOverlays[2]);
            TrackOverlays[6] = TrackOverlays[0];
            TrackOverlays[7] = TrackOverlays[1];
            TrackOverlays[8] = TrackOverlays[2];
            mmp.addImage(TrackOverlays[6]);
            mmp.addImage(TrackOverlays[7]);
            mmp.addImage(TrackOverlays[8]);
            TrackOverlays[0] = null;
            TrackOverlays[1] = null;
            TrackOverlays[2] = null;
            destroyOverlay(3);
            destroyOverlay(4);
            destroyOverlay(5);
        } else {
            if (TrackOverlays[3].isOnScreen()) { // links raus
                TrackOverlaySetCenterTopLeft.set(ScreenXY2LatLon(oldp.x - 2 * width, oldp.y));
                destroyOverlay(2);
                destroyOverlay(5);
                destroyOverlay(8);
                mmp.removeImage(TrackOverlays[0]);
                mmp.removeImage(TrackOverlays[3]);
                mmp.removeImage(TrackOverlays[6]);
                TrackOverlays[2] = TrackOverlays[0];
                TrackOverlays[5] = TrackOverlays[3];
                TrackOverlays[8] = TrackOverlays[6];
                mmp.addImage(TrackOverlays[2]);
                mmp.addImage(TrackOverlays[5]);
                mmp.addImage(TrackOverlays[8]);
                TrackOverlays[0] = null;
                TrackOverlays[3] = null;
                TrackOverlays[6] = null;
                destroyOverlay(1);
                destroyOverlay(4);
                destroyOverlay(7);
            } else {
                if (TrackOverlays[5].isOnScreen()) { // rechts raus
                    TrackOverlaySetCenterTopLeft.set(ScreenXY2LatLon(oldp.x + 2 * width, oldp.y));
                    destroyOverlay(0);
                    destroyOverlay(3);
                    destroyOverlay(6);
                    mmp.removeImage(TrackOverlays[2]);
                    mmp.removeImage(TrackOverlays[5]);
                    mmp.removeImage(TrackOverlays[8]);
                    TrackOverlays[0] = TrackOverlays[2];
                    TrackOverlays[3] = TrackOverlays[5];
                    TrackOverlays[6] = TrackOverlays[8];
                    mmp.addImage(TrackOverlays[0]);
                    mmp.addImage(TrackOverlays[3]);
                    mmp.addImage(TrackOverlays[6]);
                    TrackOverlays[2] = null;
                    TrackOverlays[5] = null;
                    TrackOverlays[8] = null;
                    destroyOverlay(1);
                    destroyOverlay(4);
                    destroyOverlay(7);
                } else {
                    if (TrackOverlays[7].isOnScreen()) { // unten raus
                        TrackOverlaySetCenterTopLeft.set(ScreenXY2LatLon(oldp.x, oldp.y + 2 * height));
                        destroyOverlay(0);
                        destroyOverlay(1);
                        destroyOverlay(2);
                        mmp.removeImage(TrackOverlays[6]);
                        mmp.removeImage(TrackOverlays[7]);
                        mmp.removeImage(TrackOverlays[8]);
                        TrackOverlays[0] = TrackOverlays[6];
                        TrackOverlays[1] = TrackOverlays[7];
                        TrackOverlays[2] = TrackOverlays[8];
                        mmp.addImage(TrackOverlays[0]);
                        mmp.addImage(TrackOverlays[1]);
                        mmp.addImage(TrackOverlays[2]);
                        TrackOverlays[6] = null;
                        TrackOverlays[7] = null;
                        TrackOverlays[8] = null;
                        destroyOverlay(3);
                        destroyOverlay(4);
                        destroyOverlay(5);
                    } else { // it is important to test for diagonal only
                        // if the other didn't match
                        if (TrackOverlays[0].isOnScreen()) { // links
                            // oben raus
                            TrackOverlaySetCenterTopLeft.set(ScreenXY2LatLon(oldp.x - 2 * width, oldp.y - 2 * height));
                            destroyOverlay(8);
                            mmp.removeImage(TrackOverlays[0]);
                            TrackOverlays[8] = TrackOverlays[0];
                            mmp.addImage(TrackOverlays[8]);
                            TrackOverlays[0] = null;
                            destroyOverlay(1);
                            destroyOverlay(2);
                            destroyOverlay(3);
                            destroyOverlay(4);
                            destroyOverlay(5);
                            destroyOverlay(6);
                            destroyOverlay(7);
                        } else {
                            if (TrackOverlays[2].isOnScreen()) { // rechts
                                // oben
                                // raus
                                TrackOverlaySetCenterTopLeft.set(ScreenXY2LatLon(oldp.x + 2 * width, oldp.y - 2 * height));
                                destroyOverlay(6);
                                mmp.removeImage(TrackOverlays[2]);
                                TrackOverlays[6] = TrackOverlays[2];
                                mmp.addImage(TrackOverlays[6]);
                                TrackOverlays[2] = null;
                                destroyOverlay(0);
                                destroyOverlay(1);
                                destroyOverlay(3);
                                destroyOverlay(4);
                                destroyOverlay(5);
                                destroyOverlay(7);
                                destroyOverlay(8);
                            } else {
                                if (TrackOverlays[6].isOnScreen()) { // links
                                    // unten
                                    // raus
                                    TrackOverlaySetCenterTopLeft.set(ScreenXY2LatLon(oldp.x - 2 * width, oldp.y + 2 * height));
                                    destroyOverlay(2);
                                    mmp.removeImage(TrackOverlays[6]);
                                    TrackOverlays[2] = TrackOverlays[6];
                                    mmp.addImage(TrackOverlays[2]);
                                    TrackOverlays[6] = null;
                                    destroyOverlay(0);
                                    destroyOverlay(1);
                                    destroyOverlay(3);
                                    destroyOverlay(4);
                                    destroyOverlay(5);
                                    destroyOverlay(7);
                                    destroyOverlay(8);
                                } else {
                                    if (TrackOverlays[8].isOnScreen()) { // rechts
                                        // unten
                                        // raus
                                        TrackOverlaySetCenterTopLeft.set(ScreenXY2LatLon(oldp.x + 2 * width, oldp.y + 2 * height));
                                        destroyOverlay(0);
                                        mmp.removeImage(TrackOverlays[8]);
                                        TrackOverlays[0] = TrackOverlays[8];
                                        mmp.addImage(TrackOverlays[0]);
                                        TrackOverlays[8] = null;
                                        destroyOverlay(1);
                                        destroyOverlay(2);
                                        destroyOverlay(3);
                                        destroyOverlay(4);
                                        destroyOverlay(5);
                                        destroyOverlay(6);
                                        destroyOverlay(7);
                                    } else
                                        for (int i = 0; i < TrackOverlays.length; i++) {
                                            destroyOverlay(i);
                                            TrackOverlaySetCenterTopLeft = ScreenXY2LatLon(100, 100);
                                        } // this happens if a position
                                    // jump occured
                                }
                            }
                        }
                    }
                }
            }
        } // close all IFs
        Vm.gc(); // call garbage collection
    }

    public void ShowLastAddedPoint(Track tr) {
        if (TrackOverlays == null || tr == null)
            return;
        for (int i = 0; i < TrackOverlays.length; i++) {
            TrackOverlays[i].paintLastAddedPoint(tr);
        }
    }

    public void updateOverlayOnlyPos() {
        if (TrackOverlays == null || TrackOverlays[4] == null)
            return;
        // Point upperleft = getMapXYPosition();
        Point posOnScreen;
        posOnScreen = getXYonScreen(TrackOverlays[4].topLeft);
        final Dimension ws = mmp.getSize(null);
        final int ww = ws.width;
        final int wh = ws.height;
        // Vm.sleep(100); // this is necessary because the ewe vm ist not
        // multi-threaded and the serial thread also needs time
        int num, pX, pY;
        for (int yi = 0; yi < 3; yi++) {
            for (int xi = 0; xi < 3; xi++) {
                num = yi * 3 + xi;
                pX = posOnScreen.x + (xi - 1) * ww;
                pY = posOnScreen.y + (yi - 1) * wh;
                TrackOverlays[num].setLocation(pX, pY);
            }
        }
    }

    public void updateOverlayPos() {
        if (tracks == null || tracks.size() == 0)
            return;
        if (TrackOverlays == null || TrackOverlays[4] == null)
            addMissingOverlays();
        else {
            updateOverlayOnlyPos();
            if (TrackOverlays[0].locAlways.x > 0 || TrackOverlays[2].locAlways.x < 0 || TrackOverlays[0].locAlways.y > 0 || TrackOverlays[8].locAlways.y < 0) { // testForNeedToRearange
                rearangeOverlays();
                addMissingOverlays();
                // updateOverlayOnlyPos(); is called from addMissingOverlays
            }
        }
    }

    /**
     * move posCircle to the Centre of the Screen
     */
    private void resetCenterOfMap() {
        if (width != 0) {
            posCircleX = width / 2;
            posCircleY = height / 2;
        } else {
            // maybe this could / should be replaced to windows size
            Preferences.itself().log("Window not yet on screen? This should never happen (again)");
            posCircleX = Preferences.itself().getScreenWidth() / 2;
            posCircleY = Preferences.itself().getScreenHeight() / 2;
        }
        posCircle.hidden = false;
        // always position the middle of a symbol
        posCircle.move(posCircleX - posCircle.getWidth() / 2, posCircleY - posCircle.getHeight() / 2);
        // posCircle.setLocation a problem -> hiding the posCircle in some situation
    }

    public void movePosCircleToLatLon(CWPoint p, boolean neuZeichnen) {
        moveScreenXYtoLatLon(new Point(posCircleX, posCircleY), p, neuZeichnen);
    }

    public void moveScreenXYtoLatLon(Point s, CWPoint c, boolean neuZeichnen) {
        final Point mappos = getMapPositionOnScreen();
        final Point onscreenpos = getXYonScreen(c);
        if (mmp != null) {
            MapImage mainMap = mmp.getMainMap();
            if (mainMap != null)
                mainMap.move(mappos.x - onscreenpos.x + s.x, mappos.y - onscreenpos.y + s.y);
        }
        mapMoved(s.x - onscreenpos.x, s.y - onscreenpos.y);
        if (neuZeichnen)
            mmp.repaintNow();
    }

    /**
     * call this if the map has moved on the screen (Ex by dragging) or size of posCircle changed this routine will adjust (move accordingly) all other symbols on the screen
     *
     * @param diffX
     * @param diffY
     */
    public void mapMoved(int diffX, int diffY) {
        final int npx = posCircleX - posCircle.getWidth() / 2 + diffX;
        final int npy = posCircleY - posCircle.getHeight() / 2 + diffY;
        posCircle.move(npx, npy);
        posCircleX = posCircleX + diffX;
        posCircleY = posCircleY + diffY;
        dontUpdatePos = false;
        updatePosition(posCircle.where);
    }

    /**
     * the map-position is calculated relativ to posCircle<br>
     * this is called when the map needs to be moved<br>
     * <br>
     * is used to move the map to the correct point<br>
     * <br>
     *
     * @return Point position of map on window (upper left corner)<br>
     * returns the same as mmp.mapImage.getLocation(mapPos);<br>
     * but also works if mmp == null<br>
     */
    private Point getMapPositionOnScreen() {
        // in case no calculation is possible return something outside of the screen
        if (currentMap == null || !posCircle.where.isValid())
            return new Point(Preferences.itself().getScreenWidth() + 1, Preferences.itself().getScreenHeight() + 1);
        final Point mapPos = new Point();
        final Point mapposint = currentMap.calcMapXY(posCircle.where);
        mapPos.x = posCircleX - mapposint.x;
        mapPos.y = posCircleY - mapposint.y;
        return mapPos;
    }

    /**
     * Method to calculate Point x,y of the current map using lat and lon target coordinates. There ist no garanty that the returned coordinates are inside of the map. They can be negative.
     *
     * @param ll CoordinatePoint
     * @return Point
     */
    public Point getXYonScreen(CoordinatePoint ll) {
        if (currentMap == null)
            return null;
        final Point coords = currentMap.calcMapXY(ll);
        final Point mapPos = getMapPositionOnScreen();
        return new Point(coords.x + mapPos.x, coords.y + mapPos.y);
    }

    public CWPoint ScreenXY2LatLon(int px, int py) {
        final Point mapPos = getMapPositionOnScreen();
        return currentMap.calcLatLon(px - mapPos.x, py - mapPos.y);
    }

    public MapSymbol addSymbol(String pName, String filename, CWPoint where) {
        final MapSymbol ms = new MapSymbol(pName, filename, where);
        setSymbolLocation(ms, where);
        addMapSymbol(ms);
        return ms;
    }

    public MapSymbol addSymbol(String pName, Object mapObject, String filename, CWPoint where) {
        final MapSymbol ms = new MapSymbol(pName, mapObject, filename, where);
        setSymbolLocation(ms, where);
        addMapSymbol(ms);
        return ms;
    }

    public void addSymbol(String pName, Object mapObject, Image imSymb, CWPoint where) {
        final MapSymbol ms = new MapSymbol(pName, mapObject, imSymb, where);
        setSymbolLocation(ms, where);
        addMapSymbol(ms);
    }

    private void setSymbolLocation(MapSymbol symbol, CoordinatePoint where) {
        final Point pOnScreen = getXYonScreen(where);
        if (pOnScreen != null) {
            symbol.setLocation(pOnScreen.x - symbol.getWidth() / 2, pOnScreen.y - symbol.getHeight() / 2);
        }
    }

    private void addMapSymbol(MapSymbol mapSymbol) {
        mapSymbol.properties |= mImage.AlwaysOnTop;
        symbols.add(mapSymbol);
        mmp.addImage(mapSymbol); // add to mmp list
    }

    public void updateSymbolPositions() {
        showCachesOnMap();
        for (int i = symbols.size() - 1; i >= 0; i--) {
            updateSymbolPosition((MapSymbol) symbols.get(i));
        }
    }

    private void updateSymbolPosition(MapSymbol symbol) {
        Point pOnScreen = getXYonScreen(symbol.where);
        if (pOnScreen != null) {
            symbol.move(pOnScreen.x - symbol.getWidth() / 2, pOnScreen.y - symbol.getHeight() / 2);
        }
    }

    private void addSymbolIfNecessary(String pName, Object mapObject, Image imSymb, CWPoint where) {
        if (findMapSymbol(pName) >= 0){
            return;
	}
        else{
            addSymbol(pName, mapObject, imSymb, where);
	}
    }

    public void addSymbolOnTop(String pName, Object mapObject, String filename, CWPoint where) {
        removeMapSymbol(mapObject);
        addSymbol(pName, mapObject, filename, where);
    }

    public CWPoint getGotoPosWhere() {
        if (gotoPos == null)
            return null;
        else
            return gotoPos.where;
    }

    public void removeAllMapSymbols() {
        for (int i = symbols.size() - 1; i >= 0; i--) {
            mmp.removeImage((MapSymbol) symbols.get(i));
        }
        symbols.removeAllElements();
    }

    public void removeMapSymbol(String pName) {
        final int symbNr = findMapSymbol(pName);
        if (symbNr != -1)
            removeMapSymbol(symbNr);
    }

    public void removeMapSymbol(Object obj) {
        final int symbNr = findMapSymbol(obj);
        if (symbNr != -1)
            removeMapSymbol(symbNr);
    }

    public void removeMapSymbol(int SymNr) {
        mmp.removeImage(((MapSymbol) symbols.get(SymNr)));
        symbols.removeElementAt(SymNr);
    }

    public int findMapSymbol(String pName) {
        MapSymbol ms;
        for (int i = symbols.size() - 1; i >= 0; i--) {
            ms = (MapSymbol) symbols.get(i);
            if (ms.name == pName)
                return i;
        }
        return -1;
    }

    public int findMapSymbol(Object obj) {
        MapSymbol ms;
        for (int i = symbols.size() - 1; i >= 0; i--) {
            ms = (MapSymbol) symbols.get(i);
            if (ms.mapObject == obj)
                return i;
        }
        return -1;
    }

    private void updatePositionOfMapElements() {
        updateSymbolPositions();
        updateDistance();
        updateOverlayPos();
    }

    public Point updatePositionOfMainMapImage(CWPoint where) {
        posCircle.where.set(where);
        final Point mapPos = getMapPositionOnScreen();
        mmp.moveMainMapImage(mapPos.x, mapPos.y);
        return mapPos;
    }

    private void showCachesOnMap() {
        CacheHolder ch;
        final BoundingBox screenArea = new BoundingBox(ScreenXY2LatLon(0, 0), ScreenXY2LatLon(width, height));
        for (int i = cacheDB.size() - 1; i >= 0; i--) {
            ch = cacheDB.get(i);
            if (screenArea.isInBound(ch.getWpt())) {
                // because visible and valid don't change while showing map
                // -->need no remove
                if (ch.isVisible() && ch.getWpt().isValid()) {
                    if (Preferences.itself().showCachesOnMap) {
                        addSymbolIfNecessary(ch.getCode(), ch, CacheType.getBigCacheIcon(ch), ch.getWpt());
                    }
		    else {
                        if (ch.isChecked || ch == cacheDB.get(MainTab.itself.tablePanel.getSelectedCache())) {
                            addSymbolIfNecessary(ch.getCode(), ch, CacheType.getBigCacheIcon(ch), ch.getWpt());
                        }
			else {
                            removeMapSymbol(ch);
                        }
                    }
                }
            } else {
                removeMapSymbol(ch);
            }
        }
        // adding target and selected
        // show target
        if (gotoPos != null) {
            // the CacheHolder Symbol must be inserted too, even if not marked (if it is Cache)
            CacheHolder gotoPosCH = null;
            if (gotoPos.mapObject instanceof CacheHolder) {
                gotoPosCH = (CacheHolder) gotoPos.mapObject;
            }
            if (gotoPosCH != null) {
                if (screenArea.isInBound(gotoPosCH.getWpt())) {
                    if (!Preferences.itself().showCachesOnMap) {
                        addSymbolIfNecessary(gotoPosCH.getCode(), gotoPosCH, CacheType.getBigCacheIcon(gotoPosCH), gotoPosCH.getWpt());
                    }
                    addSymbolIfNecessary("goto", gotoPosCH, imgGoto, gotoPos.where);
                }
            }
        }
        // mark Selected
        removeMapSymbol("selectedCache");
        ch = cacheDB.get(MainTab.itself.tablePanel.getSelectedCache());
        if (ch != null) {
            if (screenArea.isInBound(ch.getWpt())) {
                addSymbolIfNecessary("selectedCache", ch, imgSelectedCache, ch.getWpt());
            }
        }
    }

    private void fillWhiteArea() {
        Preferences.itself().log("filling white area");
        MapImage mainMap = mmp.getMainMap();
        if (mainMap == null)
            return; // if error at map load
        try {
            Vm.showWait(true);

            eventOccurred = false;
            whiteAreas.clear();
            // calculate areas which will not drawn
            final Point mapPos = getMapPositionOnScreen();
            if (mapPos.x > this.width || mapPos.y > this.height || mapPos.x + mainMap.getWidth() < 0 || mapPos.y + mainMap.getHeight() < 0) {
                Preferences.itself().log("map is outside the screen --> you only need to fill the screen");
                whiteAreas.add(new Rect(0, 0, this.width, this.height));
            } else {
                final Rect whiteArea = new Rect((-this.width / 10), (-this.height / 10), (int) (this.width * 1.1), (int) (this.height * 1.1));
                final Rect blackArea = new Rect(mapPos.x, mapPos.y, mainMap.getWidth(), mainMap.getHeight());
                Preferences.itself().log("max wA to cover: " + whiteArea);
                addRemainingWhiteAreas(blackArea, whiteArea);
            }
            // I've sometimes experienced an endless loop which might be caused by a bug in getBestMap.
            // Therefore i will stop the loop after max runs
            int max = 100;
            int count = 0;
            mmp.clearMapTiles();
            MovingMapCache.movingMapCache().clearUsedFlags();
            while (isFillWhiteArea() && currentMap.zoomFactor == 1.0 && !mapHidden && !whiteAreas.isEmpty() && count < max && !eventOccurred) {
                count++;
                Preferences.itself().log(eventOccurred + " white Area Nr.: " + count);
                try {
                    Rect stillWhite = getMapTileForWhiteArea();
                    if (stillWhite != null) {
                        int newWidth = stillWhite.width;
                        int newHeight = stillWhite.height;
                        if (stillWhite.width > smallTileWidth) {
                            newWidth = stillWhite.width / 2;
                        }
                        if (stillWhite.height > smallTileHeight) {
                            newHeight = stillWhite.height / 2;
                        }
                        if (!(newWidth == stillWhite.width && newHeight == stillWhite.height)) {
                            addRemainingWhiteAreas(new Rect(stillWhite.x + newWidth, stillWhite.y + newHeight, 0, 0), stillWhite);
                        }
                    }
                } catch (final ewe.sys.SystemResourceException sre) {
                    // next time there may be problem don't ask again
                    if (reflectResourceException) {
                        if (new InfoBox(MyLocale.getMsg(5500, "Error"), "Not enough ressources to fill white ares, disabling this").wait(FormBase.YESB | FormBase.NOB) == FormBase.IDYES) {
                            setFillWhiteArea(false);
                            reflectResourceException = true;
                        } else {
                            reflectResourceException = false;
                        }
                    }
                }
            }
        } finally {
            // Remove all tiles not needed from the cache to reduce memory
            MovingMapCache.movingMapCache().cleanCache();
            Vm.showWait(false);
        }
    }

    private Rect getMapTileForWhiteArea() {
        Rect blackArea;
        final Rect whiteArea = (Rect) whiteAreas.get(0);
        whiteAreas.removeElementAt(0);
        // calculate the center of the rectangle and try to get an map for it
        final int middleX = whiteArea.x + (whiteArea.width) / 2;
        final int middleY = whiteArea.y + (whiteArea.height) / 2;
        final CWPoint centerPoint = ScreenXY2LatLon(middleX, middleY);
        final Rect screen = new Rect();
        screen.height = whiteArea.height;
        screen.width = whiteArea.width;
        final MapInfoObject bestMap = maps.getBest(centerPoint, screen, currentMap.scale, true, false);
        if (bestMap == null) {
            // No map found, area must be left white
            Preferences.itself().log("!For wA " + whiteArea + middleX + "," + middleY + " got no map");
            return whiteArea;
        }
        // perhaps a nearby map is found, not containing the (center)Point, perhaps it fits on the screen
        // but we can't use this map: the splitting into white areas goes wrong in that case
        if (!(bestMap.bottomright.latDec <= centerPoint.latDec && centerPoint.latDec <= bestMap.topleft.latDec)) {
            Preferences.itself().log("!For wA " + whiteArea + middleX + "," + middleY + " Lat outside " + bestMap.getMapNameForList());
            return whiteArea;
        }
        if (!(bestMap.topleft.lonDec <= centerPoint.lonDec && centerPoint.lonDec <= bestMap.bottomright.lonDec)) {
            Preferences.itself().log("!For wA " + whiteArea + middleX + "," + middleY + " Lon outside " + bestMap.getMapNameForList());
            return whiteArea;
        }
        final String imagefilename = bestMap.getImagePathAndName();
        if (!imagefilename.equals(currentMap.getImagePathAndName())) {
            if (imagefilename.length() > 0) {
                // calculate position of the new map on the screen
                final Point mapPos = new Point();
                final Point mapposint = bestMap.calcMapXY(posCircle.where);
                mapPos.x = posCircleX - mapposint.x;
                mapPos.y = posCircleY - mapposint.y;
                final Point mapDimension = bestMap.calcMapXY(bestMap.bottomright);
                blackArea = new Rect(mapPos.x, mapPos.y, mapDimension.x, mapDimension.y);
                // Not all maps have the dimension 1000x1000 Pixels, we cache this information:
                Dimension rect2 = MovingMapCache.movingMapCache().getDimension(imagefilename);
                MapImage fullImage = null;
                if (rect2 == null) {
                    // the map is not in the cache
                    fullImage = new MapImage(imagefilename);
                    if (fullImage.image != null) {
                        rect2 = new Dimension(fullImage.getHeight(), fullImage.getWidth());
                        MovingMapCache.movingMapCache().putDimension(imagefilename, rect2);
                    } else {
                        Preferences.itself().log("Error getting bestMap from file: " + imagefilename);
                        maps.remove(bestMap);
                        return whiteArea;
                    }
                }
                // really adding a map image / tiles of the map image to the MovingMapPanel
                if (!generateSmallTiles(blackArea, imagefilename, mapPos, rect2, fullImage)) {
                    Preferences.itself().log("Error generate SmallTiles from file: " + imagefilename);
                    maps.remove(bestMap);
                    return whiteArea;
                }
                Preferences.itself().log("For wA " + whiteArea + middleX + "," + middleY + " got " + blackArea + " ='" + bestMap.getMapNameForList() + "'");
                // Are there any white areas left?
                addRemainingWhiteAreas(blackArea, whiteArea);
            }
        }
        return null;
    }

    private boolean generateSmallTiles(Rect blackArea, String filename, Point mapPos, Dimension rect2, MapImage fullImage) {
        // Generate tiles from the map
        final int numRows = ((rect2.height - 1) / smallTileHeight) + 1;
        final int numCols = ((rect2.width - 1) / smallTileWidth) + 1;
        for (int row = 0; row < numRows; row++) {
            for (int column = 0; column < numCols; column++) {
                // Tile is not needed, don't process
                if (!isCoveredByBlackArea(mapPos, row, column, blackArea, rect2)) {
                    Preferences.itself().log("not needed");
                    continue;
                }
                // Get tile from cache or
                // if not found, put tile into the cache
                MapImage im = MovingMapCache.movingMapCache().get(filename, row, column);
                if (im == null) {
                    if (fullImage == null) {
                        fullImage = new MapImage(filename);
                    }
                    if (fullImage.image != null) {
                        putImageIntoCache(filename, fullImage, mapPos, blackArea);
                        im = MovingMapCache.movingMapCache().get(filename, row, column);
                    } else
                        return false;
                }
                // If a tile has been found, draw it on the screen
                if (im != null) {
                    im.setLocation(mapPos.x + (column * smallTileWidth), mapPos.y + (row * smallTileHeight));
                    mmp.addMapTile(im);
                    repaintNow();
                }
            }
        }
        return true;
    }

    private void putImageIntoCache(String filename, MapImage fullImage, Point mapPos, Rect blackArea) {
        MapImage mapImage = null;
        final int numRows = (fullImage.getHeight() - 1) / smallTileHeight + 1;
        final int numCols = (fullImage.getWidth() - 1) / smallTileWidth + 1;
        for (int row2 = 0; row2 < numRows; row2++) {
            for (int column2 = 0; column2 < numCols; column2++) {
                final int realWidth = java.lang.Math.min(smallTileWidth, (fullImage.getWidth() - smallTileWidth * column2));
                final int realHeight = java.lang.Math.min(smallTileHeight, (fullImage.getHeight() - smallTileHeight * row2));
                if (!isCoveredByBlackArea(mapPos, row2, column2, blackArea, new Dimension(fullImage.getWidth(), fullImage.getHeight()))) {
                    continue;
                }
                try {
                    final Image image2 = new Image(realWidth, realHeight);
                    final int[] pixels = new int[realWidth * realHeight];
                    fullImage.getPixels(pixels, 0, smallTileWidth * column2, smallTileHeight * row2, realWidth, realHeight, 0);
                    image2.setPixels(pixels, 0, 0, 0, realWidth, realHeight, 0);
                    mapImage = new MapImage();
                    mapImage.setImage(image2);
                    MovingMapCache.movingMapCache().put(filename, row2, column2, mapImage);
                } catch (Exception e) {
                    Preferences.itself().log(e + " Error generating Tile Image from " + filename + " for " + row2 + "/" + column2 + " (" + realWidth + "x" + realHeight + ")");
                }
            }
        }
    }

    private boolean isCoveredByBlackArea(Point mapPos, int row, int column, Rect blackArea, Dimension mapDimension) {
        final int realWidth = java.lang.Math.min(smallTileWidth, (mapDimension.width - smallTileWidth * column));
        final int realHeight = java.lang.Math.min(smallTileHeight, (mapDimension.height - smallTileHeight * row));
        final int left = mapPos.x + column * smallTileWidth;
        final int right = left + realWidth;
        final int top = mapPos.y + row * smallTileHeight;
        final int bottom = top + realHeight;
        if (right < blackArea.x || bottom < blackArea.y) {
            return false;
        }
        if (left > blackArea.x + blackArea.width || top > blackArea.y + blackArea.height) {
            return false;
        }
        return true;
    }

    private void addRemainingWhiteAreas(Rect blackArea, Rect whiteArea) {
        // divide into non overlapping
        // remaining WhiteAreas must have width > mw and height > mh else they stay white
        final int mw = 10;
        final int mh = 10;
        int leftWidth = Math.max(0, blackArea.x - whiteArea.x);
        int rightWidth = Math.max(0, whiteArea.width - leftWidth - blackArea.width);
        int bottomHeight = Math.max(0, blackArea.y - whiteArea.y);
        int topYPos = blackArea.y + blackArea.height;
        int topHeight = Math.max(0, whiteArea.y + whiteArea.height - topYPos);
        // left Rect
        if (leftWidth > mw) {
            final Rect l = new Rect(whiteArea.x, whiteArea.y, leftWidth, bottomHeight + blackArea.height);
            if (l.height > mh) {
                whiteAreas.add(l);
                Preferences.itself().log("Left  : " + l);
            }
        }
        // top Rect
        if (topYPos != whiteArea.y) {
            if (topHeight > mh) {
                final Rect t = new Rect(whiteArea.x, topYPos, leftWidth + blackArea.width, topHeight);
                if (t.width > mw) {
                    whiteAreas.add(t);
                    Preferences.itself().log("Top   : " + t);
                }
            }
        }
        // right Rect
        if (rightWidth > mw) {
            final Rect r = new Rect(blackArea.x + blackArea.width, blackArea.y, rightWidth, blackArea.height + topHeight);
            if (r.height > mh) {
                whiteAreas.add(r);
                Preferences.itself().log("Right : " + r);
            }
        }
        // bottom Rect
        if (bottomHeight > mh) {
            final Rect b = new Rect(blackArea.x, whiteArea.y, blackArea.width + rightWidth, bottomHeight);
            if (b.width > mw) {
                whiteAreas.add(b);
                Preferences.itself().log("Bottom: " + b);
            }
        }
    }

    public void gpsStarted() {
        addTrack(MainTab.itself.navigate.curTrack);
        ignoreGps = false;
    }

    public void gpsStoped() {
        setGpsStatus(MovingMap.noGPS);
    }

    /**
     * loads the best map for lat/lon according to mapChangeModus lat/lon will be at the screen-pos of posCircle when posCircle is not on the screen (shifted outside my the user) then this routine uses the centre of the screen to find the best map
     * but anyway the map will be adjusted (moved) relativ to posCircle when a better map was found the called method updateposition will set posCirle Lat/-Lon to lat/lon.
     *
     * @param where lat/lon
     * @param loadIfSameScale false: will not change the map if the better map has the same scale as the current . - this is used not to change the map, if it covers already the screen completely true: willchange the map, regardless of change in scale
     */
    public void setBestMap(CWPoint where, boolean loadIfSameScale) {
        if (inSetBestMap)
            return;
        inSetBestMap = true;
        final Object[] s = getRectForMapChange(where);
        final CWPoint cll = (CWPoint) s[0];
        final Rect screen = (Rect) s[1];
        final boolean posCircleOnScreen = ((Boolean) s[2]).booleanValue();
        MapInfoObject newmap = null;
        switch (mapChangeModus) {
            case NORMAL_KEEP_RESOLUTION:
                lastHighestResolutionGPSDestScale = -1;
                newmap = maps.getBest(cll, screen, scaleWanted, false, true);
                if (newmap == null)
                    newmap = currentMap;
                break;
            case HIGHEST_RESOLUTION:
                lastHighestResolutionGPSDestScale = -1;
                newmap = maps.getBest(cll, screen, 0.000001f, false, true);
                break;
            case HIGHEST_RESOLUTION_GPS_DEST:
                if (gotoPos != null && GpsStatus != noGPS && posCircle.where.isValid()) {
                    if ((!posCircleOnScreen) && (lastHighestResolutionGPSDestScale > 0)) {
                        newmap = maps.getBest(cll, screen, lastHighestResolutionGPSDestScale, false, true);
                    } else {
                        // TODO use home-coos if no gps? - consider start from details panel and from gotopanel
                        newmap = maps.getMapForArea(posCircle.where, gotoPos.where);
                        // use map with most available overview if no map containing PosCircle and GotoPos is available
                        if (newmap == null)
                            newmap = maps.getBest(cll, screen, 10000000000000000000000000000000000f, false, true);
                        if (newmap != null) {
                            lastHighestResolutionGPSDestScale = newmap.scale;
                            if (!posCircleOnScreen) {
                                newmap = maps.getBest(cll, screen, lastHighestResolutionGPSDestScale, false, true);
                            }
                        }
                    }
                }
                // either Goto-Pos or GPS-Pos not set
                else {
                    lastHighestResolutionGPSDestScale = -1;
                    newmap = maps.getBest(cll, screen, 0.000001f, false, true);
                }
                break;
            default:
                new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(4208, "Bug: \nillegal mapChangeModus: ") + mapChangeModus).wait(FormBase.OKB);
                break;
        }

        if (newmap != null && (currentMap == null || !currentMap.getImagePathAndName().equals(newmap.getImagePathAndName()))) {
            if (loadIfSameScale || !MapsList.scaleEquals(currentMap.scale / currentMap.zoomFactor, newmap)) {
                // better map found
                setMap(newmap, where);
                moveScreenXYtoLatLon(new Point(screen.x, screen.y), cll, true);
            }
            inSetBestMap = false;
            return;
        }

        if (currentMap == null && newmap == null) {
            // F�r die aktuelle Position steht keine Karte zur Verf�gung
            // choosemap calls setmap with posCircle-coords
            posCircle.where.set(cll);
            // beware: "-4" only works if the empty maps were added last (see MapsList.addEmptyMaps)
            setMap(((MapListEntry) maps.elementAt(maps.getCount() - 4)).getMap(), where);
            while (currentMap == null) {
                // this actually cannot happen, but maybe in case of an inconstistent code change (esp. regarding empty maps)
                // force the user to select a map
                manualSelectMap();
                if (currentMap == null)
                    new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(4210, "Moving map cannot run without a map - please select one. \n You can select an empty map")).wait(FormBase.OKB);
            }
        }
        inSetBestMap = false;
    }

    /**
     * method to get a point on the screen which must be included in the map the map methods are looking for.<br>
     * If the poscircle is on the screen this will be that point.<br>
     * If it is outside then the centre of the screen will be used.<br>
     * <br>
     * returns [0] = CWPoint of that point, [1] Rect describing the screen around it<br>
     *
     * @param ll lat / lon
     * @return
     */
    public Object[] getRectForMapChange(CWPoint ll) {
        final int w = (width != 0 ? width : Preferences.itself().getScreenWidth());
        // width == 0 happens if this routine is run before the windows is on the screen
        final int h = (height != 0 ? height : Preferences.itself().getScreenHeight());
        int pX, pY;
        CWPoint cll;
        Boolean posCircleOnScreen = java.lang.Boolean.FALSE;
        if (posCircleX >= 0 && posCircleX <= w && posCircleY >= 0 && posCircleY <= h && ll.isValid()) {
            posCircleOnScreen = java.lang.Boolean.TRUE;
            // posCircle is inside the screen
            pX = posCircleX;
            pY = posCircleY;
            // TODO
            // eigentlich interessiert, ob nach dem  evtl. Kartenwechsel PosCircle on Screen ist.
            // So wie es jetzt ist, kann 2mal der gleiche Aufruf zum laden unterschiedlicher Karten f�hren,
            // wenn vorher PosCircle nicht auf dem Schirm war, nach dem ersten Laden aber schon.
            cll = new CWPoint(ll);
        } else {
            // when posCircle out of screen - use centre of screen as point which has to be included in the map
            cll = ScreenXY2LatLon(w / 2, h / 2);
            pX = w / 2;
            pY = h / 2;
        }
        final Object[] ret = new Object[3];
        ret[0] = cll;
        ret[1] = new Rect(pX, pY, w, h);
        ret[2] = posCircleOnScreen;
        return ret;
    }

    private void setGpsStatus(int status) {
        if (status == GpsStatus)
            return;
        GpsStatus = status;
        dontUpdatePos = false;
        ignoreGps = false;
        switch (status) {
            case noGPS: {
                posCircle.change(null);
                ignoreGps = true;
                break;
            }
            case gotFix: {
                posCircle.change(posCircleImageHaveSignal);
                break;
            }
            case lostFix: {
                posCircle.change(posCircleImageNoSignal);
                break;
            }
            case noGPSData: {
                posCircle.change(posCircleImageNoGps);
                break;
            }
        }
        // positions the posCircle correctly according to its size
        // which can change when the image changes, e.g. from null to something else
        mapMoved(0, 0);
        posCircle.refreshNow();
    }

    /**
     * sets and displays the map
     *
     * @param newmap
     * @param where    move map so that lat/lon is in the centre / -361: don't adust to lat/lon
     *                 lon -361: don't adust to lat/lon
     */
    public void setMap(MapInfoObject newmap, CWPoint where) {
        if (newmap == null) {
            Preferences.itself().log("da ist was schief gelaufen: keine newmap gefunden.");
            return;
        }
        if (currentMap != null) {
            // die selbe Karte gefunden
            if (newmap.getImagePathAndName().equals(currentMap.getImagePathAndName())) {
                posCircle.where.set(where);
                updatePositionOfMapElements();
                repaint();
                return;
            }
        }
        boolean remember = dontUpdatePos;
        try {
            Vm.showWait(true);
            dontUpdatePos = true; // make updatePosition ignore calls during loading new map
            Preferences.itself().log(MyLocale.getMsg(4216, "Loading map...") + newmap.getMapNameForList());
            MapImage mainMap = mmp.getMainMap();
            try {
                currentMap = newmap;
                title = currentMap.getMapNameForList();

                // neccessary to make updateposition to test if the current map is the best one for the GPS-Position
                lastCompareX = Integer.MAX_VALUE;
                lastCompareY = Integer.MAX_VALUE;
                if (mainMap != null) {
                    mmp.removeImage(mainMap);
                    mainMap.free();
                    mmp.setMainMap(null);
                    mapImage1to1 = null;
                    mainMap = null;
                    // calls the garbage collection
                    // give memory free before loading the new map to avoid out of memory error
                    Vm.getUsedMemory(true);
                }

                final String ImageFilename = currentMap.getImagePathAndName();

                if (ImageFilename == null) {
                    // no image associated with the calibration info ("empty map")
                    mainMap = new MapImage();
                } else {
                    if (ImageFilename.length() > 0) {
                        // attention: when running in native java-vm,
                        // no exception will be thrown, not even OutOfMemory Error
                        mainMap = new MapImage(ImageFilename);
                    } else {
                        // no image file exists
                        mainMap = new MapImage();
                    }
                }

                mmp.setMainMap(mainMap);
                mapImage1to1 = mainMap;
                mainMap.properties = mainMap.properties | mImage.IsMoveable;
                if (mapHidden)
                    mainMap.hide();
                mainMap.move(0, 0);
                mmp.addImage(mainMap);
                mmp.images.moveToBack(mainMap);

                rebuildOverlaySet();
                updateAfterMapChange(where);
                directionArrows.setMap(currentMap);
                updateScale();
                forceMapLoad = false;
            } catch (final IllegalArgumentException e) {
                // thrown by new AniImage() in ewe-vm if file not found;
                Preferences.itself().log("[MovingMap:setMap]IllegalArgumentException", e, true);
                if (mainMap != null) {
                    mmp.removeImage(mainMap);
                    mainMap.free();
                    mmp.setMainMap(null);
                    mapImage1to1 = mainMap;
                }
                rebuildOverlaySet();
                updatePositionOfMapElements();
                new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(4218, "Could not load map: \n") + newmap.getImagePathAndName()).wait(FormBase.OKB);
            } catch (final OutOfMemoryError e) {
                Preferences.itself().log("[MovingMap:setMap]OutOfMemoryError", e, true);
                if (mainMap != null) {
                    mmp.removeImage(mainMap);
                    mainMap.free();
                    mmp.setMainMap(null);
                    mapImage1to1 = mainMap;
                }
                rebuildOverlaySet();
                updatePositionOfMapElements();
                new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(4219, "Not enough memory to load map: \n") + newmap.getImagePathAndName() + MyLocale.getMsg(4220, "\nYou can try to close\n all prgrams and \nrestart CacheWolf"))
                        .wait(FormBase.OKB);
            } catch (final SystemResourceException e) {
                Preferences.itself().log("[MovingMap:setMap]SystemResourceException", e, true);
                if (mainMap != null) {
                    mmp.removeImage(mainMap);
                    mainMap.free();
                    mmp.setMainMap(null);
                    mapImage1to1 = mainMap;
                }
                rebuildOverlaySet();
                updatePositionOfMapElements();
                // TODO this doesn't work correctly if the resolution changed,
                // I guess because the pixels of PosCircle will be interpreted from the new resolution,
                // but should be interpreted using the old resolution to test:
                // select a map with a much greater value of m per pixel manually
                new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(4221, "Not enough ressources to load map: ") + newmap.getImagePathAndName() + MyLocale.getMsg(4220, "\nYou can try to close\n all prgrams and \nrestart CacheWolf"))
                        .wait(FormBase.OKB);
            }
        } finally {
            // this doesn't work in a ticked-thread in the ewe-vm.
            // That's why i made a new mThread in gotoPanel for ticked
            dontUpdatePos = remember;
            Vm.showWait(false);
        }
    }

    private void updateAfterMapChange(CWPoint newCenter) {
        // move mainMap
        final Point centerOnMap = currentMap.calcMapXY(newCenter);
        final int mapPosX = width / 2 - centerOnMap.x;
        final int mapPosY = height / 2 - centerOnMap.y;
        if (mmp != null) {
            MapImage mainMap = mmp.getMainMap();
            if (mainMap != null)
                mainMap.move(mapPosX, mapPosY);
        }
        if (!posCircle.where.isValid()) {
            posCircle.where.set(newCenter);
        }
        // move posCircle
        final Point circlePosOnMap = currentMap.calcMapXY(posCircle.where);
        posCircleX = mapPosX + circlePosOnMap.x;
        posCircleY = mapPosY + circlePosOnMap.y;
        final int npx = posCircleX - posCircle.getWidth() / 2;
        final int npy = posCircleY - posCircle.getHeight() / 2;
        posCircle.move(npx, npy);
        // move other MapElements
        updatePositionOfMapElements();
        repaint();
    }

    /**
     * zommes in if w>0 and out if w<0
     *
     * @param firstclickpoint
     * @param w
     * @param h
     */
    public void zoomScreenRect(Point firstclickpoint, int w, int h) {
        // maximal size of the zoomed image
        // don't make this too big, otherwise it causes out of memory errors
        int newImageWidth = (int) (this.width * (this.width < 481 ? 2 : 1.6));
        int newImageHeight = (int) (this.height * (this.width < 481 ? 2 : 1.6));
        final CWPoint center = ScreenXY2LatLon(firstclickpoint.x + w / 2, firstclickpoint.y + h / 2);
        float zoomFactor;
        if (h < 0) {
            h = java.lang.Math.abs(h);
            firstclickpoint.y = firstclickpoint.y - h;
        }
        if (w > 0) { // zoom in
            zoomFactor = java.lang.Math.min((float) this.width / (float) w, (float) this.height / (float) h);
        } else { // zoom out
            w = java.lang.Math.abs(w);
            // make firstclickedpoint the upper left corner
            firstclickpoint.x = firstclickpoint.x - w;
            zoomFactor = java.lang.Math.max((float) w / (float) this.width, (float) h / (float) this.height);
        }
        // calculate rect in unzoomed image in a way that the centre of the
        // new image is the centre of selected area but give priority to the
        // prefered image size of the scaled image
        newImageHeight = (int) (newImageHeight / zoomFactor / currentMap.zoomFactor);
        newImageWidth = (int) (newImageWidth / zoomFactor / currentMap.zoomFactor);
        final Point mappos = getMapPositionOnScreen();
        final int xinunscaledimage = (int) ((firstclickpoint.x - mappos.x + w / 2) / currentMap.zoomFactor + currentMap.shift.x - newImageWidth / 2);
        final int yinunscaledimage = (int) ((firstclickpoint.y - mappos.y + h / 2) / currentMap.zoomFactor + currentMap.shift.y - newImageHeight / 2);
        final Rect newImageRect = new Rect(xinunscaledimage, yinunscaledimage, newImageWidth, newImageHeight);
        if (mapImage1to1 != null && mmp.getMainMap() != null && mapImage1to1.image != null) {
            // try to avoid overlapping by shifting
            if (newImageRect.x < 0)
                newImageRect.x = 0; // align left if left overlapping
            if (newImageRect.y < 0)
                newImageRect.y = 0;
            // align right if right overlapping
            if (newImageRect.x + newImageRect.width >= mapImage1to1.getWidth())
                newImageRect.x = mapImage1to1.getWidth() - newImageWidth;
            if (newImageRect.y + newImageRect.height >= mapImage1to1.getHeight())
                newImageRect.y = mapImage1to1.getHeight() - newImageHeight;
            // crop if after shifting still overlapping
            if (newImageRect.x < 0)
                newImageRect.x = 0;
            if (newImageRect.y < 0)
                newImageRect.y = 0;
            if (newImageRect.x + newImageRect.width >= mapImage1to1.getWidth())
                newImageRect.width = mapImage1to1.getWidth() - newImageRect.x;
            if (newImageRect.y + newImageRect.height >= mapImage1to1.getHeight())
                newImageRect.height = mapImage1to1.getHeight() - newImageRect.y;
        }
        zoomFromUnscaled(zoomFactor * currentMap.zoomFactor, newImageRect, center);
    }

    /**
     * do the actual scaling
     *
     * @param zoomFactor   relative to original image
     * @param newImageRect Rect in the 1:1 image that contains the area to be zoomed into
     * @param center
     */
    public void zoomFromUnscaled(float zoomFactor, Rect newImageRect, CWPoint center) {
        Vm.showWait(this, true);
        final boolean remember = dontUpdatePos;
        if (mapImage1to1 != null) {
            dontUpdatePos = true; // avoid multi-thread problems
            int saveprop = mImage.IsMoveable;
            // remove + destroy the zoomed
            MapImage mainMap = mmp.getMainMap();
            if (mainMap != null) {
                saveprop = mainMap.properties;
                mmp.removeImage(mainMap);
                if (mainMap != mapImage1to1) {
                    // gezoomt
                    mainMap.free();
                    mmp.setMainMap(null);
                    mainMap = null;
                }
            }
            // do garbage collection
            Vm.getUsedMemory(true);

            mainMap = mapImage1to1;
            try {
                if (zoomFactor != 1)
                    mainMap = new MapImage(mapImage1to1.scale((int) (newImageRect.width * zoomFactor), (int) (newImageRect.height * zoomFactor), newImageRect, 0));
                currentMap.zoom(zoomFactor, newImageRect.x, newImageRect.y);
            } catch (final OutOfMemoryError e) {
                new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(4222, "Out of memory error")).wait(FormBase.OKB);
            }

            // do garbage collection
            Vm.getUsedMemory(true);

            mmp.setMainMap(mainMap);
            mainMap.properties = saveprop;
            if (mapHidden)
                mainMap.hide();
            mmp.addImage(mainMap);
            mmp.images.moveToBack(mainMap);

            if (mapImage1to1 != null && mainMap != null && mapImage1to1.image != null) {
                final Point mappos = getMapPositionOnScreen();
                mainMap.move(mappos.x, mappos.y);
            }
        } else // no map image loaded
        {
            currentMap.zoom(zoomFactor, newImageRect.x, newImageRect.y);
        }
        // scaleWanted = currentMap.scale; use this if you want to change
        // automatically to a map scale that best fits the zooming
        destroyOverlaySet();
        Vm.getUsedMemory(true); // call garbage collection
        setCenterOfScreen(center, false);
        addOverlaySet();
        updateScale();
        this.repaintNow();
        Vm.showWait(this, false);
        dontUpdatePos = remember;
    }

    public void onEvent(Event ev) {
        if (ev instanceof FormEvent && (ev.type == FormEvent.CLOSED)) {
            Preferences.itself().lastScale = currentMap.scale;
            running = false;
        }
        if (ev instanceof KeyEvent && ev.target == this && ((((KeyEvent) ev).key == IKeys.ESCAPE) || (((KeyEvent) ev).key == IKeys.ENTER) || (((KeyEvent) ev).key == IKeys.ACTION))) {
            this.close(0);
            ev.consumed = true;
        }
        super.onEvent(ev);
    }

    public boolean handleCommand(int actionCommand) {
        if (CLOSE == actionCommand) {
            final WindowEvent tmp = new WindowEvent();
            tmp.type = WindowEvent.CLOSE;
            postEvent(tmp);
            return true;
        }
        if (SELECT_MAP == actionCommand) {
            manualSelectMap();
            return true;
        }
        if (CHANGE_MAP_DIR == actionCommand) {
            final FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, MainForm.profile.getMapsDir());
            fc.addMask("*.wfl");
            fc.setTitle(MyLocale.getMsg(4200, "Select map directory:"));
            if (fc.execute() != FormBase.IDCANCEL) {
                // todo : perhaps only a temporary change
                MainForm.profile.setRelativeMapsDir(MainForm.profile.getMapsSubDir(fc.getChosen().toString()));
                mapsloaded = false;
                forceMapLoad = true;
                initMaps(posCircle.where);
            }
            return true;
        }
        if (FILL_MAP == actionCommand) {
            setFillWhiteArea(true);
            return true;
        }
        if (NO_FILL_MAP == actionCommand) {
            setFillWhiteArea(false);
            return true;
        }
        if (SHOW_CACHES == actionCommand) {
            setShowCachesOnMap(true);
            return true;
        }
        if (HIDE_CACHES == actionCommand) {
            setShowCachesOnMap(false);
            return true;
        }
        if (HIDE_MAP == actionCommand) {
            mmp.hideMap();
            mapHidden = true;
            repaintNow();
            return true;
        }
        if (SHOW_MAP == actionCommand) {
            mmp.showMap();
            mapHidden = false;
            repaintNow();
            return true;
        }
        if (HIGHEST_RES_GPS_DEST == actionCommand) {
            setResModus(MovingMap.HIGHEST_RESOLUTION_GPS_DEST);
            return true;
        }
        if (HIGHEST_RES == actionCommand) {
            setResModus(MovingMap.HIGHEST_RESOLUTION);
            return true;
        }
        if (KEEP_MAN_RESOLUTION == actionCommand) {
            setResModus(MovingMap.NORMAL_KEEP_RESOLUTION);
            return true;
        }
        if (MORE_DETAILS == actionCommand) {
            loadMoreDetailedMap(false);
            return true;
        }
        if (MORE_OVERVIEW == actionCommand) {
            loadMoreDetailedMap(true);
            return true;
        }
        if (ALL_CACHES_RES == actionCommand) {
            loadMapForAllCaches();
            return true;
        }
        if (MOVE_TO_CENTER == actionCommand) {
            setCenterOfScreen(Preferences.itself().curCentrePt, true);
            return true;
        }
        if (MOVE_TO_DEST == actionCommand) {
            if (gotoPos != null) {
                setCenterOfScreen(gotoPos.where, true);
            }
            return true;
        }
        if (MOVE_TO_GPS == actionCommand) {
            MainTab.itself.navigate.startGps(Preferences.itself().logGPS, Convert.toInt(Preferences.itself().logGPSTimer));
            SnapToGps();
            return true;
        }
        if (ZOOM_1_TO_1 == actionCommand) {
            zoom1to1();
            return true;
        }
        if (ZOOMIN == actionCommand) {
            zoomin();
            return true;
        }
        if (ZOOMOUT == actionCommand) {
            zoomout();
            return true;
        }
        return controlsLayer.handleCommand(actionCommand);
    }

    private void manualSelectMap() {
        CWPoint gpspos;
        if (Navigate.gpsPos.Fix > 0)
            gpspos = new CWPoint(Navigate.gpsPos.latDec, Navigate.gpsPos.lonDec);
        else
            gpspos = null;
        final ListBox manualSelectMap = new ListBox(maps, gpspos, getGotoPosWhere(), currentMap);
        if (manualSelectMap.execute() == FormBase.IDOK) {
            autoSelectMap = false;
            if (manualSelectMap.selectedMap.isInBound(posCircle.where) || manualSelectMap.selectedMap.getImagePathAndName().length() == 0) {
                setMap(manualSelectMap.selectedMap, posCircle.where);
                setResModus(MovingMap.NORMAL_KEEP_RESOLUTION);
                ignoreGps = false;
            } else {
                setGpsStatus(MovingMap.noGPS);
                ignoreGps = true;
                setMap(manualSelectMap.selectedMap, posCircle.where);
                // if map has an image
                if (currentMap.getMapType() != 1)
                    setCenterOfScreen(manualSelectMap.selectedMap.center, true);
                setResModus(MovingMap.NORMAL_KEEP_RESOLUTION);
            }
        }
    }

    private void initMaps(CWPoint where) {
        loadMaps(where.latDec);

        lastCompareX = Integer.MAX_VALUE;
        lastCompareY = Integer.MAX_VALUE;
        autoSelectMap = true;
        setBestMap(where, true);
        forceMapLoad = false;
    }

    private void setShowCachesOnMap(boolean value) {
        if (value != Preferences.itself().showCachesOnMap) {
            Preferences.itself().showCachesOnMap = value;
            updatePositionOfMapElements();
        }
    }

    private void setResModus(int modus) {
        scaleWanted = currentMap.scale;
        if (mapChangeModus == modus)
            return;
        mapChangeModus = modus;
        lastHighestResolutionGPSDestScale = -1;
        if (modus != NORMAL_KEEP_RESOLUTION) {
            setBestMap(posCircle.where, true);
        }
    }

    /**
     * @param betterOverview true: getmap with better overview
     * @return
     */
    private void loadMoreDetailedMap(boolean betterOverview) {
        // width == 0 happens if this routine is run before the windows is on the screen
        final int w = (width != 0 ? width : Preferences.itself().getScreenWidth());
        final int h = (height != 0 ? height : Preferences.itself().getScreenHeight());
        final Rect screen = new Rect(0, 0, w, h);

        CWPoint cll;
        if (currentMap != null) {
            cll = ScreenXY2LatLon(w / 2, h / 2);
        } else {
            cll = new CWPoint(posCircle.where);
        }

        final MapInfoObject m = maps.getMapChangeResolution(cll, screen, currentMap.scale * currentMap.zoomFactor, !betterOverview);
        if (m != null) {
            final boolean remember = dontUpdatePos;
            dontUpdatePos = true;
            setMap(m, cll);
            setResModus(MovingMap.NORMAL_KEEP_RESOLUTION);
            if (isFillWhiteArea()) {
                fillWhiteArea();
            }
            dontUpdatePos = remember;
        } else
            new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(4211, "No ") + (betterOverview ? MyLocale.getMsg(4212, "less") : MyLocale.getMsg(4213, "more")) + MyLocale.getMsg(4214, " detailed map available")).wait(FormBase.OKB);
    }

    private void loadMapForAllCaches() {
        final BoundingBox sur = MainForm.profile.getSourroundingArea(true);
        if (sur == null) {
            new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(4215, "Keine  Caches mit H?ckchen ausgew?hlt")).wait(FormBase.OKB);
            return;
        }
        MapInfoObject newmap = maps.getMapForArea(sur.topleft, sur.bottomright);
        if (newmap == null) { // no map that includs all caches is
            // available -> load map with lowest
            // resolution
            final Object[] s = getRectForMapChange(posCircle.where);
            final CWPoint cll = (CWPoint) s[0];
            final Rect screen = (Rect) s[1];
            newmap = maps.getBest(cll, screen, Float.MAX_VALUE - 1, false, true);
        }
        if (newmap == null) { // no map is covering any area of the caches
            final Object[] s = getRectForMapChange(posCircle.where);
            // CWPoint cll = (CWPoint) s[0];
            // for the size of the cache image
            final Rect screen = (Rect) s[1];
            final float neededscalex = (float) (sur.topleft.getDistance(sur.topleft.latDec, sur.bottomright.lonDec) * 1000 / (screen.width - 15));
            final float neededscaley = (float) (sur.topleft.getDistance(sur.bottomright.latDec, sur.topleft.lonDec) * 1000 / (screen.height - 15));
            // beware: "-4" only works if the empty maps were added last (see MapsList.addEmptyMaps)
            newmap = ((MapListEntry) maps.elementAt(maps.getCount() - 4)).getMap();
            newmap.zoom(newmap.scale * newmap.zoomFactor / (neededscalex > neededscaley ? neededscalex : neededscaley), 0, 0);
            forceMapLoad = true;
        }
        final boolean remember = dontUpdatePos;
        dontUpdatePos = true;
        setMap(newmap, posCircle.where);
        setResModus(MovingMap.NORMAL_KEEP_RESOLUTION);
        dontUpdatePos = remember;
    }

    private void setCenterOfScreen(CWPoint c, boolean neuZeichnen) {
        moveScreenXYtoLatLon(new Point(this.width / 2, this.height / 2), c, neuZeichnen);
    }

    private void SnapToGps() {
        resetCenterOfMap();
        dontUpdatePos = false;
        ignoreGps = false;
        lastCompareX = Integer.MAX_VALUE; // neccessary to make
        // updateposition to test if the
        // current map is the best one
        // for the GPS-Position
        lastCompareY = Integer.MAX_VALUE;
        autoSelectMap = true;
        forceMapLoad = true;
        // showMap(); why this?
        if (Navigate.gpsPos.Fix <= 0)
            updatePosition(posCircle.where);
        else
            updatePositionFromGps(Navigate.gpsPos.getFix());
    }

    private void zoom1to1() {
        final CWPoint center = ScreenXY2LatLon(this.width / 2, this.height / 2);
        if (mapImage1to1 != null)
            zoomFromUnscaled(1, new Rect(0, 0, mapImage1to1.getWidth(), mapImage1to1.getHeight()), center);
        else
            zoomFromUnscaled(1, new Rect(0, 0, 1, 1), center);
    }

    private void zoomin() {
        zoomScreenRect(new Point(this.width / 4, this.height / 4), this.width / 2, this.height / 2);
    }

    private void zoomout() {
        final CWPoint center = currentMap.center;
        float zoomfactor = currentMap.zoomFactor / 2;
        if (zoomfactor < 1) {
            zoomfactor = 1;
        }
        if (mapImage1to1 != null)
            zoomFromUnscaled(zoomfactor, new Rect(0, 0, mapImage1to1.getWidth(), mapImage1to1.getHeight()), center);
        else
            zoomFromUnscaled(zoomfactor, new Rect(0, 0, 1, 1), center);
    }

    public MapSymbol getDestination() {
        return gotoPos;
    }

    /**
     * @return the dontUpdatePos
     */
    public boolean dontUpdatePos() {
        return dontUpdatePos;
    }

    /**
     * @param dontUpdatePos the dontUpdatePos to set
     */
    public void setDontUpdatePos(boolean dontUpdatePos) {
        this.dontUpdatePos = dontUpdatePos;
    }

    /**
     * @return the zoomingMode
     */
    public boolean isZoomingMode() {
        return zoomingMode;
    }

    /**
     * @param zoomingMode the zoomingMode to set
     */
    public void setZoomingMode(boolean zoomingMode) {
        this.zoomingMode = zoomingMode;
    }

    /**
     * @param mapsloaded the mapsloaded to set
     */
    public void setMapsloaded(boolean mapsloaded) {
        this.mapsloaded = mapsloaded;
    }

    /**
     * @return the paintPosDestLine
     */
    public boolean doPaintPosDestLine() {
        return doPaintPosDestLine;
    }

    /**
     * @param doPaintPosDestLine the doPaintPosDestLine to set
     */
    public void setPaintPosDestLine(boolean doPaintPosDestLine) {
        this.doPaintPosDestLine = doPaintPosDestLine;
    }

    /**
     * @return the mobileVGA
     */
    public boolean isMobileVGA() {
        return mobileVGA;
    }

    public boolean isFillWhiteArea() {
        return Preferences.itself().fillWhiteArea;
    }

    private void setFillWhiteArea(boolean fillWhiteArea) {
        if (Preferences.itself().fillWhiteArea != fillWhiteArea) {
            Preferences.itself().fillWhiteArea = fillWhiteArea;
            if (!fillWhiteArea)
                mmp.clearMapTiles();
            else {
                forceMapLoad = true;
                updatePosition(posCircle.where);
            }
        }
    }

    public void addImage(AniImage ani) {
        mmp.addImage(ani);
    }

    public void removeImage(AniImage ani) {
        mmp.removeImage(ani);
    }

    /**
     * @return the posCircleX
     */
    public int getPosCircleX() {
        return posCircleX;
    }

    /**
     * @return the posCircleY
     */
    public int getPosCircleY() {
        return posCircleY;
    }

}

/**
 * Class to display the map bitmap and to select another bitmap to display.
 */
class MovingMapPanel extends InteractivePanel implements EventListener {
    Menu kontextMenu;
    MenuItem gotoMenuItem = new MenuItem(MyLocale.getMsg(4230, "Goto here$g"), 0, null);
    MenuItem newWayPointMenuItem = new MenuItem(MyLocale.getMsg(4232, "Create new Waypoint here$n"), 0, null);
    ;
    MenuItem openCacheDescMenuItem, openCacheDetailMenuItem, addCachetoListMenuItem, gotoCacheMenuItem, markFoundMenuItem, hintMenuItem, missionMenuItem;
    MenuItem miLuminary[];

    CacheHolder clickedCache;
    private MovingMap mm;
    private MapImage mainMap;
    private Vector mapTiles; // to remember the additional Tiles 
    private Point saveMapLoc = null;
    private boolean remember;
    private boolean paintingZoomArea;

    private ImageList saveImageList = null;
    private int lastZoomWidth, lastZoomHeight;

    private boolean ignoreNextDrag = false; // for handling a misfired drag event on pda  
    private boolean onlyIfCache = false;

    // ctor
    public MovingMapPanel(MovingMap mm) {
        this.mm = mm;
        miLuminary = new MenuItem[SkyOrientation.LUMINARY_NAMES.length];
        for (int i = 0; i < SkyOrientation.LUMINARY_NAMES.length; i++) {
            miLuminary[i] = new MenuItem(SkyOrientation.getLuminaryName(i));
        }
        // want to get simulated right-clicks
        set(ControlConstants.WantHoldDown, true);
        mapTiles = new Vector();
    }

    //Overrides: imageBeginDragged(...) in InteractivePanel
    public boolean imageBeginDragged(AniImage which, Point pos) {
        if (mm.isZoomingMode()) {
            return false;
        }
        // drag at mapcontrols doesn't drag the map
        if (mm.getControlsLayer().imageBeginDragged(which, pos)) {
            return false;
        }
        // move (drag) map
        remember = mm.dontUpdatePos();
        mm.setDontUpdatePos(true);
        saveMapLoc = pos;
        bringMapToTop();
        if (mainMap.isOnScreen() && !mainMap.hidden)
            return super.imageBeginDragged(mainMap, pos);
        else
            return super.imageBeginDragged(null, pos);
    }

    //Overrides: imageNotDragged(...) in InteractivePanel
    public boolean imageNotDragged(ImageDragContext dc, Point pos) {
        final boolean ret = super.imageNotDragged(dc, pos);
        bringMaptoBack();
        if (dc.image == null)
            moveMap(pos.x - saveMapLoc.x, pos.y - saveMapLoc.y);
        else
            mapMoved(pos.x - saveMapLoc.x, pos.y - saveMapLoc.y);
        mm.setDontUpdatePos(remember);
        return ret;
    }

    //Overrides onPenEvent(...) in MosaicPanel
    public void onPenEvent(PenEvent ev) {
        if (ignoreNextDrag) {
            // On PDA next event after a Kontext ist a drag, that will move the map unwanted
            ignoreNextDrag = false;
            if (ev.type == PenEvent.PEN_DRAG)
                return; // ignoring now
        }
        if (ev.type == PenEvent.PEN_DOWN) {
            if (mm.isZoomingMode()) {
                remember = mm.dontUpdatePos();
                mm.setDontUpdatePos(true);
                saveMapLoc = new Point(ev.x, ev.y);
                paintingZoomArea = true;
                mm.setZoomingMode(true);
            } else {
                saveMapLoc = new Point(ev.x, ev.y);
                if (ev.modifiers == PenEvent.RIGHT_BUTTON) {
                    // context penHeld is fired directly on PDA (cause WantHoldDown Control Modifier) but not on PC (Java)
                    penHeld(new Point(ev.x, ev.y));
                } else {
                    // do it even on left klick
                    onlyIfCache = true;
                    penHeld(new Point(ev.x, ev.y));
                }
            }
        } else {
            if (mm.isZoomingMode()) {
                if (ev.type == PenEvent.PEN_UP) {
                    paintingZoomArea = false;
                    mm.setZoomingMode(false);
                    mm.getControlsLayer().changeRoleState("zoom_manually", false);
                    mm.setDontUpdatePos(remember);
                    // dont make to big zoom jumps - it is most probable not an intentional zoom
                    if (java.lang.Math.abs(lastZoomWidth) < 15 || java.lang.Math.abs(lastZoomHeight) < 15) {
                        repaintNow();
                        return;
                    }
                    mm.zoomScreenRect(saveMapLoc, lastZoomWidth, lastZoomHeight);
                }
                if (paintingZoomArea && (ev.type == PenEvent.PEN_MOVED_ON || ev.type == PenEvent.PEN_MOVE || ev.type == PenEvent.PEN_DRAG)) {
                    int left, top;
                    final Graphics dr = this.getGraphics();
                    if (lastZoomWidth < 0)
                        left = saveMapLoc.x + lastZoomWidth;
                    else
                        left = saveMapLoc.x;
                    if (lastZoomHeight < 0)
                        top = saveMapLoc.y + lastZoomHeight;
                    else
                        top = saveMapLoc.y;
                    left -= 2;
                    top -= 2;
                    if (top < 0)
                        top = 0;
                    if (left < 0)
                        left = 0;
                    this.repaintNow(dr, new Rect(left, top, java.lang.Math.abs(lastZoomWidth) + 4, java.lang.Math.abs(lastZoomHeight) + 4));
                    lastZoomWidth = ev.x - saveMapLoc.x;
                    lastZoomHeight = ev.y - saveMapLoc.y;
                    if (lastZoomWidth < 0)
                        left = saveMapLoc.x + lastZoomWidth;
                    else
                        left = saveMapLoc.x;
                    if (lastZoomHeight < 0)
                        top = saveMapLoc.y + lastZoomHeight;
                    else
                        top = saveMapLoc.y;
                    dr.setPen(new Pen(new Color(255, 0, 0), Pen.SOLID, 3));
                    // bug in ewe: thickness parameter is ignored
                    dr.drawRect(left, top, java.lang.Math.abs(lastZoomWidth), java.lang.Math.abs(lastZoomHeight), 0);
                }
            }
        }
        super.onPenEvent(ev);
    }

    //Overrides: doPaint(...) in Mosaic
    public void doPaint(Graphics g, Rect area) {
        super.doPaint(g, area);
        if (mm.getGotoPosWhere() != null && mm.doPaintPosDestLine()) {
            final Point dest = mm.getXYonScreen(mm.getGotoPosWhere());
            g.setPen(new Pen(Color.DarkBlue, Pen.SOLID, 3));
            g.drawLine(mm.getPosCircleX(), mm.getPosCircleY(), dest.x, dest.y);
        }
    }

    /**
     * @return MapImage the mapImage
     */
    public MapImage getMainMap() {
        return mainMap;
    }

    /**
     * @param mainMap the mapImage to set
     */
    public void setMainMap(MapImage mainMap) {
        this.mainMap = mainMap;
    }

    public boolean ScreenCompletlyCoveredByMainMap(int ScreenWidth, int ScreenHeight) {
        if (mainMap != null) {
            Point mapPos = mainMap.locAlways;
            return !(mapPos.x > 0 || mapPos.y > 0 || mapPos.x + mainMap.getWidth() < ScreenWidth || mapPos.y + mainMap.getHeight() < ScreenHeight);
        } else
            return false;
    }

    // remove all images from screen except the main mapImage. You con visibly drag only one image 
    private void bringMapToTop() {
        if (mainMap == null || mainMap.hidden) {
            saveImageList = null;
            return;
        }
        saveImageList = new ImageList();
        saveImageList.copyFrom(images);
        images.removeAllElements();
        images.add(mainMap);
    }

    private void bringMaptoBack() {
        if (saveImageList == null)
            return;
        images = saveImageList;
        saveImageList = null;
    }

    public void moveMainMapImage(int x, int y) {
        if (mainMap != null) {
            mainMap.move(x, y);
        }
    }

    public void moveMap(int diffX, int diffY) {
        if (diffX == 0 && diffY == 0)
            return;
        Point p = new Point();
        if (mainMap != null) {
            p = mainMap.locAlways;
            mainMap.move(p.x + diffX, p.y + diffY);
        }
        mapMoved(diffX, diffY);
    }

    public void mapMoved(int diffX, int diffY) {
        mm.mapMoved(diffX, diffY);
        this.repaintNow();
    }

    public void addMapTile(MapImage mapTile) {
        if (!mapTiles.contains(mapTile)) {
            mapTiles.add(mapTile);
            addImage(mapTile);
        }
    }

    public void removeMapTile(MapImage mapTile) {
        mapTiles.remove(mapTile);
        removeImage(mapTile);
    }

    public void clearMapTiles() {
        for (int i = mapTiles.size() - 1; i >= 0; i--) {
            removeImage((AniImage) mapTiles.get(i));
        }
        mapTiles.clear();
    }

    public void updatePositionOfMapTiles(int deltaX, int deltaY) {
        if (deltaX == 0 && deltaY == 0)
            return;
        for (int i = mapTiles.size() - 1; i >= 0; i--) {
            MapImage mi = (MapImage) mapTiles.get(i);
            mi.setLocation(mi.locAlways.x + deltaX, mi.locAlways.y + deltaY);
        }
    }

    public void hideMap() {
        mainMap.hide();
        for (int i = mapTiles.size() - 1; i >= 0; i--) {
            ((MapImage) mapTiles.get(i)).hide();
        }
    }

    public void showMap() {
        mainMap.unhide();
        for (int i = mapTiles.size() - 1; i >= 0; i--) {
            ((MapImage) mapTiles.get(i)).unhide();
        }
    }

    /**
     * Method to react to user.
     */
    public void imageClicked(AniImage which, Point pos) {
        mm.getControlsLayer().imageClicked(which);
    }

    public void penHeld(Point p) {
        ignoreNextDrag = true;
        if (!mm.isZoomingMode()) {
            kontextMenu = new Menu();
            if (!onlyIfCache) {
                kontextMenu.addItem(gotoMenuItem);
                kontextMenu.addItem(newWayPointMenuItem);
                kontextMenu.addItem(new MenuItem("-"));
            }
            final AniImage clickedOnImage = images.findHotImage(p);
            if (clickedOnImage != null && clickedOnImage instanceof MapSymbol) {
                if (((MapSymbol) clickedOnImage).mapObject instanceof CacheHolder) {
                    clickedCache = (CacheHolder) (((MapSymbol) clickedOnImage).mapObject);
                    // clickedCache == null can happen if clicked on the
                    // goto-symbol
                    if (clickedCache != null) {
                        CacheHolder ch = clickedCache;
                        if (clickedCache.isAddiWpt()) {
                            ch = clickedCache.mainCache;
                        }
                        if (ch != null) {
                            kontextMenu.addItem(new MenuItem(ch.getCode() + " '" + ch.getName() + "'"));
                            if (!ch.isCustomWpt()) {
                                kontextMenu.addItem(new MenuItem( //
                                        CacheSize.cw2ExportString(ch.getSize()) + //
                                                " D: " + CacheTerrDiff.longDT(ch.getDifficulty()) + //
                                                " T: " + CacheTerrDiff.longDT(ch.getTerrain()) + //
                                                ""));
                                kontextMenu.addItem(new MenuItem( //
                                        "" + ch.getOwner() + //
                                                " " + ch.getHidden() + //
                                                ""));
                            }
                        }
                        if (clickedCache.isAddiWpt()) {
                            kontextMenu.addItem(new MenuItem(clickedCache.getCode() + " '" + clickedCache.getName() + "'"));
                        }
                        kontextMenu.addItem(new MenuItem("-"));
                        openCacheDescMenuItem = new MenuItem(MyLocale.getMsg(201, "Open Desctiption") + "$o");
                        kontextMenu.addItem(openCacheDescMenuItem);
                        openCacheDetailMenuItem = new MenuItem(MyLocale.getMsg(200, "Open Details") + "$e");
                        kontextMenu.addItem(openCacheDetailMenuItem);
                        gotoCacheMenuItem = new MenuItem(MyLocale.getMsg(4279, "Goto") + "$g");
                        kontextMenu.addItem(gotoCacheMenuItem);
                        if (!clickedCache.isFound()) {
                            int msgNr = CacheType.getLogMsgNr(clickedCache.getType());
                            markFoundMenuItem = new MenuItem(MyLocale.getMsg(msgNr, "Found") + "$m");
                            kontextMenu.addItem(markFoundMenuItem);
                        }
                        if (MainForm.itself.cacheTourVisible) {
                            addCachetoListMenuItem = new MenuItem(MyLocale.getMsg(199, "Add to cachetour"));
                            kontextMenu.addItem(addCachetoListMenuItem);
                        }
                        String stmp = clickedCache.getDetails().getHints();
                        stmp = stmp.substring(0, Math.min(10, stmp.length())).trim();
                        if (!stmp.equals("")) {
                            kontextMenu.addItem(hintMenuItem = new MenuItem("Hint: " + stmp));
                        }
                        if (clickedCache.getType() == CacheType.CW_TYPE_QUESTION) {
                            stmp = clickedCache.getDetails().getLongDescription();
                            if (!stmp.equals("")) {
                                kontextMenu.addItem(missionMenuItem = new MenuItem("?: "));
                            }
                        }
                    }
                }
            }
            /*
             * this kontext will be replaced by the settings of the rose in the goto panel
             *
             * if ( !(mm.directionArrows.onHotArea(p.x, p.y)) ) { } else { for (int i=0; i<SkyOrientation.LUMINARY_NAMES.length; i++) { kontextMenu.addItem(miLuminary[i]); if (i == mm.MainTab.itself.navigate.luminary) miLuminary[i].modifiers |=
             * MenuItem.Checked; else miLuminary[i].modifiers &= MenuItem.Checked; } }
             */
            onlyIfCache = false;
            if (kontextMenu.items.size() > 0) {
                kontextMenu.exec(this, new Point(p.x, p.y), this);
            } else
                kontextMenu = null;
        }
    }

    public boolean imageMovedOn(AniImage which) {
        if (which instanceof MapSymbol) {
            if (((MapSymbol) which).mapObject instanceof CacheHolder) {
                final CacheHolder ch = (CacheHolder) ((MapSymbol) which).mapObject;
                this.toolTip = ch.getCode() + "\n" + ch.getName();
            }
        }
        return true;
    }

    public boolean imageMovedOff(AniImage which) {
        if (which instanceof MapSymbol) {
            if (((MapSymbol) which).mapObject instanceof CacheHolder) {
                this.toolTip = null;
            }
        }
        return true;
    }

    public void onEvent(Event ev) {
        // nothing selected in kontext
        if (kontextMenu != null && ev instanceof PenEvent && ev.type == PenEvent.PEN_DOWN && ev.target == this) {
            kontextMenu.close();
            kontextMenu = null;
            return;
        }
        // something selected
        if (ev instanceof MenuEvent) {
            if (ev.target == kontextMenu) {
                if ((((MenuEvent) ev).type == MenuEvent.SELECTED)) {
                    final MenuItem action = (MenuItem) kontextMenu.getSelectedItem();
                    if (action == gotoMenuItem) {
                        closeKontextMenu();
                        MainTab.itself.navigate.setDestination(mm.ScreenXY2LatLon(saveMapLoc.x, saveMapLoc.y));
                    }
                    if (action == openCacheDescMenuItem || action == openCacheDetailMenuItem) {
                        leaveMovingMap();
                        final MainTab mainT = MainTab.itself;
                        if (action == openCacheDescMenuItem)
                            mainT.openPanel(MainTab.MAP_CARD, clickedCache.getCode(), MainTab.DESCRIPTION_CARD);
                        else
                            mainT.openPanel(MainTab.MAP_CARD, clickedCache.getCode(), MainTab.DETAILS_CARD);
                    }
                    if (action == gotoCacheMenuItem) {
                        closeKontextMenu();
                        MainTab.itself.navigate.setDestination(clickedCache);
                    }
                    if (action == markFoundMenuItem) {
                        closeKontextMenu();
                        final Time dtm = new Time();
                        dtm.setFormat("yyyy-MM-dd HH:mm");
                        clickedCache.setStatus(dtm.toString());
                        clickedCache.setFound(true);
                        clickedCache.saveCacheDetails();
                        if (clickedCache.hasAddiWpt()) {
                            CacheHolder addiWpt;
                            for (int i = clickedCache.addiWpts.getCount() - 1; i >= 0; i--) {
                                addiWpt = (CacheHolder) clickedCache.addiWpts.get(i);
                                addiWpt.setStatus(dtm.toString());
                                addiWpt.setFound(true);
                                addiWpt.saveCacheDetails();
                                mm.removeMapSymbol(addiWpt.getCode());
                            }
                        }
                        mm.removeMapSymbol(clickedCache.getCode());
                        mm.updateSymbolPositions();
                        this.repaintNow();
                    }
                    if (action == newWayPointMenuItem) {
                        leaveMovingMap();
                        final CacheHolder newWP = new CacheHolder();
                        newWP.setWpt(mm.ScreenXY2LatLon(saveMapLoc.x, saveMapLoc.y));
                        MainTab.itself.newWaypoint(newWP);
                    }
                    if (action == addCachetoListMenuItem) {
                        closeKontextMenu();
                        MainForm.itself.addCache(clickedCache.getCode());
                    }
                    if (action == hintMenuItem) {
                        new InfoBox("Hint", STRreplace.replace(Common.rot13(clickedCache.getDetails().getHints()), "<br>", "\n")).wait(FormBase.OKB);
                    }
                    if (action == missionMenuItem) {
                        new InfoBox("Mission", STRreplace.replace(clickedCache.getDetails().getLongDescription(), "<br>", "\n")).wait(FormBase.OKB);
                    }
                    /*
                     * for (int i=0; i<miLuminary.length; i++) { if (action == miLuminary[i]) { kontextMenu.close(); mm.MainTab.itself.navigate.setLuminary(i); mm.updateGps(mm.MainTab.itself.navigate.gpsPos.getFix()); miLuminary[i].modifiers |=
                     * MenuItem.Checked; } else miLuminary[i].modifiers &= ~MenuItem.Checked; }
                     */
                }
            } // if (ev.target == kontextMenu)
        }
        super.onEvent(ev);
    }

    private void closeKontextMenu() {
        kontextMenu.close();
        // for not to do an additional klick (before reacting on klicks)
        final PenEvent pev = new PenEvent();
        pev.target = this;
        pev.type = PenEvent.PEN_DOWN;
        this.postEvent(pev);
        pev.type = PenEvent.PEN_UP;
        this.postEvent(pev);
    }

    private void leaveMovingMap() {
        closeKontextMenu();
        final WindowEvent close = new WindowEvent();
        close.target = this;
        close.type = WindowEvent.CLOSE;
        this.postEvent(close);
    }
}

/**
 * Class to display maps to choose from
 */
class ListBox extends Form {
    private final ExecutePanel executePanel;
    public MapInfoObject selectedMap;
    public boolean selected = false;
    mList list = new mList(4, 1, false);
    private Vector maps;

    public ListBox(Vector maps, CWPoint Gps, CWPoint gotopos, MapInfoObject curMap) {
        this.title = MyLocale.getMsg(4271, "Maps");
        this.setPreferredSize(java.lang.Math.max(Preferences.itself().getScreenWidth() * 3 / 4, java.lang.Math.min(240, Preferences.itself().getScreenWidth())), Preferences.itself().getScreenHeight() * 3 / 4);
        this.maps = maps;
        MapInfoObject mio;
        MapListEntry ml;
        String cmp;
        int oldmap = -1;
        boolean curMapFound = false;
        final boolean[] inList = new boolean[maps.size()];
        int row = -1;
        if (curMap == null)
            curMapFound = true;
        if (gotopos != null && Gps != null) {
            list.addItem(MyLocale.getMsg(4272, "--- Maps containing GPS and goto pos. ---"));
            row++;
            cmp = "FF1" + (new BoundingBox(new CWPoint(Gps.latDec, Gps.lonDec), gotopos)).getEasyFindString();
            for (int i = 0; i < maps.size(); i++) {
                ml = (MapListEntry) maps.get(i);
                if (!BoundingBox.containsRoughly(ml.sortEntryBBox, cmp))
                    continue; // TODO if no map available
                else {
                    mio = ml.getMap();
                }
                if (mio.isInBound(Gps.latDec, Gps.lonDec) && mio.isInBound(gotopos)) {
                    list.addItem(i + ": " + mio.getMapNameForList());
                    row++;
                    inList[i] = true;
                    if (!curMapFound && curMap != null && mio.getMapNameForList().equals(curMap.getMapNameForList())) {
                        oldmap = row;
                        curMapFound = true;
                    }
                } else
                    inList[i] = false;
            }
        }
        if (Gps != null) {
            list.addItem(MyLocale.getMsg(4273, "--- Maps containing curr. position ---"));
            row++;
            cmp = "FF1" + BoundingBox.getEasyFindString(new CWPoint(Gps.latDec, Gps.lonDec), 30);
            for (int i = 0; i < maps.size(); i++) {
                ml = (MapListEntry) maps.get(i);
                if (!BoundingBox.containsRoughly(ml.sortEntryBBox, cmp))
                    continue; // TODO if no map available
                else {
                    mio = ml.getMap();
                }
                if (mio.isInBound(Gps.latDec, Gps.lonDec)) {
                    list.addItem(i + ": " + mio.getMapNameForList());
                    row++;
                    inList[i] = true;
                    if (!curMapFound && curMap != null && mio.getMapNameForList().equals(curMap.getMapNameForList())) {
                        oldmap = row;
                        curMapFound = true;
                    }
                }
            }
        }
        if (gotopos != null) {
            list.addItem(MyLocale.getMsg(4274, "--- Karten des Ziels ---"));
            row++;
            cmp = "FF1" + BoundingBox.getEasyFindString(gotopos, 30);
            for (int i = 0; i < maps.size(); i++) {
                ml = (MapListEntry) maps.get(i);
                if (!BoundingBox.containsRoughly(ml.sortEntryBBox, cmp))
                    continue; // TODO if no map available
                else {
                    mio = ml.getMap();
                }
                if (mio.isInBound(gotopos)) {
                    list.addItem(i + ": " + mio.getMapNameForList());
                    row++;
                    inList[i] = true;
                    if (!curMapFound && curMap != null && mio.getMapNameForList().equals(curMap.getMapNameForList())) {
                        oldmap = row;
                        curMapFound = true;
                    }
                }
            }
        }
        list.addItem(MyLocale.getMsg(4275, "--- andere Karten ---"));
        row++;
        for (int i = 0; i < maps.size(); i++) {
            ml = (MapListEntry) maps.get(i);
            if (!inList[i]) {
                list.addItem(i + ": " + ml.getMapNameForList());
                row++;
                if (!curMapFound && curMap != null && ml.getMapNameForList().equals(curMap.getMapNameForList())) {
                    oldmap = row;
                    curMapFound = true;
                }
            }
        }
        list.selectItem(oldmap, true);
        this.addLast(new MyScrollBarPanel(list), CellConstants.STRETCH, CellConstants.FILL);
        executePanel = new ExecutePanel(this);
        executePanel.applyButton.takeFocus(0);
    }

    public void mapSelected() {
        try {
            selectedMap = null;
            int mapNum = 0;
            String it = list.getText();
            if (it.length() > 0) {
                it = it.substring(0, it.indexOf(':'));
                mapNum = Convert.toInt(it);
                selectedMap = ((MapListEntry) maps.get(mapNum)).getMap();
                selected = true;
                this.close(FormBase.IDOK);
            } else {
                selected = false;
                this.close(FormBase.IDCANCEL);
            }
        } catch (final NegativeArraySizeException e) {
            // happens in substring when a dividing line selected
        }
    }

    public void onEvent(Event ev) {
        if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
            if (ev.target == executePanel.cancelButton) {
                selectedMap = null;
                selected = false;
                this.close(FormBase.IDCANCEL);
            }
            if (ev.target == executePanel.applyButton || ev.target == list) {
                // ev.target == list is posted by mList if a selection was double clicked
                mapSelected();
            }
        }
        super.onEvent(ev);
    }

    public void penDoubleClicked(Point where) {
        mapSelected();
    }
}

class ArrowsOnMap extends AniImage {
    final static Color sunDirColor = new Color(255, 255, 0); // Yellow
    // final static Color GREEN = new Color(0,255,0);
    final static Color gotoDirColor = new Color(0, 0, 128); // dark blue
    final static Color northDirColor = new Color(0, 0, 255); // Blue
    float gotoDir = -361;
    float sunDir = -361;
    float moveDir = -361;
    int minY;
    Graphics draw;
    Color moveDirColor = new Color(255, 0, 0); // RED
    Point[] sunDirArrow = null;
    Point[] gotoDirArrow = null;
    Point[] moveDirArrow = null;
    Point[] northDirArrow = null;
    int imageSize = Preferences.itself().fontSize * 8;
    int arrowThickness = imageSize / 28;
    private MapInfoObject map = null;

    /**
     * @param gd goto direction
     * @param sd sun direction
     * @param md moving direction
     */
    public ArrowsOnMap() {
        super();
        newImage();
        // setDirections(90, 180, -90);
    }

    public void newImage() {
        setImage(new Image(imageSize, imageSize), Color.White);
        draw = new Graphics(image);
    }

    public void setMap(MapInfoObject m) {
        map = m;
        makeArrows();
    }

    public void setDirections(float gd, float sd, float md) {
        // to save cpu-usage only update if the is a change of directions of more than 1 degree
        if (java.lang.Math.abs(gotoDir - gd) > 1 || java.lang.Math.abs(sunDir - sd) > 1 || java.lang.Math.abs(moveDir - md) > 1) {
            gotoDir = gd;
            sunDir = sd;
            moveDir = md;
            makeArrows();
        }
    }

    /**
     * draw arrows for the directions of movement and destination waypoint
     *
     * @param ctrl    the control to paint on
     * @param moveDir degrees of movement
     * @param destDir degrees of destination waypoint
     */
    public void doDraw(Graphics g, int options) {
        if (map == null || g == null)
            return;
        drawArrows(g);
        return;
	/*
	 if (!dirsChanged) {
		 g.drawImage(image,mask,transparentColor,0,-minY,location.width,location.height);
		 // the transparency with a transparent color doesn't work in ewe-vm for pocketpc, it works in java-vm, ewe-vm on pocketpc2003
	 return; 
	 }
	 dirsChanged = false;
	 //super.doDraw(g, options); 
	 draw.setColor(Color.White);
	 draw.fillRect(0, 0, location.width, location.height);
	 minY = Integer.MAX_VALUE; 
	 drawArrows(draw);
	 draw.drawImage(image,mask,Color.DarkBlue,0,0,location.width,location.height); 
	 // this trick (note: wrong transparentColor) forces a redraw 
	 g.drawImage(image,mask,transparentColor,0,-minY,location.width,location.height);
	*/
    }

    private void makeArrows() {
        // draw only valid arrows
        if (moveDir < 360 && moveDir > -360) {
            if (moveDirArrow == null)
                moveDirArrow = new Point[2];
            makeArrow(moveDirArrow, moveDir, 1.0f);
        } else
            moveDirArrow = null;
        if (gotoDir < 360 && gotoDir > -360) {
            if (gotoDirArrow == null)
                gotoDirArrow = new Point[2];
            makeArrow(gotoDirArrow, gotoDir, 1.0f);
        } else
            gotoDirArrow = null;
        if (sunDir < 360 && sunDir > -360) {
            if (sunDirArrow == null)
                sunDirArrow = new Point[2];
            makeArrow(sunDirArrow, sunDir, 0.75f);
        } else
            sunDirArrow = null;
        // show northth arrow only if it has more than 1.5 degree deviation from vertical direction
        if (map != null && java.lang.Math.abs(map.rotationRad) > 1.5 / 180 * java.lang.Math.PI) {
            if (northDirArrow == null)
                northDirArrow = new Point[2];
            makeArrow(northDirArrow, 0, 1.0f); // north direction
        } else
            northDirArrow = null;

        // select moveDirColor according to difference to gotoDir
        moveDirColor = new Color(255, 0, 0); // red

        if (moveDirArrow != null && gotoDirArrow != null) {
            float diff = java.lang.Math.abs(moveDir - gotoDir);
            while (diff > 360) {
                diff -= 360.0f;
            }
            if (diff > 180) {
                diff = 360.0f - diff;
            }

            if (diff <= 5.0) {
                moveDirColor = new Color(0, 192, 0);// darkgreen
            } else if (diff <= 22.5) {
                moveDirColor = new Color(0, 255, 0);// green
            } else if (diff <= 45.0) {
                moveDirColor = new Color(255, 128, 0);// orange
            }
        }
    }

    /**
     * make (calculate) Pixel array for a single arrow
     *
     * @param g     handle for drawing
     * @param angle angle of arrow
     * @param col   color of arrow
     */
    private void makeArrow(Point[] arrow, float angle, float scale) {
        if (map == null)
            return;

        float angleRad;
        final int centerX = location.width / 2, centerY = location.height / 2;
        if (arrow[0] == null)
            arrow[0] = new Point();
        if (arrow[1] == null)
            arrow[1] = new Point();
        arrow[0].x = centerX;
        arrow[0].y = centerY;
        angleRad = angle * (float) java.lang.Math.PI / 180 + map.rotationRad;
        arrow[1].x = centerX + new Float(centerX * java.lang.Math.sin(angleRad) * scale).intValue();
        arrow[1].y = centerY - new Float(centerY * java.lang.Math.cos(angleRad) * scale).intValue();
        // g.setPen(new Pen(Color.Black,Pen.SOLID,7));
        // g.drawLine(centerX,centerY,x,y);
    }

    public void drawArrows(Graphics g) {
        drawArrow(g, northDirArrow, northDirColor);
        drawArrow(g, gotoDirArrow, gotoDirColor);
        drawArrow(g, moveDirArrow, moveDirColor);
        drawArrow(g, sunDirArrow, sunDirColor);
    }

    public void drawArrow(Graphics g, Point[] arrow, Color col) {
        if (arrow == null)
            return;
        g.setPen(new Pen(col, Pen.SOLID, arrowThickness));
        g.drawLine(arrow[0].x, arrow[0].y, arrow[1].x, arrow[1].y);
    }
}
