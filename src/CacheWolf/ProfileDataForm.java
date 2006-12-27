package CacheWolf;


import ewe.ui.*;
import ewe.io.*;
import ewesoft.xml.*;
import ewe.util.*;
import ewe.sys.*;
import ewe.math.*;
import ewe.fx.*;

/**
*	This form displays profile specific data.
*	It allows the copying of the current centre to the profile centre
*/
public class ProfileDataForm extends Form {

	private mButton btnCancel,btnOK, btnCurrentCentre, btnProfileCentre, btnCur2Prof, btnProf2Cur, btnOldProfiles;
	Preferences pref;
	Profile profile;
	CellPanel content = new CellPanel();

	/**
	*/
	public ProfileDataForm(Preferences p, Profile prof){
		super();
		pref=p;
		profile=prof;
		
    	resizable =  false;
		/*int w=MyLocale.getScreenWidth();
		if (w>240) w=240;
		setPreferredSize(w,240);*/
		content.setText("Centre");
		content.borderStyle=CellPanel.BDR_RAISEDOUTER|CellPanel.BDR_SUNKENINNER|CellPanel.BF_RECT;
	    //defaultTags.set(this.INSETS,new Insets(2,2,2,2));		
		title = "Profile: "+profile.name;
		content.addNext(new mLabel("Current"));
		content.addLast(btnCurrentCentre=new mButton(pref.curCentrePt.toString()),HSTRETCH,HFILL|LEFT);
		content.addNext(new mLabel("      "),HSTRETCH,HFILL);
		content.addNext(btnCur2Prof=new mButton("   v   "),DONTSTRETCH,DONTFILL|LEFT);
		content.addNext(new mLabel("copy"));
		content.addLast(btnProf2Cur=new mButton("   ^   "),DONTSTRETCH,DONTFILL|RIGHT);
		content.addNext(new mLabel("Profile"));
		content.addLast(btnProfileCentre=new mButton(profile.centre.toString()),HSTRETCH,HFILL|LEFT);
		addLast(content,HSTRETCH,HFILL);
		addLast(new mLabel(""),VSTRETCH,FILL);
		addLast(btnOldProfiles=new mButton("Old Profiles"),HSTRETCH,HFILL);
		//addNext(btnCancel = new mButton(MyLocale.getMsg(1604,"Cancel")),DONTSTRETCH,DONTFILL|LEFT);
		addLast(btnOK = new mButton("OK"),DONTSTRETCH,HFILL|RIGHT);
	}
	
	/**
	*	The event handler to react to a users selection.
	*	A return value is created and passed back to the calling form
	*	while it closes itself.
	*/
	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			/*if (ev.target == btnCancel){
				close(-1);
			}*/
			if (ev.target == btnOK){
				close(1);
			}
			if (ev.target == btnCurrentCentre){
				CoordsScreen cs = new CoordsScreen();
				cs.setFields(pref.curCentrePt, CWPoint.CW);
				if (cs.execute()== CoordsScreen.IDOK){
					pref.curCentrePt.set(cs.getCoords());
					btnCurrentCentre.setText(pref.curCentrePt.toString());
				}
			}
			if (ev.target == btnProfileCentre){
				CoordsScreen cs = new CoordsScreen();
				cs.setFields(profile.centre, CWPoint.CW);
				if (cs.execute()== CoordsScreen.IDOK){
					profile.centre.set(cs.getCoords());
					btnProfileCentre.setText(profile.centre.toString());
				}
			}
			if (ev.target == btnCur2Prof){
				profile.centre.set(pref.curCentrePt);
				btnProfileCentre.setText(profile.centre.toString());
			}
			if (ev.target == btnProf2Cur){
				pref.curCentrePt.set(profile.centre);
				btnCurrentCentre.setText(pref.curCentrePt.toString());
			}
			if (ev.target == btnOldProfiles){
				ProfilesScreen pfs = new ProfilesScreen(pref);
				pfs.execute();
			}
		}
		super.onEvent(ev);
	}

}
