package CacheWolf;

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
		super();
		double testA = Convert.toDouble("1,50") + Convert.toDouble("3,00");
		if(testA == 4.5) digSep = ","; else digSep = ".";
	}
	/**
	 * Method to load a .wfl-file
	 * @throws IOException when there was a problem reading .wfl-file
	 * @throws IOException when lat/lon were out of range
	 */
	public void loadwfl(String mapsPath, String thisMap) throws IOException {
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
		Vm.debug("Right: " + lowlon);
		Vm.debug("Test: " + pos.lonDec);
		*/
		if(affine[4] >= pos.latDec && pos.latDec >= lowlat && affine[5] <= pos.lonDec && pos.lonDec <= lowlon) isInBound = true;
		return isInBound;
	}
}