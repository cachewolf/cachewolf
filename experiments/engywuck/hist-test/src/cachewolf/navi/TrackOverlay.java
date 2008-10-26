package cachewolf.navi;

//import java.awt.image.BufferedImage;

import eve.fx.Color;
import eve.fx.Graphics;

import eve.fx.Image;
import eve.fx.Point;

import java.util.Vector;


public class TrackOverlay extends MapImage {
	public TrackPoint topLeft;
	public TrackPoint bottomRight;
	Graphics draw;
	//Graphics drawMask;
	//int test;
	MapInfoObject trans; 
	Point pixelShift;
	public Vector tracks;
	boolean imageChangesDontShow = false;
	public Point trackPixels[] = null;
	public Color trackPixelsColor[] = null;
	public int numPixels = 0;
	final static int maxPixelsInCache = 100;
	final static Color transparentColorForOverlay = Color.White; // only for use when transparent color is used
	static boolean useTransparentColor;
	public TrackOverlay (TrackPoint topLefti, int widthi, int highti, MapInfoObject transi) {
		super();
		topLeft = new TrackPoint(topLefti);
		trans = transi;
		pixelShift = trans.calcMapXY(topLeft);
		bottomRight = trans.calcLatLon(widthi + pixelShift.x, highti + pixelShift.y);
//TODO		if (eve.sys.Vm.getPlatform().equalsIgnoreCase("java")) {
//			useTransparentColor = true; 
//			setImage(new Image(widthi, highti), transparentColorForOverlay); // java-vm: transparency with a mask is very memory consuming, but transparency with a mask is much faster in eve-vm and doesn't consume more memory than a transparency color (eve 1.49)
//		} else 
		{
			useTransparentColor = false; // // momentanously this it not used, but this is only because eve treats areas as opaque which has a non white color in the image, so that the mask doesn't need to be changed
			//Image maski = new Image(widthi, highti);
			//drawMask = new Graphics(maski);
			//drawMask.setColor(Color.White);
			//drawMask.fillRect(0, 0, maski.getWidth(), maski.getHeight());
			setImage(new Image(widthi, highti), Color.White); // java-vm: transparency with a mask is very memory consuming, but transparency with a mask is much faster in eve-vm and doesn't consume more memory than a transparency color (eve 1.49)
			//maski.free(); //setimage produces an inverted copy of the mask
			//maski = null;
		}
		//properties = AlwaysOnTop; // arrows are above, so dont set it.
		draw = new Graphics((Image) image);
		draw.setDrawOp(Graphics.DRAW_OVER);
		if (useTransparentColor) draw.setColor(transparentColorForOverlay);
		else draw.setColor(Color.White);
		draw.fillRect(0, 0, widthi, highti);
		//int[] markImage = {0x00ff0000, 0x00ff0000, 0x00ff0000, 0x00ff0000};
		//int[] markMaskOpaque = {0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff};
		//mask.setPixels( markMaskOpaque, 0, 50, 50, 2, 2, 0);
		//draw.fillRectRGB(50, 50, 52, 52, 0x00ff0000); // fillRectRGB has a Bug - it never returns - use fillRect instead
		//image.setPixels(markImage, 0, 50, 50, 2, 2, 0); // out of an to me unkwon reason this doesn't work here, but it does in painttracks
	}

//TODO ImageSet is not used in project	
/*	public void imageSet()
//	==================================================================
	{
		IImage i = drawable;
		if (i == null) i = image;
		if (i != null){
			location.width = i.getWidth();
			location.height = i.getHeight();
		}
		if (image != null && image != sourceImage) image.freeze();
//		if (mask != null && mask != sourceMask) mask.freeze(); // dont freeze the mask, it could change. Anyway momentanously it doesnt change, because when the image contains non-white in the opaque areas, it will be opaque without changing the mask
		properties &= ~HasChanged;
	}
*/

	public void paintTracks() {
	// for debugging TrackOverlayPositions
	// draw.setPen(new Pen(Color.LightBlue,Pen.SOLID,1));
	// draw.fillRect(1, 1, image.getWidth()-1, image.getHeight()-1);
		//draw.setColor(255,0,0);
		//draw.setPen(new Pen(new Color(255,0,0),Pen.SOLID,3));
		//draw.fillRect(50, 50, 4, 4); // fillRectRGB has a Bug - it never returns - use fillRect instead

		if (tracks == null || tracks.size() == 0) return;
		int tri, i;
		Track tr;
		int numberOfTracks = tracks.size();
		int numberOfPoints = ((Track)tracks.get(numberOfTracks - 1)).num;
		if (numberOfTracks > 1){
			numberOfPoints += (numberOfTracks - 1) * ((Track)tracks.get(0)).num;
		}
		int n = 0;
		
		for (tri=0; tri < numberOfTracks; tri++) {
			tr = (Track)tracks.get(tri);
			//draw.setPen(new Pen((Color) tr.trackColor,Pen.SOLID,3));
			draw.setColor(tr.trackColor);
			if (tr.num > 0) {
				for (i=0; i < tr.num; i++) {
					n++;
					if  ((numberOfPoints - n > 30*60) && ((n & 1) == 0)) continue;
					if  ((numberOfPoints - n > 60*60) && ((n & 2) == 0)) continue;
					paintPoint(tr.trackColor, tr.trackPoints[i]);
				}
			}
		}
	}
	

	/**
	 * 
	 * @param f
	 * @param lat
	 * @param lon
	 * @return true if point was on this overlay
	 */
	public boolean paintPoint(Color f, TrackPoint where){
		if (where.latDec < bottomRight.latDec || where.latDec > topLeft.latDec || where.lonDec < topLeft.lonDec || where.lonDec > bottomRight.lonDec) return false;
		//eve.sys.Vm.debug("showlastaddedpoint, lat: "+lat+"   lon: "+lon);
		Point p = trans.calcMapXY(where);
		int x = p.x - pixelShift.x;
		int y = p.y - pixelShift.y;
		//draw.drawLine(x, y, x, y);
		//eve.sys.Vm.debug("showlastaddedpoint, x: "+x+"   y: "+y+"loc.x: "+location.x+"  loc.y:"+location.y);
		draw.fillRect(x-1, y-1, 3, 3);
		//drawMask.fillRect(x-1, y-1, 3, 3);
		/*	if (image.bufferedImage != null) { // funktioniert gut, allerdings nur in der java-VM wenn eve.fx.Image.bufferedImage als public definiert
			int yd;
			for  (int xd=-1; xd<=1; xd++) {
				for (yd=-1; yd<=1; yd++) {
					((BufferedImage)image.bufferedImage).setRGB(x+xd, y+yd, -65536);
				}
			} */
		if (imageChangesDontShow) {
			try {addPixelIfNeccessary(x, y, f); }
			catch (IndexOutOfBoundsException e) // thrown when there are more than pixels stored than possible
			{ fixate();  }
		}
		return true;
	}

	/**
	 * this method forces eve to transfer the drawn points
	 * from _awtImage to bufferedImage, which is drawn to the screen
	 *
	 */
	private void fixate() {
		if (numPixels == 0) return;
		//	draw.drawImage(image,null,Color.DarkBlue,0,0,location.width,location.height); // changing the mask forces graphics to copy from image._awtImage to image.bufferedImage, which is displayed 
		doDraw(draw,0);   // null, Color.Pink, 0, 0, 1, 1); // width and height is anyway ignored, evtl. testen,  
		imageChangesDontShow = false;
		removeAllPixels();
	}

	private void removeAllPixels() {
		numPixels = 0;
		trackPixels = null;
		trackPixelsColor = null;
	}

	
	public void addPixel(int x, int y, Color f) throws IndexOutOfBoundsException {
		if (trackPixels==null) { trackPixels = new Point[maxPixelsInCache]; trackPixelsColor = new Color[maxPixelsInCache]; } 
		trackPixels[numPixels] = new Point(x, y); // IndexOutOfBoundsException is handled in PaintPoint
		trackPixelsColor[numPixels] = f.getCopy();
		numPixels++;
	}

	public void addPixelIfNeccessary(int x, int y, Color f){
		if (trackPixels != null) {
			int ll =(numPixels<30 ? 0 : numPixels-30); // look in the last 50 added Pixels if the same Pixel is already in the list (for performance reasons dont look in the whole list)
			for (int i=numPixels-1; i>=ll; i--) {
				if (trackPixels[i].x == x && trackPixels[i].y == y && f.equals(trackPixelsColor[i])) 
				{ return; } 
			}
		}
		addPixel(x, y, f);
	}

	public static final int FIXATE_IF_NO_PIXELS_NUM = 60;
	private int notOnThisOverlaySince = 0;

	public void paintLastAddedPoint(Track tr) { 
		//draw.setPen(new Pen((Color) tr.trackColor,Pen.SOLID,3));
		draw.setColor(tr.trackColor);
		if (paintPoint(tr.trackColor, tr.trackPoints[tr.num-1])) notOnThisOverlaySince = 0;
		else notOnThisOverlaySince++;
		if (notOnThisOverlaySince > FIXATE_IF_NO_PIXELS_NUM) { // zur Performanceverbesserung: wenn in den letzten 60 Updates keines mehr f�r dieses Overlay dabei war, Overlay Pixels fest schreiben, damit doDraw entlastet wird.
			fixate();
			notOnThisOverlaySince = 0;
		}

	}

	public void doDraw(Graphics g,int options) { // this is automatically called when the image need to be (re-)drawn on the screen
		super.doDraw(g, options);
		imageChangesDontShow = true; // g.drawImage (in super) copies _awtImage into bufferedImage, any later changes to _awtImage dont show up until the mask or the image has changed - unfortunately bufferedImage is not accessable from outside
		// draw trackpoints which were added after image changes don't show up on the screen
		if (tracks == null || tracks.size() == 0) return;
		int i;
		for (i=0; i<numPixels; i++) {
			g.setColor(trackPixelsColor[i]);
			g.fillRect(trackPixels[i].x-1, trackPixels[i].y-1, 3, 3);
		}
		//g.drawText(Convert.toString(test), 10, 10);
		//g.drawRect(10 + test, 10, 10, 10);
		//test++;
	}
}


/* draw zeichnet auf _awtImage
 * image.drawImage erzeugt bufferedImage, wenn es vorher null war 
 * und kopiert den entsprechenden Teil in das �bergebene Surface
 * Das Problem ist, dass bufferedImage nicht mehr upgedatet wird, wenn
 * es einmal erzugt wurde. Es wird nur dann upgedatet, wenn das Image ein anderes wird
 * oder die Maske eine andere. Das Update erfolgt dann �ber doDraw, was doCheckMask aufruft,
 * das das eigentliche kopieren aus _awtImage vornimmt.
 * 
 *  Problem: das kopieren von _awtImage in BufferedImage ist sehr zeitaufw�ndig, weil
 *  es keine native Routine ist und jedes Pixel einzeln gepr�ft wird.
 *  Deswegen w�re die beste L�sung, wenn ich bufferedImage direkt updaten k�nnte.
 *  Aber bufferedImage ist privat, ich kann auch in abgeleiteten Klassen nicht drauf
 *  zugreifen. 
 *  
 *  Zur Not w�re auch denkbar, doDraw zu �berschreiben, um bei jedem Aufruf alle 
 *  Trackpoints neu zu zeichnen.
 *  Work-Aorund: draw.drawImage(image,null,Color.Pink,0,0,1,1); bewirkt, dass awtImage ins
 *  bufferedImage kopiert wird. Dabei wird die transparentColor (in Picture) nicht ge�ndert
 *  und beim Aufruf von doDraw wird wieder die urspr�ngliche transparentColor verwendet
 *  
 */
//was alles nicht funktioniert:
//drawmask.setDrawOp(Graphics.DRAW_OVER);
//drawmask.drawRect(x-1, y-1, 2, 2, 1);
//this.setImage(image, mask);
//n�chster Versuch: image.bufferedImage in eve.fx.Image public definieren !!!
//image.rgb
//draw._g.surfaceData.bufImg.raster.data[y*this.location.width + x] = -65536; := image._awtImage
//((Image)image).eImage(colorOrMask)._awtImage.raster.data[0]=0;
//image
//((BufferedImage)(image).se.^.bufferedImage.raster.data[y*this.location.width + x]=-65536; //was dort steht wird tats�chlich angezeigt, allerdings kann ich es nicht direkt setzen :-(
//int[] markPixels = new int[4];
//for (int i = 0; i<markPixels.length; i++) { markPixels[i] = -65536; }
//image.transparent = null; hilft auhc nicht
//image.mask = null;
//image.bufferedImage = null;
//image.setPixels(markPixels, 0 , x-20, y, 2, 2, 0); // dadrin sollte bufferedImage = null gesetzt werden, wird es aber nicht :-(
//eve.fx.Picture mark = new Picture();
//Image mark = new Image(2,2);
//new Graphics(mark).drawImage(image, null, transparentColor, x-40, y, 2, 2);
//mark.draw(draw, x-50, y, Graphics.DRAW_OVER); // options (Graphics.DRAW_OVER) are ignored anyway
//image.bufferedImage = null; // this solves the problem
//toCursor(null);
//this.draw(draw);
//image=(BufferedImage)this.toNativeImage(transparentColor);
//(java.awt.Image.b)
//image.bufferedImage=null;
//draw.flush();
//eve.ui.PenEvent.refreshTip(draw.surface);
//draw.setPixelRGB(x, y, -65536);
//this.changed(); hilft auch nicht
//this.refresh(); // hilft nicht :-(
//lastDrawn.x = lastDrawn.x -10; hilft auch nicht
//imageMayChange = true; // hilft auch nicht :-(

/*
 * In der eve-VM f�r PocketPC-ARM funktioniert die Festlegung einer 
 * transparenten Farbe nicht (Hintergrund wird wei� statt durchsichtig)
 * deswegen (und weil in eve-VM effizienter) Umstellung auf Transparenzmaske
 * statt transparenter Farbe
 * TODO Dies ist in Java-VM allerdings extrem Speicher fressend -> evtl abfragen 
static int fixMask(WObject image,WObject col,int isMask):
	in Maske: 0 an durchsichtiger Stelle, sonst ff
	in Image: ffffff an durchsichtiger Stelle

	in java-VM
	in Maske: ffffffff in image.mask, wenn nicht durchsichtig
	          ff000000 an durchsichtiger Stelle
	image.doCheckMask erzeugt ein Image mit 0 an den durchsichtigen Stellen, die dadurch definiert sind, dass im image 0xffffff und in (mask & 0xffffff == 0) steht.
 */
/*
 * this class is only needed to have a fast access to the list of pixels
 * which are added but aniimage.draw will not lead to a change on the screen
 * so that these pixels will be drawn seperately by doDraw
 * 
 */
