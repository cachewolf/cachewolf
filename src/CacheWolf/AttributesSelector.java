package CacheWolf;

import ewe.fx.Image;
import ewe.fx.Point;
import ewe.fx.Rect;
import ewe.fx.mImage;
import ewe.graphics.AniImage;
import ewe.graphics.InteractivePanel;
import ewe.ui.CellConstants;
import ewe.ui.DataChangeEvent;
import ewe.ui.Panel;
import ewe.ui.mLabel;
import ewe.sys.*;

public class AttributesSelector extends Panel {
	protected static int TILESIZE=16; // for small screen 
	protected static int PREF_WIDTH=180; // for small screen 
	protected static int PREF_HEIGHT=150; // for small screen 
	private long[] selectionMaskYes = {0l,0l};
	private long[] selectionMaskNo = {0l,0l};
	protected mLabel mInfo;

	public AttributesSelector() {
		if(!Vm.isMobile()) { TILESIZE = 30; PREF_HEIGHT =(TILESIZE+2)*10; PREF_WIDTH =(TILESIZE+2)*9;}
		
		if(Vm.isMobile()){
			if(MyLocale.getScreenWidth() == 320 & MyLocale.getScreenHeight() == 240){
				TILESIZE = 14; PREF_HEIGHT = 120; PREF_WIDTH = 180;
			}
			if(MyLocale.getScreenWidth() == 480 & MyLocale.getScreenHeight() == 640){
				TILESIZE = 25; PREF_HEIGHT =(TILESIZE+2)*10; PREF_WIDTH =(TILESIZE+2)*9;
			}
			if(MyLocale.getScreenWidth() == 480 & MyLocale.getScreenHeight() == 800){
				TILESIZE = 30; PREF_HEIGHT =(TILESIZE+2)*10; PREF_WIDTH =(TILESIZE+2)*9;
			}
			if(MyLocale.getScreenWidth() == 640 & MyLocale.getScreenHeight() == 480){
				TILESIZE = 20; PREF_HEIGHT =(TILESIZE+2)*7; PREF_WIDTH =(TILESIZE+2)*12;
			}
		}
		iap.virtualSize =  new Rect(10,10,400,400);
		iap.setPreferredSize(PREF_WIDTH, PREF_HEIGHT);
		addLast(iap,CellConstants.STRETCH,FILL);
		addLast(mInfo=new mLabel(""),HSTRETCH,HFILL);
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
		return  selectionMaskYes[0] != 0l || selectionMaskNo[0] != 0l ||
				selectionMaskYes[1] != 0l || selectionMaskNo[1] != 0l;
	}

	protected class attImage extends AniImage {
		public Attribute att;		
		attImage (mImage img, Attribute _att) {
			super(img);
			att=_att;
		}		
		attImage(attImage cp, int val) {
			att=cp.att;
			att.setInc(val);
			mImage rawImg=att.getImage();
			setMImage (rawImg.getHeight()!=TILESIZE-2 ? rawImg.scale(TILESIZE-2,TILESIZE-2,null,Image.FOR_DISPLAY) : rawImg  );
			location = cp.location;
		}
	}
	
	protected class attInteractivePanel extends InteractivePanel {
		public boolean imageMovedOn(AniImage which) {
			mInfo.setText(((attImage)which).att.getMsg());			
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
				int value=((attImage)which).att.getInc();
				value=(value + 1) % 3;
				((attImage)which).att.setInc(value);
				selectionMaskNo=((attImage)which).att.getNoBit(selectionMaskNo);
				selectionMaskYes=((attImage)which).att.getYesBit(selectionMaskYes);
				attImage tmpImg = new attImage(((attImage)which), value );
				removeImage(which);
				addImage(tmpImg);
				refresh();
				notifyDataChange(new DataChangeEvent(DataChangeEvent.DATA_CHANGED,this));
			}
			return true;
		}
	}
	protected InteractivePanel iap=new attInteractivePanel();

	private void showAttributePalette() {
		iap.images.clear();
		int myWidth = PREF_WIDTH;
		int myX = 2; int myY = 2;
		int inc = 2;
		for (int i = 0; i < Attribute.maxAttRef; i++) {
			long[] bitMask = Attribute.getIdBit(i);
			if ( ((selectionMaskYes[0] & bitMask[0]) != 0 ) || 
					((selectionMaskYes[1] & bitMask[1]) != 0 ))
				inc = 1;
			else if ( ((selectionMaskNo[0] & bitMask[0]) != 0 ) ||
					((selectionMaskNo[1] & bitMask[1]) != 0 ))
				inc = 0;
			else
				inc = 2;			
			Attribute att = new Attribute(i,inc); 
			mImage rawImg=att.getImage();
			attImage img=new attImage(rawImg.getHeight()!=TILESIZE-2 ? rawImg.scale(TILESIZE-2,TILESIZE-2,null,Image.FOR_DISPLAY) : rawImg, att);

			if (myX+TILESIZE > myWidth) {
				myX = 2;
				myY += TILESIZE;
			}
			img.location=new Rect(myX,myY,TILESIZE,TILESIZE);
			iap.addImage(img);
			myX += TILESIZE;
			
		}
		iap.repaintNow();
	}
	
/*	public void resizeTo(int width, int height) {
		super.resizeTo(width,height);
	}
*/	
}
