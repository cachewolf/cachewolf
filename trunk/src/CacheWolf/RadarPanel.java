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
package CacheWolf;

import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheType;
import CacheWolf.utils.Common;
import ewe.fx.Color;
import ewe.fx.Dimension;
import ewe.fx.Font;
import ewe.fx.FontMetrics;
import ewe.fx.Graphics;
import ewe.fx.Image;
import ewe.fx.Rect;
import ewe.fx.mImage;
import ewe.graphics.AniImage;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.mButton;

/**
 * The radar panel. Displays the caches around a centre point.<br>
 * Handles scaling as well as toggling the different views in the radar panel.<br>
 * Also handles clicking on a cache.<br>
 * Class ID=500
 */
public class RadarPanel extends CellPanel {
    mButton btnMinus;
    mButton btnToggle;
    mButton btnPlus;
    int toggleMod = 0; // 0 = cacheicons, 1= cacheWP, 2 = cacheNames
    CacheDB cacheDB;
    myInteractivePanel iActP;
    double scale;
    int scaleKm = 2;
    int centerX, centerY;
    int height, width;
    CacheHolder selectedWaypoint = null;
    boolean penMoving = false;
    int x1, y1, x2, y2 = 0;
    boolean reCenterImage = true;
    private final static Color RED = new Color(255, 0, 0);
    private final static Color GREEN = new Color(0, 255, 0);
    private final static Color YELLOW = new Color(255, 255, 0);

    /**
     * Constructor for the radar panel.
     * Loads images, sets up the interactive panel and
     * "navigation" buttons.
     */
    public RadarPanel() {
	btnMinus = GuiImageBroker.getButton("-", "minus");
	btnToggle = GuiImageBroker.getButton("Toggle", "toggle");
	btnPlus = GuiImageBroker.getButton("+", "plus");

	final CellPanel buttonPanel = new CellPanel();
	buttonPanel.addNext(btnMinus, CellConstants.HSTRETCH, (CellConstants.FILL | CellConstants.WEST));
	buttonPanel.addNext(btnToggle, CellConstants.HSTRETCH, CellConstants.FILL);
	buttonPanel.addLast(btnPlus, CellConstants.HSTRETCH, (CellConstants.FILL | CellConstants.EAST));

	if (!Preferences.itself().tabsAtTop)
	    this.addLast(buttonPanel, CellConstants.HSTRETCH, CellConstants.HFILL);
	this.addLast(iActP = new myInteractivePanel(), CellConstants.STRETCH, CellConstants.FILL);
	if (Preferences.itself().tabsAtTop)
	    this.addLast(buttonPanel, CellConstants.HSTRETCH, CellConstants.HFILL);
    }

    /**
     * Informs the radar panel on preferences and currently loaded cache
     * database. It also calculates the maximum size available for drawing
     * the radar.
     */
    public void setParam(CacheDB db, CacheHolder sWp) {
	selectedWaypoint = sWp;
	cacheDB = db;
	height = (Preferences.itself().myAppHeight) * 6 / 5; // add 10% each at top/bottom
	width = (Preferences.itself().myAppWidth) * 6 / 5;
    }

    // Call this after the centre has changed to re-center the radar panel
    public void recenterRadar() {
	reCenterImage = true;
    }

    /**
     * Public method to draw the different caches and the
     * radar background
     */
    public void drawThePanel() {
	// If there are any images remove them!
	final int anz = iActP.images.size();
	for (int i = 0; i < anz; i++) {
	    iActP.removeImage((AniImage) iActP.images.get(0));
	}
	iActP.refresh();
	drawBackground();
	drawCaches();
	iActP.repaintNow();
	if (reCenterImage) {
	    // Hack to scroll to left origin for a defined position for subsequent
	    // scroll which centers the image
	    iActP.scroll(-1000, -1000);
	    final Dimension dispSize = getDisplayedSize(null);
	    iActP.scroll((width - dispSize.width) / 2, (height - dispSize.height + btnMinus.getSize(null).height) / 2);
	    reCenterImage = false;
	}
    }

    /**
     * Private method to draw the caches.
     */
    private void drawCaches() {
	final Font radarFont = new Font("Gui", Font.BOLD, Preferences.itself().fontSize);
	final FontMetrics fm = getFontMetrics(radarFont);
	AniImage aImg;
	RadarPanelImage rpi;
	int drX, drY = 0;
	CacheHolder holder;
	double degrees;
	final double pi180 = java.lang.Math.PI / 180.0;
	for (int i = cacheDB.size() - 1; i >= 0; i--) {
	    holder = cacheDB.get(i);
	    if (holder.isVisible() && holder.getPos().isValid()) {
		degrees = holder.degrees * pi180;
		drX = new Float(holder.kilom / scale * java.lang.Math.sin(degrees)).intValue();
		drY = -new Float(holder.kilom / scale * java.lang.Math.cos(degrees)).intValue();
		if (centerX + drX >= 0 && centerY + drY >= 0 && centerX + drX <= width && centerY + drY <= height) {
		    if (toggleMod > 0) {
			String s;
			if (toggleMod == 1)
			    s = holder.getWayPoint();
			else
			    s = holder.getCacheName();
			if (s.length() > 0) {
			    int tw;
			    final Image img = new Image(tw = fm.getTextWidth(s), fm.getHeight());
			    final Graphics g = new Graphics(img);
			    g.setFont(radarFont);
			    g.setColor(Color.Black);
			    g.fillRect(0, 0, tw, fm.getHeight());
			    g.setColor(Color.White);
			    g.drawText(s, 0, 0);
			    aImg = new AniImage(img);
			    aImg.setLocation(centerX + drX + 5, centerY + drY);
			    aImg.transparentColor = Color.Black;
			    aImg.properties = mImage.IsNotHot;
			    iActP.addImage(aImg);
			}
		    }
		    Image imgCache = CacheType.getBigCacheIcon(holder);
		    rpi = new RadarPanelImage(imgCache);
		    rpi.wayPoint = holder.getWayPoint();
		    rpi.rownum = i;
		    final int dx = imgCache.getWidth();
		    final int dy = imgCache.getHeight();
		    rpi.setLocation(centerX + drX - dx / 2, centerY + drY - dy / 2);
		    iActP.addImage(rpi);
		    if (holder == selectedWaypoint) { // Draw red circle around selected wpt
			final int diag = (int) (java.lang.Math.sqrt(dx * dx + dy * dy) + 0.5);
			final Image imgCircle = new Image(diag, diag);
			final Graphics gCircle = new Graphics(imgCircle);
			gCircle.setColor(Color.Black);
			gCircle.fillRect(0, 0, diag, diag);
			gCircle.setColor(RED);
			gCircle.drawEllipse(0, 0, diag, diag);
			aImg = new AniImage(imgCircle);
			aImg.setLocation(centerX + drX - diag / 2, centerY + drY - diag / 2);
			aImg.transparentColor = Color.Black;
			aImg.properties = mImage.IsNotHot;
			iActP.addImage(aImg);
		    }
		}// if center...
	    }// if is_black...
	}
    }

    /**
     * Private method to draw the black background and green radar.
     * Also calculates some other parameters.
     * Always call this before calling drawCaches().
     */
    private void drawBackground() {
	final Rect r = new Rect(new Dimension(width, height));
	iActP.virtualSize = r;
	iActP.refresh();
	final Image img = new Image(width, height);
	final Graphics g = new Graphics(img);
	g.setColor(Color.Black);
	g.fillRect(0, 0, width, height);

	if (width < height) {
	    scale = (double) scaleKm / (double) height;
	} else {
	    scale = (double) scaleKm / (double) width;
	}
	centerX = (width / 2);
	centerY = (height / 2);

	g.setColor(GREEN);
	// Draw rings each 10 km
	for (int i = 1; i <= scaleKm / 10; i++) {
	    drawRangeRing(g, (float) (i * 10));
	}

	// Draw 1 to 5 km rings only if we have zoomed in (useful for cities with high density of caches)
	if (scaleKm <= 25) {
	    g.setColor(YELLOW);
	    drawRangeRing(g, 5.0f);
	    if (scaleKm <= 10) {
		drawRangeRing(g, 2.0f);
		drawRangeRing(g, 1.0f);
	    }
	    if (scaleKm <= 5) {
		drawRangeRing(g, 0.5f);
	    }
	}

	g.drawLine(centerX, 0, centerX, height);
	g.drawLine(0, centerY, width, centerY);
	g.free();
	final AniImage aImg = new AniImage(img);
	// iActP.addImage(aImg);
	iActP.backgroundImage = img;
	final int xPos = (Preferences.itself().myAppWidth / 2 - width / 2);
	aImg.setLocation(xPos, 0);
	aImg.refresh();
    }

    public void drawRangeRing(Graphics g, float radius) {
	int pixelRadius = (int) (radius / scale);
	g.drawEllipse(centerX - pixelRadius, centerY - pixelRadius, pixelRadius * 2, pixelRadius * 2);
	if (radius < 1.0) {
	    String s = Common.DoubleToString(radius * 1000, 0, 0);
	    g.drawText(s + " m", centerX - pixelRadius * 7 / 10, // ~ radius / sqrt(2)
		    centerY - pixelRadius * 7 / 10);
	} else {
	    String s = Common.DoubleToString(radius, 0, 0);
	    g.drawText(s + " km", centerX - pixelRadius * 7 / 10, // ~ radius / sqrt(2)
		    centerY - pixelRadius * 7 / 10);
	}
    }

    public void onEvent(Event ev) {
	if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
	    if (ev.target == btnPlus) {
		if (scaleKm > 10)
		    scaleKm = scaleKm - 10;
		else if (scaleKm == 10)
		    scaleKm = 5;
		else if (scaleKm == 5)
		    scaleKm = 2;
		else
		    scaleKm = 1;
		drawThePanel();
	    }
	    if (ev.target == btnMinus) {
		if (scaleKm == 1)
		    scaleKm = 2;
		else if (scaleKm == 2)
		    scaleKm = 5;
		else if (scaleKm == 5)
		    scaleKm = 10;
		else
		    scaleKm = scaleKm + 10;
		drawThePanel();
	    }
	    if (ev.target == btnToggle) {
		toggleMod++;
		if (toggleMod > 2)
		    toggleMod = 0;
		drawThePanel();
	    }
	}

    }
}
