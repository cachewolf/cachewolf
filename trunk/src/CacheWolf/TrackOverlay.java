package CacheWolf;

//import java.awt.image.BufferedImage;

import ewe.fx.Color;
import ewe.fx.Graphics;
import ewe.fx.Image;
import ewe.fx.Pen;
import ewe.graphics.AniImage;
import ewe.sys.Convert;
import ewe.fx.Point;

import ewe.util.Vector;


public class TrackOverlay extends AniImage {
	TrackPoint topLeft;
	TrackPoint bottomRight;
	Graphics draw;
	int test;
	MapInfoObject trans; 
	Vector tracks;
	boolean imageChangesDontShow = false;
	public Point trackPixels[] = null;
	public Color trackPixelsColor[] = null;
	public int numPixels = 0;
	final static Color transparentColor = Color.White;
	public TrackOverlay (TrackPoint topLefti, int widthi, int highti, MapInfoObject transi) {
		topLeft = new TrackPoint(topLefti);
		trans = transi;
		bottomRight = calcLatLonInImage(widthi, highti);
		setImage(new Image(widthi, highti), transparentColor);
		//properties = AlwaysOnTop; // arrows are above, so dont set it.
		draw = new Graphics(image);
		draw.setDrawOp(Graphics.DRAW_OVER);
		draw.setColor(transparentColor);
		draw.fillRect(0, 0, widthi, highti);
	}

	
	public void paintTracks() {
		if (tracks == null || tracks.size() == 0) return;
		int tri, i;
		Track tr;
		for (tri=tracks.size()-1; tri >= 0; tri--) {
			tr = (Track)tracks.get(tri);
			draw.setPen(new Pen((Color) tr.trackColor,Pen.SOLID,3));
			if (tr.num > 0) {
				for (i=0; i < tr.num; i++) {
					paintPoint(tr.trackColor, tr.TrackPoints[i].latDec, tr.TrackPoints[i].lonDec);
				}
			}
		}
	}

		
	public void paintPoint(Color f, double lat, double lon){
		if (lat<bottomRight.latDec || lat > topLeft.latDec || lon<topLeft.lonDec || lon>bottomRight.lonDec) return;
		//ewe.sys.Vm.debug("showlastaddedpoint, lat: "+lat+"   lon: "+lon);
		double b[] = new double[2];
		int x, y;
		b[0] = lat - topLeft.latDec; // see calcXYinImage (TrackPoint p) 
		b[1] = lon - topLeft.lonDec; 
		x=(int) (trans.transLatX* b[0] + trans.transLonX*b[1]);
		y=(int) (trans.transLatY* b[0] + trans.transLonY*b[1]);
		//draw.drawLine(x, y, x, y);
		ewe.sys.Vm.debug("showlastaddedpoint, x: "+x+"   y: "+y+"loc.x: "+location.x+"  loc.y:"+location.y);
		draw.fillRect(x-1, y-1, 3, 3);
		/*	if (image.bufferedImage != null) { // funktioniert gut, allerdings nur in der java-VM wenn ewe.fx.Image.bufferedImage als public definiert
			int yd;
			for  (int xd=-1; xd<=1; xd++) {
				for (yd=-1; yd<=1; yd++) {
					((BufferedImage)image.bufferedImage).setRGB(x+xd, y+yd, -65536);
				}
			} */
		if (imageChangesDontShow) {
			try {addPixelIfNeccessary(x, y, f); }
			catch (IndexOutOfBoundsException e) // thrown when there are more than pixels stored than possible
			{   
				draw.drawImage(image,null,Color.DarkBlue,0,0,location.width,location.height); // changing the mask forces graphics to copy from image._awtImage to image.bufferedImage, which is displayed 
				draw.drawImage(image,null,transparentColor,0,0,location.width,location.height);
				removeAllPixels();
			}
		}
	}
	
	private void removeAllPixels() {
		numPixels = 0;
		trackPixels = null;
		trackPixelsColor = null;
	}

	public Point calcXYinImage (TrackPoint p) {
		double b[] = new double[2]; // see method paintPoint it should actually call this method but it doesn't because of speed raesons
		int x, y;
		b[0] = p.latDec - topLeft.latDec;
		b[1] = p.lonDec - topLeft.lonDec;
		x=(int) (trans.transLatX* b[0] + trans.transLonX*b[1]);
		y=(int) (trans.transLatY* b[0] + trans.transLonY*b[1]);
		return new Point(x,y);
	}

	public TrackPoint calcLatLonInImage (double x, double y) {
		// see trans.calcLatLon(p);
		TrackPoint ll = new TrackPoint(); 
		ll.latDec = (double)x * trans.affine[0] + (double)y * trans.affine[2] + topLeft.latDec;
		ll.lonDec = (double)x * trans.affine[1] + (double)y * trans.affine[3] + topLeft.lonDec;
		return ll;
	}

	public void addPixel(int x, int y, Color f) throws IndexOutOfBoundsException {
		if (trackPixels==null) { trackPixels = new Point[500]; trackPixelsColor = new Color[500]; } 
		trackPixels[numPixels] = new Point(x, y); // IndexOutOfBoundsException is handled in PaintPoint
		trackPixelsColor[numPixels] = f.getCopy();
		numPixels++;
	}

	public void addPixelIfNeccessary(int x, int y, Color f){
		if (trackPixels != null) {
			int ll =(numPixels<50 ? 0 : numPixels-50); // look in the last 50 added Pixels if the same Pixel is already in the list (for performance reasons dont look in the whole list)
			for (int i=numPixels-1; i>=ll; i--) {
				if (trackPixels[i].x == x && trackPixels[i].y == y && f.equals(trackPixelsColor[i])) 
					{ return; } 
			}
		}
		addPixel(x, y, f);
	}

	public void paintLastAddedPoint(Track tr) {
		draw.setPen(new Pen((Color) tr.trackColor,Pen.SOLID,3));
		paintPoint(tr.trackColor, tr.TrackPoints[tr.num-1].latDec, tr.TrackPoints[tr.num-1].lonDec);
	}

	public boolean isOnScreen() { // i assume that location.width = screen.width and the same for hight
		if ( (location.x + location.width > 0 && location.x < location.width) || 
				(location.y + location.height > 0 && location.y < location.height) ) return true;
		else return false;
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
 * und kopiert den entsprechenden Teil in das übergebene Surface
 * Das Problem ist, dass bufferedImage nicht mehr upgedatet wird, wenn
 * es einmal erzugt wurde. Es wird nur dann upgedatet, wenn das Image ein anderes wird
 * oder die Maske eine andere. Das Update erfolgt dann über doDraw, was doCheckMask aufruft,
 * das das eigentliche kopieren aus _awtImage vornimmt.
 * 
 *  Problem: das kopieren von _awtImage in BufferedImage ist sehr zeitaufwändig, weil
 *  es keine native Routine ist und jedes Pixel einzeln geprüft wird.
 *  Deswegen wäre die beste Lösung, wenn ich bufferedImage direkt updaten könnte.
 *  Aber bufferedImage ist privat, ich kann auch in abgeleiteten Klassen nicht drauf
 *  zugreifen. 
 *  
 *  Zur Not wäre auch denkbar, doDraw zu überschreiben, um bei jedem Aufruf alle 
 *  Trackpoints neu zu zeichnen.
 */
// was alles nicht funktioniert:
//drawmask.setDrawOp(Graphics.DRAW_OVER);
//	drawmask.drawRect(x-1, y-1, 2, 2, 1);
//this.setImage(image, mask);
// nächster Versuch: image.bufferedImage in ewe.fx.Image public definieren !!!
//image.rgb
//	draw._g.surfaceData.bufImg.raster.data[y*this.location.width + x] = -65536; := image._awtImage
//	((Image)image).eImage(colorOrMask)._awtImage.raster.data[0]=0;
//	image
//	((BufferedImage)(image).se.^.bufferedImage.raster.data[y*this.location.width + x]=-65536; //was dort steht wird tatsächlich angezeigt, allerdings kann ich es nicht direkt setzen :-(
//int[] markPixels = new int[4];
//for (int i = 0; i<markPixels.length; i++) { markPixels[i] = -65536; }
// image.transparent = null; hilft auhc nicht
//image.mask = null;
//image.bufferedImage = null;
//image.setPixels(markPixels, 0 , x-20, y, 2, 2, 0); // dadrin sollte bufferedImage = null gesetzt werden, wird es aber nicht :-(
//ewe.fx.mImage mark = new mImage();
//Image mark = new Image(2,2);
//new Graphics(mark).drawImage(image, null, transparentColor, x-40, y, 2, 2);
//mark.draw(draw, x-50, y, Graphics.DRAW_OVER); // options (Graphics.DRAW_OVER) are ignored anyway
// image.bufferedImage = null; // this solves the problem
//toCursor(null);
//this.draw(draw);
//image=(BufferedImage)this.toNativeImage(transparentColor);
//(java.awt.Image.b)
//image.bufferedImage=null;
//draw.flush();
//ewe.ui.PenEvent.refreshTip(draw.surface);
//draw.setPixelRGB(x, y, -65536);
//this.changed(); hilft auch nicht
// this.refresh(); // hilft nicht :-(
//	lastDrawn.x = lastDrawn.x -10; hilft auch nicht
// imageMayChange = true; // hilft auch nicht :-(

/*
 * this class is only needed to have a fast access to the list of pixels
 * which are added but aniimage.draw will not lead to a change on the screen
 * so that these pixels will be drawn seperately by doDraw
 * 
 */
