    /*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
    See http://developer.berlios.de/projects/cachewolf/
    for more information.
    Contact: 	bilbowolf@users.berlios.de
    			kalli@users.berlios.de

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
package CacheWolf;

import CacheWolf.utils.FileBugfix;
import ewe.io.File;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.IKeys;
import ewe.ui.MessageBox;
import ewe.ui.TextMessage;
import ewe.ui.mButton;
import ewe.ui.mInput;

public class NewProfileForm extends Form {
	private mButton btnCancel,btnOK;
	private mInput inpDir;
	private TextMessage description;
	public String profileDir;
	private String baseDir;
	//private Profile profile;
	
	public NewProfileForm (String baseDir) {
        super();
		//profile=prof;
        title = MyLocale.getMsg(1111,"Create new profile:");
		addLast(inpDir=new mInput(MyLocale.getMsg(1112,"New profile name")),HSTRETCH,HFILL|LEFT);
		description = new TextMessage(MyLocale.getMsg(1123,"Click 'Next' to define the center coordinates for this profile."));
		description.setPreferredSize(240, -1);
		addLast(description,HSTRETCH,HFILL|LEFT);
		btnCancel = new mButton(MyLocale.getMsg(708,"Cancel"));
		btnCancel.setHotKey(0, IKeys.ESCAPE);
		addNext(btnCancel,HSTRETCH,LEFT);
		btnOK = new mButton(MyLocale.getMsg(1124,"Next"));
		btnOK.setHotKey(0, IKeys.ENTER);
		addLast(btnOK,HSTRETCH,HFILL|RIGHT);
		this.setPreferredSize(240,-1);
		this.baseDir=baseDir;
	}
	
	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == btnCancel){
				this.close(-1);
			}
			if (ev.target == btnOK){
				profileDir=inpDir.getDisplayText();
				if (profileDir.equalsIgnoreCase("maps")) {
					MessageBox mb=new MessageBox(MyLocale.getMsg(321,"Error"),MyLocale.getMsg(1122,"'maps' is reserved for the maps directory."),MBOK);
					mb.execute();
					profileDir="";
				} else {
					File f=new FileBugfix(baseDir+profileDir);
					if (f.exists()) {
						MessageBox mb=new MessageBox(MyLocale.getMsg(321,"Error"),MyLocale.getMsg(1114,"Directory exists already."),MBOK);
						mb.execute();
						profileDir="";
					} else {
						if (profileDir.indexOf("/")>=0 || profileDir.indexOf("\\")>=0 || !f.createDir()) {
							MessageBox mb=new MessageBox(MyLocale.getMsg(321,"Error"),MyLocale.getMsg(1113,"Cannot create directory"),MBOK);
							mb.execute();
							profileDir="";
							this.close(-1);
						}
						Global.getProfile().setFilterActive(Filter.FILTER_INACTIVE);
						this.close(0);
					}
				}
			}
		}
		super.onEvent(ev);
	}
}
