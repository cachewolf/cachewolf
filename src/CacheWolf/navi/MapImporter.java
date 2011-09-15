    /*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
    See http://developer.berlios.de/projects/cachewolf/
    for more information.
    Contact: 	bilbowolf@users.berlios.de
    			kalli@users.berlios.de

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

import CacheWolf.CWPoint;
import CacheWolf.Common;
import CacheWolf.CoordsScreen;
import CacheWolf.Global;
import CacheWolf.InfoBox;
import CacheWolf.MyLocale;
import CacheWolf.Preferences;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.fx.Color;
import ewe.fx.Graphics;
import ewe.fx.Image;
import ewe.fx.ImageInfo;
import ewe.fx.PixelBuffer;
import ewe.fx.Point;
import ewe.fx.mImage;
import ewe.graphics.AniImage;
import ewe.graphics.InteractivePanel;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.io.FileInputStream;
import ewe.io.FileNotFoundException;
import ewe.io.FileOutputStream;
import ewe.io.FileReader;
import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.io.OutputStream;
import ewe.sys.Convert;
import ewe.sys.Vm;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.ui.ScrollBarPanel;
import ewe.ui.mButton;
import ewe.ui.mLabel;
import ewe.util.ByteArray;
import ewe.util.Vector;
import ewe.util.mString;

/**
 *	This class is for importing and manually georeferencing maps
 *	This class id=4100 for cachewolf-languages
 */
public class MapImporter extends Form {
	Preferences pref;
	String mapsPath = "";
	String thisMap = "";
	public String selectedMap = "";
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
	public MapImporter(Preferences pref){
		this.pref = pref;
		mapsPath = pref.getMapManuallySavePath(true)+"/"; //File.getProgramDirectory() + "/maps/";
	}

	/**
	 *	When a user clicks on the map and more than three ground control points exist
	 *	then the calculated coordinate based on the affine transformation is displayed in the
	 *	info panel below the map.
	 *	It helps to identify how good the georeferencing works based on the set GCPs.
	 */
	public void updatePosition(int px, int py){
		if(GCPs.size()>=3  || (wfl.topleft.isValid())){
			CWPoint p = wfl.calcLatLon(px,py);
			infLabel.setText("--> " + p.getLatDeg(TransformCoordinates.DMS) + " " +p.getLatMin(TransformCoordinates.DMM) + " / " + p.getLonDeg(TransformCoordinates.DMS) + " " + p.getLonMin(TransformCoordinates.DMM));
		}
	}

	/**
	 *	This is the correct constructor for georeferencing maps.
	 */
	public MapImporter(Preferences pref, String mapToLoad, boolean worldfileexists){
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
		}catch (IOException ex) { // is thrown if lat/lon out of range
			MessageBox tmpMB=new MessageBox(MyLocale.getMsg(312, "Error"), ex.getMessage(), FormBase.OKB);
			tmpMB.execute();
			pref.log("Cannot load world file!",ex);
		}
		mapInteractivePanel pane = new mapInteractivePanel(this);
		scp = new CacheWolf.MyScrollBarPanel(pane);
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
			wfl.evalGCP(GCPs, imageWidth, imageHeight);
		}
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
		String rawFileName = "";
		FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, Global.getPref().absoluteBaseDir);
		fc.addMask("*.png,*.gif,*.bmp,*.jpg");
		fc.setTitle(MyLocale.getMsg(4100,"Select Directory:"));
		int tmp = fc.execute() ; 
		if(tmp != FormBase.IDYES) return FormBase.IDCANCEL;
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
		String line = "";
		InputStream in = null;
		OutputStream out = null;
		FileReader inMap;
		byte[] buf;
		int len;
		String[] parts;
		String [] files = inDir.listMultiple("*.png,*.jpg,*.gif,*.bmp", FileBase.LIST_FILES_ONLY);

		String currfile = null;
		String curInFullPath;
		String curOutFullPath;
		int num = files.length;
		for(int i =  num -1 ; i >= 0;i--){
			currfile = files[i];
			inf.setInfo(MyLocale.getMsg(4110,"Loading: ")+ "\n" + currfile + "\n("+(num-i)+"/"+num+")");
			//Copy the file
			curInFullPath = inDir.getFullPath() + "/" +currfile;
			curOutFullPath = mapsPath + currfile;
			boolean imageerror = false;
			try {
				in = new FileInputStream(curInFullPath);
				buf = new byte[1024*10];
				ByteArray header = new ByteArray(buf);
				out = null; // May be unequal to null because of prior loop
				while ((len = in.read(buf)) > 0) {
					if (out==null) {
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
				inf.addWarning("\n"+MyLocale.getMsg(4112, "IO-Error while copying image from: ") + curInFullPath + MyLocale.getMsg(4113, " to: ") + curOutFullPath + MyLocale.getMsg(4114, " error: ") + ex.getMessage());
			} catch (IllegalArgumentException e) { // thrown from Image.getImageInfo when it could not interprete the header (e.g. bmp with 32 bits per pixel)
				imageerror = true;
				inf.addWarning("\n"+MyLocale.getMsg(4115, "Error: could not decode image: ") + curInFullPath + MyLocale.getMsg(4116, " - image not copied"));
			} finally {
				try {
					if (in != null) in.close();
					if (out  != null) out.close(); 
				} catch (Throwable e) {
					// Global.getPref().log("Ignored Exception", e, true);
				}
			}
			//Check for a .map file
			rawFileName = currfile.substring(0, currfile.lastIndexOf('.'));
			mapFile = new File(inDir.getFullPath() + "/" + rawFileName + ".map");
			if(!imageerror && mapFile.exists()){
				GCPoint gcp1 = new GCPoint();
				GCPoint gcp2 = new GCPoint();
				GCPoint gcp3 = new GCPoint();
				GCPoint gcp4 = new GCPoint();
				GCPoint gcpG = new GCPoint();
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
							if(MyLocale.getDigSeparator().equals(",")) {
								parts[3]= parts[3].replace('.', ',');
								parts[2]= parts[2].replace('.', ',');
							}
							gcpG = new GCPoint(Convert.toDouble(parts[3]), Convert.toDouble(parts[2]));
							gcpG.bitMapX = gcp1.bitMapX;
							gcpG.bitMapY = gcp1.bitMapY;
							addGCP(gcpG);

							line = inMap.readLine();
							parts = mString.split(line, ',');
							if(MyLocale.getDigSeparator().equals(",")) {
								parts[3]= parts[3].replace('.', ',');
								parts[2]= parts[2].replace('.', ',');
							}
							gcpG = new GCPoint(Convert.toDouble(parts[3]), Convert.toDouble(parts[2]));
							gcpG.bitMapX = gcp2.bitMapX;
							gcpG.bitMapY = gcp2.bitMapY;
							addGCP(gcpG);

							line = inMap.readLine();
							parts = mString.split(line, ',');
							if(MyLocale.getDigSeparator().equals(",")) {
								parts[3]= parts[3].replace('.', ',');
								parts[2]= parts[2].replace('.', ',');
							}
							gcpG = new GCPoint(Convert.toDouble(parts[3]), Convert.toDouble(parts[2]));
							gcpG.bitMapX = gcp3.bitMapX;
							gcpG.bitMapY = gcp3.bitMapY;
							addGCP(gcpG);

							line = inMap.readLine();
							parts = mString.split(line, ',');
							if(MyLocale.getDigSeparator().equals(",")) {
								parts[3]= parts[3].replace('.', ',');
								parts[2]= parts[2].replace('.', ',');
							}
							gcpG = new GCPoint(Convert.toDouble(parts[3]), Convert.toDouble(parts[2]));
							gcpG.bitMapX = gcp4.bitMapX;
							gcpG.bitMapY = gcp4.bitMapY;
							addGCP(gcpG);
							/* already read from image file itself
							// get dimensions of image
							while ( (line = inMap.readLine()) != null){
								if (line.startsWith("IWH")){
									parts = mString.split(line, ',');
									imageWidth = Convert.toInt(parts[2]);
									imageHeight = Convert.toInt(parts[3]);
								}
							}
							 */
							wfl.evalGCP(GCPs, imageWidth, imageHeight);
							wfl.saveWFL(mapsPath, rawFileName);
							GCPs.clear();
						} // if

					} // while
					inMap.close();
				} catch(IllegalArgumentException ex){ // is thrown from Convert.toDouble and saveWFL if affine[0-5]==0 NumberFormatException is a subclass of IllegalArgumentExepction
					inf.addWarning("\n"+MyLocale.getMsg(4117, "Error while importing .map-file: ")+ex.getMessage());
				} catch(IOException ex){
					inf.addWarning("\n"+MyLocale.getMsg(4118, "IO-Error while reading or writing calibration file")+"\n" + ex.getMessage());
				} 
			} else { // if map file.exists
				if (!imageerror) inf.addWarning("\n"+MyLocale.getMsg(4119, "No calibration file found for: ") + currfile + " - you can calibrate it manually");
			}
		} // for file
		Vm.showWait(this, false);
		inf.addText("\n"+MyLocale.getMsg(4120, "done."));
		inf.addOkButton();
		//inf.addOkButton(); doesn't work
		if(Global.mainTab.mm != null) Global.mainTab.mm.mapsloaded = false; 
		return FormBase.IDOK;
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
						MessageBox tmpMB = new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(321, "Error writing file ") + e.getMessage()+MyLocale.getMsg(324, " - retry?"), FormBase.YESB | FormBase.NOB);
						if (tmpMB.execute() == FormBase.IDYES) retry = true;
					}catch (IllegalArgumentException e) {
						MessageBox tmpMB = new MessageBox(MyLocale.getMsg(144, "Warning"), MyLocale.getMsg(325, "Map not calibrated")+MyLocale.getMsg(324, " - retry?"), FormBase.YESB | FormBase.NOB);
						if (tmpMB.execute() == FormBase.IDYES) { retry = true; break; }
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
	MapImporter f;
	public mapInteractivePanel(MapImporter f){
		this.f = f;
	}

	/**
	 *	Event handler to catch clicks on the map
	 */
	public void imageClicked(AniImage which, Point pos){
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
		if (cooS.execute()==FormBase.IDOK) {
			GCPoint gcp = new GCPoint(cooS.getCoords());
			gcp.bitMapX = pos.x;
			gcp.bitMapY = pos.y;
			f.addGCP(gcp); 
		} else this.removeImage(aImg); // CANCEL pressed
	}
}
