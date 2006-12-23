package CacheWolf;

import ewe.sys.*;
import ewe.ui.*;
import ewe.filechooser.*;

/**
*	This form displays fields to enter up to 4 different profile configurations.
*	A profile consists of the name an directory entered here as well as the current
*	home coordinates set up in the preferences screen.
*	Class ID = 1100
*/
public class ProfilesScreen extends Form {
	mButton cancelB, applyB;
	Preferences myPrefs;
	mInput name1,dir1;
	mInput name2,dir2;
	mInput name3,dir3;
	mInput name4,dir4;
	mButton but1,but2,but3,but4,btnCoords1,btnCoords2,btnCoords3,btnCoords4;
	
	/**
	*	Constructor!
	*	Existing profile values are assigned upon set-up.
	*/
	public ProfilesScreen(Preferences myp){
		myPrefs = myp;
		this.title = MyLocale.getMsg(1100,"Profiles");
		
		//this.addLast(new mLabel(MyLocale.getMsg(1101,"Profile 1")));
		this.addNext(new mLabel(MyLocale.getMsg(1101,"Profile 1")));
		this.addLast(btnCoords1 = new mButton(myPrefs.lats[0] + " " + myPrefs.longs[0]));
		this.addNext(new mLabel("Name"));
		this.addLast(name1 = new mInput());
		this.addNext(new mLabel("Dir"));
		this.addNext(dir1 = new mInput());
		this.addLast(but1 = new mButton(MyLocale.getMsg(1105,"Browse")));
		name1.setText(myPrefs.profiles[0]);
		dir1.setText(myPrefs.profdirs[0]);
		
		//this.addLast(new mLabel(MyLocale.getMsg(1102,"Profile 1")));
		this.addNext(new mLabel(MyLocale.getMsg(1102,"Profile 2")));
		this.addLast(btnCoords2 = new mButton(myPrefs.lats[1] + " " + myPrefs.longs[1]));
		this.addNext(new mLabel("Name"));
		this.addLast(name2 = new mInput());
		this.addNext(new mLabel("Dir"));
		this.addNext(dir2 = new mInput());
		this.addLast(but2 = new mButton(MyLocale.getMsg(1105,"Browse")));
		name2.setText(myPrefs.profiles[1]);
		dir2.setText(myPrefs.profdirs[1]);
		
		//this.addLast(new mLabel(MyLocale.getMsg(1103,"Profile 1")));
		this.addNext(new mLabel(MyLocale.getMsg(1103,"Profile 3")));
		this.addLast(btnCoords3 = new mButton(myPrefs.lats[2] + " " + myPrefs.longs[2]));
		this.addNext(new mLabel("Name"));
		this.addLast(name3 = new mInput());
		this.addNext(new mLabel("Dir"));
		this.addNext(dir3 = new mInput());
		this.addLast(but3 = new mButton(MyLocale.getMsg(1105,"Browse")));
		name3.setText(myPrefs.profiles[2]);
		dir3.setText(myPrefs.profdirs[2]);
		
		//this.addLast(new mLabel(MyLocale.getMsg(1104,"Profile 1")));
		this.addNext(new mLabel(MyLocale.getMsg(1104,"Profile 4")));
		this.addLast(btnCoords4 = new mButton(myPrefs.lats[3] + " " + myPrefs.longs[3]));
		this.addNext(new mLabel("Name"));
		this.addLast(name4 = new mInput());
		this.addNext(new mLabel("Dir"));
		this.addNext(dir4 = new mInput());
		this.addLast(but4 = new mButton(MyLocale.getMsg(1105,"Browse")));
		name4.setText(myPrefs.profiles[3]);
		dir4.setText(myPrefs.profdirs[3]);
		
		this.addNext(cancelB = new mButton(MyLocale.getMsg(614,"Cancel")),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		this.addLast(applyB = new mButton(MyLocale.getMsg(615,"Apply")),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
	}
	
	/**
	*	Event handler to react to a users input.
	*	First of all the form catches if a user enteres or changes any of the
	*	input fields.
	*	When the user closes the form by hitting the Apply - Button the form checks
	*	if any of the fields have been changed. If so the values are updated in preferences
	*	and the currently set up home coordinates are associated with the updated
	*	profile.
	*	The form the requests the profiles to be saved and then closes itself.
	*/
	public void onEvent(Event ev){
		
		
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == cancelB){
				this.close(0);
			}
			if (ev.target == applyB){
				myPrefs.profiles[0] = name1.getText();
				myPrefs.profdirs[0] = dir1.getText();
				myPrefs.profiles[1] = name2.getText();
				myPrefs.profdirs[1] = dir2.getText();
				myPrefs.profiles[2] = name3.getText();
				myPrefs.profdirs[2] = dir3.getText();
				myPrefs.profiles[3] = name4.getText();
				myPrefs.profdirs[3] = dir4.getText();
				myPrefs.savePreferences();
				myPrefs.dirty = true;
				this.close(0);
			}

			if(ev.target == btnCoords1){
				getCoords((mButton)ev.target,0);
			}

			if(ev.target == btnCoords2){
				getCoords((mButton)ev.target,1);
			}
			
			if(ev.target == btnCoords3){
				getCoords((mButton)ev.target,2);
			}
			
			if(ev.target == btnCoords4){
				getCoords((mButton)ev.target,3);
			}
			
			if(ev.target == but1){
				FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, myPrefs.mydatadir);
				fc.setTitle(MyLocale.getMsg(616,"Select data directory"));
				if(fc.execute() != IDCANCEL)	{
					dir1.setText(fc.getChosen()+"/");
				}
			}
			if(ev.target == but2){
				FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, myPrefs.mydatadir);
				fc.setTitle(MyLocale.getMsg(616,"Select data directory"));
				if(fc.execute() != IDCANCEL)	{
					dir2.setText(fc.getChosen()+"/");
				}
			}
			if(ev.target == but3){
				FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, myPrefs.mydatadir);
				fc.setTitle(MyLocale.getMsg(616,"Select data directory"));
				if(fc.execute() != IDCANCEL)	{
					dir3.setText(fc.getChosen()+"/");
				}
			}
			if(ev.target == but4){
				FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, myPrefs.mydatadir);
				fc.setTitle(MyLocale.getMsg(616,"Select data directory"));
				if(fc.execute() != IDCANCEL)	{
					dir4.setText(fc.getChosen()+"/");
				}
			}
			
		}
		super.onEvent(ev);
	}
	
	private void getCoords(mButton btn,int idx){
		CoordsScreen cs = new CoordsScreen();
		CWPoint profCoords = new CWPoint();
		//minimum check if there is some Data in the prefs
		if (myPrefs.lats[idx] != null &&  myPrefs.longs[idx] != null 
		&& (myPrefs.lats[idx].length() > 9) && (myPrefs.longs[idx].length() > 9)) {
			profCoords.set(myPrefs.lats[idx] + " " + myPrefs.longs[idx], CWPoint.CW);
		}
		cs.setFields(profCoords, CWPoint.DMM);
		if (cs.execute()== IDOK){
			profCoords = cs.getCoords();
			// remember coords for profile ...
			myPrefs.lats[idx] = profCoords.getNSLetter() + " " + profCoords.getLatDeg(CWPoint.DMM) + " " +
							  profCoords.getLatMin(CWPoint.DMM);
			myPrefs.longs[idx] = profCoords.getEWLetter() + " " + profCoords.getLonDeg(CWPoint.DMM) + " " +
			  				   profCoords.getLonMin(CWPoint.DMM);
			btn.setText((myPrefs.lats[idx] + " " + myPrefs.longs[idx]));
			// .. and set current preferences
			myPrefs.curCentrePt.set(profCoords);
		}
		
	}
}
