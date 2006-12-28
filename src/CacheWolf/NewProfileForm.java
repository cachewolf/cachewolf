package CacheWolf;

import ewe.io.File;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.MessageBox;
import ewe.ui.mButton;
import ewe.ui.mInput;

public class NewProfileForm extends Form {
	private mButton btnCancel,btnOK;
	private mInput inpDir;
	public String profileDir;
	private String baseDir;
	//private Profile profile;
	
	public NewProfileForm (String baseDir) {
        super();
		//profile=prof;
        title = MyLocale.getMsg(1111,"Create new profile:");
		addLast(inpDir=new mInput(MyLocale.getMsg(1112,"New profile name")),HSTRETCH,HFILL|LEFT);
		addNext(btnCancel=new mButton(MyLocale.getMsg(708,"Cancel")),HSTRETCH,LEFT);
		addLast(btnOK=new mButton(MyLocale.getMsg(1605,"OK")),HSTRETCH,HFILL|RIGHT);
		this.setPreferredSize(240,50);
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
					MessageBox mb=new MessageBox(MyLocale.getMsg(321,"Error"),MyLocale.getMsg(1114,"Directory exists already."),IDOK);
					mb.execute();
					profileDir="";
				} else {
					if (!f.createDir()) {
						MessageBox mb=new MessageBox(MyLocale.getMsg(321,"Error"),MyLocale.getMsg(1113,"Cannot create directory"),IDOK);
						mb.execute();
						profileDir="";
						this.close(-1);
					}
					this.close(0);
				}
			}
		}
		super.onEvent(ev);
	}
}
