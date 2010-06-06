package CacheWolf;
import CacheWolf.utils.FileBugfix;
import ewe.graphics.*;
import ewe.sys.*;
import ewe.fx.*;
import ewe.ui.*;
import ewe.io.*;


/**
*	Class to display the cache and log images. It creates a thumbnail view and
*	allows the user to click on an image that will then be displayed in its original size
*	as long as the image fits the application size. If the application size is not sufficient
*	then the image will be scaled to the available screen size.
*   A right mouseclick on an image will open a dialogue to delete the file. 
*/
public class ImagePanel extends InteractivePanel{
	/** Picture to replace deleted pictures */
	private final String NO_IMAGE="no_picture.png";
	/** Minimum time (msec) to recognise a long pen down event (=right mouse key) */
	private final int LONG_PEN_DOWN_DURATION=500;

	Preferences pref;
	Profile profile;
	//private final int thumb_max_size = 300;
	//private final int thumb_min_size = 100;
	private final int padding = 20;
	private int thumb_size = 0;
	private int locX, locY, locCounter;
	/** Start and duration of pen-pressed event to simulate right mouse key */
	private long start, duration=0;
	
	/**
	* Constructor to create the image panel.<p>
	*/
	public ImagePanel(){ // Public constructor
	}
	
	static CacheHolderDetail oldCache=null;
	/**
	* Method to set the individual cache images.
	* Gets called immediatly before panel is displayed
	* @see MainTab#onEvent(Event ev)
	*/
	public void setImages(CacheHolderDetail cache){
		if (cache!=oldCache) { 
			pref = Global.getPref();
			profile=Global.getProfile();
			Vm.showWait(true);
			clearImages();
			thumb_size = ((pref.myAppWidth-2*padding) / 3);
			thumb_size = thumb_size - padding;
			double rowCounter1 = 0;
			if (cache.images.getDisplayImages(cache.getParent().getWayPoint()).size()>0) {
				rowCounter1 = cache.images.getDisplayImages(cache.getParent().getWayPoint()).size();
				rowCounter1 = java.lang.Math.ceil(rowCounter1/3);
			}
			double rowCounter2 = 0;
			if (cache.userImages.size()>0){
				rowCounter2 = cache.userImages.size();
				rowCounter2 = java.lang.Math.ceil(rowCounter2/3);
			}
			int rowCounter = (int) (rowCounter1 + rowCounter2);
			Rect r = new Rect(0, 0, pref.myAppWidth, rowCounter*thumb_size+rowCounter*padding+padding);
			this.virtualSize = r;
			//this.setPreferredSize(pref.myAppWidth, rowCounter*thumb_size+rowCounter*padding+40);
			// this.checkScrolls();
			// this.refresh();
			locY=0;
			addTitle(MyLocale.getMsg(340,"Cache Images:"));
			locY = 20;
			locX = padding;
			addImages(cache.images.getDisplayImages(cache.getParent().getWayPoint()));
			// load user images
			if(locCounter==1 || locCounter ==2) locY = locY + thumb_size;
			//Vm.debug("thumb_size: " + Convert.toString(thumb_size));
			//Vm.debug("locy after: " + Convert.toString(locY));
			if (cache.userImages.size()> 0){
				addTitle(MyLocale.getMsg(341,"User Images:"));
				locY = locY + 20;
				locX = padding;
				locCounter = 0;
				addImages(cache.userImages);
			}
			oldCache=cache;
		} // cache!=oldCache	
		if (locY>this.height) {
			this.checkScrolls();
		}
		this.refresh();
		Vm.showWait(false);
		//this.repaintNow();
	}

	/**
	 * Clear the images in the panel
	 *
	 */
	public void clearImages() {
		oldCache=null;
		int lgr = images.size();
		for(int i = 0; i<lgr;i++){
			this.removeImage((AniImage)images.get(0));
		}
	}
	
	/**
	 * Add a title above the cache images and above the user images
	 * @param title Title to add ("cache images" or "user images")
	 */
	private void addTitle(String title) {
		AniImage aImg;
		Font titleFont = new Font("Verdana", Font.BOLD, 20);
		FontMetrics fm = getFontMetrics();
		int stringWidth = fm.getTextWidth(title);
		int stringHeight = fm.getHeight();
		Image img = new Image(stringWidth*2,stringHeight+5);
		Graphics g = new Graphics(img);
		g.setColor(new Color(195,195,195));
		g.fillRect(0,0,stringWidth*2,stringHeight+5);
		g.setColor(new Color(0,0,0));
		g.setFont(titleFont);
		g.drawText(title, 0,0);
		g.free();
		aImg = new AniImage(img);
		aImg.setLocation(0, locY);
		addImage(aImg);
		aImg.refresh();
	}
	
	/**
	 * Add the images to the panel. Can add both normal and user images
	 * @param pImages Vector of images or userImages
	 * @param imagesText Vector of image texts or user image texts
	 */
	private void addImages(CacheImages pImages) {
		String location, imgText;
		mImage mI;
		int scaleX, scaleY;
		double dummyC;
		ImagePanelImage ipi;
		AniImage AimgText;
		locCounter=0;
		for(int i = 0; i<pImages.size(); i++){
			location = profile.dataDir + pImages.get(i).getFilename();
			if (!(new FileBugfix(location)).exists()) {
				location=NO_IMAGE;
				if (!pref.showDeletedImages) continue; // Don't show the deleted Image if user does not want it
			}
			try{
				mI = new mImage(location);
				// actuall new mImage(location); should do the following "if" but it doesn't anyhow
				if (mI.getWidth() <= 0 || mI.getHeight() <= 0 ) throw new IllegalArgumentException(location);
				scaleX = thumb_size;
				scaleY = thumb_size;
				dummyC = 0;
				double thumb_size2 = thumb_size;
				if(mI.getWidth()>mI.getHeight()){
					scaleX = thumb_size;
					dummyC = (double)mI.getHeight()/ (double)mI.getWidth();
					dummyC = dummyC * thumb_size2;
					scaleY = (int)dummyC;
				}
				if(mI.getWidth() <= mI.getHeight()){
					scaleY = thumb_size;
					dummyC = (double)mI.getWidth()/(double)mI.getHeight();
					dummyC = dummyC * thumb_size2;
					scaleX = (int)dummyC;
				}
				if(mI.getWidth() <= thumb_size){
					scaleX = mI.getWidth();
					scaleY = mI.getHeight();
				}
				mI = mI.scale(scaleX,scaleY,null,0);
				mI.freeSource();
				ipi = new ImagePanelImage(mI);
				ipi.freeSource();
				//mI.free(); --> this only works in java-VM, in ewe it will delete the image, so leave it commented out
				ipi.fileName = location; // this is set only to easily identify the filename of the image clicked
				ipi.setLocation(locX, locY);
				addImage(ipi);
				//Name of picture:
				if(pImages.size()>i){
					if (location.equals(NO_IMAGE))
						imgText=MyLocale.getMsg(342,"Deleted");
					else
						imgText = SafeXML.cleanback(pImages.get(i).getTitle());
					if(imgText.length()==0) imgText = "???";
					AimgText = new AniImage();
					AimgText = getImageText(imgText);
					AimgText.setLocation(locX,locY+scaleY);
					addImage(AimgText);
					AimgText.refresh();
					ipi.imageText = imgText; 
				}
				ipi.refresh();
				locX = locX + thumb_size + padding;
				
				locCounter++;
				if(locCounter > 2) {
					locCounter = 0;
					locX = padding;
					locY = locY+thumb_size+padding;
				}
			}catch(IllegalArgumentException imex){ // file not found, could not decode etc.
				MessageBox tmp = new MessageBox(MyLocale.getMsg(321,"Fehler"), MyLocale.getMsg(322,"Kann Bild/Karte nicht laden")+":\n"+imex.getMessage(), FormBase.OKB); // @todo: language support
				tmp.exec();
			} catch (OutOfMemoryError e) { // TODO show an error icon in the panel instead of nothing
				(new MessageBox(MyLocale.getMsg(321,"Error"),MyLocale.getMsg(343,"Not enough free memory to load cache image")+":\n"+location,FormBase.OKB)).exec();
			} catch (SystemResourceException e) { // TODO show an error icon in the panel instead of nothing
				(new MessageBox(MyLocale.getMsg(321,"Error"),MyLocale.getMsg(343,"Not enough free memory to load cache image")+"\n"+location,FormBase.OKB)).exec();
			}
		} //for
		
	}
	
	private AniImage getImageText(String pText){
		Font aniImageFont = new Font("Verdana", Font.BOLD, 14);
		FontMetrics fm = getFontMetrics();
		int stringWidth = fm.getTextWidth(pText);
		int stringHeight = fm.getHeight();
		Image img = new Image(stringWidth*2,stringHeight+5);
		Graphics g = new Graphics(img);
		g.setColor(new Color(195,195,195));
		g.fillRect(0,0,stringWidth*2,stringHeight+5);
		g.setColor(new Color(0,0,0));
		g.setFont(aniImageFont);
		g.drawText(pText, 0,0);
		g.free();
		AniImage a = new AniImage(img);
		return a;
	}

	
	/**
	* React to when a user clicks an image.
	* If left mouse key is clicked, will open a new window displaying the image scaled
	* to window size if the image is larger, otherwise the true size is displayed.
	* If right mouse key is clicked, a dialogue to delete the image wil be displayed
	*/
	public void imageClicked(AniImage which, Point pos){
//		Vm.debug("Clicked"+pos.x+","+pos.y+" "+(((Control.currentPenEvent.modifiers&PenEvent.RIGHT_BUTTON)==PenEvent.RIGHT_BUTTON)?"RIGHT":"LEFT") );
		if ((ControlBase.currentPenEvent.modifiers&PenEvent.RIGHT_BUTTON)==PenEvent.RIGHT_BUTTON || duration>LONG_PEN_DOWN_DURATION) {
			// Right button pressed - delete image to conserve space
			if (which instanceof ImagePanelImage && !((ImagePanelImage)which).fileName.equals(NO_IMAGE)) {
				MessageBox mBox = new MessageBox (MyLocale.getMsg(144,"Warning"),MyLocale.getMsg(344,"Delete image")+" \""+((ImagePanelImage)which).imageText+"\"?", FormBase.IDYES |FormBase.IDNO);
				if (mBox.execute() == FormBase.IDOK){
						//Vm.debug("Deleting "+((ImagePanelImage)which).fileName);
						try {
							File f=new File(((ImagePanelImage)which).fileName);
							f.delete();
							removeImage(which);
						} catch(Exception e) {
							Global.getPref().log("Ignored Exception", e, true);
						};
				}
			}
		} else { 
			String fn = new String();
			if(which instanceof ImagePanelImage){
				ImagePanelImage ich = (ImagePanelImage)which;
				fn = ich.fileName;
				try {
					ImageDetailForm iF = new ImageDetailForm(fn, pref);
					iF.execute(null, Gui.CENTER_FRAME);
				} catch (IllegalArgumentException e) {
					MessageBox tmp = new MessageBox(MyLocale.getMsg(321,"Fehler"), MyLocale.getMsg(322,"Kann Bild/Karte nicht finden"), FormBase.OKB); // @todo: language support
					tmp.exec();
				} catch (OutOfMemoryError e) {
					(new MessageBox(MyLocale.getMsg(321,"Error"),MyLocale.getMsg(343,"Not enough free memory to load cache image")+"\n"+fn,FormBase.OKB)).exec();
				}
			}
		}
	}

	/**
	 * Create a "pen held down" event on hardware that does not support a right mouse key (e.g. Windows Mobile)
	 * by measuring the time between pen down and pen up events. This is used in imageClicked to differentiate 
	 * between left and right mouse keys.
	 */
	public void onPenEvent(PenEvent ev) {
		if (ev.type==PenEvent.PEN_DOWN) {
			start = Vm.getTimeStampLong();
		}
		if (ev.type==PenEvent.PEN_UP) {
			duration=Vm.getTimeStampLong()-start;
		}
		super.onPenEvent(ev);
	}
}
