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

import CacheWolf.Preferences;
import CacheWolf.TravelbugScreen;
import CacheWolf.database.TravelbugList;
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
	Preferences.itself().log("Static initializer start ");
	Class swingClass = null;
	try {
	    swingClass = TravelbugJourneyScreen.class.getClassLoader().loadClass("javax.swing.JTable");
	} catch (Exception e) {
	    Preferences.itself().log("Swing not found");
	    Preferences.itself().log(e.toString());
	    // ignore!
	}
	isSwing = swingClass != null && false;
    }

    /**
     * Variante fuer Android-PDA
     */
    private static final boolean isAndroid = false;

    public static ITravelbugScreen createTravelbugScreen(TravelbugList tbl, String title, Boolean allowNew) {
	Preferences.itself().log("Mobile-Device: " + Vm.isMobile());
	Preferences.itself().log("Preference for Mobile-Device: " + Preferences.itself().mobileGUI);
	if (Vm.isMobile() && Preferences.itself().mobileGUI) {
	    try {
		Class loadClass = Reflect.getForName("CacheWolf.view.pda.PDATravelbugScreen").getReflectedClass();
		Constructor constructor = loadClass.getConstructor(new Class[] { TravelbugList.class, String.class, boolean.class });
		ITravelbugScreen result = (ITravelbugScreen) constructor.newInstance(new Object[] { tbl, title, allowNew });
		Preferences.itself().log("TBScreen successfully instantiated");
		return result;
	    } catch (Throwable e) {
		Preferences.itself().log("CacheWolf.view.pda.PDATravelbugScreen not found");
		Preferences.itself().log("Error in instantiating TravelBugScreen", e, true);
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
