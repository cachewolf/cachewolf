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

import ewe.fx.Image;
import ewe.fx.Point;
import ewe.fx.Rect;
import ewe.fx.mImage;
import ewe.graphics.AniImage;
import ewe.graphics.InteractivePanel;
import ewe.sys.Vm;
import ewe.ui.DataChangeEvent;
import ewe.ui.Panel;
import ewe.ui.mLabel;

public class AttributesSelector extends Panel {
	protected static int TILESIZE;
	protected static int W_OFFSET; // depends on Global.getPref().fontSize ?
	protected static int H_OFFSET; // depends on Global.getPref().fontSize ?
	private long[] selectionMaskYes = { 0l, 0l };
	private long[] selectionMaskNo = { 0l, 0l };
	protected mLabel mInfo;
	protected InteractivePanel iap = new attInteractivePanel();
	protected MyScrollBarPanel scp = new MyScrollBarPanel(iap);
	private int virtualWidth;

	public AttributesSelector() {
		scp.setOptions(MyScrollBarPanel.NeverShowHorizontalScrollers);
		TILESIZE = 30;
		W_OFFSET = 100;
		H_OFFSET = 150;
		if (Vm.isMobile()) {
			if (MyLocale.getScreenWidth() == 240 & MyLocale.getScreenHeight() == 320) {
				TILESIZE = 28;
				W_OFFSET = 80;
				H_OFFSET = 120;
			}
			if (MyLocale.getScreenWidth() == 320 & MyLocale.getScreenHeight() == 240) {
			}
			if (MyLocale.getScreenWidth() == 480 & MyLocale.getScreenHeight() == 640) {
			}
			if (MyLocale.getScreenWidth() == 480 & MyLocale.getScreenHeight() == 800) {
			}
			if (MyLocale.getScreenWidth() == 640 & MyLocale.getScreenHeight() == 480) {
			}
		} else {
			TILESIZE = 36;
			W_OFFSET = 106;
			H_OFFSET = 150;
		}
		iap.virtualSize = new Rect(0, 0, 0, 0); // create once
		addLast(scp, STRETCH, FILL);
		addLast(mInfo = new mLabel(""), HSTRETCH, HFILL);
	}

	public void setSelectionMasks(long[] SelectionMasks) {
		selectionMaskYes[0] = SelectionMasks[0];
		selectionMaskYes[1] = SelectionMasks[1];
		selectionMaskNo[0] = SelectionMasks[2];
		selectionMaskNo[1] = SelectionMasks[3];
		showAttributePalette();
	}

	public long[] getSelectionMasks() {
		long[] SelectionMasks = new long[4];
		SelectionMasks[0] = selectionMaskYes[0];
		SelectionMasks[1] = selectionMaskYes[1];
		SelectionMasks[2] = selectionMaskNo[0];
		SelectionMasks[3] = selectionMaskNo[1];
		return SelectionMasks;
	}

	public boolean isSetSelectionMask() {
		return selectionMaskYes[0] != 0l || selectionMaskNo[0] != 0l || selectionMaskYes[1] != 0l || selectionMaskNo[1] != 0l;
	}

	protected class attImage extends AniImage {
		public Attribute att;

		attImage(mImage img, Attribute _att) {
			super(img);
			att = _att;
		}

		attImage(attImage cp, int val) {
			att = cp.att;
			att.setInc(val);
			mImage rawImg = att.getImage();
			setMImage(rawImg.getHeight() != TILESIZE - 2 ? rawImg.scale(TILESIZE - 2, TILESIZE - 2, null, Image.FOR_DISPLAY) : rawImg);
			location = cp.location;
		}
	}

	protected class attInteractivePanel extends InteractivePanel {
		public boolean imageMovedOn(AniImage which) {
			mInfo.setText(((attImage) which).att.getMsg());
			mInfo.repaintNow();
			return true;
		}

		public boolean imageMovedOff(AniImage which) {
			mInfo.setText("");
			mInfo.repaintNow();
			return true;
		}

		public boolean imagePressed(AniImage which, Point pos) {
			if (which != null) {
				int value = ((attImage) which).att.getInc();
				value = (value + 1) % 3;
				((attImage) which).att.setInc(value);
				selectionMaskNo = ((attImage) which).att.getNoBit(selectionMaskNo);
				selectionMaskYes = ((attImage) which).att.getYesBit(selectionMaskYes);
				attImage tmpImg = new attImage(((attImage) which), value);
				removeImage(which);
				addImage(tmpImg);
				refresh();
				notifyDataChange(new DataChangeEvent(DataChangeEvent.DATA_CHANGED, this));
			}
			return true;
		}
	}

	private void showAttributePalette() {
		iap.images.clear();
		int myWidth = virtualWidth;
		int myX = 2;
		int myY = 2;
		int inc = 2;
		for (int i = 0; i < Attribute.maxAttRef; i++) {
			long[] bitMask = Attribute.getIdBit(i);
			if (((selectionMaskYes[0] & bitMask[0]) != 0) || ((selectionMaskYes[1] & bitMask[1]) != 0))
				inc = 1;
			else if (((selectionMaskNo[0] & bitMask[0]) != 0) || ((selectionMaskNo[1] & bitMask[1]) != 0))
				inc = 0;
			else
				inc = 2;
			Attribute att = new Attribute(i, inc);
			mImage rawImg = att.getImage();
			attImage img = new attImage(rawImg.getHeight() != TILESIZE - 2 ? rawImg.scale(TILESIZE - 2, TILESIZE - 2, null, Image.FOR_DISPLAY) : rawImg, att);

			if (myX + TILESIZE > myWidth) {
				myX = 2;
				myY += TILESIZE;
			}
			img.location = new Rect(myX, myY, TILESIZE, TILESIZE);
			iap.addImage(img);
			myX += TILESIZE;

		}
		iap.repaintNow();
	}

	private void setIapSize(int width, int height) {
		iap.setPreferredSize(width, height);
		Global.getPref().log("[AttributesSelector:changeIapSize]  pref. area: " + width + "x" + height);

		int anzPerWidth = width / (TILESIZE + 2) - 1;
		virtualWidth = anzPerWidth * (TILESIZE + 2);
		double max = Attribute.maxAttRef;
		int anzPerHeight = (int) java.lang.Math.ceil(max / anzPerWidth);
		iap.virtualSize.set(0, 0, virtualWidth, anzPerHeight * (TILESIZE + 2));
		Global.getPref().log("[AttributesSelector:setIapSize] virt. area: " + virtualWidth + "x" + anzPerHeight * (TILESIZE + 2));

	}

	public void changeIapSize(int width, int height) {
		Global.getPref().log("[AttributesSelector:changeIapSize]  max. area: " + width + "x" + height);
		setIapSize(width - W_OFFSET, height - H_OFFSET);
		showAttributePalette();
	}

}