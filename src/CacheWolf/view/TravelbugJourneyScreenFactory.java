package CacheWolf.view;

import java.lang.reflect.Constructor;

import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.Global;
import CacheWolf.TravelbugJourneyList;
import CacheWolf.model.TravelBugScreenModel;
import CacheWolf.view.ewe.TravelbugJourneyScreen;
import CacheWolf.view.pda.PDATravelbugJourneyScreen;
import ewe.reflect.Reflect;
import ewe.sys.Vm;
import ewe.ui.Form;

public class TravelbugJourneyScreenFactory {

	/**
	 * Variante fuer Desktop unter Java???
	 */
	private static boolean isSwing ;
	static{
		Global.getPref().log("Static initializer start ");
		Class swingClass = null;
		try {
			swingClass = TravelbugJourneyScreen.class.getClassLoader().loadClass("javax.swing.JTable");
		} catch (Exception e) {
			Global.getPref().log("Swing not found");
			Global.getPref().log(e.toString());
			//ignore!
		}
		isSwing = swingClass!=null;
	}

	/**
	 * Variante fuer Android-PDA
	 */
	private static final boolean isAndroid = false;

	public static Form createTravelbugJourneyScreen() {
		TravelBugScreenModel model = new TravelBugScreenModel();
		model.onlyLogged = Global.getPref().travelbugShowOnlyNonLogged;
		int curCacheNo = Global.mainTab.tbP.getSelectedCache();
		CacheDB cacheDB = Global.getProfile().cacheDB;
		CacheHolder ch = cacheDB.get(curCacheNo);
		model.actualCache = ch;
		TravelbugJourneyList myTravelbugJourneys = new TravelbugJourneyList();
		myTravelbugJourneys.readTravelbugsFile();

		model.allTravelbugJourneys = myTravelbugJourneys;

		Global.getPref().log("Mobile-Device: " + Vm.isMobile());
		Global.getPref().log("Preference for Mobile-Device: " + Global.getPref().mobileGUI);
		if (Vm.isMobile() && Global.getPref().mobileGUI) {
			try {
				Class loadClass = Reflect.getForName("CacheWolf.view.pda.PDATravelbugJourneyScreen").getReflectedClass();
				Constructor constructor = loadClass.getConstructor(new Class[]{model.getClass()});
				Form result = (Form) constructor.newInstance(new Object[] {model});
				Global.getPref().log("TBScreen successfully instantiated");
				return result;
			} catch (Throwable e) {
				Global.getPref().log("CacheWolf.view.pda.PDATravelbugJourneyScreen not found");
				Global.getPref().log("Error in instantiating TravelBugJourneyScreen", e, true);
				e.printStackTrace();
				//ignore?? VM on WinPC seems to have no classloader
				return new PDATravelbugJourneyScreen(model);
			}
		} else if (isSwing) {
			return new TravelbugJourneyScreen(model);
		} else if (isAndroid) {
			return new TravelbugJourneyScreen(model);
		} else {
			return new TravelbugJourneyScreen(model);
		}
	}
}
