package CacheWolf;

import ewe.ui.*;
import ewe.sys.*;

public class CacheWolf extends Editor{
	
	
	public static void main(String args[])
	{
		//start with parameters:
		//args[0]: spider
		//args[1]: distance
		ewe.sys.Vm.startEwe(args);
/*		Gui.screenIs(Gui.PDA_SCREEN);
		Rect s = (Rect)Window.getGuiInfo(Window.INFO_SCREEN_RECT,null,new Rect(),0);
		//Gui.screenIs(Gui.PDA_SCREEN)
		if (Vm.isMobile() && s.height >= 400) {
			Font defaultGuiFont = mApp.findFont("gui");
			int sz = (int)(defaultGuiFont.getSize());
			Font newGuiFont = new Font(defaultGuiFont.getName(), defaultGuiFont.getStyle(), sz); 
			mApp.addFont(newGuiFont, "gui"); 
			mApp.fontsChanged();
			mApp.mainApp.font = newGuiFont;
		}
*/		
		if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
			Vm.setSIP(Vm.SIP_LEAVE_BUTTON);
		}
		
		if(args.length > 0){
			if(args[0].equals("test")){
				Test t=new Test(); 
				t.testAll();
			}
		}
		Editor mainF = new MainForm();
		Device.preventIdleState(true);
		mainF.execute();
		Device.preventIdleState(false);
		ewe.sys.Vm.exit(0);
	}
	
}

// for javadoc see: http://java.sun.com/j2se/javadoc/writingdoccomments/index.html#exampleresult
// or the local files "JavaDoc" directory
// Javadoc Main Page: http://java.sun.com/j2se/javadoc/index.jsp
// javadoc -classpath ewe.jar -d "cachewolf doc" cachewolf/*.java
