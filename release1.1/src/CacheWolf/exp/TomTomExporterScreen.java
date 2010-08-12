package CacheWolf.exp;

import CacheWolf.MyLocale;
import ewe.ui.*;

public class TomTomExporterScreen extends Form {
	mButton cancelB, okB;
	mCheckBox chkASC, chkOV2, chkOneFilePerType;
	CheckBoxGroup chkGroupFormat;
	mInput inpPrefix;
	
	public TomTomExporterScreen(String title){
		super();
		this.title = title;

		// checkboxgroup for fileformat
		chkGroupFormat = new CheckBoxGroup();
		chkASC = new mCheckBox(".asc");
		chkASC.setGroup(chkGroupFormat);
		chkOV2 = new mCheckBox(".ov2");
		chkOV2.setGroup(chkGroupFormat);
		chkGroupFormat.selectIndex(TomTomExporter.TT_OV2);
		
		this.addLast(new mLabel("Fileformat"));
		this.addNext(chkASC);
		this.addLast(chkOV2);
		
		// checkbox for one file for all or one file per cachetype
		chkOneFilePerType = new mCheckBox("Eine Datei pro Cachetyp");
		chkOneFilePerType.setState(true);
		this.addLast(chkOneFilePerType);
		
		//prefix for files, if one file per cachetype
		inpPrefix = new mInput("GC-");
		activateInpPrefix();
		this.addLast(inpPrefix);
		
		// cancel and ok Button
		cancelB = new mButton(MyLocale.getMsg(1604,"Cancel"));
		cancelB.setHotKey(0, IKeys.ESCAPE);
		this.addNext(cancelB,CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		okB = new mButton(MyLocale.getMsg(1605,"OK"));
		okB.setHotKey(0, IKeys.ACTION);
		okB.setHotKey(0, IKeys.ENTER);
		this.addLast(okB,CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
	}
	
	public int getFormat() {
		return chkGroupFormat.getSelectedIndex();
	}
	
	public boolean oneFilePerType(){
		return chkOneFilePerType.getState();
	}
	
	public String getPrefix(){
		return inpPrefix.getText();
	}
	
	private void activateInpPrefix(){
		if (chkOneFilePerType.getState()) inpPrefix.modify(0, ControlConstants.Disabled);
		else inpPrefix.modify(ControlConstants.Disabled,0);
		inpPrefix.repaintNow();
	}

	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == cancelB){
				this.close(FormBase.IDCANCEL);
			}
			if (ev.target == okB){
				this.close(FormBase.IDOK);
			}
			if (ev.target == chkOneFilePerType){
				activateInpPrefix(); 
			}
		}
		super.onEvent(ev);
	}

}
