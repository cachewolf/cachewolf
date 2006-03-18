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
	Locale l = Vm.getLocale();
	LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
	String mapsPath = new String();
	String thisMap = new String();
	public String selectedMap = new String();
	CellPanel infPanel;
	mLabel infLabel = new mLabel("                          ");
	Vector GCPs = new Vector();
	double[] affine = {0,0,0,0,0,0};
	double bottomlon, bottomlat = 0;
	mButton infButton;
	ScrollBarPanel scp;
	AniImage mapImg;
	int imageWidth, imageHeight = 0;
	
	/**
	*	This constructor should be used when importing maps
	*/
	public Map(Preferences pref){
		this.pref = pref;
		mapsPath = File.getProgramDirectory() + "/maps/";
	}
	
	/**
	*	When a user clicks on the map and more than three ground control points exist
	*	then the calculated coordinate based on the affine transformation is displayed in the
	*	info panel below the map.
	*	It helps to identify how good the georeferencing works based on the set GCPs.
	*/
	public void updatePosition(int x, int y){
		if(GCPs.size()>=3  || (affine[4] > 0 && affine[5] > 0)){
			double x_ = 0;
			double y_ = 0;
			x_ = affine[0]*x + affine[2]*y + affine[4];
			y_ = affine[1]*x + affine[3]*y + affine[5];
			CWPoint p = new CWPoint(x_ , y_);
			infLabel.setText("--> " + p.getLatDeg(CWPoint.DMS) + " " +p.getLatMin(CWPoint.DMM) + " / " + p.getLonDeg(CWPoint.DMS) + " " + p.getLonMin(CWPoint.DMM));
		}
	}
	
	/**
	*	This is the correct constructor for georeferencing maps.
	*/
	public Map(Preferences pref, String mapToLoad, boolean worldfileexists){
		this.pref = pref;
		this.title = (String)lr.get(4106,"Calibrate map:") + " " + mapToLoad;
		this.resizable = true;
		this.moveable = true;
		//this.windowFlagsToSet = Window.FLAG_MAXIMIZE;
		this.setPreferredSize(pref.myAppWidth, pref.myAppHeight);
		thisMap = mapToLoad;
		mapsPath = File.getProgramDirectory() + "/maps/";
		try{
			FileReader in = new FileReader(mapsPath + thisMap + ".wfl");
			String linetest = new String();
			for(int i = 0; i<6;i++){
				linetest = in.readLine();
				linetest = linetest.replace(',','.');
				affine[i] = Convert.toDouble(linetest);
			}
		}catch(Exception ex){
			Vm.debug("Cannot load world file!");
		}
		mapInteractivePanel pane = new mapInteractivePanel(this);
		scp = new ScrollBarPanel(pane);
		Image img = new Image(mapsPath + thisMap + ".png");
		PixelBuffer pB = new PixelBuffer(img);
		//pB = pB.scale((int)(pref.myAppWidth*0.98),(int)(pref.myAppHeight*0.98));
		mapImg = new AniImage(pB.toDrawableImage());
		pane.addImage(mapImg);
		scp.setPreferredSize(mapImg.getWidth(),mapImg.getHeight());
		imageWidth = mapImg.getWidth();
		imageHeight = mapImg.getHeight();
		this.addLast(scp.getScrollablePanel(), this.STRETCH, this.FILL);
		infPanel = new CellPanel();
		infPanel.addNext(infLabel,this.STRETCH, this.FILL);
		infButton = new mButton((String)lr.get(4107,"Done!"));
		infPanel.addLast(infButton,this.DONTSTRETCH, this.FILL);
		this.addLast(infPanel, this.DONTSTRETCH, this.FILL);
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
		affine[0] = beta.matrix[1][0];
		affine[2] = beta.matrix[2][0];
		affine[4] = beta.matrix[0][0];
		
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
		affine[1] = beta.matrix[1][0];
		affine[3] = beta.matrix[2][0];
		affine[5] = beta.matrix[0][0];
		double x_ = 0;
		double y_ = 0;
		x_ = affine[0]*imageWidth+ affine[2]*imageHeight + affine[4];
		y_ = affine[1]*imageWidth + affine[3]*imageHeight + affine[5];
		CWPoint p = new CWPoint(x_ , y_);
		bottomlon = p.lonDec;
		bottomlat = p.latDec;
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
	public boolean importMap(){
		Extractor ext;
		String rawFileName = new String();
		FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, File.getProgramDirectory());
		fc.addMask("*.png");
		fc.setTitle((String)lr.get(4100,"Select Directory:"));
		if(fc.execute() != fc.IDCANCEL){
			File inDir = fc.getChosenFile();
			File dir = new File(mapsPath);
			File mapFile;
			if (!dir.exists()) {
				dir.createDir();
			}
			try{
				//User selected a map, but maybe there are more png(s)
				//copy all of them!
				//at the same time try to find associated .map files!
				//These are georeference files targeted for OziExplorer.
				//So lets check if we have more than 1 png file:
				String[] files;
				String line = new String();
				InputStream in;
				OutputStream out;
				FileReader inMap;
				byte[] buf;
				int len;
				String[] parts;
				files = inDir.list("*.png", File.LIST_FILES_ONLY);
				InfoBox inf = new InfoBox("Info", (String)lr.get(4109,"Loading maps...")); 
				inf.show();
				Vm.showWait(true);
				for(int i = 0; i<files.length;i++){
					inf.setInfo((String)lr.get(4110,"Loading:")+ " " + files[i]);
					//Copy the file
					//Vm.debug("Copy: " + inDir.getFullPath() + "/" +files[i]);
					//Vm.debug("to: " + mapsPath + files[i]);
					in = new FileInputStream(inDir.getFullPath() + "/" +files[i]);
					out = new FileOutputStream(mapsPath + files[i]);
					buf = new byte[1024];
					while ((len = in.read(buf)) > 0) {
					    out.write(buf, 0, len);
					}
					in.close();
					out.close();
					
					//Check for a .map file
					ext = new Extractor(files[i], "", ".", 0, true);
					rawFileName = ext.findNext();
					mapFile = new File(inDir.getFullPath() + "/" + rawFileName + ".map");
					if(mapFile.exists()){
						GCPoint gcp1 = new GCPoint();
						GCPoint gcp2 = new GCPoint();
						GCPoint gcp3 = new GCPoint();
						GCPoint gcp4 = new GCPoint();
						GCPoint gcpG = new GCPoint();
						//Vm.debug("Found file: " + inDir.getFullPath() + "/" + rawFileName + ".map");
						inMap = new FileReader(inDir.getFullPath() + "/" + rawFileName + ".map");
						while((line = inMap.readLine()) != null){
							if(line.equals("MMPNUM,4")){
								
								line = inMap.readLine();
								parts = mString.split(line, ',');
								gcp1.bitMapX = Convert.toInt(parts[2]);
								gcp1.bitMapY = Convert.toInt(parts[3]);
								
								line = inMap.readLine();
								parts = mString.split(line, ',');
								gcp2.bitMapX = Convert.toInt(parts[2]);
								gcp2.bitMapY = Convert.toInt(parts[3]);
								
								line = inMap.readLine();
								parts = mString.split(line, ',');
								gcp3.bitMapX = Convert.toInt(parts[2]);
								gcp3.bitMapY = Convert.toInt(parts[3]);
								imageWidth = gcp3.bitMapX;
								imageHeight = gcp3.bitMapY;
								
								line = inMap.readLine();
								parts = mString.split(line, ',');
								gcp4.bitMapX = Convert.toInt(parts[2]);
								gcp4.bitMapY = Convert.toInt(parts[3]);
								
								line = inMap.readLine();
								parts = mString.split(line, ',');
								gcpG = new GCPoint(Convert.toDouble(parts[2]), Convert.toDouble(parts[3]));
								gcpG.bitMapX = gcp1.bitMapX;
								gcpG.bitMapY = gcp1.bitMapY;
								addGCP(gcpG);
								
								line = inMap.readLine();
								parts = mString.split(line, ',');
								gcpG = new GCPoint(Convert.toDouble(parts[2]), Convert.toDouble(parts[3]));
								gcpG.bitMapX = gcp2.bitMapX;
								gcpG.bitMapY = gcp2.bitMapY;
								addGCP(gcpG);
								
								line = inMap.readLine();
								parts = mString.split(line, ',');
								gcpG = new GCPoint(Convert.toDouble(parts[2]), Convert.toDouble(parts[3]));
								gcpG.bitMapX = gcp3.bitMapX;
								gcpG.bitMapY = gcp3.bitMapY;
								addGCP(gcpG);

								line = inMap.readLine();
								parts = mString.split(line, ',');
								gcpG = new GCPoint(Convert.toDouble(parts[2]), Convert.toDouble(parts[3]));
								gcpG.bitMapX = gcp4.bitMapX;
								gcpG.bitMapY = gcp4.bitMapY;
								addGCP(gcpG);
								
								evalGCP();
								//Vm.debug("Saving .map file to: " + mapsPath + "/" + rawFileName + ".wfl");
								saveWFL(mapsPath + "/" + rawFileName + ".wfl");
							}
						}
						inMap.close();
					}
				}
				inf.close(0);
				Vm.showWait(false);
				return true;
			}catch(Exception ex){
				Vm.debug("Error:" + ex.toString());
			}
		}
		return false;
	}
	
	/**
	*	Method to save a world file (.wfl)
	*/
	private void saveWFL(String saveTo){
		try{
			PrintWriter outp =  new PrintWriter(new BufferedWriter(new FileWriter(saveTo)));
			outp.print(Convert.toString(affine[0])+"\n");
			outp.print(Convert.toString(affine[1])+"\n");
			outp.print(Convert.toString(affine[2])+"\n");
			outp.print(Convert.toString(affine[3])+"\n");
			outp.print(Convert.toString(affine[4])+"\n");
			outp.print(Convert.toString(affine[5])+"\n");
			outp.print(Convert.toString(bottomlat)+"\n");
			outp.print(Convert.toString(bottomlon)+"\n");
			outp.close();
		}catch(Exception ex){
			Vm.debug("Error writing wfl file!");
		}
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
				String saveTo = new String();
				saveTo = mapsPath + thisMap + ".wfl";
				saveWFL(saveTo);
				close(0);
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
		InfoBox inf = new InfoBox((String)lr.get(4108,"Coordinates:"), (String)lr.get(4108,"Coordinates:"), InfoBox.INPUT);
		inf.execute();
		String txt = inf.feedback.getText();
		Regex rex = new Regex("(N|S).*?([0-9]{1,2}).*?([0-9]{1,3})(,|.)([0-9]{1,3}).*?(E|W).*?([0-9]{1,2}).*?([0-9]{1,3})(,|.)([0-9]{1,3})");
		rex.search(txt);
		if(rex.didMatch()){
			double lat = Convert.toDouble(rex.stringMatched(2)) + Convert.toDouble(rex.stringMatched(3))/60 + Convert.toDouble(rex.stringMatched(5))/60000;
			double lon = Convert.toDouble(rex.stringMatched(7)) + Convert.toDouble(rex.stringMatched(8))/60 + Convert.toDouble(rex.stringMatched(10))/60000;
			if(rex.stringMatched(1).equals("S") || rex.stringMatched(1).equals("s")) lat = lat * -1;
			if(rex.stringMatched(6).equals("W") || rex.stringMatched(6).equals("w")) lon = lon * -1;	
			GCPoint gcp = new GCPoint(lat, lon);
			gcp.bitMapX = pos.x;
			gcp.bitMapY = pos.y;
			f.addGCP(gcp);
		}
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
	
	public GCPoint(double lat, double lon){
		this.latDec = lat;
		this.lonDec = lon;
		this.utmValid = false;
	}
}