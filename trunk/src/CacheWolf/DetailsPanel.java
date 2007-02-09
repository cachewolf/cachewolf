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
	Vector cacheDB;
	CacheHolder thisCache;
	CellPanel pnlTools = new CellPanel(); 
	
	private boolean dirty_notes = false;
	private boolean dirty_details = false;
	private boolean blackStatus = false;
	Preferences pref; // Test
	Profile profile;
	mImage mIsBlack;
	mImage mNoBlack;
	mImage mI, mI_no;
	
	public DetailsPanel(){
		pref = Global.getPref();
		profile=Global.getProfile();
		cacheDB = profile.cacheDB;
		////////////////////
		// Tools
		////////////////////
		pnlTools.addLast(btnNewWpt = new mButton(MyLocale.getMsg(311,"Create Waypoint")),CellConstants.DONTSTRETCH, CellConstants.WEST);
		pnlTools.addNext(btnGoto = new mButton("Goto"),CellConstants.DONTSTRETCH, CellConstants.WEST);
		mI = new mImage("bug.gif");
		mI_no = new mImage("bug_no.gif");
		mImage mI2 = new mImage("globe_small.gif");
		
		mImage mI4 = new mImage("images.gif");
		mNoBlack = new mImage("no_black.png");
		mIsBlack = new mImage("is_black.png");
		btnShowBug = new mButton((IImage)mI_no);
		btnShowMap = new mButton((IImage)mI2);
		
		btnAddDateTime = new mButton((IImage)new mImage("date_time.png"));
		btnAddPicture = new mButton((IImage)mI4);
		btnBlack = new mButton((IImage)mNoBlack);
		pnlTools.addNext(btnShowBug,CellConstants.DONTSTRETCH, CellConstants.WEST);
		btnShowBug.modify(Control.Disabled,0);
		pnlTools.addNext(btnShowMap,CellConstants.DONTSTRETCH, CellConstants.WEST);
		pnlTools.addNext(btnAddPicture,CellConstants.DONTSTRETCH, CellConstants.WEST);
		pnlTools.addNext(btnBlack,CellConstants.DONTSTRETCH, CellConstants.WEST);
		pnlTools.addLast(btnAddDateTime,CellConstants.DONTSTRETCH, CellConstants.WEST);
		//showMap.modify(Control.Disabled,0);
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
		this.addLast(inpHidden.setTag(Control.SPAN, new Dimension(2,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		
		this.addNext(new mLabel(MyLocale.getMsg(306,"Owner:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(inpOwner.setTag(Control.SPAN, new Dimension(2,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		
		this.addNext(new mLabel(MyLocale.getMsg(307,"Status:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(chcStatus.setTag(Control.SPAN, new Dimension(2,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		
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
	public void setDetails(CacheHolder ch){
		thisCache = ch;
		dirty_notes = false;
		dirty_details = false;
		inpWaypoint.setText(ch.wayPoint);
		inpName.setText(ch.CacheName);
	    btnWayLoc.setText(ch.pos.toString());
		inpHidden.setText(ch.DateHidden);
		inpOwner.setText(ch.CacheOwner);
		chcStatus.setText(ch.CacheStatus);
		
		chcType.setInt(transType(ch.type));
		if(ch.is_black){
			btnBlack.image = mIsBlack;
		} else {
			btnBlack.image = mNoBlack;
		}
		blackStatus=ch.is_black; 
		btnBlack.repaintNow();
		if(ch.has_bug == true) {
			btnShowBug.modify(Control.Disabled,1);
			btnShowBug.image = mI;
		} else {
			btnShowBug.modify(Control.Disabled,0);
			btnShowBug.image = mI_no;
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

		if(ch.is_found == true) chcStatus.setText(MyLocale.getMsg(318,"Found"));
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
					btnBlack.image = mNoBlack;
				}
				else {
					thisCache.is_black = true;
					btnBlack.image = mIsBlack;
				}
				blackStatus = thisCache.is_black;
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
					coords = cs.getCoords();
					thisCache.pos.set(coords);
					btnWayLoc.setText(coords.toString());
					thisCache.LatLon=coords.toString();
				}
			}
			ev.consumed=true;
		}
	}
	
	public void saveDirtyWaypoint() {
		CacheHolder ch;
		  ch = (CacheHolder)cacheDB.get(Global.mainTab.tbP.getSelectedCache());
		  ch.CacheStatus = chcStatus.getText();
		  ch.is_found = ch.CacheStatus.equals(MyLocale.getMsg(318,"Found"));
		  ch.is_black = blackStatus;
		  ch.wayPoint = inpWaypoint.getText().trim();
		  ch.CacheName = inpName.getText().trim();
		  ch.LatLon = new CWPoint(btnWayLoc.getText(),CWPoint.CW).toString();
		  ch.DateHidden = inpHidden.getText().trim();
		  ch.CacheOwner = inpOwner.getText().trim();
		  ch.is_owned = pref.myAlias.equals(ch.CacheOwner);
		  ch.type = transSelect(chcType.getInt());
		  if (CacheType.isAddiWpt(ch.type)) 
			  profile.buildReferences();
		  // set status also on addi wpts
		  if (ch.hasAddiWpt()){
			  CacheHolder addiWpt;
			  for (int i=0;i<ch.addiWpts.getCount();i++){
				  addiWpt = (CacheHolder)ch.addiWpts.get(i);
				  addiWpt.CacheStatus = ch.CacheStatus;
				  addiWpt.is_found = ch.is_found;
				  addiWpt.is_owned = ch.is_owned;
			  }
		  }
		  if (dirty_notes) ch.saveCacheDetails(profile.dataDir);
		  dirty_notes=false;
		  dirty_details=false;
		  
		  Global.mainTab.tbP.refreshTable();
		  ////Vm.debug("New status updated!");
	}
}
