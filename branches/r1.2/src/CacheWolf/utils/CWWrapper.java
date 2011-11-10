/*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
	See http://www.cachewolf.de/ for more information.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package CacheWolf.utils;

import ewe.sys.Vm;

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
public final class CWWrapper {

	/**
	 * thou shallst not instantiate this object
	 */
	private CWWrapper() {
		// Nothing to do
	}


	/**
	 * Apply needed quotes around the command or the argument,
	 * then call Vm.exec() appropriately.
	 * @param cmd
	 * @param arg (only one argument)
	 * @throws ewe.io.IOException
	 */
	public static int exec(final String cmd, final String arg) throws ewe.io.IOException{
		return exec(cmd, arg, false, true);
	}
	public static int exec(String cmd, String arg, final boolean wait, final boolean surround) throws ewe.io.IOException {
		// works if there is only one argument
		if (surround) {
			if (Vm.getPlatform().equals("WinCE") || Vm.getPlatform().equals("Win32"))
			{
				/* we need extra quotes here, see vm/nmwin32_c.c */
				if (arg.indexOf(' ') > -1) {
					arg = "\"" + arg + "\"";
				}
			} else if (Vm.getPlatform().equals("Java")) {
				/* on win32 we need extra quotes here to support filenames whith spaces
				 * (see ewe/sys/Vm.java)			 *
				 * on linux (and os x?) we must not have extra quotes, filenames with spaces are unsupported
				 * */
				if (cmd.indexOf(':') == 1) {
					// java on Windows
					if (cmd.indexOf(' ') > -1) {
						cmd = "\"" + cmd + "\"";
					}
				}
				if (arg.indexOf(' ') > -1) {
					arg = "\"" + arg + "\"";
				}
			}
		}
		return Vm.exec(cmd, arg, 0, wait);
	}

}
