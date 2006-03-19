package CacheWolf;

import ewe.ui.*;
import ewe.fx.*;
import ewe.sys.*;
import ewe.util.*;
import ewe.io.*;

public class GeoToadUI extends Form{
	mLabel 	lblLogin,	lblPass, 	lblDist;
	mInput	inpLogin,	inpPass,	inpDist;
	mButton btCancel, btOK;
	
	public GeoToadUI(){
		this.title = "GeoToad UI";
		this.addNext(lblLogin = new mLabel("GC.com login:"),this.DONTSTRETCH, (this.DONTFILL|this.NORTHWEST));
		this.addLast(inpLogin = new mInput(),this.DONTSTRETCH, (this.DONTFILL|this.NORTHWEST));
		
		this.addNext(lblPass = new mLabel("Password:"),this.DONTSTRETCH, (this.DONTFILL|this.NORTHWEST));
		this.addLast(inpPass = new mInput(),this.DONTSTRETCH, (this.DONTFILL|this.NORTHWEST));
		
		this.addNext(lblDist = new mLabel("Distance:"),this.DONTSTRETCH, (this.DONTFILL|this.NORTHWEST));
		this.addLast(inpDist = new mInput(),this.DONTSTRETCH, (this.DONTFILL|this.NORTHWEST));
		
		this.addNext(btCancel = new mButton("Cancel"));
		this.addLast(btOK = new mButton("Go!"));
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
				this.close(0);
			}
		}
	}
}