package CacheWolf;

import ewe.ui.*;
import ewe.io.*;
import ewesoft.xml.*;
import ewe.util.*;
import ewe.sys.*;
import ewe.math.*;
import ewe.fx.*;

/**
*	This form displays the list of profiles for a user to choose from,
*	when CacheWolf starts up. Also allows to open a new profile.
*	ClassID = 1300
*/
public class ProfilesForm extends Form{

	// A subclassed mList which allows the highlighting of an entry
	// Maybe there is an easier way of making this happen, but I could not find it.
	private class MyList extends mList {
		private int first=1;
		private mList ml;
		private int select;
	    
		public MyList() {
			super(1,1,false);
		}
		
		public void selectLastProfile(String selectedItem) {
			selectItem(selectedItem);
			select=getSelectedIndex(0);
		}

		public void doPaint(Graphics gr,Rect area) {
			if (first==1) { 
				first=0;
				selectAndView(select);
				makeVisible(select); 
			}
			super.doPaint(gr,area);
		}
	}

	private MyList choice;
	private ScrollablePanel spMList;
	private mButton btnCancel,btnNew,btnOK;
	private String baseDir;
	public String newSelectedProfile;	// This is only used if a new profile is being created

	/**
	*	Constructor to create a form to select profiles. It requires that the preferences 
	*	have been loaded so that the calling parameters can be set.
	* @param baseDir The base directory which holds one subdirectory per profile
	* @param oldProfiles List of names of old profiles
	* @param selectedProfile Name of the last used profile
	*/
	public ProfilesForm(String baseDir, String[] oldProfiles,String selectedProfile, boolean hasNewButton){
		super();
    	resizable =  false;
		int w=MyLocale.getScreenWidth();
		if (w>240) w=240;
		setPreferredSize(w,240);
	    defaultTags.set(this.INSETS,new Insets(2,2,2,2));		
		title = MyLocale.getMsg(1301,"Select Profile:");
		if (hasNewButton) {
			addNext(new mLabel(MyLocale.getMsg(1106,"Choose profile or New")),DONTSTRETCH,DONTSTRETCH|LEFT);
			addLast(btnNew=new mButton(MyLocale.getMsg(1107,"New")),HSTRETCH,HFILL|RIGHT);
		} else {
			addLast(new mLabel(MyLocale.getMsg(1108,"Choose profile")),DONTSTRETCH,DONTSTRETCH|LEFT);
		}
		
		choice=new MyList();
        // First add the old style profiles (stored in pref.xml)
        if (oldProfiles[0].length()>0) choice.addItem(oldProfiles[0]);
        if (oldProfiles[1].length()>0) choice.addItem(oldProfiles[1]);
        if (oldProfiles[2].length()>0) choice.addItem(oldProfiles[2]);
        if (oldProfiles[3].length()>0) choice.addItem(oldProfiles[3]);
        if (choice.itemsSize()>0) choice.addItem("-");
		// Get all subdirectories in the base directory
		File fileBaseDir=new File(baseDir);
		String[] existingProfiles=fileBaseDir.list("*",FileBase.LIST_DIRECTORIES_ONLY);
        // Now add these subdirectories to the list of profiles but
        // exclude the "maps" directory which will contain the moving maps
        for (int i=0; i<existingProfiles.length; i++) 
			if (!existingProfiles[i].equals("maps")) choice.addItem(existingProfiles[i]);
        // Highlight the profile that was used last
        choice.selectLastProfile(selectedProfile);
        // Add a scroll bar to the list of profiles
		spMList=choice.getScrollablePanel();
		spMList.setOptions(ScrollablePanel.NeverShowHorizontalScrollers);
		choice.setServer(spMList);
		addLast(spMList);
		addNext(btnCancel = new mButton(MyLocale.getMsg(1604,"Cancel")),DONTSTRETCH,DONTFILL|LEFT);
		addNext(btnOK = new mButton(MyLocale.getMsg(1605,"OK")),DONTSTRETCH,HFILL|RIGHT);
		this.baseDir=baseDir;
		choice.takeFocus(Control.ByKeyboard);
	}
	
	/**
	 * Ask for a new profile directory. If it exists, cancel. If it does not exist, create it
	 * @return Name of directory (just the part below baseDir)
	 */
	public String createNewProfile() {
		NewProfileForm f=new NewProfileForm(baseDir);
	    int code=f.execute(getFrame(), Gui.CENTER_FRAME);
		if (code==0) { 
			 return f.profileDir;
		} else
			 return "";
	}
	
	private boolean first=true;
	/**
	*	The event handler to react to a users selection.
	*	A return value is created and passed back to the calling form
	*	while it closes itself.
	*/
	public void onEvent(Event ev){
		// Set focus on the choice control initially
		if (ev instanceof ControlEvent && ev.type == ControlEvent.FOCUS_IN && first) {
			first=false;  // There must be a better way to set the focus to the choice control ??!?
			choice.takeFocus(Control.ByKeyboard);
		}
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == btnCancel){
				close(-1);
			}
			if (ev.target == btnOK || ev.target == choice){
				newSelectedProfile=choice.getSelectedItem().toString();
				close(1);
			}
			if (ev.target == btnNew){
				newSelectedProfile=createNewProfile();
				if (newSelectedProfile.length()>0) close(1);
			}
		}
		super.onEvent(ev);
	}

}
