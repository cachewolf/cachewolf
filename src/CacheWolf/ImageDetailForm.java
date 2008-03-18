package CacheWolf;
import ewe.graphics.*;
import ewe.fx.*;
import ewe.ui.*;


/**
* Class creates a view on the image scaled
* to the application size, but only if the image is larger than
* the available app size.
*/
public class ImageDetailForm extends Form{
	ImageInteractivePanel ipp = new ImageInteractivePanel();
	ScrollBarPanel scp;
	
	public ImageDetailForm(String imgLoc, Preferences p){
		scp = new MyScrollBarPanel(ipp);
		ipp.setImage(imgLoc);
		this.title = "Image";
		this.setPreferredSize(p.myAppWidth, p.myAppHeight);
		this.addLast(scp, CellConstants.STRETCH, CellConstants.FILL);
	}
	
	public void onEvent(Event ev) {
		if (ev instanceof ControlEvent && ev.type==ControlEvent.EXITED) {
			ev.consumed=true;
			this.close(0);
		} else super.onEvent(ev);
	}
}

