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

import CacheWolf.MainForm;
import CacheWolf.MainTab;
import CacheWolf.Preferences;
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.TravelbugJourneyList;
import CacheWolf.model.TravelBugJourneyScreenModel;
import CacheWolf.view.ewe.TravelbugJourneyScreen;
import CacheWolf.view.pda.PDATravelbugJourneyScreen;
import ewe.reflect.Reflect;
import ewe.sys.Vm;
import ewe.ui.Form;

import java.lang.reflect.Constructor;

public class TravelbugJourneyScreenFactory {

    /**
     * Variante fuer Android-PDA
     */
    private static final boolean isAndroid = false;
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
            //ignore!
        }
        isSwing = swingClass != null;
        isSwing = false;
    }

    public static Form createTravelbugJourneyScreen() {
        TravelBugJourneyScreenModel model = new TravelBugJourneyScreenModel();
        model.onlyLogged = Preferences.itself().travelbugShowOnlyNonLogged;
        int curCacheNo = MainTab.itself.tablePanel.getSelectedCache();
        CacheDB cacheDB = MainForm.profile.cacheDB;
        CacheHolder ch = cacheDB.get(curCacheNo);
        model.actualCache = ch;
        TravelbugJourneyList myTravelbugJourneys = new TravelbugJourneyList();
        myTravelbugJourneys.readTravelbugsFile();

        model.allTravelbugJourneys = myTravelbugJourneys;

        Preferences.itself().log("Mobile-Device: " + Vm.isMobile());
        Preferences.itself().log("Preference for Mobile-Device: " + Preferences.itself().mobileGUI);
        if (Vm.isMobile() && Preferences.itself().mobileGUI) {
            try {
                Class loadClass = Reflect.getForName("CacheWolf.view.pda.PDATravelbugJourneyScreen").getReflectedClass();
                Constructor constructor = loadClass.getConstructor(new Class[]{model.getClass()});
                Form result = (Form) constructor.newInstance(new Object[]{model});
                Preferences.itself().log("TBScreen successfully instantiated");
                return result;
            } catch (Throwable e) {
                Preferences.itself().log("CacheWolf.view.pda.PDATravelbugJourneyScreen not found");
                Preferences.itself().log("Error in instantiating TravelBugJourneyScreen", e, true);
                e.printStackTrace();
                //ignore?? VM on WinPC seems to have no classloader
                return new PDATravelbugJourneyScreen(model);
            }
        } else if (isSwing) {
            throw new InstantiationError("No Swing GUI available");
        } else if (isAndroid) {
            throw new InstantiationError("No Android GUI available");
        } else {
            return new TravelbugJourneyScreen(model);
        }
    }
}
