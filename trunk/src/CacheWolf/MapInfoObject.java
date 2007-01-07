package CacheWolf;

import ewe.fx.Point;
import ewe.io.BufferedWriter;
import ewe.io.FileReader;
import ewe.io.FileWriter;
import ewe.io.FilenameFilter;
import ewe.io.IOException;
import ewe.io.PrintWriter;
import ewe.sys.*;
import ewe.ui.MessageBox;
import ewe.ui.SplittablePanel;

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
	public MapInfoObject(double scale) {
		digSep = MyLocale.getDigSeparator();
		mapName="empty 1 Pixel = "+scale+"meters";
		double meters2deg = 1/(1000*(new CWPoint(0,0)).getDistance(new CWPoint(1,0)));
		double pixel2deg = meters2deg * scale;
		affine[0]=0; //x2lat
		affine[1]=pixel2deg; //x2lon
		affine[2]=-pixel2deg; //y2lat
		affine[3]=0; //y2lon
		affine[4]=0; //left
		affine[5]=1; //top
		lowlat = 0; //bottom 
		lowlon = 1; //right
		doCalculations();
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
			fileName = mapsPath + thisMap + ".png";
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
		doCalculations();
	}

	/**
	 * calculates center, diagonal size of the map and inverse to affine transformation
	 * @throws ArithmeticException when affine data is not correct, e.g. it is not possible to inverse affine-transformation
	 */
	
	private void doCalculations() throws ArithmeticException {
	center.set((lowlat + affine[4])/2,(lowlon + affine[5])/2);
	sizeKm = java.lang.Math.abs((float)center.getDistance(lowlat, lowlon)) *2;
	
	double nenner=(-affine[1]*affine[2]+affine[0]*affine[3]);
	transLatX = affine[3]/nenner; // nenner == 0 cannot happen as long als affine is correct
	transLonX = -affine[2]/nenner;
	transLatY = -affine[1]/nenner;
	transLonY = affine[0]/nenner;
}
	
	
	/**
	*	Method to save a world file (.wfl)
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
		this.fileName = mapsPath + "/" + mapFileName + ".png";
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
	public CWPoint calcLatLon(int x, int y) {
		 CWPoint ll = new CWPoint();
		 ll.latDec = (double)x * affine[0] + (double)y * affine[2] + affine[4];
		 ll.lonDec = (double)x * affine[1] + (double)y * affine[3] + affine[5];
		 return ll;
	}
	public CWPoint calcLatLon(Point p) {
		return calcLatLon(p.x, p.y);
	}
}