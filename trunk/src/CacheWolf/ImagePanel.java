package CacheWolf;
import ewe.graphics.*;
import ewe.sys.*;
import ewe.fx.*;
import ewe.ui.*;

/**
*	Class to display the cache and log images. It creates a thumbnail view and
*	allows the user to click on an image that will then be displayed in its original size
*	as long as the image fits the application size. If the application size is not sufficient
*	then the image will be scaled to the available screen size.
*/
public class ImagePanel extends InteractivePanel{
	Preferences pref = new Preferences();
	int thumb_max_size = 300;
	int thumb_min_size = 100;
	int padding = 20;
	int thumb_size = 0;
	/**
	* Constructor to create the image panel.<p>
	* Also calculates the possible sizes for the
	* thumbnail view.
	*/
	public ImagePanel(Preferences p){
		pref = p;
	}
	
	/**
	* Method to set the individual cache images.
	* Gets called immediatly before panel is displayed
	* @see MainTab#onEvent(Event ev)
	*/
	public void setImages(CacheHolder cache){
		String imgText = new String();
		AniImage AimgText;
		double dummyC = 0;
		Vm.showWait(true);
		ImageList liste = images;
		thumb_size = (int)((pref.myAppWidth-2*padding) / 3);
		thumb_size = thumb_size - padding;
		int lgr = liste.size();
		for(int i = 0; i<lgr;i++){
			this.removeImage((AniImage)liste.get(0));
		}
		int rowCounter = cache.Images.size() + cache.UserImages.size();
		rowCounter = (int)(rowCounter/3)+1;
		Rect r = new Rect(new Dimension(pref.myAppWidth,rowCounter*thumb_size+rowCounter*padding+padding));
		this.virtualSize = r;
		//this.setPreferredSize(pref.myAppWidth, rowCounter*thumb_size+rowCounter*padding+40);
		this.checkScrolls();
		this.refresh();
		AniImage aImg;
		String location = new String();
		Font font = new Font("Verdana", Font.BOLD, 20);
		FontMetrics fm = getFontMetrics();
		int stringWidth = fm.getTextWidth("Cache Images");
		int stringHeight = fm.getHeight();
		Image img = new Image(stringWidth*2,stringHeight+5);
		Graphics g = new Graphics(img);
		g.setColor(new Color(195,195,195));
		g.fillRect(0,0,stringWidth*2,stringHeight+5);
		g.setColor(new Color(0,0,0));
		g.setFont(font);
		g.drawText("Cache Images:", 0,0);
		g.free();
		aImg = new AniImage(img);
		addImage(aImg);
		aImg.refresh();
		mImage mI;
		int locX, locY;
		int scaleX, scaleY = 0;
		locY = 20;
		locX = padding;
		int locCounter = 0;
		ImagePanelImage ipi;
		for(int i = 0; i<cache.Images.size(); i++){
			location = pref.mydatadir + (String)cache.Images.get(i);
			try{
				mI = new mImage(location);
				scaleX = thumb_size;
				scaleY = thumb_size;
				dummyC = 0;
				if(mI.getWidth()>mI.getHeight()){
					scaleX = thumb_size;
					dummyC = (double)mI.getHeight()/ (double)mI.getWidth();
					dummyC = dummyC * (double)thumb_size;
					scaleY = (int)dummyC;
				}
				if(mI.getWidth()<mI.getHeight()){
					scaleY = thumb_size;
					dummyC = (double)mI.getWidth()/(double)mI.getHeight();
					dummyC = dummyC * (double)thumb_size;
					scaleX = (int)dummyC;
				}
				if(mI.getWidth() <= thumb_size){
					scaleX = mI.getWidth();
					scaleY = mI.getHeight();
				}
				mI = mI.scale(scaleX,scaleY,null,0);
				ipi = new ImagePanelImage(mI);
				ipi.fileName = location;

				ipi.setLocation(locX, locY);
				addImage(ipi);
				//Name of picture:
				if(cache.ImagesText.size()>i){
					imgText = (String)cache.ImagesText.get(i);
					if(imgText.length()==0) imgText = "???";
					AimgText = new AniImage();
					AimgText = getImageText(imgText);
					AimgText.setLocation(locX,locY+scaleY);
					addImage(AimgText);
					AimgText.refresh();
				}
				ipi.refresh();
				locX = locX + thumb_size + padding;
				
				locCounter++;
				if(locCounter > 2) {
					locCounter = 0;
					locX = padding;
					locY = locY+thumb_size+padding;
				}
			}catch(Exception imex){Vm.debug("Error: " + imex.toString());}
		} //for
		//Vm.debug("LocCounter: " +Convert.toString(locCounter));
		//Vm.debug("locy before: " + Convert.toString(locY));
		
		
		if(locCounter==1 || locCounter ==2) locY = locY + thumb_size;
		//Vm.debug("thumb_size: " + Convert.toString(thumb_size));
		//Vm.debug("locy after: " + Convert.toString(locY));
		stringWidth = fm.getTextWidth("User Images");
		Image img2 = new Image(stringWidth*2,stringHeight+5);
		Graphics g2 = new Graphics(img2);
		g2.setColor(new Color(195,195,195));
		g2.fillRect(0,0,stringWidth*2,stringHeight+5);
		g2.setColor(new Color(0,0,0));
		g2.setFont(font);
		g2.drawText("User Images:", 0,0);
		g2.free();
		aImg = new AniImage(img2);
		aImg.setLocation(0, locY);
		addImage(aImg);
		aImg.refresh();
		locY = locY + 20;
		locX = padding;
		locCounter = 0;
		for(int i = 0; i<cache.UserImages.size(); i++){
			location = pref.mydatadir + (String)cache.UserImages.get(i);
			//Vm.debug(location);
			try{
				mI = new mImage(location);
				scaleX = thumb_size;
				scaleY = thumb_size;
				dummyC = 0;
				if(mI.getWidth()>mI.getHeight()){
					scaleX = thumb_size;
					dummyC = (double)mI.getHeight()/ (double)mI.getWidth();
					dummyC = dummyC * (double)thumb_size;
					scaleY = (int)dummyC;
				}
				if(mI.getWidth()<mI.getHeight()){
					scaleY = thumb_size;
					dummyC = (double)mI.getWidth()/(double)mI.getHeight();
					dummyC = dummyC * (double)thumb_size;
					scaleX = (int)dummyC;
				}
				if(mI.getWidth() <= thumb_size){
					scaleX = mI.getWidth();
					scaleY = mI.getHeight();
				}
				mI = mI.scale(scaleX,scaleY,null,0);
				ipi = new ImagePanelImage(mI);
				ipi.fileName = location;
				ipi.setLocation(locX, locY);
				addImage(ipi);
				//Name of picture:
				if(cache.UserImagesText.size()>i){
					imgText = (String)cache.UserImagesText.get(i);
					if(imgText.length()==0) imgText = "???";
					AimgText = new AniImage();
					AimgText = getImageText(imgText);
					AimgText.setLocation(locX,locY+scaleY);
					addImage(AimgText);
					AimgText.refresh();
				}
				ipi.refresh();
				locX = locX + thumb_size + padding;
				locCounter++;
				if(locCounter > 2) {
					locCounter = 0;
					locX = padding;
					locY = locY+thumb_size+padding;
				}
			}catch(Exception imex){
				Vm.debug("Error: " + imex.toString());
			}
		} //for
		
		
		this.refresh();
		Vm.showWait(false);
		//this.repaintNow();
	}
	
	private AniImage getImageText(String text){
		Font font = new Font("Verdana", Font.BOLD, 14);
		FontMetrics fm = getFontMetrics();
		int stringWidth = fm.getTextWidth(text);
		int stringHeight = fm.getHeight();
		Image img = new Image(stringWidth*2,stringHeight+5);
		Graphics g = new Graphics(img);
		g.setColor(new Color(195,195,195));
		g.fillRect(0,0,stringWidth*2,stringHeight+5);
		g.setColor(new Color(0,0,0));
		g.setFont(font);
		g.drawText(text, 0,0);
		g.free();
		AniImage a = new AniImage(img);
		return a;
	}
	/**
	* React to when a user clicks an image.
	* Will open a new window displaying the image scaled
	* to window size if the image is larger, otherwise
	* the true size is displayed.
	*/
	public void imageClicked(AniImage which, Point pos){
		String fn = new String();
		if(which instanceof ImagePanelImage){
			ImagePanelImage ich = (ImagePanelImage)which;
			fn = ich.fileName;
			try {
				ImageDetailForm iF = new ImageDetailForm(fn, pref);
				iF.execute(null, Gui.CENTER_FRAME);
			} catch (IllegalArgumentException e) {
				Locale l = Vm.getLocale();
				LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
				MessageBox tmp = new MessageBox((String)lr.get(321,"Fehler"), (String)lr.get(322,"Kann Bild/Karte nicht finden"), MessageBox.OKB); // @todo: language support
				tmp.exec();
			}
		}
	}
}
