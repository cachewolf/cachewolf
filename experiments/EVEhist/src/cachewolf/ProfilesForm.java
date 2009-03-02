package cachewolf;

import eve.ui.*;
import eve.io.*;
import eve.sys.*;
import eve.fx.*;
import eve.fx.gui.IKeys;
import eve.ui.event.ControlEvent;


/**
*	This form displays the list of profiles for a user to choose from,
*	when CacheWolf starts up. Also allows to open a new profile.
*	ClassID = 1300
*/
public class ProfilesForm extends Form{

	// A subclassed mList which allows the highlighting of an entry
	// Maybe there is an easier way of making this happen, but I could not find it.
	private class MyList extends List {
		private int first=1;
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

		// Copied from BasicList.getScrollablePanel(), but exchanging
		// the standard scroll bar with the fontsize sensitive one.
		public ScrollablePanel getScrollablePanel() {
			dontAutoScroll = amScrolling = true;
			ScrollablePanel sp = new MyScrollBarPanel(this);
			sp.modify(0,TakeControlEvents);
			return sp;
		}
	}

	private MyList choice;
	private ScrollablePanel spMList;
	private Button btnCancel,btnNew,btnOK;
	private String baseDir;
	public String newSelectedProfile;	// This is only used if a new profile is being created

	/**
	*	Constructor to create a form to select profiles. It requires that the preferences
	*	have been loaded so that the calling parameters can be set.
	* @param baseDir The base directory which holds one subdirectory per profile
	* @param oldProfiles List of names of old profiles
	* @param selectedProfile Name of the last used profile
	*/
	public ProfilesForm(String baseDir, String selectedProfile, boolean hasNewButton){
		super();
    	resizable =  false;
		int w=MyLocale.getScreenWidth();
		if (w>240) w=240;
		int h=MyLocale.getScreenHeight();
		if (h>320) h=320;
		setPreferredSize(w,h);
	    defaultTags.set(CellConstants.TAG_INSETS,new Insets(2,2,2,2));
		title = MyLocale.getMsg(1301,"Select Profile:");
		if (hasNewButton) {
			addNext(new Label(MyLocale.getMsg(1106,"Choose profile or New")),DONTSTRETCH,DONTSTRETCH|LEFT);
			addLast(btnNew=new Button(MyLocale.getMsg(1107,"New")),HSTRETCH,HFILL|RIGHT);
		} else {
			addLast(new Label(MyLocale.getMsg(1108,"Choose profile")),DONTSTRETCH,DONTSTRETCH|LEFT);
		}

		choice=new MyList();
		// Get all subdirectories in the base directory
		File fileBaseDir=new File(baseDir);
		String[] existingProfiles=fileBaseDir.list("*.*",File.LIST_DIRECTORIES_ONLY);
        // Now add these subdirectories to the list of profiles but
        // exclude the "maps" directory which will contain the moving maps
        for (int i=0; i<existingProfiles.length; i++)
			if (!existingProfiles[i].equalsIgnoreCase("maps")) choice.addItem(existingProfiles[i]);
        // Highlight the profile that was used last
        choice.selectLastProfile(selectedProfile);
        // Add a scroll bar to the list of profiles
		spMList=choice.getScrollablePanel();
		spMList.setOptions(ScrollablePanel.NeverShowHorizontalScrollers);
		choice.setServer(spMList);
		addLast(spMList);
		addNext(btnCancel = new Button(MyLocale.getMsg(1604,"Cancel")),DONTSTRETCH,DONTFILL|LEFT);
		addNext(btnOK = new Button(MyLocale.getMsg(1605,"OK")),DONTSTRETCH,HFILL|RIGHT);
		if (choice.getListItems().length==0) btnOK.modify(Disabled,0);
		btnOK.setHotKey(0, IKeys.ENTER);
		btnCancel.setHotKey(0, IKeys.ESCAPE);
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
		}
		return "";
	}

	/**
	*	The event handler to react to a users selection.
	*	A return value is created and passed back to the calling form
	*	while it closes itself.
	*/
	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == btnCancel){
				close(-1);
			}
			if (ev.target == btnOK || ev.target == choice){
				Global.getProfile().filterActive=Filter.FILTER_INACTIVE;
				Global.getProfile().filterInverted=false;
				if (choice.getSelectedItem()!=null) {
					newSelectedProfile=choice.getSelectedItem().toString();
					close(1);
				}
			}
			if (ev.target == btnNew){
				if (NewProfileWizard.startNewProfileWizard(getFrame()) ) {
					newSelectedProfile = Global.getProfile().name;
					close(1);
				}
			}
		}
		super.onEvent(ev);
	}

}
