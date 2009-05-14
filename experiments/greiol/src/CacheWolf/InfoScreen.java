package CacheWolf;

import ewe.ui.*;
import ewe.io.*;

/**
*	This class displays an information screen. It loads the html text to display
*	from a file that is given upon creation of this class. It offers
*	a cancel button enabling the user to close the screen and return to
*	wherever the user was before
*	Class ID = 3000
*/
public class InfoScreen extends Form {
	
	HtmlDisplay disp = new HtmlDisplay();
	mButton btCancel;
	Preferences pref;
	
	public InfoScreen(String datei, String tit, boolean readFromFile, Preferences p){
		pref = p;
		String myText = new String();
		this.setTitle(tit);
		this.setPreferredSize(pref.myAppWidth, pref.myAppHeight);
		if(readFromFile == true){
			try{
				FileReader in = new FileReader(datei);
				myText = in.readAll();
				in.close();
			}catch(Exception ex){
				//Vm.debug("Error! Could not open " + datei);
			}
		} else myText = datei;
		disp.setHtml(myText);
		ScrollBarPanel sbp = new MyScrollBarPanel(disp, ScrollablePanel.NeverShowHorizontalScrollers);
		this.addLast(sbp);
		this.addLast(btCancel = new mButton(MyLocale.getMsg(3000,"Close")),CellConstants.DONTSTRETCH, CellConstants.FILL);
	}
	
	public void onEvent(Event ev){
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == btCancel){
				this.close(0);
			}
		}
	}
}
