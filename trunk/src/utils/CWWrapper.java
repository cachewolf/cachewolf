package utils;
import ewe.sys.*;

/**
 * Class built as a wrapper for the buggy Vm.exec
 * in EWE Version 1.49
 * It identifies the plattform and uses a different call
 * to Vm.exec() depending on the plattform.
 * Bugs identified:
 * ewe.jar: cmd needs quoting, args are whitespace-split
 * Ewe VM:  cmd must not be quoted, args can be only one
 */
public class CWWrapper {
	/**
	 * It doesn't work at all on Loox N520 with WM 5 :-(
	 * @param cmd
	 * @param args
	 * @throws ewe.io.IOException
	 */
	public static void exec(String cmd, String args) throws ewe.io.IOException{
		if (Vm.getPlatform().equals("Java")) {
			/* we need extra quotes here, see ewe/sys/Vm.java */
			cmd = "\"" + cmd + "\"";
			args = "\"" + args + "\"";
		}
		Vm.exec(cmd, args, 0, false);
	}
}
