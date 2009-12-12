package CacheWolf;

import ewe.ui.*;
import ewe.sys.*;

public class CacheWolf extends Editor{
	
	
	public static void main(String vmargs[])
	{
		//start with parameters:
		//args[0]: spider
		//args[1]: distance
		ewe.sys.Vm.startEwe(vmargs);
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
		
		// get program command line parameters and switches
		String[] args = vmargs; // Vm.getProgramArguments(); <-- only works in eclipse, but mixes the letters in the ewe-vm (tested in ewe-1.49 on win xp)
		String configfile = null;
		boolean debug = false;
		if(args.length > 0){
			/* 
			if(args[0].equals("test")){
				Test t=new Test(); 
				t.testAll();
			}
			*/
			for (int i=0; i < args.length ; i++) {
				// Vm.debug("prog: " + args[i]);
				// Vm.debug("vm: " + vmargs[i]);
				if (args[i] != null && args[i].length() > 1 &&
						(args[i].startsWith("-") || args[i].startsWith("/")) ) {
					String c = args[i].substring(1, args[i].length());
					if (c.equalsIgnoreCase("c")) {
						if (i < args.length -1 ) {
							configfile = args[i+1];
							i++;
						} else {
							(new MessageBox("Error", MyLocale.getMsg(7200, "Usage: CacheWolf [-c <path to pref.xml>] [-debug]"), FormBase.OKB)).execute();
							// return usage info
							ewe.sys.Vm.exit(1);
						}
					}
					if (c.equalsIgnoreCase("debug")) {
						//Vm.debug("d");
						debug = true;
					}

				}
			}
		}
		/*
		if (debug) {
			Vm.debug("prg-args: " + args.length);
			Vm.debug("vm-args: " + vmargs.length);
		}
		*/
		
		Editor mainF = new MainForm(debug, configfile);
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
