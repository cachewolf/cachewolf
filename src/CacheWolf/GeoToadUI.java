package CacheWolf;

import ewe.ui.*;
import ewe.fx.*;
import ewe.sys.*;
import ewe.util.*;
import ewe.io.*;

public class GeoToadUI extends Form{
	mLabel 	lblLogin,	lblPass, 	lblDist;
	mInput	inpLogin,	inpPass,	inpDist;
	public mCheckBox chkImport, chkSpoilers;
	mButton btCancel, btOK;
	Preferences pref;
	String cwd = new String();
	InfoBox inf;
	public int waitTimer = 0;
	Vector DB;	
	ewe.sys.Process spd;
	
	public GeoToadUI(Preferences pref, String cwd, Vector DB){
		this.DB = DB;
		this.cwd = cwd;
		this.pref = pref;
		this.title = "GeoToad UI";
		this.addNext(lblLogin = new mLabel("GC.com login:"),this.DONTSTRETCH, (this.DONTFILL|this.NORTHWEST));
		this.addLast(inpLogin = new mInput(),this.DONTSTRETCH, (this.DONTFILL|this.NORTHWEST));
		
		this.addNext(lblPass = new mLabel("Password:"),this.DONTSTRETCH, (this.DONTFILL|this.NORTHWEST));
		this.addLast(inpPass = new mInput(),this.DONTSTRETCH, (this.DONTFILL|this.NORTHWEST));
		
		this.addNext(lblDist = new mLabel("Distance:"),this.DONTSTRETCH, (this.DONTFILL|this.NORTHWEST));
		this.addLast(inpDist = new mInput(),this.DONTSTRETCH, (this.DONTFILL|this.NORTHWEST));
		
		this.addNext(chkImport = new mCheckBox("Import"),this.DONTSTRETCH, (this.DONTFILL|this.NORTHWEST));
		this.addLast(chkSpoilers = new mCheckBox("Load Spoilers"),this.DONTSTRETCH, (this.DONTFILL|this.NORTHWEST));
		
		this.addNext(btCancel = new mButton("Cancel"));
		this.addLast(btOK = new mButton("Go!"));
	}
	
	public void ticked(int timerId, int elapsed){
		if(timerId == waitTimer){
			File ftest = new File(cwd + "/temp.gpx");
			if(ftest.exists()){
				Vm.cancelTimer(waitTimer);
				
				spd.destroy();
				inf.close(0);
				this.close(0);
			} else {
				//Vm.debug("timer checking...");
			}
		}
	}
	/**
	*	Method to react to a user input.
	*/
	public void onEvent(Event ev){
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if(ev.target == btCancel){
				this.close(0);
			}
			if(ev.target == btOK){
				String command = new String();
				command = "geotoad -u " + inpLogin.getText();
				command += " -p " + inpPass.getText();
				command += " -y " + inpDist.getText();
				command += " -q coord \"" + pref.mylgNS + " " + pref.mylgDeg + " " + pref.mylgMin + ", ";
				command += " " + pref.mybrWE + " " +pref.mybrDeg + " " + pref.mybrMin + "\"";
				command += " -o " + cwd + "/temp.gpx";
				try{
					inf = new InfoBox("Geotoad:", "...spider pages");
					inf.show();
					File temp = new File(cwd + "/temp.gpx");
					temp.delete();
					waitTimer = Vm.requestTimer(this, 1000);
					spd  = Vm.exec(command);
					
				}catch(Exception ex){
					Vm.debug("Cannot start geotoad!");
				}
			}
		}
	}
}
