/*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
	See http://www.cachewolf.de/ for more information.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package CacheWolf.navi;

import CacheWolf.Preferences;
import CacheWolf.database.BoundingBox;
import CacheWolf.database.CWPoint;
import CacheWolf.database.CoordinatePoint;
import CacheWolf.utils.Common;
import CacheWolf.utils.Matrix;
import CacheWolf.utils.MyLocale;
import ewe.fx.Point;
import ewe.io.BufferedWriter;
import ewe.io.FileInputStream;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.InputStreamReader;
import ewe.io.PrintWriter;
import ewe.sys.Convert;
import ewe.util.mString;

// World file:
// x scale affine[1]
// y scale affine[2]
// x rotation affine[0]
// y rotation affine[3]
// lon of upper left corner of image
// lat of upper left corner of image
// lon of lower right corner of image
// lat of lower right corner of image

/**
 * class to read, save and do the calculations for calibrated and calibrating maps
 * start offset for language file: 4300
 * 
 * @author pfeffer
 * 
 */
public class MapInfoObject extends BoundingBox {
    public CWPoint center = new CWPoint();
    public float sizeKm = 0; // Diagonal in meters per pixel
    public float scale; // identifying the scale of the map, automatically adjusted when zooming
    public float zoomFactor = 1; // if the image is zoomed else 1
    public Point shift = new Point(0, 0);
    public CWPoint origAffineUpperLeft; // this is only valid after zooming
    public float rotationRad; // contains the rotation of the map == north direction in rad
    private MapImageFileNameObject mapImageFileNameObject;
    private byte mapType = 0; // 0=aus wfl-file, 1=empty map, 2=aus Tilestructure, 3=aus pack-file
    private final double[] affine = { 0, 0, 0, 0 };
    private CWPoint affineTopleft = new CWPoint();;
    private double transLatX, transLatY, transLonX, transLonY; // needed for the inverse calculation from lat/lon to x/y
    private int coordTrans = 0;

    public MapInfoObject() {
	this.mapImageFileNameObject = new MapImageFileNameObject("", "", "");
    }

    public MapInfoObject(MapInfoObject mapInfoObject) {
	super(mapInfoObject.topleft, mapInfoObject.bottomright);
	this.mapImageFileNameObject = mapInfoObject.mapImageFileNameObject;
	affine[0] = mapInfoObject.affine[0];
	affine[1] = mapInfoObject.affine[1];
	affine[2] = mapInfoObject.affine[2];
	affine[3] = mapInfoObject.affine[3];
	origAffineUpperLeft = new CWPoint(mapInfoObject.origAffineUpperLeft);
	affineTopleft = new CWPoint(mapInfoObject.affineTopleft);
	zoomFactor = mapInfoObject.zoomFactor;
	shift.set(mapInfoObject.shift);
	coordTrans = mapInfoObject.coordTrans;
	doCalculations();
	this.mapType = mapInfoObject.mapType;
    }

    /**
     * constructes an MapInfoObject without an associated map but with 1 Pixel = scale meters
     */
    public MapInfoObject(double scalei, double lat, MapImageFileNameObject mapImageFileNameObject) {
	super(new CWPoint(1, 0), new CWPoint(0, 1));
	this.mapImageFileNameObject = mapImageFileNameObject;
	final double meters2deg = 1 / (1000 * (new CWPoint(0, 0)).getDistance(new CWPoint(1, 0)));
	final double pixel2deg = meters2deg * scalei;
	affine[0] = 0; // x2lat
	affine[1] = pixel2deg / java.lang.Math.cos(lat * java.lang.Math.PI / 180); // x2lon
	affine[2] = -pixel2deg; // y2lat
	affine[3] = 0; // y2lon
	topleft.latDec = 1; // top
	topleft.lonDec = 0; // left
	bottomright.latDec = 0; // bottom
	bottomright.lonDec = 1; // right
	affineTopleft.set(topleft);
	doCalculations();
	origAffineUpperLeft = new CWPoint(affineTopleft);
	this.mapType = 1;
    }

    /**
     * using wfl-file
     */
    public MapInfoObject(MapImageFileNameObject mapImageFileNameObject) throws IOException, ArithmeticException {
	super();
	this.mapImageFileNameObject = mapImageFileNameObject;
	this.loadWFL();
	this.mapType = 0;
    }

    // used by download Tile und calc easyFindString in generation MapsList
    /**
     * using Tile - Infos x, y, zoom
     * Tile must have Size of 256 * 256
     */
    public MapInfoObject(int x, int y, int zoom, MapImageFileNameObject mapImageFileNameObject) {
	super();
	calcTile(x, y, zoom);
	this.mapImageFileNameObject = mapImageFileNameObject;
	this.mapType = 2; // oder 3
    }

    /**
     * using mapListEntry
     * 
     */
    public MapInfoObject(MapListEntry mapListEntry) {
	super(); // creates this.topleft and this.bottomright
	String p[];
	int zoom;
	int x;
	int y;
	// 		
	this.mapType = mapListEntry.mapType;
	this.mapImageFileNameObject = mapListEntry.getMapImageFileNameObject();
	//
	switch (mapListEntry.mapType) {
	case 1:
	    Preferences.itself().log("create MapInfoObject from MapListEntry should never be called for empty map. the MapInfoObject should be created on creation of MapListEntry");
	    break;
	case 2:
	    p = ewe.util.mString.split(mapImageFileNameObject.getPath(), '/');
	    zoom = Common.parseInt(p[p.length - 3]);
	    x = Common.parseInt(p[p.length - 2]);
	    y = Common.parseInt(mapImageFileNameObject.getMapName());
	    calcTile(x, y, zoom);
	    mapImageFileNameObject.setImageExtension(Common.getExtension(Common.getImageName(Preferences.itself().absoluteMapsBaseDir + mapImageFileNameObject.getPath() + mapImageFileNameObject.getMapName())));
	    break;
	case 3:
	    p = ewe.util.mString.split(mapImageFileNameObject.getMapName(), '!');
	    zoom = Common.parseInt(p[4]);
	    x = Common.parseInt(p[5]);
	    y = Common.parseInt(p[6]);
	    calcTile(x, y, zoom);
	    break;
	default: // 0
	    try {
		this.loadWFL();
	    } catch (Exception e) {
	    }
	    break;
	}
	mapListEntry.map = this;
    }

    private CWPoint Tile2LatLon(int x, int y, int zoom) {
	double xx = x;
	double yy = y;
	// double zz = zoom;
	double zz2 = Math.pow(2, zoom);
	double lon = xx / zz2 * 360.0 - 180.0;
	double n = Math.PI - (2.0 * Math.PI * yy) / zz2;
	double lat = (Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)))) * 180.0 / Math.PI;
	return new CWPoint(lat, lon);
    }

    /**
     * 
     * @param n
     *            without ".wfl"
     * @return name of the map including fast-find-prefix
     */
    public String createMapName(String n) {
	return getFfPrefix() + n;
    }

    /**
     * @return the filename of the associated map image<br>
     *         null : the referenced file doesn't exist<br>
     *         "" = empty map -> no file is associated<br>
     */
    public String getImagePathAndName() {
	if (this.mapType == 1)
	    return ""; // 1=empty map
	else if (this.mapType == 3) {
	    return Preferences.itself().absoluteMapsBaseDir + this.mapImageFileNameObject.getPath() + ".pack!" + this.mapImageFileNameObject.getMapName();
	} else {
	    if (this.mapImageFileNameObject.getImageExtension().length() > 0) {
		return Preferences.itself().absoluteMapsBaseDir + this.mapImageFileNameObject.getPath() + this.mapImageFileNameObject.getMapName() + this.mapImageFileNameObject.getImageExtension();
	    } else {
		String in = Common.getImageName(Preferences.itself().absoluteMapsBaseDir + this.mapImageFileNameObject.getPath() + this.mapImageFileNameObject.getMapName());
		this.mapImageFileNameObject.setImageExtension(Common.getExtension(in));
		return in;
	    }
	}
    }

    /**
     * Method to load a .wfl-file
     * 
     * @param mapsPath
     *            path to the map inclunding / at the end
     * @param thisMap
     *            name of the map without extension
     * @throws IOException
     *             when there was a problem reading .wfl-file
     * @throws IOException
     *             when lat/lon were out of range
     * @throws ArithmeticException
     *             when affine data is not correct, e.g. it is not possible to inverse affine-transformation
     */
    public void loadWFL() throws IOException, ArithmeticException {
	final String FilePaN = Preferences.itself().absoluteMapsBaseDir + mapImageFileNameObject.getPath() + "/" + mapImageFileNameObject.getMapName() + ".wfl";
	final FileInputStream instream = new FileInputStream(FilePaN);
	final InputStreamReader in = new InputStreamReader(instream);
	try {
	    for (int i = 0; i < 4; i++) {
		affine[i] = Common.parseDoubleException(in.readLine());
	    }
	    affineTopleft.latDec = Common.parseDoubleException(in.readLine());
	    affineTopleft.lonDec = Common.parseDoubleException(in.readLine());
	    bottomright.latDec = Common.parseDoubleException(in.readLine());
	    bottomright.lonDec = Common.parseDoubleException(in.readLine());
	    // readLine returns null, if End of File reached (==> coordTrans = 0)
	    coordTrans = Common.parseInt(in.readLine());
	    in.close();
	    if (!bottomright.isValid()) {
		affine[0] = 0;
		affine[1] = 0;
		affine[2] = 0;
		affine[3] = 0;
		topleft.makeInvalid();
		throw (new IOException(MyLocale.getMsg(4301, "Lat/Lon out of range while reading ") + FilePaN));
	    }
	} catch (final NullPointerException e) {
	    // in.readline liefert null zurück, wenn keine Daten mehr vorhanden sind
	    throw (new IOException(MyLocale.getMsg(4303, "not enough lines in file ") + FilePaN));
	}
	doCalculations();
	origAffineUpperLeft = new CWPoint(affineTopleft);
    }

    private void calcTile(int x, int y, int zoom) {

	coordTrans = TransformCoordinates.EPSG_WGS84;
	// coordTrans = TransformCoordinates.EPSG_Mercator_1SP_Google;

	CWPoint tl = Tile2LatLon(x, y, zoom);
	affineTopleft = new CWPoint(TransformCoordinatesProperties.fromWgs84(tl, coordTrans));
	bottomright = Tile2LatLon(x + 1, y + 1, zoom);
	CWPoint affineBottomright = new CWPoint(TransformCoordinatesProperties.fromWgs84(bottomright, coordTrans));

	affine[1] = (affineBottomright.lonDec - affineTopleft.lonDec) / 256;
	affine[2] = (affineBottomright.latDec - affineTopleft.latDec) / 256;
	affine[0] = 0;
	affine[3] = 0;

	doCalculations();

	origAffineUpperLeft = new CWPoint(affineTopleft);

    }

    public void evalGCP(ewe.util.Vector GCPs, int imageWidth, int imageHeight) throws IllegalArgumentException {
	evalGCP(GCPs, imageWidth, imageHeight, 0);
    }

    /**
     * Method to evaluate ground control points (georeferenced points) and identify the parameters for the affine transformation
     * 
     * @throws IllegalArgumentException
     *             when less than 3 georeferenced points were given in GCPs
     */

    public void evalGCP(ewe.util.Vector GCPs, int imageWidth, int imageHeight, int epsg_code) throws IllegalArgumentException {
	// N 48 16.000 E 11 32.000
	// N 48 16.000 E 11 50.000
	// N 48 9.000 E 11 32.000
	if (GCPs.size() < 3)
	    throw new IllegalArgumentException(MyLocale.getMsg(4304, "not enough points to calibrate the map"));
	GCPoint gcp = new GCPoint();
	// Calculate parameters for latitutde affine transformation (affine
	// 0,2,4)
	Matrix X = new Matrix(GCPs.size(), 3);
	Matrix trg = new Matrix(GCPs.size(), 1);
	for (int i = 0; i < GCPs.size(); i++) {
	    gcp = (GCPoint) GCPs.get(i);
	    X.matrix[i][0] = 1;
	    X.matrix[i][1] = gcp.bitMapX;
	    X.matrix[i][2] = gcp.bitMapY;
	    trg.matrix[i][0] = gcp.latDec;
	}
	Matrix Xtran = new Matrix(X);
	Xtran.Transpose();
	Matrix XtranX = new Matrix(Xtran);
	XtranX.Multiply(X);
	Matrix XtranXinv = new Matrix(XtranX);
	XtranXinv.Inverse();
	Matrix beta = new Matrix(XtranXinv);
	beta.Multiply(Xtran);
	beta.Multiply(trg);
	affine[0] = beta.matrix[1][0];
	affine[2] = beta.matrix[2][0];
	affineTopleft.latDec = beta.matrix[0][0];

	// Calculate parameters for longitude affine transformation (affine
	// 1,3,5)
	X = new Matrix(GCPs.size(), 3);
	trg = new Matrix(GCPs.size(), 1);
	for (int i = 0; i < GCPs.size(); i++) {
	    gcp = (GCPoint) GCPs.get(i);
	    X.matrix[i][0] = 1;
	    X.matrix[i][1] = gcp.bitMapX;
	    X.matrix[i][2] = gcp.bitMapY;
	    trg.matrix[i][0] = gcp.lonDec;
	}
	Xtran = new Matrix(X);
	Xtran.Transpose();
	XtranX = new Matrix(Xtran);
	XtranX.Multiply(X);
	XtranXinv = new Matrix(XtranX);
	XtranXinv.Inverse();
	beta = new Matrix(XtranXinv);
	beta.Multiply(Xtran);
	beta.Multiply(trg);
	affine[1] = beta.matrix[1][0];
	affine[3] = beta.matrix[2][0];
	affineTopleft.lonDec = beta.matrix[0][0];
	coordTrans = epsg_code;
	bottomright = calcLatLon(imageWidth, imageHeight);
	doCalculations();
    }

    /**
     * calculates centre, diagonal size of the map and inverse to affine transformation
     * 
     * @throws ArithmeticException
     *             when affine data is not correct, e.g. it is not possible to inverse affine-transformation
     */

    private void doCalculations() throws ArithmeticException {
	try {
	    // topleft = wgs84(affineTopLeft)
	    topleft.set(calcLatLon(0, 0));
	    // die Mitte durch Halbierung
	    center.set((bottomright.latDec + topleft.latDec) / 2, (bottomright.lonDec + topleft.lonDec) / 2);
	    // Diagonale Länge = 2 * (Mitte bis UntenRechts)  
	    sizeKm = java.lang.Math.abs((float) center.getDistance(bottomright)) * 2;

	    // calculate reverse affine
	    // nenner == 0 cannot happen as long als affine is correct
	    final double nenner = (-affine[1] * affine[2] + affine[0] * affine[3]);
	    transLatX = affine[3] / nenner;
	    transLonX = -affine[2] / nenner;
	    transLatY = -affine[1] / nenner;
	    transLonY = affine[0] / nenner;

	    // calculate north direction
	    final Point c = calcMapXY(center);
	    final int heightpixel = c.y * 2;

	    c.y -= 1000;
	    rotationRad = (float) (center.getBearing(calcLatLon(c)) / 180 * Math.PI);
	    // note: the direction of nord can vary across the image.
	    // In Gauß-Krüger Projection it does change about 1 degree per 10km!
	    // (float)java.lang.Math.atan(rotationX2y);
	    if (rotationRad > Math.PI)
		rotationRad -= 2 * Math.PI;

	    // calculate scale in meters per pixel
	    final double heightkm = calcLatLon(0, heightpixel).getDistance(topleft);
	    scale = (float) (heightkm * 1000 / heightpixel);
	} catch (final ArithmeticException ex) {
	    throw new ArithmeticException(MyLocale.getMsg(4305, "Not allowed values in affine\n (matrix cannot be inverted)\n in file \n") + this.mapImageFileNameObject.getMapName());
	}
    }

    /**
     * Method to save a world file (.wfl)
     * 
     * @throws IOException
     *             when there was a problem writing .wfl-file
     * @throws IllegalArgumentException
     *             when affine[x] for all x == 0 ("map not calibrated").
     */
    public void saveWFL() throws IOException, IllegalArgumentException {
	if (affine[0] == 0 && affine[1] == 0 && affine[2] == 0 && affine[3] == 0 && !topleft.isValid())
	    throw (new IllegalArgumentException(MyLocale.getMsg(4306, "map not calibrated")));
	String PaN = Preferences.itself().absoluteMapsBaseDir + this.mapImageFileNameObject.getPath() + this.mapImageFileNameObject.getMapName() + ".wfl";
	final PrintWriter outp = new PrintWriter(new BufferedWriter(new FileWriter(PaN)));
	final StringBuffer towriteB = new StringBuffer(400);
	towriteB.append(Convert.toString(affine[0])).append("\n");
	towriteB.append(Convert.toString(affine[1])).append("\n");
	towriteB.append(Convert.toString(affine[2])).append("\n");
	towriteB.append(Convert.toString(affine[3])).append("\n");
	towriteB.append(Convert.toString(affineTopleft.latDec)).append("\n"); // EPSG values
	towriteB.append(Convert.toString(affineTopleft.lonDec)).append("\n"); // EPSG values
	towriteB.append(Convert.toString(bottomright.latDec)).append("\n"); // WGS84 values
	towriteB.append(Convert.toString(bottomright.lonDec)).append("\n"); // WGS84 values
	towriteB.append(((coordTrans == 0 || coordTrans == TransformCoordinates.EPSG_WGS84) ? "" : "" + coordTrans + "\n"));
	String towrite = towriteB.toString();
	if (Common.getDigSeparator() == ',')
	    towrite = towrite.replace(',', '.');
	outp.print(towrite);
	outp.close();
    }

    /**
     * zoom in / out
     * 
     * @param zf
     *            zf > 1 == zoom in, zoom is relative to original unscaled image
     * @param diffX
     *            shift of map in pixels (if the map was cropped while zooming) in the not zoomed image
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
	CoordinatePoint upperleft = calcLatLon(diffX, diffY);
	if (coordTrans != 0)
	    upperleft = TransformCoordinatesProperties.fromWgs84(upperleft, coordTrans);
	affineTopleft.latDec = upperleft.latDec;
	affineTopleft.lonDec = upperleft.lonDec;
	affine[0] = affine[0] / zf;
	affine[1] = affine[1] / zf;
	affine[2] = affine[2] / zf;
	affine[3] = affine[3] / zf;
	zoomFactor = zf;
	shift.x = diffX;
	shift.y = diffY;
	doCalculations();
    }

    /**
     * Method to calculate bitmap x,y of the current map using lat and lon target coordinates.<br>
     * There is no guaranty that the returned coordinates are inside of the map. They can be negative.<br>
     * 
     * @param ll
     * @return Point
     */
    public Point calcMapXY(CoordinatePoint ll) {
	CoordinatePoint t;
	if (coordTrans != 0)
	    t = TransformCoordinatesProperties.fromWgs84(ll, coordTrans);
	else
	    t = ll;
	final Point coords = new Point();
	double b0, b1;
	b0 = t.latDec - affineTopleft.latDec;
	b1 = t.lonDec - affineTopleft.lonDec;
	final double mapx = transLatX * b0 + transLonX * b1;
	final double mapy = transLatY * b0 + transLonY * b1;
	coords.x = (int) Math.round(mapx);
	coords.y = (int) Math.round(mapy);
	return coords;
    }

    /**
     * gives back lat/lon from x, y in map
     * 
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
     * Get the prefix used for easy and fast finding of the best map The filname of the .wfl and respective image should start with this prefix in order to make finding the best map much faster
     * 
     * @return
     */
    public String getFfPrefix() {
	return "FF1" + getEasyFindString() + "E-";
    }

    public MapImageFileNameObject getMapImageFileNameObject() {
	return mapImageFileNameObject;
    }

    public String getMapNameForList() {
	return mapImageFileNameObject.getMapNameForList(mapType);
    }

    public byte getMapType() {
	return mapType;
    }

}

/**
 * Class based on CWPoint but intended to handle bitmap x and y Used for georeferencing bitmaps.
 */
class GCPoint extends CWPoint {
    public int bitMapX = 0;
    public int bitMapY = 0;

    public GCPoint() { // Public constructor
    }

    public GCPoint(CWPoint p) {
	super(p);
    }

    /**
     * If you are using Gauß-Krüger, put lat = northing, lon = easting
     * 
     * @param lat
     * @param lon
     */
    public GCPoint(double lat, double lon) {
	this.latDec = lat;
	this.lonDec = lon;
    }

    public GCPoint(CWPoint ll, Point px) {
	super(ll);
	bitMapX = px.x;
	bitMapY = px.y;
    }
}

class MapImageFileNameObject {

    private String path; // relativ to Preferences.itself().absoluteMapsBaseDir
    private String mapName; // filename without Extension
    private String imageExtension; // ".png"; with dot

    MapImageFileNameObject(String path, String mapName, String extension) {
	this.path = path;
	this.mapName = mapName;
	this.imageExtension = extension;
    }

    public String getPath() {
	return path;
    }

    public void setPath(String path) {
	this.path = path;
    }

    public String getMapName() {
	return mapName;
    }

    public void setMapName(String mapName) {
	this.mapName = mapName;
    }

    public String getImageExtension() {
	return imageExtension;
    }

    public void setImageExtension(String imageExtension) {
	this.imageExtension = imageExtension;
    }

    public String getMapNameForList(byte mapType) {
	if (mapType == 2) {
	    return this.path + this.mapName;
	} else if (mapType == 3) {
	    String s[] = mString.split(this.mapName, '!');
	    return this.path + " zoom: " + s[4] + " x,y: " + s[5] + "," + s[6];
	} else
	    return this.mapName;
    }

}