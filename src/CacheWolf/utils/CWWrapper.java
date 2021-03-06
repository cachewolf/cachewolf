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

import ewe.io.StreamReader;
import ewe.sys.Process;
import ewe.sys.Vm;

/**
 * Class built as a wrapper for the buggy^Winconsistent Vm.exec in EWE Version 1.49
 * It identifies the plattform and uses a different call to Vm.exec() depending on the plattform. Bugs identified: ewe.jar: cmd needs quoting, arg are
 * whitespace-split Unix VM: cmd must not be quoted, arg can be only one Win* VM: cmd may be quoted, arg are whitespace-split
 */
public final class CWWrapper {

    /**
     * thou shallst not instantiate this object
     */
    private CWWrapper() {
        // Nothing to do
    }

    /**
     * Apply needed quotes around the command or the argument, then call exec() /execute() appropriately.
     *
     * @param cmd
     * @param arg (only one argument)
     * @throws ewe.io.IOException
     */
    public static int exec(final String cmd, final String arg) throws ewe.io.IOException {
        return exec(cmd, arg, false, true);
    }

    private static int exec(String cmd, String arg, final boolean wait, final boolean surround) throws ewe.io.IOException {
        // works if there is only one argument
        if (surround) {
            if (Vm.getPlatform().equals("WinCE") || Vm.getPlatform().equals("Win32")) {
                // on pda surrounding quotes " will be converted to %22 (and a %22file:// does not work)
                /* we need extra quotes here, see vm/nmwin32_c.c */
                if (arg.indexOf(' ') > -1) {
                    arg = "\"" + arg + "\"";
                }
            } else if (Vm.getPlatform().equals("Java")) {
                /*
                 * on win32 we need extra quotes here to support filenames whith spaces (see ewe/sys/Vm.java)
                 * on linux (and os x?) we must not have extra quotes, filenames with spaces are unsupported
                 */
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

        if (wait) {
            if (execute(cmd + " " + arg))
                return 0;
            else
                return 1;
        } else {
            if (exec(cmd + " " + arg))
                return 0;
            else
                return 1;
        }

    }

    /**
     * Execute the command defined by cmd
     *
     * @param cmd command and options to execute.
     * @return a handle to the process on success or null otherwise
     */
    public static boolean exec(String cmd) {
        try {
            if (cmd == null) {
                return false;
            }
            if (cmd.length() == 0) {
                return false;
            }

            //Process p =
            Vm.exec(cmd);
	    /*
	    StreamReader errorStream = new StreamReader(p.getErrorStream());
	    String errorMsg = errorStream.readALine();
	    if (errorMsg != null) {
	    Preferences.itself().log("execute: " + cmd + errorMsg, null);
	    return false;
	    }
	    errorStream.close();
	    */
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Execute the command defined by cmd with wait for exit of command
     *
     * @param cmd command and options to execute.
     * @return a handle to the process on success or null otherwise
     */
    public static boolean execute(String cmd) {
        try {
            if (cmd == null) {
                return false;
            }
            if (cmd.length() == 0) {
                return false;
            }

            Process p = Vm.exec(cmd);
            p.waitFor();
            StreamReader errorStream = new StreamReader(p.getErrorStream());
            String errorMsg = errorStream.readALine();
            if (errorMsg != null) {
                return false;
            }
            errorStream.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

}
