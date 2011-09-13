package CacheWolf.view;

import java.lang.reflect.Constructor;

import CacheWolf.Global;
import CacheWolf.TravelbugList;
import CacheWolf.TravelbugScreen;
import CacheWolf.view.ewe.TravelbugJourneyScreen;
import CacheWolf.view.pda.PDATravelbugScreen;
import ewe.reflect.Reflect;
import ewe.sys.Vm;

public class TravelBugScreenFactory {

	/**
	 * Variante fuer Desktop unter Java???
	 */
	private static boolean isSwing;
	static {
		Global.getPref().log("Static initializer start ");
		Class swingClass = null;
		try {
			swingClass = TravelbugJourneyScreen.class.getClassLoader().loadClass("javax.swing.JTable");
		} catch (Exception e) {
			Global.getPref().log("Swing not found");
			Global.getPref().log(e.toString());
			// ignore!
		}
		isSwing = swingClass != null && false;
	}

	/**
	 * Variante fuer Android-PDA
	 */
	private static final boolean isAndroid = false;

	public static ITravelbugScreen createTravelbugScreen(TravelbugList tbl, String title, Boolean allowNew) {
		Global.getPref().log("Mobile-Device: " + Vm.isMobile());
		Global.getPref().log("Preference for Mobile-Device: " + Global.getPref().mobileGUI);
		if (Vm.isMobile() && Global.getPref().mobileGUI) {
			try {
				Class loadClass = Reflect.getForName("CacheWolf.view.pda.PDATravelbugScreen").getReflectedClass();
				Constructor constructor = loadClass.getConstructor(new Class[] { TravelbugList.class, String.class, boolean.class });
				ITravelbugScreen result = (ITravelbugScreen) constructor.newInstance(new Object[] { tbl, title, allowNew });
				Global.getPref().log("TBScreen successfully instantiated");
				return result;
			} catch (Throwable e) {
				Global.getPref().log("CacheWolf.view.pda.PDATravelbugScreen not found");
				Global.getPref().log("Error in instantiating TravelBugScreen", e, true);
				e.printStackTrace();
				// ignore?? VM on WinPC seems to have no classloader
				return new PDATravelbugScreen(tbl, title, allowNew.booleanValue());
			}
		} else if (isSwing) {
			throw new InstantiationError("No Swing GUI available");
			// return new TravelbugJourneyScreen(model);
		} else if (isAndroid) {
			throw new InstantiationError("No Android GUI available");
			// return new TravelbugJourneyScreen(model);
		} else {
			return new TravelbugScreen(tbl, title, allowNew.booleanValue());
		}
	}

}
