package CacheWolf;

import ewe.ui.*;
import ewe.fx.*;
import ewe.util.*;
import ewe.sys.*;
import ewe.sys.Long;

/**
*	Class to create the panel to show the cache details.<br>
*	Also allows for creation of a custom waypoint.<br>
*	Class ID = 300
*/
public class DetailsPanel extends CellPanel{
	Locale l = Vm.getLocale();
	LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
	mInput wayPoint = new mInput();
	mInput wayName = new mInput();
	mButton btnWayLoc = new mButton();
	mInput wayHidden = new mInput();
	mInput wayOwner = new mInput();
	//mInput wayStatus = new mInput();
	mTextPad wayNotes = new mTextPad();
	mCheckBox chkDelete, chkCenter;
	mChoice wayType = new mChoice(new String[]{"Custom", "Traditional", "Multi", "Virtual", "Letterbox", "Event", "Mega Event", "Mystery", "Webcam", "Locationless", "CITO", "Earthcache", "Parking", "Stage", "Question", "Final","Trailhead","Reference"},0);
	mChoice waySize = new mChoice(new String[]{"", "Micro", "Small", "Regular", "Large","Other","Very Large","None"},0);
	mComboBox wayStatus = new mComboBox(new String[]{"", (String)lr.get(313,"Flag 1"), (String)lr.get(314,"Flag 2"), (String)lr.get(315,"Flag 3"), (String)lr.get(316,"Flag 4"), (String)lr.get(317,"Search"), (String)lr.get(318,"Found"), (String)lr.get(319,"Not Found"), (String)lr.get(320,"Owner")},0);
	mButton btCrWp, showBug, showMap, addDateTime, btnGoto, addPicture;
	Vector cacheDB;
	CacheHolder thisCache;
	CellPanel toolP = new CellPanel();
	public boolean dirty_notes = false;
	public boolean dirty_details = false;
	public boolean dirty_new = false;
	public boolean dirty_delete = false;
	public boolean dirty_status = false;
	boolean newWp = false;
	MainTab mainT;
	Preferences pref;
	
	public DetailsPanel(){
		//String welcomeMessage = (String)lr.get(1,"how about that?");
		
		toolP.addNext(btCrWp = new mButton((String)lr.get(311,"Create Waypoint")),CellConstants.DONTSTRETCH, CellConstants.WEST);
		toolP.addNext(btnGoto = new mButton("Goto"),CellConstants.DONTSTRETCH, CellConstants.WEST);
		mImage mI = new mImage("bug.gif");
		mImage mI2 = new mImage("globe_small.gif");
		mImage mI3 = new mImage("date_time.png");
		mImage mI4 = new mImage("images.gif");
		showBug = new mButton((IImage)mI);
		showMap = new mButton((IImage)mI2);
		addDateTime = new mButton((IImage)mI3);
		addPicture = new mButton((IImage)mI4);
		toolP.addNext(showBug,CellConstants.DONTSTRETCH, CellConstants.WEST);
		showBug.modify(Control.Disabled,0);
		toolP.addNext(showMap,CellConstants.DONTSTRETCH, CellConstants.WEST);
		toolP.addNext(addDateTime,CellConstants.DONTSTRETCH, CellConstants.WEST);
		toolP.addLast(addPicture,CellConstants.DONTSTRETCH, CellConstants.WEST);
			
		//showMap.modify(Control.Disabled,0);
		this.addLast(toolP,CellConstants.DONTSTRETCH, CellConstants.WEST).setTag(SPAN,new Dimension(3,1));;
		
		this.addNext(new mLabel((String)lr.get(300,"Type:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.NORTHWEST));
		this.addLast(wayType,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addNext(new mLabel((String)lr.get(301,"Size:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(waySize,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		
		this.addNext(new mLabel((String)lr.get(302,"Waypoint:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(wayPoint.setTag(Control.SPAN, new Dimension(2,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		
		this.addNext(new mLabel((String)lr.get(303,"Name:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(wayName.setTag(Control.SPAN, new Dimension(2,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		
		this.addNext(new mLabel((String)lr.get(304,"Location:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(btnWayLoc.setTag(Control.SPAN, new Dimension(2,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		
		this.addNext(new mLabel((String)lr.get(305,"Hidden on:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(wayHidden.setTag(Control.SPAN, new Dimension(2,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		
		this.addNext(new mLabel((String)lr.get(306,"Owner:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(wayOwner.setTag(Control.SPAN, new Dimension(2,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		
		this.addNext(new mLabel((String)lr.get(307,"Status:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(wayStatus.setTag(Control.SPAN, new Dimension(2,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		
		this.addNext(new mLabel((String)lr.get(308,"Notes:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addNext(chkCenter = new mCheckBox((String)lr.get(309,"Make Center")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(chkDelete = new mCheckBox((String)lr.get(310,"Delete")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));

		ScrollBarPanel sbp = new ScrollBarPanel(wayNotes);
		//this.addLast(sbp, this.STRETCH, this.FILL);
		this.addLast(sbp.setTag(Control.SPAN, new Dimension(3,1)),CellConstants.STRETCH, (CellConstants.FILL|CellConstants.WEST));
	}
	
	/**
	*	Set the values to display.
	*/
	public void setDetails(CacheHolder ch, Vector DB, MainTab mt, Preferences p){
		if (this.newWp){
			this.newWp = false;
			btCrWp.setText((String)lr.get(312,"Save"));
			return;
		}
		else {
			btCrWp.setText((String)lr.get(311,"Create Waypoint"));
		}
		pref = p;
		mainT = mt;
		cacheDB = DB;
		thisCache = ch;
		dirty_notes = false;
		dirty_details = false;
		dirty_new = false;
		dirty_delete = false;
		chkCenter.setState(false);
		chkDelete.setState(false);
		wayPoint.setText(ch.wayPoint);
		wayName.setText(ch.CacheName);
	    btnWayLoc.setText(ch.LatLon);
		wayHidden.setText(ch.DateHidden);
		wayOwner.setText(ch.CacheOwner);
		wayStatus.setText(ch.CacheStatus);
		wayNotes.setText(ch.CacheNotes);
		wayType.setInt(transType(ch.type));
		if(ch.has_bug == true) {
			showBug.modify(Control.Disabled,1);
		} else {
			showBug.modify(Control.Disabled,0);
		}
		showBug.repaintNow();
		if(ch.CacheSize.equals("Micro")) waySize.setInt(1);
		if(ch.CacheSize.equals("Small")) waySize.setInt(2);
		if(ch.CacheSize.equals("Regular")) waySize.setInt(3);
		if(ch.CacheSize.equals("Large")) waySize.setInt(4);
		if(ch.CacheSize.equals("Other")) waySize.setInt(5);
		if(ch.CacheSize.equals("Very Large")) waySize.setInt(6);
		if(ch.CacheSize.equals("None")) waySize.setInt(7);
		if(ch.CacheSize.equals("Not chosen")) waySize.setInt(7);
		
		if(ch.is_found == true) wayStatus.setText((String)lr.get(318,"Found"));
	}
	
	private String getNewWayPointName(Vector DB){
		String strWp;
		Long  lgWp = new Long();
		//Create new waypoint,look if not in db
		lgWp.set(1);
		strWp = "CW" + l.format(Locale.FORMAT_PARSE_NUMBER, lgWp, "0000");
		CacheHolder tmpCh = new CacheHolder();
		for(int i = 0;i < DB.size();i++){
			tmpCh = (CacheHolder)DB.get(i);
			if(tmpCh.wayPoint.indexOf(strWp) >=0 ){
				//waypoint exists in database
				lgWp.set(lgWp.value + 1);
				strWp = "CW" + l.format(Locale.FORMAT_PARSE_NUMBER, lgWp, "0000");
				i = 0;
			}
		}
		return strWp;
	}
	
	public void newWaypoint(CacheHolder ch, Vector DB, MainTab mt, Preferences p){

		ch.wayPoint = getNewWayPointName(DB);
		ch.type = "0";
		ch.CacheSize = "None";
		setDetails(ch, DB, mt,p);
		this.newWp = true;
		cacheDB.add(thisCache);
		mt.select(this);
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
	
	private void saveWpt() {
		//Vm.debug("Sollte speichern");
		//CacheHolder ch = new CacheHolder();
		thisCache.wayPoint = wayPoint.getText();
		thisCache.CacheName = wayName.getText();
		thisCache.LatLon = new CWPoint(btnWayLoc.getText(),CWPoint.REGEX).toString();
		thisCache.DateHidden = wayHidden.getText();
		thisCache.CacheOwner = wayOwner.getText();
		thisCache.CacheStatus = wayStatus.getText();
		thisCache.CacheNotes = wayNotes.getText();
		thisCache.type = transSelect(wayType.getInt());
		//cacheDB.add(ch);
		
		if(thisCache.CacheNotes.length()>0){
			CacheReaderWriter crw = new CacheReaderWriter();
			crw.saveCacheDetails(thisCache, pref.mydatadir);
		}
		
		dirty_new = true;
		mainT.selectAndActive(cacheDB.size()-1);
	}
	
	/**
	*	Method to react to a user input.
	*/
	public void onEvent(Event ev){
		/**
		*	User changed status or notes.
		*/
		if(ev instanceof ControlEvent && ev.type == ControlEvent.FOCUS_OUT){
			if(ev.target == wayNotes){
				if(wayNotes.getText().length() > 0){
					////Vm.debug("Saving!!!");
					thisCache.CacheNotes = wayNotes.getText();
					CacheReaderWriter crw = new CacheReaderWriter();
					crw.saveCacheDetails(thisCache, pref.mydatadir);
				}
			}
			if(ev.target != wayNotes){
				dirty_status = true;
			}
		}
		/**
		*	User wishes to either delete a cache or to set the cache as
		*	a center location.<br>
		*	Also possible: the user created a custom waypoint.
		*/
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == chkDelete){
				//Vm.debug(thisCache.CacheName);
				cacheDB.remove(thisCache);
				dirty_delete = true;
			}
			if(ev.target == showMap){
				try {
					MapDetailForm mdf = new MapDetailForm(thisCache.wayPoint, pref);
					mdf.execute();
				} catch (IllegalArgumentException e) {
					MessageBox tmp = new MessageBox((String)lr.get(321,"Error"), (String)lr.get(322,"Kann Bild/Karte nicht finden")+": "+e.getMessage(), MessageBox.OKB);
					tmp.exec();
				}
			}
			if(ev.target == showBug){
				InfoScreen is = new InfoScreen(thisCache.Bugs, "Travelbugs", false, pref);
				is.execute();
			}
			if (ev.target == chkCenter){
				int counter = 0;
				//Vm.debug("This Cache: " + thisCache.LatLon);
				Extractor ex = new Extractor(" " + thisCache.LatLon, " ", " ", 0, true);
				String br2 = new String(); 
				String lg2 = new String();
				String br2_buf = new String(); 
				String lg2_buf = new String();
				String br2WE = new String();
				String lg2NS = new String();
				while(ex.endOfSearch() != true){
					if(counter == 0) lg2NS = ex.findNext();
					if(counter == 1) lg2 = ex.findNext();
					if(counter == 2) lg2_buf = ex.findNext();
					if(counter == 3) br2WE = ex.findNext();
					if(counter == 4) br2 = ex.findNext();
					if(counter == 5) br2_buf = ex.findNext();
					counter++;
					if(counter >= 10) break;
				}//while
				////Vm.debug("Extracted: " + lg2NS + " " + lg2 + " " + lg2_buf);
				////Vm.debug("Extracted: " + br2WE + " " + br2 + " " + br2_buf);
				pref.mylgNS = lg2NS;
				
				if(lg2.length() == 3) pref.mylgDeg = lg2.substring(0,2); 
				else pref.mylgDeg = lg2;
				
				pref.mylgMin = lg2_buf;
				pref.mybrWE = br2WE;
				
				if(br2.length() == 4) pref.mybrDeg = br2.substring(0,3); 
				else pref.mybrDeg = br2;
				
				pref.mybrMin = br2_buf;
				mainT.updateBearDist();
			}
			if (ev.target == addDateTime){
				String note = wayNotes.getText();
				Time dtm = new Time();
				dtm.getTime();
				dtm.setFormat("E dd.MM.yyyy '/' H:m");
				if(note.length() > 0)	note = note + "\n" + dtm.toString();
				else 	note = note + dtm.toString();
				note = note + "\n";
				wayNotes.setText(note);
				thisCache.CacheNotes = wayNotes.getText();
				CacheReaderWriter crw = new CacheReaderWriter();
				crw.saveCacheDetails(thisCache, pref.mydatadir);
			}
			if (ev.target == addPicture){
				thisCache.addUserImage(pref);
			}
			if (ev.target == btCrWp){
				if(btCrWp.getText().equals((String)lr.get(312,"Save"))){
					saveWpt();
				}
				if(btCrWp.getText().equals((String)lr.get(311,"Create Waypoint"))){
					thisCache = new CacheHolder();
					dirty_new = true;
					wayPoint.setText(getNewWayPointName(cacheDB));
					wayName.setText("");
					btnWayLoc.setText("N hh dd.mmm E hh dd.mmm");
					wayHidden.setText("");
					wayOwner.setText("");
					wayStatus.setText("");
					wayNotes.setText("");
					wayType.setInt(0);
					waySize.setInt(7);
					thisCache.wayPoint = wayPoint.getText();
					cacheDB.add(thisCache);
				}
				if(btCrWp.getText().equals((String)lr.get(312,"Save"))) btCrWp.setText((String)lr.get(311,"Create Waypoint"));
				else btCrWp.setText((String)lr.get(312,"Save"));
			}
			if (ev.target == btnGoto){
				// TODO if something changed saveWpt();
				mainT.gotoPoint(thisCache.LatLon);
			}
			if (ev.target == btnWayLoc){
				CWPoint coords = new CWPoint(btnWayLoc.getText(),CWPoint.CW);
				CoordsScreen cs = new CoordsScreen();
				cs.setFields(coords, CWPoint.CW);
				if (cs.execute()== CoordsScreen.IDOK){
					coords = cs.getCoords();
					btnWayLoc.setText(coords.toString());
				}
			}

		}
	}
}
