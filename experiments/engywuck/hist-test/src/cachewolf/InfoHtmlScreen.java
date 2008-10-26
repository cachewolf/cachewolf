package cachewolf;

import eve.ui.*;
import eve.io.File;
import eve.sys.*;
import eve.ui.formatted.HtmlDisplay;
import eve.ui.event.ControlEvent;


/**
*	This class displays an information screen. It loads the html text to display
*	from a file that is given upon creation of this class. It offers
*	a cancel button enabling the user to close the screen and return to
*	wherever the user was before
*/
public class InfoHtmlScreen extends Form {
	
	HtmlDisplay disp = new HtmlDisplay();
	Button btCancel;
	
	public InfoHtmlScreen(String datei, String tit, boolean readFromFile){
		String text = "";
		this.title=tit;
		Preferences pref = Global.getPref();
		this.setPreferredSize(pref.myAppWidth, pref.myAppHeight);
		if(readFromFile == true){
			try{
				char buf[]=new char[(int) (new File(datei)).getLength()];
				java.io.InputStreamReader in = new java.io.InputStreamReader(new java.io.FileInputStream(datei),"UTF8");
				int len=in.read(buf);
				in.close();
				eve.util.CharArray ca=new eve.util.CharArray(buf); ca.setLength(len);
				text=(ca).toString();
				//BufferedReader in = new BufferedReader(new FileReader(datei));
				//text = in.readAll();
				//in.close();
			}catch(Exception ex){
				//Vm.debug("Error! Could not open " + datei);
			}
		} else text = datei;
		disp.setHtml(text);
		ScrollBarPanel sbp = new MyScrollBarPanel(disp, ScrollBarPanel.NeverShowHorizontalScrollers);
		this.addLast(sbp);
		this.addLast(btCancel = new Button(MyLocale.getMsg(3000,"Close")),CellConstants.DONTSTRETCH, CellConstants.FILL);
	}
	
	public void onEvent(Event ev){
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == btCancel){
				this.close(0);
			}
		}
	}
}
