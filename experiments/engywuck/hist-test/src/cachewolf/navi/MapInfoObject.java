package cachewolf.navi;

import eve.fx.Point;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import eve.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import eve.sys.*;
import java.lang.Math;

import cachewolf.CWPoint;
import cachewolf.MyLocale;
import cachewolf.utils.Common;
import cachewolf.utils.STRreplace;
import cachewolf.utils.Matrix;



/**
 * class to read, save and do the calculations for calibrated and
 * calibrating maps
 * start offset for language file: 4300
 * @author pfeffer
 *
 */
public class MapInfoObject extends Area {
	//World file:
	// x scale
	// y scale
	// x rotation
	// y rotation
	// lon of upper left corner of image
	// lat of upper left corner of image
	// lon of lower right corner of image
	// lat of lower right corner of image

	private double[] affine = {0,0,0,0};
	private CWPoint affineTopleft = new CWPoint();;
	private double transLatX, transLatY, transLonX, transLonY; // this are needed for the inervers calculation from lat/lon to x/y
	public CWPoint center = new CWPoint();
	public float sizeKm = 0; // diagonale
	public float scale; // in meters per pixel, note: it is assumed that this scale identifying the scale of the map, automatically adjusted when zooming
	public float zoomFactor = 1; // if the image is zoomed, direct after laoding always 1
	public Point shift = new Point (0,0);
	public CWPoint origAffineUpperLeft; // this is only valid after zooming
	public float rotationRad; // contains the rotation of the map == north direction in rad
	/** full path to the respective worldfile, including ".wfl"*/
	public String fileNameWFL = "";
	/** filename wihout directory */
//	public String fileName = new String();
	/** name of the map, introduced to allow 'maps' without an image (empty maps) */
	public String mapName = "";
	//private Character digSep = new Character(' ');
	static private String digSep = MyLocale.getDigSeparator();
	private int coordTrans = 0;

	public MapInfoObject() {}

	public MapInfoObject(MapInfoObject map) {
		super (map.topleft, map.buttomright);
		mapName = map.mapName;
		affine[0] = map.affine[0];
		affine[1] = map.affine[1];
		affine[2] = map.affine[2];
		affine[3] = map.affine[3];
		origAffineUpperLeft = new CWPoint (map.origAffineUpperLeft);
		affineTopleft = new CWPoint(map.affineTopleft);
		zoomFactor = map.zoomFactor;
		shift.set(map.shift);
		coordTrans = map.coordTrans;
		//	fileName = new String(map.fileName);
		fileNameWFL = new String(map.fileNameWFL);
		mapName = new String(mapName);
		doCalculations();
	}

	/**
	 * constructes an MapInfoObject without an associated map
	 * but with 1 Pixel = scale meters
	 */
	public MapInfoObject(double scalei, double lat) {
		super(new CWPoint(1,0), new CWPoint(0,1));
		mapName=MyLocale.getMsg(4300, "empty 1 Pixel = ") + scalei + MyLocale.getMsg(4301,"meters");
		double meters2deg = 1/(1000*(new CWPoint(0,0)).getDistance(new CWPoint(1,0)));
		double pixel2deg = meters2deg * scalei;
		affine[0]=0; //x2lat
		affine[1]=pixel2deg / java.lang.Math.cos(lat*java.lang.Math.PI / 180); //x2lon
		affine[2]=-pixel2deg; //y2lat
		affine[3]=0; //y2lon
		topleft.latDec=1; //top
		topleft.lonDec=0; //left
		buttomright.latDec = 0; //buttom
		buttomright.lonDec = 1; //right
		affineTopleft.set(topleft);
		doCalculations();
		origAffineUpperLeft = new CWPoint(affineTopleft);
	}

	/**
	 * constructs an MapInfoObject with an associated map
	 * with 1 Pixel = scale meters, centre and width, hight in pixels
	 * @param name path and filename of .wfl file without the extension (it is needed because the image will be searched in the same directory)
	 */
	public MapInfoObject(double scalei, CWPoint center, int width, int hight, String name) {
		super();
		mapName = name+".wfl";
		double meters2deg = 1/(1000*(new CWPoint(0,0)).getDistance(new CWPoint(1,0)));
		double pixel2deg = meters2deg * scalei;
		double pixel2deghorizontal = pixel2deg / java.lang.Math.cos(center.latDec*java.lang.Math.PI / 180);
		affine[0]=0; //x2lat
		affine[1]=pixel2deghorizontal; //x2lon
		affine[2]=-pixel2deg; //y2lat
		affine[3]=0; //y2lon
		topleft.latDec=center.latDec + hight / 2 *pixel2deg; //top
		topleft.lonDec=center.lonDec - width / 2 *pixel2deghorizontal; //left
		affineTopleft.set(topleft);
		buttomright.latDec = center.latDec - hight / 2 *pixel2deg; //buttom
		buttomright.lonDec = center.lonDec + width / 2 *pixel2deghorizontal; //right
		fileNameWFL = name;
		origAffineUpperLeft = new CWPoint(affineTopleft);
		doCalculations();
	}

	public MapInfoObject(String mapsPath, String thisMap) throws IOException, ArithmeticException {
		super();
		loadwfl(mapsPath, thisMap);
	}

	/**
	 *
	 * @param path including trailing "/"
	 * @param n without ".wfl"
	 * @return name of the map including fast-find-prefix
	 */
	public String setName(String path, String n) {
		String pref = getFfPrefix();
		mapName = pref + n;
		fileNameWFL = path + pref + mapName + ".wfl";
		return mapName;
	}

	/**
	 * @return the filename of the associated map image, "" if no file is associated, null if associated file could not be found
	 */
	public String getImageFilename() {
		// if (fileName == null || fileName.length() > 0) return fileName;
		if (fileNameWFL.length() == 0) return ""; // no image associated (empty map)
		String n = fileNameWFL.substring(0, fileNameWFL.lastIndexOf("."));
		return Common.getImageName(STRreplace.replace(n, "//", "/"));
	}

	/**
	 * Method to load a .wfl-file
	 * @param mapsPath path to the map inclunding / at the end
	 * @param thisMap name of the map without extension
	 * @throws IOException when there was a problem reading .wfl-file
	 * @throws IOException when lat/lon were out of range
	 * @throws ArithmeticException when affine data is not correct, e.g. it is not possible to inverse affine-transformation
	 */
	public void loadwfl(String mapsPath, String thisMap) throws IOException, ArithmeticException {
		FileInputStream instream = new FileInputStream (STRreplace.replace(mapsPath + thisMap + ".wfl", "//", "/"));
		BufferedReader in = new BufferedReader(new InputStreamReader(instream));

		String line = "";
		try {
			for(int i = 0; i<4;i++){
				line = in.readLine();
				affine[i] = Common.parseDoubleException(line);
			}
			line = in.readLine();
			affineTopleft.latDec = Common.parseDoubleException(line);
			line = in.readLine();
			affineTopleft.lonDec = Common.parseDoubleException(line);
			line = in.readLine();
			buttomright.latDec = Common.parseDoubleException(line);
			line = in.readLine();
			buttomright.lonDec = Common.parseDoubleException(line);
			line = in.readLine(); // readLine returns null, if End of File reached
			if (line != null)
				coordTrans = Common.parseInt(line);
			else
				coordTrans = 0;
			fileNameWFL = mapsPath + thisMap + ".wfl";
//			fileName = ""; //mapsPath + thisMap + ".png";
			mapName = thisMap;
			in.close();
			if( !buttomright.isValid() ) {
				affine[0] = 0; affine[1] = 0; affine[2] = 0; affine[3] = 0;
				topleft.makeInvalid();
				throw (new IOException(MyLocale.getMsg(4301, "Lat/Lon out of range while reading ")+mapsPath + thisMap + ".wfl"));
			}
		} catch (NullPointerException e) { // in.readline liefert null zur�ck, wenn keine Daten mehr vorhanden sind
			throw (new IOException(MyLocale.getMsg(4303, "not enough lines in file ")+mapsPath + thisMap + ".wfl"));
		}
		doCalculations();
		origAffineUpperLeft = new CWPoint(affineTopleft);
	}

	public void evalGCP(java.util.Vector GCPs, int imageWidth, int imageHeight) throws IllegalArgumentException {
		evalGCP(GCPs, imageWidth, imageHeight, 0);
	}
		/**
	 *	Method to evaluate ground control points (georeferenced points) and identify the parameters
	 *	for the affine transformation
	 *  @throws IllegalArgumentException when less than 3 georeferenced points were given in GCPs
	 */

	public void evalGCP(java.util.Vector GCPs, int imageWidth, int imageHeight, int epsg_code) throws IllegalArgumentException {
		//N 48 16.000 E 11 32.000
		//N 48 16.000 E 11 50.000
		//N 48 9.000 E 11 32.000
		if (GCPs.size() < 3 ) throw new IllegalArgumentException(MyLocale.getMsg(4304, "not enough points to calibrate the map"));
		GCPoint gcp;
		//Calculate parameters for latitutde affine transformation (affine 0,2,4)
		Matrix X = new Matrix(GCPs.size(),3);
		Matrix trg = new Matrix(GCPs.size(),1);
		for(int i = 0; i < GCPs.size();i++){
			gcp = (GCPoint)GCPs.get(i);
			X.matrix[i][0] = 1; X.matrix[i][1] = gcp.bitMapX; X.matrix[i][2] = gcp.bitMapY;
			trg.matrix[i][0] = gcp.latDec;
		}
		Matrix Xtran = new Matrix(X);
		Xtran.transpose();
		Matrix XtranX = new Matrix(Xtran);
		XtranX.multiply(X);
		Matrix XtranXinv = new Matrix(XtranX);
		XtranXinv.inverse();
		Matrix beta = new Matrix(XtranXinv);
		beta.multiply(Xtran);
		beta.multiply(trg);
		affine[0] = beta.matrix[1][0];
		affine[2] = beta.matrix[2][0];
		affineTopleft.latDec = beta.matrix[0][0];

		//Calculate parameters for longitude affine transformation (affine 1,3,5)
		X = new Matrix(GCPs.size(),3);
		trg = new Matrix(GCPs.size(),1);
		for(int i = 0; i < GCPs.size();i++){
			gcp = (GCPoint)GCPs.get(i);
			X.matrix[i][0] = 1;
			X.matrix[i][1] = gcp.bitMapX;
			X.matrix[i][2] = gcp.bitMapY;
			trg.matrix[i][0] = gcp.lonDec;
		}
		Xtran = new Matrix(X);
		Xtran.transpose();
		XtranX = new Matrix(Xtran);
		XtranX.multiply(X);
		XtranXinv = new Matrix(XtranX);
		XtranXinv.inverse();
		beta = new Matrix(XtranXinv);
		beta.multiply(Xtran);
		beta.multiply(trg);
		affine[1] = beta.matrix[1][0];
		affine[3] = beta.matrix[2][0];
		affineTopleft.lonDec = beta.matrix[0][0];
		coordTrans = epsg_code;
		buttomright = calcLatLon(imageWidth, imageHeight);
		doCalculations();
		//Vm.debug("A B C" + affine[0] + " " + affine[2] + " " + affine[4]);
		//Vm.debug("D E F" + affine[1] + " " + affine[3] + " " + affine[5]);
	}

	/**
	 * calculates centre, diagonal size of the map and inverse to affine transformation
	 * @throws ArithmeticException when affine data is not correct, e.g. it is not possible to inverse affine-transformation
	 */

	private void doCalculations() throws ArithmeticException {
		try {
			topleft.set(calcLatLon(0, 0));
			center.set((buttomright.latDec + topleft.latDec)/2,(buttomright.lonDec + topleft.lonDec)/2);
			sizeKm = java.lang.Math.abs((float)center.getDistance(buttomright.latDec, buttomright.lonDec)) *2;

			//calculate reverse affine
			double nenner=(-affine[1]*affine[2]+affine[0]*affine[3]);
			transLatX = affine[3]/nenner; // nenner == 0 cannot happen as long als affine is correct
			transLonX = -affine[2]/nenner;
			transLatY = -affine[1]/nenner;
			transLonY = affine[0]/nenner;

			// calculate north direction
			Point c = calcMapXY(center);
			int heightpixel = c.y * 2;
			c.y -= 1000;
			rotationRad = (float) (center.getBearing(calcLatLon(c)) / 180 * Math.PI);  // note: the direction of nord can vary across the image. In Gau�-Kr�ger Projection it does change about 1 degree per 10km! //(float)java.lang.Math.atan(rotationX2y);
			if (rotationRad > Math.PI) rotationRad -= 2* Math.PI;

			// calculate scale in meters per pixel
			double heightkm = calcLatLon(0, heightpixel).getDistance(topleft);
			scale = (float) (heightkm * 1000 / heightpixel);
		} catch (ArithmeticException ex) { throw new ArithmeticException(MyLocale.getMsg(4305, "Not allowed values in affine\n (matrix cannot be inverted)\n in file \n") + fileNameWFL); }
	}

	public void saveWFL() throws IOException, IllegalArgumentException {
		File dateiF = new File(fileNameWFL);
		String tmp = dateiF.getDrivePath(); // contains the name and the extension
		saveWFL(tmp, mapName);
	}

	/**
	 *	Method to save a world file (.wfl)
	 * @param mapsPath without "/" at the end
	 * @param mapFileName without file extension
	 * @throws IOException when there was a problem writing .wfl-file
	 * @throws IllegalArgumentException when affine[x] for all x == 0 ("map not calibrated").
	 */
	public void saveWFL(String mapsPath, String mapFileName) throws IOException, IllegalArgumentException {
		if (affine[0]==0 && affine[1]==0 && affine[2]==0 && affine[3]==0 &&
				!topleft.isValid()) throw (new IllegalArgumentException(MyLocale.getMsg(4306, "map not calibrated")));
		PrintWriter outp =  new PrintWriter(new BufferedWriter(new FileWriter(mapsPath + "/" + mapFileName + ".wfl")));
		StringBuffer towriteB=new StringBuffer(400);
		towriteB.append(Convert.toString(affine[0])).append("\n");
		towriteB.append(Convert.toString(affine[1])).append("\n");
		towriteB.append(Convert.toString(affine[2])).append("\n");
		towriteB.append(Convert.toString(affine[3])).append("\n");
		towriteB.append(Convert.toString(affineTopleft.latDec)).append("\n");
		towriteB.append(Convert.toString(affineTopleft.lonDec)).append("\n");
		towriteB.append(Convert.toString(buttomright.latDec)).append("\n");
		towriteB.append(Convert.toString(buttomright.lonDec)).append("\n");
		towriteB.append(((coordTrans == 0 || coordTrans == TransformCoordinates.EPSG_WGS84) ? "" : Convert.toString(coordTrans)+"\n"));
		String towrite = towriteB.toString();
		if (digSep.equals(",")) towrite=towrite.replace(',', '.');
		outp.print(towrite);
		outp.close();
//		this.fileName = ""; // this will be set in getImageFilenam //mapsPath + "/" + mapFileName + ".png";
		this.fileNameWFL = mapsPath + "/" + mapFileName + ".wfl";
		this.mapName = mapFileName;
	}


	/**
	 * zoom in / out
	 * @param zf zf > 1 == zoom in, zoom is relative to original unscaled image
	 * @param diffX shift of map in pixels (if the map was cropped while zooming) in the not zoomed image
	 * @param diffY
	 */
	public void zoom(float zf, int diffX, int diffY) {
		// restore original values to calculate corret shift (upperleft)
		affineTopleft.latDec = origAffineUpperLeft.latDec;
		affineTopleft.lonDec = origAffineUpperLeft.lonDec;
		affine[0] = affine[0] * zoomFactor;
		affine[1] = affine[1] * zoomFactor;
		affine[2] = affine[2] * zoomFactor;
		affine[3] = affine[3] * zoomFactor;
		TrackPoint upperleft = calcLatLon(diffX, diffY);
		if (coordTrans != 0) upperleft = TransformCoordinatesProperties.fromWgs84(upperleft, coordTrans);
		affineTopleft.latDec = upperleft.latDec; // TODO nachdenken affineTopleft
		affineTopleft.lonDec = upperleft.lonDec;
		affine[0] = affine[0] / zf ;
		affine[1] = affine[1] / zf ;
		affine[2] = affine[2] / zf ;
		affine[3] = affine[3] / zf ;
		zoomFactor = zf ;
		shift.x = diffX;
		shift.y = diffY;
		doCalculations(); // TODO lowlat neu berechnen?
	}

	/**
	 * Method to calculate bitmap x,y of the current map using
	 * lat and lon target coordinates. There ist no garanty that
	 * the returned coordinates are inside of the map. They can be negative.
	 * @param lat
	 * @param lon
	 */
	public Point calcMapXY(TrackPoint ll){
		TrackPoint t;
		if (coordTrans != 0) t = TransformCoordinatesProperties.fromWgs84(ll, coordTrans);
		else t = ll;
		Point coords = new Point();
		double b0,b1;
		b0 = t.latDec - affineTopleft.latDec;
		b1 = t.lonDec - affineTopleft.lonDec;
		double mapx = transLatX* b0 + transLonX*b1;
		double mapy = transLatY* b0 + transLonY*b1;
		coords.x = (int)Math.round(mapx);
		coords.y = (int)Math.round(mapy);
		//Vm.debug("mapX=mapx2: "+mapx+"="+mapx2+"; mapy=mapy2: "+mapy+"="+mapy2);
		return coords;
	}

	/**
	 * gives back lat/lon from x, y in map
	 * @param x
	 * @param y
	 * @return
	 */
	public CWPoint calcLatLon(int x, int y) {
		CWPoint ll = new CWPoint();
		ll.latDec = x * affine[0] + y * affine[2] + affineTopleft.latDec;
		ll.lonDec = x * affine[1] + y * affine[3] + affineTopleft.lonDec;
		if (coordTrans != 0)
			ll = TransformCoordinatesProperties.toWgs84(ll, coordTrans);
		return ll;
	}

	public CWPoint calcLatLon(Point p) {
		return calcLatLon(p.x, p.y);
	}

	/**
	 * Get the prefix used for easy and fast finding of the best map
	 * The filname of the .wfl and respective image should start with this
	 * prefix in order to make finding the best map much faster
	 * @return
	 */
	public String getFfPrefix() {
		return "FF1"+getEasyFindString()+"E-";
	}
}

	/**
	 *	Class based on CWPoint but intended to handle bitmap x and y
	 *	Used for georeferencing bitmaps.
	 */
	class GCPoint extends CWPoint{
		public int bitMapX = 0;
		public int bitMapY = 0;

		public GCPoint(){
		}

		public GCPoint(CWPoint p) {
			super(p);
		}

		/**
		 * If you are using Gau�-Kr�ger, put lat = northing, lon = easting
		 * @param lat
		 * @param lon
		 */
		public GCPoint(double lat, double lon){
			this.latDec = lat;
			this.lonDec = lon;
			this.utmValid = false;
		}
		public GCPoint(CWPoint ll, Point px) {
			super(ll);
			bitMapX = px.x;
			bitMapY = px.y;
		}
	}
