package CacheWolf;

import CacheWolf.navi.TransformCoordinates;
import ewe.ui.FormBase;
import ewe.ui.Gui;

public class NewProfileWizard {

	static public boolean startNewProfileWizard(ewe.ui.Frame parent) {
		if (Global.mainTab != null) Global.mainTab.saveUnsavedChanges(true);
		Preferences pref = Global.getPref();
		NewProfileForm f=new NewProfileForm(pref.absoluteBaseDir);
		int code=f.execute(parent, Gui.CENTER_FRAME);
		if (code==0) {
			Profile profile = Global.getProfile();
			profile.clearProfile(); 
			pref.lastProfile=profile.name=f.profileDir;
			pref.savePreferences(); // Remember that this was the last profile used
			profile.dataDir=pref.absoluteBaseDir+f.profileDir+"/";
			
			CoordsScreen cs = new CoordsScreen();
			cs.setFields(new CWPoint(), TransformCoordinates.CW);
			if (cs.execute() == FormBase.IDOK) {
				profile.setCenterCoords(cs.getCoords());
			}
			Global.mainForm.setTitle("Cachewolf "+Version.getRelease()+" - "+profile.name);
			profile.notifyUnsavedChanges(true);
		}
		f.close(0);
		return (code == 0);
	}
}
