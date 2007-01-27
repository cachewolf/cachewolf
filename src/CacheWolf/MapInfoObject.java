package CacheWolf;

import ewe.fx.Point;
import ewe.io.BufferedWriter;
import ewe.io.FileReader;
import ewe.io.FileWriter;
import ewe.io.FilenameFilter;
import ewe.io.File;
import ewe.io.IOException;
import ewe.io.PrintWriter;
import ewe.sys.*;

/**
 * @author r
 *
 */
public class MapInfoObject{
	//World file:
	// x scale
	// y scale
	// x rotation
	// y rotation
	// lon of upper left corner of image
	// lat of upper left corner of image
	// lon of lower right corner of image
	// lat of lower right corner of image
	public double[] affine = {0,0,0,0,0,0};
	public double lowlat = 0;
	public double lowlon = 0;
	public double transLatX, transLatY, transLonX, transLonY; // this are needed for the inervers calculation from lat/lon to x/y
	public CWPoint center = new CWPoint();
	public float sizeKm = 0; // diagonale
	public float scale; // in meters per pixel, note: it is assumed that this scale identifying the scale of the map
	public float zoomFactor = 1; // if the image is zoomed, direct after laoding always 1
	public Point shift = new Point (0,0);
	public CWPoint OrigUpperLeft; // this is only valid after zooming 
	public float rotationRad; // contains the rotation of the map == north direction in rad
	public String fileNameWFL = new String();
	public String fileName = new String();
	public String mapName = new String();
	//private Character digSep = new Character(' ');
	private String digSep = new String();
	/*
	 * loads an .wfl file
	 * throws FileNotFoundException and IOException (data out of range)
	 * @maps Path to .wfl file
	 * @thisMap filename of .wfl file without ".wfl"
	 * @DigSep "." or ","
	 */	

	public MapInfoObject() {
		digSep = MyLocale.getDigSeparator();
		//double testA = Convert.toDouble("1,50") + Convert.toDouble("3,00");
		//if(testA == 4.5) digSep = ","; else digSep = ".";
	}

	/*
	 * constructes an MapInfoObject without an associated map
	 * but with 1 Pixel = scale meters
	 */
	public MapInfoObject(double scalei, double lat) {
		digSep = MyLocale.getDigSeparator();
		mapName="empty 1 Pixel = "+scalei+"meters";
		double meters2deg = 1/(1000*(new CWPoint(0,0)).getDistance(new CWPoint(1,0)));
		double pixel2deg = meters2deg * scalei;
		affine[0]=0; //x2lat
		affine[1]=pixel2deg / java.lang.Math.cos(lat); //x2lon
		affine[2]=-pixel2deg; //y2lat
		affine[3]=0; //y2lon
		affine[4]=1; //top
		affine[5]=0; //left
		lowlat = 0; //buttom
		lowlon = 1; //right
		OrigUpperLeft = new CWPoint(affine[4], affine[5]);
		doCalculations();
	}

	/**
	 * constructs an MapInfoObject with an associated map
	 * with 1 Pixel = scale meters, center and width, hight in pixels
	 * @param name path and filename of .wfl file without the extension (it is needed because the image will be searched in the same directory)
	 */
	public MapInfoObject(double scalei, CWPoint center, int width, int hight, String name) {
		digSep = MyLocale.getDigSeparator();
		mapName = name+".wfl";

		double meters2deg = 1/(1000*(new CWPoint(0,0)).getDistance(new CWPoint(1,0)));
		double pixel2deg = meters2deg * scalei;
		double pixel2deghorizontal = pixel2deg / java.lang.Math.cos(center.latDec*java.lang.Math.PI / 180); 
		affine[0]=0; //x2lat
		affine[1]=pixel2deghorizontal; //x2lon
		affine[2]=-pixel2deg; //y2lat
		affine[3]=0; //y2lon
		affine[4]=center.latDec + hight / 2 *pixel2deg; //top
		affine[5]=center.lonDec - width / 2 *pixel2deghorizontal; //left
		lowlat = center.latDec - hight / 2 *pixel2deg; //buttom
		lowlon = center.lonDec + width / 2 *pixel2deghorizontal; //right
		fileNameWFL = name;
		OrigUpperLeft = new CWPoint(affine[4], affine[5]);
		doCalculations();
	}
	

	/** 
	 * @return the filename of the associated map image, "" if no file is associated, null if associated file could not be found
	 */
	public String getImageFilename() {
		if (fileName == null || fileName.length() > 0) return fileName;
		if (fileNameWFL.length() == 0) return "";
		String n = fileNameWFL.substring(0, fileNameWFL.lastIndexOf("."));
		return Common.getImageName(n);
	}
	
	/**
	 * Method to load a .wfl-file
	 * @throws IOException when there was a problem reading .wfl-file
	 * @throws IOException when lat/lon were out of range
	 * @throws ArithmeticException when affine data is not correct, e.g. it is not possible to inverse affine-transformation
	 */
	public void loadwfl(String mapsPath, String thisMap) throws IOException, ArithmeticException {
		FileReader in = new FileReader(mapsPath + thisMap + ".wfl");
		String line = new String();
		try {
			for(int i = 0; i<6;i++){
				line = in.readLine();
				if (digSep.equals(",")) {line = line.replace('.',','); } // digSep == ',' musss genau so lauten. digsep.equals(',') wirft eine Exception auf PocketPC, digsep.equals(",") wirft keine Exception, funktioniert aber nicht! 
				else line = line.replace(',','.');
				affine[i] = Convert.toDouble(line);
			}
			line = in.readLine();
			if (digSep.equals(",")) {line = line.replace('.',','); }
			else line = line.replace(',','.');
			lowlat = Convert.toDouble(line);
			line = in.readLine();
			if (digSep.equals(",")) {line = line.replace('.',','); }
			else line = line.replace(',','.');
			lowlon = Convert.toDouble(line);

			fileNameWFL = mapsPath + thisMap + ".wfl";
			fileName = ""; //mapsPath + thisMap + ".png";
			mapName = thisMap;
			in.close();
			if(affine[4] > 90 || affine[4] < -90 || affine[5] < -180 || affine[5] > 360 ||
					lowlat > 90 || lowlat < -90 || lowlon > 360 || lowlon < -180 ) {
				affine[0] = 0; affine[1] = 0; affine[2] = 0; affine[3] = 0; affine[4] = 0; affine[5] = 0;
				lowlat = 0; lowlon = 0;
				throw (new IOException("Lat/Lon out of range while reading "+mapsPath + thisMap + ".wfl"));
			}
		} catch (NullPointerException e) { // in.readline liefert null zurück, wenn keine Daten mehr vorhanden sind
			throw (new IOException("not enough lines in file "+mapsPath + thisMap + ".wfl"));
		}
		OrigUpperLeft = new CWPoint(affine[4], affine[5]);
		doCalculations();
	}

	/**
	 * calculates center, diagonal size of the map and inverse to affine transformation
	 * @throws ArithmeticException when affine data is not correct, e.g. it is not possible to inverse affine-transformation
	 */

	private void doCalculations() throws ArithmeticException {
		try {
			center.set((lowlat + affine[4])/2,(lowlon + affine[5])/2);
			sizeKm = java.lang.Math.abs((float)center.getDistance(lowlat, lowlon)) *2;

			//calculate reverse affine
			double nenner=(-affine[1]*affine[2]+affine[0]*affine[3]);
			transLatX = affine[3]/nenner; // nenner == 0 cannot happen as long als affine is correct
			transLonX = -affine[2]/nenner;
			transLatY = -affine[1]/nenner;
			transLonY = affine[0]/nenner;

			// calculate north direction
			float scaleXpixels = 1/(float)java.lang.Math.sqrt(java.lang.Math.pow(transLonX,2)+java.lang.Math.pow(transLonY,2));
			//	float scaleY = 1/(float)java.lang.Math.sqrt(java.lang.Math.pow(transLatX,2)+java.lang.Math.pow(transLatY,2));
			float rotationX2x=(float)transLonX*scaleXpixels;
			float rotationX2y=(float)transLonY*scaleXpixels;
			//rotationY2y=-(float)transLatY*scaleY; // lat -> y = -, y -> y = +
			//rotationY2x=-(float)transLatX*scaleY; // uncomment an make it a field of MapInfoObject if you need translation from x to x rotated
			rotationRad = (float)java.lang.Math.atan(rotationX2y);
			if (rotationX2x < 0) rotationRad = (float)java.lang.Math.PI - rotationRad;
			// calculate scale in meters per pixel
			double metersPerLat = 1000*(new CWPoint(0,0)).getDistance(new CWPoint(1,0));
			scale = (float) java.lang.Math.abs((affine[2] * metersPerLat)); 
		} catch (ArithmeticException ex) { throw new ArithmeticException("Not allowed values in affine\n (matrix cannot be inverted)\n in file \n" + fileNameWFL); }
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
				affine[4]==0 && affine[5]==0 ) throw (new IllegalArgumentException("map not calibrated"));
		PrintWriter outp =  new PrintWriter(new BufferedWriter(new FileWriter(mapsPath + "/" + mapFileName + ".wfl")));
		String towrite=Convert.toString(affine[0])+"\n" +
		Convert.toString(affine[1])+"\n" +
		Convert.toString(affine[2])+"\n" + 
		Convert.toString(affine[3])+"\n" + 
		Convert.toString(affine[4])+"\n" +
		Convert.toString(affine[5])+"\n" +
		Convert.toString(lowlat)+"\n" +
		Convert.toString(lowlon)+"\n";
		if (digSep.equals(",")) towrite=towrite.replace(',', '.');
		outp.print(towrite);
		outp.close();
		this.fileName = ""; // this will be set in getImageFilenam //mapsPath + "/" + mapFileName + ".png";
		this.fileNameWFL = mapsPath + "/" + mapFileName + ".wfl";
		this.mapName = mapFileName;
	}

	public boolean inBound(CWPoint pos){
		boolean isInBound = false;
		/*
		Vm.debug(mapName);
		Vm.debug("Top: " + affine[4]);
		Vm.debug("Bottom: " + lowlat);
		Vm.debug("Test: " + pos.latDec);
		Vm.debug("Left: " + affine[5]);
		Vm.debug("Right: " + lowlon);
		Vm.debug("Test: " + pos.lonDec);
		 */
		if(affine[4] >= pos.latDec && pos.latDec >= lowlat && affine[5] <= pos.lonDec && pos.lonDec <= lowlon) isInBound = true;
		return isInBound;
	}

	/**
	 * zoom in / out
	 * @param zf zf > 1 == zoom in, zoom is relative to original unscaled image
	 * @param diffX shift of map in pixels (if the map was cropped while zooming) in the not zoomed image
	 * @param diffY
	 */
	public void zoom(float zf, int diffX, int diffY) {
		// restore original values to calculate corret shift (upperleft)
		affine[4] = OrigUpperLeft.latDec;
		affine[5] = OrigUpperLeft.lonDec;
		affine[0] = affine[0] * zoomFactor; 
		affine[1] = affine[1] * zoomFactor; 
		affine[2] = affine[2] * zoomFactor;
		affine[3] = affine[3] * zoomFactor;
		CWPoint upperleft = calcLatLon(diffX, diffY);
		affine[4] = upperleft.latDec;
		affine[5] = upperleft.lonDec;
		affine[0] = affine[0] / zf ; 
		affine[1] = affine[1] / zf ; 
		affine[2] = affine[2] / zf ; 
		affine[3] = affine[3] / zf ; 
		zoomFactor = zf ;
		shift.x = diffX;
		shift.y = diffY;
		doCalculations(); // TODO lowlat neu berechnen?
	}

	public boolean inBound(CWGPSPoint pos){
		boolean isInBound = false;
		/*
		Vm.debug(mapName);
		Vm.debug("Top: " + affine[4]);
		Vm.debug("Bottom: " + lowlat);
		Vm.debug("Test: " + pos.latDec);
		Vm.debug("Left: " + affine[5]);
		Vm.debug("Right: " + lowlon); // lowlon should be left?!
		Vm.debug("Test: " + pos.lonDec);
		 */
		if(affine[4] >= pos.latDec && pos.latDec >= lowlat && affine[5] <= pos.lonDec && pos.lonDec <= lowlon) isInBound = true;
		return isInBound;
	}
	public boolean inBound(double lati, double loni){
		boolean isInBound = false;
		if(affine[4] >= lati && lati >= lowlat && affine[5] <= loni && loni <= lowlon) isInBound = true;
		return isInBound;
	}
	/**
	 * Method to calculate bitmap x,y of the current map using
	 * lat and lon target coordinates. There ist no garanty that
	 * the returned coordinates are inside of the map. They can be negative.
	 * @param lat
	 * @param lon
	 */
	public Point calcMapXY(double lat, double lon){
		Point coords = new Point();
		double b[] = new double[2];
		b[0] = lat - affine[4];
		b[1] = lon - affine[5];
		double mapx=transLatX* b[0] + transLonX*b[1];
		double mapy=transLatY* b[0] + transLonY*b[1];
		coords.x = (int)mapx;
		coords.y = (int)mapy;
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
		ll.latDec = (double)x * affine[0] + (double)y * affine[2] + affine[4];
		ll.lonDec = (double)x * affine[1] + (double)y * affine[3] + affine[5];
		return ll;
	}
	public CWPoint calcLatLon(Point p) {
		return calcLatLon(p.x, p.y);
	}
	public Area getArea(){
		return new Area(new CWPoint(affine[4], affine[5]), new CWPoint(lowlat, lowlon));
	}
}