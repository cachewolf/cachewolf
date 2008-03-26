/**
 * 
 */
package utils;

import ewe.io.*;
import ewe.util.FileComparer;
import ewe.util.mString;
/**
 * @author pfeffer
 * class to fix a bug in ewe.io.File, which occurs only on some systems (e.g. linux): the mask "*.xyz" doesn't work
 * so I get all the files wich null in spite of the mask and filter afterwords 
 */
public class FileBugfix extends File{
	public FileBugfix(String path) 
	{super(path);}

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
		String[] found; //the following code is mainly copoed from FileBase.listmultiple to avoid recursion it is not called
		char c = mask.indexOf(',') == -1 ? ';' : ',';
		String masks [] = mString.split(mask,c);
		String dirs [] = new String[0];
		if ((listAndSortOptions & LIST_FILES_ONLY) == 0)
			dirs = super.list(null,LIST_DIRECTORIES_ONLY); // add dirs if not only asked for files
		if ((listAndSortOptions & LIST_DIRECTORIES_ONLY) == 0)
			found = super.list(null,FileBase.LIST_FILES_ONLY|listAndSortOptions); // add files if not dirs only
		else {
			found = dirs; // if dirs only -> aplpy masks to the dirs
			dirs = new String[0]; // this line is missing in ewe FileBase.listmultiple -> doubled dirs when using listmultiple with the option dirs_only
		}

		ewe.util.FileComparer [] fcs = new ewe.util.FileComparer[masks.length];

		for (int i = 0; i<masks.length; i++)
			fcs[i] = new FileComparer(this,ewe.sys.Vm.getLocale(),listAndSortOptions,masks[i]);
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
		for (int i = 0, d = dirs.length; i<found.length; i++)
			if (found[i] != null)
				isMatching[d++] = found[i];
		found = isMatching;
		return found;
	}
}
