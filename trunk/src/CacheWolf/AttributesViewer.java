    /*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
    See http://developer.berlios.de/projects/cachewolf/
    for more information.
    Contact: 	bilbowolf@users.berlios.de
    			kalli@users.berlios.de

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

import ewe.fx.Rect;
import ewe.fx.mImage;
import ewe.graphics.AniImage;
import ewe.graphics.InteractivePanel;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.mLabel;

public class AttributesViewer extends CellPanel {
	protected static int TILESIZE=Attribute.getImageWidth()+2;
	protected final static int ICONS_PER_ROW=MyLocale.getScreenWidth()/TILESIZE<Attributes.MAXATTRIBS ? MyLocale.getScreenWidth()/TILESIZE : Attributes.MAXATTRIBS;
	protected final static int ICONROWS=(Attributes.MAXATTRIBS+ICONS_PER_ROW-1)/ICONS_PER_ROW;
	protected mLabel mInfo;

	protected class attInteractivePanel extends InteractivePanel {
		public boolean imageMovedOn(AniImage which) {
			if (!((attAniImage)which).info.startsWith("*")) { // If text starts with * we have no explanation yet
				mInfo.setText(((attAniImage)which).info);
				mInfo.repaintNow();
			}
			return true;
		}
		public boolean imageMovedOff(AniImage which) {
			mInfo.setText("");
			mInfo.repaintNow();
			return true;
		}
	}
	
	protected class attAniImage extends AniImage {
		public String info;
		attAniImage (mImage img) {
			super(img);
		}
	}
	
	public AttributesViewer (){
		Rect r = new Rect(0,0,TILESIZE * ICONS_PER_ROW,TILESIZE * ICONROWS); // As on GC: 6 wide, 2 high
		iap.virtualSize = r;
		iap.setFixedSize(TILESIZE * ICONS_PER_ROW,TILESIZE * ICONROWS);
		addLast(iap,CellConstants.HSTRETCH,CellConstants.FILL);
		addLast(mInfo=new mLabel(""),HSTRETCH,HFILL);
	}
	protected InteractivePanel iap=new attInteractivePanel();
	
	public void showImages(Attributes atts) {
		iap.images.clear();
		for (int i=0; i<atts.count(); i++) {
			attAniImage img=new attAniImage( atts.getAttribute(i).getImage());
			img.info=atts.getAttribute(i).getMsg();
			img.location=new Rect((i % ICONS_PER_ROW)*TILESIZE,(i / ICONS_PER_ROW)*TILESIZE,TILESIZE,TILESIZE);
			iap.addImage(img);			
		}
		iap.repaintNow();
	}
	
	public void clear() {
		iap.images.clear();
	}
/*	public void resizeTo(int width, int height) {
		super.resizeTo(width,height);
	}
*/	
}
