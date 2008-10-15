package cachewolf;

import eve.io.File;
import eve.ui.event.ControlEvent;
import eve.ui.Form;
import eve.fx.gui.IKeys;
import eve.sys.Event;
import eve.ui.MessageBox;
import eve.ui.Button;
import eve.ui.Input;

public class NewProfileForm extends Form {
	private Button btnCancel,btnOK;
	private Input inpDir;
	public String profileDir;
	private String baseDir;
	//private Profile profile;

	public NewProfileForm (String baseDir) {
        super();
		//profile=prof;
        title = MyLocale.getMsg(1111,"Create new profile:");
		addLast(inpDir=new Input(MyLocale.getMsg(1112,"New profile name")),HSTRETCH,HFILL|LEFT);
		btnCancel = new Button(MyLocale.getMsg(708,"Cancel"));
		btnCancel.setHotKey(0, IKeys.ESCAPE);
		addNext(btnCancel,HSTRETCH,LEFT);
		btnOK = new Button(MyLocale.getMsg(1605,"OK"));
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
				File f=new File(baseDir+profileDir);
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
					Global.getProfile().filterActive=Filter.FILTER_INACTIVE;
					Global.getProfile().filterInverted=false;
					this.close(0);
				}
			}
		}
		super.onEvent(ev);
	}
}
