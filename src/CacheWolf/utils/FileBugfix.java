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

import CacheWolf.STRreplace;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.util.FileComparer;
import ewe.util.mString;
/**
 * @author pfeffer
 * class to fix a bug in ewe.io.File, which occurs only on some systems (e.g. linux): the mask "*.xyz" doesn't work
 * so I get all the files which null in spite of the mask and filter afterwords
 */
public class FileBugfix extends File{
	File ewefile;
	public FileBugfix(String path) {
		super(STRreplace.replace(path, "//", "/"));
		ewefile = ewe.sys.Vm.newFileObject();
		ewefile.set(null, name);
	}
	
	public FileBugfix(String path, String fName) {
		this(path + "/" + fName);
	}


	public void set(File dir, String name) {
		ewefile.set(dir, STRreplace.replace(name, "//", "/")); // this or null?
	}

	public String[] list(final String mask,final int listAndSortOptions)
	{
		return listBugFixed(mask, listAndSortOptions);
		/*
		 * super.list has on some systems (linux, but also Windows Mobile 2003) a bug 
		 * in ewe 1.49: 
		 * when filenames contain more than one ".", 
		 * then the mask matches on these systems the first in spite of the last "."
		 * listmultiple doesn't have this bug 
		 */
	}
	public String [] listMultiple(final String compositeMask,final int listAndSortOptions) {
		/* super.listMultiple in ewe 1.49
		 * usually works correct, but when called with Option Dirs_Only, it gives the dirs 
		 * twice (once filtered by mask, once all)
		 */
		return listBugFixed (compositeMask, listAndSortOptions);
	}

	public String[] listBugFixed(final String compositeMask,final int listAndSortOptions) {
		String mask = (compositeMask == null) ? "*.*" : compositeMask;
		String[] found; //the following code is mainly copied from FileBase.listmultiple to avoid recursion it is not called
		char c = mask.indexOf(',') == -1 ? ';' : ',';
		String masks [] = mString.split(mask,c);
		String dirs [] = new String[0];
		if ((listAndSortOptions & LIST_FILES_ONLY) == 0)
			dirs = ewefile.list(null,LIST_DIRECTORIES_ONLY); // add dirs if not only asked for files
		if ((listAndSortOptions & LIST_DIRECTORIES_ONLY) == 0)
			found = ewefile.list(null,FileBase.LIST_FILES_ONLY|listAndSortOptions); // add files if not dirs only
		else {
			found = dirs; // if dirs only -> apply masks to the dirs
			dirs = new String[0]; // this line is missing in ewe FileBase.listmultiple -> doubled dirs when using listmultiple with the option dirs_only
		}
		if (found == null) return null;
		ewe.util.FileComparer [] fcs = new ewe.util.FileComparer[masks.length];

		for (int i = 0; i<masks.length; i++)
			fcs[i] = new FileComparer(ewefile,ewe.sys.Vm.getLocale(),listAndSortOptions,masks[i]);
		int left = found.length;
		for (int i = 0; i<found.length; i++){
			boolean matched = false;
			for (int f = 0; f<fcs.length; f++){
				if (fcs[f].matches(found[i])){
					matched = true;
					break;
				}
			}
			if (!matched) {
				found[i] = null;
				left--;
			}
		}
		String [] isMatching = new String[dirs.length+left];
		ewe.sys.Vm.copyArray(dirs,0,isMatching,0,dirs.length);
		for (int i = 0; i < dirs.length; i++)
			isMatching[i]=isMatching[i].replace('\\', '/'); // on some PDAs a "\" in the path seems to make problems, but it seems that is ewe (files.list) returns sometimes a path containing a "\"
		for (int i = 0, d = dirs.length; i<found.length; i++)
			if (found[i] != null)
				isMatching[d++] = found[i].replace('\\', '/'); // on some PDAs a "\" in the path seems to make problems, but it seems that is ewe (files.list) returns sometimes a path containing a "\"
		found = isMatching;
		return found;
	}

	/**
	 * this is needed in order to be able to use the simulated 
	 * file system _filesystem.zip when running as applet 
	 */
	public boolean exists() {
		return (ewefile.exists());
	}

	public boolean isDirectory() {
		return (ewefile.isDirectory());
	}

	public boolean createDir() {
		return (ewefile.createDir());

	}
	public boolean delete() {
		return (ewefile.delete());
	}
	public int getLength() {
		return (ewefile.getLength());
	}

	public String getDrivePath() {
		return (ewefile.getDrivePath());
	}

	public String getAbsolutePath() {
		String fullPath = ewefile.getFullPath();
		int rel = fullPath.indexOf("/..");
		while (rel > 0) {
			int parent = fullPath.lastIndexOf('/', rel-1);
			if (parent == -1) {
				break;
			}
			fullPath = fullPath.substring(0, parent) + fullPath.substring(rel+3);

			rel = fullPath.indexOf("/..");
		}
		return fullPath;
	}



}
