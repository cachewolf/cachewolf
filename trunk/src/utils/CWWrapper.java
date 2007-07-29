package utils;
import CacheWolf.*;
import ewe.sys.*;

/**
 * Class built as a wrapper for the buggy Vm.exec
 * in EWE Version 1.49
 * It identifies the plattform and uses a different call
 * to Vm.exec() depending on the plattform.
 * It uses depreciated classes, which is OK for now.
 */
public class CWWrapper {
	
	public static exec(String str) throws IOException{
		String plattform = Vm.getPlatform();
		if(plattform.equals("WinCE")){
			Vm.exec(str, "", 0, true);
		}
		if(plattform.equals("Win32")){
			Vm.exec(str, "", 0, true);
		}
		if(plattform.equals("Unix")){
			String[] strarr = new String[1];
			strarr[0] = str;
			String[] dummy = new String[1];
			Vm.exec(strarr, dummy);
		}
		if(plattform.equals("Java")){
			ewe.sys.Process p = Vm.exec(str);
			p.waitFor();
		}
	}
}