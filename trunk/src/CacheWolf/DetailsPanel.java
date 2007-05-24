package CacheWolf;

import ewe.ui.*;
import ewe.fx.*;
import ewe.util.*;
import ewe.sys.*;
import ewe.sys.Long;

/**
*	Class to create the panel to show the cache details.<br>
*	Also allows for creation of a custom waypoint.<br>
*
*   
*/
public class DetailsPanel extends CellPanel{
	mInput inpWaypoint = new mInput();
	mInput inpName = new mInput();
	mButton btnWayLoc = new mButton();
	mInput inpHidden = new mInput();
	mInput inpOwner = new mInput();
	mButton btnDelete,btnCenter, btnAddDateTime;
	mChoice chcType = new mChoice(new String[]{"Custom", "Traditional", "Multi", "Virtual", "Letterbox", "Event", "Mega Event", "Mystery", "Webcam", "Locationless", "CITO", "Earthcache", "Parking", "Stage", "Question", "Final","Trailhead","Reference"},0);
	mChoice chcSize = new mChoice(new String[]{"", "Micro", "Small", "Regular", "Large","Other","Very Large","None"},0);
	mComboBox chcStatus = new mComboBox(new String[]{"", MyLocale.getMsg(313,"Flag 1"), MyLocale.getMsg(314,"Flag 2"), MyLocale.getMsg(315,"Flag 3"), MyLocale.getMsg(316,"Flag 4"), MyLocale.getMsg(317,"Search"), MyLocale.getMsg(318,"Found"), MyLocale.getMsg(319,"Not Found"), MyLocale.getMsg(320,"Owner")},0);
	mButton btnNewWpt, btnShowBug, btnShowMap, btnGoto, btnAddPicture, btnBlack, btnNotes, btnSave, btnCancel;
	mButton btnFoundDate,btnHiddenDate;
	Vector cacheDB;
	CacheHolderDetail thisCache;
	CellPanel pnlTools = new CellPanel(); 
	
	private boolean dirty_notes = false;
	private boolean dirty_details = false;
	private boolean blackStatus = false;
	Preferences pref; // Test
	Profile profile;
	mImage imgBlack;
	mImage imgBlackNo;
	mImage imgShowBug, imgShowBugNo,imgNewWpt,imgGoto;
	mImage imgShowMaps,imgAddImages;
	
	public DetailsPanel(){
		pref = Global.getPref();
		profile=Global.getProfile();
		cacheDB = profile.cacheDB;
		////////////////////
		// Tools
		////////////////////
		// Button 1: New Waypoint
		pnlTools.addNext(btnNewWpt = new mButton(imgNewWpt=new mImage("newwpt.png"))); 
		btnNewWpt.setToolTip(MyLocale.getMsg(311,"Create Waypoint"));
		PenEvent.wantPenMoved(btnNewWpt,PenEvent.WANT_PEN_MOVED_ONOFF,true);
		imgNewWpt.transparentColor=new Color(255,0,0);
		// Button 2: Goto
		pnlTools.addNext(btnGoto = new mButton(imgGoto=new mImage("goto.png")));//Goto.gif funzt manchmal nicht
		imgGoto.transparentColor=Color.White;
		btnGoto.setToolTip(MyLocale.getMsg(345,"Goto these coordinates"));
		// Button 3: Travelbugs
		imgShowBug = new mImage("bug.gif");
		imgShowBugNo = new mImage("bug_no.gif");
		pnlTools.addNext(btnShowBug = new mButton(imgShowBugNo)); 
		btnShowBug.modify(Control.Disabled,0);
		btnShowBug.setToolTip(MyLocale.getMsg(346,"Show travelbugs"));
		// Button 4: Show Map
		pnlTools.addNext(btnShowMap = new mButton(imgShowMaps = new mImage("globe_small.gif"))); 
		btnShowMap.setToolTip(MyLocale.getMsg(347,"Show map"));
		// Button 5: Add images
		pnlTools.addNext(btnAddPicture = new mButton(imgAddImages = new mImage("images.gif"))); 
		btnAddPicture.setToolTip(MyLocale.getMsg(348,"Add user pictures"));
		// Button 6: Toggle blacklist status
		imgBlackNo = new mImage("no_black.png"); imgBlackNo.transparentColor=Color.Black;
		imgBlack = new mImage("is_black.png"); imgBlack.transparentColor=Color.White;
		pnlTools.addNext(btnBlack=new mButton(imgBlackNo)); 
		btnBlack.setToolTip(MyLocale.getMsg(349,"Toggle Blacklist status"));
		// Button 7: Date/time stamp
		pnlTools.addLast(btnAddDateTime = new mButton(new mImage("date_time.png"))); 
		btnAddDateTime.setToolTip(MyLocale.getMsg(350,"Add timestamp to notes"));
		//showMap.modify(Control.Disabled,0);
		pnlTools.stretchFirstRow=true;
		this.addLast(pnlTools,CellConstants.DONTSTRETCH, CellConstants.WEST).setTag(SPAN,new Dimension(3,1));;
		
		////////////////////
		// Main body of screen
		////////////////////

		this.addNext(new mLabel(MyLocale.getMsg(300,"Type:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.NORTHWEST));
		this.addLast(chcType,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addNext(new mLabel(MyLocale.getMsg(301,"Size:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(chcSize,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		
		this.addNext(new mLabel(MyLocale.getMsg(302,"Waypoint:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(inpWaypoint.setTag(Control.SPAN, new Dimension(2,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		
		this.addNext(new mLabel(MyLocale.getMsg(303,"Name:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(inpName.setTag(Control.SPAN, new Dimension(2,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		
		this.addNext(new mLabel(MyLocale.getMsg(304,"Location:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(btnWayLoc.setTag(Control.SPAN, new Dimension(2,1)),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		
		this.addNext(new mLabel(MyLocale.getMsg(305,"Hidden on:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		
		CellPanel ip=new CellPanel();
		ip.addNext(inpHidden,CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		ip.addLast(btnHiddenDate=new mButton(new mImage("calendar.png")),DONTSTRETCH,DONTFILL);
		this.addLast(ip,DONTSTRETCH,HFILL).setTag(Control.SPAN, new Dimension(2,1));
		inpHidden.modifyAll(DisplayOnly,0);
		
		this.addNext(new mLabel(MyLocale.getMsg(306,"Owner:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(inpOwner.setTag(Control.SPAN, new Dimension(2,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		
		this.addNext(new mLabel(MyLocale.getMsg(307,"Status:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		CellPanel cp=new CellPanel();
		cp.addNext(chcStatus,CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		cp.addLast(btnFoundDate=new mButton(new mImage("calendar.png")),DONTSTRETCH,DONTFILL);
		this.addLast(cp,DONTSTRETCH,HFILL).setTag(Control.SPAN, new Dimension(2,1));
		
		btnNotes = new mButton("Notes");
		this.addLast(btnNotes.setTag(Control.SPAN, new Dimension(3,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		//this.addNext(new mLabel(MyLocale.getMsg(308,"Notes:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		//this.addLast(btnCenter = new mButton(MyLocale.getMsg(309,"Make Center")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
//		this.addLast(btnDelete = new mButton(MyLocale.getMsg(310,"Delete")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		
	}
	
	public boolean isDirty() {
		return dirty_notes || dirty_details;
	}
	
	
	/**
	*	Set the values to display.
	*/
	public void setDetails(CacheHolderDetail ch){
		thisCache = ch;
		dirty_notes = false;
		dirty_details = false;
		inpWaypoint.setText(ch.wayPoint);
		inpName.setText(ch.CacheName);
	    btnWayLoc.setText(ch.pos.toString());
		inpHidden.setText(ch.DateHidden);
		inpOwner.setText(ch.CacheOwner);
		if (ch.CacheStatus.length()>=10 && ch.CacheStatus.charAt(4)=='-')
			chcStatus.setText(MyLocale.getMsg(318,"Found")+" "+ch.CacheStatus);
		else {
			chcStatus.setText(ch.CacheStatus);
			// If the cache status contains a date, do not overwrite it with 'found' message
			if(ch.is_found == true) chcStatus.setText(MyLocale.getMsg(318,"Found"));
		}
		chcType.setInt(transType(ch.type));
		if(ch.is_black){
			btnBlack.image = imgBlack;
		} else {
			btnBlack.image = imgBlackNo;
		}
		blackStatus=ch.is_black; 
		btnBlack.repaintNow();
		if(ch.has_bug == true) {
			btnShowBug.modify(Control.Disabled,1);
			btnShowBug.image = imgShowBug;
		} else {
			btnShowBug.modify(Control.Disabled,0);
			btnShowBug.image = imgShowBugNo;
		}
		btnShowBug.repaintNow();
		if(ch.CacheSize.equals("Micro")) chcSize.setInt(1);
		if(ch.CacheSize.equals("Small")) chcSize.setInt(2);
		if(ch.CacheSize.equals("Regular")) chcSize.setInt(3);
		if(ch.CacheSize.equals("Large")) chcSize.setInt(4);
		if(ch.CacheSize.equals("Other")) chcSize.setInt(5);
		if(ch.CacheSize.equals("Very Large")) chcSize.setInt(6);
		if(ch.CacheSize.equals("None")) chcSize.setInt(7);
		if(ch.CacheSize.equals("Not chosen")) chcSize.setInt(7);
	}
	
	
	/**
	*	Translate the cache type to the value in the cache type dropdown
	*	control.
	*/
	private int transType(String type){
		int c_type = 0;
		int tt = 0;
		tt = Convert.parseInt(type);
		switch(tt){
			case 0: c_type = 0; break;
			case 2: c_type = 1; break;
			case 3: c_type = 2; break;
			case 4: c_type = 3; break;
			case 5: c_type = 4; break;
			case 6: c_type = 5; break;
			case 453: c_type = 6;break;
			case 8: c_type = 7; break;
			case 11: c_type = 8; break;
			case 12: c_type = 9; break;
			case 13: c_type = 10; break;
			case 137: c_type = 11;break;
			case 50: c_type = 12;break;
			case 51: c_type = 13;break;
			case 52: c_type = 14;break;
			case 53: c_type = 15;break;
			case 54: c_type = 16;break;
			case 55: c_type = 17;break;

			default: Vm.debug("Unknown cachetype: " + type);
					break;
		}
		return c_type;
	}
	
	/**
	*	Method to translate a selected cache type in the drop down control
	*	to a "true" cache type.<br>
	*	This transformation is required to ease the display of the cache type
	*	icon in the table display.
	*/
	public String transSelect(int num){
		String ret = new String("");
		switch(num){
			case 0: ret = "0"; break;
			case 1: ret = "2"; break;
			case 2: ret = "3"; break;
			case 3: ret = "4"; break;
			case 4: ret = "5"; break;
			case 5: ret = "6"; break;
			case 6: ret = "453"; break;
			case 7: ret = "8"; break;
			case 8: ret = "11"; break;
			case 9: ret = "12"; break;
			case 10: ret = "13";break;
			case 11: ret = "137";break;
			case 12: ret = "50";break;
			case 13: ret = "51";break;
			case 14: ret = "52";break;
			case 15: ret = "53";break;
			case 16: ret = "54";break;
			case 17: ret = "55";break;

			default: Vm.debug("Unknown cachetype: " + num);
			break;

		} //switch
		return ret;
	}
	
	/**
	*	Method to react to a user input.
	*/
	public void onEvent(Event ev){
		if (ev instanceof DataChangeEvent ) {
			dirty_details = true;
			profile.hasUnsavedChanges=true;
		}
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if(ev.target == btnNotes){
				dirty_notes=true;
				NotesScreen nsc = new NotesScreen(thisCache);
				nsc.execute(this.getFrame(), Gui.CENTER_FRAME);
			}
			else if(ev.target == btnShowMap){
				Global.mainTab.SwitchToMovingMap(thisCache.pos, true);
/*				try {
					MapDetailForm mdf = new MapDetailForm(thisCache.wayPoint, pref, profile);
					mdf.execute();
				} catch (IllegalArgumentException e) {
					MessageBox tmp = new MessageBox(MyLocale.getMsg(321,"Error"), MyLocale.getMsg(322,"Kann Bild/Karte nicht finden")+": "+e.getMessage(), MessageBox.OKB);
					tmp.exec();
				}
	*/		}
			else if(ev.target == btnShowBug){
				InfoScreen is = new InfoScreen(thisCache.Bugs, "Travelbugs", false, pref);
				is.execute();
			}
			else if (ev.target == btnCenter){
				CWPoint cp=new CWPoint(thisCache.LatLon);
				if (!cp.isValid()){
					MessageBox tmpMB = new MessageBox(MyLocale.getMsg(312,"Error"), MyLocale.getMsg(4111,"Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM"), MessageBox.OKB);
					tmpMB.exec();
				} else {				
					pref.curCentrePt.set(cp);
					Global.mainTab.updateBearDist();
				}
			}
			else if (ev.target == btnAddDateTime){
				dirty_notes=true;
				String note = thisCache.CacheNotes;
				Time dtm = new Time();
				dtm.getTime();
				dtm.setFormat("E dd.MM.yyyy '/' HH:mm");
				if(note.length() > 0)	note = note + "\n" + dtm.toString();
				else 	note = note + dtm.toString();
				note = note + "\n";
				thisCache.CacheNotes = note;
				thisCache.saveCacheDetails( Global.getProfile().dataDir);
			}
			else if (ev.target == btnAddPicture){
				thisCache.addUserImage(profile);
			}
			else if(ev.target == btnBlack){
				if(thisCache.is_black) {
					thisCache.is_black = false;
					btnBlack.image = imgBlackNo;
				}
				else {
					thisCache.is_black = true;
					btnBlack.image = imgBlack;
				}
				blackStatus = thisCache.is_black;
				thisCache.setAttributesToAddiWpts();
				btnBlack.repaintNow();
			}
			else if (ev.target == btnNewWpt){
				Global.mainTab.newWaypoint(new CacheHolder());
			}
			else if (ev.target == btnGoto){
				// TODO if something changed saveWpt();
				Global.mainTab.gotoPoint(thisCache.LatLon);
			}
			else if (ev.target == btnWayLoc){
				CWPoint coords = new CWPoint(btnWayLoc.getText(),CWPoint.CW);
				CoordsScreen cs = new CoordsScreen();
				cs.setFields(coords, CWPoint.CW);
				if (cs.execute()== CoordsScreen.IDOK){
					dirty_details=true;
					coords = cs.getCoords();
					thisCache.pos.set(coords);
					btnWayLoc.setText(coords.toString());
					thisCache.LatLon=coords.toString();
					// If the current center is valid, calculate the distance and bearing to it
					CWPoint center=Global.getPref().curCentrePt;
					if (center.isValid()) thisCache.calcDistance(center);
				}
			}
			else if (ev.target==btnFoundDate) {
				DateChooser.dayFirst=true;
				DateChooser dc=new DateChooser(Vm.getLocale());
				dc.title=MyLocale.getMsg(328,"Date found"); 
				dc.setPreferredSize(240,240);
				if (dc.execute()==ewe.ui.FormBase.IDOK) {
				  chcStatus.setText(MyLocale.getMsg(318,"Found")+" "+Convert.toString(dc.year)+"-"+MyLocale.formatLong(dc.month,"00")+"-"+MyLocale.formatLong(dc.day,"00"));
				  dirty_details=true;
				}
			}
			else if (ev.target==btnHiddenDate) {
				DateChooser.dayFirst=true;
				DateChooser dc=new DateChooser(Vm.getLocale());
				dc.title=MyLocale.getMsg(329,"Hidden date"); 
				dc.setPreferredSize(240,240);
				if (inpHidden.getText().length()==10)
				try {
					dc.setDate(new Time(
						Convert.parseInt(inpHidden.getText().substring(8)),
					    Convert.parseInt(inpHidden.getText().substring(5,7)),
						Convert.parseInt(inpHidden.getText().substring(0,4))));
				} catch (NumberFormatException e) {
					dc.reset(new Time());
				}
				if (dc.execute()==ewe.ui.FormBase.IDOK) {
				  inpHidden.setText(Convert.toString(dc.year)+"-"+MyLocale.formatLong(dc.month,"00")+"-"+MyLocale.formatLong(dc.day,"00"));
				  dirty_details=true;
				}
			}
			ev.consumed=true;
		}
	}
	
	public void saveDirtyWaypoint() {
		// We have to update two objects: thisCache (a CacheHolderDetail) which contains 
		// the full cache which will be written to the cache.xml file AND
		// the CacheHolder object which sits in cacheDB
		  // Strip the found message if the status contains a date
		if (chcStatus.getText().startsWith(MyLocale.getMsg(318,"Found")) && 
				  chcStatus.getText().length()==MyLocale.getMsg(318,"Found").length()+11)
			  thisCache.CacheStatus = chcStatus.getText().substring(MyLocale.getMsg(318,"Found").length()+1);
		  else	  
			  thisCache.CacheStatus = chcStatus.getText();
		  thisCache.is_found = chcStatus.getText().startsWith(MyLocale.getMsg(318,"Found"));
		  thisCache.CacheOwner = inpOwner.getText().trim();
		  thisCache.is_owned = thisCache.CacheStatus.equals(MyLocale.getMsg(320,"Owner"));
		  // Avoid setting is_owned if alias is empty and username is empty
		  if(thisCache.is_owned == false){
			  thisCache.is_owned = (!pref.myAlias.equals("") && pref.myAlias.equals(thisCache.CacheOwner)) || 
					        (!pref.myAlias2.equals("") && pref.myAlias2.equals(thisCache.CacheOwner));
		  }
		  thisCache.is_black = blackStatus;
		  thisCache.wayPoint = inpWaypoint.getText().trim();
		  thisCache.CacheSize = chcSize.getText();
		  // If the waypoint does not have a name, give it one
		  if (thisCache.wayPoint.equals("")) { 
			  thisCache.wayPoint=profile.getNewWayPointName();
		  }
		  //Don't allow single letter names=> Problems in updateBearingDistance
		  // This is a hack but faster than slowing down the loop in updateBearingDistance
		  if (thisCache.wayPoint.length()<2) thisCache.wayPoint+=" ";
		  thisCache.CacheName = inpName.getText().trim();
		  thisCache.LatLon = thisCache.pos.toString();
		  thisCache.DateHidden = inpHidden.getText().trim();
		  thisCache.type = transSelect(chcType.getInt());
		  thisCache.saveCacheDetails(profile.dataDir);
		  // Now update the table
		  CacheHolder ch = (CacheHolder)cacheDB.get(Global.mainTab.tbP.getSelectedCache());
		  ch.CacheStatus=thisCache.CacheStatus;
		  ch.is_found=thisCache.is_found;
		  ch.is_owned=thisCache.is_owned;
		  ch.is_black=thisCache.is_black;
		  ch.wayPoint=thisCache.wayPoint;
		  ch.CacheSize=thisCache.CacheSize;
		  ch.wayPoint=thisCache.wayPoint;
		  ch.CacheName=thisCache.CacheName;
		  ch.LatLon=thisCache.LatLon;
		  ch.DateHidden=thisCache.DateHidden;
		  ch.CacheOwner=thisCache.CacheOwner;
		  ch.type=thisCache.type;
		  if (CacheType.isAddiWpt(ch.type)) {
			  int idx;
			  if (ch.wayPoint.length()<5)
				  idx=-1;
			  else
				  idx=profile.getCacheIndex("GC"+ ch.wayPoint.substring(ch.wayPoint.length() == 5?1:2));
			  if (idx<0) (new MessageBox(MyLocale.getMsg(144,"Warning"),
					  MyLocale.getMsg(734,"No main cache found for addi waypoint ")+" "+ch.wayPoint+
					  "\n"+MyLocale.getMsg(735,"Addi Waypoints must have the format xxYYYY, where xx are any 2 chars and YYYY are the main cache's chars after the GC"),FormBase.OKB)).execute();
			  profile.buildReferences();
		  }
		  // set status also on addi wpts
		  ch.setAttributesToAddiWpts();
		  dirty_notes=false;
		  dirty_details=false;
		  
		  Global.mainTab.tbP.refreshTable();
		  ////Vm.debug("New status updated!");
	}
}
