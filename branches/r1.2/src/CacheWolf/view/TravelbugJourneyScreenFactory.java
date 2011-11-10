/*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
	See http://www.cachewolf.de/ for more information.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package CacheWolf.view;

import java.lang.reflect.Constructor;

import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.Global;
import CacheWolf.TravelbugJourneyList;
import CacheWolf.model.TravelBugJourneyScreenModel;
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
		isSwing = false;
	}

	/**
	 * Variante fuer Android-PDA
	 */
	private static final boolean isAndroid = false;

	public static Form createTravelbugJourneyScreen() {
		TravelBugJourneyScreenModel model = new TravelBugJourneyScreenModel();
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
		} else if ( isSwing) {
			throw new InstantiationError ("No Swing GUI available");
		} else if (isAndroid) {
			throw new InstantiationError ("No Android GUI available");
		} else {
			return new TravelbugJourneyScreen(model);
		}
	}
}
