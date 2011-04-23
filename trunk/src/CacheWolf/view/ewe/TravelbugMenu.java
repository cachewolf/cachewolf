package CacheWolf.view.ewe;

import CacheWolf.Global;
import CacheWolf.MyLocale;
import CacheWolf.Travelbug;
import CacheWolf.TravelbugJourney;
import CacheWolf.TravelbugList;
import CacheWolf.TravelbugPickup;
import CacheWolf.imp.SpiderGC;
import CacheWolf.model.TravelBugScreenModel;
import CacheWolf.utils.CWWrapper;
import ewe.fx.Rect;
import ewe.sys.Vm;
import ewe.ui.Event;
import ewe.ui.Menu;
import ewe.ui.MenuBar;
import ewe.ui.MenuEvent;
import ewe.ui.MenuItem;
import ewe.ui.PullDownMenu;

/**
 * Controller for the {@link TravelbugList}. Preferrably used by
 * {@link TravelbugJourneyScreen}
 * 
 * @author andi
 * 
 */
public class TravelbugMenu extends MenuBar {
	private MenuItem mnuNewTB;
	private MenuItem mnuDeleteTB;
	private MenuItem mnuGetMission;
	private MenuItem mnuOpenOnline;
	private MenuItem mnuDropTB;
	private MenuItem mnuPickupTB;
	private MenuItem mnuDeleteTBs;

	/**
	 * The model controlled by this
	 */
	/**
	 * The View displaying the model for this
	 */
	public TravelbugJourneyScreen view;
	private TravelBugScreenModel model;

	
	public TravelbugMenu(TravelBugScreenModel model) {
		this.model = model;
		MenuItem[] TBMenuItems = new MenuItem[9];
		TBMenuItems[0] = mnuPickupTB = new MenuItem(MyLocale.getMsg(6040, "Pick up TB from current cache"));
		TBMenuItems[1] = mnuDropTB = new MenuItem(MyLocale.getMsg(6041, "Drop TB in cache"));
		TBMenuItems[2] = new MenuItem("-");
		TBMenuItems[3] = mnuNewTB = new MenuItem(MyLocale.getMsg(6042, "New Travelbug"));
		TBMenuItems[4] = mnuDeleteTB = new MenuItem(MyLocale.getMsg(6043, "Delete Travelbug"));
		TBMenuItems[5] = new MenuItem("-");
		TBMenuItems[6] = mnuGetMission = new MenuItem(MyLocale.getMsg(6044, "Get Mission"));
		TBMenuItems[7] = mnuOpenOnline = new MenuItem(MyLocale.getMsg(6045, "Open on-line"));
		TBMenuItems[8] = new MenuItem("-");
		// A second pop-up menu with only one entry, if a range of rows is
		// selected
		MenuItem[] TBMenuItemsDel = new MenuItem[1];
		TBMenuItemsDel[0] = mnuDeleteTBs = new MenuItem(MyLocale.getMsg(6047, "Delete selected Travelbugs"));
		// mnuDropTB.modifiers |= MenuItem.Disabled;
		// mnuDeleteTB.modifiers |= MenuItem.Disabled;
		// mnuGetMission.modifiers |= MenuItem.Disabled;
		// mnuOpenOnline.modifiers |= MenuItem.Disabled;

		this.addMenu(new PullDownMenu(MyLocale.getMsg(120, "Application"), new Menu(TBMenuItems, null)));

	}

	public void onEvent(Event event) {
		if (event instanceof MenuEvent) {
			MenuEvent mev = (MenuEvent) event;
			if (mev.selectedItem == mnuPickupTB) {
				Travelbug tb = TravelbugPickup.pickupTravelbug(view.tblSrcCache);
				if (tb != null) {
					view.chDmodified = true;
					model.allTravelbugJourneys.addTbPickup(tb, Global.getProfile().name, view.waypoint);
					view.modTbJourneyList.numRows = model.allTravelbugJourneys.size();
					view.repaint();
				}

			} else if (mev.selectedItem == mnuDropTB) {
				if (view.selectedRow >= 0 && view.selectedRow < view.modTbJourneyList.numRows) {
					Travelbug tb = model.allTravelbugJourneys.getTBJourney(view.selectedRow).getTb();
					view.chD.Travelbugs.add(tb);
					model.allTravelbugJourneys.addTbDrop(tb, Global.getProfile().name, view.waypoint);
					view.chDmodified = true;
					view.ch.setHas_bugs(true);
				}
				view.repaint();
			} else if (mev.selectedItem == mnuNewTB) {
				TravelbugJourney tbj = new TravelbugJourney("New");
				tbj.setFromProfile(Global.getProfile().name);
				tbj.setFromWaypoint(view.waypoint);
				model.allTravelbugJourneys.add(tbj);
				view.modTbJourneyList.numRows = model.allTravelbugJourneys.size();
				// view.cursorTo(view.tblMyTravelbugJourneys.size()-1,1,true);
				view.repaint();
			} else if (mev.selectedItem == mnuDeleteTB && view.selectedRow >= 0) {
				model.allTravelbugJourneys.remove(view.selectedRow);
				view.modTbJourneyList.numRows = model.allTravelbugJourneys.size();
				if (view.selectedRow > 0) {
					// cursorTo(view.selectedRow-1,0,true);
				} else {
					// view.modTbJourneyList.showFields(new
					// TravelbugJourney(""));
				}
				view.repaint();
			}
			/*
			 * Delete a group of travelbugs which have been marked with
			 * Shift-Click
			 */
			if (mev.selectedItem == mnuDeleteTBs) {
				Rect sel = view.tcTbJourneyList.getSelection(null);
				for (int i = 0; i < sel.height; i++)
					model.allTravelbugJourneys.remove(sel.y);
				view.modTbJourneyList.numRows = model.allTravelbugJourneys.size();
				if (sel.y < view.modTbJourneyList.numRows) {
					// cursorTo(sel.y,0,true);
				} else {
					view.modTbJourneyList.showFields(new TravelbugJourney(""));
				}
				view.repaint();
			} else if (mev.selectedItem == mnuGetMission && view.selectedRow > -1) {
				TravelbugJourney tbj = model.allTravelbugJourneys.getTBJourney(view.selectedRow);
				SpiderGC spider = new SpiderGC(Global.getPref(), Global.getProfile());
				Vm.showWait(true);

				// if we have an ID, get mission by ID
				if (tbj.getTb().getGuid().length() != 0) {
					tbj.getTb().setMission(spider.getBugMissionByGuid(tbj.getTb().getGuid()));
				} else {
					// try to get mission and name by tracking number
					boolean suceeded = false;
					if (tbj.getTb().getTrackingNo().length() != 0) {
						suceeded = spider.getBugMissionAndNameByTrackNr(tbj.getTb());
					}
					// if this has't worked, try to get ID by name
					if (!suceeded) {
						tbj.getTb().setGuid(spider.getBugId(tbj.getTb().getName().trim()));
						// if we have an ID now, get mission by ID
						if (tbj.getTb().getGuid().length() != 0) {
							tbj.getTb().setMission(spider.getBugMissionByGuid(tbj.getTb().getGuid()));
						}
					}
				}
			} else if (mev.selectedItem == mnuOpenOnline && view.selectedRow >= 0) {
				TravelbugJourney tbj = model.allTravelbugJourneys.getTBJourney(view.selectedRow);
				SpiderGC spider = new SpiderGC(Global.getPref(), Global.getProfile());
				Vm.showWait(true);
				// First check whether ID is set, if not get it
				if (tbj.getTb().getGuid().length() == 0)
					tbj.getTb().setGuid(spider.getBugId(tbj.getTb().getName()));
				if (tbj.getTb().getGuid().length() != 0) {
					Vm.showWait(false);
					try {
						String s;
						if (tbj.getTb().getGuid().length() > 10)
							s = "http://www.geocaching.com/track/details.aspx?guid=" + tbj.getTb().getGuid();
						else
							s = "http://www.geocaching.com/track/details.aspx?id=" + tbj.getTb().getGuid();

						CWWrapper.exec(Global.getPref().browser, s);
						Global.getPref().log("Executed: \"" + Global.getPref().browser + "\" \"" + s + "\"");
					} catch (Exception ioex) {
						Global.getPref().log("Ignored Exception", ioex, true);
					}
				}
			}

		}
	}
}
