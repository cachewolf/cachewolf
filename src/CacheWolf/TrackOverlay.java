package CacheWolf;

import ewe.fx.Color;
import ewe.fx.Graphics;
import ewe.fx.Image;
import ewe.fx.Pen;
import ewe.graphics.AniImage;
import ewe.ui.MessageBox;
import ewe.fx.Point;

import ewe.util.Utils;
import ewe.util.Vector;
import ewe.io.BufferedWriter;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.PrintWriter;

public class TrackOverlay extends AniImage {
	TrackPoint topLeft;
	TrackPoint bottomRight;
	Graphics draw;
	MapInfoObject trans; 
	Vector tracks;
	final static Color transparentColor = Color.White;
	public TrackOverlay (TrackPoint topLefti, int widthi, int highti, MapInfoObject transi) {
		topLeft = new TrackPoint(topLefti);
		trans = transi;
		bottomRight = calcLatLonInImage(widthi, highti);
		setImage(new Image(widthi, highti), transparentColor);
		//properties = AlwaysOnTop;
		draw = new Graphics(image);
		draw.setColor(transparentColor);
		draw.fillRect(0, 0, widthi, highti);
	}
	public void setBottimRightLatLon(double lat, double lon) {
		bottomRight = new TrackPoint(lat, lon);
	}
	public void addTrack(Track tr) {
		if (tr == null) return;
		if (tracks == null) tracks = new Vector();
		tracks.add(tr);
	}
	public void addTracks(Track[] trs) {
		if (trs==null || trs.length == 0) return;
		for (int i=0; i<trs.length; i++) {
			addTrack(trs[i]);
		}
		
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
					paintPoint(tr.TrackPoints[i].latDec, tr.TrackPoints[i].lonDec);
				}
			}
		}
	}

	public void paintPoint(double lat, double lon){
		if (lat<bottomRight.latDec || lat > topLeft.latDec || lon<topLeft.lonDec || lon>bottomRight.lonDec) return;
		ewe.sys.Vm.debug("showlastaddedpoint, lat: "+lat+"   lon: "+lon);
		double b[] = new double[2];
		int x, y;
		b[0] = lat - topLeft.latDec; // see calcXYinImage (TrackPoint p) 
		b[1] = lon - topLeft.lonDec; 
		x=(int) (trans.transLatX* b[0] + trans.transLonX*b[1]);
		y=(int) (trans.transLatY* b[0] + trans.transLonY*b[1]);
		//draw.drawLine(x, y, x, y);
		ewe.sys.Vm.debug("showlastaddedpoint, x: "+x+"   y: "+y+"loc.x: "+location.x+"  loc.y:"+location.y);
		draw.drawRect(x-1, y-1, 2, 2, 1);
	//	draw._g.surfaceData.bufImg.raster.data[y*this.location.width + x] = -65536;
		//image._awtImage.raster.data
		//image.bufferedImage.raster.data[y*this.location.width + x]=-65536; //was dort steht wird tatsächlich angezeigt, allerdings kann ich es nicht direkt setzen :-(
		int[] markPixels = new int[4];
		for (int i = 0; i<markPixels.length; i++) { markPixels[i] = -65536; }
		image.setPixels(markPixels, 0 , 400, 300, 1, 1, 0);
		//draw.flush();
		//ewe.ui.PenEvent.refreshTip(draw.surface);
		//draw.setPixelRGB(x, y, -65536);
		this.changed();
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
	
	public void addTrackPoint(Track tr, double lat, double lon) {
	tr.add(lat, lon);
	paintPoint(lat, lon);
	}
	
	public void paintLastAddedPoint(Track tr) {
		draw.setPen(new Pen((Color) tr.trackColor,Pen.SOLID,3));
		paintPoint(tr.TrackPoints[tr.num-1].latDec, tr.TrackPoints[tr.num-1].lonDec);
	}
	public boolean isOnScreen() { // i assume that location.width = screen.width and the same for hight
		if ( (location.x + location.width > 0 && location.x < location.width) || 
				(location.y + location.height > 0 && location.y < location.height) ) return true;
		else return false;
	}
}
