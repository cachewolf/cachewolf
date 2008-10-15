package cachewolf; // REV 1332 / 1360

import eve.ui.data.*;
import eve.ui.*;
import eve.sys.*;
/**
*	This is the application starter class.
*	It startes the eve VM and creates the main form that displays
*	the user interface.
*	@param 	null	no parameters required.
*	@return null	does not return any return codes
*	@see			MainForm
*	@author	 Marc Schnitzler
*	@version version of this class, Date: date of release of the version of the class
*/


public class CacheWolf extends Editor{


	public static void main(String vmArgs[])
	{
		//start with parameters:
		//args[0]: spider
		//args[1]: distance
		Application.startApplication(vmArgs);
		Gui.screenIs(Gui.PDA_SCREEN);
		//eve.io.File f=new eve.io.File("c:/TEMP/");
		//String [] files=f.listMultiple("*.*;*.zip;*.pdf",eve.io.File.LIST_DIRECTORIES_ONLY);
		//Gui.screenIs(Gui.PDA_SCREEN)
		/*if (Device.isMobile() && MyLocale.getScreenHeight() >= 400) {
			Font defaultGuiFont = Application.findFont("gui");
			int sz = 10; //(int)(defaultGuiFont.getSize());
			Font newGuiFont = new Font(defaultGuiFont.getName(), defaultGuiFont.getStyle(), sz);
			Application.addFont(newGuiFont, "gui");
			Application.mainApp.font = newGuiFont;
			Application.fontsChanged();

		}*/
		if (Gui.screenIs(Gui.PDA_SCREEN) && Device.isMobile()) {
			//TODO Vm.setSIP(Vm.SIP_LEAVE_BUTTON);
		}

        // get program command line parameters and switches
        String[] args = vmArgs; // Vm.getProgramArguments(); <-- only works in eclipse, but mixes the letters in the ewe-vm (tested in ewe-1.49 on win xp)
        String configfile = null;
        boolean debug = false;
        if(args.length > 0){
                if(args[0].equals("test")){
                        Test t=new Test();
                        t.testAll();
                }
                for (int i=0; i < args.length ; i++) {
                        Vm.debug("prog: " + args[i]);
                        Vm.debug("vm: " + vmArgs[i]);
                        if (args[i] != null && args[i].length() > 1 &&
                                        (args[i].startsWith("-") || args[i].startsWith("/")) ) {
                                String c = args[i].substring(1, args[i].length());
                                if (c.equalsIgnoreCase("c")) {
                                        if (i < args.length -1 ) {
                                                configfile = args[i+1];
                                                i++;
                                        } else {
                                                (new MessageBox("Error", MyLocale.getMsg(7200, "Usage: CacheWolf [-c <path to pref.xml>] [-debug]"), MessageBox.OKB)).execute();
                                                // return usage info
                                                eve.sys.Vm.exit(1);
                                        }
                                }
                                if (c.equalsIgnoreCase("debug")) {
                                        //Vm.debug("d");
                                        debug = true;
                                }

                        }
                }
        }
        if (debug) {
            Vm.debug("prg-args: " + args.length);
            Vm.debug("vm-args: " + vmArgs.length);
        }

		MainForm mainF = new MainForm(debug,configfile);
		Device.preventIdleState(true);
		mainF.execute();
		Device.preventIdleState(false);
		Application.exit(0);
	}

}

// for javadoc see: http://java.sun.com/j2se/javadoc/writingdoccomments/index.html#exampleresult
// or the local files "JavaDoc" directory
// Javadoc Main Page: http://java.sun.com/j2se/javadoc/index.jsp
// javadoc -classpath eve.jar -d "cachewolf doc" cachewolf/*.java
