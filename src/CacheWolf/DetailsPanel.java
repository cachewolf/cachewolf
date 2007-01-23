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
	mInput wayPoint = new mInput();
	mInput wayName = new mInput();
	mButton btnWayLoc = new mButton();
	mInput wayHidden = new mInput();
	mInput wayOwner = new mInput();
	//mInput wayStatus = new mInput();
	
	mButton btnDelete,btnCenter;
	mChoice wayType = new mChoice(new String[]{"Custom", "Traditional", "Multi", "Virtual", "Letterbox", "Event", "Mega Event", "Mystery", "Webcam", "Locationless", "CITO", "Earthcache", "Parking", "Stage", "Question", "Final","Trailhead","Reference"},0);
	mChoice waySize = new mChoice(new String[]{"", "Micro", "Small", "Regular", "Large","Other","Very Large","None"},0);
	mComboBox wayStatus = new mComboBox(new String[]{"", MyLocale.getMsg(313,"Flag 1"), MyLocale.getMsg(314,"Flag 2"), MyLocale.getMsg(315,"Flag 3"), MyLocale.getMsg(316,"Flag 4"), MyLocale.getMsg(317,"Search"), MyLocale.getMsg(318,"Found"), MyLocale.getMsg(319,"Not Found"), MyLocale.getMsg(320,"Owner")},0);
	mButton btCrWp, showBug, showMap, btnGoto, addPicture, btnBlack, btNotes;
	Vector cacheDB;
	CacheHolder thisCache;
	CellPanel toolP = new CellPanel();
	public boolean dirty_notes = false;
	public boolean dirty_details = false;
	public boolean dirty_newOrDelete = false;
	public boolean dirty_delete = false;
	public boolean dirty_status = false;
	boolean newWp = false;
	public boolean blackStatus = false;
	MainTab mainT;
	Preferences pref; // Test
	Profile profile;
	mImage mIsBlack;
	mImage mNoBlack;
	
	public DetailsPanel(){
		//String welcomeMessage = MyLocale.getMsg(1,"how about that?");
		
		toolP.addNext(btCrWp = new mButton(MyLocale.getMsg(311,"Create Waypoint")),CellConstants.DONTSTRETCH, CellConstants.WEST);
		toolP.addNext(btnGoto = new mButton("Goto"),CellConstants.DONTSTRETCH, CellConstants.WEST);
		mImage mI = new mImage("bug.gif");
		mImage mI2 = new mImage("globe_small.gif");
		
		mImage mI4 = new mImage("images.gif");
		mNoBlack = new mImage("no_black.png");
		mIsBlack = new mImage("is_black.png");
		showBug = new mButton((IImage)mI);
		showMap = new mButton((IImage)mI2);
		
		addPicture = new mButton((IImage)mI4);
		btnBlack = new mButton((IImage)mNoBlack);
		toolP.addNext(showBug,CellConstants.DONTSTRETCH, CellConstants.WEST);
		showBug.modify(Control.Disabled,0);
		toolP.addNext(showMap,CellConstants.DONTSTRETCH, CellConstants.WEST);
		toolP.addNext(addPicture,CellConstants.DONTSTRETCH, CellConstants.WEST);
		toolP.addLast(btnBlack,CellConstants.DONTSTRETCH, CellConstants.WEST);
			
		//showMap.modify(Control.Disabled,0);
		this.addLast(toolP,CellConstants.DONTSTRETCH, CellConstants.WEST).setTag(SPAN,new Dimension(3,1));;
		
		this.addNext(new mLabel(MyLocale.getMsg(300,"Type:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.NORTHWEST));
		this.addLast(wayType,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addNext(new mLabel(MyLocale.getMsg(301,"Size:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(waySize,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		
		this.addNext(new mLabel(MyLocale.getMsg(302,"Waypoint:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(wayPoint.setTag(Control.SPAN, new Dimension(2,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		
		this.addNext(new mLabel(MyLocale.getMsg(303,"Name:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(wayName.setTag(Control.SPAN, new Dimension(2,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		
		this.addNext(new mLabel(MyLocale.getMsg(304,"Location:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(btnWayLoc.setTag(Control.SPAN, new Dimension(2,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		
		this.addNext(new mLabel(MyLocale.getMsg(305,"Hidden on:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(wayHidden.setTag(Control.SPAN, new Dimension(2,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		
		this.addNext(new mLabel(MyLocale.getMsg(306,"Owner:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(wayOwner.setTag(Control.SPAN, new Dimension(2,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		
		this.addNext(new mLabel(MyLocale.getMsg(307,"Status:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(wayStatus.setTag(Control.SPAN, new Dimension(2,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		
		btNotes = new mButton("Notes");
		this.addLast(btNotes.setTag(Control.SPAN, new Dimension(3,1)),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		//this.addNext(new mLabel(MyLocale.getMsg(308,"Notes:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		//this.addLast(btnCenter = new mButton(MyLocale.getMsg(309,"Make Center")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
//		this.addLast(btnDelete = new mButton(MyLocale.getMsg(310,"Delete")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		
	}
	
	/**
	*	Set the values to display.
	*/
	public void setDetails(CacheHolder ch, MainTab mt){
		if (this.newWp){
			this.newWp = false;
			btCrWp.setText(MyLocale.getMsg(312,"Save"));
			return;
		}
		else {
			btCrWp.setText(MyLocale.getMsg(311,"Create Waypoint"));
		}
		pref = Global.getPref();
		profile=Global.getProfile();
		mainT = mt;
		cacheDB = profile.cacheDB;
		thisCache = ch;
		dirty_notes = false;
		dirty_details = false;
		dirty_newOrDelete = false; // Cache has been created/deleted but not saved
		wayPoint.setText(ch.wayPoint);
		wayName.setText(ch.CacheName);
	    btnWayLoc.setText(ch.pos.toString());
		wayHidden.setText(ch.DateHidden);
		wayOwner.setText(ch.CacheOwner);
		wayStatus.setText(ch.CacheStatus);
		
		wayType.setInt(transType(ch.type));
		if(ch.is_black){
			btnBlack.image = mIsBlack;
		} else {
			btnBlack.image = mNoBlack;
		}
		btnBlack.repaintNow();
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
		
		if(ch.is_found == true) wayStatus.setText(MyLocale.getMsg(318,"Found"));
	}
	
	private String getNewWayPointName(Vector DB){
		String strWp=null;
		long  lgWp=1;
		//Create new waypoint,look if not in db
		for(int i = 0;i < DB.size();i++){
			strWp = "CW" + MyLocale.formatLong(lgWp, "0000");
			if(((CacheHolder)DB.get(i)).wayPoint.indexOf(strWp) >=0 ){
				//waypoint exists in database
				lgWp++;
				i = -1; // Because i++ will be executed next, so we start the loop with 0
			}
		}
		return strWp;
	}
	
	/**
	 * this is called from goto / MovingMap and so on to 
	 * offer the user the possibility of entering an new waypoint
	 * at a given position
	 * 
	 * @param ch
	 * @param mt
	 */
	public void newWaypoint(CacheHolder ch, MainTab mt){
		this.profile = Global.getProfile();
		ch.wayPoint = getNewWayPointName(profile.cacheDB);
		ch.type = "0";
		ch.CacheSize = "None";
		setDetails(ch, mt);
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
		thisCache.pos = new CWPoint(btnWayLoc.getText(),CWPoint.REGEX);
		thisCache.LatLon = thisCache.pos.toString();
		thisCache.DateHidden = wayHidden.getText();
		thisCache.CacheOwner = wayOwner.getText();
		thisCache.CacheStatus = wayStatus.getText();
		thisCache.type = transSelect(wayType.getInt());
		//cacheDB.add(ch);
		
		if(thisCache.CacheNotes.length()>0){
			thisCache.saveCacheDetails(profile.dataDir);
		}
		
		dirty_newOrDelete = true;
		mainT.selectAndActive(cacheDB.size()-1);
	}
	
	/**
	*	Method to react to a user input.
	*/
	public void onEvent(Event ev){
		if(ev instanceof ControlEvent && ev.type == ControlEvent.FOCUS_OUT){
			dirty_status = true;
		}
		/**
		*	User changed status.
		*/
		/**
		*	User wishes to either delete a cache or to set the cache as
		*	a center location.<br>
		*	Also possible: the user created a custom waypoint.
		*/
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			
			if(ev.target == btNotes){
				NotesScreen nsc = new NotesScreen(thisCache, profile);
				nsc.execute(this.getFrame(), Gui.CENTER_FRAME);
			}
			if(ev.target == showMap){
				try {
					MapDetailForm mdf = new MapDetailForm(thisCache.wayPoint, pref, profile);
					mdf.execute();
				} catch (IllegalArgumentException e) {
					MessageBox tmp = new MessageBox(MyLocale.getMsg(321,"Error"), MyLocale.getMsg(322,"Kann Bild/Karte nicht finden")+": "+e.getMessage(), MessageBox.OKB);
					tmp.exec();
				}
			}
			if(ev.target == showBug){
				InfoScreen is = new InfoScreen(thisCache.Bugs, "Travelbugs", false, pref);
				is.execute();
			}
			if (ev.target == btnCenter){
				CWPoint cp=new CWPoint(thisCache.LatLon);
				if (!cp.isValid()){
					MessageBox tmpMB = new MessageBox(MyLocale.getMsg(312,"Error"), MyLocale.getMsg(4111,"Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM"), MessageBox.OKB);
					tmpMB.exec();
				} else {				
					pref.curCentrePt.set(cp);
					mainT.updateBearDist();
				}
			}
			
			if (ev.target == addPicture){
				thisCache.addUserImage(profile);
			}
			if(ev.target == btnBlack){
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
			if (ev.target == btCrWp){
				if(btCrWp.getText().equals(MyLocale.getMsg(312,"Save"))){
					saveWpt();
				}
				if(btCrWp.getText().equals(MyLocale.getMsg(311,"Create Waypoint"))){
					thisCache = new CacheHolder();
					dirty_newOrDelete = true;
					wayPoint.setText(getNewWayPointName(cacheDB));
					wayName.setText("");
					btnWayLoc.setText("N hh dd.mmm E hh dd.mmm");
					wayHidden.setText("");
					wayOwner.setText("");
					wayStatus.setText("");
					wayType.setInt(0);
					waySize.setInt(7);
					thisCache.wayPoint = wayPoint.getText();
					cacheDB.add(thisCache);
				}
				if(btCrWp.getText().equals(MyLocale.getMsg(312,"Save"))) btCrWp.setText(MyLocale.getMsg(311,"Create Waypoint"));
				else btCrWp.setText(MyLocale.getMsg(312,"Save"));
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
