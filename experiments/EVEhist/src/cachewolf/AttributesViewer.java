package cachewolf;

import eve.fx.*;
import eve.ui.*;
import eve.ui.game.*;

public class AttributesViewer extends Panel {
	protected static final int TILESIZE=Attribute.getImageWidth()+2;
	protected final static int ICONS_PER_ROW=MyLocale.getScreenWidth()/TILESIZE<Attributes.MAXATTRIBS ? MyLocale.getScreenWidth()/TILESIZE : Attributes.MAXATTRIBS;
	protected final static int ICONROWS=(Attributes.MAXATTRIBS+ICONS_PER_ROW-1)/ICONS_PER_ROW;
	protected Label mInfo;

	protected class AttInteractivePanel extends InteractivePanel {
		public boolean imageMovedOn(AniImage which) {
			if (!((AttAniImage)which).info.startsWith("*")) { // If text starts with * we have no explanation yet
				mInfo.setText(((AttAniImage)which).info);
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
	
	protected class AttAniImage extends AniImage {
		public String info;
		AttAniImage (Picture img) {
			super(img);
		}
	}
	
	public AttributesViewer (){
		Rect r = new Rect(0,0,TILESIZE * ICONS_PER_ROW,TILESIZE * ICONROWS); // As on GC: 6 wide, 2 high
		iap.virtualSize = r;
		iap.setFixedSize(TILESIZE * ICONS_PER_ROW,TILESIZE * ICONROWS);
		addLast(iap,CellConstants.HSTRETCH,CellConstants.FILL);
		addLast(mInfo=new Label(""),HSTRETCH,HFILL);
	}
	protected InteractivePanel iap=new AttInteractivePanel();
	
	public void showImages(Attributes att) {
		iap.images.clear();
		for (int i=0; i<att.getCount(); i++) {
			AttAniImage img=new AttAniImage( att.getImage(i));
			img.info=att.getInfo(i);
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
