package CacheWolf;

import ewe.fx.*;
import ewe.graphics.*;
import ewe.sys.*;
import ewe.ui.*;

public class AttributesViewer extends CellPanel {
	private final static int ICONS_PER_ROW=6;
	private final static int ICONROWS=2;
	private static int TILESIZE=22;
	private mLabel mInfo;

	private class attInteractivePanel extends InteractivePanel {
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
	
	private class attAniImage extends AniImage {
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
	private InteractivePanel iap=new attInteractivePanel();
	
	public void showImages(Attributes att) {
		iap.images.clear();
		for (int i=0; i<att.getCount(); i++) {
			attAniImage img=new attAniImage( att.getImage(i));
			img.info=att.getInfo(i);
			img.location=new Rect((i % ICONS_PER_ROW)*TILESIZE,(i / ICONS_PER_ROW)*TILESIZE,TILESIZE,TILESIZE);
			iap.addImage(img);			
		}
		iap.repaintNow();
	}
	
/*	public void resizeTo(int width, int height) {
		super.resizeTo(width,height);
	}
*/	
}
