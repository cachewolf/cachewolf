package utils;
import ewe.sys.*;

/**
 * Class built as a wrapper for the buggy^Winconsistent Vm.exec
 * in EWE Version 1.49
 * It identifies the plattform and uses a different call
 * to Vm.exec() depending on the plattform.
 * Bugs identified:
 * ewe.jar: cmd needs quoting, arg are whitespace-split
 * Unix VM: cmd must not be quoted, arg can be only one
 * Win* VM: cmd may be quoted, arg are whitespace-split
 */
public class CWWrapper {
	/**
	 * Apply needed quotes around the command or the argument,
	 * then call Vm.exec() appropriately.
	 * @param cmd
	 * @param arg (only one argument)
	 * @throws ewe.io.IOException
	 */
	public static void exec(String cmd, String arg) throws ewe.io.IOException{
		if (Vm.getPlatform().equals("WinCE") ||
		    Vm.getPlatform().equals("Win32")) {
			/* we need extra quotes here, see vm/nmwin32_c.c */
			if (arg.indexOf(" ") > -1)
				arg = "\"" + arg + "\"";
		} else if (Vm.getPlatform().equals("Java")) {
			/* on win32 we need extra quotes here to support filenames whith spaces
			 * (see ewe/sys/Vm.java)			 *
			 * on linux (and os x?) we must not have extra quotes, filenames with spaces are unsupported
			 * */
			if (cmd.indexOf(" ") > -1)
				cmd = "\"" + cmd + "\"";
			if (arg.indexOf(" ") > -1)
				arg = "\"" + arg + "\"";
		}
		Vm.exec(cmd, arg, 0, false);
	}
}
