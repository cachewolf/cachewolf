package cachewolf.navi;

import cachewolf.Global;
import cachewolf.InfoBox;
import cachewolf.MyLocale;
import eve.io.*;
import eve.sys.*;
import eve.ui.*;
import eve.ui.event.ControlEvent;
import eve.ui.event.ListEvent;

/**
*	This class displays a user interface to select maps that should be or are already calibrated
*	class id = 4100
*/
public class SelectMap extends Form{
	ScrollBarPanel top;
	ScrollBarPanel bot;
	Button mBCancel, mBLoad;
	String mapsPath = "";
	eve.ui.List nonCMaps = new eve.ui.List(3,1,false);
	eve.ui.List CMaps = new eve.ui.List(3,1,false);
	String dateien[];
	String selectedMap = "";
	public boolean worldfileexists = false;
	
	public SelectMap(){
		mapsPath = Global.getPref().getMapManuallySavePath(false)+"/"; //  File.getProgramDirectory() + "/maps/";
		top = new cachewolf.MyScrollBarPanel(CMaps);
		bot = new cachewolf.MyScrollBarPanel(nonCMaps);
		this.title = MyLocale.getMsg(4101,"Maps");
		this.addLast(new Label(MyLocale.getMsg(4102,"Calibrated Maps")), CellConstants.STRETCH, CellConstants.FILL);
		this.addLast(top, CellConstants.STRETCH, CellConstants.FILL);
		this.addLast(new Label(MyLocale.getMsg(4103,"Non Calibrated Maps")), CellConstants.STRETCH, CellConstants.FILL);
		this.addLast(bot, CellConstants.STRETCH, CellConstants.FILL);
		this.addNext(mBCancel = new Button(MyLocale.getMsg(4104,"Cancel")),CellConstants.STRETCH, CellConstants.FILL);
		this.addLast(mBLoad = new Button(MyLocale.getMsg(4105,"Open")),CellConstants.STRETCH, CellConstants.FILL);
		InfoBox inf = new InfoBox("Info", MyLocale.getMsg(4109,"Loading maps...")); 
		inf.show();
		try{
			File files = new File(mapsPath);
			File checkWFL;
			String rawFileName;
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