package utils;
import ewe.sys.*;

/**
 * Class built as a wrapper for the buggy Vm.exec
 * in EWE Version 1.49
 * It identifies the plattform and uses a different call
 * to Vm.exec() depending on the plattform.
 * It uses depreciated classes, which is OK for now.
 */
public class CWWrapper {
	
	/**
	 * It doesn't work at all on Loox N520 with WM 5 :-(
	 * @param cmd
	 * @param args
	 * @throws ewe.io.IOException
	 */
	public static void exec(String cmd, String args) throws ewe.io.IOException{
		String plattform = Vm.getPlatform();
		if(plattform.equals("WinCE")){
			Vm.exec(cmd, args, 0, false);
		}
		if(plattform.equals("Win32")){
			Vm.exec(cmd, args, 0, false);
		}
		if(plattform.equals("Unix")){
			String[] strarr = new String[1];
			strarr[0] = "\"" + cmd + "\" "+args;
			String[] dummy = new String[1];
			Vm.exec(strarr, dummy);
		}
		if(plattform.equals("Java")){
			Vm.exec("\"" + cmd + "\" "+args, null);
		}
	}
}