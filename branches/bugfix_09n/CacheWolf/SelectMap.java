package CacheWolf;

import ewe.util.*;
import ewe.io.*;
import ewe.filechooser.*;
import ewe.sys.*;
import ewe.ui.*;

/**
*	This class displays a user interface to select maps that should be or are already calibrated
*	class id = 4100
*/
class SelectMap extends Form{
	ScrollBarPanel top;
	ScrollBarPanel bot;
	mButton mBCancel, mBLoad;
	Locale l = Vm.getLocale();
	LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
	String mapsPath = new String();
	mList nonCMaps = new mList(3,1,false);
	mList CMaps = new mList(3,1,false);
	String dateien[];
	String selectedMap = new String();
	public boolean worldfileexists = false;
	
	public SelectMap(){
		mapsPath = Global.getPref().getMapManuallySavePath(false)+"/"; //  File.getProgramDirectory() + "/maps/";
		top = new ScrollBarPanel(CMaps);
		bot = new ScrollBarPanel(nonCMaps);
		this.title = (String)lr.get(4101,"Maps");
		this.addLast(new mLabel((String)lr.get(4102,"Calibrated Maps")), CellConstants.STRETCH, CellConstants.FILL);
		this.addLast(top, CellConstants.STRETCH, CellConstants.FILL);
		this.addLast(new mLabel((String)lr.get(4103,"Non Calibrated Maps")), CellConstants.STRETCH, CellConstants.FILL);
		this.addLast(bot, CellConstants.STRETCH, CellConstants.FILL);
		this.addNext(mBCancel = new mButton((String)lr.get(4104,"Cancel")),CellConstants.STRETCH, CellConstants.FILL);
		this.addLast(mBLoad = new mButton((String)lr.get(4105,"Open")),CellConstants.STRETCH, CellConstants.FILL);
		InfoBox inf = new InfoBox("Info", (String)lr.get(4109,"Loading maps...")); 
		inf.show();
		try{
			File files = new File(mapsPath);
			File checkWFL;
			String rawFileName = new String();
			dateien = files.listMultiple("*.png,*.jpg,*.gif,*.bmp", File.LIST_FILES_ONLY);
			for(int i = 0; i < dateien.length;i++){
				rawFileName = dateien[i].substring(0, dateien[i].lastIndexOf("."));
				checkWFL = new File(mapsPath + rawFileName + ".wfl");
				
				if(checkWFL.exists()){
					CMaps.addItem(rawFileName);
				} else {
					nonCMaps.addItem(rawFileName);
				}
			}
		}catch(Exception ex){
			//Vm.debug("Problem retrieveing map files");
		}
		inf.close(0);
	}
	
	public void onEvent(Event ev){
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if(ev.target == mBCancel){
				this.close(0);
			}
			if(ev.target == mBLoad){
				this.close(0);
			}
		}
		if(ev instanceof ListEvent && ev.type == ListEvent.SELECTED){
			if(ev.target == nonCMaps){
				selectedMap = nonCMaps.getText();
				CMaps.deleteSelection();
				worldfileexists = false;
			}
			if(ev.target == CMaps){
				selectedMap = CMaps.getText();
				nonCMaps.deleteSelection();
				worldfileexists = true;
			}
		}
		super.onEvent(ev);
	}
	
	public String getSelectedMap(){
		return selectedMap;
	}
}