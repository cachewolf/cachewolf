package cachewolf;

import eve.ui.*;
import eve.ui.formatted.*;
import eve.fx.*;

import eve.sys.*;
import eve.ui.event.*;

/**
*	Class to create the panel to show the cache details.<br>
*	Also allows for creation of a custom waypoint.<br>
*
*
*/
public class DetailsPanel extends CellPanel{
	Input inpWaypoint = new Input();
	Input inpName = new Input();
	Button btnWayLoc = new Button();
	Input inpHidden = new Input();
	Input inpOwner = new Input();
	Button btnAddDateTime;
	Choice chcType = new Choice(CacheType.wayTypeList(),0);
	Choice chcSize = new Choice(CacheHolder.cacheSizeList,0);
	ComboBox chcStatus = new ComboBox(new String[]{"", MyLocale.getMsg(313,"Flag 1"), MyLocale.getMsg(314,"Flag 2"), MyLocale.getMsg(315,"Flag 3"), MyLocale.getMsg(316,"Flag 4"), MyLocale.getMsg(317,"Search"), MyLocale.getMsg(318,"Found"), MyLocale.getMsg(319,"Not Found"), MyLocale.getMsg(320,"Owner")},0);
	Button btnNewWpt, btnShowBug, btnShowMap, btnGoto, btnAddPicture, btnBlack, btnNotes;
	Button btnFoundDate,btnHiddenDate;
	CacheHolder thisCache;
	Panel pnlTools = new Panel();
	AttributesViewer attV;
	TextPad mNotes;

	private boolean dirty_notes = false;
	private boolean dirty_details = false;
	private boolean blackStatus = false;
	private boolean blackStatusChanged=false;
	private boolean needsTableUpdate = false;
	private boolean isBigScreen = false;

	Preferences pref; // Test
	Profile profile;
	Picture imgBlack;
	Picture imgBlackNo;
	Picture imgShowBug, imgShowBugNo;
	Label lblDiff, lblTerr;

	public DetailsPanel(){
		pref = Global.getPref();
		profile=Global.getProfile();
		//cacheDB = profile.cacheDB;
		////////////////////
		// Tools
		////////////////////
		// Use larger Button-Icons on VGA-mobiles
		int sw = MyLocale.getScreenWidth();
		String imagesize="";
		if (eve.sys.Device.isMobile() && sw >= 400) imagesize="_vga";
		// Button 1: New Waypoint
		pnlTools.addNext(btnNewWpt = new Button(new Picture("newwpt"+imagesize+".png",new Color(255,0,0),0)));
		btnNewWpt.setToolTip(MyLocale.getMsg(311,"Create Waypoint"));
		PenEvent.wantPenMoved(btnNewWpt,PenEvent.WANT_PEN_MOVED_ONOFF,true);
		// Button 2: Goto
		pnlTools.addNext(btnGoto = new Button(new Picture("goto"+imagesize+".png",Color.White,0)));//Goto.gif funzt manchmal nicht
		btnGoto.setToolTip(MyLocale.getMsg(345,"Goto these coordinates"));
		// Button 3: Travelbugs
		imgShowBug = new Picture("bug"+imagesize+".gif");
		imgShowBugNo = new Picture("bug_no"+imagesize+".gif");
		pnlTools.addNext(btnShowBug = new Button(imgShowBugNo));
		//btnShowBug.modify(Control.Disabled,0);
		btnShowBug.setToolTip(MyLocale.getMsg(346,"Show travelbugs"));
		// Button 4: Show Map
		pnlTools.addNext(btnShowMap = new Button(new Picture("globe_small"+imagesize+".gif")));
		btnShowMap.setToolTip(MyLocale.getMsg(347,"Show map"));
		// Button 5: Add images
		pnlTools.addNext(btnAddPicture = new Button(new Picture("images"+imagesize+".gif")));
		btnAddPicture.setToolTip(MyLocale.getMsg(348,"Add user pictures"));
		// Button 6: Toggle blacklist status
		imgBlackNo = new Picture("no_black"+imagesize+".png",Color.Black,0);
		imgBlack = new Picture("is_black"+imagesize+".png",Color.White,0);
		pnlTools.addNext(btnBlack=new Button(imgBlackNo));
		btnBlack.setToolTip(MyLocale.getMsg(349,"Toggle Blacklist status"));
		// Button 7: Notes
		pnlTools.addNext(btnNotes=new Button(new Picture("notes"+imagesize+".gif",Color.DarkBlue,0)));
		btnNotes.setToolTip(MyLocale.getMsg(351,"Add/Edit notes"));
		// Button 8: Date/time stamp
		pnlTools.addLast(btnAddDateTime = new Button(new Picture("date_time"+imagesize+".png")));
		btnAddDateTime.setToolTip(MyLocale.getMsg(350,"Add timestamp to notes"));
		//showMap.modify(Control.Disabled,0);
		pnlTools.stretchFirstRow=true;
		this.addLast(pnlTools,CellConstants.DONTSTRETCH, CellConstants.WEST).setTag(TAG_SPAN,new Dimension(3,1));;

		////////////////////
		// Main body of screen
		////////////////////

		this.addNext(new Label(MyLocale.getMsg(300,"Type:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.NORTHWEST));
		Panel line1Panel = new Panel(); line1Panel.stretchFirstColumn=true;
		line1Panel.addNext(chcType,CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		line1Panel.addLast(lblDiff=new Label(MyLocale.getMsg(1000,"D")+": 5.5"),CellConstants.DONTSTRETCH,(CellConstants.DONTFILL|CellConstants.EAST));
		this.addLast(line1Panel,HSTRETCH,HFILL);

		this.addNext(new Label(MyLocale.getMsg(301,"Size:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		Panel line2Panel = new Panel(); line2Panel.stretchFirstColumn=true;
		line2Panel.addNext(chcSize,CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		line2Panel.addLast(lblTerr=new Label(MyLocale.getMsg(1001,"T")+": 5.5"),CellConstants.DONTSTRETCH,(CellConstants.DONTFILL|CellConstants.EAST));
		this.addLast(line2Panel,HSTRETCH,HFILL);

		this.addNext(new Label(MyLocale.getMsg(302,"Waypoint:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(inpWaypoint,CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));

		this.addNext(new Label(MyLocale.getMsg(303,"Name:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(inpName,CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));

		this.addNext(new Label(MyLocale.getMsg(304,"Location:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(btnWayLoc,CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));

		this.addNext(new Label(MyLocale.getMsg(307,"Status:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		Panel cp=new Panel(); cp.stretchFirstColumn=true;
		cp.addNext(chcStatus,CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		cp.addLast(btnFoundDate=new Button(new Picture("calendar"+imagesize+".png")),DONTSTRETCH,DONTFILL);
		this.addLast(cp,HSTRETCH,HFILL);

		this.addNext(new Label(MyLocale.getMsg(306,"Owner:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		this.addLast(inpOwner,CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));

		this.addNext(new Label(MyLocale.getMsg(305,"Hidden on:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		Panel ip=new Panel(); ip.stretchFirstColumn=true;
		ip.addNext(inpHidden,CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		ip.addLast(btnHiddenDate=new Button(new Picture("calendar"+imagesize+".png")),DONTSTRETCH,DONTFILL);
		this.addLast(ip,HSTRETCH,HFILL);
		inpHidden.modifyAll(DisplayOnly,0);

		attV=new AttributesViewer();
		this.addLast(attV);
		if ((MyLocale.getScreenWidth() >= 400) && (MyLocale.getScreenHeight() >= 600)){
			isBigScreen = true;
			this.addLast(new Label(MyLocale.getMsg(308,"Notes:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
			mNotes = new TextPad();
			mNotes.modify(ControlConstants.NotEditable,0);
			this.addLast(new MyScrollBarPanel(mNotes));
		}
	}

	public void clear() {
		attV.clear();
	}

	public void setNeedsTableUpdate(boolean tableUpdate) {
		needsTableUpdate = tableUpdate;
	}
	public boolean needsTableUpdate() {
		return needsTableUpdate;
	}

	public boolean isDirty() {
		return dirty_notes || dirty_details || needsTableUpdate;
	}
	public boolean hasBlackStatusChanged() {
		return blackStatusChanged;
	}


	/**
	* @param chD details of the cache to display
	* @param dbindex index in cacheDB, in which changes will be saved
	*/
	public void setDetails(CacheHolder ch){
		thisCache = ch;
		dirty_notes = false;
		dirty_details = false;
		inpWaypoint.setText(ch.wayPoint);
		inpName.setText(ch.cacheName);
	    btnWayLoc.setText(ch.pos.toString());
		inpHidden.setText(ch.dateHidden);
		inpOwner.setText(ch.cacheOwner);
		if (ch.cacheStatus.length()>=10 && ch.cacheStatus.charAt(4)=='-')
			chcStatus.setText(MyLocale.getMsg(318,"Found")+" "+ch.cacheStatus);
		else {
			chcStatus.setText(ch.cacheStatus);
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
		blackStatusChanged=false;
		btnBlack.repaintNow();
		if (inpWaypoint.getText().length() == 0)
			createWptName();
		if(ch.has_bug == true) {
			//btnShowBug.modify(Control.Disabled,1);
			btnShowBug.image = imgShowBug;
		} else {
			//btnShowBug.modify(Control.Disabled,0);
			btnShowBug.image = imgShowBugNo;
		}
		btnShowBug.repaintNow();
		chcSize.setInt(ch.cacheSize);
		attV.showImages(ch.getCacheDetails(true).attributes);
		lblTerr.setText((ch.terrain.length()>0) ? (MyLocale.getMsg(1001,"T")+": "+ch.terrain) : "");
		lblDiff.setText((ch.hard.length()>0)    ? (MyLocale.getMsg(1000,"D")+": "+ch.hard) : "");
		if(isBigScreen)	mNotes.setText(ch.details.cacheNotes);	}


	/**
	*	Translate the cache type to the value in the cache type dropdown
	*	control.
	*/
	private int transType(int type){
		int c_type = CacheType.getWayTypePos(type);
		if (c_type<0) c_type=0;
		return c_type;
	}

	/**
	*	Method to translate a selected cache type in the drop down control
	*	to a "true" cache type.<br>
	*	This transformation is required to ease the display of the cache type
	*	icon in the table display.
	*/
	public int transSelect(int num){
		return CacheType.wayTypeNo[num];
	}

	/**
	 * if is addi -> returns the respective AddiWpt
	 * if is main -> returns the respective MainWpt
	 *
	 */
	public void createWptName() {
		String wpt = inpWaypoint.getText().toUpperCase();
		if (CacheType.isAddiWpt(transSelect(chcType.getInt())) &&
				(Global.mainTab.mainCache.startsWith("GC")||Global.mainTab.mainCache.startsWith("OC")||Global.mainTab.mainCache.startsWith("CW")) &&
				wpt.startsWith("CW")) {
			// for what was this?:
			Global.mainTab.lastselected=Global.mainTab.mainCache; //I don't know exactly, but it's needed for creating a series of Addis

			inpWaypoint.setText(Global.getProfile().getNewAddiWayPointName(Global.mainTab.mainCache));
		}
		if (!CacheType.isAddiWpt(transSelect(chcType.getInt())) && !(wpt.startsWith("GC")
				|| wpt.startsWith("OC") || wpt.startsWith("CW")) ) {
			inpWaypoint.setText(Global.getProfile().getNewWayPointName());
		}
	}

	/**
	*	Method to react to a user input.
	*/
	public void onEvent(Event ev){
		if (ev instanceof DataChangeEvent ) {
			if (ev.target == inpWaypoint) {
				// If user used lower case -> convert directly to upper case
				inpWaypoint.setText(inpWaypoint.getText().toUpperCase());
			}
			dirty_details = true;
			needsTableUpdate = true;
			profile.hasUnsavedChanges=true;
			if (ev.target==chcType) {
				createWptName();
			}
		}
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if(ev.target == btnNotes){
				dirty_notes=true; // TODO I think this is redundant, because the notes are saved seperately by the notes screen itself
				NotesScreen nsc = new NotesScreen(thisCache.getCacheDetails(true));
				nsc.execute(this.getFrame(), Gui.CENTER_FRAME);
				if(isBigScreen) mNotes.setText(thisCache.getCacheDetails(true).cacheNotes);
			} else if(ev.target == btnShowMap){
				Global.mainTab.switchToMovingMap(thisCache.pos, true);
/*				try {
					MapDetailForm mdf = new MapDetailForm(thisCache.wayPoint, pref, profile);
					mdf.execute();
				} catch (IllegalArgumentException e) {
					MessageBox tmp = new MessageBox(MyLocale.getMsg(321,"Error"), MyLocale.getMsg(322,"Kann Bild/Karte nicht finden")+": "+e.getMessage(), MessageBox.OKB);
					tmp.exec();
				}
	*/		} else if(ev.target == btnShowBug){
				//InfoScreen is = new InfoScreen(thisCache.Travelbugs.toHtml(), "Travelbugs", false, pref);
				//is.execute();
				TravelbugInCacheScreen ts = new TravelbugInCacheScreen(thisCache.getCacheDetails(true).travelbugs.toHtml(), "Travelbugs");
				ts.execute(this.getFrame(), Gui.CENTER_FRAME);
			} else /* if (ev.target == btnCenter){
				CWPoint cp=new CWPoint(thisCache.LatLon);
				if (!cp.isValid()){
					MessageBox tmpMB = new MessageBox(MyLocale.getMsg(312,"Error"), MyLocale.getMsg(4111,"Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM"), MessageBox.OKB);
					tmpMB.exec();
				} else {
					pref.curCentrePt.set(cp);
					Global.mainTab.updateBearDist();
				}
			}
			else */ if (ev.target == btnAddDateTime){
				dirty_notes=true;
				String note = thisCache.getCacheDetails(true).cacheNotes;
				Time dtm = new Time();
				dtm.getTime();
				dtm.setFormat("E dd.MM.yyyy '/' HH:mm");
				if(note.length() > 0)	note = note + "\n" + dtm.toString();
				else 	note = note + dtm.toString();
				note = note + "\n";
				thisCache.getCacheDetails(true).cacheNotes = note;
				thisCache.getCacheDetails(true).saveCacheDetails( Global.getProfile().dataDir);
			} else if (ev.target == btnAddPicture){
				thisCache.getCacheDetails(true).addUserImage(profile);
			} else if(ev.target == btnBlack){
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
				dirty_details=true;
				blackStatusChanged=true;
			} else if (ev.target == btnNewWpt){
				CacheHolder ch = new CacheHolder();
				ch.latLon = thisCache.latLon;
				ch.pos = new CWPoint( thisCache.pos );
				ch.type = 51;
				Global.mainTab.newWaypoint(ch);
			} else if (ev.target == btnGoto){
				// TODO if something changed saveWpt();
				Global.mainTab.gotoPoint(thisCache.pos);
			} else if (ev.target == btnWayLoc){
				CWPoint coords = new CWPoint(btnWayLoc.getText(),CWPoint.CW);
				CoordsScreen cs = new CoordsScreen(true);
				cs.setFields(coords, CWPoint.CW);
				if (cs.execute()== CoordsScreen.IDOK){
					dirty_details=true;
					coords = cs.getCoords();
					thisCache.pos.set(coords);
					btnWayLoc.setText(coords.toString());
					thisCache.latLon=coords.toString();
					// If the current centre is valid, calculate the distance and bearing to it
					CWPoint centre=Global.getPref().curCentrePt;
					if (centre.isValid()) thisCache.calcDistance(centre);
				}
			} else if (ev.target==btnFoundDate) {
				DateTimeChooser dc=new DateTimeChooser(Vm.getLocale(), true);
				dc.title=MyLocale.getMsg(328,"Date found");
				dc.setPreferredSize(240,240);
				String foundDate=chcStatus.getText();
				if (foundDate.startsWith(MyLocale.getMsg(318,"Found")+" ")) foundDate=foundDate.substring(MyLocale.getMsg(318,"Found").length()+1);
				Time t=new Time();
				try {
					t.parse(foundDate,"y-M-d H:m");
				} catch(IllegalArgumentException e) {
					try {
						t.parse(foundDate,"y-M-d");
					} catch(IllegalArgumentException e1) {}
				};
				dc.reset(t);
				if (dc.execute()==eve.ui.FormBase.IDOK) {
				  chcStatus.setText(MyLocale.getMsg(318,"Found")+" "+Convert.toString(dc.year)+"-"+MyLocale.formatLong(dc.month,"00")+"-"+MyLocale.formatLong(dc.day,"00")+" "+dc.time);
				  dirty_details=true;
				  profile.hasUnsavedChanges=true;
				}
			} else if (ev.target==btnHiddenDate) {
				//DateTimeChooser.dayFirst=true;
				DateTimeChooser dc=new DateTimeChooser(Vm.getLocale(),false);
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
				if (dc.execute()==eve.ui.FormBase.IDOK) {
				  inpHidden.setText(Convert.toString(dc.year)+"-"+MyLocale.formatLong(dc.month,"00")+"-"+MyLocale.formatLong(dc.day,"00"));
				  dirty_details=true;
				  profile.hasUnsavedChanges=true;
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
				  chcStatus.getText().length()>=MyLocale.getMsg(318,"Found").length()+11)
			  thisCache.cacheStatus = chcStatus.getText().substring(MyLocale.getMsg(318,"Found").length()+1);
		  else
			  thisCache.cacheStatus = chcStatus.getText();
		  thisCache.is_found = chcStatus.getText().startsWith(MyLocale.getMsg(318,"Found"));
		  thisCache.cacheOwner = inpOwner.getText().trim();
		  thisCache.is_owned = thisCache.cacheStatus.equals(MyLocale.getMsg(320,"Owner"));
		  // Avoid setting is_owned if alias is empty and username is empty
		  if(thisCache.is_owned == false){
			  thisCache.is_owned = (!pref.myAlias.equals("") && pref.myAlias.equals(thisCache.cacheOwner)) ||
					        (!pref.myAlias2.equals("") && pref.myAlias2.equals(thisCache.cacheOwner));
		  }
		  thisCache.is_black = blackStatus;
		  String oldWaypoint=thisCache.wayPoint;
		  thisCache.wayPoint = inpWaypoint.getText().toUpperCase().trim();
		  thisCache.setCacheSize(chcSize.getText());
		  // If the waypoint does not have a name, give it one
		  if (thisCache.wayPoint.equals("")) {
			  thisCache.wayPoint=profile.getNewWayPointName();
		  }
		  //Don't allow single letter names=> Problems in updateBearingDistance
		  // This is a hack but faster than slowing down the loop in updateBearingDistance
		  if (thisCache.wayPoint.length()<2) thisCache.wayPoint+=" ";
		  thisCache.cacheName = inpName.getText().trim();
		  thisCache.latLon = thisCache.pos.toString();
		  thisCache.dateHidden = inpHidden.getText().trim();
		  int oldType=thisCache.type;
		  thisCache.type = transSelect(chcType.getInt());
		 // thisCache.saveCacheDetails(profile.dataDir); // this is redundant, because all changes affecting the details are immediately saved
		  // Now update the table
		  CacheHolder ch = thisCache; // TODO variable ch is redundant

	  /* The references have to be rebuilt if:
	   *   - the cachetype changed from addi->normal or normal->addi
	   *   - the old cachetype or the new cachetype were 'addi' and
	   *     the waypointname has changed
	   */
	  if (CacheType.isAddiWpt(ch.type)!=CacheType.isAddiWpt(oldType) ||
		 ((CacheType.isAddiWpt(ch.type) || CacheType.isAddiWpt(oldType)) &&
		 !thisCache.wayPoint.equals(oldWaypoint))) {
			  // If we changed the type to addi, check that a parent exists
			  if (CacheType.isAddiWpt(ch.type)) {
				  int idx;
				  if (ch.wayPoint.length()<5)
					  idx=-1;
				  else {
					  idx=profile.getCacheIndex("GC"+ ch.wayPoint.substring(ch.wayPoint.length() == 5?1:2));
					  if (idx<0) idx=profile.getCacheIndex("OC"+ ch.wayPoint.substring(ch.wayPoint.length() == 5?1:2));
					  if (idx<0) idx=profile.getCacheIndex("CW"+ ch.wayPoint.substring(ch.wayPoint.length() == 5?1:2));
					  if (idx<0) (new MessageBox(MyLocale.getMsg(144,"Warning"),
							  MyLocale.getMsg(734,"No main cache found for addi waypoint ")+" "+ch.wayPoint+
							  "\n"+MyLocale.getMsg(735,"Addi Waypoints must have the format xxYYYY, where xx are any 2 chars and YYYY are the main cache's chars after the GC"),FormBase.OKB)).execute();
				  }
				  profile.buildReferences(); // TODO this takes quite long -> use profile.setAddiRef instead
			  } else {
				  profile.buildReferences(); // we have to do this to release the link between the two caches
			  }
		  }
		  // set status also on addi wpts
		  ch.setAttributesToAddiWpts();
		  dirty_notes=false;
		  dirty_details=false;
		  setNeedsTableUpdate(false);
		  if (thisCache.details != null) thisCache.details.hasUnsavedChanges = false;
		  thisCache.getCacheDetails(true).hasUnsavedChanges = true;
	}

	private class TravelbugInCacheScreen extends Form {

		private DispPanel disp = new DispPanel();
		private Button btCancel;
		private TravelbugJourneyList tbjList;

		TravelbugInCacheScreen(String text, String title) {
			this.title=title;
			this.setPreferredSize(pref.myAppWidth, pref.myAppHeight);
			disp.setHtml(text);
			ScrollBarPanel sbp = new MyScrollBarPanel(disp, ScrollBarPanel.NeverShowHorizontalScrollers);
			this.addLast(sbp);
			this.addLast(btCancel = new Button(MyLocale.getMsg(3000,"Close")),CellConstants.DONTSTRETCH, CellConstants.FILL);
		}

		public void onEvent(Event ev){
			if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
				if (ev.target == btCancel){
					this.close(0);
				}
			}
		}

		// Subclassed HtmlDisplay with Pop-up menu
		private class DispPanel extends HtmlDisplay {
			MenuItem mnuPickupTB, mnuDropTB;
			MenuItem[] TBMenuItems=new MenuItem[2];
			Menu mnuPopup;
			DispPanel() {
				TBMenuItems[0]= mnuPickupTB = new MenuItem(MyLocale.getMsg(6016,"Pick up Travelbug"));
				TBMenuItems[1]= mnuDropTB = new MenuItem(MyLocale.getMsg(6017,"Drop Travelbug"));
				mnuPopup=new Menu(TBMenuItems,"");
			}
			public void penRightReleased(Point p){
				setMenu(mnuPopup);
				doShowMenu(p); // direct call (not through doMenu) is neccesary because it will exclude the whole table
			}
			public void penHeld(Point p){
				setMenu(mnuPopup);
				doShowMenu(p);
			}
			public void popupMenuEvent(Object selectedItem){
				if (selectedItem==mnuPickupTB) {
					Travelbug tb=TravelbugPickup.pickupTravelbug(thisCache.getCacheDetails(true).travelbugs);
					if (tb!=null) {
						dirty_details=true;
						// Get the list of my travelbugs
						tbjList=new TravelbugJourneyList();
						tbjList.readTravelbugsFile();
						// Add the tb to this list
						tbjList.addTbPickup(tb,Global.getProfile().name,thisCache.wayPoint);
						tbjList.saveTravelbugsFile();
						tbjList=null;
						setHtml(thisCache.getCacheDetails(true).travelbugs.toHtml());
						repaint();
						thisCache.has_bug=thisCache.getCacheDetails(true).travelbugs.size()>0;
					}
				} else if (selectedItem==mnuDropTB) {
					tbjList=new TravelbugJourneyList();
					tbjList.readTravelbugsFile();
					TravelbugList tbl=tbjList.getMyTravelbugs();
					TravelbugScreen tbs=new TravelbugScreen(tbl,MyLocale.getMsg(6017,"Drop a travelbug"),false);
					tbs.execute();
					if (tbs.selectedItem>=0) {
						Travelbug tb=tbl.getTB(tbs.selectedItem);
						thisCache.getCacheDetails(true).travelbugs.add(tb);
						tbjList.addTbDrop(tb,Global.getProfile().name,thisCache.wayPoint);
					}
					tbjList.saveTravelbugsFile();
					tbjList=null;
					thisCache.has_bug=thisCache.getCacheDetails(true).travelbugs.size()>0;
					setHtml(thisCache.getCacheDetails(true).travelbugs.toHtml());
					repaint();
					dirty_details=true;
				} else
					super.popupMenuEvent(selectedItem);
			}
		}


	}

//#############################################################################
//  NotesScreen
//#############################################################################

	/**
	*	This class displays a form to show and edit notes for a cache.
	*/
	private class NotesScreen extends Form{
		TextPad wayNotes = new TextPad();
		CacheHolderDetail thisCache = null;
		Button addDateTime;
		Button btSave = new Button(MyLocale.getMsg(127,"Save"));
		Button cancelBtn = new Button("Cancel");
		ScrollBarPanel sbp = new MyScrollBarPanel(wayNotes);

		public NotesScreen(CacheHolderDetail ch){
			this.title = "Notes";
			String imagesize = "";
			if (eve.sys.Device.isMobile() && MyLocale.getScreenWidth() >= 400) imagesize="_vga";
			addDateTime = new Button(new Picture("date_time"+imagesize+".png"));
			setPreferredSize(Global.getPref().myAppWidth, Global.getPref().myAppHeight);
			this.resizeOnSIP = true;
			thisCache = ch;
			wayNotes.setText(thisCache.cacheNotes);
			addLast(sbp.setTag(Control.TAG_SPAN, new Dimension(3,1)),CellConstants.STRETCH, (CellConstants.FILL|CellConstants.WEST));
			titleControls=new CellPanel();
			titleControls.addNext(addDateTime,CellConstants.HSTRETCH,CellConstants.HFILL);
			titleControls.addNext(cancelBtn,CellConstants.HSTRETCH,CellConstants.HFILL);
			titleControls.addLast(btSave,CellConstants.HSTRETCH,CellConstants.HFILL);
		}

		public void onEvent(Event ev){
			if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
				if (ev.target == addDateTime){
					String note = wayNotes.getText();
					Time dtm = new Time();
					dtm.getTime();
					dtm.setFormat("E dd.MM.yyyy '/' HH:mm");
					if(note.length() > 0)	note = note + "\n" + dtm.toString();
					else 	note = note + dtm.toString();
					note = note + "\n";
					wayNotes.setText(note);
				}
				if(ev.target == btSave){
					thisCache.cacheNotes = wayNotes.getText();
					thisCache.saveCacheDetails( Global.getProfile().dataDir);
					this.close(0);
				}
				if(ev.target == cancelBtn){
					if ( (!thisCache.cacheNotes.equals(wayNotes.getText())) ) {
						if ( (new MessageBox("Warning", "You will loose any changes made to the notes. Do you want to continue?"
								, MessageBox.YESB|MessageBox.NOB)).execute() == MessageBox.IDYES) {
							this.close(0);
						}
					} else this.close(0); // no changes -> exit without asking
				}
				if(ev.target == titleOK){
					if ( (!thisCache.cacheNotes.equals(wayNotes.getText())) ) {
						if ( (new MessageBox("Warning", "Save changes made to the notes?"
								, MessageBox.YESB|MessageBox.NOB)).execute() == MessageBox.IDYES) {
							thisCache.cacheNotes = wayNotes.getText();
							thisCache.saveCacheDetails( Global.getProfile().dataDir);
						}
					}
				}
			}
			super.onEvent(ev);
		}
	}


}
