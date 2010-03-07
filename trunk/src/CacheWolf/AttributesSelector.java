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

public class AttributesSelector extends Panel {
	protected static int TILESIZE=17; // for small screen 
	protected static int PREF_WIDTH=180; // for small screen 
	protected static int PREF_HEIGHT=145; // for small screen 
	private long[] selectionMaskYes = {0l,0l};
	private long[] selectionMaskNo = {0l,0l};
	protected mLabel mInfo;

	public AttributesSelector() {
		if (MyLocale.getScreenWidth() > 240) { TILESIZE=22; PREF_HEIGHT=180; PREF_WIDTH=205;}
		//Rect r = new Rect(0,0,TILESIZE * ICONS_PER_ROW,TILESIZE * ICONROWS); // As on GC: 6 wide, 2 high
		iap.virtualSize =  new Rect(0,0,200,200);
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
				inc = 0;
			else if ( ((selectionMaskNo[0] & bitMask[0]) != 0 ) ||
					((selectionMaskNo[1] & bitMask[1]) != 0 ))
				inc = 1;
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
