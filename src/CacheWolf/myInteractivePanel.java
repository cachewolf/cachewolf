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
import CacheWolf.database.CacheSize;
import CacheWolf.database.CacheType;
import ewe.fx.Color;
import ewe.fx.Font;
import ewe.fx.FontMetrics;
import ewe.fx.Graphics;
import ewe.fx.IconAndText;
import ewe.fx.Image;
import ewe.fx.Point;
import ewe.fx.Rect;
import ewe.fx.mImage;
import ewe.graphics.AniImage;
import ewe.graphics.ImageDragContext;
import ewe.graphics.InteractivePanel;
import ewe.sys.Vm;
import ewe.ui.Control;
import ewe.ui.DragContext;
import ewe.ui.Gui;
import ewe.ui.PenEvent;
import ewe.ui.mList;

/**
 * This class allows handling of a user click on a cache
 * in the radar panel.
 * 
 * @see RadarPanel
 */
public class myInteractivePanel extends InteractivePanel {

    boolean penMoving = false;
    int x1, y1, x2, y2 = 0;
    static Color RED = new Color(255, 0, 0);
    //Font font = new Font("gui", Font.BOLD,Global.pref.fontSize);
    FontMetrics fm = null;
    long timePenOn = 0;
    AniImage imgInfo;
    String strDifficulty = MyLocale.getMsg(1120, "Diff");
    String strTerrain = MyLocale.getMsg(1121, "Terr");
    AniImage imgDrag; // Allows the dragging of the cache into the cachelist
    boolean canScroll = true;
    {
	font = new Font("gui", Font.BOLD, Global.pref.fontSize);
	fm = getFontMetrics(font);
    }

    private void clearInfo() {
	removeImage(imgInfo);
	imgInfo = null;
	refresh();
	onImage = null;
    }

    public void imageClicked(AniImage which, Point pos) {
	long timePenOff = Vm.getTimeStampLong();
	// If the pen rested more than 500 msec, we only display the info and don't treat it as a click
	if (timePenOff - timePenOn < 500 || !Vm.isMobile()) {
	    new String();
	    if (which instanceof RadarPanelImage) {
		RadarPanelImage ich = (RadarPanelImage) which;
		Global.mainTab.clearDetails();
		Global.mainTab.selectAndActive(ich.rownum);
	    }
	} else {
	    if (imgInfo != null)
		clearInfo();
	}
    }

    public boolean imageMovedOn(AniImage which) {
	timePenOn = Vm.getTimeStampLong();
	setFont(font);
	RadarPanelImage imgRP = (RadarPanelImage) which;
	CacheDB cacheDB = Global.profile.cacheDB;
	CacheHolder ch = cacheDB.get(imgRP.rownum);
	wayPoint = ch.getWayPoint();
	String s = wayPoint + "  " + CacheSize.getExportShortId(ch.getCacheSize()) + " / " + strDifficulty + "=" + ch.getHard() + "  " + strTerrain + "=" + ch.getTerrain();
	String s1 = ch.getCacheName();
	if (s1.length() > 40)
	    s1 = s1.substring(0, 40);
	int tw = fm.getTextWidth(s) + 2;
	int tw1 = fm.getTextWidth(s1) + 2;
	if (tw1 > tw)
	    tw = tw1;
	int h = fm.getHeight();
	Image img = new Image(tw, h + h);
	Graphics g = new Graphics(img);
	g.setFont(font);
	g.setColor(new Color(0, 0, 255));
	g.fillRect(0, 0, tw, h + h);
	g.setColor(Color.White);
	g.drawText(s, 1, 1);
	g.drawText(s1, 1, h);
	imgInfo = new AniImage(img);
	Rect r = getVisibleArea(null);
	imgInfo.setLocation(r.x, r.y); // Place the info at top left corner
	imgInfo.properties = mImage.IsNotHot;
	addImage(imgInfo);
	refreshOnScreen(imgInfo);
	imgDrag = which;
	return true;
    }

    public boolean imageMovedOff(AniImage which) {
	clearInfo();
	return true;
    }

    public void onPenEvent(PenEvent ev) {
	super.onPenEvent(ev);
	if (ev.type == PenEvent.PEN_UP) {
	    clearInfo();
	    // The next line is needed due to a bug in EWE (it does not call penReleased)
	    if (isDragging)
		penReleased(new Point(ev.x, ev.y));
	}
    }

    ///////////////////////////////////////////////////
    //  Allow the caches to be dragged into a cachelist
    ///////////////////////////////////////////////////

    String wayPoint;

    public void startDragging(DragContext dc) {
	if (!MainForm.itself.cacheListVisible)
	    return;
	CacheHolder ch = Global.profile.cacheDB.get(wayPoint);
	if (ch != null) {
	    IconAndText icnDrag = new IconAndText();
	    icnDrag.addColumn(CacheType.getTypeImage(ch.getType()));
	    icnDrag.addColumn(ch.getWayPoint());
	    dc.dragData = dc.startImageDrag(icnDrag, new Point(8, 8), this);
	    canScroll = false;
	}
    }

    public void stopDragging(DragContext dc) {
	canScroll = true;
    }

    public boolean imageBeginDragged(AniImage which, Point pos) {
	if (!MainForm.itself.cacheListVisible)
	    return false;
	canScroll = false;
	clearInfo();
	wayPoint = null;
	AniImage dragImage = null;
	if (which instanceof RadarPanelImage) {
	    RadarPanelImage imgRP = (RadarPanelImage) which;
	    CacheDB cacheDB = Global.profile.cacheDB;
	    CacheHolder ch = cacheDB.get(imgRP.rownum);
	    wayPoint = ch.getWayPoint();

	    int tw, th;
	    Image img = new Image(tw = fm.getTextWidth(wayPoint + 15), th = fm.getHeight() > 15 ? fm.getHeight() : 15);
	    Graphics g = new Graphics(img);
	    g.setFont(font);
	    g.setColor(Color.White);
	    g.fillRect(0, 0, tw, th);
	    g.setColor(new Color(255, 0, 0));
	    g.drawText(wayPoint, 15, 1);
	    g.drawImage(which.image, 0, 0);
	    dragImage = new AniImage(img);
	    dragImage.properties |= mImage.IsMoveable;
	    dragImage.setLocation(pos.x, pos.y);
	}
	return super.imageBeginDragged(dragImage, pos);
    }

    public boolean imageDragged(ImageDragContext drag, Point pos) {
	if (drag.image != null) {
	    /*			    Point p = Gui.getPosInParent(this,getWindow());
	    			 	p.x += pos.x-origin.x;
	    			 	p.y += pos.y-origin.y;
	    			 	Control c = getWindow().findChild(p.x,p.y);
	    */
	    drag.clearPendingDrags();
	}
	return super.imageDragged(drag, pos);
    }

    public boolean imageNotDragged(ImageDragContext drag, Point pos) {
	if (drag.image != null) {
	    images.remove(drag.image);
	    drag.image = null;
	    refresh();
	}
	Point p = Gui.getPosInParent(this, getWindow());
	p.x += drag.curPoint.x - origin.x;
	p.y += drag.curPoint.y - origin.y;
	Control c = getWindow().findChild(p.x, p.y);
	if (c instanceof mList && c.text.equals("CacheList")) {
	    if (MainForm.itself.cacheList.addCache(wayPoint)) {
		c.repaintNow();
		((mList) c).makeItemVisible(((mList) c).itemsSize() - 1);
	    }
	}
	return false;
    }

    public boolean canScreenScroll() {
	return canScroll;
    }

    public boolean scroll(int dx, int dy, Point moved) {
	if (canScroll)
	    return super.scroll(dx, dy, moved);
	else
	    return false;
    }
}
