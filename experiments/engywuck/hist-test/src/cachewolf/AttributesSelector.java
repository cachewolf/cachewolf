package cachewolf;

import eve.fx.*;
import eve.sys.*;
import eve.ui.*;
import eve.ui.game.*;
import eve.ui.event.*;

public class AttributesSelector extends Panel {
	protected static final int TILESIZE=22; // Here we always use the small icons thus tilesize=22
	public long selectionMaskYes = 0;
	public long selectionMaskNo = 0;
	protected Label mInfo;

	public AttributesSelector() {
		//Rect r = new Rect(0,0,TILESIZE * ICONS_PER_ROW,TILESIZE * ICONROWS); // As on GC: 6 wide, 2 high
		iap.virtualSize = new Rect(0,0,200,200);
		iap.setPreferredSize(170, 155);
		addLast(iap,CellConstants.STRETCH,FILL);
		addLast(mInfo=new Label(""),HSTRETCH,HFILL);
	}

	public void setSelectionMasks(long yes, long no) {
		selectionMaskYes = yes;
		selectionMaskNo = no;
		showAttributePalette();
	}

	protected class AttAniImage extends AniImage {
		public String info;
		public String attrName;
		public String value;
		public int attrNr;
		public long bitMask;
		AttAniImage (ImageData img) {
			super(img);
		}
		AttAniImage (AttAniImage cp, String val) {
			//super(null);
			PixelBuffer rawImg=new PixelBuffer(Attribute.getImageDir() + cp.attrName + val );
			change (rawImg.getHeight()!=20 ? rawImg.scale(20,20) : rawImg  );
			value = val;
			info = MyLocale.getMsg( value.equals("-no.gif") ? (2500+cp.attrNr-1) : 2500+cp.attrNr,"No attribute info found");
			attrName = cp.attrName;
			location = cp.location;
			attrNr = cp.attrNr;
			bitMask = cp.bitMask;
		}
	}
	
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
		public boolean imagePressed(AniImage which, Point pos) {
			if (which != null) {
				String value = ((AttAniImage)which).value;
				long bit = ((AttAniImage)which).bitMask;
				if (value.equals("-non.gif")) {
					selectionMaskYes |= bit;
					selectionMaskNo  &= ~bit;
					value="-yes.gif";
				} else if (value.equals("-yes.gif")) {
					selectionMaskYes &= ~bit;
					selectionMaskNo  |= bit;
					value="-no.gif";
				} else {
					selectionMaskYes &= ~bit;
					selectionMaskNo  &= ~bit;
					value="-non.gif";
				}
				AttAniImage tmpImg = new AttAniImage( (AttAniImage)which, value );
				removeImage(which);
				addImage(tmpImg);
				//System.out.println ("AniImage pressed: " + ((attAniImage)which).info);
				refresh();
				notifyDataChange(new DataChangeEvent(DataChangeEvent.DATA_CHANGED,this));
			}
			return true;
		}
	}
	protected InteractivePanel iap=new AttInteractivePanel();

	public void showAttributePalette() {
		iap.images.clear();
		int width = 170;
		int x = 2; int y = 2;
		long bitMask = 0;
		String attrName;
		String value;
		for (int i=0; i < Attribute.attributeNames.length; ++i) {
			if (Attribute.attributeNames[i].endsWith("-yes.gif")) {
				attrName = Attribute.attributeNames[i].substring(0,Attribute.attributeNames[i].length()-8);
				bitMask = ( 1l << ( (long)(java.lang.Math.ceil(i / 2.0) - 1.0) ) );
				if ( (selectionMaskYes & bitMask) != 0 )
					value = "-yes.gif";
				else if ( (selectionMaskNo & bitMask) != 0 )
					value = "-no.gif";
				else
					value = "-non.gif";
				PixelBuffer rawImg=new PixelBuffer(Attribute.getImageDir()+attrName+value);	
				AttAniImage img=new AttAniImage((rawImg.getHeight()!=20 ? rawImg.scale(20,20) : rawImg ));
				img.info=MyLocale.getMsg(2500+i,"No attribute info found");
				img.value=value;
				img.attrName=attrName;
				img.attrNr = i;
				img.bitMask = bitMask;

				if (x+TILESIZE > width) {
					x = 2;
					y += TILESIZE;
				}
				img.location=new Rect(x,y,TILESIZE,TILESIZE);
				//System.out.println("img.location=new Rect("+x+","+y+","+TILESIZE+","+TILESIZE+");");
				iap.addImage(img);
				x += TILESIZE;
			}
		}
		iap.repaintNow();
	}
	
/*	public void resizeTo(int width, int height) {
		super.resizeTo(width,height);
	}
*/	
}
