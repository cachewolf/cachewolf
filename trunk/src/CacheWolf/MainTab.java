package CacheWolf;

import ewe.ui.*;
import ewe.fx.*;
import ewe.util.*;
import ewe.sys.*;

/**
*	This class creates the tabbed panel and sets the tabs to the respective
*	other panels. Important is to have a look at the event handler!<br>
*	Class ID = 1200
*	@see MainForm
*	@see MainMenu
*/
public class MainTab extends mTabbedPanel {
	DescriptionPanel descP= new DescriptionPanel();
	HintLogPanel hintLP = new HintLogPanel();
	TablePanel tbP;
	Vector cDB;
	DetailsPanel detP = new DetailsPanel();
	CalcPanel calcP = new CalcPanel();
	Preferences myPreferences = new Preferences();
	GotoPanel gotoP; 
	RadarPanel radarP = new RadarPanel();
	ImagePanel imageP;

	String lastselected = new String();
	CacheHolder ch = new CacheHolder();
	Locale l = Vm.getLocale();
	LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
	
	public MainTab(Vector cacheDB, Preferences pref){
		if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
			Vm.setSIP(Vm.SIP_LEAVE_BUTTON);
		}
		myPreferences = pref;
		cDB = cacheDB;
		ch.wayPoint = "null";
		Card c = this.addCard(tbP = new TablePanel(cDB, myPreferences), (String)lr.get(1200,"List"), null);
		
		c = this.addCard(detP, (String)lr.get(1201,"Details"), null);
		c.iconize(new Image("details.gif"),true);
		
		c = this.addCard(descP, (String)lr.get(1202,"Description"), null);
		c.iconize(new Image("descr.gif"),true);
		
		c = this.addCard(new ScrollBarPanel(imageP = new ImagePanel(myPreferences)), (String)lr.get(1203,"Images"), null);
		c.iconize(new Image("images.gif"),true);
		
		c = this.addCard(hintLP, (String)lr.get(1204,"Hints & Logs"), null);
		c.iconize(new Image("more.gif"),true);

		c = this.addCard(calcP, (String)lr.get(1206,"Calc"), null);
		c.iconize(new Image("ewe/HandHeld.bmp"),true);
		
		c = this.addCard(gotoP = new GotoPanel(myPreferences, this, detP, cDB), "Goto", null);
		//c.iconize(new Image("ewe/HandHeld.bmp"),true);
		
		c = this.addCard(new SolverPanel(myPreferences), (String)lr.get(1205,"Solver"), null);
		c.iconize(new Image("solver.gif"),true);
		
		c = this.addCard(radarP, "Radar", null);
		radarP.setMainTab(this);
		c.iconize(new Image("radar.gif"),true);
		
	}
	
	public TablePanel getTablePanel(){
		return tbP;
	}
	
	public void selectAndActive(int rownum){
		tbP.selectAndActive(rownum);
		this.selectAndExpand(0);
	}
	
	public void updateBearDist(){
		tbP.myPreferences = myPreferences;
		TablePanel.updateBearingDistance(cDB,myPreferences);
		tbP.refreshTable();
	}
	
	public void gotoPoint(String LatLon) {
		gotoP.setDestination(LatLon);
	}
	
	public void onEvent(Event ev)
		{
		  ////Vm.debug(ev.toString());
		  if(ev instanceof MultiPanelEvent){
			  //if(this.getSelectedItem() == 0){
				  //Vm.debug(Convert.toString(cDB.size()));
				  Vm.debug("Panel 0");
				  if(detP.dirty_new == true || detP.dirty_delete == true) {
					  tbP.refreshTable();
					  Vm.debug("Panel 0.1");
					  detP.dirty_new = false;
					  detP.dirty_delete = false;
					  detP.dirty_status = false;
				  }
				  if(detP.dirty_status == true){
					  Vm.debug("Panel 0.2");
					  ch = new CacheHolder();
					  ch = (CacheHolder)cDB.get(tbP.getSelectedCache());
					  ch.CacheStatus = detP.wayStatus.getText();
					  if(ch.CacheStatus.equals((String)lr.get(318,"Found"))){
						  ch.is_found = true;
					  } else {
						  ch.is_found = false;
					  }
					  ch.wayPoint = detP.wayPoint.getText();
					  ch.CacheName = detP.wayName.getText();
					  ch.LatLon = detP.wayLoc.getText();
					  ch.DateHidden = detP.wayHidden.getText();
					  ch.CacheOwner = detP.wayOwner.getText();
					  ch.CacheNotes = detP.wayNotes.getText();
					  ch.type = detP.transSelect(detP.wayType.getInt());
					  cDB.set(tbP.getSelectedCache(), ch);
					  detP.dirty_status = false;
					  tbP.refreshTable();
					  ////Vm.debug("New status updated!");
				  }
			  //}
			  if(this.getSelectedItem() != 0){
				  try{
					  ch = new CacheHolder();
					  ch = (CacheHolder)cDB.get(tbP.getSelectedCache());
					  if(ch.wayPoint.equals(lastselected) == false){
						  CacheReaderWriter crw = new CacheReaderWriter();
						  //OperationTimer opt = new OperationTimer();
						  //opt.start("Reading: ");
						  crw.readCache(ch, myPreferences.mydatadir);
						  //opt.end();
						  ////Vm.debug(opt.toString());
						  lastselected = ch.wayPoint;
					  }
				  } catch(Exception e){
					//Vm.debug("Error loading: "+ch.wayPoint);
				  }
			  }
			  if(this.getSelectedItem() == 1){ // DetailsPanel
				if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
					Vm.setSIP(Vm.SIP_LEAVE_BUTTON);
				}
				detP.setDetails(ch, cDB, this,myPreferences);
			  }
			  if(this.getSelectedItem() == 2) { // Description Panel
				  if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
						Vm.setSIP(0);
					}
				  descP.setText(ch);
				  if(detP.dirty_new == true || detP.dirty_delete == true) {
					  tbP.refreshTable();
					  detP.dirty_new = false;
					  detP.dirty_delete = false;
				  }
			  }
			  if(this.getSelectedItem() == 3) { // Picture Panel
				  imageP.setImages(ch);
				  if(detP.dirty_new == true || detP.dirty_delete == true) {
					  tbP.refreshTable();
					  detP.dirty_new = false;
					  detP.dirty_delete = false;
				  }
			  }
			  if(this.getSelectedItem() == 4) { // Log Hint Panel
				  hintLP.setText(ch);
				  if(detP.dirty_new == true || detP.dirty_delete == true) {
					  tbP.refreshTable();
					  detP.dirty_new = false;
					  detP.dirty_delete = false;
				  }
			  }
			  if(this.getSelectedItem() == 5){ // CalcPanel
					if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
						Vm.setSIP(Vm.SIP_LEAVE_BUTTON);
					}
					calcP.setFields(ch, cDB, this, detP, myPreferences);
					calcP.activateFields(CWPoint.DMM);
				  }
			  
			  if(this.getSelectedItem() == 6){ // GotoPanel
					if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
						Vm.setSIP(Vm.SIP_LEAVE_BUTTON);
					}
				  }


			  if(this.getSelectedItem() == 7) { // Solver Panel
				  if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
					Vm.setSIP(Vm.SIP_LEAVE_BUTTON);
				}
				  if(detP.dirty_new == true || detP.dirty_delete == true) {
					  tbP.refreshTable();
					  detP.dirty_new = false;
					  detP.dirty_delete = false;
				  }
			  }
			  if(this.getSelectedItem() == 8) { // Cache Radar Panel
				  if(detP.dirty_new == true || detP.dirty_delete == true) {
					  //tbP.refreshTable();
					  detP.dirty_new = false;
					  detP.dirty_delete = false;
				  }
				  radarP.setParam(myPreferences, cDB, ch.wayPoint);
				  radarP.drawThePanel();
			  }
		  }
		  
		  super.onEvent(ev); //Make sure you call this.
		}
		
		/*
		public void resizeTo(int w, int h){
			//super.resizeTo(w,h);
			////Vm.debug(Convert.toString(w));
		}
		*/
}
