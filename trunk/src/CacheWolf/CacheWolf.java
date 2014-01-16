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
package CacheWolf;

import CacheWolf.controls.InfoBox;
import ewe.sys.Device;
import ewe.ui.Editor;
import ewe.ui.FormBase;

public class CacheWolf {
    public static void main(String args[]) {
	ewe.sys.Vm.startEwe(args);

	// get program command line parameters and switches
	// Vm.getProgramArguments(); <-- only works in eclipse, but mixes the letters in the ewe-vm (tested in ewe-1.49 on win xp)
	String configfile = null;
	boolean debug = false;
	if (args.length > 0) {
	    for (int i = 0; i < args.length; i++) {
		if (args[i] != null && args[i].length() > 1 && (args[i].startsWith("-") || args[i].startsWith("/"))) {
		    String c = args[i].substring(1, args[i].length());
		    if (c.equalsIgnoreCase("c")) {
			if (i < args.length - 1) {
			    configfile = args[i + 1];
			    i++;
			} else {
			    new InfoBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(7200, "Usage: CacheWolf [-c <path to pref.xml>] [-debug]")).wait(FormBase.OKB);
			    // return usage info
			    ewe.sys.Vm.exit(1);
			}
		    }
		    if (c.equalsIgnoreCase("debug")) {
			debug = true;
		    }

		}
	    }
	}
	// debug = true will permanently set this in pref.xml
	// !!! debug = false will be overwritten by reading pref.xml
	Editor mainForm = new MainForm(debug, configfile);
	Device.preventIdleState(true);
	mainForm.execute();
	Device.preventIdleState(false);
	ewe.sys.Vm.exit(0);
    }

}

// for javadoc see: http://java.sun.com/j2se/javadoc/writingdoccomments/index.html#exampleresult
// or the local files "JavaDoc" directory
// Javadoc Main Page: http://java.sun.com/j2se/javadoc/index.jsp
// javadoc -classpath ewe.jar -d "cachewolf doc" cachewolf/*.java
