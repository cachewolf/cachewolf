package cachewolf;
import eve.fx.*;
import eve.ui.*;
import eve.ui.game.AniImage;
import eve.ui.game.InteractivePanel;
import eve.sys.Event;
import eve.ui.event.ControlEvent;
import eve.fx.Fx;

/**
* Class creates a view on the image scaled
* to the application size, but only if the image is larger than
* the available app size.
*/
public class ImageDetailScreen extends Form{
	ImageInteractivePanel ipp = new ImageInteractivePanel();
	ScrollBarPanel scp;
	
	public ImageDetailScreen(String imgLoc, Preferences p){
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

//#####################################################################################
//  ImageInteractivePanel	
//#####################################################################################
	
	/**
	*	This class handles the resizing im images
	*/
	private class ImageInteractivePanel extends InteractivePanel{
		int state = -1; // 0 = nothing, -1 = scaled to app, 1 = scaled to original size
		//ScrollBarPanel scp;
		String imgLoc = "";
		AniImage pic=null;
		
		public ImageInteractivePanel() {
			super();
			this.setMenu(mClose);
		}
		
		private Menu mClose = new Menu(new String[]{
				"Close"},"");
		
		public void resizeTo(int w, int h) {
			this.width = w;
			this.height = h;
			if (state == -1) fitImageToWindow();
			virtualSize = new Rect(0, 0, java.lang.Math.max(w, pic.getWidth()), java.lang.Math.max(h, pic.getHeight()));
			checkScrolls();
			super.resizeTo(w, h);
		}

		public void setImage(String filename) {
			imgLoc = filename;
			Picture mI = new Picture(imgLoc);
			if (pic != null) {
				this.removeImage(pic);
				pic.freeImage();
				pic.free();
			}
			pic = new AniImage(mI);
			pic.setLocation(0, 0);
			// mI.free(); this works in the java-VM, but it will delete the image in the ewe-vm --> leave it commeted out
			this.addImage(pic);
			virtualSize = new Rect(pic.getSize(null));
			checkScrolls();
		}
			
		public void fitImageToWindow() {
			Rect s = this.parent.getRect();
			int ww = pic.getWidth();
			int wh = pic.getHeight();
			double scale =  java.lang.Math.max((double)ww/(double)s.width, (double)wh/(double)s.height);
			if (scale != 1){
				this.removeImage(pic);
				AniImage tmp = new AniImage(Fx.scaleImage(pic.getImageData(),(int)(ww/scale), (int)(wh/scale)));
				pic.freeImage();
				pic.free();
				pic = tmp;
				pic.setLocation(0, 0);
				this.addImage(pic);
				virtualSize = new Rect(pic.getSize(null));
				checkScrolls();
			}
		}
			
		
		public void imageClicked(AniImage which, Point pos){
			state = -state;
			if(state == 1){
				setImage(imgLoc);
				this.repaintNow();
			}
			if(state == -1){
				fitImageToWindow();
				this.repaintNow();
			}
		}

		public void penRightReleased(Point p){
				menuState.doShowMenu(p,true,null); // direct call (not through doMenu) is neccesary because it will exclude the whole table
		}
		
		public void penHeld(Point p){
				menuState.doShowMenu(p,true,null); 
		}
		public void popupMenuEvent(Object selectedItem){
			postEvent(new ControlEvent(ControlEvent.EXITED,this));
		}
		public void formClosing() {
			super.formClosing();
			if (pic != null) {
				pic.freeImage();
				pic.free();
			}
		}	
	} // ImageInteractivePanel

}

