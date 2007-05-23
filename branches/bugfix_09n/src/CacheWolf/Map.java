package CacheWolf;

import ewe.util.*;
import ewe.io.*;
import ewe.filechooser.*;
import ewe.sys.*;
import ewe.ui.*;
import ewe.graphics.*;
import ewe.fx.*;

import com.stevesoft.ewe_pat.*;

/**
 *	This class is the main class for mapping,
 *	 (moving map, georeferencing maps, etc)
 *	in CacheWolf.
 *	It also provides a class for importing maps
 *	This class id=4100
 */
public class Map extends Form {
	Preferences pref;
	String mapsPath = new String();
	String thisMap = new String();
	public String selectedMap = new String();
	CellPanel infPanel;
	mLabel infLabel = new mLabel("                          ");
	Vector GCPs = new Vector();
	MapInfoObject wfl = new MapInfoObject();
	mButton infButton;
	ScrollBarPanel scp;
	AniImage mapImg;
	int imageWidth, imageHeight = 0;

	/**
	 *	This constructor should be used when importing maps
	 */
	public Map(Preferences pref){
		this.pref = pref;
		mapsPath = pref.getMapManuallySavePath(true)+"/"; //File.getProgramDirectory() + "/maps/";
	}

	/**
	 *	When a user clicks on the map and more than three ground control points exist
	 *	then the calculated coordinate based on the affine transformation is displayed in the
	 *	info panel below the map.
	 *	It helps to identify how good the georeferencing works based on the set GCPs.
	 */
	public void updatePosition(int x, int y){
		if(GCPs.size()>=3  || (wfl.affine[4] > 0 && wfl.affine[5] > 0)){
			double x_ = 0;
			double y_ = 0;
			x_ = wfl.affine[0]*x + wfl.affine[2]*y + wfl.affine[4];
			y_ = wfl.affine[1]*x + wfl.affine[3]*y + wfl.affine[5];
			CWPoint p = new CWPoint(x_ , y_);
			infLabel.setText("--> " + p.getLatDeg(CWPoint.DMS) + " " +p.getLatMin(CWPoint.DMM) + " / " + p.getLonDeg(CWPoint.DMS) + " " + p.getLonMin(CWPoint.DMM));
		}
	}

	/**
	 *	This is the correct constructor for georeferencing maps.
	 */
	public Map(Preferences pref, String mapToLoad, boolean worldfileexists){
		this.pref = pref;
		this.title = MyLocale.getMsg(4106,"Calibrate map:") + " " + mapToLoad;
		this.resizable = true;
		this.moveable = true;
		//this.windowFlagsToSet = Window.FLAG_MAXIMIZE;
		this.setPreferredSize(pref.myAppWidth, pref.myAppHeight);
		thisMap = mapToLoad;
		mapsPath = pref.getMapManuallySavePath(true)+"/"; //File.getProgramDirectory() + "/maps/"; // TDO veraltet
		try {
			wfl.loadwfl(mapsPath, thisMap);
		}catch(FileNotFoundException ex){
			//	Vm.debug("Cannot load world file!");
		}catch (IOException ex) { // is thrown if lat/lon out of range
			MessageBox tmpMB=new MessageBox(MyLocale.getMsg(312, "Error"), ex.getMessage(), MessageBox.OKB);
			tmpMB.execute();
			Vm.debug("Cannot load world file!");
		}
		mapInteractivePanel pane = new mapInteractivePanel(this);
		scp = new ScrollBarPanel(pane);
		Image img = new Image(Common.getImageName(mapsPath + thisMap));
		PixelBuffer pB = new PixelBuffer(img);
		//pB = pB.scale((int)(pref.myAppWidth*0.98),(int)(pref.myAppHeight*0.98));
		mapImg = new AniImage(pB.toDrawableImage());
		pane.addImage(mapImg);
		scp.setPreferredSize(mapImg.getWidth(),mapImg.getHeight());
		imageWidth = mapImg.getWidth();
		imageHeight = mapImg.getHeight();
		this.addLast(scp.getScrollablePanel(), CellConstants.STRETCH, CellConstants.FILL);
		infPanel = new CellPanel();
		infPanel.addNext(infLabel,CellConstants.STRETCH, CellConstants.FILL);
		infButton = new mButton(MyLocale.getMsg(4107,"Done!"));
		infPanel.addLast(infButton,CellConstants.DONTSTRETCH, CellConstants.FILL);
		this.addLast(infPanel, CellConstants.DONTSTRETCH, CellConstants.FILL);
		//scp.repaintNow();
		//this.repaintNow();
	}

	/**
	 *	Add a ground control point to the list
	 *	If the list is longer than 3 GCPs these will be evaluated
	 *	to obtain the required parameters for the affine
	 *	transformation.
	 */
	public void addGCP(GCPoint GCP){
		if (GCP.latDec>90 || GCP.latDec<-90 || GCP.lonDec>360 || GCP.lonDec<-180) throw new IllegalArgumentException("lat/lon out of range: "+GCP.toString());
		GCPs.add(GCP);
		if(GCPs.size() >= 3){
			evalGCP();
		}
	}

	/**
	 *	Returns the number of ground control points in the list. (Vector GCPs)
	 */
	public int getGCPCount(){
		return GCPs.size();
	}

	/**
	 *	Actuall method to evaluate the ground control points and identify the parameters
	 *	for thew affine transformation
	 */
	private void evalGCP(){
		//N 48 16.000 E 11 32.000
		//N 48 16.000 E 11 50.000
		//N 48 9.000 E 11 32.000
		GCPoint gcp = new GCPoint();
		//Calculate parameters for latitutde affine transformation (affine 0,2,4)
		Matrix X = new Matrix(GCPs.size(),3);
		Matrix trg = new Matrix(GCPs.size(),1);
		for(int i = 0; i < GCPs.size();i++){
			gcp = (GCPoint)GCPs.get(i);
			X.matrix[i][0] = 1; X.matrix[i][1] = gcp.bitMapX; X.matrix[i][2] = gcp.bitMapY;
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
		wfl.affine[0] = beta.matrix[1][0];
		wfl.affine[2] = beta.matrix[2][0];
		wfl.affine[4] = beta.matrix[0][0];

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
		Xtran.Transpose();
		XtranX = new Matrix(Xtran);
		XtranX.Multiply(X);
		XtranXinv = new Matrix(XtranX);
		XtranXinv.Inverse();
		beta = new Matrix(XtranXinv);
		beta.Multiply(Xtran);
		beta.Multiply(trg);
		wfl.affine[1] = beta.matrix[1][0];
		wfl.affine[3] = beta.matrix[2][0];
		wfl.affine[5] = beta.matrix[0][0];
		double x_ = 0;
		double y_ = 0;
		x_ = wfl.affine[0]*imageWidth+ wfl.affine[2]*imageHeight + wfl.affine[4];
		y_ = wfl.affine[1]*imageWidth + wfl.affine[3]*imageHeight + wfl.affine[5];
		CWPoint p = new CWPoint(x_ , y_);
		wfl.lowlon = p.lonDec;
		wfl.lowlat = p.latDec;
		//Vm.debug("A B C" + affine[0] + " " + affine[2] + " " + affine[4]);
		//Vm.debug("D E F" + affine[1] + " " + affine[3] + " " + affine[5]);
	}

	/**
	 *	Method to copy ("import") a png based map
	 *	into the maps folder in the CacheWolf base directory.
	 *	
	 *	If the maps directory does not exist it will create it.
	 *	If it finds .map files it will assume these are oziexplorer calibration files.
	 *	It will use these files to automatically georeference the files during import.
	 */
	public int importMap(){
		String rawFileName = new String();
		FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, Global.getPref().baseDir);
		fc.addMask("*.png,*.gif,*.bmp,*.jpg");
		fc.setTitle((String)MyLocale.getMsg(4100,"Select Directory:"));
		int tmp = fc.execute() ; 
		if(tmp != FileChooser.IDYES) return Form.IDCANCEL;
		File inDir = fc.getChosenFile();
		File mapFile;
		InfoBox inf = new InfoBox("Info", MyLocale.getMsg(4109,"Loading maps...            \n"), InfoBox.PROGRESS_WITH_WARNINGS, false); 
		inf.setPreferredSize(220, 300);
		inf.setInfoHeight(100);
		inf.relayout(false);
		Vm.showWait(this, true);
		inf.exec();

		//User selected a map, but maybe there are more png(s)
		//copy all of them!
		//at the same time try to find associated .map files!
		//These are georeference files targeted for OziExplorer.
		//So lets check if we have more than 1 png file:
		Vector files;
		String [] filestemp;
		String line = new String();
		InputStream in = null;
		OutputStream out = null;
		FileReader inMap;
		byte[] buf;
		int len;
		String[] parts;
		filestemp = inDir.list("*.png", File.LIST_FILES_ONLY); // TODO listmultiple verwenden
		files = new Vector(filestemp);
		filestemp = inDir.list("*.jpg", File.LIST_FILES_ONLY);
		files.addAll(filestemp);
		filestemp = inDir.list("*.gif", File.LIST_FILES_ONLY);
		files.addAll(filestemp);
		filestemp = inDir.list("*.bmp", File.LIST_FILES_ONLY);
		files.addAll(filestemp);

		String currfile = null;
		String curInFullPath;
		String curOutFullPath;
		int num = files.size();
		for(int i =  num -1 ; i >= 0;i--){
			currfile = (String) files.get(i);
			inf.setInfo(MyLocale.getMsg(4110,"Loading: ")+ "\n" + currfile + "\n("+(num-i)+"/"+num+")");
			//Copy the file
			//Vm.debug("Copy: " + inDir.getFullPath() + "/" +files[i]);
			//Vm.debug("to: " + mapsPath + files[i]);
			curInFullPath = inDir.getFullPath() + "/" +currfile;
			curOutFullPath = mapsPath + currfile;
			boolean imageerror = false;
			try {
				in = new FileInputStream(curInFullPath);
				buf = new byte[1024*10];
				boolean first = true;
				ByteArray header = new ByteArray(buf);
				while ((len = in.read(buf)) > 0) {
					if (first) {
						first = false;
						header.copyFrom(buf, 0, len);
						ImageInfo tmpII = Image.getImageInfo(header,null);
						imageWidth = tmpII.width;
						imageHeight = tmpII.height;
						out = new FileOutputStream(curOutFullPath); // only create outfile if geImageInfo didn't throw an exception so do it only here not directly after opening input stream
					}
					out.write(buf, 0, len);
				}
			} catch(IOException ex){
				imageerror = true;
				inf.addWarning("\nIO-Error while copying image from: " + curInFullPath + " to: " + curOutFullPath + " error: " + ex.getMessage());
			} catch (IllegalArgumentException e) { // thrown from Image.getImageInfo when it could not interprete the header (e.g. bmp with 32 bits per pixel)
				imageerror = true;
				inf.addWarning("\nError: could not decode image: " + curInFullPath + " - image not copied");
			} finally {
				try {
					if (in != null) in.close();
					if (out  != null) out.close(); 
				} catch (Throwable e) {}
			}
			//Check for a .map file
			rawFileName = currfile.substring(0, currfile.lastIndexOf("."));
			mapFile = new File(inDir.getFullPath() + "/" + rawFileName + ".map");
			if(!imageerror && mapFile.exists()){
				GCPoint gcp1 = new GCPoint();
				GCPoint gcp2 = new GCPoint();
				GCPoint gcp3 = new GCPoint();
				GCPoint gcp4 = new GCPoint();
				GCPoint gcpG = new GCPoint();
				//Vm.debug("Found file: " + inDir.getFullPath() + "/" + rawFileName + ".map");
				try {
					inMap = new FileReader(inDir.getFullPath() + "/" + rawFileName + ".map");
					while((line = inMap.readLine()) != null){
						if(line.equals("MMPNUM,4")){

							line = inMap.readLine();
							parts = mString.split(line, ',');
							gcp1.bitMapX = Convert.toInt(parts[2]);
							gcp1.bitMapY = Convert.toInt(parts[3]);
							if(gcp1.bitMapX == 0) gcp1.bitMapX = 1;
							if(gcp1.bitMapY == 0) gcp1.bitMapY = 1;

							line = inMap.readLine();
							parts = mString.split(line, ',');
							gcp2.bitMapX = Convert.toInt(parts[2]);
							gcp2.bitMapY = Convert.toInt(parts[3]);
							if(gcp2.bitMapX == 0) gcp2.bitMapX = 1;
							if(gcp2.bitMapY == 0) gcp2.bitMapY = 1;

							line = inMap.readLine();
							parts = mString.split(line, ',');
							gcp3.bitMapX = Convert.toInt(parts[2]);
							gcp3.bitMapY = Convert.toInt(parts[3]);
							if(gcp3.bitMapX == 0) gcp3.bitMapX = 1;
							if(gcp3.bitMapY == 0) gcp3.bitMapY = 1;
							//imageWidth = gcp3.bitMapX;
							//imageHeight = gcp3.bitMapY;

							line = inMap.readLine();
							parts = mString.split(line, ',');
							gcp4.bitMapX = Convert.toInt(parts[2]);
							gcp4.bitMapY = Convert.toInt(parts[3]);
							if(gcp4.bitMapX == 0) gcp4.bitMapX = 1;
							if(gcp4.bitMapY == 0) gcp4.bitMapY = 1;

							line = inMap.readLine();
							parts = mString.split(line, ',');
							if(pref.digSeparator.equals(",")) {
								parts[3]= parts[3].replace('.', ',');
								parts[2]= parts[2].replace('.', ',');
							}
							gcpG = new GCPoint(Convert.toDouble(parts[3]), Convert.toDouble(parts[2]));
							gcpG.bitMapX = gcp1.bitMapX;
							gcpG.bitMapY = gcp1.bitMapY;
							addGCP(gcpG);

							line = inMap.readLine();
							parts = mString.split(line, ',');
							if(pref.digSeparator.equals(",")) {
								parts[3]= parts[3].replace('.', ',');
								parts[2]= parts[2].replace('.', ',');
							}
							gcpG = new GCPoint(Convert.toDouble(parts[3]), Convert.toDouble(parts[2]));
							gcpG.bitMapX = gcp2.bitMapX;
							gcpG.bitMapY = gcp2.bitMapY;
							addGCP(gcpG);

							line = inMap.readLine();
							parts = mString.split(line, ',');
							if(pref.digSeparator.equals(",")) {
								parts[3]= parts[3].replace('.', ',');
								parts[2]= parts[2].replace('.', ',');
							}
							gcpG = new GCPoint(Convert.toDouble(parts[3]), Convert.toDouble(parts[2]));
							gcpG.bitMapX = gcp3.bitMapX;
							gcpG.bitMapY = gcp3.bitMapY;
							addGCP(gcpG);

							line = inMap.readLine();
							parts = mString.split(line, ',');
							if(pref.digSeparator.equals(",")) {
								parts[3]= parts[3].replace('.', ',');
								parts[2]= parts[2].replace('.', ',');
							}
							gcpG = new GCPoint(Convert.toDouble(parts[3]), Convert.toDouble(parts[2]));
							gcpG.bitMapX = gcp4.bitMapX;
							gcpG.bitMapY = gcp4.bitMapY;
							addGCP(gcpG);
							
							// get dimensions of image
							while ( (line = inMap.readLine()) != null){
								if (line.startsWith("IWH")){
									parts = mString.split(line, ',');
									imageWidth = Convert.toInt(parts[2]);
									imageHeight = Convert.toInt(parts[3]);
								}
							}

							evalGCP();
							//Vm.debug("Saving .map file to: " + mapsPath + "/" + rawFileName + ".wfl");
							wfl.saveWFL(mapsPath, rawFileName);
							GCPs.clear();
						} // if

					} // while
					if (inMap != null)	inMap.close();
				} catch(IllegalArgumentException ex){ // is thrown from Convert.toDouble and saveWFL if affine[0-5]==0 NumberFormatException is a subclass of IllegalArgumentExepction
					inf.addWarning("\nError while importing .map-file: "+ex.getMessage());
				} catch(IOException ex){
					inf.addWarning("\nIO-Error while reading or writing calibration file\n" + ex.getMessage());
				} 
			} else { // if map file.exists
				if (!imageerror) inf.addWarning("\nNo calibration file found for: " + currfile + " - you can calibrate it manually");
			}
		} // for file
		Vm.showWait(this, false);
		inf.addText("\ndone.");
		inf.addOkButton();
		//inf.addOkButton(); doesn't work
		if(Global.mainTab.mm != null) Global.mainTab.mm.mapsloaded = false; 
		return Form.IDOK;
	}



	/**
	 *	Handles button pressed event
	 *	When the button is pressed a mapname.wfl file is saved in the
	 *	maps directory.
	 */
	public void onEvent(Event ev){

		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			// display coords in another format
			if (ev.target == infButton){
				boolean retry = true;
				while (retry == true) {
					try {
						retry = false;
						wfl.saveWFL(mapsPath, thisMap);
						if(Global.mainTab.mm != null) Global.mainTab.mm.mapsloaded = false; 
					} catch (IOException e) {
						MessageBox tmpMB = new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(321, "Error writing file ") + e.getMessage()+MyLocale.getMsg(324, " - retry?"), MessageBox.YESB | MessageBox.NOB);
						if (tmpMB.execute() == MessageBox.IDYES) retry = true;
					}catch (IllegalArgumentException e) {
						MessageBox tmpMB = new MessageBox(MyLocale.getMsg(144, "Warning"), MyLocale.getMsg(325, "Map not calibrated")+MyLocale.getMsg(324, " - retry?"), MessageBox.YESB | MessageBox.NOB);
						if (tmpMB.execute() == MessageBox.IDYES) { retry = true; break; }
					}
				}
				if (!retry) close(0);
			}
		}
	}
}

/**
 *	Class that creates a panel and loads a map.
 *	It catches click events to display a form where the user may enter the required ccordinates
 *	The data is stored as a ground control point in the calling class: Map
 */
class mapInteractivePanel extends InteractivePanel{
	Map f;
	Locale l = Vm.getLocale();
	LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
	public mapInteractivePanel(Map f){
		this.f = f;
	}

	/**
	 *	Event handler to catch clicks on the map
	 */
	public void imageClicked(AniImage which, Point pos){
		//Vm.debug("X = " +pos.x + " Y = " + pos.y);
		Image img = new Image(31, 31);
		Graphics g = new Graphics(img);
		g.setColor(new Color(0,0,0));
		g.fillRect(0,0,31,31);
		g.setColor(new Color(255,0,0));
		g.drawLine(0,16,31,16);
		g.drawLine(16,0,16,31);
		AniImage aImg = new AniImage(img);
		aImg.setLocation(pos.x-16,pos.y-16);
		aImg.transparentColor = new Color(0,0,0);
		//aImg.properties = mImage.IsNotHot;
		aImg.properties = mImage.AlwaysOnTop;
		this.addImage(aImg);
		g.free();
		this.repaintNow();
		f.updatePosition(pos.x, pos.y);

		CoordsScreen cooS = new CoordsScreen(); // (String)lr.get(4108,"Coordinates:"), (String)lr.get(4108,"Coordinates:"), InfoBox.INPUT);
		if (cooS.execute()==CoordsScreen.IDOK) {
			GCPoint gcp = new GCPoint(cooS.getCoords());
			gcp.bitMapX = pos.x;
			gcp.bitMapY = pos.y;
			f.addGCP(gcp); // throws IllegalArgumentException in case of lon/lat out of range
//			} catch (IllegalArgumentException e) { // NumberFormatException is a subclass of IllagalArgumentException
//			coosInputFormat();
//			this.removeImage(aImg);
		} else this.removeImage(aImg); // CANCEL pressed
	}

	private void coosInputFormat () {
		MessageBox tmpMB = new MessageBox((String)lr.get(312,"Error"), (String)lr.get(4111,"Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM"), MessageBox.OKB);
		tmpMB.exec();

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

	public GCPoint(double lat, double lon){
		this.latDec = lat;
		this.lonDec = lon;
		this.utmValid = false;
	}
}