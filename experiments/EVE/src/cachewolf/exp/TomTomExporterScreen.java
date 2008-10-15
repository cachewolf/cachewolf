package cachewolf.exp;

import cachewolf.MyLocale;
import eve.ui.*;
import eve.fx.gui.IKeys; 
import eve.ui.event.ControlEvent;
import eve.sys.Event;

public class TomTomExporterScreen extends Form {
	Button cancelB, okB;
	CheckBox chkASC, chkOV2, chkOneFilePerType;
	CheckBoxGroup chkGroupFormat;
	Input inpPrefix;
	
	public TomTomExporterScreen(String title){
		super();
		this.title = title;

		// checkboxgroup for fileformat
		chkGroupFormat = new CheckBoxGroup();
		chkASC = new CheckBox(".asc");
		chkASC.setGroup(chkGroupFormat);
		chkOV2 = new CheckBox(".ov2");
		chkOV2.setGroup(chkGroupFormat);
		chkGroupFormat.selectIndex(TomTomExporter.TT_OV2);
		
		this.addLast(new Label("Fileformat"));
		this.addNext(chkASC);
		this.addLast(chkOV2);
		
		// checkbox for one file for all or one file per cachetype
		chkOneFilePerType = new CheckBox("Eine Datei pro Cachetyp");
		chkOneFilePerType.setState(true);
		this.addLast(chkOneFilePerType);
		
		//prefix for files, if one file per cachetype
		inpPrefix = new Input("GC-");
		activateInpPrefix();
		this.addLast(inpPrefix);
		
		// cancel and ok Button
		cancelB = new Button(MyLocale.getMsg(1604,"Cancel"));
		cancelB.setHotKey(0, IKeys.ESCAPE);
		this.addNext(cancelB,CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		okB = new Button(MyLocale.getMsg(1605,"OK"));
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
				this.close(Form.IDCANCEL);
			}
			if (ev.target == okB){
				this.close(Form.IDOK);
			}
			if (ev.target == chkOneFilePerType){
				activateInpPrefix(); 
			}
		}
		super.onEvent(ev);
	}

}
