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
package CacheWolf.exp;

import CacheWolf.MyLocale;
import ewe.ui.CellConstants;
import ewe.ui.CheckBoxGroup;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.IKeys;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;

public class SpoilerPOIExporterScreen extends Form {
	mButton cancelB, okB;
	mCheckBox chkAllPic, chkOnlySpoiler;
	CheckBoxGroup chkGroupFormat;
	
	public SpoilerPOIExporterScreen(String title){
		super();
		this.title = title;

		// checkboxgroup for all pictures or Spoiler only
		chkGroupFormat = new CheckBoxGroup();
		chkAllPic = new mCheckBox("all Pics"); 
		chkAllPic.setGroup(chkGroupFormat);
		chkOnlySpoiler = new mCheckBox("only Spoiler");
		chkOnlySpoiler.setGroup(chkGroupFormat);
		chkGroupFormat.selectIndex(1);
		
		this.addNext(chkAllPic);
		this.addLast(chkOnlySpoiler);
		
		// cancel and ok Button
		cancelB = new mButton(MyLocale.getMsg(1604,"Cancel"));
		cancelB.setHotKey(0, IKeys.ESCAPE);
		this.addNext(cancelB,CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		okB = new mButton(MyLocale.getMsg(1605,"OK"));
		okB.setHotKey(0, IKeys.ACTION);
		okB.setHotKey(0, IKeys.ENTER);
		this.addLast(okB,CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
	}
	
	public boolean getOnlySpoiler() {
		if ( chkGroupFormat.getSelectedIndex() == 1) return true;
		else return false;
	}
	

	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == cancelB){
				this.close(FormBase.IDCANCEL);
			}
			if (ev.target == okB){
				this.close(FormBase.IDOK);
			}
		}
		super.onEvent(ev);
	}

}
