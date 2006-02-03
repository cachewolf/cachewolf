package CacheWolf;
import ewe.graphics.*;
import ewe.sys.*;
import ewe.fx.*;
import ewe.ui.*;
import ewe.util.*;

/**
* Class creates a view on the image scaled
* to the application size, but only if the image is larger than
* the available app size.
*/
public class ImageDetailForm extends Form{
	String location = new String();
	int origH, origW;
	int state = 0; // 0 = nothing, -1 = scaled to app, 1 = scaled to original size
	int scaleX = 0, scaleY = 0;
	Preferences pref;
	ImageInteractivePanel ipp = new ImageInteractivePanel();
	AniImage ai;
	ScrollBarPanel scp;
	
	public ImageDetailForm(String imgLoc, Preferences p){
		scp = new ScrollBarPanel(ipp);
		setUp(imgLoc, p);
		this.title = "Image";
		this.setPreferredSize(pref.myAppWidth, pref.myAppHeight);
		this.addLast(scp.getScrollablePanel(), this.STRETCH, this.FILL);
	}
	
	public ImageDetailForm(){
	}

	
	public void setUp(String imgLoc, Preferences p){
		pref = p;	
		location = imgLoc;
		mImage mI = new mImage(imgLoc);
		double scaleFactorX = 1, scaleFactorY = 1, scaleFactor = 1;
		origH = mI.getHeight();
		origW = mI.getWidth();
		if(origW >= pref.myAppWidth) scaleFactorX = pref.myAppWidth/(double)origW;
		if(origH >= pref.myAppHeight) scaleFactorY = pref.myAppHeight/(double)origH;
		if(scaleFactorX >= scaleFactorY) scaleFactor = scaleFactorY;
		if(scaleFactorY >= scaleFactorX) scaleFactor = scaleFactorX;
		state = -1;
		scaleX = (int)(origW*scaleFactor);
		scaleY = (int)(origH*scaleFactor);
		mI = mI.scale(scaleX, scaleY, null, 0);
		ai = new AniImage(mI);
		ai.setLocation(0,0);
		ipp.addImage(ai);
		ipp.setPreferredSize(origW, origH);
		ipp.setParams(state, scaleX, scaleY, origH, origW, scp, imgLoc);
	}
}

