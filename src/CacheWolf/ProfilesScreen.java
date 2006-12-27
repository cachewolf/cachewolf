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
	Preferences pref;
	mInput name1,dir1;
	mInput name2,dir2;
	mInput name3,dir3;
	mInput name4,dir4;
	mButton but1,but2,but3,but4,btnCoords1,btnCoords2,btnCoords3,btnCoords4;
	CellPanel content = new CellPanel();
	ScrollBarPanel scp;
	
	/**
	*	Constructor!
	*	Existing profile values are assigned upon set-up.
	*/
	public ProfilesScreen(Preferences p){
		scp = new ScrollBarPanel(content);
		pref = p;
		setMinimumSize(240,240);
		setMaximumSize(300,400);

		this.title = MyLocale.getMsg(1100,"Profiles");
		
		//content.addLast(new mLabel(MyLocale.getMsg(1101,"Profile 1")));
		content.addNext(new mLabel(MyLocale.getMsg(1101,"Profile 1")));
		content.addLast(btnCoords1 = new mButton(pref.lats[0] + " " + pref.longs[0]));
		content.addNext(new mLabel("Name"));
		content.addLast(name1 = new mInput());
		content.addNext(new mLabel("Dir"));
		content.addNext(dir1 = new mInput());
		content.addLast(but1 = new mButton(MyLocale.getMsg(1105,"Browse")));
		name1.setText(pref.profiles[0]);
		dir1.setText(pref.profdirs[0]);
		
		//content.addLast(new mLabel(MyLocale.getMsg(1102,"Profile 1")));
		content.addNext(new mLabel(MyLocale.getMsg(1102,"Profile 2")));
		content.addLast(btnCoords2 = new mButton(pref.lats[1] + " " + pref.longs[1]));
		content.addNext(new mLabel("Name"));
		content.addLast(name2 = new mInput());
		content.addNext(new mLabel("Dir"));
		content.addNext(dir2 = new mInput());
		content.addLast(but2 = new mButton(MyLocale.getMsg(1105,"Browse")));
		name2.setText(pref.profiles[1]);
		dir2.setText(pref.profdirs[1]);
		
		//content.addLast(new mLabel(MyLocale.getMsg(1103,"Profile 1")));
		content.addNext(new mLabel(MyLocale.getMsg(1103,"Profile 3")));
		content.addLast(btnCoords3 = new mButton(pref.lats[2] + " " + pref.longs[2]));
		content.addNext(new mLabel("Name"));
		content.addLast(name3 = new mInput());
		content.addNext(new mLabel("Dir"));
		content.addNext(dir3 = new mInput());
		content.addLast(but3 = new mButton(MyLocale.getMsg(1105,"Browse")));
		name3.setText(pref.profiles[2]);
		dir3.setText(pref.profdirs[2]);
		
		//content.addLast(new mLabel(MyLocale.getMsg(1104,"Profile 1")));
		content.addNext(new mLabel(MyLocale.getMsg(1104,"Profile 4")));
		content.addLast(btnCoords4 = new mButton(pref.lats[3] + " " + pref.longs[3]));
		content.addNext(new mLabel("Name"));
		content.addLast(name4 = new mInput());
		content.addNext(new mLabel("Dir"));
		content.addNext(dir4 = new mInput());
		content.addLast(but4 = new mButton(MyLocale.getMsg(1105,"Browse")));
		name4.setText(pref.profiles[3]);
		dir4.setText(pref.profdirs[3]);
		content.addNext(new mLabel(""));
		content.addNext(cancelB = new mButton(MyLocale.getMsg(614,"Cancel")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		content.addLast(applyB = new mButton(MyLocale.getMsg(615,"Apply")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.EAST));
		
		this.addLast(scp.getScrollablePanel(), CellConstants.STRETCH, CellConstants.FILL);

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
				pref.profiles[0] = name1.getText();
				pref.profdirs[0] = dir1.getText();
				pref.profiles[1] = name2.getText();
				pref.profdirs[1] = dir2.getText();
				pref.profiles[2] = name3.getText();
				pref.profdirs[2] = dir3.getText();
				pref.profiles[3] = name4.getText();
				pref.profdirs[3] = dir4.getText();
				pref.savePreferences();
				pref.dirty = true;
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
				FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, pref.baseDir);
				fc.setTitle(MyLocale.getMsg(616,"Select data directory"));
				if(fc.execute() != IDCANCEL)	{
					dir1.setText(fc.getChosen()+"/");
				}
			}
			if(ev.target == but2){
				FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, pref.baseDir);
				fc.setTitle(MyLocale.getMsg(616,"Select data directory"));
				if(fc.execute() != IDCANCEL)	{
					dir2.setText(fc.getChosen()+"/");
				}
			}
			if(ev.target == but3){
				FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, pref.baseDir);
				fc.setTitle(MyLocale.getMsg(616,"Select data directory"));
				if(fc.execute() != IDCANCEL)	{
					dir3.setText(fc.getChosen()+"/");
				}
			}
			if(ev.target == but4){
				FileChooser fc = new FileChooser(FileChooser.DIRECTORY_SELECT, pref.baseDir);
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
		if (pref.lats[idx] != null &&  pref.longs[idx] != null 
		&& (pref.lats[idx].length() > 9) && (pref.longs[idx].length() > 9)) {
			profCoords.set(pref.lats[idx] + " " + pref.longs[idx], CWPoint.CW);
		}
		cs.setFields(profCoords, CWPoint.DMM);
		if (cs.execute()== IDOK){
			profCoords = cs.getCoords();
			// remember coords for profile ...
			pref.lats[idx] = profCoords.getNSLetter() + " " + profCoords.getLatDeg(CWPoint.DMM) + " " +
							  profCoords.getLatMin(CWPoint.DMM);
			pref.longs[idx] = profCoords.getEWLetter() + " " + profCoords.getLonDeg(CWPoint.DMM) + " " +
			  				   profCoords.getLonMin(CWPoint.DMM);
			btn.setText((pref.lats[idx] + " " + pref.longs[idx]));
			// .. and set current preferences
			pref.curCentrePt.set(profCoords);
		}
		
	}
}
