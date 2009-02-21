package cachewolf.navi;

//import java.awt.image.BufferedImage;

import eve.fx.Color;
import eve.fx.Graphics;
import eve.fx.ImageObject;
import eve.fx.PixelBuffer;

import eve.fx.Image;
import eve.fx.Point;
import eve.ui.advanced.database.GetSearchCriteria;

import java.util.Vector;


public class TrackOverlay extends MapImage {
	public TrackPoint topLeft;
	public TrackPoint bottomRight;
	ImageObject drawImage;
	MapInfoObject trans; 
	Point pixelShift;
	public Vector tracks;
	public TrackOverlay (TrackPoint topLefti, int widthi, int highti, MapInfoObject transi) {
		super();
		topLeft = new TrackPoint(topLefti);
		trans = transi;
		pixelShift = trans.calcMapXY(topLeft);
		bottomRight = trans.calcLatLon(widthi + pixelShift.x, highti + pixelShift.y);
		drawImage = new PixelBuffer(widthi, highti);
		setImage(drawImage); //, Color.White); // java-vm: transparency with a mask is very memory consuming, but transparency with a mask is much faster in eve-vm and doesn't consume more memory than a transparency color (eve 1.49)
		changed();
	}


	public void paintTracks() {
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
		//eve.sys.Vm.debug("showlastaddedpoint, x: "+x+"   y: "+y+"loc.x: "+location.x+"  loc.y:"+location.y);
		//		draw.fillRect(x-1, y-1, 3, 3);
		int[] bl = new int[3*3]; // TODO dies statisch machen
		for (int i = 0 ; bl.length > i; i++)  bl[i] = f.toInt();
		drawImage.setPixels(bl, 0, x-1, y-1, bl.length/3, 3, 0);
		return true;
	}

	public void addPixel(int x, int y, Color f) throws IndexOutOfBoundsException {
	}

	public void addPixelIfNeccessary(int x, int y, Color f){
		addPixel(x, y, f);
	}

	public static final int FIXATE_IF_NO_PIXELS_NUM = 60;

	public void paintLastAddedPoint(Track tr) { 
		paintPoint(tr.trackColor, tr.trackPoints[tr.num-1]);
	}

	public void doDraw(Graphics g,int options) { // this is automatically called when the image need to be (re-)drawn on the screen
		super.doDraw(g, options);
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
 *  Work-Aorund: draw.drawImage(image,null,Color.Pink,0,0,1,1); bewirkt, dass awtImage ins
 *  bufferedImage kopiert wird. Dabei wird die transparentColor (in Picture) nicht geändert
 *  und beim Aufruf von doDraw wird wieder die ursprüngliche transparentColor verwendet
 *  
 */
//was alles nicht funktioniert:
//drawmask.setDrawOp(Graphics.DRAW_OVER);
//drawmask.drawRect(x-1, y-1, 2, 2, 1);
//this.setImage(image, mask);
//nächster Versuch: image.bufferedImage in eve.fx.Image public definieren !!!
//image.rgb
//draw._g.surfaceData.bufImg.raster.data[y*this.location.width + x] = -65536; := image._awtImage
//((Image)image).eImage(colorOrMask)._awtImage.raster.data[0]=0;
//image
//((BufferedImage)(image).se.^.bufferedImage.raster.data[y*this.location.width + x]=-65536; //was dort steht wird tatsächlich angezeigt, allerdings kann ich es nicht direkt setzen :-(
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
 * In der eve-VM für PocketPC-ARM funktioniert die Festlegung einer 
 * transparenten Farbe nicht (Hintergrund wird weiß statt durchsichtig)
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
