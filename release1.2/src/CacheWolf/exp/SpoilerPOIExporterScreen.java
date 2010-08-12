package CacheWolf.exp;

import CacheWolf.MyLocale;
import ewe.ui.*;

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
